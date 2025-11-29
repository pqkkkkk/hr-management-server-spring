package org.pqkkkkk.hr_management_server.modules.profile.controller.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // dùng H2 memory
public class ProfileApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfileRepository profileRepository;

    private String existingUserId;

    @BeforeEach
    @Transactional
    void setUp() {
        // xóa hết dữ liệu cũ
        profileRepository.deleteAll();

        // tạo user mới cho mỗi test
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
        existingUserId = saved.getUserId(); // lấy id mới
    }

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
}
