package org.pqkkkkk.hr_management_server.modules.auth.controller.http;

import org.pqkkkkk.hr_management_server.modules.auth.controller.http.dto.AuthDto.LoginRequest;
import org.pqkkkkk.hr_management_server.modules.auth.controller.http.dto.AuthDto.LoginResponse;
import org.pqkkkkk.hr_management_server.modules.auth.domain.service.AuthService;
import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.DTO.UserDTO;
import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.Response.ApiResponse;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthApi {

    private final AuthService authService;

    public AuthApi(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.login(request.email(), request.password(), request.role());

        UserDTO userDTO = UserDTO.fromEntity(user);
        LoginResponse loginResponse = new LoginResponse(userDTO);

        ApiResponse<LoginResponse> apiResponse = new ApiResponse<>(
                loginResponse,
                true,
                HttpStatus.OK.value(),
                "Login successful",
                null);

        return ResponseEntity.ok(apiResponse);
    }
}
