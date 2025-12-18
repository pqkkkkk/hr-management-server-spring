package org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity;

import java.time.LocalTime;

/**
 * Constants for TimeSheet module business rules and calculations
 */
public class Constants {
    
    // ============= Working Hours =============
    /**
     * Standard work day start time (8:00 AM)
     */
    public static final LocalTime STANDARD_CHECK_IN_TIME = LocalTime.of(8, 0);
    
    /**
     * Standard work day end time (5:00 PM)
     */
    public static final LocalTime STANDARD_CHECK_OUT_TIME = LocalTime.of(17, 0);
    
    /**
     * Noon time for determining morning/afternoon shifts (12:00 PM)
     */
    public static final LocalTime NOON_TIME = LocalTime.of(12, 0);
    
    /**
     * Standard working hours per day (9 hours: 8:00 AM - 5:00 PM)
     */
    public static final int STANDARD_WORKING_HOURS = 9;
    
    /**
     * Standard working minutes per day (540 minutes)
     */
    public static final int STANDARD_WORKING_MINUTES = STANDARD_WORKING_HOURS * 60;
    
    // ============= Work Credit Values =============
    /**
     * Work credit for full day attendance (1.0)
     */
    public static final double FULL_DAY_WORK_CREDIT = 1.0;
    
    /**
     * Work credit for half day attendance (0.5)
     */
    public static final double HALF_DAY_WORK_CREDIT = 0.5;
    
    /**
     * Work credit for absent or leave (0.0)
     */
    public static final double ZERO_WORK_CREDIT = 0.0;
    
    // ============= Grace Periods & Thresholds =============
    /**
     * Grace period for late check-in (0 minutes - no grace period)
     */
    public static final int LATE_CHECK_IN_GRACE_MINUTES = 0;
    
    /**
     * Grace period for early check-out (0 minutes - no grace period)
     */
    public static final int EARLY_LEAVE_GRACE_MINUTES = 0;
    
    /**
     * Minimum minutes to count as overtime (0 minutes)
     */
    public static final int OVERTIME_THRESHOLD_MINUTES = 0;
    
    // ============= Calculation Constants =============
    /**
     * Minimum work hours to count as half day (4 hours)
     */
    public static final int MIN_HOURS_FOR_HALF_DAY = 4;
    
    /**
     * Minimum work hours to count as full day (8 hours)
     */
    public static final int MIN_HOURS_FOR_FULL_DAY = 8;
    
    /**
     * Minutes in one hour (for conversion)
     */
    public static final int MINUTES_PER_HOUR = 60;
    
    // ============= Validation Messages =============
    public static final String ERROR_EMPLOYEE_NOT_FOUND = "Employee not found with ID: %s";
    public static final String ERROR_TIMESHEET_NOT_FOUND = "Timesheet not found for employee %s on date %s";
    public static final String ERROR_TIMESHEET_FINALIZED = "Timesheet for date %s is already finalized and cannot be modified";
    public static final String ERROR_CHECKIN_REQUIRED = "Check-in time is required before check-out";
    public static final String ERROR_INVALID_TIME_RANGE = "Check-out time must be after check-in time";
    public static final String ERROR_FUTURE_DATE = "Cannot create timesheet for future date: %s";
    public static final String ERROR_NULL_EMPLOYEE_ID = "Employee ID cannot be null";
    public static final String ERROR_NULL_DATE = "Date cannot be null";
    public static final String ERROR_NULL_TIME = "Time cannot be null";
    
    // Private constructor to prevent instantiation
    private Constants() {
        throw new AssertionError("Cannot instantiate Constants class");
    }
}
