package org.pqkkkkk.hr_management_server.modules.timesheet.domain.service.impl;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.service.ProfileQueryService;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.ShiftType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.LeaveDate;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.WfhDate;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.dao.DailyTimeSheetDao;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity.Constants;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity.DailyTimeSheet;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity.Enums.AttendanceStatus;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.service.TimeSheetCommandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of TimeSheetCommandService.
 * Handles all timesheet creation and update operations when requests are approved.
 */
@Service
@Transactional
public class TimeSheetCommandServiceImpl implements TimeSheetCommandService {

    private final DailyTimeSheetDao dailyTimeSheetDao;
    private final ProfileQueryService profileQueryService;

    public TimeSheetCommandServiceImpl(
            DailyTimeSheetDao dailyTimeSheetDao,
            ProfileQueryService profileQueryService) {
        this.dailyTimeSheetDao = dailyTimeSheetDao;
        this.profileQueryService = profileQueryService;
    }

    @Override
    public DailyTimeSheet handleCheckInApproval(String employeeId, LocalDateTime checkInTime) {
        // Validate inputs
        if (checkInTime == null) {
            throw new IllegalArgumentException(Constants.ERROR_NULL_TIME);
        }
        
        // Validate employee exists
        validateAndGetEmployee(employeeId);
        
        // Get check-in date
        LocalDate checkInDate = checkInTime.toLocalDate();
        
        // Get or create timesheet for the date
        DailyTimeSheet timeSheet = getOrCreateTimeSheet(employeeId, checkInDate);
        
        // Set check-in time
        timeSheet.setCheckInTime(checkInTime);
        
        // Update morning status if check-in is in the morning
        if (isMorningTime(checkInTime)) {
            timeSheet.setMorningStatus(AttendanceStatus.PRESENT);
        }
        
        // Calculate and set late minutes
        int lateMinutes = calculateLateMinutes(checkInTime);
        timeSheet.setLateMinutes(lateMinutes);
        
        // Save and return
        return dailyTimeSheetDao.updateDailyTimeSheet(timeSheet);
    }

    @Override
    public DailyTimeSheet handleCheckOutApproval(String employeeId, LocalDateTime checkOutTime) {
        // Validate inputs
        if (checkOutTime == null) {
            throw new IllegalArgumentException(Constants.ERROR_NULL_TIME);
        }
        
        // Validate employee exists
        validateAndGetEmployee(employeeId);
        
        // Get check-out date
        LocalDate checkOutDate = checkOutTime.toLocalDate();
        
        // Get existing timesheet (must exist with check-in)
        DailyTimeSheet timeSheet = dailyTimeSheetDao.getTimesheetByEmployeeAndDate(
            employeeId, 
            checkOutDate
        );
        
        if (timeSheet == null) {
            throw new IllegalArgumentException(
                String.format(Constants.ERROR_TIMESHEET_NOT_FOUND, employeeId, checkOutDate)
            );
        }
        
        // Validate check-in exists
        if (timeSheet.getCheckInTime() == null) {
            throw new IllegalArgumentException(Constants.ERROR_CHECKIN_REQUIRED);
        }
        
        // Validate check-out is after check-in
        if (checkOutTime.isBefore(timeSheet.getCheckInTime())) {
            throw new IllegalArgumentException(Constants.ERROR_INVALID_TIME_RANGE);
        }
        
        // Set check-out time
        timeSheet.setCheckOutTime(checkOutTime);
        
        // Update afternoon status if check-out is in the afternoon
        if (isAfternoonTime(checkOutTime)) {
            timeSheet.setAfternoonStatus(AttendanceStatus.PRESENT);
        }
        
        // Calculate and set early leave minutes
        int earlyLeaveMinutes = calculateEarlyLeaveMinutes(checkOutTime);
        timeSheet.setEarlyLeaveMinutes(earlyLeaveMinutes);
        
        // Calculate and set overtime minutes
        int overtimeMinutes = calculateOvertimeMinutes(checkOutTime);
        timeSheet.setOvertimeMinutes(overtimeMinutes);
        
        // Calculate and set total work credit based on actual working hours
        double workCredit = calculateWorkCredit(timeSheet.getCheckInTime(), checkOutTime);
        timeSheet.setTotalWorkCredit(workCredit);
        
        // Save and return
        return dailyTimeSheetDao.updateDailyTimeSheet(timeSheet);
    }

    @Override
    public List<DailyTimeSheet> handleLeaveApproval(String employeeId, List<LeaveDate> leaveDates) {
        // Validate inputs
        if (leaveDates == null || leaveDates.isEmpty()) {
            throw new IllegalArgumentException("Leave dates cannot be null or empty");
        }
        
        // Validate employee exists (without loading the full entity)
        validateAndGetEmployee(employeeId);
        
        List<DailyTimeSheet> timeSheets = new ArrayList<>();
        
        // Process each leave date
        for (LeaveDate leaveDate : leaveDates) {
            if (leaveDate.getDate() == null) {
                throw new IllegalArgumentException(Constants.ERROR_NULL_DATE);
            }
            
            // Get or create timesheet for the date (pass userId only to avoid circular reference)
            DailyTimeSheet timeSheet = getOrCreateTimeSheet(employeeId, leaveDate.getDate());
            
            // Set attendance status based on shift type
            ShiftType shift = leaveDate.getShift();
            if (shift == ShiftType.FULL_DAY) {
                timeSheet.setMorningStatus(AttendanceStatus.LEAVE);
                timeSheet.setAfternoonStatus(AttendanceStatus.LEAVE);
            } else if (shift == ShiftType.MORNING) {
                timeSheet.setMorningStatus(AttendanceStatus.LEAVE);
            } else if (shift == ShiftType.AFTERNOON) {
                timeSheet.setAfternoonStatus(AttendanceStatus.LEAVE);
            }
            
            // Leave days have zero work credit (unpaid leave)
            timeSheet.setTotalWorkCredit(Constants.ZERO_WORK_CREDIT);
            
            // Clear check-in/out times as they're not applicable for leave
            timeSheet.setCheckInTime(null);
            timeSheet.setCheckOutTime(null);
            timeSheet.setLateMinutes(0);
            timeSheet.setEarlyLeaveMinutes(0);
            timeSheet.setOvertimeMinutes(0);
            
            // Save timesheet
            DailyTimeSheet savedTimeSheet = dailyTimeSheetDao.updateDailyTimeSheet(timeSheet);
            timeSheets.add(savedTimeSheet);
        }
        
        return timeSheets;
    }

    @Override
    public List<DailyTimeSheet> handleWfhApproval(String employeeId, List<WfhDate> wfhDates) {
        // Validate inputs
        if (wfhDates == null || wfhDates.isEmpty()) {
            throw new IllegalArgumentException("WFH dates cannot be null or empty");
        }
        
        // Validate employee exists
        validateAndGetEmployee(employeeId);
        
        List<DailyTimeSheet> timeSheets = new ArrayList<>();
        
        // Process each WFH date
        for (WfhDate wfhDate : wfhDates) {
            if (wfhDate.getDate() == null) {
                throw new IllegalArgumentException(Constants.ERROR_NULL_DATE);
            }
            
            // Get or create timesheet for the date (pass userId only to avoid circular reference)
            DailyTimeSheet timeSheet = getOrCreateTimeSheet(employeeId, wfhDate.getDate());
            
            // Set WFH flags and attendance status based on shift type
            ShiftType shift = wfhDate.getShift();
            if (shift == ShiftType.FULL_DAY) {
                timeSheet.setMorningWfh(true);
                timeSheet.setAfternoonWfh(true);
                timeSheet.setMorningStatus(AttendanceStatus.PRESENT);
                timeSheet.setAfternoonStatus(AttendanceStatus.PRESENT);
                timeSheet.setTotalWorkCredit(Constants.FULL_DAY_WORK_CREDIT);
            } else if (shift == ShiftType.MORNING) {
                timeSheet.setMorningWfh(true);
                timeSheet.setMorningStatus(AttendanceStatus.PRESENT);
                // If afternoon is not set, use half day credit; otherwise keep existing
                if (timeSheet.getAfternoonStatus() == null) {
                    timeSheet.setTotalWorkCredit(Constants.HALF_DAY_WORK_CREDIT);
                } else {
                    // Combine with afternoon (if already present or WFH)
                    timeSheet.setTotalWorkCredit(
                        Constants.HALF_DAY_WORK_CREDIT + 
                        (timeSheet.getAfternoonStatus() == AttendanceStatus.PRESENT ? 
                            Constants.HALF_DAY_WORK_CREDIT : 0.0)
                    );
                }
            } else if (shift == ShiftType.AFTERNOON) {
                timeSheet.setAfternoonWfh(true);
                timeSheet.setAfternoonStatus(AttendanceStatus.PRESENT);
                // If morning is not set, use half day credit; otherwise keep existing
                if (timeSheet.getMorningStatus() == null) {
                    timeSheet.setTotalWorkCredit(Constants.HALF_DAY_WORK_CREDIT);
                } else {
                    // Combine with morning (if already present or WFH)
                    timeSheet.setTotalWorkCredit(
                        (timeSheet.getMorningStatus() == AttendanceStatus.PRESENT ? 
                            Constants.HALF_DAY_WORK_CREDIT : 0.0) +
                        Constants.HALF_DAY_WORK_CREDIT
                    );
                }
            }
            
            // WFH doesn't require check-in/out times, but keep if already exists
            // Reset time-based metrics for WFH days
            timeSheet.setLateMinutes(0);
            timeSheet.setEarlyLeaveMinutes(0);
            timeSheet.setOvertimeMinutes(0);
            
            // Save timesheet
            DailyTimeSheet savedTimeSheet = dailyTimeSheetDao.updateDailyTimeSheet(timeSheet);
            timeSheets.add(savedTimeSheet);
        }
        
        return timeSheets;
    }

    @Override
    public DailyTimeSheet handleTimesheetUpdateApproval(
            String employeeId,
            LocalDate targetDate,
            LocalDateTime newCheckInTime,
            LocalDateTime newCheckOutTime) {
        // Validate inputs
        if (targetDate == null) {
            throw new IllegalArgumentException(Constants.ERROR_NULL_DATE);
        }
        if (newCheckInTime == null || newCheckOutTime == null) {
            throw new IllegalArgumentException(Constants.ERROR_NULL_TIME);
        }
        
        // Validate check-out is after check-in
        if (newCheckOutTime.isBefore(newCheckInTime)) {
            throw new IllegalArgumentException(Constants.ERROR_INVALID_TIME_RANGE);
        }
        
        // Validate employee exists
        validateAndGetEmployee(employeeId);
        
        // Get existing timesheet (must exist)
        DailyTimeSheet timeSheet = dailyTimeSheetDao.getTimesheetByEmployeeAndDate(
            employeeId, 
            targetDate
        );
        
        if (timeSheet == null) {
            throw new IllegalArgumentException(
                String.format(Constants.ERROR_TIMESHEET_NOT_FOUND, employeeId, targetDate)
            );
        }
        
        // Validate timesheet is not finalized
        if (Boolean.TRUE.equals(timeSheet.getIsFinalized())) {
            throw new IllegalArgumentException(
                String.format(Constants.ERROR_TIMESHEET_FINALIZED, targetDate)
            );
        }
        
        // Update check-in and check-out times
        timeSheet.setCheckInTime(newCheckInTime);
        timeSheet.setCheckOutTime(newCheckOutTime);
        
        // Update morning status if check-in is in the morning
        if (isMorningTime(newCheckInTime)) {
            timeSheet.setMorningStatus(AttendanceStatus.PRESENT);
        }
        
        // Update afternoon status if check-out is in the afternoon
        if (isAfternoonTime(newCheckOutTime)) {
            timeSheet.setAfternoonStatus(AttendanceStatus.PRESENT);
        }
        
        // Recalculate all time-based metrics
        int lateMinutes = calculateLateMinutes(newCheckInTime);
        timeSheet.setLateMinutes(lateMinutes);
        
        int earlyLeaveMinutes = calculateEarlyLeaveMinutes(newCheckOutTime);
        timeSheet.setEarlyLeaveMinutes(earlyLeaveMinutes);
        
        int overtimeMinutes = calculateOvertimeMinutes(newCheckOutTime);
        timeSheet.setOvertimeMinutes(overtimeMinutes);
        
        // Recalculate total work credit based on new working hours
        double workCredit = calculateWorkCredit(newCheckInTime, newCheckOutTime);
        timeSheet.setTotalWorkCredit(workCredit);
        
        // Reset WFH flags as this is a timesheet update (not WFH)
        timeSheet.setMorningWfh(false);
        timeSheet.setAfternoonWfh(false);
        
        // Save and return
        return dailyTimeSheetDao.updateDailyTimeSheet(timeSheet);
    }

    // ============= Helper Methods =============

    /**
     * Validates employee ID is not null and retrieves employee entity.
     * 
     * @param employeeId Employee ID to validate
     * @return User entity
     * @throws IllegalArgumentException if employeeId is null or employee not found
     */
    private User validateAndGetEmployee(String employeeId) {
        if (employeeId == null) {
            throw new IllegalArgumentException(Constants.ERROR_NULL_EMPLOYEE_ID);
        }
        
        User employee = profileQueryService.getProfileById(employeeId);
        if (employee == null) {
            throw new IllegalArgumentException(
                String.format(Constants.ERROR_EMPLOYEE_NOT_FOUND, employeeId)
            );
        }
        
        return employee;
    }

    /**
     * Gets existing timesheet or creates a new one for the specified employee and date.
     * 
     * @param employeeId Employee user ID
     * @param date Date for the timesheet
     * @return Existing or newly created DailyTimeSheet
     */
    private DailyTimeSheet getOrCreateTimeSheet(String employeeId, LocalDate date) {
        DailyTimeSheet timeSheet = dailyTimeSheetDao.getTimesheetByEmployeeAndDate(
            employeeId, 
            date
        );
        
        if (timeSheet == null) {
            // Create a new timesheet with employee reference (let JPA manage the relationship)
            User employeeRef = User.builder().userId(employeeId).build();
            timeSheet = DailyTimeSheet.builder()
                .employee(employeeRef)
                .date(date)
                .isFinalized(false)
                .totalWorkCredit(Constants.ZERO_WORK_CREDIT)
                .lateMinutes(0)
                .earlyLeaveMinutes(0)
                .overtimeMinutes(0)
                .morningWfh(false)
                .afternoonWfh(false)
                .build();
        }
        
        return timeSheet;
    }

    /**
     * Calculates late minutes if check-in is after standard time (8:00 AM).
     * 
     * @param checkInTime The check-in time
     * @return Late minutes (0 if on time or early)
     */
    private int calculateLateMinutes(LocalDateTime checkInTime) {
        LocalTime checkInLocalTime = checkInTime.toLocalTime();
        
        if (checkInLocalTime.isAfter(Constants.STANDARD_CHECK_IN_TIME)) {
            long minutesLate = Duration.between(
                Constants.STANDARD_CHECK_IN_TIME, 
                checkInLocalTime
            ).toMinutes();
            return (int) minutesLate;
        }
        
        return 0;
    }

    /**
     * Calculates early leave minutes if check-out is before standard time (5:00 PM).
     * 
     * @param checkOutTime The check-out time
     * @return Early leave minutes (0 if on time or late)
     */
    private int calculateEarlyLeaveMinutes(LocalDateTime checkOutTime) {
        LocalTime checkOutLocalTime = checkOutTime.toLocalTime();
        
        if (checkOutLocalTime.isBefore(Constants.STANDARD_CHECK_OUT_TIME)) {
            long minutesEarly = Duration.between(
                checkOutLocalTime,
                Constants.STANDARD_CHECK_OUT_TIME
            ).toMinutes();
            return (int) minutesEarly;
        }
        
        return 0;
    }

    /**
     * Calculates overtime minutes if check-out is after standard time (5:00 PM).
     * 
     * @param checkOutTime The check-out time
     * @return Overtime minutes (0 if no overtime)
     */
    private int calculateOvertimeMinutes(LocalDateTime checkOutTime) {
        LocalTime checkOutLocalTime = checkOutTime.toLocalTime();
        
        if (checkOutLocalTime.isAfter(Constants.STANDARD_CHECK_OUT_TIME)) {
            long minutesOvertime = Duration.between(
                Constants.STANDARD_CHECK_OUT_TIME,
                checkOutLocalTime
            ).toMinutes();
            return (int) minutesOvertime;
        }
        
        return 0;
    }

    /**
     * Calculates work credit based on actual working hours.
     * Work credit is proportional to standard 9-hour working day (8:00 AM - 5:00 PM).
     * Formula: (actual working minutes) / (standard working minutes = 540)
     * 
     * Examples:
     * - 9 hours (540 min) = 1.0 credit
     * - 8 hours (480 min) = 0.889 credit ≈ 0.89
     * - 4.5 hours (270 min) = 0.5 credit
     * 
     * @param checkInTime Check-in time
     * @param checkOutTime Check-out time
     * @return Work credit (0.0 to 1.0+)
     */
    private double calculateWorkCredit(LocalDateTime checkInTime, LocalDateTime checkOutTime) {
        if (checkInTime == null || checkOutTime == null) {
            return Constants.ZERO_WORK_CREDIT;
        }
        
        long actualWorkingMinutes = Duration.between(checkInTime, checkOutTime).toMinutes();
        
        if (actualWorkingMinutes <= 0) {
            return Constants.ZERO_WORK_CREDIT;
        }
        
        // Calculate proportional work credit
        double workCredit = (double) actualWorkingMinutes / Constants.STANDARD_WORKING_MINUTES;
        
        // Round to 3 decimal places for precision
        return Math.round(workCredit * 1000.0) / 1000.0;
    }

    /**
     * Determines if a given time is in the morning shift (before noon).
     * 
     * @param time The time to check
     * @return true if morning, false if afternoon
     */
    private boolean isMorningTime(LocalDateTime time) {
        return time.toLocalTime().isBefore(Constants.NOON_TIME);
    }

    /**
     * Determines if a given time is in the afternoon shift (noon or after).
     * 
     * @param time The time to check
     * @return true if afternoon, false if morning
     */
    private boolean isAfternoonTime(LocalDateTime time) {
        return !isMorningTime(time);
    }
}
