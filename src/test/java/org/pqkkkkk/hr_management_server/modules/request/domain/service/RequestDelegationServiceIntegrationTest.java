package org.pqkkkkk.hr_management_server.modules.request.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserRole;
import org.pqkkkkk.hr_management_server.modules.request.domain.dao.RequestDao;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("RequestDelegationService Integration Tests")
class RequestDelegationServiceIntegrationTest {

    @Autowired
    private RequestDelegationService delegationService;

    @Autowired
    private RequestDao requestDao;

    private String testPendingRequestId;
    private String testApprovedRequestId;
    private String hrUserId;
    private String adminUserId;
    private String employeeUserId;
    private String managerUserId;

    @BeforeEach
    void setUp() {
        // Sample data is loaded from V15__add_delegation_test_data.sql migration
        testPendingRequestId = "req-delegation-pending"; // PENDING request for delegation
        testApprovedRequestId = "req-delegation-approved"; // APPROVED request (cannot delegate)
        
        // User IDs from V3__insert_profile_sample_data.sql (actual IDs from migration)
        hrUserId = "u3c4d5e6-a7b8-9012-cdef-123456789012"; // HR role - Le Van Cuong
        adminUserId = "u7a8b9c0-e1f2-3456-0123-567890123456"; // ADMIN role - Bui Van Giang
        employeeUserId = "u1a2b3c4-e5f6-7890-abcd-ef1234567890"; // EMPLOYEE role - Nguyen Van An
        managerUserId = "u2b3c4d5-f6a7-8901-bcde-f12345678901"; // MANAGER role - Tran Thi Binh
    }

    // ==================== Successful Delegation Tests ====================

    @Test
    @DisplayName("Should successfully delegate pending request to HR user")
    void testDelegateRequest_ToHR_Success() {
        // Act
        Request delegatedRequest = delegationService.delegateRequest(testPendingRequestId, hrUserId);

        // Assert
        assertNotNull(delegatedRequest);
        assertEquals(testPendingRequestId, delegatedRequest.getRequestId());
        assertEquals(hrUserId, delegatedRequest.getProcessor().getUserId());
        assertEquals(UserRole.HR, delegatedRequest.getProcessor().getRole());
        assertEquals(RequestStatus.PENDING, delegatedRequest.getStatus());
        assertNotNull(delegatedRequest.getUpdatedAt());

        // Verify persistence via dirty checking
        Request fetchedRequest = requestDao.getRequestById(testPendingRequestId);
        assertEquals(hrUserId, fetchedRequest.getProcessor().getUserId());
    }

    @Test
    @DisplayName("Should successfully delegate pending request to ADMIN user")
    void testDelegateRequest_ToAdmin_Success() {
        // Act
        Request delegatedRequest = delegationService.delegateRequest(testPendingRequestId, adminUserId);

        // Assert
        assertNotNull(delegatedRequest);
        assertEquals(testPendingRequestId, delegatedRequest.getRequestId());
        assertEquals(adminUserId, delegatedRequest.getProcessor().getUserId());
        assertEquals(UserRole.ADMIN, delegatedRequest.getProcessor().getRole());
        assertEquals(RequestStatus.PENDING, delegatedRequest.getStatus());
    }

    // ==================== Validation Failure Tests ====================

    @Test
    @DisplayName("Should throw exception when request status is not PENDING")
    void testDelegateRequest_NotPendingStatus_ThrowsException() {
        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> delegationService.delegateRequest(testApprovedRequestId, hrUserId)
        );

        assertTrue(exception.getMessage().contains("Only pending requests can be delegated"));
        assertTrue(exception.getMessage().contains("APPROVED"));
    }

    @Test
    @DisplayName("Should throw exception when processor role is EMPLOYEE")
    void testDelegateRequest_EmployeeRole_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> delegationService.delegateRequest(testPendingRequestId, employeeUserId)
        );

        assertTrue(exception.getMessage().contains("Processor must have ADMIN or HR role"));
        assertTrue(exception.getMessage().contains("EMPLOYEE"));
    }

    @Test
    @DisplayName("Should throw exception when processor role is MANAGER")
    void testDelegateRequest_ManagerRole_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> delegationService.delegateRequest(testPendingRequestId, managerUserId)
        );

        assertTrue(exception.getMessage().contains("Processor must have ADMIN or HR role"));
        assertTrue(exception.getMessage().contains("MANAGER"));
    }
}
