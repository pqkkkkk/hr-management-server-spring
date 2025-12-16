package org.pqkkkkk.hr_management_server.modules.timesheet.infrastructure.dao.jpa_repository;

import org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity.DailyTimeSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyTimeSheetRepository extends JpaRepository<DailyTimeSheet, String>,
        JpaSpecificationExecutor<DailyTimeSheet> {
    Optional<DailyTimeSheet> findByEmployeeUserIdAndDate(String userId, LocalDate date);

    /**
     * Count timesheets of an employee within date range
     * Useful for checking if employee has submitted timesheets
     */
    Long countByEmployeeUserIdAndDateBetween(String userId, LocalDate startDate, LocalDate endDate);

    /**
     * Check if timesheet exists for employee on a specific date
     */
    boolean existsByEmployeeUserIdAndDate(String userId, LocalDate date);

    @Query("SELECT COALESCE(SUM(t.totalWorkCredit), 0.0) FROM DailyTimeSheet t " +
            "WHERE t.employee.userId = :userId " +
            "AND t.date BETWEEN :startDate AND :endDate")
    Double sumWorkCreditsByEmployeeAndDateRange(
            @Param("userId") String userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    @Query(value = """
            SELECT 
                COUNT(*) as total_days,
                COUNT(CASE WHEN morning_status = 'PRESENT' THEN 1 END) as morning_present,
                COUNT(CASE WHEN afternoon_status = 'PRESENT' THEN 1 END) as afternoon_present,
                COUNT(CASE WHEN late_minutes > 0 THEN 1 END) as late_days,
                COALESCE(SUM(late_minutes), 0) as total_late_minutes,
                COALESCE(SUM(overtime_minutes), 0) as total_overtime_minutes,
                COALESCE(SUM(total_work_credit), 0) as total_work_credit
            FROM daily_timesheet
            WHERE employee_id = :userId
            AND date BETWEEN :startDate AND :endDate
            """, nativeQuery = true)
    Object[] getAttendanceStatistics(
            @Param("userId") String userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
