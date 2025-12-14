package org.pqkkkkk.hr_management_server.modules.request.domain.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.request.domain.dao.RequestDao;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.AdditionalCheckOutInfo;
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
@DisplayName("CheckOutRequestCommandService Integration Tests")
class CheckOutRequestCommandServiceIntegrationTest {

    @Autowired
    private CheckOutRequestCommandService checkOutRequestCommandService;

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
    @DisplayName("Should successfully create check-out request after deadline (17:00) without reason")
    void testCreateCheckOutRequest_AfterDeadline_Success() {
        // Arrange
        LocalDateTime checkOutTime = LocalDateTime.now().with(LocalTime.of(17, 30)); // 5:30 PM
        Request request = buildCheckOutRequest(testEmployeeId, checkOutTime, null);

        // Act
        Request result = checkOutRequestCommandService.createRequest(request);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getRequestId());
        assertEquals(RequestType.CHECK_OUT, result.getRequestType());
        assertEquals(RequestStatus.PENDING, result.getStatus());
        assertEquals(testEmployeeId, result.getEmployee().getUserId());
        assertNotNull(result.getAdditionalCheckOutInfo());
        assertEquals(checkOutTime, result.getAdditionalCheckOutInfo().getDesiredCheckOutTime());
        
        // Verify bidirectional relationship
        assertNotNull(result.getAdditionalCheckOutInfo().getRequest());
        assertEquals(result.getRequestId(), result.getAdditionalCheckOutInfo().getRequest().getRequestId());
    }

    @Test
    @DisplayName("Should successfully create check-out request before deadline (17:00) with reason")
    void testCreateCheckOutRequest_BeforeDeadlineWithReason_Success() {
        // Arrange
        LocalDateTime checkOutTime = LocalDateTime.now().with(LocalTime.of(15, 0)); // 3:00 PM
        String reason = "Medical appointment at 4:00 PM";
        Request request = buildCheckOutRequest(testEmployeeId, checkOutTime, reason);

        // Act
        Request result = checkOutRequestCommandService.createRequest(request);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getRequestId());
        assertEquals(RequestType.CHECK_OUT, result.getRequestType());
        assertEquals(RequestStatus.PENDING, result.getStatus());
        assertEquals(reason, result.getUserReason());
        assertEquals(testEmployeeId, result.getEmployee().getUserId());
        assertNotNull(result.getAdditionalCheckOutInfo());
        assertEquals(checkOutTime, result.getAdditionalCheckOutInfo().getDesiredCheckOutTime());
    }

    @Test
    @DisplayName("Should successfully create check-out request exactly at deadline (17:00) without reason")
    void testCreateCheckOutRequest_ExactlyAtDeadline_Success() {
        // Arrange
        LocalDateTime checkOutTime = LocalDateTime.now().with(LocalTime.of(17, 0)); // Exactly 5:00 PM
        Request request = buildCheckOutRequest(testEmployeeId, checkOutTime, null);

        // Act
        Request result = checkOutRequestCommandService.createRequest(request);

        // Assert
        assertNotNull(result);
        assertEquals(RequestType.CHECK_OUT, result.getRequestType());
        assertEquals(RequestStatus.PENDING, result.getStatus());
        assertNotNull(result.getAdditionalCheckOutInfo());
    }

    // ==================== Validation Error Cases ====================

    @Test
    @DisplayName("Should throw exception when request is null")
    void testCreateCheckOutRequest_NullRequest_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> checkOutRequestCommandService.createRequest(null)
        );
        
        assertEquals("Request cannot be null.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when employee is null")
    void testCreateCheckOutRequest_NullEmployee_ThrowsException() {
        // Arrange
        LocalDateTime checkOutTime = LocalDateTime.now().with(LocalTime.of(17, 30));
        Request request = buildCheckOutRequest(null, checkOutTime, null);
        request.setEmployee(null); // Explicitly set to null

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> checkOutRequestCommandService.createRequest(request)
        );
        
        assertTrue(exception.getMessage().contains("Employee information is required"));
    }

    @Test
    @DisplayName("Should throw exception when employee ID is null")
    void testCreateCheckOutRequest_NullEmployeeId_ThrowsException() {
        // Arrange
        LocalDateTime checkOutTime = LocalDateTime.now().with(LocalTime.of(17, 30));
        Request request = buildCheckOutRequest(null, checkOutTime, null);
        request.setEmployee(User.builder().userId(null).build()); // Employee with null ID

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> checkOutRequestCommandService.createRequest(request)
        );
        
        assertTrue(exception.getMessage().contains("Employee information is required"));
    }

    @Test
    @DisplayName("Should throw exception when additionalCheckOutInfo is null")
    void testCreateCheckOutRequest_NullAdditionalCheckOutInfo_ThrowsException() {
        // Arrange
        LocalDateTime checkOutTime = LocalDateTime.now().with(LocalTime.of(17, 30));
        Request request = buildCheckOutRequest(testEmployeeId, checkOutTime, null);
        request.setAdditionalCheckOutInfo(null); // Explicitly set to null

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> checkOutRequestCommandService.createRequest(request)
        );
        
        assertEquals("Additional check-out information is required for check-out request creation.", 
                     exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when desiredCheckOutTime is null")
    void testCreateCheckOutRequest_NullDesiredCheckOutTime_ThrowsException() {
        // Arrange
        Request request = buildCheckOutRequest(testEmployeeId, null, null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> checkOutRequestCommandService.createRequest(request)
        );
        
        assertEquals("Desired check-out time is required.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when check-out is before deadline without reason")
    void testCreateCheckOutRequest_BeforeDeadlineWithoutReason_ThrowsException() {
        // Arrange - Multiple test cases for different times before 17:00
        LocalDateTime[] earlyTimes = {
            LocalDateTime.now().with(LocalTime.of(14, 0)),  // 2:00 PM
            LocalDateTime.now().with(LocalTime.of(15, 30)), // 3:30 PM
            LocalDateTime.now().with(LocalTime.of(16, 59))  // 4:59 PM
        };

        for (LocalDateTime earlyTime : earlyTimes) {
            Request request = buildCheckOutRequest(testEmployeeId, earlyTime, null); // No reason
            
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> checkOutRequestCommandService.createRequest(request),
                "Should throw exception for check-out at " + earlyTime.toLocalTime()
            );
            
            assertTrue(exception.getMessage().contains("reason must be provided"));
            assertTrue(exception.getMessage().contains("17:00"));
        }
    }

    // ==================== Business Logic Verification Cases ====================

    @Test
    @DisplayName("Should always set status to PENDING when creating request")
    void testCreateCheckOutRequest_StatusSetToPending() {
        // Arrange
        LocalDateTime checkOutTime = LocalDateTime.now().with(LocalTime.of(17, 30));
        Request request = buildCheckOutRequest(testEmployeeId, checkOutTime, null);
        
        // Try to set a different status (should be overridden)
        request.setStatus(RequestStatus.APPROVED);

        // Act
        Request result = checkOutRequestCommandService.createRequest(request);

        // Assert
        assertEquals(RequestStatus.PENDING, result.getStatus(), 
                    "Status should always be set to PENDING regardless of input");
    }

    @Test
    @DisplayName("Should always set requestType to CHECK_OUT when creating request")
    void testCreateCheckOutRequest_TypeSetToCheckOut() {
        // Arrange
        LocalDateTime checkOutTime = LocalDateTime.now().with(LocalTime.of(17, 30));
        Request request = buildCheckOutRequest(testEmployeeId, checkOutTime, null);
        
        // Try to set a different type (should be overridden)
        request.setRequestType(RequestType.CHECK_IN);

        // Act
        Request result = checkOutRequestCommandService.createRequest(request);

        // Assert
        assertEquals(RequestType.CHECK_OUT, result.getRequestType(), 
                    "RequestType should always be set to CHECK_OUT regardless of input");
    }

    @Test
    @DisplayName("Should establish bidirectional relationship between request and additionalCheckOutInfo")
    void testCreateCheckOutRequest_BidirectionalRelationshipEstablished() {
        // Arrange
        LocalDateTime checkOutTime = LocalDateTime.now().with(LocalTime.of(17, 30));
        Request request = buildCheckOutRequest(testEmployeeId, checkOutTime, null);

        // Act
        Request result = checkOutRequestCommandService.createRequest(request);

        // Assert - Verify bidirectional relationship
        assertNotNull(result.getAdditionalCheckOutInfo(), 
                     "AdditionalCheckOutInfo should not be null");
        assertNotNull(result.getAdditionalCheckOutInfo().getRequest(), 
                     "Back reference from AdditionalCheckOutInfo to Request should be set");
        assertEquals(result.getRequestId(), 
                    result.getAdditionalCheckOutInfo().getRequest().getRequestId(),
                    "Request IDs should match in both directions");
        assertEquals(result.getRequestId(), 
                    result.getAdditionalCheckOutInfo().getRequestId(),
                    "AdditionalCheckOutInfo requestId should match Request requestId");
        
        // Verify relationship integrity by fetching from database
        Request fetchedRequest = requestDao.getRequestById(result.getRequestId());
        assertNotNull(fetchedRequest.getAdditionalCheckOutInfo());
        assertEquals(fetchedRequest.getRequestId(), 
                    fetchedRequest.getAdditionalCheckOutInfo().getRequestId());
    }

    // ==================== Helper Methods ====================

    /**
     * Helper method to build a check-out request for testing
     */
    private Request buildCheckOutRequest(String employeeId, LocalDateTime desiredCheckOutTime, String userReason) {
        LocalDateTime currentCheckOutTime = desiredCheckOutTime != null 
            ? desiredCheckOutTime.minusMinutes(120) // Current time is 2 hours before desired
            : LocalDateTime.now();

        User employee = employeeId != null 
            ? User.builder().userId(employeeId).build() 
            : null;

        AdditionalCheckOutInfo checkOutInfo = AdditionalCheckOutInfo.builder()
            .desiredCheckOutTime(desiredCheckOutTime)
            .currentCheckOutTime(currentCheckOutTime)
            .build();

        return Request.builder()
            .title("Check-out Request Test")
            .userReason(userReason)
            .employee(employee)
            .additionalCheckOutInfo(checkOutInfo)
            .build();
    }
}
