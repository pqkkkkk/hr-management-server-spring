package org.pqkkkkk.hr_management_server.modules.profile.controller.http;

import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.DTO.UserDTO;
import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.Request.UpdateUserForHRRequest;
import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.Request.UpdateUserForEmployeeRequest;
import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.Response.ApiResponse;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.filter.FilterCriteria.ProfileFilter;
import org.pqkkkkk.hr_management_server.modules.profile.domain.service.ProfileQueryService;
import org.pqkkkkk.hr_management_server.modules.profile.domain.service.ProfileCommandService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class ProfileApi {
    private final ProfileCommandService profileCommandService;
    private final ProfileQueryService profileQueryService;

    public ProfileApi(@Qualifier("profileEmployeeCommandService") ProfileCommandService profileCommandService, ProfileQueryService profileQueryService) {
        this.profileCommandService = profileCommandService;
        this.profileQueryService = profileQueryService;
    }

    @PatchMapping("{userId}/for-hr")
    public ResponseEntity<ApiResponse<UserDTO>> updateUserProfileForHR(@PathVariable String userId, @Valid @RequestBody UpdateUserForHRRequest request) {

        User newUserInfo = request.toEntity();
        newUserInfo.setUserId(userId);
        
        User updatedUser = profileCommandService.updateProfile(newUserInfo);

        UserDTO userDTO = UserDTO.fromEntity(updatedUser);

        ApiResponse<UserDTO> apiResponse = new ApiResponse<UserDTO>(userDTO, true, HttpStatus.OK.value(),
                                 "User profile updated successfully.", null);
        return ResponseEntity.ok(apiResponse);
    }


    @PatchMapping("{userId}/for-employee")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfileForEmployee(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserForEmployeeRequest request) {

        User userEntity = request.toEntity();
        userEntity.setUserId(userId);

        User updatedUser = profileCommandService.updateProfile(userEntity);

        UserDTO userDTO = UserDTO.fromEntity(updatedUser);
        ApiResponse<UserDTO> apiResponse = new ApiResponse<>(
                userDTO,
                true,
                HttpStatus.OK.value(),
                "Profile updated successfully.",
                null
        );

        return ResponseEntity.ok(apiResponse);
    }


    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getProfiles(@Valid @ModelAttribute ProfileFilter filter){
        Page<User> users = profileQueryService.getProfiles(filter);


        Page<UserDTO> userDTOs = users.map(UserDTO::fromEntity);

        ApiResponse<Page<UserDTO>> apiResponse = new ApiResponse<>(userDTOs, true,
                HttpStatus.OK.value(), "Profiles retrieved successfully.", null);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDTO>> getMyProfile(@PathVariable String userId) {

        User user = profileQueryService.getProfileById(userId);

        UserDTO userDTO = UserDTO.fromEntity(user);


        ApiResponse<UserDTO> apiResponse = new ApiResponse<>(
                userDTO,
                true,
                HttpStatus.OK.value(),
                "Profile retrieved successfully.",
                null
        );

        return ResponseEntity.ok(apiResponse);
    }


}
