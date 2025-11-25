package org.pqkkkkk.hr_management_server.modules.profile.infrastructure.dao;

import org.pqkkkkk.hr_management_server.modules.profile.domain.dao.DepartmentDao;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Department;
import org.pqkkkkk.hr_management_server.modules.profile.infrastructure.dao.jpa_repository.DepartmentRepository;

public class DepartmentJpaDao implements DepartmentDao {
    private final DepartmentRepository departmentRepository;

    DepartmentJpaDao(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public Department createDepartment(Department department) {
        return departmentRepository.save(department);
    }
}
