package org.pqkkkkk.hr_management_server.modules.profile.controller.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.Request.UpdateUserForEmployeeRequest;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.infrastructure.dao.jpa_repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProfileApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String existingUserId;

    @BeforeEach
    void setUp() {
        // Sử dụng sample data có sẵn từ Flyway migration
        // User: Nguyen Van An - EMPLOYEE role
        existingUserId = "u1a2b3c4-e5f6-7890-abcd-ef1234567890";
    }

    // ---------------------------
    // GET PROFILE
    // ---------------------------
    @Test
    void getMyProfile_success() throws Exception {

        mockMvc.perform(get("/api/v1/users/" + existingUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(existingUserId))
                .andExpect(jsonPath("$.data.fullName").value("Nguyen Van An"))
                .andExpect(jsonPath("$.data.email").value("nguyenvanan@company.com"));
    }

    @Test
    void getMyProfile_userNotFound() throws Exception {

        mockMvc.perform(get("/api/v1/users/nonexistent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

     // ---------------------------
    // PATCH /for-employee: valid request with all fields -> 200
    // ---------------------------
   @Test
   @Transactional
   void updateProfileForEmployee_withAllFields_success() throws Exception {
        // Arrange
        UpdateUserForEmployeeRequest request = new UpdateUserForEmployeeRequest(
                "Updated Name",
                "updatedemail@example.com",
                "0987654321",
                "456 Updated Street"
        );

        String requestJson = objectMapper.writeValueAsString(request);

        // Act
        var result = mockMvc.perform(patch("/api/v1/users/" + existingUserId + "/for-employee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.data.email").value("updatedemail@example.com"))
                .andExpect(jsonPath("$.data.phoneNumber").value("0987654321"))
                .andExpect(jsonPath("$.data.address").value("456 Updated Street"));

        // Assert DB
        User updated = profileRepository.findById(existingUserId).orElseThrow();
        assert updated.getFullName().equals("Updated Name");
        assert updated.getEmail().equals("updatedemail@example.com");
        assert updated.getPhoneNumber().equals("0987654321");
        assert updated.getAddress().equals("456 Updated Street");
        }
        
    // ---------------------------
    // PATCH /for-employee: partial update (only email) -> 200
    // ---------------------------
    @Test
    @Transactional
    void updateProfileForEmployee_onlyEmail_success() throws Exception {
        // Arrange: chỉ update email
        String requestJson = """
                {
                  "email": "newemail@example.com"
                }
                """;

        // Act
        var result = mockMvc.perform(patch("/api/v1/users/" + existingUserId + "/for-employee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("newemail@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("Nguyen Van An"))
                .andExpect(jsonPath("$.data.phoneNumber").value("0901234567"))
                .andExpect(jsonPath("$.data.address").value("123 Le Loi Street, District 1, Ho Chi Minh City"));
    }
    
    // ---------------------------
    // PATCH /for-employee: partial update (only phone) -> 200
    // ---------------------------
    @Test
    @Transactional
    void updateProfileForEmployee_onlyPhone_success() throws Exception {
        // Arrange: chỉ update phone
        String requestJson = """
                {
                  "phoneNumber": "0999888777"
                }
                """;

        // Act & Assert
        mockMvc.perform(patch("/api/v1/users/" + existingUserId + "/for-employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.phoneNumber").value("0999888777"))
                .andExpect(jsonPath("$.data.email").value("nguyenvanan@company.com"));
    }



    // ---------------------------
    // PATCH /for-employee: invalid email format -> 400
    // ---------------------------
    @Test
    void updateProfileForEmployee_invalidEmailFormat_shouldFail() throws Exception {
        // Arrange: invalid email format
        String requestJson = """
                {
                  "email": "invalid-email-format"
                }
                """;

        // Act & Assert
        mockMvc.perform(patch("/api/v1/users/" + existingUserId + "/for-employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    // ---------------------------
    // PATCH /for-employee: invalid phone format -> 400
    // ---------------------------
    @Test
    void updateProfileForEmployee_invalidPhoneFormat_shouldFail() throws Exception {
        // Arrange: phoneNumber không đúng format (phải 10 số bắt đầu bằng 0)
        String requestJson = """
                {
                  "phoneNumber": "123456"
                }
                """;

        // Act & Assert
        mockMvc.perform(patch("/api/v1/users/" + existingUserId + "/for-employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    // ---------------------------
    // PATCH /for-employee: user not found -> 404
    // ---------------------------
    @Test
    void updateProfileForEmployee_userNotFound_shouldFail() throws Exception {
        // Arrange
        String requestJson = """
                {
                  "email": "test@example.com"
                }
                """;

        // Act & Assert
        mockMvc.perform(patch("/api/v1/users/nonexistent-user-id/for-employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }
}
