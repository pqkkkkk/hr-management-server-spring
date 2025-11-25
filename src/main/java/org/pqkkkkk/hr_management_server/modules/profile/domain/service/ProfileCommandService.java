package org.pqkkkkk.hr_management_server.modules.profile.domain.service;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;

public interface ProfileCommandService {
    public User updateProfile(User user);
    public User deactivateUser(String userId);
}
