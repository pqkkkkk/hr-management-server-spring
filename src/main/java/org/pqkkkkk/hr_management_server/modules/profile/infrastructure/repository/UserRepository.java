package org.pqkkkkk.hr_management_server.modules.profile.infrastructure.repository;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

}
