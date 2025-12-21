package org.pqkkkkk.hr_management_server.modules.auth.domain.service;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserRole;

public interface AuthService {
    /**
     * Authenticate user with email, password and role
     * 
     * @param email    user email
     * @param password plain text password
     * @param role     expected user role
     * @return User if authentication successful
     * @throws IllegalArgumentException if credentials are invalid
     */
    User login(String email, String password, UserRole role);
}
