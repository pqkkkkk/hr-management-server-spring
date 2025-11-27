package org.pqkkkkk.hr_management_server.modules.profile.domain.service.impl;

import org.pqkkkkk.hr_management_server.modules.profile.domain.dao.ProfileDao;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.filter.FilterCriteria.ProfileFilter;
import org.pqkkkkk.hr_management_server.modules.profile.domain.service.ProfileQueryService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class ProfileQueryServiceImpl implements ProfileQueryService {
    private final ProfileDao profileDao;

    public ProfileQueryServiceImpl(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }


    @Override
    public Page<User> getProfiles(ProfileFilter filter) {
        return profileDao.getProfiles(filter);
    }

    @Override
    public User getProfileById(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be null or blank");
        }
        User user = profileDao.getProfileById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User with id '" + userId + "' does not exist");
        }
        return user;
    }

}
