package org.pqkkkkk.hr_management_server.modules.profile.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.UuidGenerator;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserGender;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserPosition;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserRole;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "user_table")
@Getter
@Setter
@ToString(exclude = { "manager", "subordinates" })
@EqualsAndHashCode(exclude = { "manager", "subordinates" })
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class User {
    @Id
    @Column(name = "user_id")
    @UuidGenerator
    String userId;

    @Column(name = "full_name")
    String fullName;

    @Column(name = "email")
    String email;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    UserGender gender;

    @Column(name = "position")
    @Enumerated(EnumType.STRING)
    UserPosition position;

    @Column(name = "join_date")
    LocalDate joinDate;

    @Column(name = "identity_card_number")
    String identityCardNumber;

    @Column(name = "phone_number")
    String phoneNumber;

    @Column(name = "date_of_birth")
    LocalDate dateOfBirth;

    @Column(name = "address")
    String address;

    @Column(name = "bank_account_number")
    String bankAccountNumber;

    @Column(name = "bank_name")
    String bankName;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "department_id")
    Department department;

    // Manager relationship (self-referencing)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    User manager;

    // List of subordinates (employees managed by this user)
    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    @JsonIgnore
    List<User> subordinates;

    // Annual leave balance
    @Column(name = "max_annual_leave")
    @Builder.Default
    Integer maxAnnualLeave = 12;

    @Column(name = "remaining_annual_leave")
    @Builder.Default
    BigDecimal remainingAnnualLeave = new BigDecimal("12.0");

    // Work From Home (WFH) days balance
    @Column(name = "max_wfh_days", precision = 4, scale = 1)
    @Builder.Default
    BigDecimal maxWfhDays = new BigDecimal("10.0");

    @Column(name = "remaining_wfh_days", precision = 4, scale = 1)
    @Builder.Default
    BigDecimal remainingWfhDays = new BigDecimal("10.0");
}
