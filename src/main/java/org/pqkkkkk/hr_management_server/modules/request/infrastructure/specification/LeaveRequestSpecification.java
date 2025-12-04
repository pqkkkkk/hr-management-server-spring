package org.pqkkkkk.hr_management_server.modules.request.infrastructure.specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.pqkkkkk.hr_management_server.modules.request.domain.command.LeaveRequestFilterCommand;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

public class LeaveRequestSpecification {
    
    public static Specification<Request> filterLeaveRequests(LeaveRequestFilterCommand filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Filter by employeeId (required)
            if (filter.getEmployeeId() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("employee").get("userId"), 
                    filter.getEmployeeId()
                ));
            }
            
            // Filter by status
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("status"), 
                    filter.getStatus()
                ));
            }
            
            // Filter by requestType
            if (filter.getRequestType() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("requestType"), 
                    filter.getRequestType()
                ));
            }
            
            // Filter by date range (createdAt)
            if (filter.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("createdAt").as(LocalDate.class), 
                    filter.getStartDate()
                ));
            }
            
            if (filter.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("createdAt").as(LocalDate.class), 
                    filter.getEndDate()
                ));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
