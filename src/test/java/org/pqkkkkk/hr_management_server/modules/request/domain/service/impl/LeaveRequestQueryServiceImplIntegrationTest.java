package org.pqkkkkk.hr_management_server.modules.request.domain.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.infrastructure.dao.jpa_repository.ProfileRepository;
import org.pqkkkkk.hr_management_server.modules.request.domain.command.LeaveRequestFilterCommand;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.LeaveRequestQueryService;
import org.pqkkkkk.hr_management_server.modules.request.infrastructure.dao.jpa_repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LeaveRequestQueryServiceImplIntegrationTest {
    
    @Autowired
    private LeaveRequestQueryService leaveRequestQueryService;
    
    @Autowired
    private RequestRepository requestRepository;
    
    @Autowired
    private ProfileRepository profileRepository;
    
    private User testEmployee;
    private User anotherEmployee;
    
    @BeforeEach
    void setUp() {
        // Clean up existing data
        requestRepository.deleteAll();
        profileRepository.deleteAll();
        
        // Create test employees
        testEmployee = User.builder()
            .email("test.employee@example.com")
            .fullName("Test Employee")
            .build();
        testEmployee = profileRepository.save(testEmployee);
        
        anotherEmployee = User.builder()
            .email("another.employee@example.com")
            .fullName("Another Employee")
            .build();
        anotherEmployee = profileRepository.save(anotherEmployee);
        
        // Create test leave requests for testEmployee
        createRequest(testEmployee, RequestType.LEAVE, RequestStatus.PENDING, "Sick Leave");
        createRequest(testEmployee, RequestType.LEAVE, RequestStatus.APPROVED, "Annual Leave");
        createRequest(testEmployee, RequestType.LEAVE, RequestStatus.REJECTED, "Personal Leave");
        createRequest(testEmployee, RequestType.WFH, RequestStatus.PENDING, "Work from home");
        
        // Create requests for another employee
        createRequest(anotherEmployee, RequestType.LEAVE, RequestStatus.PENDING, "Other Leave");
    }
    
    @Test
    @DisplayName("Should return all leave requests for employee without filters")
    void testGetMyLeaveRequests_NoFilters() {
        // Given
        LeaveRequestFilterCommand command = LeaveRequestFilterCommand.builder()
            .employeeId(testEmployee.getUserId())
            .page(0)
            .size(10)
            .build();
        
        // When
        Page<Request> result = leaveRequestQueryService.getMyLeaveRequests(command);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(4);
        assertThat(result.getTotalElements()).isEqualTo(4);
        
        // Verify sorting by createdAt DESC (latest first)
        List<Request> requests = result.getContent();
        for (int i = 0; i < requests.size() - 1; i++) {
            assertThat(requests.get(i).getCreatedAt())
                .isAfterOrEqualTo(requests.get(i + 1).getCreatedAt());
        }
    }
    
    @Test
    @DisplayName("Should filter leave requests by status")
    void testGetMyLeaveRequests_FilterByStatus() {
        // Given
        LeaveRequestFilterCommand command = LeaveRequestFilterCommand.builder()
            .employeeId(testEmployee.getUserId())
            .status(RequestStatus.PENDING)
            .page(0)
            .size(10)
            .build();
        
        // When
        Page<Request> result = leaveRequestQueryService.getMyLeaveRequests(command);
        
        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
            .allMatch(request -> request.getStatus() == RequestStatus.PENDING);
    }
    
    @Test
    @DisplayName("Should filter leave requests by request type")
    void testGetMyLeaveRequests_FilterByRequestType() {
        // Given
        LeaveRequestFilterCommand command = LeaveRequestFilterCommand.builder()
            .employeeId(testEmployee.getUserId())
            .requestType(RequestType.LEAVE)
            .page(0)
            .size(10)
            .build();
        
        // When
        Page<Request> result = leaveRequestQueryService.getMyLeaveRequests(command);
        
        // Then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent())
            .allMatch(request -> request.getRequestType() == RequestType.LEAVE);
    }
    
    @Test
    @DisplayName("Should filter leave requests by multiple criteria")
    void testGetMyLeaveRequests_MultipleFilters() {
        // Given
        LeaveRequestFilterCommand command = LeaveRequestFilterCommand.builder()
            .employeeId(testEmployee.getUserId())
            .requestType(RequestType.LEAVE)
            .status(RequestStatus.PENDING)
            .page(0)
            .size(10)
            .build();
        
        // When
        Page<Request> result = leaveRequestQueryService.getMyLeaveRequests(command);
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Sick Leave");
    }
    
    @Test
    @DisplayName("Should support pagination")
    void testGetMyLeaveRequests_Pagination() {
        // Given - First page
        LeaveRequestFilterCommand command1 = LeaveRequestFilterCommand.builder()
            .employeeId(testEmployee.getUserId())
            .page(0)
            .size(2)
            .build();
        
        // When
        Page<Request> result1 = leaveRequestQueryService.getMyLeaveRequests(command1);
        
        // Then
        assertThat(result1.getContent()).hasSize(2);
        assertThat(result1.getTotalElements()).isEqualTo(4);
        assertThat(result1.getTotalPages()).isEqualTo(2);
        
        // Given - Second page
        LeaveRequestFilterCommand command2 = LeaveRequestFilterCommand.builder()
            .employeeId(testEmployee.getUserId())
            .page(1)
            .size(2)
            .build();
        
        // When
        Page<Request> result2 = leaveRequestQueryService.getMyLeaveRequests(command2);
        
        // Then
        assertThat(result2.getContent()).hasSize(2);
        assertThat(result2.getTotalElements()).isEqualTo(4);
    }
    
    @Test
    @DisplayName("Should only return requests for specified employee")
    void testGetMyLeaveRequests_OnlyForSpecifiedEmployee() {
        // Given
        LeaveRequestFilterCommand command = LeaveRequestFilterCommand.builder()
            .employeeId(testEmployee.getUserId())
            .page(0)
            .size(10)
            .build();
        
        // When
        Page<Request> result = leaveRequestQueryService.getMyLeaveRequests(command);
        
        // Then
        assertThat(result.getContent()).hasSize(4);
        assertThat(result.getContent())
            .allMatch(request -> request.getEmployee().getUserId().equals(testEmployee.getUserId()));
    }
    
    @Test
    @DisplayName("Should use default pagination values when not provided")
    void testGetMyLeaveRequests_DefaultPagination() {
        // Given
        LeaveRequestFilterCommand command = LeaveRequestFilterCommand.builder()
            .employeeId(testEmployee.getUserId())
            .build();
        
        // When
        Page<Request> result = leaveRequestQueryService.getMyLeaveRequests(command);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNumber()).isEqualTo(0); // Default page
        assertThat(result.getSize()).isEqualTo(10); // Default size
    }
    
    @Test
    @DisplayName("Should return empty page when no requests match filters")
    void testGetMyLeaveRequests_NoResults() {
        // Given
        LeaveRequestFilterCommand command = LeaveRequestFilterCommand.builder()
            .employeeId(testEmployee.getUserId())
            .status(RequestStatus.CANCELLED)
            .page(0)
            .size(10)
            .build();
        
        // When
        Page<Request> result = leaveRequestQueryService.getMyLeaveRequests(command);
        
        // Then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Should filter by date range")
    void testGetMyLeaveRequests_FilterByDateRange() {
        // Given - Filter by date range using tomorrow (no requests should match)
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        
        LeaveRequestFilterCommand command = LeaveRequestFilterCommand.builder()
            .employeeId(testEmployee.getUserId())
            .startDate(tomorrow)
            .page(0)
            .size(10)
            .build();
        
        // When
        Page<Request> result = leaveRequestQueryService.getMyLeaveRequests(command);
        
        // Then - No requests created after tomorrow
        assertThat(result.getContent()).isEmpty();
        
        // Test with yesterday - all requests should match
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LeaveRequestFilterCommand command2 = LeaveRequestFilterCommand.builder()
            .employeeId(testEmployee.getUserId())
            .startDate(yesterday)
            .page(0)
            .size(10)
            .build();
        
        Page<Request> result2 = leaveRequestQueryService.getMyLeaveRequests(command2);
        assertThat(result2.getContent()).hasSize(4); // All today's requests
    }
    
    @Test
    @DisplayName("Should verify employee isolation - different employees see different requests")
    void testGetMyLeaveRequests_EmployeeIsolation() {
        // Given
        LeaveRequestFilterCommand commandEmployee1 = LeaveRequestFilterCommand.builder()
            .employeeId(testEmployee.getUserId())
            .page(0)
            .size(10)
            .build();
        
        LeaveRequestFilterCommand commandEmployee2 = LeaveRequestFilterCommand.builder()
            .employeeId(anotherEmployee.getUserId())
            .page(0)
            .size(10)
            .build();
        
        // When
        Page<Request> resultEmployee1 = leaveRequestQueryService.getMyLeaveRequests(commandEmployee1);
        Page<Request> resultEmployee2 = leaveRequestQueryService.getMyLeaveRequests(commandEmployee2);
        
        // Then
        assertThat(resultEmployee1.getContent()).hasSize(4);
        assertThat(resultEmployee2.getContent()).hasSize(1);
        
        // Verify no overlap
        assertThat(resultEmployee1.getContent())
            .allMatch(r -> r.getEmployee().getUserId().equals(testEmployee.getUserId()));
        assertThat(resultEmployee2.getContent())
            .allMatch(r -> r.getEmployee().getUserId().equals(anotherEmployee.getUserId()));
    }
    
    @Test
    @DisplayName("Should sort by createdAt ASC when specified")
    void testGetMyLeaveRequests_SortAscending() {
        // Given
        LeaveRequestFilterCommand command = LeaveRequestFilterCommand.builder()
            .employeeId(testEmployee.getUserId())
            .sortBy("createdAt")
            .sortDirection("ASC")
            .page(0)
            .size(10)
            .build();
        
        // When
        Page<Request> result = leaveRequestQueryService.getMyLeaveRequests(command);
        
        // Then
        assertThat(result.getContent()).hasSize(4);
        
        // Verify sorting by createdAt ASC (oldest first)
        List<Request> requests = result.getContent();
        for (int i = 0; i < requests.size() - 1; i++) {
            assertThat(requests.get(i).getCreatedAt())
                .isBeforeOrEqualTo(requests.get(i + 1).getCreatedAt());
        }
    }
    
    @Test
    @DisplayName("Should handle large page size by limiting to MAX_SIZE")
    void testGetMyLeaveRequests_LargePageSize() {
        // Given
        LeaveRequestFilterCommand command = LeaveRequestFilterCommand.builder()
            .employeeId(testEmployee.getUserId())
            .page(0)
            .size(999) // Request very large size
            .build();
        
        // When
        Page<Request> result = leaveRequestQueryService.getMyLeaveRequests(command);
        
        // Then - Should be limited but still return results
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(4); // Only 4 requests exist
        assertThat(result.getSize()).isLessThanOrEqualTo(100); // Limited to MAX_SIZE
    }
    
    @Test
    @DisplayName("Should return correct pagination metadata")
    void testGetMyLeaveRequests_PaginationMetadata() {
        // Given
        LeaveRequestFilterCommand command = LeaveRequestFilterCommand.builder()
            .employeeId(testEmployee.getUserId())
            .page(0)
            .size(2)
            .build();
        
        // When
        Page<Request> result = leaveRequestQueryService.getMyLeaveRequests(command);
        
        // Then - Verify all pagination metadata
        assertThat(result.getNumber()).isEqualTo(0); // Current page
        assertThat(result.getSize()).isEqualTo(2); // Page size
        assertThat(result.getTotalElements()).isEqualTo(4); // Total records
        assertThat(result.getTotalPages()).isEqualTo(2); // Total pages
        assertThat(result.hasNext()).isTrue(); // Has next page
        assertThat(result.hasPrevious()).isFalse(); // No previous page
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isFalse();
    }
    
    @Test
    @DisplayName("Should filter by end date")
    void testGetMyLeaveRequests_FilterByEndDate() {
        // Given
        LeaveRequestFilterCommand command = LeaveRequestFilterCommand.builder()
            .employeeId(testEmployee.getUserId())
            .endDate(LocalDate.now().plusDays(1)) // All requests should be before this
            .page(0)
            .size(10)
            .build();
        
        // When
        Page<Request> result = leaveRequestQueryService.getMyLeaveRequests(command);
        
        // Then
        assertThat(result.getContent()).hasSize(4);
        assertThat(result.getContent())
            .allMatch(r -> r.getCreatedAt().toLocalDate().isBefore(LocalDate.now().plusDays(2)));
    }
    
    @Test
    @DisplayName("Should work correctly with H2 in-memory database")
    void testGetMyLeaveRequests_WithH2Database() {
        // Given - This test verifies H2 database integration
        requestRepository.deleteAll();
        profileRepository.deleteAll();
        
        // Create fresh test data
        User employee = User.builder()
            .email("h2.test@example.com")
            .fullName("H2 Test User")
            .build();
        employee = profileRepository.save(employee);
        
        createRequest(employee, RequestType.LEAVE, RequestStatus.PENDING, "H2 Test Leave");
        
        LeaveRequestFilterCommand command = LeaveRequestFilterCommand.builder()
            .employeeId(employee.getUserId())
            .page(0)
            .size(10)
            .build();
        
        // When
        Page<Request> result = leaveRequestQueryService.getMyLeaveRequests(command);
        
        // Then - Verify data persists and retrieves correctly from H2
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("H2 Test Leave");
        assertThat(result.getContent().get(0).getEmployee().getEmail()).isEqualTo("h2.test@example.com");
    }
    
    private Request createRequest(User employee, RequestType type, RequestStatus status, String title) {
        Request.RequestBuilder builder = Request.builder()
            .employee(employee)
            .requestType(type)
            .status(status)
            .title(title)
            .userReason("Test reason");
        
        // Add processed_at for APPROVED/REJECTED status (constraint requirement)
        if (status == RequestStatus.APPROVED || status == RequestStatus.REJECTED) {
            builder.processedAt(LocalDateTime.now());
        }
        
        // Add reject_reason for REJECTED status (constraint requirement)
        if (status == RequestStatus.REJECTED) {
            builder.rejectReason("Test rejection reason");
        }
        
        return requestRepository.save(builder.build());
    }
}
