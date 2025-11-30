package org.pqkkkkk.hr_management_server.modules.profile.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserGender;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserPosition;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserRole;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserStatus;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExportedUser {
    String userId;
    String fullName;
    String email;
    UserRole role;
    UserStatus status;
    UserGender gender;
    UserPosition position;
    LocalDate joinDate;
    String identityCardNumber;
    String phoneNumber;
    LocalDate dateOfBirth;
    String address;
    String bankAccountNumber;
    String bankName;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String departmentName;
    String departmentId;

    public ExportedUser(User user){
        this.userId = user.getUserId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.status = user.getStatus();
        this.gender = user.getGender();
        this.position = user.getPosition();
        this.joinDate = user.getJoinDate();
        this.identityCardNumber = user.getIdentityCardNumber();
        this.phoneNumber = user.getPhoneNumber();
        this.dateOfBirth = user.getDateOfBirth();
        this.address = user.getAddress();
        this.bankAccountNumber = user.getBankAccountNumber();
        this.bankName = user.getBankName();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.departmentId = user.getDepartment() != null ? user.getDepartment().getDepartmentId() : null;
        this.departmentName = user.getDepartment() != null ? user.getDepartment().getDepartmentName() : null;
    }
}
