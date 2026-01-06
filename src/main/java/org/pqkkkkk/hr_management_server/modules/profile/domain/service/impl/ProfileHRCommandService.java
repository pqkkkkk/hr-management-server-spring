package org.pqkkkkk.hr_management_server.modules.profile.domain.service.impl;

import org.pqkkkkk.hr_management_server.modules.profile.domain.dao.DepartmentDao;
import org.pqkkkkk.hr_management_server.modules.profile.domain.dao.ProfileDao;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Department;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserStatus;
import org.pqkkkkk.hr_management_server.modules.profile.domain.service.ProfileCommandService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;

@Service
public class ProfileHRCommandService implements ProfileCommandService {
    private final ProfileDao profileDao;
    private final DepartmentDao departmentDao;

    public ProfileHRCommandService(ProfileDao profileDao, DepartmentDao departmentDao) {
        this.profileDao = profileDao;
        this.departmentDao = departmentDao;
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
        // Lookup department by name if departmentName is provided
        if (updatedUser.getDepartment() != null &&
                StringUtils.hasText(updatedUser.getDepartment().getDepartmentName())) {
            Department dept = departmentDao
                    .getDepartmentByName(updatedUser.getDepartment().getDepartmentName())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Department not found: " + updatedUser.getDepartment().getDepartmentName()));
            existingUser.setDepartment(dept);
        } else if (updatedUser.getDepartment() != null &&
                updatedUser.getDepartment().getDepartmentId() != null) {
            // Fallback: set department by ID if provided
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

        existingUser = updateUserFields(existingUser, user);

        return profileDao.updateProfile(existingUser);
    }

    @Override
    @Transactional
    public User deactivateUser(String userId) {
        User user = profileDao.getProfileById(userId);

        if (user == null) {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist");
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new IllegalArgumentException("User is already inactive");
        }

        user.setStatus(UserStatus.INACTIVE);

        return profileDao.updateProfile(user);
    }

}
