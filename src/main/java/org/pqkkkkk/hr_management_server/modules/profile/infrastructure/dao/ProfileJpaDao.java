package org.pqkkkkk.hr_management_server.modules.profile.infrastructure.dao;

import java.util.List;

import org.pqkkkkk.hr_management_server.modules.profile.domain.dao.ProfileDao;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserSortingField;
import org.pqkkkkk.hr_management_server.modules.profile.domain.filter.FilterCriteria.ProfileFilter;
import org.pqkkkkk.hr_management_server.modules.profile.infrastructure.dao.jpa_repository.ProfileRepository;
import org.pqkkkkk.hr_management_server.shared.Constants;
import org.pqkkkkk.hr_management_server.shared.Constants.SortDirection;
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

    private Pageable createPageable(Integer currentPage, Integer pageSize, UserSortingField sortBy,
            SortDirection sortDirection) {

        String sortByString = (sortBy == null) ? Constants.DEFAULT_SORT_BY : sortBy.getFieldName();
        String sortDirectionString = (sortDirection == null) ? Constants.DEFAULT_SORT_DIRECTION : sortDirection.name();
        Sort sort = Sort.by(sortDirectionString.equals("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, sortByString);

        if (currentPage == null || pageSize == null) {
            return PageRequest.of(0, Constants.DEFAULT_PAGE_SIZE, sort);
        }
        return PageRequest.of(currentPage - 1, pageSize, sort); // -1 because pages are 0-indexed
    }

    @Override
    public Page<User> getProfiles(ProfileFilter filter) {
        Pageable pageable = createPageable(filter.currentPage(), filter.pageSize(),
                filter.sortBy(), filter.sortDirection());

        return profileRepository.getUsers(pageable, filter);
    }

    @Override
    public User getProfileById(String userId) {
        return profileRepository.findById(userId).orElse(null);
    }

    @Override
    public List<User> getAllProfiles(ProfileFilter filter) {
        return profileRepository.getAllUsers(filter);
    }

    @Override
    public User getProfileByEmail(String email) {
        return profileRepository.findByEmailIgnoreCase(email).orElse(null);
    }

}
