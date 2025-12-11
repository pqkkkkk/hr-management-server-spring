package org.pqkkkkk.hr_management_server.modules.request.infrastructure.dao.jpa_repository;

import java.time.LocalDateTime;

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestRepository extends JpaRepository<Request, String>, 
                                          JpaSpecificationExecutor<Request> {
    
    boolean existsByEmployee_UserIdAndRequestTypeAndCreatedAtBetween(
        String userId, 
        RequestType requestType, 
        LocalDateTime startOfDay, 
        LocalDateTime endOfDay
    );
}
