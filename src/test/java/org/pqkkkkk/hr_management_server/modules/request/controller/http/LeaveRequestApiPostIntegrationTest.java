package org.pqkkkkk.hr_management_server.modules.request.controller.http;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.infrastructure.dao.jpa_repository.ProfileRepository;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.infrastructure.dao.jpa_repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("LeaveRequestApi POST /api/v1/requests/leave Integration Tests")
class LeaveRequestApiPostIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private ProfileRepository profileRepository;

    private User testEmployee;
    private User anotherEmployee;
    private String testEmployeeId;
    private String anotherEmployeeId;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Clean up
        requestRepository.deleteAll();
        profileRepository.deleteAll();

        // Create test employees
        testEmployee = User.builder()
            .email("test.employee@example.com")
            .fullName("Test Employee")
            .build();
        testEmployee = profileRepository.save(testEmployee);
        testEmployeeId = testEmployee.getUserId();

        anotherEmployee = User.builder()
            .email("another.employee@example.com")
            .fullName("Another Employee")
            .build();
        anotherEmployee = profileRepository.save(anotherEmployee);
        anotherEmployeeId = anotherEmployee.getUserId();
    }

    // =============== SUCCESS TESTS ===============

    @Test
    @DisplayName("Should create leave request successfully with 201 Created")
    void createLeaveRequest_success_withAllFields() throws Exception {
        var body = new java.util.HashMap<String, Object>();
        body.put("leaveType", "ANNUAL");
        body.put("startDate", java.time.LocalDate.now().plusDays(5).toString());
        body.put("endDate", java.time.LocalDate.now().plusDays(7).toString());
        body.put("reason", "Family vacation");
        body.put("shifts", java.util.List.of("FULL_DAY"));
        body.put("attachmentUrl", "http://example.com/file.pdf");

        mockMvc.perform(post("/api/v1/requests/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .principal(() -> testEmployeeId))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.statusCode").value(201))
            .andExpect(jsonPath("$.message").value("Leave request created successfully"))
            .andExpect(jsonPath("$.data.requestId").exists())
            .andExpect(jsonPath("$.data.status").value("PENDING"))
            .andExpect(jsonPath("$.data.userReason").value("Family vacation"))
            .andExpect(jsonPath("$.data.attachmentUrl").value("http://example.com/file.pdf"));
    }

    @Test
    @DisplayName("Should create leave request successfully without optional attachmentUrl")
    void createLeaveRequest_success_withoutAttachmentUrl() throws Exception {
        var body = new java.util.HashMap<String, Object>();
        body.put("leaveType", "SICK");
        body.put("startDate", java.time.LocalDate.now().plusDays(5).toString());
        body.put("endDate", java.time.LocalDate.now().plusDays(6).toString());
        body.put("reason", "Medical appointment");

        mockMvc.perform(post("/api/v1/requests/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .principal(() -> testEmployeeId))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.requestId").exists())
            .andExpect(jsonPath("$.data.status").value("PENDING"))
            .andExpect(jsonPath("$.data.userReason").value("Medical appointment"));
    }

    @Test
    @DisplayName("Should verify response structure for created request")
    void createLeaveRequest_responseStructure() throws Exception {
        var body = new java.util.HashMap<String, Object>();
        body.put("leaveType", "ANNUAL");
        body.put("startDate", java.time.LocalDate.now().plusDays(5).toString());
        body.put("endDate", java.time.LocalDate.now().plusDays(6).toString());
        body.put("reason", "Vacation");

        mockMvc.perform(post("/api/v1/requests/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .principal(() -> testEmployeeId))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.statusCode").isNumber())
            .andExpect(jsonPath("$.message").isString())
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.requestId").isString())
            .andExpect(jsonPath("$.data.requestType").isString())
            .andExpect(jsonPath("$.data.status").isString())
            .andExpect(jsonPath("$.data.title").isString())
            .andExpect(jsonPath("$.data.userReason").isString());
    }

    // =============== VALIDATION ERROR TESTS ===============

    @Test
    @DisplayName("Should return 400 for missing required field leaveType")
    void createLeaveRequest_validationError_missingLeaveType() throws Exception {
        var body = new java.util.HashMap<String, Object>();
        body.put("startDate", java.time.LocalDate.now().plusDays(5).toString());
        body.put("endDate", java.time.LocalDate.now().plusDays(6).toString());
        body.put("reason", "No leave type");

        mockMvc.perform(post("/api/v1/requests/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .principal(() -> testEmployeeId))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.statusCode").value(400))
            .andExpect(jsonPath("$.error.error").exists());
    }

    @Test
    @DisplayName("Should return 400 for missing required field startDate")
    void createLeaveRequest_validationError_missingStartDate() throws Exception {
        var body = new java.util.HashMap<String, Object>();
        body.put("leaveType", "ANNUAL");
        body.put("endDate", java.time.LocalDate.now().plusDays(6).toString());
        body.put("reason", "No start date");

        mockMvc.perform(post("/api/v1/requests/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .principal(() -> testEmployeeId))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.error").exists());
    }

    @Test
    @DisplayName("Should return 400 for missing required field endDate")
    void createLeaveRequest_validationError_missingEndDate() throws Exception {
        var body = new java.util.HashMap<String, Object>();
        body.put("leaveType", "ANNUAL");
        body.put("startDate", java.time.LocalDate.now().plusDays(5).toString());
        body.put("reason", "No end date");

        mockMvc.perform(post("/api/v1/requests/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .principal(() -> testEmployeeId))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.error").exists());
    }

    @Test
    @DisplayName("Should return 400 for blank leaveType")
    void createLeaveRequest_validationError_blankLeaveType() throws Exception {
        var body = new java.util.HashMap<String, Object>();
        body.put("leaveType", "   ");
        body.put("startDate", java.time.LocalDate.now().plusDays(5).toString());
        body.put("endDate", java.time.LocalDate.now().plusDays(6).toString());
        body.put("reason", "Blank leave type");

        mockMvc.perform(post("/api/v1/requests/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .principal(() -> testEmployeeId))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    // =============== AUTHENTICATION TESTS ===============

    @Test
    @DisplayName("Should return 401 Unauthorized when no authentication provided")
    void createLeaveRequest_unauthorized_noPrincipal() throws Exception {
        var body = new java.util.HashMap<String, Object>();
        body.put("leaveType", "ANNUAL");
        body.put("startDate", java.time.LocalDate.now().plusDays(5).toString());
        body.put("endDate", java.time.LocalDate.now().plusDays(6).toString());
        body.put("reason", "Unauthorized test");

        mockMvc.perform(post("/api/v1/requests/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().is4xxClientError());
    }

    // =============== DATE RANGE TESTS ===============

    @Test
    @DisplayName("Should return 400 for invalid date range (startDate after endDate)")
    void createLeaveRequest_invalidDateRange_startAfterEnd() throws Exception {
        var body = new java.util.HashMap<String, Object>();
        body.put("leaveType", "ANNUAL");
        body.put("startDate", java.time.LocalDate.now().plusDays(10).toString());
        body.put("endDate", java.time.LocalDate.now().plusDays(5).toString());
        body.put("reason", "Invalid dates");

        mockMvc.perform(post("/api/v1/requests/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .principal(() -> testEmployeeId))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.statusCode").value(400))
            .andExpect(jsonPath("$.error.error").exists());
    }

    @Test
    @DisplayName("Should return 400 for past date (startDate in the past)")
    void createLeaveRequest_invalidDateRange_pastDate() throws Exception {
        var body = new java.util.HashMap<String, Object>();
        body.put("leaveType", "ANNUAL");
        body.put("startDate", java.time.LocalDate.now().minusDays(5).toString());
        body.put("endDate", java.time.LocalDate.now().minusDays(3).toString());
        body.put("reason", "Past dates");

        mockMvc.perform(post("/api/v1/requests/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .principal(() -> testEmployeeId))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.error").exists());
    }

    // =============== EMPLOYEE NOT FOUND TEST ===============

    @Test
    @DisplayName("Should return 404 or 400 when employee not found")
    void createLeaveRequest_employeeNotFound() throws Exception {
        var body = new java.util.HashMap<String, Object>();
        body.put("leaveType", "ANNUAL");
        body.put("startDate", java.time.LocalDate.now().plusDays(5).toString());
        body.put("endDate", java.time.LocalDate.now().plusDays(6).toString());
        body.put("reason", "Employee not found test");

        mockMvc.perform(post("/api/v1/requests/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .principal(() -> "non-existent-employee-id"))
            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.error").exists());
    }

    // =============== DUPLICATE REQUEST TEST ===============

    @Test
    @DisplayName("Should return 409 or 400 for duplicate/overlapping leave request")
    void createLeaveRequest_duplicateRequest_overlappingDates() throws Exception {
        LocalDate startDate = java.time.LocalDate.now().plusDays(5);
        LocalDate endDate = java.time.LocalDate.now().plusDays(7);

        // Create first request
        var body1 = new java.util.HashMap<String, Object>();
        body1.put("leaveType", "ANNUAL");
        body1.put("startDate", startDate.toString());
        body1.put("endDate", endDate.toString());
        body1.put("reason", "First vacation");

        mockMvc.perform(post("/api/v1/requests/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body1))
                .principal(() -> testEmployeeId))
            .andExpect(status().isCreated());

        // Try to create overlapping request
        var body2 = new java.util.HashMap<String, Object>();
        body2.put("leaveType", "SICK");
        body2.put("startDate", startDate.plusDays(1).toString());
        body2.put("endDate", endDate.toString());
        body2.put("reason", "Second vacation overlapping");

        mockMvc.perform(post("/api/v1/requests/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body2))
                .principal(() -> testEmployeeId))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.error").exists());
    }

    @Test
    @DisplayName("Should allow non-overlapping requests from same employee")
    void createLeaveRequest_nonOverlappingRequests() throws Exception {
        // Create first request
        var body1 = new java.util.HashMap<String, Object>();
        body1.put("leaveType", "ANNUAL");
        body1.put("startDate", java.time.LocalDate.now().plusDays(5).toString());
        body1.put("endDate", java.time.LocalDate.now().plusDays(7).toString());
        body1.put("reason", "First vacation");

        mockMvc.perform(post("/api/v1/requests/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body1))
                .principal(() -> testEmployeeId))
            .andExpect(status().isCreated());

        // Create non-overlapping request
        var body2 = new java.util.HashMap<String, Object>();
        body2.put("leaveType", "SICK");
        body2.put("startDate", java.time.LocalDate.now().plusDays(20).toString());
        body2.put("endDate", java.time.LocalDate.now().plusDays(22).toString());
        body2.put("reason", "Second vacation non-overlapping");

        mockMvc.perform(post("/api/v1/requests/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body2))
                .principal(() -> testEmployeeId))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should isolate requests by employee (different employees can have overlapping requests)")
    void createLeaveRequest_differentEmployeesOverlappingDates() throws Exception {
        LocalDate startDate = java.time.LocalDate.now().plusDays(5);
        LocalDate endDate = java.time.LocalDate.now().plusDays(7);

        // Create request for testEmployee
        var body1 = new java.util.HashMap<String, Object>();
        body1.put("leaveType", "ANNUAL");
        body1.put("startDate", startDate.toString());
        body1.put("endDate", endDate.toString());
        body1.put("reason", "Employee 1 vacation");

        mockMvc.perform(post("/api/v1/requests/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body1))
                .principal(() -> testEmployeeId))
            .andExpect(status().isCreated());

        // Create request for anotherEmployee with same dates (should succeed)
        var body2 = new java.util.HashMap<String, Object>();
        body2.put("leaveType", "ANNUAL");
        body2.put("startDate", startDate.toString());
        body2.put("endDate", endDate.toString());
        body2.put("reason", "Employee 2 vacation same dates");

        mockMvc.perform(post("/api/v1/requests/leave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body2))
                .principal(() -> anotherEmployeeId))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true));
    }
}
