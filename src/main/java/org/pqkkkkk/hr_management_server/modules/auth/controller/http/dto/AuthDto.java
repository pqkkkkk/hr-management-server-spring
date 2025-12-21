package org.pqkkkkk.hr_management_server.modules.auth.controller.http.dto;

import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.DTO.UserDTO;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AuthDto {

    /**
     * Login request DTO
     */
    public record LoginRequest(
            @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,

            @NotBlank(message = "Password is required") String password,

            @NotNull(message = "Role is required") UserRole role) {
    }

    /**
     * Login response DTO containing user profile
     */
    public record LoginResponse(
            UserDTO user) {
    }
}
