package org.pqkkkkk.hr_management_server.modules.request.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity.Enums.AttendanceStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
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
@Table(name = "additional_timesheet_info_table")
@Getter
@Setter
@ToString(exclude = { "request" })
@EqualsAndHashCode(exclude = { "request" })
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AdditionalTimesheetInfo {

    @Id
    @Column(name = "request_id")
    String requestId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "request_id")
    Request request;

    @Column(name = "desired_check_in_time", nullable = false)
    LocalDateTime desiredCheckInTime;

    @Column(name = "current_check_in_time", nullable = false)
    LocalDateTime currentCheckInTime;

    @Column(name = "desired_check_out_time", nullable = false)
    LocalDateTime desiredCheckOutTime;

    @Column(name = "current_check_out_time", nullable = false)
    LocalDateTime currentCheckOutTime;

    @Column(name = "target_date", nullable = false)
    LocalDate targetDate;

    // Extended fields for comprehensive timesheet update
    @Enumerated(EnumType.STRING)
    @Column(name = "desired_morning_status")
    AttendanceStatus desiredMorningStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "desired_afternoon_status")
    AttendanceStatus desiredAfternoonStatus;

    @Column(name = "desired_morning_wfh")
    Boolean desiredMorningWfh;

    @Column(name = "desired_afternoon_wfh")
    Boolean desiredAfternoonWfh;
}
