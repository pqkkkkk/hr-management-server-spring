package org.pqkkkkk.hr_management_server.modules.timesheet.controller.http;

import lombok.RequiredArgsConstructor;

import org.pqkkkkk.hr_management_server.modules.timesheet.controller.http.dto.Response.ApiResponse;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.service.TimeSheetQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Internal API for timesheet statistics.
 * Used by .NET application for reward policy calculations.
 */
@RestController
@RequestMapping("/internal/api/v1/timesheets")
@RequiredArgsConstructor
public class InternalTimesheetApi {

    private static final int MAX_USER_IDS = 100;
    private final TimeSheetQueryService timeSheetQueryService;

    /**
     * Response DTO for batch statistics
     */
    public record TimesheetStatisticsResponse(
            String userId,
            long totalDays,
            long morningPresent,
            long afternoonPresent,
            long lateDays,
            long totalLateMinutes,
            long totalOvertimeMinutes,
            double totalWorkCredit) {
        public static TimesheetStatisticsResponse fromMap(Map<String, Object> stats) {
            return new TimesheetStatisticsResponse(
                    (String) stats.get("userId"),
                    ((Number) stats.get("totalDays")).longValue(),
                    ((Number) stats.get("morningPresent")).longValue(),
                    ((Number) stats.get("afternoonPresent")).longValue(),
                    ((Number) stats.get("lateDays")).longValue(),
                    ((Number) stats.get("totalLateMinutes")).longValue(),
                    ((Number) stats.get("totalOvertimeMinutes")).longValue(),
                    ((Number) stats.get("totalWorkCredit")).doubleValue());
        }
    }

    /**
     * Get batch attendance statistics for multiple employees.
     * 
     * @param userIds   List of employee IDs (max 100)
     * @param startDate Start date (YYYY-MM-DD)
     * @param endDate   End date (YYYY-MM-DD)
     * @return List of statistics for each user
     */
    @GetMapping("/statistics/batch")
    public ResponseEntity<ApiResponse<List<TimesheetStatisticsResponse>>> getBatchStatistics(
            @RequestParam List<String> userIds,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        if (userIds.size() > MAX_USER_IDS) {
            throw new IllegalArgumentException("Maximum " + MAX_USER_IDS + " userIds allowed per request");
        }

        List<Map<String, Object>> rawStats = timeSheetQueryService.getBatchAttendanceStatistics(
                userIds, startDate, endDate);

        List<TimesheetStatisticsResponse> response = rawStats.stream()
                .map(TimesheetStatisticsResponse::fromMap)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
