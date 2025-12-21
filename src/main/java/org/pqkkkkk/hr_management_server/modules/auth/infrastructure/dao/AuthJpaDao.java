package org.pqkkkkk.hr_management_server.modules.auth.infrastructure.dao;

import org.pqkkkkk.hr_management_server.modules.auth.domain.dao.AuthDao;
import org.pqkkkkk.hr_management_server.modules.auth.domain.entity.UserCredentials;
import org.pqkkkkk.hr_management_server.modules.auth.infrastructure.dao.jpa_repository.UserCredentialsRepository;
import org.springframework.stereotype.Repository;

@Repository
public class AuthJpaDao implements AuthDao {

    private final UserCredentialsRepository userCredentialsRepository;

    public AuthJpaDao(UserCredentialsRepository userCredentialsRepository) {
        this.userCredentialsRepository = userCredentialsRepository;
    }

    @Override
    public UserCredentials findByUserId(String userId) {
        return userCredentialsRepository.findByUserId(userId).orElse(null);
    }
}
