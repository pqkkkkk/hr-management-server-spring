package org.pqkkkkk.hr_management_server.modules.profile.domain.dao;

import java.util.List;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.filter.FilterCriteria.ProfileFilter;
import org.springframework.data.domain.Page;

public interface ProfileDao {
    public User updateProfile(User user);

    public Page<User> getProfiles(ProfileFilter filter);

    public List<User> getAllProfiles(ProfileFilter filter);

    public User getProfileById(String userId);

    public User getProfileByEmail(String email);
}
