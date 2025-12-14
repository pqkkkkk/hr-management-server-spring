package org.pqkkkkk.hr_management_server.modules.request.domain.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pqkkkkk.hr_management_server.modules.profile.domain.dao.ProfileDao;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.request.domain.dao.RequestDao;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.AdditionalLeaveInfo;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.LeaveType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.ShiftType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.LeaveDate;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("LeaveRequestCommandService Integration Tests")
class LeaveRequestCommandServiceImplIntegrationTest {

        @Autowired
        private LeaveRequestCommandServiceImpl requestCommandService;

        @Autowired
        private RequestDao requestDao;

        @Autowired
        private ProfileDao profileDao;

        private String testEmployeeId;
        private String testApproverId;
        private String testPendingRequestId;
        private String testApprovedRequestId;
        private String testRejectedRequestId;

        @BeforeEach
        void setUp() {
                // Sample data is loaded from Flyway migrations
                // V3__insert_profile_sample_data.sql - Users
                // V5__insert_request_sample_data.sql - Initial requests
                // V6__add_leave_request_test_data.sql - Extended test data

                testEmployeeId = "u1a2b3c4-e5f6-7890-abcd-ef1234567890"; // From profile migration
                testApproverId = "u2b3c4d5-f6a7-8901-bcde-f12345678901"; // DEFAULT_MANAGER_ID from Constants

                testPendingRequestId = "req-leave-pending-valid";
                testApprovedRequestId = "req-leave-approved";
                testRejectedRequestId = "req-005"; // From V5 migration
        }

        // ==================== createLeaveRequest Tests ====================

        @Test
        @DisplayName("Should successfully create leave request with valid data")
        void testCreateLeaveRequest_ValidData_Success() {
                // Arrange
                LocalDate futureDate1 = LocalDate.now().plusDays(10);
                LocalDate futureDate2 = LocalDate.now().plusDays(11);
                LocalDate futureDate3 = LocalDate.now().plusDays(12);

                Request request = buildLeaveRequest(
                                testEmployeeId,
                                "Annual Leave - Test",
                                "Testing leave request creation",
                                LeaveType.ANNUAL,
                                List.of(
                                                buildLeaveDate(futureDate1, ShiftType.FULL_DAY),
                                                buildLeaveDate(futureDate2, ShiftType.FULL_DAY),
                                                buildLeaveDate(futureDate3, ShiftType.MORNING)));

                // Act
                Request createdRequest = requestCommandService.createRequest(request);

                // Assert
                assertNotNull(createdRequest);
                assertNotNull(createdRequest.getRequestId());
                assertEquals(RequestStatus.PENDING, createdRequest.getStatus());
                assertEquals(RequestType.LEAVE, createdRequest.getRequestType());
                assertEquals(testEmployeeId, createdRequest.getEmployee().getUserId());
                assertEquals(testApproverId, createdRequest.getApprover().getUserId());
                assertEquals(BigDecimal.valueOf(2.5), createdRequest.getAdditionalLeaveInfo().getTotalDays());

                // Verify persistence
                Request fetchedRequest = requestDao.getRequestById(createdRequest.getRequestId());
                assertNotNull(fetchedRequest);
                assertEquals(RequestStatus.PENDING, fetchedRequest.getStatus());
        }

        @Test
        @DisplayName("Should successfully create leave request with half days")
        void testCreateLeaveRequest_HalfDays_Success() {
                // Arrange
                LocalDate futureDate1 = LocalDate.now().plusDays(20);
                LocalDate futureDate2 = LocalDate.now().plusDays(21);

                Request request = buildLeaveRequest(
                                testEmployeeId,
                                "Annual Leave - Half Days",
                                "Morning and afternoon leave",
                                LeaveType.ANNUAL,
                                List.of(
                                                buildLeaveDate(futureDate1, ShiftType.MORNING),
                                                buildLeaveDate(futureDate2, ShiftType.AFTERNOON)));

                // Act
                Request createdRequest = requestCommandService.createRequest(request);

                // Assert
                assertNotNull(createdRequest);
                assertEquals(BigDecimal.valueOf(1.0), createdRequest.getAdditionalLeaveInfo().getTotalDays());
        }

        @Test
        @DisplayName("Should fail when title is empty")
        void testCreateLeaveRequest_EmptyTitle_ThrowsException() {
                // Arrange
                Request request = buildLeaveRequest(
                                testEmployeeId,
                                "", // Empty title
                                "Valid reason",
                                LeaveType.ANNUAL,
                                List.of(buildLeaveDate(LocalDate.now().plusDays(10), ShiftType.FULL_DAY)));

                // Act & Assert
                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> requestCommandService.createRequest(request));
                assertTrue(exception.getMessage().contains("Request title is required"));
        }

        @Test
        @DisplayName("Should fail when user reason is empty")
        void testCreateLeaveRequest_EmptyReason_ThrowsException() {
                // Arrange
                Request request = buildLeaveRequest(
                                testEmployeeId,
                                "Valid Title",
                                "", // Empty reason
                                LeaveType.ANNUAL,
                                List.of(buildLeaveDate(LocalDate.now().plusDays(10), ShiftType.FULL_DAY)));

                // Act & Assert
                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> requestCommandService.createRequest(request));
                assertTrue(exception.getMessage().contains("User reason is required"));
        }

        @Test
        @DisplayName("Should fail when leave dates are empty")
        void testCreateLeaveRequest_EmptyDates_ThrowsException() {
                // Arrange
                Request request = buildLeaveRequest(
                                testEmployeeId,
                                "Valid Title",
                                "Valid reason",
                                LeaveType.ANNUAL,
                                new ArrayList<>() // Empty dates
                );

                // Act & Assert
                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> requestCommandService.createRequest(request));
                assertTrue(exception.getMessage().contains("Leave dates cannot be empty"));
        }

        @Test
        @DisplayName("Should fail when requesting past dates")
        void testCreateLeaveRequest_PastDates_ThrowsException() {
                // Arrange
                LocalDate pastDate = LocalDate.now().minusDays(1);
                Request request = buildLeaveRequest(
                                testEmployeeId,
                                "Valid Title",
                                "Valid reason",
                                LeaveType.ANNUAL,
                                List.of(buildLeaveDate(pastDate, ShiftType.FULL_DAY)));

                // Act & Assert
                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> requestCommandService.createRequest(request));
                assertTrue(exception.getMessage().contains("Cannot request leave for past dates"));
        }

        @Test
        @DisplayName("Should fail when dates are duplicated")
        void testCreateLeaveRequest_DuplicateDates_ThrowsException() {
                // Arrange
                LocalDate futureDate = LocalDate.now().plusDays(10);
                Request request = buildLeaveRequest(
                                testEmployeeId,
                                "Valid Title",
                                "Valid reason",
                                LeaveType.ANNUAL,
                                List.of(
                                                buildLeaveDate(futureDate, ShiftType.FULL_DAY),
                                                buildLeaveDate(futureDate, ShiftType.MORNING) // Duplicate date
                                ));

                // Act & Assert
                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> requestCommandService.createRequest(request));
                assertTrue(exception.getMessage().contains("Duplicate leave date found"));
        }

        @Test
        @DisplayName("Should fail when advance notice requirement is not met")
        void testCreateLeaveRequest_InsufficientAdvanceNotice_ThrowsException() {
                // Arrange - Request for tomorrow (only 1 day notice)
                LocalDate tomorrow = LocalDate.now().plusDays(1);
                Request request = buildLeaveRequest(
                                testEmployeeId,
                                "Valid Title",
                                "Valid reason",
                                LeaveType.ANNUAL,
                                List.of(buildLeaveDate(tomorrow, ShiftType.FULL_DAY)));

                // Act & Assert
                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> requestCommandService.createRequest(request));
                assertTrue(exception.getMessage().contains("must be submitted at least"));
                assertTrue(exception.getMessage().contains("working days in advance"));
        }

        @Test
        @DisplayName("Should fail when exceeding max leave balance")
        void testCreateLeaveRequest_ExceedMaxBalance_ThrowsException() {
                // Arrange - Request for 31 days (exceeds MAX_LEAVE_BALANCE = 30)
                List<LeaveDate> dates = new ArrayList<>();
                for (int i = 10; i < 41; i++) {
                        dates.add(buildLeaveDate(LocalDate.now().plusDays(i), ShiftType.FULL_DAY));
                }

                Request request = buildLeaveRequest(
                                testEmployeeId,
                                "Long Leave",
                                "Very long vacation",
                                LeaveType.ANNUAL,
                                dates);

                // Act & Assert
                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> requestCommandService.createRequest(request));
                assertTrue(exception.getMessage().contains("cannot exceed"));
                assertTrue(exception.getMessage().contains("days"));
        }

        // ==================== approveRequest Tests ====================

        @Test
        @DisplayName("Should fail to approve non-existent request")
        void testApproveRequest_NonExistentRequest_ThrowsException() {
                // Arrange
                String nonExistentId = "non-existent-id";

                // Act & Assert
                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> requestCommandService.approveRequest(nonExistentId, testApproverId));
                assertTrue(exception.getMessage().contains("Request does not exist"));
        }

        @Test
        @DisplayName("Should fail to approve already approved request")
        void testApproveRequest_AlreadyApproved_ThrowsException() {
                // Arrange
                Request request = requestDao.getRequestById(testApprovedRequestId);
                assertNotNull(request);
                assertEquals(RequestStatus.APPROVED, request.getStatus());

                // Act & Assert
                IllegalStateException exception = assertThrows(
                                IllegalStateException.class,
                                () -> requestCommandService.approveRequest(testApprovedRequestId,
                                                testApproverId));
                assertTrue(exception.getMessage().contains("not in a valid state"));
        }

        @Test
        @DisplayName("Should fail to approve already rejected request")
        void testApproveRequest_AlreadyRejected_ThrowsException() {
                // Arrange
                Request request = requestDao.getRequestById(testRejectedRequestId);
                assertNotNull(request);
                assertEquals(RequestStatus.REJECTED, request.getStatus());

                // Act & Assert
                IllegalStateException exception = assertThrows(
                                IllegalStateException.class,
                                () -> requestCommandService.approveRequest(testRejectedRequestId,
                                                testApproverId));
                assertTrue(exception.getMessage().contains("not in a valid state"));
        }

        @Test
        @DisplayName("Should fail when approver does not have permission")
        void testApproveRequest_UnauthorizedApprover_ThrowsException() {
                // Arrange
                String unauthorizedApproverId = "unauthorized-user-id";

                // Act & Assert
                SecurityException exception = assertThrows(
                                SecurityException.class,
                                () -> requestCommandService.approveRequest(testPendingRequestId,
                                                unauthorizedApproverId));
                assertTrue(exception.getMessage().contains("does not have permission"));
        }

        // ==================== rejectRequest Tests ====================

        @Test
        @DisplayName("Should successfully reject pending request with valid reason")
        void testRejectRequest_ValidPendingRequest_Success() {
                // Arrange
                String rejectionReason = "Insufficient staffing during requested period";
                Request requestBefore = requestDao.getRequestById(testPendingRequestId);
                assertNotNull(requestBefore);
                assertEquals(RequestStatus.PENDING, requestBefore.getStatus());

                // Act
                Request rejectedRequest = requestCommandService.rejectRequest(
                                testPendingRequestId,
                                testApproverId,
                                rejectionReason);

                // Assert
                assertNotNull(rejectedRequest);
                assertEquals(RequestStatus.REJECTED, rejectedRequest.getStatus());
                assertEquals(rejectionReason, rejectedRequest.getRejectReason());
                assertNotNull(rejectedRequest.getProcessedAt());

                // Verify persistence
                Request fetchedRequest = requestDao.getRequestById(testPendingRequestId);
                assertEquals(RequestStatus.REJECTED, fetchedRequest.getStatus());
                assertEquals(rejectionReason, fetchedRequest.getRejectReason());
        }

        @Test
        @DisplayName("Should fail to reject when rejection reason is empty")
        void testRejectRequest_EmptyReason_ThrowsException() {
                // Act & Assert
                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> requestCommandService.rejectRequest(
                                                testPendingRequestId,
                                                testApproverId,
                                                "" // Empty reason
                                ));
                assertTrue(exception.getMessage().contains("Rejection reason is required"));
        }

        @Test
        @DisplayName("Should fail to reject when rejection reason is null")
        void testRejectRequest_NullReason_ThrowsException() {
                // Act & Assert
                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> requestCommandService.rejectRequest(
                                                testPendingRequestId,
                                                testApproverId,
                                                null // Null reason
                                ));
                assertTrue(exception.getMessage().contains("Rejection reason is required"));
        }

        @Test
        @DisplayName("Should fail to reject non-existent request")
        void testRejectRequest_NonExistentRequest_ThrowsException() {
                // Arrange
                String nonExistentId = "non-existent-id";

                // Act & Assert
                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> requestCommandService.rejectRequest(
                                                nonExistentId,
                                                testApproverId,
                                                "Valid reason"));
                assertTrue(exception.getMessage().contains("Request does not exist"));
        }

        @Test
        @DisplayName("Should fail to reject already approved request")
        void testRejectRequest_AlreadyApproved_ThrowsException() {
                // Arrange
                Request request = requestDao.getRequestById(testApprovedRequestId);
                assertNotNull(request);
                assertEquals(RequestStatus.APPROVED, request.getStatus());

                // Act & Assert
                IllegalStateException exception = assertThrows(
                                IllegalStateException.class,
                                () -> requestCommandService.rejectRequest(
                                                testApprovedRequestId,
                                                testApproverId,
                                                "Valid reason"));
                assertTrue(exception.getMessage().contains("not in a valid state"));
        }

        @Test
        @DisplayName("Should fail to reject already rejected request")
        void testRejectRequest_AlreadyRejected_ThrowsException() {
                // Arrange
                Request request = requestDao.getRequestById(testRejectedRequestId);
                assertNotNull(request);
                assertEquals(RequestStatus.REJECTED, request.getStatus());

                // Act & Assert
                IllegalStateException exception = assertThrows(
                                IllegalStateException.class,
                                () -> requestCommandService.rejectRequest(
                                                testRejectedRequestId,
                                                testApproverId,
                                                "Valid reason"));
                assertTrue(exception.getMessage().contains("not in a valid state"));
        }

        @Test
        @DisplayName("Should fail when rejecter does not have permission")
        void testRejectRequest_UnauthorizedRejecter_ThrowsException() {
                // Arrange
                String unauthorizedApproverId = "unauthorized-user-id";

                // Act & Assert
                SecurityException exception = assertThrows(
                                SecurityException.class,
                                () -> requestCommandService.rejectRequest(
                                                testPendingRequestId,
                                                unauthorizedApproverId,
                                                "Valid reason"));
                assertTrue(exception.getMessage().contains("does not have permission"));
        }

        // ==================== Leave Balance Integration Tests ====================

        @Test
        @DisplayName("Should correctly deduct full day leave balance")
        void testApproveRequest_DeductFullDayBalance_Success() {
                // Arrange - Create request with 3 full days
                LocalDate futureDate1 = LocalDate.now().plusDays(10);
                LocalDate futureDate2 = LocalDate.now().plusDays(11);
                LocalDate futureDate3 = LocalDate.now().plusDays(12);

                Request request = buildLeaveRequest(
                                testEmployeeId,
                                "Test Full Days",
                                "Testing full day deduction",
                                LeaveType.ANNUAL,
                                List.of(
                                                buildLeaveDate(futureDate1, ShiftType.FULL_DAY),
                                                buildLeaveDate(futureDate2, ShiftType.FULL_DAY),
                                                buildLeaveDate(futureDate3, ShiftType.FULL_DAY)));

                Request createdRequest = requestCommandService.createRequest(request);

                User employeeBefore = profileDao.getProfileById(testEmployeeId);
                BigDecimal balanceBefore = employeeBefore.getRemainingAnnualLeave();

                // Act
                Request approvedRequest = requestCommandService.approveRequest(
                                createdRequest.getRequestId(),
                                testApproverId);

                // Assert
                User employeeAfter = profileDao.getProfileById(testEmployeeId);
                BigDecimal expectedBalance = balanceBefore.subtract(BigDecimal.valueOf(3.0));
                assertEquals(expectedBalance, employeeAfter.getRemainingAnnualLeave());
        }

        @Test
        @DisplayName("Should correctly deduct half day leave balance")
        void testApproveRequest_DeductHalfDayBalance_Success() {
                // Arrange - Create request with 2 half days (1 morning, 1 afternoon)
                LocalDate futureDate1 = LocalDate.now().plusDays(20);
                LocalDate futureDate2 = LocalDate.now().plusDays(21);

                Request request = buildLeaveRequest(
                                testEmployeeId,
                                "Test Half Days",
                                "Testing half day deduction",
                                LeaveType.ANNUAL,
                                List.of(
                                                buildLeaveDate(futureDate1, ShiftType.MORNING),
                                                buildLeaveDate(futureDate2, ShiftType.AFTERNOON)));

                Request createdRequest = requestCommandService.createRequest(request);

                User employeeBefore = profileDao.getProfileById(testEmployeeId);
                BigDecimal balanceBefore = employeeBefore.getRemainingAnnualLeave();

                // Act
                Request approvedRequest = requestCommandService.approveRequest(
                                createdRequest.getRequestId(),
                                testApproverId);

                // Assert
                User employeeAfter = profileDao.getProfileById(testEmployeeId);
                BigDecimal expectedBalance = balanceBefore.subtract(BigDecimal.valueOf(1.0));
                assertEquals(expectedBalance, employeeAfter.getRemainingAnnualLeave());
        }

        @Test
        @DisplayName("Should correctly deduct mixed day leave balance")
        void testApproveRequest_DeductMixedDayBalance_Success() {
                // Arrange - Create request with 2 full days + 1 morning (2.5 days total)
                LocalDate futureDate1 = LocalDate.now().plusDays(30);
                LocalDate futureDate2 = LocalDate.now().plusDays(31);
                LocalDate futureDate3 = LocalDate.now().plusDays(32);

                Request request = buildLeaveRequest(
                                testEmployeeId,
                                "Test Mixed Days",
                                "Testing mixed day deduction",
                                LeaveType.ANNUAL,
                                List.of(
                                                buildLeaveDate(futureDate1, ShiftType.FULL_DAY),
                                                buildLeaveDate(futureDate2, ShiftType.FULL_DAY),
                                                buildLeaveDate(futureDate3, ShiftType.MORNING)));

                Request createdRequest = requestCommandService.createRequest(request);

                User employeeBefore = profileDao.getProfileById(testEmployeeId);
                BigDecimal balanceBefore = employeeBefore.getRemainingAnnualLeave();

                // Act
                Request approvedRequest = requestCommandService.approveRequest(
                                createdRequest.getRequestId(),
                                testApproverId);

                // Assert
                User employeeAfter = profileDao.getProfileById(testEmployeeId);
                BigDecimal expectedBalance = balanceBefore.subtract(BigDecimal.valueOf(2.5));
                assertEquals(expectedBalance, employeeAfter.getRemainingAnnualLeave());
        }

        @Test
        @DisplayName("Should not deduct balance when request is rejected")
        void testRejectRequest_ShouldNotDeductBalance_Success() {
                // Arrange - Create and approve first request to have baseline
                LocalDate futureDate = LocalDate.now().plusDays(15);

                Request request = buildLeaveRequest(
                                testEmployeeId,
                                "Request to be rejected",
                                "Testing rejection",
                                LeaveType.ANNUAL,
                                List.of(buildLeaveDate(futureDate, ShiftType.FULL_DAY)));

                Request createdRequest = requestCommandService.createRequest(request);

                User employeeBefore = profileDao.getProfileById(testEmployeeId);
                BigDecimal balanceBefore = employeeBefore.getRemainingAnnualLeave();

                // Act
                Request rejectedRequest = requestCommandService.rejectRequest(
                                createdRequest.getRequestId(),
                                testApproverId,
                                "Not enough staff coverage");

                // Assert
                User employeeAfter = profileDao.getProfileById(testEmployeeId);
                assertEquals(balanceBefore, employeeAfter.getRemainingAnnualLeave(),
                                "Leave balance should NOT be deducted when request is rejected");
        }

        @Test
        @DisplayName("Should handle multiple approvals correctly")
        void testApproveRequest_MultipleApprovals_BalanceDeductedCorrectly() {
                // Arrange - Create 2 separate requests
                Request request1 = buildLeaveRequest(
                                testEmployeeId,
                                "First Request",
                                "First leave",
                                LeaveType.ANNUAL,
                                List.of(buildLeaveDate(LocalDate.now().plusDays(40), ShiftType.FULL_DAY)));

                Request request2 = buildLeaveRequest(
                                testEmployeeId,
                                "Second Request",
                                "Second leave",
                                LeaveType.ANNUAL,
                                List.of(buildLeaveDate(LocalDate.now().plusDays(50), ShiftType.FULL_DAY)));

                Request createdRequest1 = requestCommandService.createRequest(request1);
                Request createdRequest2 = requestCommandService.createRequest(request2);

                User employeeBefore = profileDao.getProfileById(testEmployeeId);
                BigDecimal balanceBefore = employeeBefore.getRemainingAnnualLeave();

                // Act - Approve both
                requestCommandService.approveRequest(createdRequest1.getRequestId(), testApproverId);
                requestCommandService.approveRequest(createdRequest2.getRequestId(), testApproverId);

                // Assert
                User employeeAfter = profileDao.getProfileById(testEmployeeId);
                BigDecimal expectedBalance = balanceBefore.subtract(BigDecimal.valueOf(2.0));
                assertEquals(expectedBalance, employeeAfter.getRemainingAnnualLeave(),
                                "Both requests should deduct 1 day each");
        }

        // ==================== Helper Methods ====================

        private Request buildLeaveRequest(String employeeId, String title, String reason,
                        LeaveType leaveType, List<LeaveDate> leaveDates) {
                // Build Request first
                Request request = Request.builder()
                                .requestType(RequestType.LEAVE)
                                .title(title)
                                .userReason(reason)
                                .employee(User.builder().userId(employeeId).build())
                                .build();

                // Build AdditionalLeaveInfo
                AdditionalLeaveInfo leaveInfo = AdditionalLeaveInfo.builder()
                                .leaveType(leaveType)
                                .leaveDates(leaveDates)
                                .build();

                // Set bidirectional relationships
                // 1. Request <-> AdditionalLeaveInfo (CRITICAL for @MapsId)
                request.setAdditionalLeaveInfo(leaveInfo);
                leaveInfo.setRequest(request);

                // 2. AdditionalLeaveInfo <-> LeaveDates
                for (LeaveDate leaveDate : leaveDates) {
                        leaveDate.setAdditionalLeaveInfo(leaveInfo);
                }

                return request;
        }

        private LeaveDate buildLeaveDate(LocalDate date, ShiftType shift) {
                return LeaveDate.builder()
                                .date(date)
                                .shift(shift)
                                .build();
        }
}
