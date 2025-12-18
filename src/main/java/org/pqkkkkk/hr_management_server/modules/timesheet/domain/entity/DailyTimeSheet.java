package org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing daily timesheet for an employee
 */
@Entity
@Table(name = "daily_timesheet_table")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DailyTimeSheet {

    @Id
    @Column(name = "daily_ts_id")
    @UuidGenerator
    String dailyTsId;

    @Column(name = "date", nullable = false)
    LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "morning_status")
    Enums.AttendanceStatus morningStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "afternoon_status")
    Enums.AttendanceStatus afternoonStatus;

    @Column(name = "morning_wfh")
    Boolean morningWfh;

    @Column(name = "afternoon_wfh")
    Boolean afternoonWfh;

    @Column(name = "total_work_credit")
    Double totalWorkCredit;

    @Column(name = "check_in_time")
    LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    LocalDateTime checkOutTime;

    @Column(name = "late_minutes")
    Integer lateMinutes;

    @Column(name = "early_leave_minutes")
    Integer earlyLeaveMinutes;

    @Column(name = "overtime_minutes")
    Integer overtimeMinutes;

    @Column(name = "is_finalized")
    Boolean isFinalized;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    User employee;
}
