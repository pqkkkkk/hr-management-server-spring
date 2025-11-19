package org.pqkkkkk.hr_management_server.modules.profile.domain.Mappers;

import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.StaffDTO;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Department;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Staff;
import org.springframework.stereotype.Component;

@Component
public class StaffMapper {

    public StaffDTO toDTO(Staff staff) {
        if (staff == null) {
            return null;
        }

        StaffDTO dto = new StaffDTO();
        dto.setId(staff.getId().toString());
        dto.setName(staff.getName());
        dto.setDateOfBirth(staff.getDateOfBirth());
        dto.setPhoneNumber(staff.getPhoneNumber());
        dto.setEmail(staff.getEmail());
        dto.setPosition(staff.getPosition());

        if (staff.getDepartment() != null) {
            dto.setDepartmentId(staff.getDepartment().getDepartmentId());
            dto.setDepartmentName(staff.getDepartment().getDepartmentName());
        }

        return dto;
    }

    public Staff toEntity(StaffDTO dto) {
        if (dto == null) {
            return null;
        }

        Staff staff = new Staff();
        staff.setName(dto.getName());
        staff.setDateOfBirth(dto.getDateOfBirth());
        staff.setPhoneNumber(dto.getPhoneNumber());
        staff.setEmail(dto.getEmail());
        staff.setPosition(dto.getPosition());

        if (dto.getDepartmentId() != null) {
            Department department = new Department();
            department.setDepartmentId(dto.getDepartmentId());
            department.setDepartmentName(dto.getDepartmentName());
            staff.setDepartment(department);
        }

        return staff;
    }
}
