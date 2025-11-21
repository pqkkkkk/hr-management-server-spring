package org.pqkkkkk.hr_management_server.modules.profile.infrastructure.dao.jpa_repository;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<User, String> {

}
