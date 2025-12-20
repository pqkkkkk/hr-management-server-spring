package org.pqkkkkk.hr_management_server.modules.request.domain.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.filter.FilterCriteria.RequestFilter;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestQueryService;
import org.pqkkkkk.hr_management_server.shared.Constants.SortDirection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("RequestQueryService Integration Tests")
class RequestQueryServiceImplIntegrationTest {

    @Autowired
    private RequestQueryService requestQueryService;

    private String testEmployeeId;
    private String testApproverId;
    private String testProcessorId;
    private String testDepartmentId;
    private String testPendingRequestId;
    private String testApprovedRequestId;

    @BeforeEach
    void setUp() {
        // Sample data is loaded from Flyway migrations
        // V3__insert_profile_sample_data.sql - Users
        // V5__insert_request_sample_data.sql - Initial requests
        // V6__add_leave_request_test_data.sql - Extended test data

        testEmployeeId = "u1a2b3c4-e5f6-7890-abcd-ef1234567890"; // Employee from profile migration
        testApproverId = "u2b3c4d5-f6a7-8901-bcde-f12345678901"; // Manager from profile migration
        testProcessorId = "u2b3c4d5-f6a7-8901-bcde-f12345678901"; // Same as approver
        testDepartmentId = "dept-001"; // Engineering department

        testPendingRequestId = "req-leave-pending-valid";
        testApprovedRequestId = "req-leave-approved";
    }

    // ==================== getRequests Tests ====================

    @Test
    @DisplayName("Should get all requests with no filters")
    void testGetRequests_NoFilters_Success() {
        // Arrange
        RequestFilter filter = new RequestFilter(
                null, null, null, null, null, null, null, null,
                null, 1, 10, "createdAt", SortDirection.DESC);

        // Act
        Page<Request> result = requestQueryService.getRequests(filter);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        assertTrue(result.getContent().size() > 0);
    }

    @Test
    @DisplayName("Should filter requests by employee ID")
    void testGetRequests_FilterByEmployeeId_Success() {
        // Arrange
        RequestFilter filter = new RequestFilter(
                testEmployeeId, null, null, null, null, null, null, null,
                null, 1, 10, "createdAt", SortDirection.DESC);

        // Act
        Page<Request> result = requestQueryService.getRequests(filter);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        result.getContent().forEach(request -> assertEquals(testEmployeeId, request.getEmployee().getUserId()));
    }

    @Test
    @DisplayName("Should filter requests by approver ID")
    void testGetRequests_FilterByApproverId_Success() {
        // Arrange
        RequestFilter filter = new RequestFilter(
                null, testApproverId, null, null, null, null, null, null,
                null, 1, 10, "createdAt", SortDirection.DESC);

        // Act
        Page<Request> result = requestQueryService.getRequests(filter);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        result.getContent().forEach(request -> {
            assertNotNull(request.getApprover());
            assertEquals(testApproverId, request.getApprover().getUserId());
        });
    }

    @Test
    @DisplayName("Should filter requests by processor ID")
    void testGetRequests_FilterByProcessorId_Success() {
        // Arrange
        RequestFilter filter = new RequestFilter(
                null, null, testProcessorId, null, null, null, null, null,
                null, 1, 10, "createdAt", SortDirection.DESC);

        // Act
        Page<Request> result = requestQueryService.getRequests(filter);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        result.getContent().forEach(request -> {
            assertNotNull(request.getProcessor());
            assertEquals(testProcessorId, request.getProcessor().getUserId());
        });
    }

    @Test
    @DisplayName("Should filter requests by department ID")
    void testGetRequests_FilterByDepartmentId_Success() {
        // Arrange
        RequestFilter filter = new RequestFilter(
                null, null, null, testDepartmentId, null, null, null, null,
                null, 1, 10, "createdAt", SortDirection.DESC);

        // Act
        Page<Request> result = requestQueryService.getRequests(filter);

        // Assert
        assertNotNull(result);
        result.getContent().forEach(request -> {
            assertNotNull(request.getEmployee().getDepartment());
            assertEquals(testDepartmentId, request.getEmployee().getDepartment().getDepartmentId());
        });
    }

    @Test
    @DisplayName("Should filter requests by status")
    void testGetRequests_FilterByStatus_Success() {
        // Arrange
        RequestFilter filter = new RequestFilter(
                null, null, null, null, null, RequestStatus.PENDING, null, null,
                null, 1, 10, "createdAt", SortDirection.DESC);

        // Act
        Page<Request> result = requestQueryService.getRequests(filter);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        result.getContent().forEach(request -> assertEquals(RequestStatus.PENDING, request.getStatus()));
    }

    @Test
    @DisplayName("Should filter requests by type")
    void testGetRequests_FilterByType_Success() {
        // Arrange
        RequestFilter filter = new RequestFilter(
                null, null, null, null, null, null, RequestType.LEAVE, null,
                null, 1, 10, "createdAt", SortDirection.DESC);

        // Act
        Page<Request> result = requestQueryService.getRequests(filter);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        result.getContent().forEach(request -> assertEquals(RequestType.LEAVE, request.getRequestType()));
    }

    @Test
    @DisplayName("Should filter requests by employee name (partial match)")
    void testGetRequests_FilterByNameTerm_Success() {
        // Arrange - Search for employees with "Nguyen" in their name
        RequestFilter filter = new RequestFilter(
                null, null, null, null, "Nguyen", null, null, null,
                null, 1, 10, "createdAt", SortDirection.DESC);

        // Act
        Page<Request> result = requestQueryService.getRequests(filter);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        result.getContent().forEach(request -> {
            assertNotNull(request.getEmployee());
            assertTrue(request.getEmployee().getFullName().toLowerCase().contains("nguyen"));
        });
    }

    @Test
    @DisplayName("Should filter requests by employee name (case-insensitive)")
    void testGetRequests_FilterByNameTerm_CaseInsensitive_Success() {
        // Arrange - Search with different case
        RequestFilter filter = new RequestFilter(
                null, null, null, null, "NGUYEN", null, null, null,
                null, 1, 10, "createdAt", SortDirection.DESC);

        // Act
        Page<Request> result = requestQueryService.getRequests(filter);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);
        result.getContent().forEach(request -> {
            assertNotNull(request.getEmployee());
            assertTrue(request.getEmployee().getFullName().toLowerCase().contains("nguyen"));
        });
    }

    @Test
    @DisplayName("Should return empty page when name term does not match any employee")
    void testGetRequests_FilterByNameTerm_NoMatch_EmptyPage() {
        // Arrange - Search for a name that doesn't exist
        RequestFilter filter = new RequestFilter(
                null, null, null, null, "XYZNonExistentName", null, null, null,
                null, 1, 10, "createdAt", SortDirection.DESC);

        // Act
        Page<Request> result = requestQueryService.getRequests(filter);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("Should filter requests by date range")
    void testGetRequests_FilterByDateRange_Success() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now().plusDays(10);

        RequestFilter filter = new RequestFilter(
                null, null, null, null, null, null, null, startDate,
                endDate, 1, 10, "createdAt", SortDirection.DESC);

        // Act
        Page<Request> result = requestQueryService.getRequests(filter);

        // Assert
        assertNotNull(result);
        result.getContent().forEach(request -> {
            LocalDate createdDate = request.getCreatedAt().toLocalDate();
            assertTrue(
                    (createdDate.isEqual(startDate) || createdDate.isAfter(startDate)) &&
                            (createdDate.isEqual(endDate) || createdDate.isBefore(endDate)));
        });
    }

    @Test
    @DisplayName("Should filter requests with multiple criteria")
    void testGetRequests_MultipleCriteria_Success() {
        // Arrange
        RequestFilter filter = new RequestFilter(
                testEmployeeId,
                null,
                null,
                null,
                null,
                RequestStatus.PENDING,
                RequestType.LEAVE,
                null,
                null,
                1,
                10,
                "createdAt",
                SortDirection.DESC);

        // Act
        Page<Request> result = requestQueryService.getRequests(filter);

        // Assert
        assertNotNull(result);
        result.getContent().forEach(request -> {
            assertEquals(testEmployeeId, request.getEmployee().getUserId());
            assertEquals(RequestStatus.PENDING, request.getStatus());
            assertEquals(RequestType.LEAVE, request.getRequestType());
        });
    }

    @Test
    @DisplayName("Should return empty page when no requests match filter")
    void testGetRequests_NoMatchingRequests_EmptyPage() {
        // Arrange - Using a non-existent employee ID
        RequestFilter filter = new RequestFilter(
                "non-existent-id", null, null, null, null, null, null, null,
                null, 1, 10, "createdAt", SortDirection.DESC);

        // Act
        Page<Request> result = requestQueryService.getRequests(filter);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("Should sort requests ascending")
    void testGetRequests_SortAscending_Success() {
        // Arrange
        RequestFilter filter = new RequestFilter(
                null, null, null, null, null, null, null, null,
                null, 1, 10, "createdAt", SortDirection.ASC);

        // Act
        Page<Request> result = requestQueryService.getRequests(filter);

        // Assert
        assertNotNull(result);
        if (result.getContent().size() > 1) {
            for (int i = 0; i < result.getContent().size() - 1; i++) {
                assertTrue(
                        result.getContent().get(i).getCreatedAt()
                                .isBefore(result.getContent().get(i + 1).getCreatedAt()) ||
                                result.getContent().get(i).getCreatedAt()
                                        .isEqual(result.getContent().get(i + 1).getCreatedAt()));
            }
        }
    }

    @Test
    @DisplayName("Should sort requests descending")
    void testGetRequests_SortDescending_Success() {
        // Arrange
        RequestFilter filter = new RequestFilter(
                null, null, null, null, null, null, null, null,
                null, 1, 10, "createdAt", SortDirection.DESC);

        // Act
        Page<Request> result = requestQueryService.getRequests(filter);

        // Assert
        assertNotNull(result);
        if (result.getContent().size() > 1) {
            for (int i = 0; i < result.getContent().size() - 1; i++) {
                assertTrue(
                        result.getContent().get(i).getCreatedAt()
                                .isAfter(result.getContent().get(i + 1).getCreatedAt()) ||
                                result.getContent().get(i).getCreatedAt()
                                        .isEqual(result.getContent().get(i + 1).getCreatedAt()));
            }
        }
    }

    @Test
    @DisplayName("Should throw exception when filter is null")
    void testGetRequests_NullFilter_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> requestQueryService.getRequests(null));

        assertEquals("Filter cannot be null", exception.getMessage());
    }

    // ==================== getRequestById Tests ====================

    @Test
    @DisplayName("Should get request by valid ID")
    void testGetRequestById_ValidId_Success() {
        // Act
        Request result = requestQueryService.getRequestById(testPendingRequestId);

        // Assert
        assertNotNull(result);
        assertEquals(testPendingRequestId, result.getRequestId());
        assertEquals(RequestStatus.PENDING, result.getStatus());
        assertEquals(RequestType.LEAVE, result.getRequestType());
    }

    @Test
    @DisplayName("Should get approved request by ID")
    void testGetRequestById_ApprovedRequest_Success() {
        // Act
        Request result = requestQueryService.getRequestById(testApprovedRequestId);

        // Assert
        assertNotNull(result);
        assertEquals(testApprovedRequestId, result.getRequestId());
        assertEquals(RequestStatus.APPROVED, result.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when request not found")
    void testGetRequestById_NotFound_ThrowsException() {
        // Arrange
        String nonExistentId = "non-existent-request-id";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> requestQueryService.getRequestById(nonExistentId));

        assertTrue(exception.getMessage().contains("Request not found"));
        assertTrue(exception.getMessage().contains(nonExistentId));
    }

    @Test
    @DisplayName("Should throw exception when request ID is null")
    void testGetRequestById_NullId_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> requestQueryService.getRequestById(null));

        assertEquals("Request ID cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when request ID is empty")
    void testGetRequestById_EmptyId_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> requestQueryService.getRequestById(""));

        assertEquals("Request ID cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when request ID is blank")
    void testGetRequestById_BlankId_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> requestQueryService.getRequestById("   "));

        assertEquals("Request ID cannot be null or empty", exception.getMessage());
    }

    // ==================== Complex Scenario Tests ====================

    @Test
    @DisplayName("Should get employee's own requests only")
    void testGetRequests_EmployeeViewOwnRequests_Success() {
        // Arrange - Simulating employee viewing their own requests
        RequestFilter filter = new RequestFilter(
                testEmployeeId, // Only this employee's requests
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                1,
                10,
                "createdAt",
                SortDirection.DESC);

        // Act
        Page<Request> result = requestQueryService.getRequests(filter);

        // Assert
        assertNotNull(result);
        result.getContent().forEach(request -> assertEquals(testEmployeeId, request.getEmployee().getUserId()));
    }

    @Test
    @DisplayName("Should get requests for manager to approve")
    void testGetRequests_ManagerViewPendingRequests_Success() {
        // Arrange - Simulating manager viewing pending requests to approve
        RequestFilter filter = new RequestFilter(
                null,
                testApproverId, // Requests where this user is approver
                null,
                testDepartmentId, // From their department
                null,
                RequestStatus.PENDING, // Only pending
                null,
                null,
                null,
                1,
                10,
                "createdAt",
                SortDirection.DESC);

        // Act
        Page<Request> result = requestQueryService.getRequests(filter);

        // Assert
        assertNotNull(result);
        result.getContent().forEach(request -> {
            assertNotNull(request.getApprover());
            assertEquals(testApproverId, request.getApprover().getUserId());
            assertEquals(RequestStatus.PENDING, request.getStatus());
        });
    }

    @Test
    @DisplayName("Should get requests assigned to processor")
    void testGetRequests_ProcessorViewAssignedRequests_Success() {
        // Arrange - Simulating processor viewing requests assigned to them
        RequestFilter filter = new RequestFilter(
                null,
                null,
                testProcessorId, // Requests assigned to this processor
                null,
                null,
                null,
                null,
                null,
                null,
                1,
                10,
                "createdAt",
                SortDirection.DESC);

        // Act
        Page<Request> result = requestQueryService.getRequests(filter);

        // Assert
        assertNotNull(result);
        result.getContent().forEach(request -> {
            assertNotNull(request.getProcessor());
            assertEquals(testProcessorId, request.getProcessor().getUserId());
        });
    }

    @Test
    @DisplayName("Should get all leave requests in date range with status")
    void testGetRequests_LeaveRequestsInDateRange_Success() {
        // Arrange - HR viewing all leave requests in a specific period
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(30);

        RequestFilter filter = new RequestFilter(
                null,
                null,
                null,
                null,
                null,
                null,
                RequestType.LEAVE,
                startDate,
                endDate,
                1,
                20,
                "createdAt",
                SortDirection.DESC);

        // Act
        Page<Request> result = requestQueryService.getRequests(filter);

        // Assert
        assertNotNull(result);
        result.getContent().forEach(request -> {
            assertEquals(RequestType.LEAVE, request.getRequestType());
            LocalDate createdDate = request.getCreatedAt().toLocalDate();
            assertTrue(
                    (createdDate.isEqual(startDate) || createdDate.isAfter(startDate)) &&
                            (createdDate.isEqual(endDate) || createdDate.isBefore(endDate)));
        });
    }
}
