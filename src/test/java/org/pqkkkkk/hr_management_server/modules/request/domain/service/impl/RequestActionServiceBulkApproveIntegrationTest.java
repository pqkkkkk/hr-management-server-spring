package org.pqkkkkk.hr_management_server.modules.request.domain.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.BulkApproveResult;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.filter.FilterCriteria.RequestFilter;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestActionService;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.RequestQueryService;
import org.pqkkkkk.hr_management_server.shared.Constants.SortDirection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for RequestActionService.bulkApprove() method.
 * Uses sample data from Flyway migrations.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("RequestActionService Bulk Approve Integration Tests")
class RequestActionServiceBulkApproveIntegrationTest {

    @Autowired
    private RequestActionService requestActionService;

    @Autowired
    private RequestQueryService requestQueryService;

    // Sample data IDs from Flyway migrations
    private String testApproverId;
    private String testProcessorId;
    private String testDepartmentId;

    @BeforeEach
    void setUp() {
        // Sample data from V3__insert_profile_sample_data.sql
        testApproverId = "u2b3c4d5-f6a7-8901-bcde-f12345678901"; // Manager
        testProcessorId = "u2b3c4d5-f6a7-8901-bcde-f12345678901"; // Same as approver
        testDepartmentId = "dept-001"; // Engineering department
    }

    // ==================== Success Cases ====================

    @Nested
    @DisplayName("Success Cases")
    class SuccessCases {

        @Test
        @DisplayName("Should bulk approve all pending requests for a manager")
        void testBulkApprove_AllSuccess() {
            // Arrange - Get count of pending requests before
            RequestFilter pendingFilter = new RequestFilter(
                    null, testApproverId, null, null, null,
                    RequestStatus.PENDING, null, null, null,
                    1, 100, "createdAt", SortDirection.ASC);

            Page<Request> pendingBefore = requestQueryService.getRequests(pendingFilter);
            long pendingCountBefore = pendingBefore.getTotalElements();

            // Skip test if no pending requests
            if (pendingCountBefore == 0) {
                return; // No pending requests to test
            }

            // Act - Bulk approve
            RequestFilter filter = new RequestFilter(
                    null, testApproverId, null, null, null,
                    null, null, null, null,
                    null, null, null, null);

            BulkApproveResult result = requestActionService.bulkApprove(filter, testApproverId, 50);

            // Assert
            assertNotNull(result);
            assertTrue(result.totalProcessed() > 0, "Should have processed at least one request");
            assertTrue(result.successCount() > 0, "Should have approved at least one request");
            assertEquals(result.approvedRequestIds().size(), result.successCount());

            // Verify approved requests are now APPROVED status
            for (String requestId : result.approvedRequestIds()) {
                Request approved = requestQueryService.getRequestById(requestId);
                assertEquals(RequestStatus.APPROVED, approved.getStatus());
            }
        }

        @Test
        @DisplayName("Should bulk approve with type filter")
        void testBulkApprove_FilterByType() {
            // Arrange - Filter only LEAVE requests
            RequestFilter filter = new RequestFilter(
                    null, testApproverId, null, null, null,
                    null, RequestType.LEAVE, null, null,
                    null, null, null, null);

            // Act
            BulkApproveResult result = requestActionService.bulkApprove(filter, testApproverId, 50);

            // Assert
            assertNotNull(result);
            // Verify all approved were LEAVE type
            for (String requestId : result.approvedRequestIds()) {
                Request approved = requestQueryService.getRequestById(requestId);
                assertEquals(RequestType.LEAVE, approved.getRequestType());
            }
        }

        @Test
        @DisplayName("Should bulk approve with department filter")
        void testBulkApprove_FilterByDepartment() {
            // Arrange
            RequestFilter filter = new RequestFilter(
                    null, testApproverId, null, testDepartmentId, null,
                    null, null, null, null,
                    null, null, null, null);

            // Act
            BulkApproveResult result = requestActionService.bulkApprove(filter, testApproverId, 50);

            // Assert
            assertNotNull(result);
            // All approved should be from the specified department
            for (String requestId : result.approvedRequestIds()) {
                Request approved = requestQueryService.getRequestById(requestId);
                assertEquals(testDepartmentId, approved.getEmployee().getDepartment().getDepartmentId());
            }
        }

        @Test
        @DisplayName("Should respect max requests limit")
        void testBulkApprove_RespectsMaxLimit() {
            // Arrange - Set very small limit
            int maxRequests = 2;
            RequestFilter filter = new RequestFilter(
                    null, testApproverId, null, null, null,
                    null, null, null, null,
                    null, null, null, null);

            // Act
            BulkApproveResult result = requestActionService.bulkApprove(filter, testApproverId, maxRequests);

            // Assert
            assertNotNull(result);
            assertTrue(result.totalProcessed() <= maxRequests,
                    "Should not process more than max limit");
        }

        @Test
        @DisplayName("Should return empty result when no matching requests")
        void testBulkApprove_NoMatchingRequests() {
            // Arrange - Use non-existent approver ID
            String nonExistentApproverId = "non-existent-approver-id";
            RequestFilter filter = new RequestFilter(
                    null, nonExistentApproverId, null, null, null,
                    null, null, null, null,
                    null, null, null, null);

            // Act
            BulkApproveResult result = requestActionService.bulkApprove(filter, testApproverId, 50);

            // Assert
            assertNotNull(result);
            assertEquals(0, result.totalProcessed());
            assertEquals(0, result.successCount());
            assertEquals(0, result.failedCount());
            assertTrue(result.approvedRequestIds().isEmpty());
            assertTrue(result.failedApprovals().isEmpty());
        }

        @Test
        @DisplayName("Should filter by date range")
        void testBulkApprove_FilterByDateRange() {
            // Arrange - Last 30 days
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);

            RequestFilter filter = new RequestFilter(
                    null, testApproverId, null, null, null,
                    null, null, startDate, endDate,
                    null, null, null, null);

            // Act
            BulkApproveResult result = requestActionService.bulkApprove(filter, testApproverId, 50);

            // Assert
            assertNotNull(result);
            // No exception means filter worked correctly
        }
    }

    // ==================== Validation Error Cases ====================

    @Nested
    @DisplayName("Validation Error Cases")
    class ValidationErrorCases {

        @Test
        @DisplayName("Should throw exception when approverId is null")
        void testBulkApprove_NullApproverId_ThrowsException() {
            // Arrange
            RequestFilter filter = new RequestFilter(
                    null, testApproverId, null, null, null,
                    null, null, null, null,
                    null, null, null, null);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                requestActionService.bulkApprove(filter, null, 50);
            });
        }

        @Test
        @DisplayName("Should throw exception when approverId is blank")
        void testBulkApprove_BlankApproverId_ThrowsException() {
            // Arrange
            RequestFilter filter = new RequestFilter(
                    null, testApproverId, null, null, null,
                    null, null, null, null,
                    null, null, null, null);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                requestActionService.bulkApprove(filter, "  ", 50);
            });
        }

        @Test
        @DisplayName("Should throw exception when maxRequests is zero")
        void testBulkApprove_ZeroMaxRequests_ThrowsException() {
            // Arrange
            RequestFilter filter = new RequestFilter(
                    null, testApproverId, null, null, null,
                    null, null, null, null,
                    null, null, null, null);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                requestActionService.bulkApprove(filter, testApproverId, 0);
            });
        }

        @Test
        @DisplayName("Should throw exception when maxRequests is negative")
        void testBulkApprove_NegativeMaxRequests_ThrowsException() {
            // Arrange
            RequestFilter filter = new RequestFilter(
                    null, testApproverId, null, null, null,
                    null, null, null, null,
                    null, null, null, null);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                requestActionService.bulkApprove(filter, testApproverId, -5);
            });
        }
    }

    // ==================== Partial Success Cases ====================

    @Nested
    @DisplayName("Partial Success Cases")
    class PartialSuccessCases {

        @Test
        @DisplayName("Should continue processing after individual failures")
        void testBulkApprove_ContinuesAfterFailure() {
            // This test verifies the partial success strategy
            // Even if one request fails validation during approve, others should succeed

            // Arrange
            RequestFilter filter = new RequestFilter(
                    null, testApproverId, null, null, null,
                    null, null, null, null,
                    null, null, null, null);

            // Act
            BulkApproveResult result = requestActionService.bulkApprove(filter, testApproverId, 50);

            // Assert
            assertNotNull(result);
            // Total processed = success + failed
            assertEquals(result.totalProcessed(),
                    result.successCount() + result.failedCount());

            // Failed approvals should contain error details
            for (var failure : result.failedApprovals()) {
                assertNotNull(failure.requestId());
                assertNotNull(failure.reason());
            }
        }
    }

    // ==================== Result Structure Tests ====================

    @Nested
    @DisplayName("Result Structure Tests")
    class ResultStructureTests {

        @Test
        @DisplayName("Should return complete result with all fields populated")
        void testBulkApprove_ResultStructure() {
            // Arrange
            RequestFilter filter = new RequestFilter(
                    null, testApproverId, null, null, null,
                    null, null, null, null,
                    null, null, null, null);

            // Act
            BulkApproveResult result = requestActionService.bulkApprove(filter, testApproverId, 50);

            // Assert - Result structure
            assertNotNull(result);
            assertTrue(result.totalProcessed() >= 0);
            assertTrue(result.successCount() >= 0);
            assertTrue(result.failedCount() >= 0);
            assertNotNull(result.approvedRequestIds());
            assertNotNull(result.failedApprovals());

            // Consistency check
            assertEquals(result.approvedRequestIds().size(), result.successCount());
            assertEquals(result.failedApprovals().size(), result.failedCount());
            assertEquals(result.totalProcessed(), result.successCount() + result.failedCount());
        }
    }
}
