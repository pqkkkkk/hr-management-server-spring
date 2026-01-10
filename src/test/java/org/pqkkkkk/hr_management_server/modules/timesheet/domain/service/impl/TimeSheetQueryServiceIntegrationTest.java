package org.pqkkkkk.hr_management_server.modules.timesheet.domain.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.pqkkkkk.hr_management_server.modules.timesheet.domain.service.TimeSheetQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for TimeSheetQueryService.getBatchAttendanceStatistics().
 * Sample data loaded from V14__insert_timesheet_sample_data.sql (test profile)
 * 
 * Sample data in V14:
 * - u1a2b3c4-e5f6-7890-abcd-ef1234567890 (User 1): 5 records (Dec 1-5, 2024)
 * - Dec 1: PRESENT/PRESENT, 0 late
 * - Dec 2: PRESENT/PRESENT, 60 late minutes
 * - Dec 3: LEAVE/LEAVE
 * - Dec 4: PRESENT/PRESENT, WFH
 * - Dec 5: PRESENT/PRESENT, finalized
 * - u2b3c4d5-f6a7-8901-bcde-f12345678901 (User 2): 1 record (Dec 1, 2024)
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("TimeSheetQueryService - getBatchAttendanceStatistics() Integration Tests")
class TimeSheetQueryServiceIntegrationTest {

    @Autowired
    private TimeSheetQueryService timeSheetQueryService;

    // Sample user IDs from V14 migration
    private static final String USER_1_ID = "u1a2b3c4-e5f6-7890-abcd-ef1234567890";
    private static final String USER_2_ID = "u2b3c4d5-f6a7-8901-bcde-f12345678901";
    private static final String NON_EXISTENT_USER_ID = "non-existent-user-id-12345";

    @Nested
    @DisplayName("Happy Path Tests")
    class HappyPathTests {

        @Test
        @DisplayName("Should return statistics for single user")
        void shouldReturnStatisticsForSingleUser() {
            // Arrange
            List<String> userIds = Collections.singletonList(USER_1_ID);
            LocalDate startDate = LocalDate.of(2024, 12, 1);
            LocalDate endDate = LocalDate.of(2024, 12, 5);

            // Act
            List<Map<String, Object>> results = timeSheetQueryService.getBatchAttendanceStatistics(
                    userIds, startDate, endDate);

            // Assert
            assertEquals(1, results.size());
            Map<String, Object> stats = results.get(0);

            assertEquals(USER_1_ID, stats.get("userId"));
            assertNotNull(stats.get("totalDays"));
            assertNotNull(stats.get("morningPresent"));
            assertNotNull(stats.get("afternoonPresent"));
            assertNotNull(stats.get("lateDays"));
            assertNotNull(stats.get("totalLateMinutes"));
            assertNotNull(stats.get("totalOvertimeMinutes"));
            assertNotNull(stats.get("totalWorkCredit"));

            // User 1 has 5 records in Dec 1-5
            assertEquals(5L, ((Number) stats.get("totalDays")).longValue());
        }

        @Test
        @DisplayName("Should return statistics for multiple users")
        void shouldReturnStatisticsForMultipleUsers() {
            // Arrange - Both users have data on Dec 1
            List<String> userIds = Arrays.asList(USER_1_ID, USER_2_ID);
            LocalDate startDate = LocalDate.of(2024, 12, 1);
            LocalDate endDate = LocalDate.of(2024, 12, 1);

            // Act
            List<Map<String, Object>> results = timeSheetQueryService.getBatchAttendanceStatistics(
                    userIds, startDate, endDate);

            // Assert
            assertEquals(2, results.size());

            // Verify both users have statistics
            List<String> returnedUserIds = results.stream()
                    .map(r -> (String) r.get("userId"))
                    .toList();
            assertTrue(returnedUserIds.contains(USER_1_ID));
            assertTrue(returnedUserIds.contains(USER_2_ID));
        }

        @Test
        @DisplayName("Should return correct late statistics for user with late day")
        void shouldReturnCorrectLateStatisticsForUserWithLateDay() {
            // Arrange - User 1 has 60 late minutes on Dec 2
            List<String> userIds = Collections.singletonList(USER_1_ID);
            LocalDate startDate = LocalDate.of(2024, 12, 1);
            LocalDate endDate = LocalDate.of(2024, 12, 5);

            // Act
            List<Map<String, Object>> results = timeSheetQueryService.getBatchAttendanceStatistics(
                    userIds, startDate, endDate);

            // Assert
            assertEquals(1, results.size());
            Map<String, Object> stats = results.get(0);

            // User 1 has 1 late day (Dec 2 with 60 minutes late)
            long lateDays = ((Number) stats.get("lateDays")).longValue();
            long totalLateMinutes = ((Number) stats.get("totalLateMinutes")).longValue();

            assertEquals(1L, lateDays, "User 1 should have 1 late day");
            assertEquals(60L, totalLateMinutes, "User 1 should have 60 late minutes");
        }

        @Test
        @DisplayName("Should return correct morning present count")
        void shouldReturnCorrectMorningPresentCount() {
            // Arrange - User 1 has 4 PRESENT mornings (Dec 1, 2, 4, 5) and 1 LEAVE (Dec 3)
            List<String> userIds = Collections.singletonList(USER_1_ID);
            LocalDate startDate = LocalDate.of(2024, 12, 1);
            LocalDate endDate = LocalDate.of(2024, 12, 5);

            // Act
            List<Map<String, Object>> results = timeSheetQueryService.getBatchAttendanceStatistics(
                    userIds, startDate, endDate);

            // Assert
            assertEquals(1, results.size());
            Map<String, Object> stats = results.get(0);

            long morningPresent = ((Number) stats.get("morningPresent")).longValue();
            assertEquals(4L, morningPresent, "User 1 should have 4 morning PRESENT days");
        }
    }

    @Nested
    @DisplayName("Empty and Partial Result Tests")
    class EmptyAndPartialResultTests {

        @Test
        @DisplayName("Should return zero values when no data exists in date range")
        void shouldReturnZeroValuesWhenNoDataExistsInDateRange() {
            // Arrange - Future dates with no data
            List<String> userIds = Arrays.asList(USER_1_ID, USER_2_ID);
            LocalDate startDate = LocalDate.of(2030, 1, 1);
            LocalDate endDate = LocalDate.of(2030, 1, 31);

            // Act
            List<Map<String, Object>> results = timeSheetQueryService.getBatchAttendanceStatistics(
                    userIds, startDate, endDate);

            // Assert - Should return 2 results with zero values
            assertEquals(2, results.size());

            for (Map<String, Object> stats : results) {
                assertEquals(0L, ((Number) stats.get("totalDays")).longValue());
                assertEquals(0L, ((Number) stats.get("morningPresent")).longValue());
                assertEquals(0L, ((Number) stats.get("afternoonPresent")).longValue());
                assertEquals(0L, ((Number) stats.get("lateDays")).longValue());
                assertEquals(0L, ((Number) stats.get("totalLateMinutes")).longValue());
                assertEquals(0L, ((Number) stats.get("totalOvertimeMinutes")).longValue());
                assertEquals(0.0, ((Number) stats.get("totalWorkCredit")).doubleValue(), 0.001);
            }
        }

        @Test
        @DisplayName("Should return zero values for user without data in range")
        void shouldReturnZeroValuesForUserWithoutDataInRange() {
            // Arrange - User 2 only has data on Dec 1, so query Dec 2-5 returns zeros for
            // User 2
            List<String> userIds = Arrays.asList(USER_1_ID, USER_2_ID);
            LocalDate startDate = LocalDate.of(2024, 12, 2);
            LocalDate endDate = LocalDate.of(2024, 12, 5);

            // Act
            List<Map<String, Object>> results = timeSheetQueryService.getBatchAttendanceStatistics(
                    userIds, startDate, endDate);

            // Assert - Both users should have results
            assertEquals(2, results.size());

            // Find USER_2's stats
            Map<String, Object> user2Stats = results.stream()
                    .filter(r -> USER_2_ID.equals(r.get("userId")))
                    .findFirst()
                    .orElseThrow();

            // USER_2 should have zero values
            assertEquals(0L, ((Number) user2Stats.get("totalDays")).longValue());
            assertEquals(0.0, ((Number) user2Stats.get("totalWorkCredit")).doubleValue(), 0.001);
        }

        @Test
        @DisplayName("Should return zero values for non-existent users")
        void shouldReturnZeroValuesForNonExistentUsers() {
            // Arrange - Non-existent users
            List<String> userIds = Arrays.asList(NON_EXISTENT_USER_ID, "another-fake-user-id");
            LocalDate startDate = LocalDate.of(2024, 12, 1);
            LocalDate endDate = LocalDate.of(2024, 12, 5);

            // Act
            List<Map<String, Object>> results = timeSheetQueryService.getBatchAttendanceStatistics(
                    userIds, startDate, endDate);

            // Assert - Both users should have results with zero values
            assertEquals(2, results.size());

            for (Map<String, Object> stats : results) {
                assertEquals(0L, ((Number) stats.get("totalDays")).longValue());
                assertEquals(0.0, ((Number) stats.get("totalWorkCredit")).doubleValue(), 0.001);
            }
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when userIds is null")
        void shouldThrowExceptionWhenUserIdsIsNull() {
            // Arrange
            LocalDate startDate = LocalDate.of(2024, 12, 1);
            LocalDate endDate = LocalDate.of(2024, 12, 31);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> timeSheetQueryService.getBatchAttendanceStatistics(null, startDate, endDate));

            assertTrue(exception.getMessage().contains("null") ||
                    exception.getMessage().contains("empty"));
        }

        @Test
        @DisplayName("Should throw exception when userIds is empty")
        void shouldThrowExceptionWhenUserIdsIsEmpty() {
            // Arrange
            List<String> emptyUserIds = Collections.emptyList();
            LocalDate startDate = LocalDate.of(2024, 12, 1);
            LocalDate endDate = LocalDate.of(2024, 12, 31);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> timeSheetQueryService.getBatchAttendanceStatistics(emptyUserIds, startDate, endDate));

            assertTrue(exception.getMessage().contains("null") ||
                    exception.getMessage().contains("empty"));
        }

        @Test
        @DisplayName("Should throw exception when startDate is null")
        void shouldThrowExceptionWhenStartDateIsNull() {
            // Arrange
            List<String> userIds = Collections.singletonList(USER_1_ID);
            LocalDate endDate = LocalDate.of(2024, 12, 31);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> timeSheetQueryService.getBatchAttendanceStatistics(userIds, null, endDate));

            assertTrue(exception.getMessage().contains("null"));
        }

        @Test
        @DisplayName("Should throw exception when endDate is null")
        void shouldThrowExceptionWhenEndDateIsNull() {
            // Arrange
            List<String> userIds = Collections.singletonList(USER_1_ID);
            LocalDate startDate = LocalDate.of(2024, 12, 1);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> timeSheetQueryService.getBatchAttendanceStatistics(userIds, startDate, null));

            assertTrue(exception.getMessage().contains("null"));
        }

        @Test
        @DisplayName("Should throw exception when startDate is after endDate")
        void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
            // Arrange
            List<String> userIds = Collections.singletonList(USER_1_ID);
            LocalDate startDate = LocalDate.of(2024, 12, 31);
            LocalDate endDate = LocalDate.of(2024, 12, 1);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> timeSheetQueryService.getBatchAttendanceStatistics(userIds, startDate, endDate));

            assertTrue(exception.getMessage().contains("before") ||
                    exception.getMessage().contains("after") ||
                    exception.getMessage().contains("date"));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle single day date range")
        void shouldHandleSingleDayDateRange() {
            // Arrange
            List<String> userIds = Collections.singletonList(USER_1_ID);
            LocalDate singleDate = LocalDate.of(2024, 12, 1);

            // Act
            List<Map<String, Object>> results = timeSheetQueryService.getBatchAttendanceStatistics(
                    userIds, singleDate, singleDate);

            // Assert
            assertEquals(1, results.size());
            Map<String, Object> stats = results.get(0);
            assertEquals(1L, ((Number) stats.get("totalDays")).longValue());
        }

        @Test
        @DisplayName("Should calculate total work credit correctly")
        void shouldCalculateTotalWorkCreditCorrectly() {
            // Arrange - User 1 Dec 1-5:
            // Dec 1: 1.0, Dec 2: 0.889, Dec 3: 0.0 (leave), Dec 4: 1.0 (WFH), Dec 5: 1.0
            // Total = 3.889
            List<String> userIds = Collections.singletonList(USER_1_ID);
            LocalDate startDate = LocalDate.of(2024, 12, 1);
            LocalDate endDate = LocalDate.of(2024, 12, 5);

            // Act
            List<Map<String, Object>> results = timeSheetQueryService.getBatchAttendanceStatistics(
                    userIds, startDate, endDate);

            // Assert
            assertEquals(1, results.size());
            Map<String, Object> stats = results.get(0);

            double totalWorkCredit = ((Number) stats.get("totalWorkCredit")).doubleValue();
            assertEquals(3.889, totalWorkCredit, 0.01);
        }

        @Test
        @DisplayName("Should handle maximum allowed userIds (100)")
        void shouldHandleMaximumAllowedUserIds() {
            // Arrange - 100 user IDs (most won't have data but should return zero values)
            List<String> userIds = IntStream.range(0, 100)
                    .mapToObj(i -> "user-" + i)
                    .toList();
            LocalDate startDate = LocalDate.of(2024, 12, 1);
            LocalDate endDate = LocalDate.of(2024, 12, 31);

            // Act - Should not throw exception
            List<Map<String, Object>> results = timeSheetQueryService.getBatchAttendanceStatistics(
                    userIds, startDate, endDate);

            // Assert - Should return 100 results with zero values
            assertEquals(100, results.size());

            // All should have zero values since none exist in sample data
            for (Map<String, Object> stats : results) {
                assertEquals(0L, ((Number) stats.get("totalDays")).longValue());
            }
        }
    }
}
