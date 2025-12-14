package org.pqkkkkk.hr_management_server.modules.profile.domain.service.impl;

import org.pqkkkkk.hr_management_server.modules.profile.domain.dao.ProfileDao;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.service.ProfileCommandService;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

/**
 * Profile command service implementation for Request module.
 * This service is used by the Request module to update user profiles
 * as side effects of request processing (e.g., deducting leave balance
 * when approving leave requests).
 * 
 * This implementation has restricted update capabilities compared to
 * ProfileHRCommandService - it should only update fields related to
 * request processing, not general profile management fields.
 */
@Service("profileRequestCommandService")
public class ProfileRequestCommandService implements ProfileCommandService {
    private final ProfileDao profileDao;

    public ProfileRequestCommandService(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    /**
     * Updates user profile with restrictions for request module usage.
     * Currently supports:
     * - Updating remaining annual leave balance
     * - Updating maximum annual leave entitlement
     * - Updating remaining WFH days
     * - Updating maximum WFH days
     * 
     * This method should NOT be used to update:
     * - Personal information (name, email, phone, etc.)
     * - Employment details (role, status, department, etc.)
     * - Banking information
     * 
     * @param user User object containing userId and fields to update
     * @return Updated user object
     * @throws IllegalArgumentException if user is null, userId is invalid,
     *         or attempting to update restricted fields
     */
    @Override
    @Transactional
    public User updateProfile(User user) {
        // Validate user input
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (user.getUserId() == null || user.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }

        // Fetch existing user
        User existingUser = profileDao.getProfileById(user.getUserId());
        if (existingUser == null) {
            throw new IllegalArgumentException("User with ID " + user.getUserId() + " does not exist");
        }

        updateFields(user, existingUser);

        // Save and return
        return profileDao.updateProfile(existingUser);
    }

    private void updateFields(User user, User existingUser){
        if(user.getRemainingAnnualLeave() != null){
            existingUser.setRemainingAnnualLeave(user.getRemainingAnnualLeave());
        }
        if(user.getMaxAnnualLeave() != null){
            existingUser.setMaxAnnualLeave(user.getMaxAnnualLeave());
        }
        if(user.getMaxWfhDays() != null){
            existingUser.setMaxWfhDays(user.getMaxWfhDays());
        }
        if(user.getRemainingWfhDays() != null){
            existingUser.setRemainingWfhDays(user.getRemainingWfhDays());
        }
    }

    /**
     * Deactivate user functionality is not supported by request module.
     * This operation should only be performed by HR.
     * 
     * @param userId User ID
     * @throws UnsupportedOperationException always
     */
    @Override
    public User deactivateUser(String userId) {
        throw new UnsupportedOperationException(
            "Request module cannot deactivate users. " +
            "This operation should be performed by HR through ProfileHRCommandService."
        );
    }
}
