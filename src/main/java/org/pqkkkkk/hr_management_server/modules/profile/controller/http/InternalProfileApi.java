package org.pqkkkkk.hr_management_server.modules.profile.controller.http;

import java.util.List;

import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.Response.ApiResponse;
import org.pqkkkkk.hr_management_server.modules.profile.domain.dao.ProfileDao;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserRole;
import org.pqkkkkk.hr_management_server.modules.profile.domain.filter.FilterCriteria.ProfileFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal API Controller for Profile operations
 * Used by external services (e.g., .NET Reward module) to get user information
 * 
 * Prefix: /internal/api/v1/profiles
 */
@RestController
@RequestMapping("/internal/api/v1/profiles")
public class InternalProfileApi {

    private final ProfileDao profileDao;

    public InternalProfileApi(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    /**
     * Get all users with optional role filter
     * Used by .NET app to create wallets for all users when reward program is
     * created
     * 
     * @param roles Optional list of roles to filter by (e.g., EMPLOYEE, MANAGER)
     * @return List of UserBasicDto containing userId, fullName, email, role
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserBasicResponse>>> getAllUsers(
            @RequestParam(required = false) List<UserRole> roles) {

        // Build filter with roles only (no pagination for internal API)
        ProfileFilter filter = new ProfileFilter(
                null, // currentPage
                null, // pageSize
                null, // sortBy
                null, // sortDirection
                null, // nameTerm
                roles, // roles filter
                null, // gender
                null, // status
                null, // position
                null, // departmentId
                null // departmentName
        );

        List<User> users = profileDao.getAllProfiles(filter);

        List<UserBasicResponse> response = users.stream()
                .map(UserBasicResponse::fromEntity)
                .toList();

        ApiResponse<List<UserBasicResponse>> apiResponse = new ApiResponse<>(
                response,
                true,
                HttpStatus.OK.value(),
                "Users retrieved successfully.",
                null);

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * DTO for basic user information (internal API response)
     */
    public record UserBasicResponse(
            String userId,
            String fullName,
            String email,
            UserRole role) {
        public static UserBasicResponse fromEntity(User user) {
            return new UserBasicResponse(
                    user.getUserId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole());
        }
    }
}
