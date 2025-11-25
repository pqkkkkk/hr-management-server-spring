package org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto;

import jakarta.validation.constraints.NotBlank;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Department;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserGender;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserPosition;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserRole;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserStatus;

import java.time.LocalDate;

/**
 * Request DTOs for Profile module.
 * <p> They are used to encapsulate data sent by clients in HTTP requests.
 * <p> Each request DTO includes methods to convert to entity objects.
 */
public class Request {
    public record UpdateUserForHRRequest(
            String fullName,
            String email,
            UserRole role,
            UserStatus status,
            UserGender gender,
            UserPosition position,
            LocalDate joinDate,
            String identityCardNumber,
            String phoneNumber,
            LocalDate dateOfBirth,
            String address,
            String bankAccountNumber,
            String bankName,
            String departmentId
    ){
        public User toEntity(){
            return User.builder()
                    .fullName(fullName)
                    .email(email)
                    .role(role)
                    .status(status)
                    .gender(gender)
                    .position(position)
                    .joinDate(joinDate)
                    .identityCardNumber(identityCardNumber)
                    .phoneNumber(phoneNumber)
                    .dateOfBirth(dateOfBirth)
                    .address(address)
                    .bankAccountNumber(bankAccountNumber)
                    .bankName(bankName)
                    .department(Department.builder().departmentId(departmentId).build())
                    .build();
        }
    }

    public record DepartmentRequest(
            @NotBlank(message = "Department name is required")
            String departmentName
    ){
        public Department toEntity(){
            return Department.builder()
                    .departmentName(departmentName)
                    .build();
        }
    }
}
