package org.pqkkkkk.hr_management_server.modules.profile.domain.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pqkkkkk.hr_management_server.modules.profile.domain.dao.ProfileDao;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Department;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.*;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ProfileHRCommandService Integration Tests")
class ProfileHRCommandServiceIntegrationTest {

    @Autowired
    private ProfileHRCommandService profileHRCommandService;

    @Autowired
    private ProfileDao profileDao;

    private User testUser;
    private String testUserId;

    @BeforeEach
    void setUp() {
        // Sample data is loaded from Flyway migration V3__insert_profile_sample_data.sql
        // Using existing test user ID from migration
        testUserId = "u1a2b3c4-e5f6-7890-abcd-ef1234567890";
        testUser = profileDao.getProfileById(testUserId);
        assertNotNull(testUser, "Test user should be loaded from migration script");
    }

    @Test
    @DisplayName("Should successfully update user's full name")
    void testUpdateProfile_WithFullName_Success() {
        // Arrange
        String newFullName = "Nguyen Van An Updated";
        User updateRequest = User.builder()
                .userId(testUserId)
                .fullName(newFullName)
                .build();

        // Act
        User updatedUser = profileHRCommandService.updateProfile(updateRequest);

        // Assert
        assertNotNull(updatedUser);
        assertEquals(newFullName, updatedUser.getFullName());
        assertEquals(testUser.getEmail(), updatedUser.getEmail());
        assertEquals(testUser.getRole(), updatedUser.getRole());
        
        // Verify persistence
        User fetchedUser = profileDao.getProfileById(testUserId);
        assertEquals(newFullName, fetchedUser.getFullName());
    }

    @Test
    @DisplayName("Should successfully update user's email")
    void testUpdateProfile_WithEmail_Success() {
        // Arrange
        String newEmail = "newemail@company.com";
        User updateRequest = User.builder()
                .userId(testUserId)
                .email(newEmail)
                .build();

        // Act
        User updatedUser = profileHRCommandService.updateProfile(updateRequest);

        // Assert
        assertEquals(newEmail, updatedUser.getEmail());
        
        // Verify persistence
        User fetchedUser = profileDao.getProfileById(testUserId);
        assertEquals(newEmail, fetchedUser.getEmail());
    }

    @Test
    @DisplayName("Should successfully update user's role")
    void testUpdateProfile_WithRole_Success() {
        // Arrange
        UserRole newRole = UserRole.MANAGER;
        User updateRequest = User.builder()
                .userId(testUserId)
                .role(newRole)
                .build();

        // Act
        User updatedUser = profileHRCommandService.updateProfile(updateRequest);

        // Assert
        assertEquals(newRole, updatedUser.getRole());
        
        // Verify persistence
        User fetchedUser = profileDao.getProfileById(testUserId);
        assertEquals(newRole, fetchedUser.getRole());
    }

    @Test
    @DisplayName("Should successfully update user's status")
    void testUpdateProfile_WithStatus_Success() {
        // Arrange
        UserStatus newStatus = UserStatus.INACTIVE;
        User updateRequest = User.builder()
                .userId(testUserId)
                .status(newStatus)
                .build();

        // Act
        User updatedUser = profileHRCommandService.updateProfile(updateRequest);

        // Assert
        assertEquals(newStatus, updatedUser.getStatus());
        
        // Verify persistence
        User fetchedUser = profileDao.getProfileById(testUserId);
        assertEquals(newStatus, fetchedUser.getStatus());
    }

    @Test
    @DisplayName("Should successfully update user's gender")
    void testUpdateProfile_WithGender_Success() {
        // Arrange
        UserGender newGender = UserGender.FEMALE;
        User updateRequest = User.builder()
                .userId(testUserId)
                .gender(newGender)
                .build();

        // Act
        User updatedUser = profileHRCommandService.updateProfile(updateRequest);

        // Assert
        assertEquals(newGender, updatedUser.getGender());
    }

    @Test
    @DisplayName("Should successfully update user's position")
    void testUpdateProfile_WithPosition_Success() {
        // Arrange
        UserPosition newPosition = UserPosition.TEAM_LEAD;
        User updateRequest = User.builder()
                .userId(testUserId)
                .position(newPosition)
                .build();

        // Act
        User updatedUser = profileHRCommandService.updateProfile(updateRequest);

        // Assert
        assertEquals(newPosition, updatedUser.getPosition());
    }

    @Test
    @DisplayName("Should successfully update user's join date")
    void testUpdateProfile_WithJoinDate_Success() {
        // Arrange
        LocalDate newJoinDate = LocalDate.of(2024, 1, 1);
        User updateRequest = User.builder()
                .userId(testUserId)
                .joinDate(newJoinDate)
                .build();

        // Act
        User updatedUser = profileHRCommandService.updateProfile(updateRequest);

        // Assert
        assertEquals(newJoinDate, updatedUser.getJoinDate());
    }

    @Test
    @DisplayName("Should successfully update user's phone number")
    void testUpdateProfile_WithPhoneNumber_Success() {
        // Arrange
        String newPhoneNumber = "0999888777";
        User updateRequest = User.builder()
                .userId(testUserId)
                .phoneNumber(newPhoneNumber)
                .build();

        // Act
        User updatedUser = profileHRCommandService.updateProfile(updateRequest);

        // Assert
        assertEquals(newPhoneNumber, updatedUser.getPhoneNumber());
    }

    @Test
    @DisplayName("Should successfully update user's identity card number")
    void testUpdateProfile_WithIdentityCardNumber_Success() {
        // Arrange
        String newIdCard = "999888777666";
        User updateRequest = User.builder()
                .userId(testUserId)
                .identityCardNumber(newIdCard)
                .build();

        // Act
        User updatedUser = profileHRCommandService.updateProfile(updateRequest);

        // Assert
        assertEquals(newIdCard, updatedUser.getIdentityCardNumber());
    }

    @Test
    @DisplayName("Should successfully update user's date of birth")
    void testUpdateProfile_WithDateOfBirth_Success() {
        // Arrange
        LocalDate newDob = LocalDate.of(1991, 6, 15);
        User updateRequest = User.builder()
                .userId(testUserId)
                .dateOfBirth(newDob)
                .build();

        // Act
        User updatedUser = profileHRCommandService.updateProfile(updateRequest);

        // Assert
        assertEquals(newDob, updatedUser.getDateOfBirth());
    }

    @Test
    @DisplayName("Should successfully update user's address")
    void testUpdateProfile_WithAddress_Success() {
        // Arrange
        String newAddress = "456 New Street, District 3, Ho Chi Minh City";
        User updateRequest = User.builder()
                .userId(testUserId)
                .address(newAddress)
                .build();

        // Act
        User updatedUser = profileHRCommandService.updateProfile(updateRequest);

        // Assert
        assertEquals(newAddress, updatedUser.getAddress());
    }

    @Test
    @DisplayName("Should successfully update user's bank account number")
    void testUpdateProfile_WithBankAccountNumber_Success() {
        // Arrange
        String newBankAccount = "9876543210123";
        User updateRequest = User.builder()
                .userId(testUserId)
                .bankAccountNumber(newBankAccount)
                .build();

        // Act
        User updatedUser = profileHRCommandService.updateProfile(updateRequest);

        // Assert
        assertEquals(newBankAccount, updatedUser.getBankAccountNumber());
    }

    @Test
    @DisplayName("Should successfully update user's bank name")
    void testUpdateProfile_WithBankName_Success() {
        // Arrange
        String newBankName = "Techcombank";
        User updateRequest = User.builder()
                .userId(testUserId)
                .bankName(newBankName)
                .build();

        // Act
        User updatedUser = profileHRCommandService.updateProfile(updateRequest);

        // Assert
        assertEquals(newBankName, updatedUser.getBankName());
    }

    @Test
    @DisplayName("Should successfully update user's department")
    void testUpdateProfile_WithDepartment_Success() {
        // Arrange
        // HR department from migration: d2b3c4d5-f6a7-8901-bcde-f12345678901
        Department newDepartment = Department.builder()
                .departmentId("d2b3c4d5-f6a7-8901-bcde-f12345678901")
                .build();
        User updateRequest = User.builder()
                .userId(testUserId)
                .department(newDepartment)
                .build();

        // Act
        User updatedUser = profileHRCommandService.updateProfile(updateRequest);

        // Assert
        assertNotNull(updatedUser.getDepartment());
        assertEquals("d2b3c4d5-f6a7-8901-bcde-f12345678901", updatedUser.getDepartment().getDepartmentId());
    }

    @Test
    @DisplayName("Should successfully update multiple fields at once")
    void testUpdateProfile_WithMultipleFields_Success() {
        // Arrange
        String newFullName = "Nguyen Van An Updated";
        String newEmail = "updated@company.com";
        UserRole newRole = UserRole.MANAGER;
        UserStatus newStatus = UserStatus.ACTIVE;
        String newPhoneNumber = "0988777666";

        User updateRequest = User.builder()
                .userId(testUserId)
                .fullName(newFullName)
                .email(newEmail)
                .role(newRole)
                .status(newStatus)
                .phoneNumber(newPhoneNumber)
                .build();

        // Act
        User updatedUser = profileHRCommandService.updateProfile(updateRequest);

        // Assert
        assertEquals(newFullName, updatedUser.getFullName());
        assertEquals(newEmail, updatedUser.getEmail());
        assertEquals(newRole, updatedUser.getRole());
        assertEquals(newStatus, updatedUser.getStatus());
        assertEquals(newPhoneNumber, updatedUser.getPhoneNumber());
        
        // Verify other fields remain unchanged
        assertEquals(testUser.getAddress(), updatedUser.getAddress());
        assertEquals(testUser.getBankName(), updatedUser.getBankName());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when user is null")
    void testUpdateProfile_WithNullUser_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> profileHRCommandService.updateProfile(null)
        );
        
        assertEquals("User cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when user ID does not exist")
    void testUpdateProfile_WithNonExistentUserId_ThrowsException() {
        // Arrange
        String nonExistentUserId = "non-existent-user-id";
        User updateRequest = User.builder()
                .userId(nonExistentUserId)
                .fullName("Test Name")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> profileHRCommandService.updateProfile(updateRequest)
        );
        
        assertTrue(exception.getMessage().contains("does not exist"));
    }

    @Test
    @DisplayName("Should not update fields when they are null")
    void testUpdateProfile_WithNullFields_DoesNotUpdateThoseFields() {
        // Arrange
        String originalFullName = testUser.getFullName();
        String originalEmail = testUser.getEmail();
        UserRole originalRole = testUser.getRole();
        
        User updateRequest = User.builder()
                .userId(testUserId)
                .phoneNumber("0999888777")
                // Other fields are null and should not be updated
                .build();

        // Act
        User updatedUser = profileHRCommandService.updateProfile(updateRequest);

        // Assert
        assertEquals("0999888777", updatedUser.getPhoneNumber());
        assertEquals(originalFullName, updatedUser.getFullName());
        assertEquals(originalEmail, updatedUser.getEmail());
        assertEquals(originalRole, updatedUser.getRole());
    }

    @Test
    @DisplayName("Should successfully update user when only userId is provided in update request")
    void testUpdateProfile_WithOnlyUserId_ReturnsUnchangedUser() {
        // Arrange
        User updateRequest = User.builder()
                .userId(testUserId)
                .build();

        String originalFullName = testUser.getFullName();
        String originalEmail = testUser.getEmail();

        // Act
        User updatedUser = profileHRCommandService.updateProfile(updateRequest);

        // Assert
        assertNotNull(updatedUser);
        assertEquals(originalFullName, updatedUser.getFullName());
        assertEquals(originalEmail, updatedUser.getEmail());
    }
}
