package org.pqkkkkk.hr_management_server.modules.auth.domain.service.impl;

import org.pqkkkkk.hr_management_server.modules.auth.domain.dao.AuthDao;
import org.pqkkkkk.hr_management_server.modules.auth.domain.entity.UserCredentials;
import org.pqkkkkk.hr_management_server.modules.auth.domain.service.AuthService;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserRole;
import org.pqkkkkk.hr_management_server.modules.profile.domain.service.ProfileQueryService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthDao authDao;
    private final ProfileQueryService profileQueryService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthServiceImpl(AuthDao authDao, ProfileQueryService profileQueryService,
            BCryptPasswordEncoder passwordEncoder) {
        this.authDao = authDao;
        this.profileQueryService = profileQueryService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User login(String email, String password, UserRole role) {
        validateLoginInfo(email, password, role);

        // Step 1: Find user by email
        User user = profileQueryService.getProfileByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("Invalid email");
        }

        // Step 2: Check if role matches
        if (user.getRole() == UserRole.EMPLOYEE && user.getRole() != role) {
            throw new IllegalArgumentException("Invalid role");
        }

        // Step 3: Find credentials and verify password
        UserCredentials credentials = authDao.findByUserId(user.getUserId());
        if (credentials == null) {
            throw new IllegalArgumentException("Invalid email");
        }

        // Step 4: Verify password using BCrypt
        if (!passwordEncoder.matches(password, credentials.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password");
        }

        // Authentication successful, return user
        return user;
    }

    private void validateLoginInfo(String email, String password, UserRole role) {
        // Validate input
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role is required");
        }
    }
}
