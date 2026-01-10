package org.pqkkkkk.hr_management_server.modules.timesheet.infrastructure.dao;

import org.pqkkkkk.hr_management_server.modules.timesheet.domain.dao.DailyTimeSheetDao;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity.DailyTimeSheet;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.filter.FilterCriteria.TimeSheetFilter;
import org.pqkkkkk.hr_management_server.modules.timesheet.infrastructure.dao.jpa_repository.DailyTimeSheetRepository;
import org.pqkkkkk.hr_management_server.modules.timesheet.infrastructure.dao.jpa_specification.DailyTimeSheetSpecification;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DailyTimeSheetJpaDao implements DailyTimeSheetDao {

    private final DailyTimeSheetRepository repository;

    public DailyTimeSheetJpaDao(DailyTimeSheetRepository repository) {
        this.repository = repository;
    }

    @Override
    public DailyTimeSheet createDailyTimeSheet(DailyTimeSheet dailyTimeSheet) {
        return repository.save(dailyTimeSheet);
    }

    @Override
    public DailyTimeSheet updateDailyTimeSheet(DailyTimeSheet dailyTimeSheet) {
        return repository.save(dailyTimeSheet);
    }

    @Override
    public void deleteDailyTimeSheet(String dailyTsId) {
        repository.deleteById(dailyTsId);
    }

    @Override
    public DailyTimeSheet getDailyTimeSheetById(String dailyTsId) {
        return repository.findById(dailyTsId).orElse(null);
    }

    @Override
    public DailyTimeSheet getTimesheetByEmployeeAndDate(String employeeId, LocalDate date) {
        return repository.findByEmployeeUserIdAndDate(employeeId, date).orElse(null);
    }

    @Override
    public Double sumWorkCreditsByEmployeeAndDateRange(
            String employeeId,
            LocalDate startDate,
            LocalDate endDate) {
        return repository.sumWorkCreditsByEmployeeAndDateRange(employeeId, startDate, endDate);
    }

    @Override
    public Map<String, Object> getAttendanceStatistics(
            String employeeId,
            LocalDate startDate,
            LocalDate endDate) {
        Object[] result = (Object[]) repository.getAttendanceStatistics(employeeId, startDate, endDate)[0];

        Map<String, Object> statistics = new HashMap<>();
        if (result != null && result.length >= 7) {
            statistics.put("totalDays", result[0]);
            statistics.put("morningPresent", result[1]);
            statistics.put("afternoonPresent", result[2]);
            statistics.put("lateDays", result[3]);
            statistics.put("totalLateMinutes", result[4]);
            statistics.put("totalOvertimeMinutes", result[5]);
            statistics.put("totalWorkCredit", result[6]);
        }

        return statistics;
    }

    @Override
    public boolean existsByEmployeeAndDate(String employeeId, LocalDate date) {
        return repository.existsByEmployeeUserIdAndDate(employeeId, date);
    }

    @Override
    public Long countByEmployeeAndDateRange(String employeeId, LocalDate startDate, LocalDate endDate) {
        return repository.countByEmployeeUserIdAndDateBetween(employeeId, startDate, endDate);
    }

    @Override
    public List<DailyTimeSheet> getTimeSheets(TimeSheetFilter filter) {
        Specification<DailyTimeSheet> spec = DailyTimeSheetSpecification.buildSpecification(filter);

        Sort sort = Sort.by(
                Sort.Direction.fromString(filter.sortDirection()),
                filter.sortBy());

        return repository.findAll(spec, sort);
    }

    @Override
    public List<Map<String, Object>> getBatchAttendanceStatistics(
            List<String> userIds, LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = repository.getBatchAttendanceStatistics(userIds, startDate, endDate);

        return results.stream().map(row -> {
            Map<String, Object> stats = new HashMap<>();
            stats.put("userId", row[0]);
            stats.put("totalDays", row[1]);
            stats.put("morningPresent", row[2]);
            stats.put("afternoonPresent", row[3]);
            stats.put("lateDays", row[4]);
            stats.put("totalLateMinutes", row[5]);
            stats.put("totalOvertimeMinutes", row[6]);
            stats.put("totalWorkCredit", row[7]);
            return stats;
        }).toList();
    }
}
