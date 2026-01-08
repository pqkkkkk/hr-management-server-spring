package org.pqkkkkk.hr_management_server.modules.timesheet.domain.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.ShiftType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.LeaveDate;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.WfhDate;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.dao.DailyTimeSheetDao;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity.DailyTimeSheet;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity.Enums.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TimeSheetValidationService.
 * Uses mock DAO to test business logic validation rules without database.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TimeSheetValidationService Unit Tests")
class TimeSheetValidationServiceTest {

    @Mock
    private DailyTimeSheetDao dailyTimeSheetDao;

    @InjectMocks
    private TimeSheetValidationServiceImpl validationService;

    private static final String EMPLOYEE_ID = "emp-123";
    private static final LocalDate TEST_DATE = LocalDate.of(2024, 12, 15);

    // ============= validateCheckInApproval() Tests =============

    @Nested
    @DisplayName("validateCheckInApproval")
    class ValidateCheckInApprovalTests {

        @Test
        @DisplayName("Should pass when no existing timesheet")
        void shouldPassWhenNoExistingTimesheet() {
            // Arrange
            LocalDateTime checkInTime = LocalDateTime.of(2024, 12, 15, 8, 0);
            when(dailyTimeSheetDao.getTimesheetByEmployeeAndDate(eq(EMPLOYEE_ID), any()))
                    .thenReturn(null);

            // Act & Assert - No exception thrown
            assertDoesNotThrow(() -> validationService.validateCheckInApproval(EMPLOYEE_ID, checkInTime));
        }

        @Test
        @DisplayName("Should pass when existing timesheet has no check-in")
        void shouldPassWhenExistingTimesheetHasNoCheckIn() {
            // Arrange
            LocalDateTime checkInTime = LocalDateTime.of(2024, 12, 15, 8, 0);
            DailyTimeSheet existing = DailyTimeSheet.builder()
                    .date(TEST_DATE)
                    .checkInTime(null) // No check-in yet
                    .build();
            when(dailyTimeSheetDao.getTimesheetByEmployeeAndDate(eq(EMPLOYEE_ID), any()))
                    .thenReturn(existing);

            // Act & Assert
            assertDoesNotThrow(() -> validationService.validateCheckInApproval(EMPLOYEE_ID, checkInTime));
        }

        @Test
        @DisplayName("Should throw when check-in already exists (duplicate)")
        void shouldThrowWhenCheckInAlreadyExists() {
            // Arrange
            LocalDateTime checkInTime = LocalDateTime.of(2024, 12, 15, 8, 0);
            DailyTimeSheet existing = DailyTimeSheet.builder()
                    .date(TEST_DATE)
                    .checkInTime(LocalDateTime.of(2024, 12, 15, 8, 30)) // Already has check-in
                    .build();
            when(dailyTimeSheetDao.getTimesheetByEmployeeAndDate(eq(EMPLOYEE_ID), any()))
                    .thenReturn(existing);

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> validationService.validateCheckInApproval(EMPLOYEE_ID, checkInTime));

            assertTrue(exception.getMessage().contains("already has check-in"));
            assertTrue(exception.getMessage().contains("Timesheet Update"));
        }

        @Test
        @DisplayName("Should throw when date is full-day leave")
        void shouldThrowWhenFullDayLeave() {
            // Arrange
            LocalDateTime checkInTime = LocalDateTime.of(2024, 12, 15, 8, 0);
            DailyTimeSheet existing = DailyTimeSheet.builder()
                    .date(TEST_DATE)
                    .morningStatus(AttendanceStatus.LEAVE)
                    .afternoonStatus(AttendanceStatus.LEAVE)
                    .build();
            when(dailyTimeSheetDao.getTimesheetByEmployeeAndDate(eq(EMPLOYEE_ID), any()))
                    .thenReturn(existing);

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> validationService.validateCheckInApproval(EMPLOYEE_ID, checkInTime));

            assertTrue(exception.getMessage().contains("full-day leave"));
        }

        @Test
        @DisplayName("Should throw when morning check-in but morning is LEAVE")
        void shouldThrowWhenMorningCheckInButMorningLeave() {
            // Arrange (check-in at 8 AM = morning)
            LocalDateTime morningCheckIn = LocalDateTime.of(2024, 12, 15, 8, 0);
            DailyTimeSheet existing = DailyTimeSheet.builder()
                    .date(TEST_DATE)
                    .morningStatus(AttendanceStatus.LEAVE)
                    .afternoonStatus(AttendanceStatus.PRESENT) // Not full-day leave
                    .build();
            when(dailyTimeSheetDao.getTimesheetByEmployeeAndDate(eq(EMPLOYEE_ID), any()))
                    .thenReturn(existing);

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> validationService.validateCheckInApproval(EMPLOYEE_ID, morningCheckIn));

            assertTrue(exception.getMessage().contains("morning leave"));
        }

        @Test
        @DisplayName("Should pass for afternoon check-in when morning is LEAVE")
        void shouldPassForAfternoonCheckInWhenMorningLeave() {
            // Arrange (check-in at 1 PM = afternoon)
            LocalDateTime afternoonCheckIn = LocalDateTime.of(2024, 12, 15, 13, 0);
            DailyTimeSheet existing = DailyTimeSheet.builder()
                    .date(TEST_DATE)
                    .morningStatus(AttendanceStatus.LEAVE)
                    .afternoonStatus(null) // Afternoon available
                    .build();
            when(dailyTimeSheetDao.getTimesheetByEmployeeAndDate(eq(EMPLOYEE_ID), any()))
                    .thenReturn(existing);

            // Act & Assert - Should not throw
            assertDoesNotThrow(() -> validationService.validateCheckInApproval(EMPLOYEE_ID, afternoonCheckIn));
        }

        @Test
        @DisplayName("Should throw for null employee ID")
        void shouldThrowForNullEmployeeId() {
            LocalDateTime checkInTime = LocalDateTime.of(2024, 12, 15, 8, 0);

            assertThrows(IllegalArgumentException.class,
                    () -> validationService.validateCheckInApproval(null, checkInTime));
        }

        @Test
        @DisplayName("Should throw for null check-in time")
        void shouldThrowForNullCheckInTime() {
            assertThrows(IllegalArgumentException.class,
                    () -> validationService.validateCheckInApproval(EMPLOYEE_ID, null));
        }
    }

    // ============= validateCheckOutApproval() Tests =============

    @Nested
    @DisplayName("validateCheckOutApproval")
    class ValidateCheckOutApprovalTests {

        @Test
        @DisplayName("Should pass with valid check-out after check-in")
        void shouldPassWithValidCheckOut() {
            // Arrange
            LocalDateTime checkInTime = LocalDateTime.of(2024, 12, 15, 8, 0);
            LocalDateTime checkOutTime = LocalDateTime.of(2024, 12, 15, 17, 0);
            DailyTimeSheet existing = DailyTimeSheet.builder()
                    .date(TEST_DATE)
                    .checkInTime(checkInTime)
                    .checkOutTime(null) // No check-out yet
                    .build();
            when(dailyTimeSheetDao.getTimesheetByEmployeeAndDate(eq(EMPLOYEE_ID), any()))
                    .thenReturn(existing);

            // Act & Assert
            assertDoesNotThrow(() -> validationService.validateCheckOutApproval(EMPLOYEE_ID, checkOutTime));
        }

        @Test
        @DisplayName("Should throw when no timesheet exists")
        void shouldThrowWhenNoTimesheetExists() {
            LocalDateTime checkOutTime = LocalDateTime.of(2024, 12, 15, 17, 0);
            when(dailyTimeSheetDao.getTimesheetByEmployeeAndDate(eq(EMPLOYEE_ID), any()))
                    .thenReturn(null);

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> validationService.validateCheckOutApproval(EMPLOYEE_ID, checkOutTime));

            assertTrue(exception.getMessage().contains("No timesheet found"));
        }

        @Test
        @DisplayName("Should throw when no check-in exists")
        void shouldThrowWhenNoCheckInExists() {
            LocalDateTime checkOutTime = LocalDateTime.of(2024, 12, 15, 17, 0);
            DailyTimeSheet existing = DailyTimeSheet.builder()
                    .date(TEST_DATE)
                    .checkInTime(null) // No check-in
                    .build();
            when(dailyTimeSheetDao.getTimesheetByEmployeeAndDate(eq(EMPLOYEE_ID), any()))
                    .thenReturn(existing);

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> validationService.validateCheckOutApproval(EMPLOYEE_ID, checkOutTime));

            assertTrue(exception.getMessage().contains("no check-in"));
        }

        @Test
        @DisplayName("Should throw when check-out already exists (duplicate)")
        void shouldThrowWhenCheckOutAlreadyExists() {
            LocalDateTime checkInTime = LocalDateTime.of(2024, 12, 15, 8, 0);
            LocalDateTime existingCheckOut = LocalDateTime.of(2024, 12, 15, 16, 0);
            LocalDateTime newCheckOut = LocalDateTime.of(2024, 12, 15, 17, 0);

            DailyTimeSheet existing = DailyTimeSheet.builder()
                    .date(TEST_DATE)
                    .checkInTime(checkInTime)
                    .checkOutTime(existingCheckOut) // Already has check-out
                    .build();
            when(dailyTimeSheetDao.getTimesheetByEmployeeAndDate(eq(EMPLOYEE_ID), any()))
                    .thenReturn(existing);

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> validationService.validateCheckOutApproval(EMPLOYEE_ID, newCheckOut));

            assertTrue(exception.getMessage().contains("already has check-out"));
        }

        @Test
        @DisplayName("Should throw when check-out is before check-in")
        void shouldThrowWhenCheckOutBeforeCheckIn() {
            LocalDateTime checkInTime = LocalDateTime.of(2024, 12, 15, 17, 0); // PM
            LocalDateTime checkOutTime = LocalDateTime.of(2024, 12, 15, 8, 0); // AM - invalid

            DailyTimeSheet existing = DailyTimeSheet.builder()
                    .date(TEST_DATE)
                    .checkInTime(checkInTime)
                    .checkOutTime(null)
                    .build();
            when(dailyTimeSheetDao.getTimesheetByEmployeeAndDate(eq(EMPLOYEE_ID), any()))
                    .thenReturn(existing);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> validationService.validateCheckOutApproval(EMPLOYEE_ID, checkOutTime));

            assertTrue(exception.getMessage().contains("cannot be before"));
        }
    }

    // ============= validateLeaveApproval() Tests =============

    @Nested
    @DisplayName("validateLeaveApproval")
    class ValidateLeaveApprovalTests {

        @Test
        @DisplayName("Should pass for new timesheet")
        void shouldPassForNewTimesheet() {
            LeaveDate leaveDate = LeaveDate.builder()
                    .date(TEST_DATE)
                    .shift(ShiftType.FULL_DAY)
                    .build();
            when(dailyTimeSheetDao.getTimesheetByEmployeeAndDate(eq(EMPLOYEE_ID), any()))
                    .thenReturn(null);

            assertDoesNotThrow(() -> validationService.validateLeaveApproval(EMPLOYEE_ID, List.of(leaveDate)));
        }

        @Test
        @DisplayName("Should throw when morning already PRESENT")
        void shouldThrowWhenMorningAlreadyPresent() {
            LeaveDate leaveDate = LeaveDate.builder()
                    .date(TEST_DATE)
                    .shift(ShiftType.MORNING) // Try to set morning leave
                    .build();
            DailyTimeSheet existing = DailyTimeSheet.builder()
                    .date(TEST_DATE)
                    .morningStatus(AttendanceStatus.PRESENT) // Already PRESENT
                    .build();
            when(dailyTimeSheetDao.getTimesheetByEmployeeAndDate(eq(EMPLOYEE_ID), any()))
                    .thenReturn(existing);

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> validationService.validateLeaveApproval(EMPLOYEE_ID, List.of(leaveDate)));

            assertTrue(exception.getMessage().contains("Conflict"));
        }

        @Test
        @DisplayName("Should pass for afternoon leave when morning is PRESENT")
        void shouldPassForAfternoonLeaveWhenMorningPresent() {
            LeaveDate leaveDate = LeaveDate.builder()
                    .date(TEST_DATE)
                    .shift(ShiftType.AFTERNOON) // Afternoon leave
                    .build();
            DailyTimeSheet existing = DailyTimeSheet.builder()
                    .date(TEST_DATE)
                    .morningStatus(AttendanceStatus.PRESENT) // Morning worked
                    .afternoonStatus(null) // Afternoon available
                    .build();
            when(dailyTimeSheetDao.getTimesheetByEmployeeAndDate(eq(EMPLOYEE_ID), any()))
                    .thenReturn(existing);

            assertDoesNotThrow(() -> validationService.validateLeaveApproval(EMPLOYEE_ID, List.of(leaveDate)));
        }

        @Test
        @DisplayName("Should throw for full-day leave when any shift has status")
        void shouldThrowForFullDayLeaveWhenPartialStatus() {
            LeaveDate leaveDate = LeaveDate.builder()
                    .date(TEST_DATE)
                    .shift(ShiftType.FULL_DAY) // Full day leave
                    .build();
            DailyTimeSheet existing = DailyTimeSheet.builder()
                    .date(TEST_DATE)
                    .morningStatus(AttendanceStatus.PRESENT) // Morning worked
                    .build();
            when(dailyTimeSheetDao.getTimesheetByEmployeeAndDate(eq(EMPLOYEE_ID), any()))
                    .thenReturn(existing);

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> validationService.validateLeaveApproval(EMPLOYEE_ID, List.of(leaveDate)));

            assertTrue(exception.getMessage().contains("Conflict"));
        }
    }

    // ============= validateWfhApproval() Tests =============

    @Nested
    @DisplayName("validateWfhApproval")
    class ValidateWfhApprovalTests {

        @Test
        @DisplayName("Should pass for new timesheet")
        void shouldPassForNewTimesheet() {
            WfhDate wfhDate = WfhDate.builder()
                    .date(TEST_DATE)
                    .shift(ShiftType.FULL_DAY)
                    .build();
            when(dailyTimeSheetDao.getTimesheetByEmployeeAndDate(eq(EMPLOYEE_ID), any()))
                    .thenReturn(null);

            assertDoesNotThrow(() -> validationService.validateWfhApproval(EMPLOYEE_ID, List.of(wfhDate)));
        }

        @Test
        @DisplayName("Should throw when morning is already LEAVE")
        void shouldThrowWhenMorningIsLeave() {
            WfhDate wfhDate = WfhDate.builder()
                    .date(TEST_DATE)
                    .shift(ShiftType.MORNING) // WFH morning
                    .build();
            DailyTimeSheet existing = DailyTimeSheet.builder()
                    .date(TEST_DATE)
                    .morningStatus(AttendanceStatus.LEAVE) // Already leave
                    .build();
            when(dailyTimeSheetDao.getTimesheetByEmployeeAndDate(eq(EMPLOYEE_ID), any()))
                    .thenReturn(existing);

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> validationService.validateWfhApproval(EMPLOYEE_ID, List.of(wfhDate)));

            assertTrue(exception.getMessage().contains("LEAVE"));
        }

        @Test
        @DisplayName("Should pass for afternoon WFH when morning is LEAVE")
        void shouldPassForAfternoonWfhWhenMorningLeave() {
            WfhDate wfhDate = WfhDate.builder()
                    .date(TEST_DATE)
                    .shift(ShiftType.AFTERNOON) // WFH afternoon
                    .build();
            DailyTimeSheet existing = DailyTimeSheet.builder()
                    .date(TEST_DATE)
                    .morningStatus(AttendanceStatus.LEAVE) // Morning leave is OK
                    .afternoonStatus(null) // Afternoon available
                    .build();
            when(dailyTimeSheetDao.getTimesheetByEmployeeAndDate(eq(EMPLOYEE_ID), any()))
                    .thenReturn(existing);

            assertDoesNotThrow(() -> validationService.validateWfhApproval(EMPLOYEE_ID, List.of(wfhDate)));
        }
    }

    // ============= validateTimesheetUpdateApproval() Tests =============

    @Nested
    @DisplayName("validateTimesheetUpdateApproval")
    class ValidateTimesheetUpdateApprovalTests {

        @Test
        @DisplayName("Should throw when timesheet not found")
        void shouldThrowWhenTimesheetNotFound() {
            when(dailyTimeSheetDao.getTimesheetByEmployeeAndDate(eq(EMPLOYEE_ID), any()))
                    .thenReturn(null);

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> validationService.validateTimesheetUpdateApproval(
                            EMPLOYEE_ID, TEST_DATE,
                            LocalDateTime.of(2024, 12, 15, 8, 0),
                            LocalDateTime.of(2024, 12, 15, 17, 0),
                            null, null, null, null));

            assertTrue(exception.getMessage().contains("No timesheet found"));
        }

        @Test
        @DisplayName("Should throw when timesheet is finalized")
        void shouldThrowWhenFinalized() {
            DailyTimeSheet existing = DailyTimeSheet.builder()
                    .date(TEST_DATE)
                    .isFinalized(true) // Finalized
                    .build();
            when(dailyTimeSheetDao.getTimesheetByEmployeeAndDate(eq(EMPLOYEE_ID), any()))
                    .thenReturn(existing);

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> validationService.validateTimesheetUpdateApproval(
                            EMPLOYEE_ID, TEST_DATE, null, null, null, null, null, null));

            assertTrue(exception.getMessage().contains("finalized"));
        }

        @Test
        @DisplayName("Should throw when PRESENT status without check-in time")
        void shouldThrowWhenPresentWithoutCheckIn() {
            DailyTimeSheet existing = DailyTimeSheet.builder()
                    .date(TEST_DATE)
                    .isFinalized(false)
                    .build();
            when(dailyTimeSheetDao.getTimesheetByEmployeeAndDate(eq(EMPLOYEE_ID), any()))
                    .thenReturn(existing);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> validationService.validateTimesheetUpdateApproval(
                            EMPLOYEE_ID, TEST_DATE,
                            null, // No check-in
                            null,
                            AttendanceStatus.PRESENT, // Morning PRESENT
                            null,
                            false, // Not WFH
                            null));

            assertTrue(exception.getMessage().contains("Check-in time is required"));
        }

        @Test
        @DisplayName("Should pass when PRESENT status with WFH flag")
        void shouldPassWhenPresentWithWfh() {
            DailyTimeSheet existing = DailyTimeSheet.builder()
                    .date(TEST_DATE)
                    .isFinalized(false)
                    .build();
            when(dailyTimeSheetDao.getTimesheetByEmployeeAndDate(eq(EMPLOYEE_ID), any()))
                    .thenReturn(existing);

            // PRESENT + WFH = no check-in required
            assertDoesNotThrow(() -> validationService.validateTimesheetUpdateApproval(
                    EMPLOYEE_ID, TEST_DATE,
                    null, // No check-in
                    null,
                    AttendanceStatus.PRESENT, // Morning PRESENT
                    AttendanceStatus.PRESENT,
                    true, // WFH morning
                    true)); // WFH afternoon
        }

        @Test
        @DisplayName("Should throw for full-day leave with check times")
        void shouldThrowForLeaveWithCheckTimes() {
            DailyTimeSheet existing = DailyTimeSheet.builder()
                    .date(TEST_DATE)
                    .isFinalized(false)
                    .build();
            when(dailyTimeSheetDao.getTimesheetByEmployeeAndDate(eq(EMPLOYEE_ID), any()))
                    .thenReturn(existing);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> validationService.validateTimesheetUpdateApproval(
                            EMPLOYEE_ID, TEST_DATE,
                            LocalDateTime.of(2024, 12, 15, 8, 0), // Has check-in
                            null,
                            AttendanceStatus.LEAVE, // Both LEAVE
                            AttendanceStatus.LEAVE,
                            null, null));

            assertTrue(exception.getMessage().contains("should not be provided"));
        }

        @Test
        @DisplayName("Should throw when check-out before check-in")
        void shouldThrowWhenCheckOutBeforeCheckIn() {
            DailyTimeSheet existing = DailyTimeSheet.builder()
                    .date(TEST_DATE)
                    .isFinalized(false)
                    .build();
            when(dailyTimeSheetDao.getTimesheetByEmployeeAndDate(eq(EMPLOYEE_ID), any()))
                    .thenReturn(existing);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> validationService.validateTimesheetUpdateApproval(
                            EMPLOYEE_ID, TEST_DATE,
                            LocalDateTime.of(2024, 12, 15, 17, 0), // Check-in PM
                            LocalDateTime.of(2024, 12, 15, 8, 0), // Check-out AM
                            null, null, null, null));

            assertTrue(exception.getMessage().contains("after check-in"));
        }
    }

    // ============= Utility Methods Tests =============

    @Nested
    @DisplayName("Utility Methods")
    class UtilityMethodsTests {

        @Test
        @DisplayName("isFullDayLeave should return true when both LEAVE")
        void shouldReturnTrueWhenBothLeave() {
            DailyTimeSheet ts = DailyTimeSheet.builder()
                    .morningStatus(AttendanceStatus.LEAVE)
                    .afternoonStatus(AttendanceStatus.LEAVE)
                    .build();

            assertTrue(validationService.isFullDayLeave(ts));
        }

        @Test
        @DisplayName("isFullDayLeave should return false when only morning LEAVE")
        void shouldReturnFalseWhenOnlyMorningLeave() {
            DailyTimeSheet ts = DailyTimeSheet.builder()
                    .morningStatus(AttendanceStatus.LEAVE)
                    .afternoonStatus(AttendanceStatus.PRESENT)
                    .build();

            assertFalse(validationService.isFullDayLeave(ts));
        }

        @Test
        @DisplayName("hasShiftConflict should return true when morning conflicts")
        void shouldReturnTrueWhenMorningConflicts() {
            DailyTimeSheet ts = DailyTimeSheet.builder()
                    .morningStatus(AttendanceStatus.PRESENT)
                    .build();

            assertTrue(validationService.hasShiftConflict(ts, ShiftType.MORNING, AttendanceStatus.LEAVE));
        }

        @Test
        @DisplayName("hasShiftConflict should return false when no existing status")
        void shouldReturnFalseWhenNoExistingStatus() {
            DailyTimeSheet ts = DailyTimeSheet.builder()
                    .morningStatus(null)
                    .build();

            assertFalse(validationService.hasShiftConflict(ts, ShiftType.MORNING, AttendanceStatus.LEAVE));
        }

        @Test
        @DisplayName("hasShiftConflict should check both for FULL_DAY")
        void shouldCheckBothForFullDay() {
            DailyTimeSheet ts = DailyTimeSheet.builder()
                    .morningStatus(AttendanceStatus.PRESENT)
                    .afternoonStatus(null)
                    .build();

            assertTrue(validationService.hasShiftConflict(ts, ShiftType.FULL_DAY, AttendanceStatus.LEAVE));
        }
    }
}
