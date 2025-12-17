package org.pqkkkkk.hr_management_server.modules.timesheet.domain.service.impl;

import org.pqkkkkk.hr_management_server.modules.timesheet.domain.dao.DailyTimeSheetDao;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity.DailyTimeSheet;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.filter.FilterCriteria.TimeSheetFilter;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.service.TimeSheetQueryService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of TimeSheetQueryService
 * Handles all read operations for timesheets
 */
@Service
public class TimeSheetQueryServiceImpl implements TimeSheetQueryService {

    private final DailyTimeSheetDao dailyTimeSheetDao;

    public TimeSheetQueryServiceImpl(DailyTimeSheetDao dailyTimeSheetDao) {
        this.dailyTimeSheetDao = dailyTimeSheetDao;
    }

    @Override
    public DailyTimeSheet getDailyTimeSheetById(String dailyTsId) {
        if (dailyTsId == null || dailyTsId.isBlank()) {
            throw new IllegalArgumentException("Daily timesheet ID must not be null or empty");
        }
        
        DailyTimeSheet timesheet = dailyTimeSheetDao.getDailyTimeSheetById(dailyTsId);
        if (timesheet == null) {
            throw new IllegalArgumentException("Timesheet not found with ID: " + dailyTsId);
        }
        
        return timesheet;
    }

    @Override
    public List<DailyTimeSheet> getMonthlyTimeSheetsOfEmployee(String employeeId, YearMonth yearMonth) {
        if (employeeId == null || employeeId.isBlank()) {
            throw new IllegalArgumentException("Employee ID must not be null or empty");
        }
        if (yearMonth == null) {
            throw new IllegalArgumentException("Year-month must not be null");
        }

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        TimeSheetFilter filter = new TimeSheetFilter(
            employeeId,
            startDate.toString(),
            endDate.toString(),
            null, null, null, null, null,
            null, null, "date", "ASC"
        );

        return dailyTimeSheetDao.getTimeSheets(filter);
    }

    @Override
    public List<DailyTimeSheet> getTimeSheetsOfEmployeeByDateRange(
            String employeeId, 
            LocalDate startDate, 
            LocalDate endDate) {
        
        if (employeeId == null || employeeId.isBlank()) {
            throw new IllegalArgumentException("Employee ID must not be null or empty");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date must not be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        TimeSheetFilter filter = new TimeSheetFilter(
            employeeId,
            startDate.toString(),
            endDate.toString(),
            null, null, null, null, null,
            null, null, "date", "ASC"
        );

        return dailyTimeSheetDao.getTimeSheets(filter);
    }

    @Override
    public Map<String, List<DailyTimeSheet>> getMonthlyTimeSheetsOfEmployees(
            List<String> employeeIds, 
            YearMonth yearMonth) {
        
        if (employeeIds == null || employeeIds.isEmpty()) {
            throw new IllegalArgumentException("Employee IDs list must not be null or empty");
        }
        if (yearMonth == null) {
            throw new IllegalArgumentException("Year-month must not be null");
        }

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        return employeeIds.stream()
            .collect(Collectors.toMap(
                empId -> empId,
                empId -> {
                    TimeSheetFilter filter = new TimeSheetFilter(
                        empId,
                        startDate.toString(),
                        endDate.toString(),
                        null, null, null, null, null,
                        null, null, "date", "ASC"
                    );
                    return dailyTimeSheetDao.getTimeSheets(filter);
                }
            ));
    }

    @Override
    public Map<String, List<DailyTimeSheet>> getMonthlyTimeSheetsOfDepartment(
            String departmentId, 
            YearMonth yearMonth) {
        
        if (departmentId == null || departmentId.isBlank()) {
            throw new IllegalArgumentException("Department ID must not be null or empty");
        }
        if (yearMonth == null) {
            throw new IllegalArgumentException("Year-month must not be null");
        }

        // Note: This implementation assumes you have a way to get employee IDs by department
        // If you need to implement this, you'll need to inject UserDao or ProfileDao
        throw new UnsupportedOperationException(
            "Department-wide timesheet query requires UserDao dependency. " +
            "Please inject UserDao to get employees by department ID."
        );
    }

    @Override
    public DailyTimeSheet getTimesheetByEmployeeAndDate(String employeeId, LocalDate date) {
        if (employeeId == null || employeeId.isBlank()) {
            throw new IllegalArgumentException("Employee ID must not be null or empty");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date must not be null");
        }

        return dailyTimeSheetDao.getTimesheetByEmployeeAndDate(employeeId, date);
    }

    @Override
    public Double calculateMonthlyWorkCredits(String employeeId, YearMonth yearMonth) {
        if (employeeId == null || employeeId.isBlank()) {
            throw new IllegalArgumentException("Employee ID must not be null or empty");
        }
        if (yearMonth == null) {
            throw new IllegalArgumentException("Year-month must not be null");
        }

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Double totalCredits = dailyTimeSheetDao.sumWorkCreditsByEmployeeAndDateRange(
            employeeId, startDate, endDate
        );

        return totalCredits != null ? totalCredits : 0.0;
    }

    @Override
    public Map<String, Object> calculateMonthlyAttendanceStatistics(
            String employeeId, 
            YearMonth yearMonth) {
        
        if (employeeId == null || employeeId.isBlank()) {
            throw new IllegalArgumentException("Employee ID must not be null or empty");
        }
        if (yearMonth == null) {
            throw new IllegalArgumentException("Year-month must not be null");
        }

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        return dailyTimeSheetDao.getAttendanceStatistics(employeeId, startDate, endDate);
    }
}
