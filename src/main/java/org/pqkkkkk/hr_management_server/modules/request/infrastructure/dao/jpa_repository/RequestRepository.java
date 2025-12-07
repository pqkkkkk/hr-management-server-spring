package org.pqkkkkk.hr_management_server.modules.request.infrastructure.dao.jpa_repository;

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestRepository extends JpaRepository<Request, String>, 
                                          JpaSpecificationExecutor<Request> {
    // Custom query methods if needed in the future
}
