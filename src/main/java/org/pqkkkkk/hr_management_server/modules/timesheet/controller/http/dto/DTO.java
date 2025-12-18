package org.pqkkkkk.hr_management_server.modules.timesheet.controller.http.dto;

import org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity.DailyTimeSheet;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity.Enums.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Data Transfer Objects for Timesheet module
 * Each DTO includes methods to convert to and from entity objects
 */
public class DTO {
    
    /**
     * DTO for a single daily timesheet record
     */
    public record DailyTimeSheetDTO(
        String dailyTsId,
        LocalDate date,
        AttendanceStatus morningStatus,
        AttendanceStatus afternoonStatus,
        Boolean morningWfh,
        Boolean afternoonWfh,
        Double totalWorkCredit,
        LocalDateTime checkInTime,
        LocalDateTime checkOutTime,
        Integer lateMinutes,
        Integer earlyLeaveMinutes,
        Integer overtimeMinutes,
        Boolean isFinalized,
        String employeeId,
        String employeeName
    ) {
        /**
         * Convert DTO to Entity
         */
        public DailyTimeSheet toEntity() {
            return DailyTimeSheet.builder()
                .dailyTsId(dailyTsId)
                .date(date)
                .morningStatus(morningStatus)
                .afternoonStatus(afternoonStatus)
                .morningWfh(morningWfh)
                .afternoonWfh(afternoonWfh)
                .totalWorkCredit(totalWorkCredit)
                .checkInTime(checkInTime)
                .checkOutTime(checkOutTime)
                .lateMinutes(lateMinutes)
                .earlyLeaveMinutes(earlyLeaveMinutes)
                .overtimeMinutes(overtimeMinutes)
                .isFinalized(isFinalized)
                .build();
        }

        /**
         * Convert Entity to DTO
         */
        public static DailyTimeSheetDTO fromEntity(DailyTimeSheet entity) {
            return new DailyTimeSheetDTO(
                entity.getDailyTsId(),
                entity.getDate(),
                entity.getMorningStatus(),
                entity.getAfternoonStatus(),
                entity.getMorningWfh(),
                entity.getAfternoonWfh(),
                entity.getTotalWorkCredit(),
                entity.getCheckInTime(),
                entity.getCheckOutTime(),
                entity.getLateMinutes(),
                entity.getEarlyLeaveMinutes(),
                entity.getOvertimeMinutes(),
                entity.getIsFinalized(),
                entity.getEmployee() != null ? entity.getEmployee().getUserId() : null,
                entity.getEmployee() != null ? entity.getEmployee().getFullName() : null
            );
        }

        /**
         * Convert list of entities to list of DTOs
         */
        public static List<DailyTimeSheetDTO> fromEntities(List<DailyTimeSheet> entities) {
            return entities.stream()
                .map(DailyTimeSheetDTO::fromEntity)
                .collect(Collectors.toList());
        }
    }

    /**
     * DTO for monthly timesheet summary with statistics
     */
    public record MonthlyTimeSheetDTO(
        String employeeId,
        String employeeName,
        String yearMonth,
        List<DailyTimeSheetDTO> timesheets,
        TimeSheetStatisticsDTO summary
    ) {
        /**
         * Create DTO from entity list and statistics
         */
        public static MonthlyTimeSheetDTO create(
            String employeeId,
            String employeeName,
            String yearMonth,
            List<DailyTimeSheet> timesheets,
            Map<String, Object> statistics
        ) {
            List<DailyTimeSheetDTO> timeSheetDTOs = DailyTimeSheetDTO.fromEntities(timesheets);
            TimeSheetStatisticsDTO summaryDTO = TimeSheetStatisticsDTO.fromMap(statistics);
            
            return new MonthlyTimeSheetDTO(
                employeeId,
                employeeName,
                yearMonth,
                timeSheetDTOs,
                summaryDTO
            );
        }
    }

    /**
     * DTO for timesheet statistics
     */
    public record TimeSheetStatisticsDTO(
        Long totalDays,
        Long morningPresentCount,
        Long afternoonPresentCount,
        Long lateDaysCount,
        Long totalLateMinutes,
        Long totalOvertimeMinutes,
        Double totalWorkCredit
    ) {
        /**
         * Create DTO from statistics map
         */
        public static TimeSheetStatisticsDTO fromMap(Map<String, Object> statistics) {
            if (statistics == null || statistics.isEmpty()) {
                return new TimeSheetStatisticsDTO(0L, 0L, 0L, 0L, 0L, 0L, 0.0);
            }

            return new TimeSheetStatisticsDTO(
                getLongValue(statistics, "totalDays"),
                getLongValue(statistics, "morningPresent"),
                getLongValue(statistics, "afternoonPresent"),
                getLongValue(statistics, "lateDays"),
                getLongValue(statistics, "totalLateMinutes"),
                getLongValue(statistics, "totalOvertimeMinutes"),
                getDoubleValue(statistics, "totalWorkCredit")
            );
        }

        private static Long getLongValue(Map<String, Object> map, String key) {
            Object value = map.get(key);
            if (value == null) return 0L;
            if (value instanceof Long) return (Long) value;
            if (value instanceof Integer) return ((Integer) value).longValue();
            if (value instanceof Number) return ((Number) value).longValue();
            return 0L;
        }

        private static Double getDoubleValue(Map<String, Object> map, String key) {
            Object value = map.get(key);
            if (value == null) return 0.0;
            if (value instanceof Double) return (Double) value;
            if (value instanceof Number) return ((Number) value).doubleValue();
            return 0.0;
        }
    }

    /**
     * DTO for date range timesheet response
     */
    public record DateRangeTimeSheetDTO(
        String employeeId,
        String employeeName,
        LocalDate startDate,
        LocalDate endDate,
        List<DailyTimeSheetDTO> timesheets,
        TimeSheetStatisticsDTO summary
    ) {
        /**
         * Create DTO from entity list and statistics
         */
        public static DateRangeTimeSheetDTO create(
            String employeeId,
            String employeeName,
            LocalDate startDate,
            LocalDate endDate,
            List<DailyTimeSheet> timesheets,
            Map<String, Object> statistics
        ) {
            List<DailyTimeSheetDTO> timeSheetDTOs = DailyTimeSheetDTO.fromEntities(timesheets);
            TimeSheetStatisticsDTO summaryDTO = TimeSheetStatisticsDTO.fromMap(statistics);
            
            return new DateRangeTimeSheetDTO(
                employeeId,
                employeeName,
                startDate,
                endDate,
                timeSheetDTOs,
                summaryDTO
            );
        }
    }
}
