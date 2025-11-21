package org.pqkkkkk.hr_management_server.modules.profile.infrastructure.dao;

import org.pqkkkkk.hr_management_server.modules.profile.domain.dao.ProfileDao;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.filter.FilterCriteria.ProfileFilter;
import org.pqkkkkk.hr_management_server.modules.profile.infrastructure.dao.jpa_repository.ProfileRepository;
import org.pqkkkkk.hr_management_server.shared.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class ProfileJpaDao implements ProfileDao {
    private final ProfileRepository profileRepository;

    public ProfileJpaDao(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }
    
    @Override
    public User updateProfile(User user) {
        return profileRepository.save(user);
    }
    private Pageable createPageable(Integer currentPage, Integer pageSize, String sortBy,
            String sortDirection) {
                
        sortBy = (sortBy == null || sortBy.isBlank()) ? Constants.DEFAULT_SORT_BY : sortBy;
        sortDirection = (sortDirection == null || sortDirection.isBlank()) ? Constants.DEFAULT_SORT_DIRECTION : sortDirection;
        Sort sort = Sort.by(sortDirection.equals("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);

        if(currentPage == null || pageSize == null) {
            return PageRequest.of(0, Constants.DEFAULT_PAGE_SIZE, sort);
        }
        return PageRequest.of(currentPage - 1, pageSize, sort); // -1 because pages are 0-indexed
    }
    @Override
    public Page<User> getProfiles(ProfileFilter filter) {
        Pageable pageable = createPageable(filter.currentPage(), filter.pageSize(),
                     filter.sortBy().toString(), filter.sortDirection().toString());
                     
        return profileRepository.getUsers(pageable, filter);
    }

    @Override
    public User getProfileById(String userId) {
        return profileRepository.findById(userId).orElse(null);
    }

}
