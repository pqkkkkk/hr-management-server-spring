package org.pqkkkkk.hr_management_server.modules.profile.controller.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.pqkkkkk.hr_management_server.modules.profile.domain.dao.ProfileDao;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ProfileApi Integration Tests")
class ProfileApiIntegrationTest {

    private static final String baseUrl = "/users";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProfileDao profileDao;

    private User testUser;
    private String testUserId;

    @BeforeEach
    void setUp() {
        // Sample data is loaded from Flyway migration V3__insert_profile_sample_data.sql
        testUserId = "u1a2b3c4-e5f6-7890-abcd-ef1234567890";
        testUser = profileDao.getProfileById(testUserId);
        assertNotNull(testUser, "Test user should be loaded from migration script");
    }

    @Nested
    @DisplayName("Deactivate User Tests")
    class DeactivateUserTests {

        @Test
        @DisplayName("Should return 200 and deactivate user successfully with valid userId")
        void testDeactivateUser_WithValidUserId_Success() throws Exception {
            // Arrange
            assertNotNull(testUser, "User should exist before deactivation");

            // Act
            MvcResult result = mockMvc.perform(
                    patch(baseUrl + "/" + testUserId + "/deactivate")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String responseBody = result.getResponse().getContentAsString();
            assertThat(responseBody).isNotBlank();

            com.fasterxml.jackson.databind.JsonNode jsonResponse = objectMapper.readTree(responseBody);
            assertTrue(jsonResponse.get("success").asBoolean(), "Response should have success = true");
            assertEquals(200, jsonResponse.get("statusCode").asInt(), "Status code should be 200");
            assertThat(jsonResponse.get("message").asText()).contains("deactivated successfully");

            com.fasterxml.jackson.databind.JsonNode data = jsonResponse.get("data");
            assertNotNull(data, "Response data should not be null");
            assertEquals(testUserId, data.get("userId").asText());
            assertFalse(data.get("isActive").asBoolean(), "User should be deactivated (isActive = false)");

            // Verify persistence in database
            User deactivatedUser = profileDao.getProfileById(testUserId);
            assertNotNull(deactivatedUser, "Deactivated user should still exist in database");
            assertFalse(deactivatedUser.getIsActive(), "User should be deactivated in database");
        }

        @Test
        @DisplayName("Should return 400 with invalid userId format")
        void testDeactivateUser_WithInvalidUserIdFormat_ReturnsBadRequest() throws Exception {
            // Arrange
            String invalidUserId = "invalid-user-id-format";

            // Act & Assert
            MvcResult result = mockMvc.perform(
                    patch(baseUrl + "/" + invalidUserId + "/deactivate")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            assertThat(responseBody).isNotBlank();

            com.fasterxml.jackson.databind.JsonNode jsonResponse = objectMapper.readTree(responseBody);
            assertFalse(jsonResponse.get("success").asBoolean(), "Response should have success = false");
        }

        @Test
        @DisplayName("Should return 400 with non-existent userId")
        void testDeactivateUser_WithNonExistentUserId_ReturnsNotFound() throws Exception {
            // Arrange
            String nonExistentUserId = "00000000-0000-0000-0000-000000000000";

            // Act & Assert
            MvcResult result = mockMvc.perform(
                    patch(baseUrl + "/" + nonExistentUserId + "/deactivate")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            assertThat(responseBody).isNotBlank();

            com.fasterxml.jackson.databind.JsonNode jsonResponse = objectMapper.readTree(responseBody);
            assertFalse(jsonResponse.get("success").asBoolean(), "Response should have success = false");
        }

        @Test
        @DisplayName("Should return 400 when trying to deactivate already deactivated user")
        void testDeactivateUser_WithAlreadyDeactivatedUser_ReturnsBadRequest() throws Exception {
            // Arrange - First deactivate the user
            testUser.setIsActive(false);
            profileDao.updateProfile(testUser);

            // Act & Assert
            MvcResult result = mockMvc.perform(
                    patch(baseUrl + "/" + testUserId + "/deactivate")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            assertThat(responseBody).isNotBlank();

            com.fasterxml.jackson.databind.JsonNode jsonResponse = objectMapper.readTree(responseBody);
            assertFalse(jsonResponse.get("success").asBoolean(), "Response should have success = false");
            assertThat(jsonResponse.get("message").asText()).containsIgnoringCase("already");
        }

        @Test
        @DisplayName("Should return complete user data in response after deactivation")
        void testDeactivateUser_ValidResponse_ContainsCompleteUserData() throws Exception {
            // Act
            MvcResult result = mockMvc.perform(
                    patch(baseUrl + "/" + testUserId + "/deactivate")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String responseBody = result.getResponse().getContentAsString();
            com.fasterxml.jackson.databind.JsonNode jsonResponse = objectMapper.readTree(responseBody);
            com.fasterxml.jackson.databind.JsonNode data = jsonResponse.get("data");

            // Verify all expected fields are present
            assertNotNull(data.get("userId"));
            assertNotNull(data.get("fullName"));
            assertNotNull(data.get("email"));
            assertNotNull(data.get("role"));
            assertNotNull(data.get("status"));
            assertNotNull(data.get("isActive"));

            assertEquals(testUserId, data.get("userId").asText());
            assertFalse(data.get("isActive").asBoolean());
        }

        @Test
        @DisplayName("Should maintain other user fields unchanged after deactivation")
        void testDeactivateUser_OtherFieldsRemainUnchanged() throws Exception {
            // Arrange
            String originalFullName = testUser.getFullName();
            String originalEmail = testUser.getEmail();
            UserRole originalRole = testUser.getRole();
            UserStatus originalStatus = testUser.getStatus();

            // Act
            MvcResult result = mockMvc.perform(
                    patch(baseUrl + "/" + testUserId + "/deactivate")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String responseBody = result.getResponse().getContentAsString();
            com.fasterxml.jackson.databind.JsonNode jsonResponse = objectMapper.readTree(responseBody);
            com.fasterxml.jackson.databind.JsonNode data = jsonResponse.get("data");

            assertEquals(originalFullName, data.get("fullName").asText());
            assertEquals(originalEmail, data.get("email").asText());
            assertEquals(originalRole.toString(), data.get("role").asText());
            assertEquals(originalStatus.toString(), data.get("status").asText());
        }

        @Test
        @DisplayName("Should handle multiple calls properly")
        void testDeactivateUser_MultipleCallsHandledProperly() throws Exception {
            // First deactivation should succeed
            mockMvc.perform(
                    patch(baseUrl + "/" + testUserId + "/deactivate")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Verify user is deactivated
            User deactivatedUser = profileDao.getProfileById(testUserId);
            assertFalse(deactivatedUser.getIsActive());

            // Second deactivation should fail
            MvcResult result = mockMvc.perform(
                    patch(baseUrl + "/" + testUserId + "/deactivate")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            com.fasterxml.jackson.databind.JsonNode jsonResponse = objectMapper.readTree(responseBody);
            assertFalse(jsonResponse.get("success").asBoolean());
        }

        @Test
        @DisplayName("Should return proper error message for non-existent user")
        void testDeactivateUser_NonExistentUser_ReturnsProperErrorMessage() throws Exception {
            // Arrange
            String nonExistentUserId = "11111111-1111-1111-1111-111111111111";

            // Act
            MvcResult result = mockMvc.perform(
                    patch(baseUrl + "/" + nonExistentUserId + "/deactivate")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andReturn();

            // Assert
            String responseBody = result.getResponse().getContentAsString();
            com.fasterxml.jackson.databind.JsonNode jsonResponse = objectMapper.readTree(responseBody);

            assertFalse(jsonResponse.get("success").asBoolean());
            assertThat(jsonResponse.get("message").asText())
                    .containsIgnoringCase("does not exist");
        }

        @Test
        @DisplayName("Should verify response structure for successful deactivation")
        void testDeactivateUser_ResponseStructure_IsCorrect() throws Exception {
            // Act
            MvcResult result = mockMvc.perform(
                    patch(baseUrl + "/" + testUserId + "/deactivate")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String responseBody = result.getResponse().getContentAsString();
            com.fasterxml.jackson.databind.JsonNode jsonResponse = objectMapper.readTree(responseBody);

            // Check ApiResponse structure
            assertTrue(jsonResponse.has("data"));
            assertTrue(jsonResponse.has("success"));
            assertTrue(jsonResponse.has("statusCode"));
            assertTrue(jsonResponse.has("message"));
            assertTrue(jsonResponse.has("error"));

            // Verify types
            assertTrue(jsonResponse.get("data").isObject());
            assertTrue(jsonResponse.get("success").isBoolean());
            assertTrue(jsonResponse.get("statusCode").isNumber());
            assertTrue(jsonResponse.get("message").isTextual());
        }
    }
}
