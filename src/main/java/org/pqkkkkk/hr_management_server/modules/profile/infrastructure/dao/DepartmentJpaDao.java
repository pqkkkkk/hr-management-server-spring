package org.pqkkkkk.hr_management_server.modules.profile.infrastructure.dao;

import java.util.Optional;

import org.pqkkkkk.hr_management_server.modules.profile.domain.dao.DepartmentDao;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Department;
import org.pqkkkkk.hr_management_server.modules.profile.infrastructure.dao.jpa_repository.DepartmentRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA implementation of DepartmentDao.
 * <p>
 * Uses DepartmentRepository for database operations.
 */
@Repository
public class DepartmentJpaDao implements DepartmentDao {

    private final DepartmentRepository departmentRepository;

    public DepartmentJpaDao(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public Optional<Department> getDepartmentByName(String departmentName) {
        return departmentRepository.findByDepartmentName(departmentName);
    }

    @Override
    public Optional<Department> getDepartmentById(String departmentId) {
        return departmentRepository.findById(departmentId);
    }
}
