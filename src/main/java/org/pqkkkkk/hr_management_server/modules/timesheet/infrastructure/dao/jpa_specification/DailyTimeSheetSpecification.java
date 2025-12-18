package org.pqkkkkk.hr_management_server.modules.timesheet.infrastructure.dao.jpa_specification;

import jakarta.persistence.criteria.Predicate;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity.DailyTimeSheet;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity.Enums.AttendanceStatus;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.filter.FilterCriteria.TimeSheetFilter;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Specification builder for DailyTimeSheet queries
 * Uses JPA Criteria API to build dynamic queries based on filter parameters
 */
public class DailyTimeSheetSpecification {

    /**
     * Build Specification from TimeSheetFilter
     * Creates predicates for each non-null filter parameter
     */
    public static Specification<DailyTimeSheet> buildSpecification(TimeSheetFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by employee ID
            if (filter.employeeId() != null && !filter.employeeId().isBlank()) {
                predicates.add(criteriaBuilder.equal(
                    root.get("employee").get("userId"), 
                    filter.employeeId()
                ));
            }

            // Filter by date range - from date
            if (filter.dateFrom() != null && !filter.dateFrom().isBlank()) {
                try {
                    LocalDate fromDate = LocalDate.parse(filter.dateFrom());
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("date"), 
                        fromDate
                    ));
                } catch (Exception e) {
                    // Invalid date format - skip this filter
                }
            }

            // Filter by date range - to date
            if (filter.dateTo() != null && !filter.dateTo().isBlank()) {
                try {
                    LocalDate toDate = LocalDate.parse(filter.dateTo());
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("date"), 
                        toDate
                    ));
                } catch (Exception e) {
                    // Invalid date format - skip this filter
                }
            }

            // Filter by morning attendance status
            if (filter.morningStatus() != null && !filter.morningStatus().isBlank()) {
                try {
                    AttendanceStatus status = AttendanceStatus.valueOf(filter.morningStatus());
                    predicates.add(criteriaBuilder.equal(
                        root.get("morningStatus"), 
                        status
                    ));
                } catch (IllegalArgumentException e) {
                    // Invalid status - skip this filter
                }
            }

            // Filter by afternoon attendance status
            if (filter.afternoonStatus() != null && !filter.afternoonStatus().isBlank()) {
                try {
                    AttendanceStatus status = AttendanceStatus.valueOf(filter.afternoonStatus());
                    predicates.add(criteriaBuilder.equal(
                        root.get("afternoonStatus"), 
                        status
                    ));
                } catch (IllegalArgumentException e) {
                    // Invalid status - skip this filter
                }
            }

            // Filter by finalized status
            if (filter.isFinalized() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("isFinalized"), 
                    filter.isFinalized()
                ));
            }

            // Filter by WFH status (morning)
            if (filter.morningWfh() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("morningWfh"), 
                    filter.morningWfh()
                ));
            }

            // Filter by WFH status (afternoon)
            if (filter.afternoonWfh() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("afternoonWfh"), 
                    filter.afternoonWfh()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Helper Specification: Filter by employee ID
     */
    public static Specification<DailyTimeSheet> byEmployeeId(String employeeId) {
        return (root, query, cb) -> 
            cb.equal(root.get("employee").get("userId"), employeeId);
    }

    /**
     * Helper Specification: Filter by date range
     */
    public static Specification<DailyTimeSheet> byDateRange(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> 
            cb.between(root.get("date"), startDate, endDate);
    }

    /**
     * Helper Specification: Filter by specific date
     */
    public static Specification<DailyTimeSheet> byDate(LocalDate date) {
        return (root, query, cb) -> 
            cb.equal(root.get("date"), date);
    }

    /**
     * Helper Specification: Filter by finalized status
     */
    public static Specification<DailyTimeSheet> byFinalizedStatus(boolean isFinalized) {
        return (root, query, cb) -> 
            cb.equal(root.get("isFinalized"), isFinalized);
    }

    /**
     * Helper Specification: Filter by attendance status (morning)
     */
    public static Specification<DailyTimeSheet> byMorningStatus(AttendanceStatus status) {
        return (root, query, cb) -> 
            cb.equal(root.get("morningStatus"), status);
    }

    /**
     * Helper Specification: Filter by attendance status (afternoon)
     */
    public static Specification<DailyTimeSheet> byAfternoonStatus(AttendanceStatus status) {
        return (root, query, cb) -> 
            cb.equal(root.get("afternoonStatus"), status);
    }
}
