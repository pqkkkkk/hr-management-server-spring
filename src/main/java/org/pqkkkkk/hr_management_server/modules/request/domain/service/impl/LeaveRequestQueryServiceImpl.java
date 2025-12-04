package org.pqkkkkk.hr_management_server.modules.request.domain.service.impl;

import org.pqkkkkk.hr_management_server.modules.request.domain.command.LeaveRequestFilterCommand;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.LeaveRequestQueryService;
import org.pqkkkkk.hr_management_server.modules.request.infrastructure.dao.jpa_repository.RequestRepository;
import org.pqkkkkk.hr_management_server.modules.request.infrastructure.specification.LeaveRequestSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LeaveRequestQueryServiceImpl implements LeaveRequestQueryService {
    
    private final RequestRepository requestRepository;
    
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;
    
    @Override
    public Page<Request> getMyLeaveRequests(LeaveRequestFilterCommand filterCommand) {
        log.info("Fetching leave requests for employee: {} with filters: {}", 
                 filterCommand.getEmployeeId(), filterCommand);
        
        // Validate and set pagination defaults
        int page = filterCommand.getPage() != null ? filterCommand.getPage() : DEFAULT_PAGE;
        int size = filterCommand.getSize() != null ? filterCommand.getSize() : DEFAULT_SIZE;
        
        // Limit size to prevent excessive queries
        if (size > MAX_SIZE) {
            size = MAX_SIZE;
        }
        
        // Create sort configuration (default: createdAt DESC)
        Sort sort = createSort(filterCommand);
        
        // Create pageable
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Build specification for dynamic filtering
        Specification<Request> specification = LeaveRequestSpecification.filterLeaveRequests(filterCommand);
        
        // Execute query with pagination and filtering
        Page<Request> result = requestRepository.findAll(specification, pageable);
        
        log.info("Found {} leave requests for employee: {}", 
                 result.getTotalElements(), filterCommand.getEmployeeId());
        
        return result;
    }
    
    private Sort createSort(LeaveRequestFilterCommand filterCommand) {
        String sortBy = filterCommand.getSortBy() != null ? filterCommand.getSortBy() : "createdAt";
        String sortDirection = filterCommand.getSortDirection() != null ? filterCommand.getSortDirection() : "DESC";
        
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        
        return Sort.by(direction, sortBy);
    }
}
