package org.pqkkkkk.hr_management_server.modules.request.infrastructure.dao;

import org.pqkkkkk.hr_management_server.modules.profile.infrastructure.dao.jpa_repository.ProfileRepository;
import org.pqkkkkk.hr_management_server.modules.request.domain.dao.EmployeeDao;
import org.springframework.stereotype.Component;

@Component
public class JpaEmployeeDao implements EmployeeDao {

    private final ProfileRepository profileRepository;

    public JpaEmployeeDao(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public boolean existsById(String employeeId) {
        return profileRepository.existsById(employeeId);
    }
}
