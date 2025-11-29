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

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    @Transactional
    void setUp() {
        // Arrange: xóa dữ liệu cũ
        profileRepository.deleteAll();

        // Arrange: tạo user mới
        User user = User.builder()
                .fullName("Test User")
                .email("testuser@example.com")
                .role(null)
                .status(null)
                .gender(null)
                .position(null)
                .joinDate(LocalDate.now())
                .identityCardNumber("123456789")
                .phoneNumber("0123456789")
                .dateOfBirth(LocalDate.of(1990,1,1))
                .address("123 Test Street")
                .bankAccountNumber("111222333")
                .bankName("Test Bank")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .department(null)
                .build();

        User saved = profileRepository.save(user);
        existingUserId = saved.getUserId();
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
                .andExpect(jsonPath("$.data.fullName").value("Test User"))
                .andExpect(jsonPath("$.data.email").value("testuser@example.com"));
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
    // PATCH /for-employee: valid request -> 200
    // ---------------------------
   @Test
   void updateProfileForEmployee_success() throws Exception {
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
                .andExpect(jsonPath("$.data.email").value("updatedemail@example.com"))
                .andExpect(jsonPath("$.data.phoneNumber").value("0987654321"))
                .andExpect(jsonPath("$.data.address").value("456 Updated Street"));

        // Assert DB
        User updated = profileRepository.findById(existingUserId).orElseThrow();
        assert updated.getEmail().equals("updatedemail@example.com");
        assert updated.getPhoneNumber().equals("0987654321");
        assert updated.getAddress().equals("456 Updated Street");
        }



    // ---------------------------
    // PATCH /for-employee: invalid email -> 400
    // ---------------------------
    @Test
    void updateProfileForEmployee_missingFullName_shouldFail() throws Exception {
        // Arrange: invalid email
        String requestJson = """
                {
                  "fullName": null,
                  "emai": "",
                  "phoneNumber": "0987654321",
                  "address": "Some Street"
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
    // PATCH /for-employee: invalid phone -> 400
    // ---------------------------
    @Test
    void updateProfileForEmployee_invalidPhone_shouldFail() throws Exception {
        // Arrange: phoneNumber invalid
        String requestJson = """
                {
                  "fullName": null,
                  "emai": "email@.com",
                  "phoneNumber": "",
                  "address": "Some Street"
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
    // PATCH /for-employee: missing required fields -> 400
    // ---------------------------
    @Test
    void updateProfileForEmployee_missingRequiredFields_shouldFail() throws Exception {
        // Arrange: missing email
        String requestJson = """
                {
                  "phoneNumber": "0123456789",
                  "address": "Some Address",
                  "fullName": "Some Name"
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
}
