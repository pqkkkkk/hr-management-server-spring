package org.pqkkkkk.hr_management_server.modules.profile.infrastructure.dao.jpa_repository;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, String> {
}
