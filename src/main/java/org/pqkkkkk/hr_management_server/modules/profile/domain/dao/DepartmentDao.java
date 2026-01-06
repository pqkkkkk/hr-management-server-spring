package org.pqkkkkk.hr_management_server.modules.profile.domain.dao;

import java.util.Optional;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Department;

/**
 * Data Access Object interface for Department entity.
 * <p>
 * Provides abstraction for department data access operations.
 */
public interface DepartmentDao {

    /**
     * Find department by its name.
     * 
     * @param departmentName the department name to search for
     * @return Optional containing Department if found, empty otherwise
     */
    Optional<Department> getDepartmentByName(String departmentName);

    /**
     * Find department by its ID.
     * 
     * @param departmentId the department ID to search for
     * @return Optional containing Department if found, empty otherwise
     */
    Optional<Department> getDepartmentById(String departmentId);
}
