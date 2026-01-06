package org.pqkkkkk.hr_management_server.modules.profile.infrastructure.dao.jpa_repository;

import java.util.Optional;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {

    /**
     * Find department by exact name match
     * 
     * @param departmentName the department name to search for
     * @return Optional containing Department if found
     */
    Optional<Department> findByDepartmentName(String departmentName);
}
