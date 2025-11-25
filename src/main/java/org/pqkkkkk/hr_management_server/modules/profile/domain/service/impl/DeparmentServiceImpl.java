package org.pqkkkkk.hr_management_server.modules.profile.domain.service.impl;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Department;
import org.pqkkkkk.hr_management_server.modules.profile.domain.service.DeparmentSeviceI;
import org.pqkkkkk.hr_management_server.modules.profile.infrastructure.dao.jpa_repository.DepartmentRepository;
import org.springframework.stereotype.Service;

@Service
public class DeparmentServiceImpl implements DeparmentSeviceI {
    private final DepartmentRepository departmentRepository;

    DeparmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public Department createDepartment(Department department) {
        return departmentRepository.save(department);
    }
}
