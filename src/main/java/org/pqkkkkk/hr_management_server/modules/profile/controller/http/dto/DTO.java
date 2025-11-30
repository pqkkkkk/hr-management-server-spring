package org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserGender;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserPosition;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserRole;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Objects for Profile module.
 * <p> Each DTO includes methods to convert to and from entity objects
 */
public class DTO {
    public record UserDTO(
            String userId,
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
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String departmentId,
            String departmentName
    ){
        public User toEntity(){
            return User.builder()
                    .userId(userId)
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
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();
        }
        public static UserDTO fromEntity(User user){
            return new UserDTO(
                    user.getUserId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole(),
                    user.getStatus(),
                    user.getGender(),
                    user.getPosition(),
                    user.getJoinDate(),
                    user.getIdentityCardNumber(),
                    user.getPhoneNumber(),
                    user.getDateOfBirth(),
                    user.getAddress(),
                    user.getBankAccountNumber(),
                    user.getBankName(),
                    user.getCreatedAt(),
                    user.getUpdatedAt(),
                    user.getDepartment() != null ? user.getDepartment().getDepartmentId() : null,
                    user.getDepartment() != null ? user.getDepartment().getDepartmentName() : null
            );
        }
    }
    public record ExportProfilesDTO(
            String fileUrl
    ){}
}
