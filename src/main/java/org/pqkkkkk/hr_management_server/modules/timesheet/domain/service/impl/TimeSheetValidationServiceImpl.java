package org.pqkkkkk.hr_management_server.modules.timesheet.domain.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.LeaveDate;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.WfhDate;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.ShiftType;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.dao.DailyTimeSheetDao;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity.DailyTimeSheet;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity.Enums.AttendanceStatus;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.service.TimeSheetValidationService;
import org.springframework.stereotype.Service;

/**
 * Implementation of TimeSheetValidationService.
 * Validates timesheet operations before approval to prevent data inconsistency.
 */
@Service
public class TimeSheetValidationServiceImpl implements TimeSheetValidationService {

    private final DailyTimeSheetDao dailyTimeSheetDao;

    public TimeSheetValidationServiceImpl(DailyTimeSheetDao dailyTimeSheetDao) {
        this.dailyTimeSheetDao = dailyTimeSheetDao;
    }

    // ===== Check-in/out validations =====

    @Override
    public void validateCheckInApproval(String employeeId, LocalDateTime checkInTime) {
        if (employeeId == null || checkInTime == null) {
            throw new IllegalArgumentException("Employee ID and check-in time are required");
        }

        LocalDate checkInDate = checkInTime.toLocalDate();
        DailyTimeSheet existing = dailyTimeSheetDao.getTimesheetByEmployeeAndDate(employeeId, checkInDate);

        if (existing != null) {
            // Check duplicate check-in
            if (existing.getCheckInTime() != null) {
                throw new IllegalStateException(
                        "Timesheet for " + checkInDate + " already has check-in at " +
                                existing.getCheckInTime() + ". Use Timesheet Update request instead.");
            }

            // Check conflict with full-day leave
            if (isFullDayLeave(existing)) {
                throw new IllegalStateException(
                        "Cannot check-in on " + checkInDate + " which is a full-day leave.");
            }

            // Check conflict with morning leave (if check-in is in morning)
            if (isMorningTime(checkInTime) && existing.getMorningStatus() == AttendanceStatus.LEAVE) {
                throw new IllegalStateException(
                        "Cannot check-in in morning on " + checkInDate + " which has morning leave.");
            }
        }
    }

    @Override
    public void validateCheckOutApproval(String employeeId, LocalDateTime checkOutTime) {
        if (employeeId == null || checkOutTime == null) {
            throw new IllegalArgumentException("Employee ID and check-out time are required");
        }

        LocalDate checkOutDate = checkOutTime.toLocalDate();
        DailyTimeSheet existing = dailyTimeSheetDao.getTimesheetByEmployeeAndDate(employeeId, checkOutDate);

        if (existing == null) {
            throw new IllegalStateException(
                    "No timesheet found for " + checkOutDate + ". Check-in must be approved first.");
        }

        if (existing.getCheckInTime() == null) {
            throw new IllegalStateException(
                    "Timesheet has no check-in. Check-in must be approved before check-out.");
        }

        if (existing.getCheckOutTime() != null) {
            throw new IllegalStateException(
                    "Timesheet already has check-out at " + existing.getCheckOutTime() +
                            ". Use Timesheet Update request instead.");
        }

        if (checkOutTime.isBefore(existing.getCheckInTime())) {
            throw new IllegalArgumentException(
                    "Check-out time " + checkOutTime + " cannot be before check-in time " +
                            existing.getCheckInTime());
        }

        // Check conflict with afternoon leave
        if (isAfternoonTime(checkOutTime) && existing.getAfternoonStatus() == AttendanceStatus.LEAVE) {
            throw new IllegalStateException(
                    "Cannot check-out in afternoon on " + checkOutDate + " which has afternoon leave.");
        }
    }

    // ===== Leave/WFH validations =====

    @Override
    public void validateLeaveApproval(String employeeId, List<LeaveDate> leaveDates) {
        if (employeeId == null || leaveDates == null || leaveDates.isEmpty()) {
            throw new IllegalArgumentException("Employee ID and leave dates are required");
        }

        for (LeaveDate leaveDate : leaveDates) {
            LocalDate date = leaveDate.getDate();
            ShiftType shift = leaveDate.getShift();
            DailyTimeSheet existing = dailyTimeSheetDao.getTimesheetByEmployeeAndDate(employeeId, date);

            if (existing != null && hasShiftConflict(existing, shift, AttendanceStatus.LEAVE)) {
                throw new IllegalStateException(
                        "Conflict on " + date + ": Cannot set " + shift + " to LEAVE. " +
                                "Current status - Morning: " + existing.getMorningStatus() +
                                ", Afternoon: " + existing.getAfternoonStatus());
            }
        }
    }

    @Override
    public void validateWfhApproval(String employeeId, List<WfhDate> wfhDates) {
        if (employeeId == null || wfhDates == null || wfhDates.isEmpty()) {
            throw new IllegalArgumentException("Employee ID and WFH dates are required");
        }

        for (WfhDate wfhDate : wfhDates) {
            LocalDate date = wfhDate.getDate();
            ShiftType shift = wfhDate.getShift();
            DailyTimeSheet existing = dailyTimeSheetDao.getTimesheetByEmployeeAndDate(employeeId, date);

            if (existing != null) {
                // Check conflict - WFH should not conflict with LEAVE
                if (shift == ShiftType.FULL_DAY || shift == ShiftType.MORNING) {
                    if (existing.getMorningStatus() == AttendanceStatus.LEAVE) {
                        throw new IllegalStateException(
                                "Conflict on " + date + ": Cannot set morning to WFH when it's already LEAVE.");
                    }
                }
                if (shift == ShiftType.FULL_DAY || shift == ShiftType.AFTERNOON) {
                    if (existing.getAfternoonStatus() == AttendanceStatus.LEAVE) {
                        throw new IllegalStateException(
                                "Conflict on " + date + ": Cannot set afternoon to WFH when it's already LEAVE.");
                    }
                }
            }
        }
    }

    // ===== Timesheet update validation =====

    @Override
    public void validateTimesheetUpdateApproval(String employeeId, LocalDate targetDate,
            LocalDateTime desiredCheckIn, LocalDateTime desiredCheckOut,
            AttendanceStatus desiredMorningStatus, AttendanceStatus desiredAfternoonStatus,
            Boolean desiredMorningWfh, Boolean desiredAfternoonWfh) {

        if (employeeId == null || targetDate == null) {
            throw new IllegalArgumentException("Employee ID and target date are required");
        }

        DailyTimeSheet existing = dailyTimeSheetDao.getTimesheetByEmployeeAndDate(employeeId, targetDate);

        if (existing == null) {
            throw new IllegalStateException(
                    "No timesheet found for " + targetDate + ". Cannot update non-existent timesheet.");
        }

        if (Boolean.TRUE.equals(existing.getIsFinalized())) {
            throw new IllegalStateException(
                    "Timesheet for " + targetDate + " is finalized and cannot be modified.");
        }

        // Validate consistency of requested state
        validateRequestedStateConsistency(
                desiredCheckIn, desiredCheckOut,
                desiredMorningStatus, desiredAfternoonStatus,
                desiredMorningWfh, desiredAfternoonWfh);
    }

    /**
     * Validates that the requested timesheet state is logically consistent.
     */
    private void validateRequestedStateConsistency(
            LocalDateTime checkIn, LocalDateTime checkOut,
            AttendanceStatus morningStatus, AttendanceStatus afternoonStatus,
            Boolean morningWfh, Boolean afternoonWfh) {

        // Rule 1: If PRESENT and not WFH, check times should be provided
        if (morningStatus == AttendanceStatus.PRESENT && !Boolean.TRUE.equals(morningWfh)) {
            if (checkIn == null) {
                throw new IllegalArgumentException(
                        "Check-in time is required when setting morning status to PRESENT (not WFH).");
            }
        }

        if (afternoonStatus == AttendanceStatus.PRESENT && !Boolean.TRUE.equals(afternoonWfh)) {
            if (checkOut == null) {
                throw new IllegalArgumentException(
                        "Check-out time is required when setting afternoon status to PRESENT (not WFH).");
            }
        }

        // Rule 2: If both LEAVE, check times should not be provided
        if (morningStatus == AttendanceStatus.LEAVE && afternoonStatus == AttendanceStatus.LEAVE) {
            if (checkIn != null || checkOut != null) {
                throw new IllegalArgumentException(
                        "Check times should not be provided for full-day leave.");
            }
        }

        // Rule 3: Check-out must be after check-in
        if (checkIn != null && checkOut != null && !checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException(
                    "Check-out time must be after check-in time.");
        }
    }

    // ===== Utility methods =====

    @Override
    public boolean isFullDayLeave(DailyTimeSheet timeSheet) {
        if (timeSheet == null)
            return false;
        return timeSheet.getMorningStatus() == AttendanceStatus.LEAVE &&
                timeSheet.getAfternoonStatus() == AttendanceStatus.LEAVE;
    }

    @Override
    public boolean hasShiftConflict(DailyTimeSheet timeSheet, ShiftType shift, AttendanceStatus newStatus) {
        if (timeSheet == null)
            return false;

        if (shift == ShiftType.FULL_DAY) {
            // For full day, conflict if either shift has a different non-null status
            AttendanceStatus morning = timeSheet.getMorningStatus();
            AttendanceStatus afternoon = timeSheet.getAfternoonStatus();

            return (morning != null && morning != newStatus) ||
                    (afternoon != null && afternoon != newStatus);
        } else if (shift == ShiftType.MORNING) {
            AttendanceStatus morning = timeSheet.getMorningStatus();
            return morning != null && morning != newStatus;
        } else { // AFTERNOON
            AttendanceStatus afternoon = timeSheet.getAfternoonStatus();
            return afternoon != null && afternoon != newStatus;
        }
    }

    // ===== Helper methods =====

    private boolean isMorningTime(LocalDateTime time) {
        return time.getHour() < 12;
    }

    private boolean isAfternoonTime(LocalDateTime time) {
        return time.getHour() >= 12;
    }
}
