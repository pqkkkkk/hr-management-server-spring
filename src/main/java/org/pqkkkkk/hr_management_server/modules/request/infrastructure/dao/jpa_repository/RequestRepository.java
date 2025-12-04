package org.pqkkkkk.hr_management_server.modules.request.infrastructure.dao.jpa_repository;

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestRepository extends JpaRepository<Request, String> {
    Page<Request> findAll(Specification<Request> spec, Pageable pageable);
}
