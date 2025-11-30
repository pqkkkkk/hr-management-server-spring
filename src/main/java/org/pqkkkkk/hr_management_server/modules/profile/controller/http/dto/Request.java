package org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Department;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;

import jakarta.validation.constraints.*;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserGender;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserPosition;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserRole;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserStatus;
import org.pqkkkkk.hr_management_server.modules.profile.domain.filter.FilterCriteria.ProfileFilter;
import org.pqkkkkk.hr_management_server.shared.Constants.SupportedFileFormat;

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

    public record ExportProfilesRequest(
            @NotNull(message = "File format is required")
            SupportedFileFormat fileFormat,
            ProfileFilter filter
    ){}
    public record UpdateUserForEmployeeRequest(

            String fullName,

            @Email(message = "Invalid email format")
            String email,

            @Pattern(regexp = "^0\\d{9}$", message = "Phone number must be 10 digits starting with 0")
            String phoneNumber,

            String address

    ) {
        public User toEntity() {
            User.UserBuilder builder = User.builder();
            
            if (fullName != null && !fullName.isBlank()) {
                builder.fullName(fullName);
            }
            if (email != null && !email.isBlank()) {
                builder.email(email);
            }
            if (phoneNumber != null && !phoneNumber.isBlank()) {
                builder.phoneNumber(phoneNumber);
            }
            if (address != null && !address.isBlank()) {
                builder.address(address);
            }
            
            return builder.build();
        }
    }

}
