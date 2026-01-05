package org.pqkkkkk.hr_management_server.modules.profile.domain.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.*;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.filter.FilterCriteria.ProfileFilter;
import org.pqkkkkk.hr_management_server.shared.Constants.SortDirection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ProfileQueryServiceImpl Integration Tests")
class ProfileQueryServiceImplIntegrationTest {

    @Autowired
    private ProfileQueryServiceImpl profileQueryService;

    @Test
    @DisplayName("Should return all profiles with default pagination")
    void testGetProfiles_WithDefaultPagination_Success() {
        // Arrange
        ProfileFilter filter = new ProfileFilter(
                1, // currentPage
                10, // pageSize
                null, // sortBy
                null, // sortDirection
                null, // nameTerm
                null, // role
                null, // gender
                null, // status
                null, // position
                null, // departmentId,
                null // departmentName
        );

        // Act
        Page<User> result = profileQueryService.getProfiles(filter);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalElements() >= 10, "Should have at least 10 users from migration data");
        assertEquals(10, result.getSize(), "Page size should be 10");
        assertEquals(0, result.getNumber(), "Page number should be 0 (Spring uses 0-based indexing)");
    }

    @Test
    @DisplayName("Should return second page of profiles")
    void testGetProfiles_WithSecondPage_Success() {
        // Arrange
        ProfileFilter filter = new ProfileFilter(
                2, // currentPage
                5, // pageSize
                null, // sortBy
                null, // sortDirection
                null, // nameTerm
                null, // role
                null, // gender
                null, // status
                null, // position
                null, // departmentId
                null // departmentName
        );

        // Act
        Page<User> result = profileQueryService.getProfiles(filter);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.getSize());
        assertEquals(1, result.getNumber(), "Page number should be 1 (second page in 0-based indexing)");
    }

    @Test
    @DisplayName("Should filter profiles by name term")
    void testGetProfiles_FilterByNameTerm_Success() {
        // Arrange - Search for "Nguyen"
        ProfileFilter filter = new ProfileFilter(
                1, // currentPage
                10, // pageSize
                null, // sortBy
                null, // sortDirection
                "Nguyen", // nameTerm
                null, // role
                null, // gender
                null, // status
                null, // position
                null, // departmentId
                null // departmentName
        );

        // Act
        Page<User> result = profileQueryService.getProfiles(filter);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalElements() >= 2, "Should find at least 2 users with 'Nguyen' in name");
        result.getContent().forEach(user -> assertTrue(user.getFullName().toLowerCase().contains("nguyen"),
                "All results should contain 'Nguyen' in name"));
    }

    @Test
    @DisplayName("Should filter profiles by role - EMPLOYEE")
    void testGetProfiles_FilterByRole_Success() {
        // Arrange
        ProfileFilter filter = new ProfileFilter(
                1, // currentPage
                10, // pageSize
                null, // sortBy
                null, // sortDirection
                null, // nameTerm
                UserRole.EMPLOYEE, // role
                null, // gender
                null, // status
                null, // position
                null, // departmentId
                null // departmentName
        );

        // Act
        Page<User> result = profileQueryService.getProfiles(filter);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0, "Should find at least one EMPLOYEE");
        result.getContent().forEach(
                user -> assertEquals(UserRole.EMPLOYEE, user.getRole(), "All results should have EMPLOYEE role"));
    }

    @Test
    @DisplayName("Should filter profiles by role - HR")
    void testGetProfiles_FilterByHRRole_Success() {
        // Arrange
        ProfileFilter filter = new ProfileFilter(
                1, // currentPage
                10, // pageSize
                null, // sortBy
                null, // sortDirection
                null, // nameTerm
                UserRole.HR, // role
                null, // gender
                null, // status
                null, // position
                null, // departmentId
                null // departmentName
        );

        // Act
        Page<User> result = profileQueryService.getProfiles(filter);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalElements() >= 2, "Should find at least 2 HR users from migration data");
        result.getContent()
                .forEach(user -> assertEquals(UserRole.HR, user.getRole(), "All results should have HR role"));
    }

    @Test
    @DisplayName("Should filter profiles by gender - MALE")
    void testGetProfiles_FilterByGender_Success() {
        // Arrange
        ProfileFilter filter = new ProfileFilter(
                1, // currentPage
                10, // pageSize
                null, // sortBy
                null, // sortDirection
                null, // nameTerm
                null, // role
                UserGender.MALE, // gender
                null, // status
                null, // position
                null, // departmentId
                null // departmentName
        );

        // Act
        Page<User> result = profileQueryService.getProfiles(filter);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0, "Should find at least one MALE user");
        result.getContent()
                .forEach(user -> assertEquals(UserGender.MALE, user.getGender(), "All results should be MALE"));
    }

    @Test
    @DisplayName("Should filter profiles by gender - FEMALE")
    void testGetProfiles_FilterByFemaleGender_Success() {
        // Arrange
        ProfileFilter filter = new ProfileFilter(
                1, // currentPage
                10, // pageSize
                null, // sortBy
                null, // sortDirection
                null, // nameTerm
                null, // role
                UserGender.FEMALE, // gender
                null, // status
                null, // position
                null, // departmentId
                null // departmentName
        );

        // Act
        Page<User> result = profileQueryService.getProfiles(filter);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0, "Should find at least one FEMALE user");
        result.getContent()
                .forEach(user -> assertEquals(UserGender.FEMALE, user.getGender(), "All results should be FEMALE"));
    }

    @Test
    @DisplayName("Should filter profiles by status - ACTIVE")
    void testGetProfiles_FilterByActiveStatus_Success() {
        // Arrange
        ProfileFilter filter = new ProfileFilter(
                1, // currentPage
                10, // pageSize
                null, // sortBy
                null, // sortDirection
                null, // nameTerm
                null, // role
                null, // gender
                UserStatus.ACTIVE, // status
                null, // position
                null, // departmentId
                null // departmentName
        );

        // Act
        Page<User> result = profileQueryService.getProfiles(filter);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0, "Should find at least one ACTIVE user");
        result.getContent().forEach(
                user -> assertEquals(UserStatus.ACTIVE, user.getStatus(), "All results should have ACTIVE status"));
    }

    @Test
    @DisplayName("Should filter profiles by status - INACTIVE")
    void testGetProfiles_FilterByInactiveStatus_Success() {
        // Arrange
        ProfileFilter filter = new ProfileFilter(
                1, // currentPage
                10, // pageSize
                null, // sortBy
                null, // sortDirection
                null, // nameTerm
                null, // role
                null, // gender
                UserStatus.INACTIVE, // status
                null, // position
                null, // departmentId
                null // departmentName
        );

        // Act
        Page<User> result = profileQueryService.getProfiles(filter);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalElements() >= 1, "Should find at least one INACTIVE user from migration data");
        result.getContent().forEach(
                user -> assertEquals(UserStatus.INACTIVE, user.getStatus(), "All results should have INACTIVE status"));
    }

    @Test
    @DisplayName("Should filter profiles by position")
    void testGetProfiles_FilterByPosition_Success() {
        // Arrange
        ProfileFilter filter = new ProfileFilter(
                1, // currentPage
                10, // pageSize
                null, // sortBy
                null, // sortDirection
                null, // nameTerm
                null, // role
                null, // gender
                null, // status
                UserPosition.JUNIOR_DEVELOPER, // position
                null, // departmentId
                null // departmentName
        );

        // Act
        Page<User> result = profileQueryService.getProfiles(filter);

        // Assert
        assertNotNull(result);
        // May or may not have results depending on data
        if (result.getTotalElements() > 0) {
            result.getContent().forEach(user -> assertEquals(UserPosition.JUNIOR_DEVELOPER, user.getPosition()));
        }
    }

    @Test
    @DisplayName("Should filter profiles by department")
    void testGetProfiles_FilterByDepartment_Success() {
        // Arrange - Engineering department ID from migration
        String engineeringDeptId = "d1a2b3c4-e5f6-7890-abcd-ef1234567890";
        ProfileFilter filter = new ProfileFilter(
                1, // currentPage
                10, // pageSize
                null, // sortBy
                null, // sortDirection
                null, // nameTerm
                null, // role
                null, // gender
                null, // status
                null, // position
                engineeringDeptId, // departmentId
                null // departmentName
        );

        // Act
        Page<User> result = profileQueryService.getProfiles(filter);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalElements() >= 2, "Engineering dept should have at least 2 users");
        result.getContent().forEach(user -> {
            assertNotNull(user.getDepartment());
            assertEquals(engineeringDeptId, user.getDepartment().getDepartmentId(),
                    "All results should belong to Engineering department");
        });
    }

    @Test
    @DisplayName("Should filter profiles by multiple criteria")
    void testGetProfiles_FilterByMultipleCriteria_Success() {
        // Arrange - Active male employees
        ProfileFilter filter = new ProfileFilter(
                1, // currentPage
                10, // pageSize
                null, // sortBy
                null, // sortDirection
                null, // nameTerm
                UserRole.EMPLOYEE, // role
                UserGender.MALE, // gender
                UserStatus.ACTIVE, // status
                null, // position
                null, // departmentId
                null // departmentName
        );

        // Act
        Page<User> result = profileQueryService.getProfiles(filter);

        // Assert
        assertNotNull(result);
        // Verify all filters are applied
        result.getContent().forEach(user -> {
            assertEquals(UserRole.EMPLOYEE, user.getRole());
            assertEquals(UserGender.MALE, user.getGender());
            assertEquals(UserStatus.ACTIVE, user.getStatus());
        });
    }

    @Test
    @DisplayName("Should sort profiles by name ascending")
    void testGetProfiles_SortByNameAscending_Success() {
        // Arrange
        ProfileFilter filter = new ProfileFilter(
                1,
                10,
                UserSortingField.NAME,
                SortDirection.ASC,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        // Act
        Page<User> result = profileQueryService.getProfiles(filter);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalElements() > 1, "Should have multiple users to verify sorting");

        // Verify ascending order
        String previousName = null;
        for (User user : result.getContent()) {
            if (previousName != null) {
                assertTrue(user.getFullName().compareToIgnoreCase(previousName) >= 0,
                        "Names should be in ascending order");
            }
            previousName = user.getFullName();
        }
    }

    @Test
    @DisplayName("Should sort profiles by name descending")
    void testGetProfiles_SortByNameDescending_Success() {
        // Arrange
        ProfileFilter filter = new ProfileFilter(
                1,
                10,
                UserSortingField.NAME,
                SortDirection.DESC,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        // Act
        Page<User> result = profileQueryService.getProfiles(filter);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalElements() > 1, "Should have multiple users to verify sorting");

        // Verify descending order
        String previousName = null;
        for (User user : result.getContent()) {
            if (previousName != null) {
                assertTrue(user.getFullName().compareToIgnoreCase(previousName) <= 0,
                        "Names should be in descending order");
            }
            previousName = user.getFullName();
        }
    }

    @Test
    @DisplayName("Should sort profiles by role ascending")
    void testGetProfiles_SortByRoleAscending_Success() {
        // Arrange
        ProfileFilter filter = new ProfileFilter(
                1, // currentPage
                10, // pageSize
                UserSortingField.ROLE, // sortBy
                SortDirection.ASC, // sortDirection
                null, // nameTerm
                null, // role
                null, // gender
                null, // status
                null, // position
                null, // departmentId
                null // departmentName
        );

        // Act
        Page<User> result = profileQueryService.getProfiles(filter);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalElements() > 1, "Should have multiple users to verify sorting");

        String previousRoleName = null;
        for (User user : result.getContent()) {
            if (previousRoleName != null) {
                assertTrue(user.getRole().name().compareTo(previousRoleName) >= 0,
                        "Roles should be in alphabetical ascending order. Previous: " + previousRoleName + ", Current: "
                                + user.getRole().name());
            }
            previousRoleName = user.getRole().name();
        }
    }

    @Test
    @DisplayName("Should return empty page when no profiles match filter")
    void testGetProfiles_NoMatchingProfiles_ReturnsEmptyPage() {
        // Arrange - Use a name that doesn't exist
        ProfileFilter filter = new ProfileFilter(
                1, // currentPage
                10, // pageSize
                null, // sortBy
                null, // sortDirection
                "NonExistentName12345", // nameTerm
                null, // role
                null, // gender
                null, // status
                null, // position
                null, // departmentId
                null // departmentName
        );

        // Act
        Page<User> result = profileQueryService.getProfiles(filter);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements(), "Should find no matching users");
        assertTrue(result.getContent().isEmpty(), "Content should be empty");
    }

    @Test
    @DisplayName("Should handle combination of name search and role filter")
    void testGetProfiles_CombineNameAndRoleFilter_Success() {
        // Arrange - Search for HR employees with specific name pattern
        ProfileFilter filter = new ProfileFilter(
                1, // currentPage
                10, // pageSize
                null, // sortBy
                null, // sortDirection
                "Le", // nameTerm
                UserRole.HR, // role
                null, // gender
                null, // status
                null, // position
                null, // departmentId
                null // departmentName
        );

        // Act
        Page<User> result = profileQueryService.getProfiles(filter);

        // Assert
        assertNotNull(result);
        result.getContent().forEach(user -> {
            assertTrue(user.getFullName().toLowerCase().contains("le"));
            assertEquals(UserRole.HR, user.getRole());
        });
    }

    @Test
    @DisplayName("Should handle combination of department and status filter")
    void testGetProfiles_CombineDepartmentAndStatusFilter_Success() {
        // Arrange - Active users in Engineering department
        String engineeringDeptId = "d1a2b3c4-e5f6-7890-abcd-ef1234567890";
        ProfileFilter filter = new ProfileFilter(
                1, // currentPage
                10, // pageSize
                null, // sortBy
                null, // sortDirection
                null, // nameTerm
                null, // role
                null, // gender
                UserStatus.ACTIVE, // status
                null, // position
                engineeringDeptId, // departmentId
                null // departmentName
        );

        // Act
        Page<User> result = profileQueryService.getProfiles(filter);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0, "Should find active users in Engineering");
        result.getContent().forEach(user -> {
            assertEquals(UserStatus.ACTIVE, user.getStatus());
            assertNotNull(user.getDepartment());
            assertEquals(engineeringDeptId, user.getDepartment().getDepartmentId());
        });
    }

    @Test
    @DisplayName("Should verify user entity contains all expected fields")
    void testGetProfiles_VerifyUserEntityCompleteness_Success() {
        // Arrange
        ProfileFilter filter = new ProfileFilter(
                1, 1, null, null, null, null, null, null, null, null, null);

        // Act
        Page<User> result = profileQueryService.getProfiles(filter);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);

        User user = result.getContent().get(0);
        assertNotNull(user.getUserId());
        assertNotNull(user.getFullName());
        assertNotNull(user.getEmail());
        assertNotNull(user.getRole());
        assertNotNull(user.getStatus());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertNotNull(user.getDepartment());
    }
    @Test
    @DisplayName("getProfileById - valid userId - success")
    void testGetProfileById_ValidUserId_Success() {
        // Arrange: Thay userId này bằng userId thực tế có trong migration/test data
        String validUserId = "u1a2b3c4-e5f6-7890-abcd-ef1234567890";        // Act
        User user = profileQueryService.getProfileById(validUserId);
        // Assert
        assertNotNull(user);
        assertEquals(validUserId, user.getUserId());
    }

    @Test
    @DisplayName("getProfileById - invalid userId - should throw exception")
    void testGetProfileById_InvalidUserId_ThrowsException() {
        // Arrange
        String invalidUserId = "not-exist-id";
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> profileQueryService.getProfileById(invalidUserId));
    }

    @Test
    @DisplayName("getProfileById - null userId - should throw exception")
    void testGetProfileById_NullUserId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> profileQueryService.getProfileById(null));
    }
}
