package org.pqkkkkk.hr_management_server.modules.request.infrastructure.dao;

import java.util.ArrayList;
import java.util.List;

import org.pqkkkkk.hr_management_server.modules.request.domain.dao.RequestDao;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.filter.FilterCriteria.RequestFilter;
import org.pqkkkkk.hr_management_server.modules.request.infrastructure.dao.jpa_repository.RequestRepository;
import org.pqkkkkk.hr_management_server.shared.Constants.SortDirection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.Predicate;

@Repository
public class RequestJpaDao implements RequestDao {
    
    private final RequestRepository requestRepository;

    public RequestJpaDao(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Override
    public Request createRequest(Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        
        // Ensure this is a new request (no ID set yet, or ID doesn't exist)
        if (request.getRequestId() != null && requestRepository.existsById(request.getRequestId())) {
            throw new IllegalArgumentException("Request with ID " + request.getRequestId() + " already exists");
        }
        
        return requestRepository.save(request);
    }

    @Override
    public Request updateRequest(Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        
        if (request.getRequestId() == null) {
            throw new IllegalArgumentException("Request ID cannot be null for update operation");
        }
        
        return requestRepository.save(request);
    }

    @Override
    public Request getRequestById(String requestId) {
        return requestRepository.findById(requestId).orElse(null);
    }

    @Override
    public Page<Request> getRequests(RequestFilter filter) {
        Specification<Request> specification = buildSpecification(filter);
        Pageable pageable = buildPageable(filter);
        return requestRepository.findAll(specification, pageable);
    }

    /**
     * Build JPA Specification from RequestFilter
     */
    private Specification<Request> buildSpecification(RequestFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by employee ID
            if (filter.employeeId() != null && !filter.employeeId().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("employee").get("userId"), filter.employeeId()));
            }

            // Filter by approver ID
            if (filter.approverId() != null && !filter.approverId().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("approver").get("userId"), filter.approverId()));
            }

            // Filter by processor ID
            if (filter.processorId() != null && !filter.processorId().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("processor").get("userId"), filter.processorId()));
            }

            // Filter by department ID (via employee's department)
            if (filter.departmentId() != null && !filter.departmentId().isBlank()) {
                predicates.add(criteriaBuilder.equal(
                    root.get("employee").get("department").get("departmentId"), 
                    filter.departmentId()
                ));
            }

            // Filter by request status
            if (filter.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.status()));
            }

            // Filter by request type
            if (filter.type() != null) {
                predicates.add(criteriaBuilder.equal(root.get("requestType"), filter.type()));
            }

            // Filter by date range (createdAt)
            if (filter.startDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("createdAt").as(java.time.LocalDate.class),
                    filter.startDate()
                ));
            }

            if (filter.endDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("createdAt").as(java.time.LocalDate.class),
                    filter.endDate()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Build Pageable object from RequestFilter
     */
    private Pageable buildPageable(RequestFilter filter) {
        Sort sort = filter.sortDirection() == SortDirection.ASC
            ? Sort.by(filter.sortBy()).ascending()
            : Sort.by(filter.sortBy()).descending();

        return PageRequest.of(
            filter.currentPage() - 1, // Spring Data JPA uses 0-based page index
            filter.pageSize(),
            sort
        );
    }
}
