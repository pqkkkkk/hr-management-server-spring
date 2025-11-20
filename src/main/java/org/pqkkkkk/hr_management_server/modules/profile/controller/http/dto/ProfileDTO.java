package org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Department;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;

/**
 * ProfileDTO đứng riêng, chứa mapping to/from `User` entity.
 */
public record ProfileDTO(
        String userId,
        String fullName,
        String email,
        String role,
        String position,
        String departmentId
) {
    public User toEntity() {
        return User.builder()
                .userId(userId)
                .fullName(fullName)
                .email(email)
                .position(position)
                .role(role != null ? org.pqkkkkk.hr_management_server.modules.profile.domain.entity.UserRole.valueOf(role) : null)
                .department(departmentId != null ? Department.builder().departmentId(departmentId).build() : null)
                .build();
    }

    public static ProfileDTO fromEntity(User staff) {
        return new ProfileDTO(
                staff.getUserId(),
                staff.getFullName(),
                staff.getEmail(),
                staff.getRole() != null ? staff.getRole().name() : null,
                staff.getPosition(),
                staff.getDepartment() != null ? staff.getDepartment().getDepartmentId() : null
        );
    }
}
