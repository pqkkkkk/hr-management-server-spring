package org.pqkkkkk.hr_management_server.modules.timesheet.domain.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity.DailyTimeSheet;

/**
 * Query Service for Timesheet operations
 * Note: No pagination is used because:
 * - Timesheets are always queried by date ranges (month/week)
 * - Number of records per query is naturally limited (max 31 days/month)
 * - Need full dataset for calculations (total work credits, attendance
 * statistics)
 */
public interface TimeSheetQueryService {

    /**
     * Get daily timesheet by ID
     * 
     * @param dailyTsId Daily timesheet ID
     * @return DailyTimeSheet or null if not found
     */
    DailyTimeSheet getDailyTimeSheetById(String dailyTsId);

    /**
     * Get all timesheets of an employee for a specific month
     * This is the most common query for employees to view their own timesheet
     * 
     * @param employeeId Employee ID
     * @param yearMonth  Year and month (e.g., 2024-12)
     * @return List of DailyTimeSheet for the specified month
     */
    List<DailyTimeSheet> getMonthlyTimeSheetsOfEmployee(String employeeId, YearMonth yearMonth);

    /**
     * Get all timesheets of an employee within a date range
     * Used for custom date range queries (e.g., last week, last quarter)
     * 
     * @param employeeId Employee ID
     * @param startDate  Start date (inclusive)
     * @param endDate    End date (inclusive)
     * @return List of DailyTimeSheet within the date range
     */
    List<DailyTimeSheet> getTimeSheetsOfEmployeeByDateRange(String employeeId, LocalDate startDate, LocalDate endDate);

    /**
     * Get all timesheets for multiple employees in a specific month
     * Used by HR/Manager to view timesheets of their team
     * 
     * @param employeeIds List of employee IDs
     * @param yearMonth   Year and month
     * @return Map of employeeId to List of DailyTimeSheet
     */
    Map<String, List<DailyTimeSheet>> getMonthlyTimeSheetsOfEmployees(List<String> employeeIds, YearMonth yearMonth);

    /**
     * Get timesheets of all employees in a department for a specific month
     * Used by HR/Manager to view department-wide timesheets
     * 
     * @param departmentId Department ID
     * @param yearMonth    Year and month
     * @return Map of employeeId to List of DailyTimeSheet
     */
    Map<String, List<DailyTimeSheet>> getMonthlyTimeSheetsOfDepartment(String departmentId, YearMonth yearMonth);

    /**
     * Get timesheet for a specific employee on a specific date
     * Used for checking if timesheet exists for today before creating new one
     * 
     * @param employeeId Employee ID
     * @param date       Date
     * @return DailyTimeSheet or null if not found
     */
    DailyTimeSheet getTimesheetByEmployeeAndDate(String employeeId, LocalDate date);

    /**
     * Calculate total work credits for an employee in a specific month
     * Used for payroll calculation
     * 
     * @param employeeId Employee ID
     * @param yearMonth  Year and month
     * @return Total work credits
     */
    Double calculateMonthlyWorkCredits(String employeeId, YearMonth yearMonth);

    /**
     * Calculate attendance statistics for an employee in a specific month
     * Returns statistics like: total days worked, total late days, total overtime
     * minutes, etc.
     * 
     * @param employeeId Employee ID
     * @param yearMonth  Year and month
     * @return Map of statistic name to value
     */
    Map<String, Object> calculateMonthlyAttendanceStatistics(String employeeId, YearMonth yearMonth);

    /**
     * Get batch attendance statistics for multiple employees within a date range.
     * Used by internal APIs for reward policy calculations.
     * 
     * @param userIds   List of employee IDs (max 100)
     * @param startDate Start date (inclusive)
     * @param endDate   End date (inclusive)
     * @return List of statistics maps, each containing userId and attendance
     *         metrics
     */
    List<Map<String, Object>> getBatchAttendanceStatistics(
            List<String> userIds, LocalDate startDate, LocalDate endDate);
}
