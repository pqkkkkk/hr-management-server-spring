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
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.service.TimeSheetValidationService;
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
 * Handles all timesheet creation and update operations when requests are
 * approved.
 */
@Service
@Transactional
public class TimeSheetCommandServiceImpl implements TimeSheetCommandService {

    private final DailyTimeSheetDao dailyTimeSheetDao;
    private final ProfileQueryService profileQueryService;
    private final TimeSheetValidationService validationService;

    public TimeSheetCommandServiceImpl(
            DailyTimeSheetDao dailyTimeSheetDao,
            ProfileQueryService profileQueryService,
            TimeSheetValidationService validationService) {
        this.dailyTimeSheetDao = dailyTimeSheetDao;
        this.profileQueryService = profileQueryService;
        this.validationService = validationService;
    }

    @Override
    public DailyTimeSheet handleCheckInApproval(String employeeId, LocalDateTime checkInTime) {
        // Validate inputs
        if (checkInTime == null) {
            throw new IllegalArgumentException(Constants.ERROR_NULL_TIME);
        }

        // Validate employee exists
        validateAndGetEmployee(employeeId);

        // Validate check-in approval (duplicate, conflict checks)
        validationService.validateCheckInApproval(employeeId, checkInTime);

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

        // Validate check-out approval (duplicate, conflict, time order checks)
        validationService.validateCheckOutApproval(employeeId, checkOutTime);

        // Get check-out date
        LocalDate checkOutDate = checkOutTime.toLocalDate();

        // Get existing timesheet
        DailyTimeSheet timeSheet = dailyTimeSheetDao.getTimesheetByEmployeeAndDate(
                employeeId,
                checkOutDate);

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

        // Validate employee exists
        validateAndGetEmployee(employeeId);

        // Validate leave approval (conflict checks)
        validationService.validateLeaveApproval(employeeId, leaveDates);

        List<DailyTimeSheet> timeSheets = new ArrayList<>();

        // Process each leave date
        for (LeaveDate leaveDate : leaveDates) {
            if (leaveDate.getDate() == null) {
                throw new IllegalArgumentException(Constants.ERROR_NULL_DATE);
            }

            DailyTimeSheet timeSheet = getOrCreateTimeSheet(employeeId, leaveDate.getDate());
            ShiftType shift = leaveDate.getShift();

            if (shift == ShiftType.FULL_DAY) {
                // Full day leave - clear everything
                timeSheet.setMorningStatus(AttendanceStatus.LEAVE);
                timeSheet.setAfternoonStatus(AttendanceStatus.LEAVE);
                timeSheet.setTotalWorkCredit(Constants.ZERO_WORK_CREDIT);
                timeSheet.setCheckInTime(null);
                timeSheet.setCheckOutTime(null);
                timeSheet.setLateMinutes(0);
                timeSheet.setEarlyLeaveMinutes(0);
                timeSheet.setOvertimeMinutes(0);
            } else if (shift == ShiftType.MORNING) {
                // Morning leave - preserve afternoon data
                timeSheet.setMorningStatus(AttendanceStatus.LEAVE);
                // Clear morning check-in only if no afternoon work
                if (timeSheet.getAfternoonStatus() != AttendanceStatus.PRESENT) {
                    timeSheet.setCheckInTime(null);
                }
                // Recalculate work credit based on afternoon status
                if (timeSheet.getAfternoonStatus() == AttendanceStatus.PRESENT) {
                    timeSheet.setTotalWorkCredit(Constants.HALF_DAY_WORK_CREDIT);
                } else {
                    timeSheet.setTotalWorkCredit(Constants.ZERO_WORK_CREDIT);
                }
                timeSheet.setLateMinutes(0); // No late for morning leave
            } else if (shift == ShiftType.AFTERNOON) {
                // Afternoon leave - preserve morning data
                timeSheet.setAfternoonStatus(AttendanceStatus.LEAVE);
                // Clear checkout only, preserve check-in
                timeSheet.setCheckOutTime(null);
                // Recalculate work credit based on morning status
                if (timeSheet.getMorningStatus() == AttendanceStatus.PRESENT) {
                    timeSheet.setTotalWorkCredit(Constants.HALF_DAY_WORK_CREDIT);
                } else {
                    timeSheet.setTotalWorkCredit(Constants.ZERO_WORK_CREDIT);
                }
                timeSheet.setEarlyLeaveMinutes(0); // No early leave tracking
                timeSheet.setOvertimeMinutes(0);
            }

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

        // Validate WFH approval (conflict checks)
        validationService.validateWfhApproval(employeeId, wfhDates);

        List<DailyTimeSheet> timeSheets = new ArrayList<>();

        // Process each WFH date
        for (WfhDate wfhDate : wfhDates) {
            if (wfhDate.getDate() == null) {
                throw new IllegalArgumentException(Constants.ERROR_NULL_DATE);
            }

            // Get or create timesheet for the date (pass userId only to avoid circular
            // reference)
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
                                    (timeSheet.getAfternoonStatus() == AttendanceStatus.PRESENT
                                            ? Constants.HALF_DAY_WORK_CREDIT
                                            : 0.0));
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
                            (timeSheet.getMorningStatus() == AttendanceStatus.PRESENT ? Constants.HALF_DAY_WORK_CREDIT
                                    : 0.0) +
                                    Constants.HALF_DAY_WORK_CREDIT);
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

        // At least one time must be provided
        if (newCheckInTime == null && newCheckOutTime == null) {
            throw new IllegalArgumentException("At least one of check-in or check-out time must be provided");
        }

        // Validate employee exists
        validateAndGetEmployee(employeeId);

        // Get existing timesheet (must exist)
        DailyTimeSheet timeSheet = dailyTimeSheetDao.getTimesheetByEmployeeAndDate(
                employeeId,
                targetDate);

        if (timeSheet == null) {
            throw new IllegalArgumentException(
                    String.format(Constants.ERROR_TIMESHEET_NOT_FOUND, employeeId, targetDate));
        }

        // Validate timesheet is not finalized
        if (Boolean.TRUE.equals(timeSheet.getIsFinalized())) {
            throw new IllegalArgumentException(
                    String.format(Constants.ERROR_TIMESHEET_FINALIZED, targetDate));
        }

        // Update check-in time if provided
        if (newCheckInTime != null) {
            timeSheet.setCheckInTime(newCheckInTime);

            // Update morning status if check-in is in the morning
            if (isMorningTime(newCheckInTime)) {
                timeSheet.setMorningStatus(AttendanceStatus.PRESENT);
            }

            // Calculate and set late minutes
            int lateMinutes = calculateLateMinutes(newCheckInTime);
            timeSheet.setLateMinutes(lateMinutes);
        }

        // Update check-out time if provided
        if (newCheckOutTime != null) {
            // Validate check-out is after check-in (if both exist)
            LocalDateTime effectiveCheckIn = newCheckInTime != null ? newCheckInTime : timeSheet.getCheckInTime();
            if (effectiveCheckIn != null && newCheckOutTime.isBefore(effectiveCheckIn)) {
                throw new IllegalArgumentException(Constants.ERROR_INVALID_TIME_RANGE);
            }

            timeSheet.setCheckOutTime(newCheckOutTime);

            // Update afternoon status if check-out is in the afternoon
            if (isAfternoonTime(newCheckOutTime)) {
                timeSheet.setAfternoonStatus(AttendanceStatus.PRESENT);
            }

            // Calculate and set early leave minutes
            int earlyLeaveMinutes = calculateEarlyLeaveMinutes(newCheckOutTime);
            timeSheet.setEarlyLeaveMinutes(earlyLeaveMinutes);

            // Calculate and set overtime minutes
            int overtimeMinutes = calculateOvertimeMinutes(newCheckOutTime);
            timeSheet.setOvertimeMinutes(overtimeMinutes);
        }

        // Recalculate total work credit if both check-in and check-out exist
        LocalDateTime effectiveCheckIn = timeSheet.getCheckInTime();
        LocalDateTime effectiveCheckOut = timeSheet.getCheckOutTime();
        if (effectiveCheckIn != null && effectiveCheckOut != null) {
            double workCredit = calculateWorkCredit(effectiveCheckIn, effectiveCheckOut);
            timeSheet.setTotalWorkCredit(workCredit);
        }

        // Reset WFH flags as this is a timesheet update (not WFH)
        timeSheet.setMorningWfh(false);
        timeSheet.setAfternoonWfh(false);

        // Save and return
        return dailyTimeSheetDao.updateDailyTimeSheet(timeSheet);
    }

    @Override
    public DailyTimeSheet handleTimesheetUpdateApproval(
            String employeeId,
            LocalDate targetDate,
            LocalDateTime newCheckInTime,
            LocalDateTime newCheckOutTime,
            AttendanceStatus newMorningStatus,
            AttendanceStatus newAfternoonStatus,
            Boolean newMorningWfh,
            Boolean newAfternoonWfh) {

        // Validate inputs
        if (targetDate == null) {
            throw new IllegalArgumentException(Constants.ERROR_NULL_DATE);
        }

        // Validate employee exists
        validateAndGetEmployee(employeeId);

        // Validate using validation service
        validationService.validateTimesheetUpdateApproval(
                employeeId, targetDate, newCheckInTime, newCheckOutTime,
                newMorningStatus, newAfternoonStatus, newMorningWfh, newAfternoonWfh);

        // Get existing timesheet
        DailyTimeSheet timeSheet = dailyTimeSheetDao.getTimesheetByEmployeeAndDate(
                employeeId, targetDate);

        // Update morning status if provided
        if (newMorningStatus != null) {
            timeSheet.setMorningStatus(newMorningStatus);
        }

        // Update afternoon status if provided
        if (newAfternoonStatus != null) {
            timeSheet.setAfternoonStatus(newAfternoonStatus);
        }

        // Update WFH flags if provided
        if (newMorningWfh != null) {
            timeSheet.setMorningWfh(newMorningWfh);
        }
        if (newAfternoonWfh != null) {
            timeSheet.setAfternoonWfh(newAfternoonWfh);
        }

        // Update check-in time if provided
        if (newCheckInTime != null) {
            timeSheet.setCheckInTime(newCheckInTime);
            int lateMinutes = calculateLateMinutes(newCheckInTime);
            timeSheet.setLateMinutes(lateMinutes);
        }

        // Update check-out time if provided
        if (newCheckOutTime != null) {
            timeSheet.setCheckOutTime(newCheckOutTime);
            int earlyLeaveMinutes = calculateEarlyLeaveMinutes(newCheckOutTime);
            timeSheet.setEarlyLeaveMinutes(earlyLeaveMinutes);
            int overtimeMinutes = calculateOvertimeMinutes(newCheckOutTime);
            timeSheet.setOvertimeMinutes(overtimeMinutes);
        }

        // Recalculate total work credit based on final state
        double workCredit = calculateWorkCreditFromStatus(timeSheet);
        timeSheet.setTotalWorkCredit(workCredit);

        // Save and return
        return dailyTimeSheetDao.updateDailyTimeSheet(timeSheet);
    }

    /**
     * Calculate work credit based on attendance status and WFH flags.
     */
    private double calculateWorkCreditFromStatus(DailyTimeSheet timeSheet) {
        AttendanceStatus morning = timeSheet.getMorningStatus();
        AttendanceStatus afternoon = timeSheet.getAfternoonStatus();

        double credit = 0.0;

        // Morning credit
        if (morning == AttendanceStatus.PRESENT) {
            credit += Constants.HALF_DAY_WORK_CREDIT;
        }

        // Afternoon credit
        if (afternoon == AttendanceStatus.PRESENT) {
            credit += Constants.HALF_DAY_WORK_CREDIT;
        }

        // If both check-in/out exist and both are PRESENT, use time-based calculation
        if (timeSheet.getCheckInTime() != null && timeSheet.getCheckOutTime() != null
                && morning == AttendanceStatus.PRESENT && afternoon == AttendanceStatus.PRESENT
                && !Boolean.TRUE.equals(timeSheet.getMorningWfh())
                && !Boolean.TRUE.equals(timeSheet.getAfternoonWfh())) {
            credit = calculateWorkCredit(timeSheet.getCheckInTime(), timeSheet.getCheckOutTime());
        }

        return credit;
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
                    String.format(Constants.ERROR_EMPLOYEE_NOT_FOUND, employeeId));
        }

        return employee;
    }

    /**
     * Gets existing timesheet or creates a new one for the specified employee and
     * date.
     * 
     * @param employeeId Employee user ID
     * @param date       Date for the timesheet
     * @return Existing or newly created DailyTimeSheet
     */
    private DailyTimeSheet getOrCreateTimeSheet(String employeeId, LocalDate date) {
        DailyTimeSheet timeSheet = dailyTimeSheetDao.getTimesheetByEmployeeAndDate(
                employeeId,
                date);

        if (timeSheet == null) {
            // Create a new timesheet with employee reference (let JPA manage the
            // relationship)
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
                    checkInLocalTime).toMinutes();
            return (int) minutesLate;
        }

        return 0;
    }

    /**
     * Calculates early leave minutes if check-out is before standard time (5:00
     * PM).
     * 
     * @param checkOutTime The check-out time
     * @return Early leave minutes (0 if on time or late)
     */
    private int calculateEarlyLeaveMinutes(LocalDateTime checkOutTime) {
        LocalTime checkOutLocalTime = checkOutTime.toLocalTime();

        if (checkOutLocalTime.isBefore(Constants.STANDARD_CHECK_OUT_TIME)) {
            long minutesEarly = Duration.between(
                    checkOutLocalTime,
                    Constants.STANDARD_CHECK_OUT_TIME).toMinutes();
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
                    checkOutLocalTime).toMinutes();
            return (int) minutesOvertime;
        }

        return 0;
    }

    /**
     * Calculates work credit based on actual working hours.
     * Work credit is proportional to standard 9-hour working day (8:00 AM - 5:00
     * PM).
     * Formula: (actual working minutes) / (standard working minutes = 540)
     * 
     * Examples:
     * - 9 hours (540 min) = 1.0 credit
     * - 8 hours (480 min) = 0.889 credit ≈ 0.89
     * - 4.5 hours (270 min) = 0.5 credit
     * 
     * @param checkInTime  Check-in time
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
