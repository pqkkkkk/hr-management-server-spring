package org.pqkkkkk.hr_management_server.modules.profile.domain.service.impl;

import org.pqkkkkk.hr_management_server.modules.profile.domain.dao.ProfileDao;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.service.ProfileCommandService;

import jakarta.transaction.Transactional;

/**
 * Employee profile command service implementation.
 * Handles employee-related profile operations.
 *
 * Note: Not registered as @Service bean to avoid duplicate bean conflicts.
 * Use ProfileHRCommandService as the primary implementation.
 */
public class ProfileEmployeeCommandService implements ProfileCommandService {
    private final ProfileDao profileDao;

    public ProfileEmployeeCommandService(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    @Override
    @Transactional
    public User updateProfile(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        User existingUser = profileDao.getProfileById(user.getUserId());

        if (existingUser == null) {
            throw new IllegalArgumentException("User with ID " + user.getUserId() + " does not exist");
        }

        // Update fields only if they are provided (not null)
        if (user.getFullName() != null) {
            existingUser.setFullName(user.getFullName());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getStatus() != null) {
            existingUser.setStatus(user.getStatus());
        }
        if (user.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(user.getPhoneNumber());
        }
        if (user.getAddress() != null) {
            existingUser.setAddress(user.getAddress());
        }
        if (user.getDateOfBirth() != null) {
            existingUser.setDateOfBirth(user.getDateOfBirth());
        }
        if (user.getGender() != null) {
            existingUser.setGender(user.getGender());
        }

        return profileDao.updateProfile(existingUser);
    }

    @Override
    @Transactional
    public User deactivateUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        User existingUser = profileDao.getProfileById(userId);

        if (existingUser == null) {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist");
        }

        if (existingUser.getIsActive() != null && !existingUser.getIsActive()) {
            throw new IllegalArgumentException("User with ID " + userId + " is already deactivated");
        }

        existingUser.setIsActive(false);

        return profileDao.updateProfile(existingUser);
    }
}

