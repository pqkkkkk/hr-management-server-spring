package org.pqkkkkk.hr_management_server.modules.timesheet.controller.http;

import org.pqkkkkk.hr_management_server.modules.timesheet.controller.http.dto.DTO.*;
import org.pqkkkkk.hr_management_server.modules.timesheet.controller.http.dto.Response.ApiResponse;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity.DailyTimeSheet;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.service.TimeSheetQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Timesheet operations
 * Handles all HTTP requests related to employee timesheets
 */
@RestController
@RequestMapping("/api/v1/timesheets")
public class TimeSheetApi {

    private final TimeSheetQueryService timeSheetQueryService;

    public TimeSheetApi(TimeSheetQueryService timeSheetQueryService) {
        this.timeSheetQueryService = timeSheetQueryService;
    }

    /**
     * Get monthly timesheet for a specific employee
     * 
     * @param employeeId Employee ID (path variable)
     * @param yearMonth Year and month in format "yyyy-MM" (e.g., "2024-12")
     * @return MonthlyTimeSheetDTO with timesheets and statistics
     * 
     * Example: GET /api/v1/timesheets/employee/NV001/monthly?yearMonth=2024-12
     */
    @GetMapping("/employee/{employeeId}/monthly")
    public ResponseEntity<ApiResponse<MonthlyTimeSheetDTO>> getMonthlyTimeSheet(
            @PathVariable String employeeId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth
    ) {
        // Validate input
        if (employeeId == null || employeeId.isBlank()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.badRequest("Employee ID is required"));
        }
        if (yearMonth == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.badRequest("Year-month is required"));
        }

        // Get timesheets
        List<DailyTimeSheet> timesheets = timeSheetQueryService
            .getMonthlyTimeSheetsOfEmployee(employeeId, yearMonth);

        // Get statistics
        Map<String, Object> statistics = timeSheetQueryService
            .calculateMonthlyAttendanceStatistics(employeeId, yearMonth);

        // Get employee name from first timesheet (if available)
        String employeeName = timesheets.isEmpty() ? null : 
            timesheets.get(0).getEmployee().getFullName();

        // Create response DTO
        MonthlyTimeSheetDTO responseDTO = MonthlyTimeSheetDTO.create(
            employeeId,
            employeeName,
            yearMonth.toString(),
            timesheets,
            statistics
        );

        return ResponseEntity.ok(ApiResponse.success(responseDTO));
    }

    /**
     * Get timesheet for a specific employee within a date range
     * 
     * @param employeeId Employee ID (path variable)
     * @param startDate Start date (inclusive) in format "yyyy-MM-dd"
     * @param endDate End date (inclusive) in format "yyyy-MM-dd"
     * @return DateRangeTimeSheetDTO with timesheets and statistics
     * 
     * Example: GET /api/v1/timesheets/employee/NV001?startDate=2024-12-01&endDate=2024-12-15
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<DateRangeTimeSheetDTO>> getTimeSheetByDateRange(
            @PathVariable String employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // Validate input
        if (employeeId == null || employeeId.isBlank()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.badRequest("Employee ID is required"));
        }
        if (startDate == null || endDate == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.badRequest("Start date and end date are required"));
        }
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.badRequest("Start date must be before or equal to end date"));
        }

        // Get timesheets
        List<DailyTimeSheet> timesheets = timeSheetQueryService
            .getTimeSheetsOfEmployeeByDateRange(employeeId, startDate, endDate);

        // Get statistics
        Map<String, Object> statistics = timeSheetQueryService
            .calculateMonthlyAttendanceStatistics(
                employeeId, 
                YearMonth.from(startDate)
            );

        // Get employee name from first timesheet (if available)
        String employeeName = timesheets.isEmpty() ? null : 
            timesheets.get(0).getEmployee().getFullName();

        // Create response DTO
        DateRangeTimeSheetDTO responseDTO = DateRangeTimeSheetDTO.create(
            employeeId,
            employeeName,
            startDate,
            endDate,
            timesheets,
            statistics
        );

        return ResponseEntity.ok(ApiResponse.success(responseDTO));
    }

    /**
     * Get attendance statistics for a specific employee in a month
     * 
     * @param employeeId Employee ID (path variable)
     * @param yearMonth Year and month in format "yyyy-MM" (e.g., "2024-12")
     * @return TimeSheetStatisticsDTO with aggregated statistics
     * 
     * Example: GET /api/v1/timesheets/employee/NV001/statistics?yearMonth=2024-12
     */
    @GetMapping("/employee/{employeeId}/statistics")
    public ResponseEntity<ApiResponse<TimeSheetStatisticsDTO>> getAttendanceStatistics(
            @PathVariable String employeeId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth
    ) {
        // Validate input
        if (employeeId == null || employeeId.isBlank()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.badRequest("Employee ID is required"));
        }
        if (yearMonth == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.badRequest("Year-month is required"));
        }

        // Get statistics
        Map<String, Object> statistics = timeSheetQueryService
            .calculateMonthlyAttendanceStatistics(employeeId, yearMonth);

        // Create response DTO
        TimeSheetStatisticsDTO statisticsDTO = TimeSheetStatisticsDTO.fromMap(statistics);

        return ResponseEntity.ok(ApiResponse.success(statisticsDTO));
    }

    /**
     * Get a single daily timesheet by ID
     * 
     * @param dailyTsId Daily timesheet ID
     * @return DailyTimeSheetDTO
     * 
     * Example: GET /api/v1/timesheets/daily/uuid-here
     */
    @GetMapping("/daily/{dailyTsId}")
    public ResponseEntity<ApiResponse<DailyTimeSheetDTO>> getDailyTimeSheet(
            @PathVariable String dailyTsId
    ) {
        // Validate input
        if (dailyTsId == null || dailyTsId.isBlank()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.badRequest("Daily timesheet ID is required"));
        }

        // Get timesheet
        DailyTimeSheet timesheet = timeSheetQueryService.getDailyTimeSheetById(dailyTsId);

        // Convert to DTO
        DailyTimeSheetDTO dto = DailyTimeSheetDTO.fromEntity(timesheet);

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    /**
     * Get timesheet for a specific employee on a specific date
     * 
     * @param employeeId Employee ID (path variable)
     * @param date Date in format "yyyy-MM-dd"
     * @return DailyTimeSheetDTO or null if not found
     * 
     * Example: GET /api/v1/timesheets/employee/NV001/date/2024-12-15
     */
    @GetMapping("/employee/{employeeId}/date/{date}")
    public ResponseEntity<ApiResponse<DailyTimeSheetDTO>> getTimeSheetByDate(
            @PathVariable String employeeId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        // Validate input
        if (employeeId == null || employeeId.isBlank()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.badRequest("Employee ID is required"));
        }
        if (date == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.badRequest("Date is required"));
        }

        // Get timesheet
        DailyTimeSheet timesheet = timeSheetQueryService
            .getTimesheetByEmployeeAndDate(employeeId, date);

        if (timesheet == null) {
            return ResponseEntity.ok(
                ApiResponse.success(null, "No timesheet found for the specified date")
            );
        }

        // Convert to DTO
        DailyTimeSheetDTO dto = DailyTimeSheetDTO.fromEntity(timesheet);

        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
