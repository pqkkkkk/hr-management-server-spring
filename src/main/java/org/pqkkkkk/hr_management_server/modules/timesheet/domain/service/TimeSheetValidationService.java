package org.pqkkkkk.hr_management_server.modules.timesheet.domain.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.LeaveDate;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.WfhDate;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.ShiftType;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity.DailyTimeSheet;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity.Enums.AttendanceStatus;

/**
 * Service for validating timesheet operations before approval.
 * Each validation method throws an exception if validation fails.
 */
public interface TimeSheetValidationService {

    // ===== Check-in/out validations =====

    /**
     * Validates check-in approval.
     * - Throws if timesheet already has check-in for the date
     * - Throws if date is a full-day leave
     */
    void validateCheckInApproval(String employeeId, LocalDateTime checkInTime);

    /**
     * Validates check-out approval.
     * - Throws if no existing check-in for the date
     * - Throws if timesheet already has check-out
     * - Throws if check-out time is before check-in
     */
    void validateCheckOutApproval(String employeeId, LocalDateTime checkOutTime);

    // ===== Leave/WFH validations =====

    /**
     * Validates leave approval for given dates.
     * - Throws if any shift has conflict with existing PRESENT/WFH status
     */
    void validateLeaveApproval(String employeeId, List<LeaveDate> leaveDates);

    /**
     * Validates WFH approval for given dates.
     * - Throws if any shift has conflict with existing status
     */
    void validateWfhApproval(String employeeId, List<WfhDate> wfhDates);

    // ===== Timesheet update validation =====

    /**
     * Validates timesheet update approval.
     * - Throws if timesheet doesn't exist
     * - Throws if timesheet is finalized
     * - Throws if requested state is inconsistent
     */
    void validateTimesheetUpdateApproval(String employeeId, LocalDate targetDate,
            LocalDateTime desiredCheckIn, LocalDateTime desiredCheckOut,
            AttendanceStatus desiredMorningStatus, AttendanceStatus desiredAfternoonStatus,
            Boolean desiredMorningWfh, Boolean desiredAfternoonWfh);

    // ===== Utility methods =====

    /**
     * Checks if timesheet represents a full-day leave.
     */
    boolean isFullDayLeave(DailyTimeSheet timeSheet);

    /**
     * Checks if there's a conflict when trying to set a new status for a shift.
     */
    boolean hasShiftConflict(DailyTimeSheet timeSheet, ShiftType shift, AttendanceStatus newStatus);
}
