package org.pqkkkkk.hr_management_server.modules.profile.infrastructure.dao;

import org.pqkkkkk.hr_management_server.modules.profile.domain.dao.ProfileDao;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.filter.FilterCriteria.ProfileFilter;
import org.pqkkkkk.hr_management_server.modules.profile.infrastructure.dao.jpa_repository.ProfileRepository;
import org.springframework.data.domain.Page;

public class ProfileJpaDao implements ProfileDao {
    private final ProfileRepository profileRepository;

    public ProfileJpaDao(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }
    
    @Override
    public User updateProfile(User user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateProfile'");
    }

    @Override
    public Page<User> getProfiles(ProfileFilter filter) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUsers'");
    }

}
