package org.pqkkkkk.hr_management_server.modules.auth.domain.service.impl;

import org.junit.jupiter.api.Test;
import org.pqkkkkk.hr_management_server.modules.auth.domain.service.AuthService;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AuthServiceImplIntegrationTest {

    @Autowired
    private AuthService authService;

    // ---------------------------
    // LOGIN SUCCESS - EMPLOYEE
    // ---------------------------
    @Test
    void login_withValidCredentialsEmployee_shouldReturnUser() {
        // Arrange - using sample data from V3 and V19 migrations
        String email = "nguyenvanan@company.com";
        String password = "password123";
        UserRole role = UserRole.EMPLOYEE;

        // Act
        User result = authService.login(email, password, role);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals("Nguyen Van An", result.getFullName());
        assertEquals(UserRole.EMPLOYEE, result.getRole());
        assertEquals("u1a2b3c4-e5f6-7890-abcd-ef1234567890", result.getUserId());
    }

    // ---------------------------
    // LOGIN SUCCESS - MANAGER
    // ---------------------------
    @Test
    void login_withValidCredentialsManager_shouldReturnUser() {
        // Arrange
        String email = "tranthibinh@company.com";
        String password = "password123";
        UserRole role = UserRole.MANAGER;

        // Act
        User result = authService.login(email, password, role);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals("Tran Thi Binh", result.getFullName());
        assertEquals(UserRole.MANAGER, result.getRole());
    }

    // ---------------------------
    // LOGIN SUCCESS - HR
    // ---------------------------
    @Test
    void login_withValidCredentialsHR_shouldReturnUser() {
        // Arrange
        String email = "levancuong@company.com";
        String password = "password123";
        UserRole role = UserRole.HR;

        // Act
        User result = authService.login(email, password, role);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(UserRole.HR, result.getRole());
    }

    // ---------------------------
    // LOGIN SUCCESS - ADMIN
    // ---------------------------
    @Test
    void login_withValidCredentialsAdmin_shouldReturnUser() {
        // Arrange
        String email = "buivangiang@company.com";
        String password = "password123";
        UserRole role = UserRole.ADMIN;

        // Act
        User result = authService.login(email, password, role);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(UserRole.ADMIN, result.getRole());
    }

    // ---------------------------
    // LOGIN FAILED - INVALID EMAIL
    // ---------------------------
    @Test
    void login_withInvalidEmail_shouldThrowException() {
        // Arrange
        String email = "nonexistent@company.com";
        String password = "password123";
        UserRole role = UserRole.EMPLOYEE;

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(email, password, role));
    }

    // ---------------------------
    // LOGIN FAILED - INVALID PASSWORD
    // ---------------------------
    @Test
    void login_withInvalidPassword_shouldThrowException() {
        // Arrange
        String email = "nguyenvanan@company.com";
        String password = "wrongpassword";
        UserRole role = UserRole.EMPLOYEE;

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(email, password, role));
    }

    // ---------------------------
    // LOGIN FAILED - ROLE MISMATCH
    // ---------------------------
    @Test
    void login_withRoleMismatch_shouldThrowException() {
        // Arrange - Nguyen Van An is EMPLOYEE, trying to login as ADMIN
        String email = "nguyenvanan@company.com";
        String password = "password123";
        UserRole role = UserRole.ADMIN;

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(email, password, role));
    }

    // ---------------------------
    // LOGIN FAILED - NULL EMAIL
    // ---------------------------
    @Test
    void login_withNullEmail_shouldThrowException() {
        // Arrange
        String email = null;
        String password = "password123";
        UserRole role = UserRole.EMPLOYEE;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(email, password, role));
        assertEquals("Email is required", exception.getMessage());
    }

    // ---------------------------
    // LOGIN FAILED - BLANK EMAIL
    // ---------------------------
    @Test
    void login_withBlankEmail_shouldThrowException() {
        // Arrange
        String email = "   ";
        String password = "password123";
        UserRole role = UserRole.EMPLOYEE;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(email, password, role));
        assertEquals("Email is required", exception.getMessage());
    }

    // ---------------------------
    // LOGIN FAILED - NULL PASSWORD
    // ---------------------------
    @Test
    void login_withNullPassword_shouldThrowException() {
        // Arrange
        String email = "nguyenvanan@company.com";
        String password = null;
        UserRole role = UserRole.EMPLOYEE;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(email, password, role));
        assertEquals("Password is required", exception.getMessage());
    }

    // ---------------------------
    // LOGIN FAILED - BLANK PASSWORD
    // ---------------------------
    @Test
    void login_withBlankPassword_shouldThrowException() {
        // Arrange
        String email = "nguyenvanan@company.com";
        String password = "";
        UserRole role = UserRole.EMPLOYEE;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(email, password, role));
        assertEquals("Password is required", exception.getMessage());
    }

    // ---------------------------
    // LOGIN FAILED - NULL ROLE
    // ---------------------------
    @Test
    void login_withNullRole_shouldThrowException() {
        // Arrange
        String email = "nguyenvanan@company.com";
        String password = "password123";
        UserRole role = null;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(email, password, role));
        assertEquals("Role is required", exception.getMessage());
    }

    // ---------------------------
    // LOGIN - CASE INSENSITIVE EMAIL
    // ---------------------------
    @Test
    void login_withCaseInsensitiveEmail_shouldReturnUser() {
        // Arrange - email with different case
        String email = "NguyenVanAn@Company.COM";
        String password = "password123";
        UserRole role = UserRole.EMPLOYEE;

        // Act
        User result = authService.login(email, password, role);

        // Assert
        assertNotNull(result);
        assertEquals("nguyenvanan@company.com", result.getEmail());
        assertEquals("Nguyen Van An", result.getFullName());
    }
}
