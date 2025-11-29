package org.pqkkkkk.hr_management_server.modules.profile.domain.service.impl;

import org.pqkkkkk.hr_management_server.modules.profile.domain.dao.ProfileDao;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.service.ProfileCommandService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
@Primary
public class ProfileHRCommandService implements ProfileCommandService {
    private final ProfileDao profileDao;

    public ProfileHRCommandService(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    private User updateUserFields(User existingUser, User updatedUser) {
        if (updatedUser.getFullName() != null) {
            existingUser.setFullName(updatedUser.getFullName());
        }
        if (updatedUser.getEmail() != null) {
            existingUser.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getRole() != null) {
            existingUser.setRole(updatedUser.getRole());
        }
        if (updatedUser.getStatus() != null) {
            existingUser.setStatus(updatedUser.getStatus());
        }
        if (updatedUser.getGender() != null) {
            existingUser.setGender(updatedUser.getGender());
        }
        if (updatedUser.getPosition() != null) {
            existingUser.setPosition(updatedUser.getPosition());
        }
        if (updatedUser.getJoinDate() != null) {
            existingUser.setJoinDate(updatedUser.getJoinDate());
        }
        if (updatedUser.getIdentityCardNumber() != null) {
            existingUser.setIdentityCardNumber(updatedUser.getIdentityCardNumber());
        }
        if (updatedUser.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        }
        if (updatedUser.getDateOfBirth() != null) {
            existingUser.setDateOfBirth(updatedUser.getDateOfBirth());
        }
        if (updatedUser.getAddress() != null) {
            existingUser.setAddress(updatedUser.getAddress());
        }
        if (updatedUser.getBankAccountNumber() != null) {
            existingUser.setBankAccountNumber(updatedUser.getBankAccountNumber());
        }
        if (updatedUser.getBankName() != null) {
            existingUser.setBankName(updatedUser.getBankName());
        }
        if (updatedUser.getDepartment() != null && updatedUser.getDepartment().getDepartmentId() != null) {
            existingUser.setDepartment(updatedUser.getDepartment());
        }

        return existingUser;
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

        // Missing validate department existence?

        existingUser = updateUserFields(existingUser, user);

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
