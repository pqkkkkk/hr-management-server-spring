package org.pqkkkkk.hr_management_server.modules.auth.infrastructure.dao.jpa_repository;

import java.util.Optional;

import org.pqkkkkk.hr_management_server.modules.auth.domain.entity.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCredentialsRepository extends JpaRepository<UserCredentials, String> {

    /**
     * Find credentials by user ID
     * 
     * @param userId the user ID to search for
     * @return Optional containing UserCredentials if found
     */
    Optional<UserCredentials> findByUserId(String userId);
}
