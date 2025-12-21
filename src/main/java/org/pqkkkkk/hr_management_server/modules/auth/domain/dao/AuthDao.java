package org.pqkkkkk.hr_management_server.modules.auth.domain.dao;

import org.pqkkkkk.hr_management_server.modules.auth.domain.entity.UserCredentials;

public interface AuthDao {
    /**
     * Find user credentials by user ID
     * 
     * @param userId the user ID to search for
     * @return UserCredentials if found, null otherwise
     */
    UserCredentials findByUserId(String userId);
}
