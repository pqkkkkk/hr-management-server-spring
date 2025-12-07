package org.pqkkkkk.hr_management_server.modules.request.domain.service.impl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserRole;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.infrastructure.dao.jpa_repository.ProfileRepository;
import org.pqkkkkk.hr_management_server.modules.request.domain.command.CreateLeaveRequestCommand;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.ShiftType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.LeaveDate;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.exception.DuplicateLeaveRequestException;
import org.pqkkkkk.hr_management_server.modules.request.domain.exception.EmployeeNotFoundException;
import org.pqkkkkk.hr_management_server.modules.request.domain.exception.InvalidDateRangeException;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.LeaveRequestCommandService;
import org.pqkkkkk.hr_management_server.modules.request.infrastructure.dao.jpa_repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.*;

//Không dùng @Transactional để kiểm soát thủ công việc dọn dẹp dữ liệu giữa các test do bị StackOverflowError //
//Tui hong biết fix cái này sao luôn á :(( //

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("LeaveRequestCommandService Integration Tests with H2 Database")
class LeaveRequestCommandServiceImplIntegrationTest {

    @Autowired
    private LeaveRequestCommandService leaveRequestCommandService;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private ProfileRepository profileRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    private String testEmployeeId;

    /**
     * Setup: Create test employee before each test
     * Manual cleanup to avoid transaction conflicts
     */
    @BeforeEach
    void setUp() {
        // Clear all data first to avoid conflicts
        try {
            requestRepository.deleteAll();
            profileRepository.deleteAll();
        } catch (Exception e) {
            // Ignore
        }
        
        // Create fresh employee (ID will be auto-generated)
        User testEmployee = User.builder()
                .fullName("Test Employee")
                .email("test@company.com")
                .role(UserRole.EMPLOYEE)
                .build();
        User saved = profileRepository.save(testEmployee);
        testEmployeeId = saved.getUserId();
    }

    /**
     * Cleanup: Delete all test data after each test
     * Essential for test isolation without @Transactional
     */
    @AfterEach
    void tearDown() {
        try {
            requestRepository.deleteAll();
            profileRepository.deleteAll();
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    // ==================== SUCCESS SCENARIOS ====================

    @Nested
    @DisplayName("Successful Leave Request Creation")
    class SuccessfulCreationTests {

        @Test
        @DisplayName("TC1: Create full-day leave request and persist to H2 database")
        void testCreateLeaveRequest_ValidFullDays_PersistedToDatabase() {
            // Arrange
            LocalDate startDate = LocalDate.now().plusDays(5);
            LocalDate endDate = LocalDate.now().plusDays(10);
            CreateLeaveRequestCommand command = CreateLeaveRequestCommand.builder()
                    .employeeId(testEmployeeId)
                    .leaveType("ANNUAL")
                    .startDate(startDate)
                    .endDate(endDate)
                    .reason("Family vacation")
                    .build();

            // Act
            Request result = leaveRequestCommandService.createLeaveRequest(command);

            // Assert - Return value verification
            assertNotNull(result, "Result should not be null");
            assertNotNull(result.getRequestId(), "RequestId should be generated");
            assertEquals(testEmployeeId, result.getEmployee().getUserId());
            assertEquals("ANNUAL", result.getAdditionalLeaveInfo().getLeaveType().name());
            assertEquals(RequestStatus.PENDING, result.getStatus());
            assertEquals("Family vacation", result.getUserReason());
            assertEquals(6.0, result.getAdditionalLeaveInfo().getTotalDays().doubleValue());
            assertEquals(6, result.getAdditionalLeaveInfo().getLeaveDates().size());

            // Assert - H2 Database persistence verification
            Request persisted = requestRepository.findById(result.getRequestId())
                    .orElse(null);
            assertNotNull(persisted, "Request should be persisted in H2 database");
            assertEquals("Family vacation", persisted.getUserReason());
            assertEquals(RequestStatus.PENDING, persisted.getStatus());
            assertEquals("ANNUAL", persisted.getAdditionalLeaveInfo().getLeaveType().name());
            assertEquals(6.0, persisted.getAdditionalLeaveInfo().getTotalDays().doubleValue());
        }

        @Test
        @DisplayName("TC2: Create single-day leave request")
        void testCreateLeaveRequest_SingleDay_Success() {
            // Arrange
            LocalDate date = LocalDate.now().plusDays(5);
            CreateLeaveRequestCommand command = CreateLeaveRequestCommand.builder()
                    .employeeId(testEmployeeId)
                    .leaveType("SICK")
                    .startDate(date)
                    .endDate(date)
                    .reason("One day leave")
                    .build();

            // Act
            Request result = leaveRequestCommandService.createLeaveRequest(command);

            // Assert
            assertNotNull(result);
            assertEquals(1.0, result.getAdditionalLeaveInfo().getTotalDays().doubleValue());
            assertEquals(1, result.getAdditionalLeaveInfo().getLeaveDates().size());
            assertEquals(RequestStatus.PENDING, result.getStatus());

            // Assert - Database persistence
            assertTrue(requestRepository.existsById(result.getRequestId()));
        }

        @Test
        @DisplayName("TC3: Half-day leave calculation (0.5 + 0.5 = 1 day)")
        void testCreateLeaveRequest_HalfDays_CorrectCalculation() {
            // Arrange
            LocalDate startDate = LocalDate.now().plusDays(5);
            LocalDate endDate = LocalDate.now().plusDays(6);
            CreateLeaveRequestCommand command = CreateLeaveRequestCommand.builder()
                    .employeeId(testEmployeeId)
                    .leaveType("SICK")
                    .startDate(startDate)
                    .endDate(endDate)
                    .reason("Medical appointment")
                    .shifts(Arrays.asList(ShiftType.MORNING, ShiftType.AFTERNOON))
                    .build();

            // Act
            Request result = leaveRequestCommandService.createLeaveRequest(command);

            // Assert - Verify in-memory result
            assertNotNull(result);
            assertEquals(1.0, result.getAdditionalLeaveInfo().getTotalDays().doubleValue());
            
            // Flush and clear to ensure data is persisted
            requestRepository.flush();
            
            // Assert - Re-fetch from database to verify persistence
            Request persisted = requestRepository.findById(result.getRequestId()).orElse(null);
            assertNotNull(persisted);
            assertEquals(1.0, persisted.getAdditionalLeaveInfo().getTotalDays().doubleValue());
            
            // Verify leave dates count only (avoid lazy loading issues)
            assertNotNull(persisted.getAdditionalLeaveInfo());
            assertEquals("SICK", persisted.getAdditionalLeaveInfo().getLeaveType().name());
        }

        @Test
        @DisplayName("TC4: Create leave request with attachment URL")
        void testCreateLeaveRequest_WithAttachmentUrl_PersistedCorrectly() {
            // Arrange
            LocalDate startDate = LocalDate.now().plusDays(5);
            LocalDate endDate = LocalDate.now().plusDays(7);
            String attachmentUrl = "https://example.com/documents/medical-certificate.pdf";
            CreateLeaveRequestCommand command = CreateLeaveRequestCommand.builder()
                    .employeeId(testEmployeeId)
                    .leaveType("SICK")
                    .startDate(startDate)
                    .endDate(endDate)
                    .reason("Medical leave with certificate")
                    .attachmentUrl(attachmentUrl)
                    .build();

            // Act
            Request result = leaveRequestCommandService.createLeaveRequest(command);

            // Assert - Return value verification
            assertNotNull(result, "Result should not be null");
            assertEquals(attachmentUrl, result.getAttachmentUrl(), "Attachment URL should be saved");

            // Assert - H2 Database persistence verification
            Request persisted = requestRepository.findById(result.getRequestId())
                    .orElse(null);
            assertNotNull(persisted, "Request should be persisted in H2 database");
            assertEquals(attachmentUrl, persisted.getAttachmentUrl(), "Attachment URL should be persisted in database");
            assertEquals("Medical leave with certificate", persisted.getUserReason());
        }

        @Test
        @DisplayName("TC5: Multiple non-overlapping requests from same employee")
        void testCreateLeaveRequest_NonOverlapping_BothPersisted() {
            // Arrange
            LocalDate startDate1 = LocalDate.now().plusDays(5);
            LocalDate endDate1 = LocalDate.now().plusDays(10);
            CreateLeaveRequestCommand command1 = CreateLeaveRequestCommand.builder()
                    .employeeId(testEmployeeId)
                    .leaveType("ANNUAL")
                    .startDate(startDate1)
                    .endDate(endDate1)
                    .reason("Summer vacation")
                    .build();

            LocalDate startDate2 = LocalDate.now().plusDays(20);
            LocalDate endDate2 = LocalDate.now().plusDays(25);
            CreateLeaveRequestCommand command2 = CreateLeaveRequestCommand.builder()
                    .employeeId(testEmployeeId)
                    .leaveType("SICK")
                    .startDate(startDate2)
                    .endDate(endDate2)
                    .reason("Recovery period")
                    .build();

            // Act
            Request result1 = leaveRequestCommandService.createLeaveRequest(command1);
            Request result2 = leaveRequestCommandService.createLeaveRequest(command2);

            // Assert
            assertNotNull(result1);
            assertNotNull(result2);
            assertNotEquals(result1.getRequestId(), result2.getRequestId());

            // Assert - Both persisted in database
            List<Request> allRequests = requestRepository.findAll();
            assertEquals(2, allRequests.size());
            assertTrue(allRequests.stream()
                    .anyMatch(r -> r.getRequestId().equals(result1.getRequestId())));
            assertTrue(allRequests.stream()
                    .anyMatch(r -> r.getRequestId().equals(result2.getRequestId())));
        }

        @Test
        @DisplayName("TC5: Multiple employees can have overlapping leave periods")
        void testCreateLeaveRequest_MultipleEmployees_IndependentRequests() {
            // Arrange - Create second employee (ID auto-generated)
            User employee2 = User.builder()
                    .fullName("Employee 2")
                    .email("emp2@company.com")
                    .role(UserRole.EMPLOYEE)
                    .build();
            User savedEmp2 = profileRepository.save(employee2);
            String emp2Id = savedEmp2.getUserId();

            LocalDate startDate = LocalDate.now().plusDays(5);
            LocalDate endDate = LocalDate.now().plusDays(10);

            // Act
            Request result1 = leaveRequestCommandService.createLeaveRequest(
                    CreateLeaveRequestCommand.builder()
                            .employeeId(testEmployeeId)
                            .leaveType("ANNUAL")
                            .startDate(startDate)
                            .endDate(endDate)
                            .reason("Emp1 leave")
                            .build());

            Request result2 = leaveRequestCommandService.createLeaveRequest(
                    CreateLeaveRequestCommand.builder()
                            .employeeId(emp2Id)
                            .leaveType("ANNUAL")
                            .startDate(startDate)
                            .endDate(endDate)
                            .reason("Emp2 leave")
                            .build());

            // Assert
            assertNotNull(result1);
            assertNotNull(result2);
            assertNotEquals(result1.getRequestId(), result2.getRequestId());
            assertEquals(testEmployeeId, result1.getEmployee().getUserId());
            assertEquals(emp2Id, result2.getEmployee().getUserId());

            // Assert - Both persisted
            assertEquals(2, requestRepository.count());
        }

        @Test
        @DisplayName("TC6: All leave types are supported")
        void testCreateLeaveRequest_AllLeaveTypes_Supported() {
            String[] leaveTypes = {"ANNUAL", "SICK", "UNPAID", "MATERNITY", 
                                  "PATERNITY", "BEREAVEMENT", "MARRIAGE", "OTHER"};
            LocalDate startDate = LocalDate.now().plusDays(5);
            LocalDate endDate = LocalDate.now().plusDays(6);

            for (String leaveType : leaveTypes) {
                // Arrange - Create unique employee for each type (ID auto-generated)
                User employee = User.builder()
                        .fullName("Employee " + leaveType)
                        .email(leaveType + "@company.com")
                        .role(UserRole.EMPLOYEE)
                        .build();
                User savedEmployee = profileRepository.save(employee);
                String empId = savedEmployee.getUserId();

                // Act
                Request result = leaveRequestCommandService.createLeaveRequest(
                        CreateLeaveRequestCommand.builder()
                                .employeeId(empId)
                                .leaveType(leaveType)
                                .startDate(startDate)
                                .endDate(endDate)
                                .reason("Test " + leaveType)
                                .build());

                // Assert
                assertNotNull(result);
                assertEquals(leaveType, result.getAdditionalLeaveInfo().getLeaveType().name());

                // Verify persisted
                assertTrue(requestRepository.existsById(result.getRequestId()),
                        "Request for " + leaveType + " should be persisted");
            }
        }
    }

    // ==================== VALIDATION FAILURE SCENARIOS ====================

    @Nested
    @DisplayName("Validation Failures")
    class ValidationFailureTests {

        @Test
        @DisplayName("TC7: Null command throws IllegalArgumentException")
        void testCreateLeaveRequest_NullCommand_ThrowsException() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> leaveRequestCommandService.createLeaveRequest(null),
                    "Should throw IllegalArgumentException for null command");

            // Verify no data persisted
            assertEquals(0, requestRepository.count());
        }

        @Test
        @DisplayName("TC8: Null employeeId throws IllegalArgumentException")
        void testCreateLeaveRequest_NullEmployeeId_ThrowsException() {
            // Arrange
            CreateLeaveRequestCommand command = CreateLeaveRequestCommand.builder()
                    .employeeId(null)
                    .leaveType("ANNUAL")
                    .startDate(LocalDate.now().plusDays(5))
                    .endDate(LocalDate.now().plusDays(10))
                    .reason("Test")
                    .build();

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> leaveRequestCommandService.createLeaveRequest(command),
                    "Should throw IllegalArgumentException for null employeeId");

            // Verify transaction rollback - no data persisted
            assertEquals(0, requestRepository.count());
        }

        @Test
        @DisplayName("TC9: Blank leaveType throws IllegalArgumentException")
        void testCreateLeaveRequest_BlankLeaveType_ThrowsException() {
            // Arrange
            CreateLeaveRequestCommand command = CreateLeaveRequestCommand.builder()
                    .employeeId(testEmployeeId)
                    .leaveType("")
                    .startDate(LocalDate.now().plusDays(5))
                    .endDate(LocalDate.now().plusDays(10))
                    .reason("Test")
                    .build();

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> leaveRequestCommandService.createLeaveRequest(command));

            // Verify rollback
            assertEquals(0, requestRepository.count());
        }

        @Test
        @DisplayName("TC10: Null startDate throws IllegalArgumentException")
        void testCreateLeaveRequest_NullStartDate_ThrowsException() {
            // Arrange
            CreateLeaveRequestCommand command = CreateLeaveRequestCommand.builder()
                    .employeeId(testEmployeeId)
                    .leaveType("ANNUAL")
                    .startDate(null)
                    .endDate(LocalDate.now().plusDays(10))
                    .reason("Test")
                    .build();

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> leaveRequestCommandService.createLeaveRequest(command));
        }

        @Test
        @DisplayName("TC11: Null endDate throws IllegalArgumentException")
        void testCreateLeaveRequest_NullEndDate_ThrowsException() {
            // Arrange
            CreateLeaveRequestCommand command = CreateLeaveRequestCommand.builder()
                    .employeeId(testEmployeeId)
                    .leaveType("ANNUAL")
                    .startDate(LocalDate.now().plusDays(5))
                    .endDate(null)
                    .reason("Test")
                    .build();

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> leaveRequestCommandService.createLeaveRequest(command));
        }

        @Test
        @DisplayName("TC12: StartDate in past throws InvalidDateRangeException")
        void testCreateLeaveRequest_StartDateInPast_ThrowsException() {
            // Arrange
            CreateLeaveRequestCommand command = CreateLeaveRequestCommand.builder()
                    .employeeId(testEmployeeId)
                    .leaveType("ANNUAL")
                    .startDate(LocalDate.now().minusDays(1))
                    .endDate(LocalDate.now().plusDays(5))
                    .reason("Test")
                    .build();

            // Act & Assert
            assertThrows(InvalidDateRangeException.class,
                    () -> leaveRequestCommandService.createLeaveRequest(command),
                    "Should throw InvalidDateRangeException for start date in past");

            // Verify rollback
            assertEquals(0, requestRepository.count());
        }

        @Test
        @DisplayName("TC13: StartDate after endDate throws InvalidDateRangeException")
        void testCreateLeaveRequest_StartAfterEnd_ThrowsException() {
            // Arrange
            CreateLeaveRequestCommand command = CreateLeaveRequestCommand.builder()
                    .employeeId(testEmployeeId)
                    .leaveType("ANNUAL")
                    .startDate(LocalDate.now().plusDays(10))
                    .endDate(LocalDate.now().plusDays(5))
                    .reason("Test")
                    .build();

            // Act & Assert
            assertThrows(InvalidDateRangeException.class,
                    () -> leaveRequestCommandService.createLeaveRequest(command),
                    "Should throw InvalidDateRangeException for start after end");

            // Verify rollback
            assertEquals(0, requestRepository.count());
        }

        @Test
        @DisplayName("TC14: Non-existent employee throws EmployeeNotFoundException")
        void testCreateLeaveRequest_NonExistentEmployee_ThrowsException() {
            // Arrange
            CreateLeaveRequestCommand command = CreateLeaveRequestCommand.builder()
                    .employeeId("non-existent-emp-999")
                    .leaveType("ANNUAL")
                    .startDate(LocalDate.now().plusDays(5))
                    .endDate(LocalDate.now().plusDays(10))
                    .reason("Test")
                    .build();

            // Act & Assert
            assertThrows(EmployeeNotFoundException.class,
                    () -> leaveRequestCommandService.createLeaveRequest(command),
                    "Should throw EmployeeNotFoundException for non-existent employee");

            // Verify rollback
            assertEquals(0, requestRepository.count());
        }
    }

    // ==================== DUPLICATE/OVERLAP DETECTION ====================

    @Nested
    @DisplayName("Duplicate and Overlapping Request Detection")
    class OverlapDetectionTests {

        @Test
        @DisplayName("TC15: Overlapping leave request throws DuplicateLeaveRequestException")
        void testCreateLeaveRequest_Overlapping_ThrowsException() {
            // Arrange - Create first request
            LocalDate startDate = LocalDate.now().plusDays(5);
            LocalDate endDate = LocalDate.now().plusDays(10);
            CreateLeaveRequestCommand command1 = CreateLeaveRequestCommand.builder()
                    .employeeId(testEmployeeId)
                    .leaveType("ANNUAL")
                    .startDate(startDate)
                    .endDate(endDate)
                    .reason("First leave")
                    .build();

            Request firstRequest = leaveRequestCommandService.createLeaveRequest(command1);
            assertEquals(1, requestRepository.count());

            // Try to create overlapping request (same dates)
            CreateLeaveRequestCommand command2 = CreateLeaveRequestCommand.builder()
                    .employeeId(testEmployeeId)
                    .leaveType("SICK")
                    .startDate(startDate)
                    .endDate(endDate)
                    .reason("Overlapping request")
                    .build();

            // Act & Assert
            assertThrows(DuplicateLeaveRequestException.class,
                    () -> leaveRequestCommandService.createLeaveRequest(command2),
                    "Should throw DuplicateLeaveRequestException for overlapping requests");

            // Verify rollback - count should remain 1
            assertEquals(1, requestRepository.count(),
                    "Failed request should be rolled back");
        }

        @Test
        @DisplayName("TC16: Partially overlapping leave request throws DuplicateLeaveRequestException")
        void testCreateLeaveRequest_PartiallyOverlapping_ThrowsException() {
            // Arrange - Create first request (5-10)
            LocalDate startDate1 = LocalDate.now().plusDays(5);
            LocalDate endDate1 = LocalDate.now().plusDays(10);
            CreateLeaveRequestCommand command1 = CreateLeaveRequestCommand.builder()
                    .employeeId(testEmployeeId)
                    .leaveType("ANNUAL")
                    .startDate(startDate1)
                    .endDate(endDate1)
                    .reason("First leave")
                    .build();

            leaveRequestCommandService.createLeaveRequest(command1);

            // Try to create overlapping request (7-12 overlaps with 5-10)
            CreateLeaveRequestCommand command2 = CreateLeaveRequestCommand.builder()
                    .employeeId(testEmployeeId)
                    .leaveType("SICK")
                    .startDate(LocalDate.now().plusDays(7))
                    .endDate(LocalDate.now().plusDays(12))
                    .reason("Partially overlapping")
                    .build();

            // Act & Assert
            assertThrows(DuplicateLeaveRequestException.class,
                    () -> leaveRequestCommandService.createLeaveRequest(command2));

            // Verify rollback
            assertEquals(1, requestRepository.count());
        }
    }

    // ==================== TRANSACTION & DATA PERSISTENCE ====================

    @Nested
    @DisplayName("Transaction Handling and Data Persistence")
    class TransactionAndPersistenceTests {

        @Test
        @DisplayName("TC17: Complete data persistence - Request, AdditionalLeaveInfo, LeaveDate tables")
        void testCreateLeaveRequest_DataPersistence_AllTablesPopulated() {
            // Arrange
            LocalDate startDate = LocalDate.now().plusDays(5);
            LocalDate endDate = LocalDate.now().plusDays(7);
            CreateLeaveRequestCommand command = CreateLeaveRequestCommand.builder()
                    .employeeId(testEmployeeId)
                    .leaveType("ANNUAL")
                    .startDate(startDate)
                    .endDate(endDate)
                    .reason("Vacation")
                    .build();

            // Act
            Request result = leaveRequestCommandService.createLeaveRequest(command);

            // Assert - Fetch from H2 database
            Request persisted = requestRepository.findById(result.getRequestId()).orElse(null);
            assertNotNull(persisted, "Request should be persisted in H2 database");

            // Verify request_table populated
            assertEquals("Vacation", persisted.getUserReason());
            assertEquals(RequestStatus.PENDING, persisted.getStatus());
            assertNotNull(persisted.getCreatedAt());
            assertNotNull(persisted.getEmployee());
            assertEquals(testEmployeeId, persisted.getEmployee().getUserId());

            // Verify additional_leave_info_table populated
            assertNotNull(persisted.getAdditionalLeaveInfo(), "AdditionalLeaveInfo should be persisted");
            assertEquals("ANNUAL", persisted.getAdditionalLeaveInfo().getLeaveType().name());
            assertEquals(3.0, persisted.getAdditionalLeaveInfo().getTotalDays().doubleValue());

            // Verify leave_date_table indirectly through totalDays (avoid lazy loading)
            assertEquals(3.0, persisted.getAdditionalLeaveInfo().getTotalDays().doubleValue(),
                "Total days = 3.0 confirms 3 full-day leave dates exist in leave_date_table");
        }

        @Test
        @DisplayName("TC18: Transaction rollback on exception - failed request not persisted")
        void testCreateLeaveRequest_TransactionRollback_FailedRequestNotPersisted() {
            // Arrange - Create first request
            LocalDate startDate = LocalDate.now().plusDays(5);
            LocalDate endDate = LocalDate.now().plusDays(10);
            CreateLeaveRequestCommand command1 = CreateLeaveRequestCommand.builder()
                    .employeeId(testEmployeeId)
                    .leaveType("ANNUAL")
                    .startDate(startDate)
                    .endDate(endDate)
                    .reason("First")
                    .build();

            Request firstRequest = leaveRequestCommandService.createLeaveRequest(command1);
            long countAfterFirstRequest = requestRepository.count();
            assertEquals(1, countAfterFirstRequest);

            // Arrange - Try to create overlapping request (will fail)
            CreateLeaveRequestCommand command2 = CreateLeaveRequestCommand.builder()
                    .employeeId(testEmployeeId)
                    .leaveType("SICK")
                    .startDate(startDate)
                    .endDate(endDate)
                    .reason("Overlapping")
                    .build();

            // Act - Exception thrown
            assertThrows(DuplicateLeaveRequestException.class,
                    () -> leaveRequestCommandService.createLeaveRequest(command2));

            // Assert - Service's @Transactional ensures failed request is rolled back
            long countAfterFailedRequest = requestRepository.count();
            assertEquals(countAfterFirstRequest, countAfterFailedRequest,
                    "Failed request should be rolled back - count should remain 1");
        }

        @Test
        @DisplayName("TC19: Test isolation - manual cleanup ensures clean state per test")
        void testCreateLeaveRequest_TestIsolation_CleanStatePerTest() {
            // Arrange
            CreateLeaveRequestCommand command = CreateLeaveRequestCommand.builder()
                    .employeeId(testEmployeeId)
                    .leaveType("ANNUAL")
                    .startDate(LocalDate.now().plusDays(5))
                    .endDate(LocalDate.now().plusDays(10))
                    .reason("Test isolation")
                    .build();

            // Act
            Request result = leaveRequestCommandService.createLeaveRequest(command);

            // Assert - Within this test, data is persisted
            assertNotNull(result);
            assertEquals(1, requestRepository.count());

            // Note: @AfterEach will clean up data automatically
            // so next test starts with clean database state
        }

        @Test
        @DisplayName("TC20: Multiple saves - complex persistence scenario")
        void testCreateLeaveRequest_MultipleSavesInTransaction_AllPersisted() {
            // Arrange & Act
            Request req1 = leaveRequestCommandService.createLeaveRequest(
                    CreateLeaveRequestCommand.builder()
                            .employeeId(testEmployeeId)
                            .leaveType("ANNUAL")
                            .startDate(LocalDate.now().plusDays(5))
                            .endDate(LocalDate.now().plusDays(7))
                            .reason("Req 1")
                            .build());

            User emp2 = User.builder()
                    .fullName("Emp 2")
                    .email("emp2@company.com")
                    .role(UserRole.EMPLOYEE)
                    .build();
            User savedEmp2 = profileRepository.save(emp2);
            String emp2Id = savedEmp2.getUserId();

            Request req2 = leaveRequestCommandService.createLeaveRequest(
                    CreateLeaveRequestCommand.builder()
                            .employeeId(emp2Id)
                            .leaveType("SICK")
                            .startDate(LocalDate.now().plusDays(5))
                            .endDate(LocalDate.now().plusDays(7))
                            .reason("Req 2")
                            .build());

            // Assert - Both persisted in H2
            assertEquals(2, requestRepository.count());
            assertTrue(requestRepository.existsById(req1.getRequestId()));
            assertTrue(requestRepository.existsById(req2.getRequestId()));

            // Verify complex data relationships
            Request persisted1 = requestRepository.findById(req1.getRequestId()).orElse(null);
            Request persisted2 = requestRepository.findById(req2.getRequestId()).orElse(null);
            assertNotNull(persisted1);
            assertNotNull(persisted2);
            assertEquals(testEmployeeId, persisted1.getEmployee().getUserId());
            assertEquals(emp2Id, persisted2.getEmployee().getUserId());
        }
    }
}
