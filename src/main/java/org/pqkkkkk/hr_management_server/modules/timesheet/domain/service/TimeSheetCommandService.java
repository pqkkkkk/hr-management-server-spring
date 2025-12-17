package org.pqkkkkk.hr_management_server.modules.timesheet.domain.service;

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.LeaveDate;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.WfhDate;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity.DailyTimeSheet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for TimeSheet command operations.
 * These methods are primarily used by the Request module when approving various types of requests.
 * Each method handles the specific timesheet updates required for different request types.
 */
public interface TimeSheetCommandService {
    
    /**
     * Handles check-in request approval by creating or updating daily timesheet.
     * <p>
     * Operations performed:
     * - Creates or retrieves existing timesheet for the check-in date
     * - Sets checkInTime to the approved check-in time
     * - Updates morningStatus to PRESENT (if check-in is before noon)
     * - Calculates and sets lateMinutes if check-in is after 8:00 AM
     * </p>
     * 
     * @param employeeId ID of the employee checking in
     * @param checkInTime The approved check-in time from the request
     * @return Created or updated DailyTimeSheet with check-in information
     * @throws IllegalArgumentException if employeeId is null or employee not found
     * @throws IllegalArgumentException if checkInTime is null
     */
    DailyTimeSheet handleCheckInApproval(String employeeId, LocalDateTime checkInTime);
    
    /**
     * Handles check-out request approval by updating the existing daily timesheet.
     * <p>
     * Operations performed:
     * - Retrieves existing timesheet (must exist with check-in)
     * - Sets checkOutTime to the approved check-out time
     * - Updates afternoonStatus to PRESENT (if check-out is after noon)
     * - Calculates earlyLeaveMinutes if check-out is before 5:00 PM
     * - Calculates overtimeMinutes if check-out is after 5:00 PM
     * - Calculates totalWorkCredit based on actual working hours (proportional to 9 hours standard)
     * </p>
     * 
     * @param employeeId ID of the employee checking out
     * @param checkOutTime The approved check-out time from the request
     * @return Updated DailyTimeSheet with check-out information and calculated metrics
     * @throws IllegalArgumentException if employeeId is null or employee not found
     * @throws IllegalArgumentException if checkOutTime is null
     * @throws IllegalArgumentException if timesheet doesn't exist or checkInTime is not set
     * @throws IllegalArgumentException if checkOutTime is before checkInTime
     */
    DailyTimeSheet handleCheckOutApproval(String employeeId, LocalDateTime checkOutTime);
    
    /**
     * Handles leave request approval by creating or updating timesheets for multiple dates.
     * <p>
     * Operations performed for each leave date:
     * - Creates or retrieves timesheet for the date
     * - Based on ShiftType:
     *   - FULL_DAY: Sets both morningStatus and afternoonStatus to LEAVE
     *   - MORNING: Sets only morningStatus to LEAVE
     *   - AFTERNOON: Sets only afternoonStatus to LEAVE
     * - Sets totalWorkCredit to 0.0 (leave is unpaid)
     * - Clears check-in/out times (not applicable for leave)
     * </p>
     * 
     * @param employeeId ID of the employee taking leave
     * @param leaveDates List of leave dates with shift information from the request
     * @return List of created or updated DailyTimeSheets for all leave dates
     * @throws IllegalArgumentException if employeeId is null or employee not found
     * @throws IllegalArgumentException if leaveDates is null or empty
     */
    List<DailyTimeSheet> handleLeaveApproval(String employeeId, List<LeaveDate> leaveDates);
    
    /**
     * Handles WFH (Work From Home) request approval by creating or updating timesheets for multiple dates.
     * <p>
     * Operations performed for each WFH date:
     * - Creates or retrieves timesheet for the date
     * - Based on ShiftType:
     *   - FULL_DAY: Sets morningWfh=true, afternoonWfh=true, both statuses to PRESENT, workCredit=1.0
     *   - MORNING: Sets morningWfh=true, morningStatus to PRESENT, workCredit=0.5
     *   - AFTERNOON: Sets afternoonWfh=true, afternoonStatus to PRESENT, workCredit=0.5
     * - WFH days count as normal working days for work credit calculation
     * </p>
     * 
     * @param employeeId ID of the employee working from home
     * @param wfhDates List of WFH dates with shift information from the request
     * @return List of created or updated DailyTimeSheets for all WFH dates
     * @throws IllegalArgumentException if employeeId is null or employee not found
     * @throws IllegalArgumentException if wfhDates is null or empty
     */
    List<DailyTimeSheet> handleWfhApproval(String employeeId, List<WfhDate> wfhDates);
    
    /**
     * Handles timesheet update request approval by modifying an existing timesheet.
     * <p>
     * Operations performed:
     * - Retrieves existing timesheet for the target date
     * - Validates timesheet exists and is not finalized
     * - Updates both checkInTime and checkOutTime with new values
     * - Recalculates all time-related metrics:
     *   - lateMinutes (if new check-in is after 8:00 AM)
     *   - earlyLeaveMinutes (if new check-out is before 5:00 PM)
     *   - overtimeMinutes (if new check-out is after 5:00 PM)
     *   - totalWorkCredit (based on new working hours, proportional to 9 hours standard)
     * - Updates morning/afternoon status based on new times
     * </p>
     * 
     * @param employeeId ID of the employee whose timesheet is being updated
     * @param targetDate The date of the timesheet to update
     * @param newCheckInTime The new check-in time from the request
     * @param newCheckOutTime The new check-out time from the request
     * @return Updated DailyTimeSheet with recalculated metrics
     * @throws IllegalArgumentException if employeeId is null or employee not found
     * @throws IllegalArgumentException if targetDate is null
     * @throws IllegalArgumentException if newCheckInTime or newCheckOutTime is null
     * @throws IllegalArgumentException if timesheet doesn't exist for the target date
     * @throws IllegalArgumentException if timesheet is already finalized
     * @throws IllegalArgumentException if newCheckOutTime is before newCheckInTime
     */
    DailyTimeSheet handleTimesheetUpdateApproval(
        String employeeId,
        LocalDate targetDate,
        LocalDateTime newCheckInTime,
        LocalDateTime newCheckOutTime
    );
}
