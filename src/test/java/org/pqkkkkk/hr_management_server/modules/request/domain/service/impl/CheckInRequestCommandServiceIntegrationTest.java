package org.pqkkkkk.hr_management_server.modules.request.domain.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.request.domain.dao.RequestDao;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.AdditionalCheckInInfo;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("CheckInRequestCommandService Integration Tests")
class CheckInRequestCommandServiceIntegrationTest {

    @Autowired
    private CheckInRequestCommandService checkInRequestCommandService;

    @Autowired
    private RequestDao requestDao;

    private String testEmployeeId;

    @BeforeEach
    void setUp() {
        // Sample data is loaded from Flyway migrations
        // V3__insert_profile_sample_data.sql - Users
        // V5__insert_request_sample_data.sql - Initial requests
        testEmployeeId = "u1a2b3c4-e5f6-7890-abcd-ef1234567890"; // Employee from profile migration
    }

    // ==================== Success Cases ====================

    @Test
    @DisplayName("Should successfully create check-in request before deadline (8:00 AM) without reason")
    void testCreateCheckInRequest_BeforeDeadline_Success() {
        // Arrange
        LocalDateTime checkInTime = LocalDateTime.now().with(LocalTime.of(7, 30)); // 7:30 AM
        Request request = buildCheckInRequest(testEmployeeId, checkInTime, null);

        // Act
        Request result = checkInRequestCommandService.createRequest(request);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getRequestId());
        assertNotNull(result.getApprover());
        assertNotNull(result.getProcessor());
        assertEquals(RequestType.CHECK_IN, result.getRequestType());
        assertEquals(RequestStatus.PENDING, result.getStatus());
        assertEquals(testEmployeeId, result.getEmployee().getUserId());
        assertNotNull(result.getAdditionalCheckInInfo());
        assertEquals(checkInTime, result.getAdditionalCheckInInfo().getDesiredCheckInTime());
        
        // Verify bidirectional relationship
        assertNotNull(result.getAdditionalCheckInInfo().getRequest());
        assertEquals(result.getRequestId(), result.getAdditionalCheckInInfo().getRequest().getRequestId());
    }

    @Test
    @DisplayName("Should successfully create check-in request after deadline (8:00 AM) with reason")
    void testCreateCheckInRequest_AfterDeadlineWithReason_Success() {
        // Arrange
        LocalDateTime checkInTime = LocalDateTime.now().with(LocalTime.of(9, 15)); // 9:15 AM
        String reason = "Traffic jam due to accident on highway";
        Request request = buildCheckInRequest(testEmployeeId, checkInTime, reason);

        // Act
        Request result = checkInRequestCommandService.createRequest(request);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getRequestId());
        assertNotNull(result.getApprover());
        assertNotNull(result.getProcessor());
        assertEquals(RequestType.CHECK_IN, result.getRequestType());
        assertEquals(RequestStatus.PENDING, result.getStatus());
        assertEquals(reason, result.getUserReason());
        assertEquals(testEmployeeId, result.getEmployee().getUserId());
        assertNotNull(result.getAdditionalCheckInInfo());
        assertEquals(checkInTime, result.getAdditionalCheckInInfo().getDesiredCheckInTime());
    }

    @Test
    @DisplayName("Should successfully create check-in request exactly at deadline (8:00 AM) without reason")
    void testCreateCheckInRequest_ExactlyAtDeadline_Success() {
        // Arrange
        LocalDateTime checkInTime = LocalDateTime.now().with(LocalTime.of(8, 0)); // Exactly 8:00 AM
        Request request = buildCheckInRequest(testEmployeeId, checkInTime, null);

        // Act
        Request result = checkInRequestCommandService.createRequest(request);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getProcessor());
        assertNotNull(result.getApprover());
        assertEquals(RequestType.CHECK_IN, result.getRequestType());
        assertEquals(RequestStatus.PENDING, result.getStatus());
        assertNotNull(result.getAdditionalCheckInInfo());
    }

    // ==================== Validation Error Cases ====================

    @Test
    @DisplayName("Should throw exception when request is null")
    void testCreateCheckInRequest_NullRequest_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> checkInRequestCommandService.createRequest(null)
        );
        
        assertEquals("Request cannot be null.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when employee is null")
    void testCreateCheckInRequest_NullEmployee_ThrowsException() {
        // Arrange
        LocalDateTime checkInTime = LocalDateTime.now().with(LocalTime.of(7, 30));
        Request request = buildCheckInRequest(null, checkInTime, null);
        request.setEmployee(null); // Explicitly set to null

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> checkInRequestCommandService.createRequest(request)
        );
        
        assertTrue(exception.getMessage().contains("Employee information is required"));
    }

    @Test
    @DisplayName("Should throw exception when employee ID is null")
    void testCreateCheckInRequest_NullEmployeeId_ThrowsException() {
        // Arrange
        LocalDateTime checkInTime = LocalDateTime.now().with(LocalTime.of(7, 30));
        Request request = buildCheckInRequest(null, checkInTime, null);
        request.setEmployee(User.builder().userId(null).build()); // Employee with null ID

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> checkInRequestCommandService.createRequest(request)
        );
        
        assertTrue(exception.getMessage().contains("Employee information is required"));
    }

    @Test
    @DisplayName("Should throw exception when additionalCheckInInfo is null")
    void testCreateCheckInRequest_NullAdditionalCheckInInfo_ThrowsException() {
        // Arrange
        LocalDateTime checkInTime = LocalDateTime.now().with(LocalTime.of(7, 30));
        Request request = buildCheckInRequest(testEmployeeId, checkInTime, null);
        request.setAdditionalCheckInInfo(null); // Explicitly set to null

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> checkInRequestCommandService.createRequest(request)
        );
        
        assertEquals("Additional check-in information is required for check-in request creation.", 
                     exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when desiredCheckInTime is null")
    void testCreateCheckInRequest_NullDesiredCheckInTime_ThrowsException() {
        // Arrange
        Request request = buildCheckInRequest(testEmployeeId, null, null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> checkInRequestCommandService.createRequest(request)
        );
        
        assertEquals("Desired check-in time is required.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when check-in is after deadline without reason")
    void testCreateCheckInRequest_AfterDeadlineWithoutReason_ThrowsException() {
        // Arrange - Multiple test cases for different times after 8:00 AM
        LocalDateTime[] lateTimes = {
            LocalDateTime.now().with(LocalTime.of(8, 1)),  // 8:01 AM
            LocalDateTime.now().with(LocalTime.of(9, 0)),  // 9:00 AM
            LocalDateTime.now().with(LocalTime.of(10, 30)) // 10:30 AM
        };

        for (LocalDateTime lateTime : lateTimes) {
            Request request = buildCheckInRequest(testEmployeeId, lateTime, null); // No reason
            
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> checkInRequestCommandService.createRequest(request),
                "Should throw exception for check-in at " + lateTime.toLocalTime()
            );
            
            assertTrue(exception.getMessage().contains("reason must be provided"));
            assertTrue(exception.getMessage().contains("08:00"));
        }
    }

    // ==================== Business Logic Verification Cases ====================

    @Test
    @DisplayName("Should always set status to PENDING when creating request")
    void testCreateCheckInRequest_StatusSetToPending() {
        // Arrange
        LocalDateTime checkInTime = LocalDateTime.now().with(LocalTime.of(7, 30));
        Request request = buildCheckInRequest(testEmployeeId, checkInTime, null);
        
        // Try to set a different status (should be overridden)
        request.setStatus(RequestStatus.APPROVED);

        // Act
        Request result = checkInRequestCommandService.createRequest(request);

        // Assert
        assertEquals(RequestStatus.PENDING, result.getStatus(), 
                    "Status should always be set to PENDING regardless of input");
    }

    @Test
    @DisplayName("Should always set requestType to CHECK_IN when creating request")
    void testCreateCheckInRequest_TypeSetToCheckIn() {
        // Arrange
        LocalDateTime checkInTime = LocalDateTime.now().with(LocalTime.of(7, 30));
        Request request = buildCheckInRequest(testEmployeeId, checkInTime, null);
        
        // Try to set a different type (should be overridden)
        request.setRequestType(RequestType.CHECK_OUT);

        // Act
        Request result = checkInRequestCommandService.createRequest(request);

        // Assert
        assertEquals(RequestType.CHECK_IN, result.getRequestType(), 
                    "RequestType should always be set to CHECK_IN regardless of input");
    }

    @Test
    @DisplayName("Should establish bidirectional relationship between request and additionalCheckInInfo")
    void testCreateCheckInRequest_BidirectionalRelationshipEstablished() {
        // Arrange
        LocalDateTime checkInTime = LocalDateTime.now().with(LocalTime.of(7, 30));
        Request request = buildCheckInRequest(testEmployeeId, checkInTime, null);

        // Act
        Request result = checkInRequestCommandService.createRequest(request);

        // Assert - Verify bidirectional relationship
        assertNotNull(result.getAdditionalCheckInInfo(), 
                     "AdditionalCheckInInfo should not be null");
        assertNotNull(result.getAdditionalCheckInInfo().getRequest(), 
                     "Back reference from AdditionalCheckInInfo to Request should be set");
        assertEquals(result.getRequestId(), 
                    result.getAdditionalCheckInInfo().getRequest().getRequestId(),
                    "Request IDs should match in both directions");
        assertEquals(result.getRequestId(), 
                    result.getAdditionalCheckInInfo().getRequestId(),
                    "AdditionalCheckInInfo requestId should match Request requestId");
        
        // Verify relationship integrity by fetching from database
        Request fetchedRequest = requestDao.getRequestById(result.getRequestId());
        assertNotNull(fetchedRequest.getAdditionalCheckInInfo());
        assertEquals(fetchedRequest.getRequestId(), 
                    fetchedRequest.getAdditionalCheckInInfo().getRequestId());
    }

    // ==================== Helper Methods ====================

    /**
     * Helper method to build a check-in request for testing
     */
    private Request buildCheckInRequest(String employeeId, LocalDateTime desiredCheckInTime, String userReason) {
        LocalDateTime currentCheckInTime = desiredCheckInTime != null 
            ? desiredCheckInTime.plusMinutes(30) 
            : LocalDateTime.now();

        User employee = employeeId != null 
            ? User.builder().userId(employeeId).build() 
            : null;

        AdditionalCheckInInfo checkInInfo = AdditionalCheckInInfo.builder()
            .desiredCheckInTime(desiredCheckInTime)
            .currentCheckInTime(currentCheckInTime)
            .build();

        return Request.builder()
            .title("Check-in Request Test")
            .userReason(userReason)
            .employee(employee)
            .additionalCheckInInfo(checkInInfo)
            .build();
    }
}
