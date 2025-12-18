package org.pqkkkkk.hr_management_server.modules.timesheet.domain.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.service.ProfileQueryService;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.ShiftType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.LeaveDate;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.WfhDate;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.dao.DailyTimeSheetDao;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity.DailyTimeSheet;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity.Enums.AttendanceStatus;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.service.TimeSheetCommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for TimeSheetCommandService.
 * Tests focus on business logic and calculation accuracy.
 * Sample data loaded from V14__insert_timesheet_sample_data.sql
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("TimeSheetCommandService Integration Tests")
class TimeSheetCommandServiceIntegrationTest {

    @Autowired
    private TimeSheetCommandService timeSheetCommandService;

    @Autowired
    private DailyTimeSheetDao dailyTimeSheetDao;

    @Autowired
    private ProfileQueryService profileQueryService;

    private String testEmployeeId;
    private User testEmployee;

    @BeforeEach
    void setUp() {
        // Use sample employee from V3__insert_profile_sample_data.sql
        testEmployeeId = "u1a2b3c4-e5f6-7890-abcd-ef1234567890"; // Nguyen Van An
        testEmployee = profileQueryService.getProfileById(testEmployeeId);
        assertNotNull(testEmployee, "Test employee should be loaded from migration");
    }

    // ============= handleCheckInApproval() Tests =============

    @Test
    @DisplayName("Should create new timesheet with on-time check-in")
    void testHandleCheckInApproval_OnTime_CreateNewTimesheet() {
        // Arrange - On-time check-in at 8:00 AM
        LocalDateTime checkInTime = LocalDateTime.of(2024, 12, 20, 8, 0);

        // Act
        DailyTimeSheet result = timeSheetCommandService.handleCheckInApproval(
            testEmployeeId, 
            checkInTime
        );

        // Assert
        assertNotNull(result);
        assertEquals(checkInTime, result.getCheckInTime());
        assertEquals(AttendanceStatus.PRESENT, result.getMorningStatus());
        assertEquals(0, result.getLateMinutes());
        assertNull(result.getCheckOutTime());
        
        // Verify persistence
        DailyTimeSheet saved = dailyTimeSheetDao.getTimesheetByEmployeeAndDate(
            testEmployeeId, 
            LocalDate.of(2024, 12, 20)
        );
        assertNotNull(saved);
        assertEquals(checkInTime, saved.getCheckInTime());
    }

    @Test
    @DisplayName("Should calculate late minutes correctly for late check-in")
    void testHandleCheckInApproval_Late_CalculateLateMinutes() {
        // Arrange - Late check-in at 9:30 AM (90 minutes late)
        LocalDateTime checkInTime = LocalDateTime.of(2024, 12, 21, 9, 30);

        // Act
        DailyTimeSheet result = timeSheetCommandService.handleCheckInApproval(
            testEmployeeId, 
            checkInTime
        );

        // Assert
        assertEquals(90, result.getLateMinutes());
        assertEquals(AttendanceStatus.PRESENT, result.getMorningStatus());
    }

    @Test
    @DisplayName("Should only set morning status for morning check-in")
    void testHandleCheckInApproval_Morning_OnlySetMorningStatus() {
        // Arrange - Morning check-in at 7:30 AM
        LocalDateTime checkInTime = LocalDateTime.of(2024, 12, 22, 7, 30);

        // Act
        DailyTimeSheet result = timeSheetCommandService.handleCheckInApproval(
            testEmployeeId, 
            checkInTime
        );

        // Assert
        assertEquals(AttendanceStatus.PRESENT, result.getMorningStatus());
        assertNull(result.getAfternoonStatus());
        assertEquals(0, result.getLateMinutes()); // Early, not late
    }

    @Test
    @DisplayName("Should not set morning status for afternoon check-in")
    void testHandleCheckInApproval_Afternoon_NoMorningStatus() {
        // Arrange - Afternoon check-in at 1:00 PM
        LocalDateTime checkInTime = LocalDateTime.of(2024, 12, 23, 13, 0);

        // Act
        DailyTimeSheet result = timeSheetCommandService.handleCheckInApproval(
            testEmployeeId, 
            checkInTime
        );

        // Assert
        assertNull(result.getMorningStatus());
        assertEquals(checkInTime, result.getCheckInTime());
        // Should calculate late minutes (5 hours = 300 minutes late)
        assertEquals(300, result.getLateMinutes());
    }

    @Test
    @DisplayName("Should update existing timesheet on duplicate check-in")
    void testHandleCheckInApproval_ExistingTimesheet_Update() {
        // Arrange - Date with existing timesheet from sample data
        LocalDateTime newCheckInTime = LocalDateTime.of(2024, 12, 1, 8, 30);

        // Act
        DailyTimeSheet result = timeSheetCommandService.handleCheckInApproval(
            testEmployeeId, 
            newCheckInTime
        );

        // Assert
        assertEquals(newCheckInTime, result.getCheckInTime());
        assertEquals(30, result.getLateMinutes()); // 30 minutes late
        
        // Verify only one timesheet exists for this date
        DailyTimeSheet saved = dailyTimeSheetDao.getTimesheetByEmployeeAndDate(
            testEmployeeId, 
            LocalDate.of(2024, 12, 1)
        );
        assertEquals(result.getDailyTsId(), saved.getDailyTsId());
    }

    // ============= handleCheckOutApproval() Tests =============

    @Test
    @DisplayName("Should calculate work credit correctly for normal check-out")
    void testHandleCheckOutApproval_NormalCheckOut_CorrectWorkCredit() {
        // Arrange - Check-in at 8:00 AM, check-out at 5:00 PM (9 hours = 1.0 credit)
        LocalDate workDate = LocalDate.of(2024, 12, 24);
        LocalDateTime checkInTime = LocalDateTime.of(2024, 12, 24, 8, 0);
        LocalDateTime checkOutTime = LocalDateTime.of(2024, 12, 24, 17, 0);
        
        // Create check-in first
        timeSheetCommandService.handleCheckInApproval(testEmployeeId, checkInTime);

        // Act - Check-out
        DailyTimeSheet result = timeSheetCommandService.handleCheckOutApproval(
            testEmployeeId, 
            checkOutTime
        );

        // Assert
        assertEquals(checkOutTime, result.getCheckOutTime());
        assertEquals(AttendanceStatus.PRESENT, result.getAfternoonStatus());
        assertEquals(0, result.getEarlyLeaveMinutes());
        assertEquals(0, result.getOvertimeMinutes());
        assertEquals(1.0, result.getTotalWorkCredit(), 0.001); // 9 hours = 1.0 credit
    }

    @Test
    @DisplayName("Should calculate work credit for early leave (8 hours)")
    void testHandleCheckOutApproval_EarlyLeave_ProportionalWorkCredit() {
        // Arrange - Check-in at 8:00 AM, check-out at 4:00 PM (8 hours = 0.889 credit)
        LocalDate workDate = LocalDate.of(2024, 12, 25);
        LocalDateTime checkInTime = LocalDateTime.of(2024, 12, 25, 8, 0);
        LocalDateTime checkOutTime = LocalDateTime.of(2024, 12, 25, 16, 0);
        
        timeSheetCommandService.handleCheckInApproval(testEmployeeId, checkInTime);

        // Act
        DailyTimeSheet result = timeSheetCommandService.handleCheckOutApproval(
            testEmployeeId, 
            checkOutTime
        );

        // Assert
        assertEquals(60, result.getEarlyLeaveMinutes()); // 1 hour early
        assertEquals(0, result.getOvertimeMinutes());
        // 8 hours = 480 minutes / 540 standard minutes = 0.889
        assertEquals(0.889, result.getTotalWorkCredit(), 0.001);
    }

    @Test
    @DisplayName("Should calculate overtime and work credit correctly")
    void testHandleCheckOutApproval_Overtime_IncreasedWorkCredit() {
        // Arrange - Check-in at 8:00 AM, check-out at 6:00 PM (10 hours = 1.111 credit)
        LocalDate workDate = LocalDate.of(2024, 12, 26);
        LocalDateTime checkInTime = LocalDateTime.of(2024, 12, 26, 8, 0);
        LocalDateTime checkOutTime = LocalDateTime.of(2024, 12, 26, 18, 0);
        
        timeSheetCommandService.handleCheckInApproval(testEmployeeId, checkInTime);

        // Act
        DailyTimeSheet result = timeSheetCommandService.handleCheckOutApproval(
            testEmployeeId, 
            checkOutTime
        );

        // Assert
        assertEquals(0, result.getEarlyLeaveMinutes());
        assertEquals(60, result.getOvertimeMinutes()); // 1 hour overtime
        // 10 hours = 600 minutes / 540 standard minutes = 1.111
        assertEquals(1.111, result.getTotalWorkCredit(), 0.001);
    }

    @Test
    @DisplayName("Should calculate work credit for partial day work")
    void testHandleCheckOutApproval_PartialDay_LowWorkCredit() {
        // Arrange - Check-in at 10:00 AM, check-out at 2:00 PM (4 hours = 0.444 credit)
        LocalDate workDate = LocalDate.of(2024, 12, 27);
        LocalDateTime checkInTime = LocalDateTime.of(2024, 12, 27, 10, 0);
        LocalDateTime checkOutTime = LocalDateTime.of(2024, 12, 27, 14, 0);
        
        timeSheetCommandService.handleCheckInApproval(testEmployeeId, checkInTime);

        // Act
        DailyTimeSheet result = timeSheetCommandService.handleCheckOutApproval(
            testEmployeeId, 
            checkOutTime
        );

        // Assert
        assertEquals(180, result.getEarlyLeaveMinutes()); // 3 hours early
        assertEquals(0, result.getOvertimeMinutes());
        // 4 hours = 240 minutes / 540 standard minutes = 0.444
        assertEquals(0.444, result.getTotalWorkCredit(), 0.001);
    }

    @Test
    @DisplayName("Should throw exception when check-in is missing")
    void testHandleCheckOutApproval_NoCheckIn_ThrowsException() {
        // Arrange - Try to check-out without check-in
        LocalDateTime checkOutTime = LocalDateTime.of(2024, 12, 28, 17, 0);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> timeSheetCommandService.handleCheckOutApproval(testEmployeeId, checkOutTime)
        );
        
        assertTrue(exception.getMessage().contains("not found") || 
                   exception.getMessage().contains("required"));
    }

    @Test
    @DisplayName("Should set afternoon status for afternoon check-out")
    void testHandleCheckOutApproval_Afternoon_SetAfternoonStatus() {
        // Arrange - Check-in morning, check-out afternoon
        LocalDate workDate = LocalDate.of(2024, 12, 29);
        LocalDateTime checkInTime = LocalDateTime.of(2024, 12, 29, 8, 0);
        LocalDateTime checkOutTime = LocalDateTime.of(2024, 12, 29, 13, 30); // After noon
        
        timeSheetCommandService.handleCheckInApproval(testEmployeeId, checkInTime);

        // Act
        DailyTimeSheet result = timeSheetCommandService.handleCheckOutApproval(
            testEmployeeId, 
            checkOutTime
        );

        // Assert
        assertEquals(AttendanceStatus.PRESENT, result.getAfternoonStatus());
        assertEquals(AttendanceStatus.PRESENT, result.getMorningStatus());
    }

    // ============= handleLeaveApproval() Tests =============

    @Test
    @DisplayName("Should create timesheet for full-day leave with zero work credit")
    void testHandleLeaveApproval_FullDay_ZeroWorkCredit() {
        // Arrange - Full day leave
        LocalDate leaveDate = LocalDate.of(2025, 1, 10);
        LeaveDate leaveDateEntity = LeaveDate.builder()
            .date(leaveDate)
            .shift(ShiftType.FULL_DAY)
            .build();

        // Act
        List<DailyTimeSheet> results = timeSheetCommandService.handleLeaveApproval(
            testEmployeeId, 
            Arrays.asList(leaveDateEntity)
        );

        // Assert
        assertEquals(1, results.size());
        DailyTimeSheet result = results.get(0);
        assertEquals(AttendanceStatus.LEAVE, result.getMorningStatus());
        assertEquals(AttendanceStatus.LEAVE, result.getAfternoonStatus());
        assertEquals(0.0, result.getTotalWorkCredit(), 0.001);
        assertNull(result.getCheckInTime());
        assertNull(result.getCheckOutTime());
        assertEquals(0, result.getLateMinutes());
        assertEquals(0, result.getEarlyLeaveMinutes());
    }

    @Test
    @DisplayName("Should create timesheet for morning leave only")
    void testHandleLeaveApproval_Morning_OnlyMorningLeave() {
        // Arrange - Morning leave
        LocalDate leaveDate = LocalDate.of(2025, 1, 11);
        LeaveDate leaveDateEntity = LeaveDate.builder()
            .date(leaveDate)
            .shift(ShiftType.MORNING)
            .build();

        // Act
        List<DailyTimeSheet> results = timeSheetCommandService.handleLeaveApproval(
            testEmployeeId, 
            Arrays.asList(leaveDateEntity)
        );

        // Assert
        DailyTimeSheet result = results.get(0);
        assertEquals(AttendanceStatus.LEAVE, result.getMorningStatus());
        assertNull(result.getAfternoonStatus());
        assertEquals(0.0, result.getTotalWorkCredit(), 0.001);
    }

    @Test
    @DisplayName("Should create timesheet for afternoon leave only")
    void testHandleLeaveApproval_Afternoon_OnlyAfternoonLeave() {
        // Arrange - Afternoon leave
        LocalDate leaveDate = LocalDate.of(2025, 1, 12);
        LeaveDate leaveDateEntity = LeaveDate.builder()
            .date(leaveDate)
            .shift(ShiftType.AFTERNOON)
            .build();

        // Act
        List<DailyTimeSheet> results = timeSheetCommandService.handleLeaveApproval(
            testEmployeeId, 
            Arrays.asList(leaveDateEntity)
        );

        // Assert
        DailyTimeSheet result = results.get(0);
        assertNull(result.getMorningStatus());
        assertEquals(AttendanceStatus.LEAVE, result.getAfternoonStatus());
        assertEquals(0.0, result.getTotalWorkCredit(), 0.001);
    }

    @Test
    @DisplayName("Should create multiple timesheets for multiple leave dates")
    void testHandleLeaveApproval_MultipleDates_CreateMultipleTimesheets() {
        // Arrange - 3 consecutive leave days
        LeaveDate day1 = LeaveDate.builder()
            .date(LocalDate.of(2025, 1, 13))
            .shift(ShiftType.FULL_DAY)
            .build();
        LeaveDate day2 = LeaveDate.builder()
            .date(LocalDate.of(2025, 1, 14))
            .shift(ShiftType.FULL_DAY)
            .build();
        LeaveDate day3 = LeaveDate.builder()
            .date(LocalDate.of(2025, 1, 15))
            .shift(ShiftType.MORNING)
            .build();

        // Act
        List<DailyTimeSheet> results = timeSheetCommandService.handleLeaveApproval(
            testEmployeeId, 
            Arrays.asList(day1, day2, day3)
        );

        // Assert
        assertEquals(3, results.size());
        
        // Verify all have zero work credit
        results.forEach(ts -> assertEquals(0.0, ts.getTotalWorkCredit(), 0.001));
        
        // Verify first two are full day
        assertEquals(AttendanceStatus.LEAVE, results.get(0).getMorningStatus());
        assertEquals(AttendanceStatus.LEAVE, results.get(0).getAfternoonStatus());
        assertEquals(AttendanceStatus.LEAVE, results.get(1).getMorningStatus());
        assertEquals(AttendanceStatus.LEAVE, results.get(1).getAfternoonStatus());
        
        // Verify last is morning only
        assertEquals(AttendanceStatus.LEAVE, results.get(2).getMorningStatus());
        assertNull(results.get(2).getAfternoonStatus());
    }

    @Test
    @DisplayName("Should clear existing check-in/out times when applying leave")
    void testHandleLeaveApproval_ExistingTimesheet_ClearTimes() {
        // Arrange - Date with existing timesheet with check-in/out times
        LocalDate existingDate = LocalDate.of(2024, 12, 1);
        LeaveDate leaveDateEntity = LeaveDate.builder()
            .date(existingDate)
            .shift(ShiftType.FULL_DAY)
            .build();

        // Act
        List<DailyTimeSheet> results = timeSheetCommandService.handleLeaveApproval(
            testEmployeeId, 
            Arrays.asList(leaveDateEntity)
        );

        // Assert
        DailyTimeSheet result = results.get(0);
        assertNull(result.getCheckInTime());
        assertNull(result.getCheckOutTime());
        assertEquals(0, result.getLateMinutes());
        assertEquals(0, result.getEarlyLeaveMinutes());
        assertEquals(0, result.getOvertimeMinutes());
        assertEquals(AttendanceStatus.LEAVE, result.getMorningStatus());
        assertEquals(AttendanceStatus.LEAVE, result.getAfternoonStatus());
    }

    // ============= handleWfhApproval() Tests =============

    @Test
    @DisplayName("Should create timesheet for full-day WFH with full work credit")
    void testHandleWfhApproval_FullDay_FullWorkCredit() {
        // Arrange - Full day WFH
        LocalDate wfhDate = LocalDate.of(2025, 1, 20);
        WfhDate wfhDateEntity = WfhDate.builder()
            .date(wfhDate)
            .shift(ShiftType.FULL_DAY)
            .build();

        // Act
        List<DailyTimeSheet> results = timeSheetCommandService.handleWfhApproval(
            testEmployeeId, 
            Arrays.asList(wfhDateEntity)
        );

        // Assert
        assertEquals(1, results.size());
        DailyTimeSheet result = results.get(0);
        assertTrue(result.getMorningWfh());
        assertTrue(result.getAfternoonWfh());
        assertEquals(AttendanceStatus.PRESENT, result.getMorningStatus());
        assertEquals(AttendanceStatus.PRESENT, result.getAfternoonStatus());
        assertEquals(1.0, result.getTotalWorkCredit(), 0.001);
        assertEquals(0, result.getLateMinutes());
        assertEquals(0, result.getEarlyLeaveMinutes());
        assertEquals(0, result.getOvertimeMinutes());
    }

    @Test
    @DisplayName("Should create timesheet for morning WFH with half work credit")
    void testHandleWfhApproval_Morning_HalfWorkCredit() {
        // Arrange - Morning WFH
        LocalDate wfhDate = LocalDate.of(2025, 1, 21);
        WfhDate wfhDateEntity = WfhDate.builder()
            .date(wfhDate)
            .shift(ShiftType.MORNING)
            .build();

        // Act
        List<DailyTimeSheet> results = timeSheetCommandService.handleWfhApproval(
            testEmployeeId, 
            Arrays.asList(wfhDateEntity)
        );

        // Assert
        DailyTimeSheet result = results.get(0);
        assertTrue(result.getMorningWfh());
        assertFalse(result.getAfternoonWfh());
        assertEquals(AttendanceStatus.PRESENT, result.getMorningStatus());
        assertNull(result.getAfternoonStatus());
        assertEquals(0.5, result.getTotalWorkCredit(), 0.001);
    }

    @Test
    @DisplayName("Should create timesheet for afternoon WFH with half work credit")
    void testHandleWfhApproval_Afternoon_HalfWorkCredit() {
        // Arrange - Afternoon WFH
        LocalDate wfhDate = LocalDate.of(2025, 1, 22);
        WfhDate wfhDateEntity = WfhDate.builder()
            .date(wfhDate)
            .shift(ShiftType.AFTERNOON)
            .build();

        // Act
        List<DailyTimeSheet> results = timeSheetCommandService.handleWfhApproval(
            testEmployeeId, 
            Arrays.asList(wfhDateEntity)
        );

        // Assert
        DailyTimeSheet result = results.get(0);
        assertFalse(result.getMorningWfh());
        assertTrue(result.getAfternoonWfh());
        assertNull(result.getMorningStatus());
        assertEquals(AttendanceStatus.PRESENT, result.getAfternoonStatus());
        assertEquals(0.5, result.getTotalWorkCredit(), 0.001);
    }

    @Test
    @DisplayName("Should create multiple timesheets for multiple WFH dates")
    void testHandleWfhApproval_MultipleDates_CreateMultipleTimesheets() {
        // Arrange - 3 WFH dates with different shifts
        WfhDate day1 = WfhDate.builder()
            .date(LocalDate.of(2025, 1, 23))
            .shift(ShiftType.FULL_DAY)
            .build();
        WfhDate day2 = WfhDate.builder()
            .date(LocalDate.of(2025, 1, 24))
            .shift(ShiftType.MORNING)
            .build();
        WfhDate day3 = WfhDate.builder()
            .date(LocalDate.of(2025, 1, 25))
            .shift(ShiftType.AFTERNOON)
            .build();

        // Act
        List<DailyTimeSheet> results = timeSheetCommandService.handleWfhApproval(
            testEmployeeId, 
            Arrays.asList(day1, day2, day3)
        );

        // Assert
        assertEquals(3, results.size());
        
        // Verify first is full day
        assertEquals(1.0, results.get(0).getTotalWorkCredit(), 0.001);
        assertTrue(results.get(0).getMorningWfh());
        assertTrue(results.get(0).getAfternoonWfh());
        
        // Verify second is morning only
        assertEquals(0.5, results.get(1).getTotalWorkCredit(), 0.001);
        assertTrue(results.get(1).getMorningWfh());
        assertFalse(results.get(1).getAfternoonWfh());
        
        // Verify third is afternoon only
        assertEquals(0.5, results.get(2).getTotalWorkCredit(), 0.001);
        assertFalse(results.get(2).getMorningWfh());
        assertTrue(results.get(2).getAfternoonWfh());
    }

    @Test
    @DisplayName("Should reset time-based metrics for WFH days")
    void testHandleWfhApproval_ExistingTimesheet_ResetTimeMetrics() {
        // Arrange - Date with existing timesheet that has time metrics
        LocalDate existingDate = LocalDate.of(2024, 12, 2); // Has late minutes from sample data
        WfhDate wfhDateEntity = WfhDate.builder()
            .date(existingDate)
            .shift(ShiftType.FULL_DAY)
            .build();

        // Act
        List<DailyTimeSheet> results = timeSheetCommandService.handleWfhApproval(
            testEmployeeId, 
            Arrays.asList(wfhDateEntity)
        );

        // Assert
        DailyTimeSheet result = results.get(0);
        assertEquals(0, result.getLateMinutes());
        assertEquals(0, result.getEarlyLeaveMinutes());
        assertEquals(0, result.getOvertimeMinutes());
        assertTrue(result.getMorningWfh());
        assertTrue(result.getAfternoonWfh());
        assertEquals(1.0, result.getTotalWorkCredit(), 0.001);
    }

    // ============= handleTimesheetUpdateApproval() Tests =============

    @Test
    @DisplayName("Should update timesheet and recalculate all metrics")
    void testHandleTimesheetUpdateApproval_UpdateTimes_RecalculateMetrics() {
        // Arrange - Date with existing timesheet (2024-12-01 from sample data)
        LocalDate targetDate = LocalDate.of(2024, 12, 1);
        LocalDateTime newCheckIn = LocalDateTime.of(2024, 12, 1, 9, 0);  // 1 hour late
        LocalDateTime newCheckOut = LocalDateTime.of(2024, 12, 1, 16, 0); // 1 hour early

        // Act
        DailyTimeSheet result = timeSheetCommandService.handleTimesheetUpdateApproval(
            testEmployeeId,
            targetDate,
            newCheckIn,
            newCheckOut
        );

        // Assert
        assertEquals(newCheckIn, result.getCheckInTime());
        assertEquals(newCheckOut, result.getCheckOutTime());
        assertEquals(60, result.getLateMinutes());      // 1 hour late
        assertEquals(60, result.getEarlyLeaveMinutes()); // 1 hour early
        assertEquals(0, result.getOvertimeMinutes());
        // 7 hours = 420 minutes / 540 standard = 0.778
        assertEquals(0.778, result.getTotalWorkCredit(), 0.001);
        assertEquals(AttendanceStatus.PRESENT, result.getMorningStatus());
        assertEquals(AttendanceStatus.PRESENT, result.getAfternoonStatus());
    }

    @Test
    @DisplayName("Should recalculate work credit for overtime scenario")
    void testHandleTimesheetUpdateApproval_Overtime_CorrectCalculation() {
        // Arrange
        LocalDate targetDate = LocalDate.of(2024, 12, 1);
        LocalDateTime newCheckIn = LocalDateTime.of(2024, 12, 1, 7, 0);  // 1 hour early
        LocalDateTime newCheckOut = LocalDateTime.of(2024, 12, 1, 19, 0); // 2 hours overtime

        // Act
        DailyTimeSheet result = timeSheetCommandService.handleTimesheetUpdateApproval(
            testEmployeeId,
            targetDate,
            newCheckIn,
            newCheckOut
        );

        // Assert
        assertEquals(0, result.getLateMinutes());        // On time (early)
        assertEquals(0, result.getEarlyLeaveMinutes());  // No early leave
        assertEquals(120, result.getOvertimeMinutes());  // 2 hours overtime
        // 12 hours = 720 minutes / 540 standard = 1.333
        assertEquals(1.333, result.getTotalWorkCredit(), 0.001);
    }

    @Test
    @DisplayName("Should update from late to on-time correctly")
    void testHandleTimesheetUpdateApproval_LateToOnTime_UpdateMetrics() {
        // Arrange - Date with late check-in (2024-12-02 from sample data has 60 late minutes)
        LocalDate targetDate = LocalDate.of(2024, 12, 2);
        LocalDateTime newCheckIn = LocalDateTime.of(2024, 12, 2, 8, 0);  // On time now
        LocalDateTime newCheckOut = LocalDateTime.of(2024, 12, 2, 17, 0); // On time

        // Act
        DailyTimeSheet result = timeSheetCommandService.handleTimesheetUpdateApproval(
            testEmployeeId,
            targetDate,
            newCheckIn,
            newCheckOut
        );

        // Assert
        assertEquals(0, result.getLateMinutes());         // No longer late
        assertEquals(0, result.getEarlyLeaveMinutes());
        assertEquals(0, result.getOvertimeMinutes());
        assertEquals(1.0, result.getTotalWorkCredit(), 0.001); // Full credit now
    }

    @Test
    @DisplayName("Should throw exception when updating finalized timesheet")
    void testHandleTimesheetUpdateApproval_Finalized_ThrowsException() {
        // Arrange - Finalized timesheet from sample data (2024-12-05)
        LocalDate finalizedDate = LocalDate.of(2024, 12, 5);
        LocalDateTime newCheckIn = LocalDateTime.of(2024, 12, 5, 8, 0);
        LocalDateTime newCheckOut = LocalDateTime.of(2024, 12, 5, 17, 0);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> timeSheetCommandService.handleTimesheetUpdateApproval(
                testEmployeeId,
                finalizedDate,
                newCheckIn,
                newCheckOut
            )
        );
        
        assertTrue(exception.getMessage().contains("finalized"));
    }

    @Test
    @DisplayName("Should reset WFH flags when updating timesheet")
    void testHandleTimesheetUpdateApproval_ExistingWfh_ResetFlags() {
        // Arrange - Date with WFH flags (2024-12-04 from sample data)
        LocalDate targetDate = LocalDate.of(2024, 12, 4);
        LocalDateTime newCheckIn = LocalDateTime.of(2024, 12, 4, 8, 30);
        LocalDateTime newCheckOut = LocalDateTime.of(2024, 12, 4, 17, 30);

        // Act
        DailyTimeSheet result = timeSheetCommandService.handleTimesheetUpdateApproval(
            testEmployeeId,
            targetDate,
            newCheckIn,
            newCheckOut
        );

        // Assert
        assertFalse(result.getMorningWfh());    // WFH flags should be reset
        assertFalse(result.getAfternoonWfh());  // WFH flags should be reset
        assertEquals(30, result.getLateMinutes());
        assertEquals(0, result.getEarlyLeaveMinutes());
        assertEquals(30, result.getOvertimeMinutes());
        // 9 hours = 540 minutes / 540 standard = 1.0
        assertEquals(1.0, result.getTotalWorkCredit(), 0.001);
    }

    @Test
    @DisplayName("Should throw exception when timesheet does not exist")
    void testHandleTimesheetUpdateApproval_NonExistent_ThrowsException() {
        // Arrange - Future date without timesheet
        LocalDate futureDate = LocalDate.of(2025, 12, 31);
        LocalDateTime newCheckIn = LocalDateTime.of(2025, 12, 31, 8, 0);
        LocalDateTime newCheckOut = LocalDateTime.of(2025, 12, 31, 17, 0);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> timeSheetCommandService.handleTimesheetUpdateApproval(
                testEmployeeId,
                futureDate,
                newCheckIn,
                newCheckOut
            )
        );
        
        assertTrue(exception.getMessage().contains("not found"));
    }
}
