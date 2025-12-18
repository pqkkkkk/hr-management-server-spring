package org.pqkkkkk.hr_management_server.modules.timesheet.domain.dao;

import org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity.DailyTimeSheet;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.filter.FilterCriteria.TimeSheetFilter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DailyTimeSheetDao {
    
    // Command operations
    DailyTimeSheet createDailyTimeSheet(DailyTimeSheet dailyTimeSheet);
    DailyTimeSheet updateDailyTimeSheet(DailyTimeSheet dailyTimeSheet);
    void deleteDailyTimeSheet(String dailyTsId);
    
    // Query operations
    DailyTimeSheet getDailyTimeSheetById(String dailyTsId);
    DailyTimeSheet getTimesheetByEmployeeAndDate(String employeeId, LocalDate date);
    List<DailyTimeSheet> getTimeSheets(TimeSheetFilter filter);
    
    // Aggregate operations
    Double sumWorkCreditsByEmployeeAndDateRange(String employeeId, LocalDate startDate, LocalDate endDate);
    public Map<String, Object> getAttendanceStatistics(
            String employeeId,
            LocalDate startDate,
            LocalDate endDate);
    
    // Utility operations
    boolean existsByEmployeeAndDate(String employeeId, LocalDate date);
    Long countByEmployeeAndDateRange(String employeeId, LocalDate startDate, LocalDate endDate);
}
