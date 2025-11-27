package org.pqkkkkk.hr_management_server.modules.profile.domain.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ProfileEmployeeCommandService Integration Tests")
class ProfileEmployeeCommandServiceIntegrationTest {

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("profileEmployeeCommandService")
    private org.pqkkkkk.hr_management_server.modules.profile.domain.service.ProfileCommandService profileCommandService;

    @Test
    @DisplayName("updateProfile - valid user - success")
    void testUpdateProfile_ValidUser_Success() {
        // Arrange
        String userId = "u1a2b3c4-e5f6-7890-abcd-ef1234567890";
        User update = new User();
        update.setUserId(userId);
        update.setEmail("newemail@company.com");
        update.setPhoneNumber("0999999999");
        update.setAddress("New Address 123");

        // Act
        User result = profileCommandService.updateProfile(update);

        // Assert
        org.junit.jupiter.api.Assertions.assertNotNull(result);
        org.junit.jupiter.api.Assertions.assertEquals("newemail@company.com", result.getEmail());
        org.junit.jupiter.api.Assertions.assertEquals("0999999999", result.getPhoneNumber());
        org.junit.jupiter.api.Assertions.assertEquals("New Address 123", result.getAddress());
    }
    @Test
    @DisplayName("updateProfile - null user - should throw exception")
    void testUpdateProfile_NullUser_ThrowsException() {
        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> profileCommandService.updateProfile(null));
    }

    @Test
    @DisplayName("updateProfile - non-existent user - should throw exception")
    void testUpdateProfile_NonExistentUser_ThrowsException() {
        // Arrange
        User update = new User();
        update.setUserId("not-exist-id");
        update.setEmail("a@b.com");
        update.setPhoneNumber("0123456789");
        update.setAddress("Somewhere");
        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> profileCommandService.updateProfile(update));
    }

    @Test
    @DisplayName("updateProfile - missing required fields - should throw exception")
    void testUpdateProfile_MissingFields_ThrowsException() {
        // Arrange
        User update = new User();
        update.setUserId("u1a2b3c4-e5f6-7890-abcd-ef1234567890");
        // Không set email, phone, address
        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> profileCommandService.updateProfile(update));
    }
}
