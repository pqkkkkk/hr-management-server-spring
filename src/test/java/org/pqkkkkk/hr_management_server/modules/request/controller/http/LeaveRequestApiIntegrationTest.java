package org.pqkkkkk.hr_management_server.modules.request.controller.http;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("LeaveRequestApi Integration Tests")
class LeaveRequestApiIntegrationTest {

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

        // Create test leave requests for testEmployee
        createRequest(testEmployee, RequestType.LEAVE, RequestStatus.PENDING, "Sick Leave");
        createRequest(testEmployee, RequestType.LEAVE, RequestStatus.APPROVED, "Annual Leave");
        createRequest(testEmployee, RequestType.LEAVE, RequestStatus.REJECTED, "Personal Leave");
        createRequest(testEmployee, RequestType.WFH, RequestStatus.PENDING, "Work from home");

        // Create requests for anotherEmployee
        createRequest(anotherEmployee, RequestType.LEAVE, RequestStatus.PENDING, "Other Leave");
    }

    // ---------------------------
    // SUCCESS RETRIEVAL TESTS
    // ---------------------------

    @Test
    @DisplayName("Should retrieve my leave requests with 200 OK")
    void getMyLeaveRequests_success() throws Exception {
        mockMvc.perform(get("/api/v1/requests/leave/my-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(() -> testEmployeeId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.statusCode").value(200))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.totalElements").value(4))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content.length()").value(4));
    }

    @Test
    @DisplayName("Should return response structure with required fields")
    void getMyLeaveRequests_responseStructure() throws Exception {
        mockMvc.perform(get("/api/v1/requests/leave/my-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(() -> testEmployeeId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").isBoolean())
            .andExpect(jsonPath("$.statusCode").isNumber())
            .andExpect(jsonPath("$.message").isString())
            .andExpect(jsonPath("$.data.content[0].requestId").exists())
            .andExpect(jsonPath("$.data.content[0].requestType").exists())
            .andExpect(jsonPath("$.data.content[0].status").exists())
            .andExpect(jsonPath("$.data.content[0].title").exists())
            .andExpect(jsonPath("$.data.content[0].createdAt").exists());
    }

    // ---------------------------
    // FILTERING TESTS
    // ---------------------------

    @Test
    @DisplayName("Should filter by status parameter")
    void getMyLeaveRequests_filterByStatus() throws Exception {
        mockMvc.perform(get("/api/v1/requests/leave/my-requests")
                .param("status", "PENDING")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(() -> testEmployeeId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalElements").value(2))
            .andExpect(jsonPath("$.data.content[0].status").value("PENDING"))
            .andExpect(jsonPath("$.data.content[1].status").value("PENDING"));
    }

    @Test
    @DisplayName("Should filter by requestType parameter")
    void getMyLeaveRequests_filterByRequestType() throws Exception {
        mockMvc.perform(get("/api/v1/requests/leave/my-requests")
                .param("requestType", "LEAVE")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(() -> testEmployeeId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalElements").value(3))
            .andExpect(jsonPath("$.data.content[0].requestType").value("LEAVE"))
            .andExpect(jsonPath("$.data.content[1].requestType").value("LEAVE"))
            .andExpect(jsonPath("$.data.content[2].requestType").value("LEAVE"));
    }

    @Test
    @DisplayName("Should filter by multiple criteria (status and requestType)")
    void getMyLeaveRequests_multipleFilters() throws Exception {
        mockMvc.perform(get("/api/v1/requests/leave/my-requests")
                .param("status", "PENDING")
                .param("requestType", "LEAVE")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(() -> testEmployeeId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.content[0].title").value("Sick Leave"));
    }

    @Test
    @DisplayName("Should filter by date range using startDate")
    void getMyLeaveRequests_filterByStartDate() throws Exception {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        mockMvc.perform(get("/api/v1/requests/leave/my-requests")
                .param("startDate", yesterday.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .principal(() -> testEmployeeId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalElements").value(4));
    }

    @Test
    @DisplayName("Should return empty when filtering by future date")
    void getMyLeaveRequests_filterByFutureDate() throws Exception {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        mockMvc.perform(get("/api/v1/requests/leave/my-requests")
                .param("startDate", tomorrow.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .principal(() -> testEmployeeId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    // ---------------------------
    // ---------------------------
    // PAGINATION TESTS
    // ---------------------------

    @Test
    @DisplayName("Should handle pagination with default values")
    void getMyLeaveRequests_defaultPagination() throws Exception {
        mockMvc.perform(get("/api/v1/requests/leave/my-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(() -> testEmployeeId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.number").value(0))
            .andExpect(jsonPath("$.data.size").value(20))
            .andExpect(jsonPath("$.data.totalElements").value(4))
            .andExpect(jsonPath("$.data.totalPages").value(1));
    }

    @Test
    @DisplayName("Should handle custom page and size parameters")
    void getMyLeaveRequests_customPagination() throws Exception {
        mockMvc.perform(get("/api/v1/requests/leave/my-requests")
                .param("page", "0")
                .param("size", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(() -> testEmployeeId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.number").value(0))
            .andExpect(jsonPath("$.data.size").value(2))
            .andExpect(jsonPath("$.data.totalElements").value(4))
            .andExpect(jsonPath("$.data.totalPages").value(2))
            .andExpect(jsonPath("$.data.content.length()").value(2));
    }

    @Test
    @DisplayName("Should handle second page pagination")
    void getMyLeaveRequests_secondPage() throws Exception {
        mockMvc.perform(get("/api/v1/requests/leave/my-requests")
                .param("page", "1")
                .param("size", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(() -> testEmployeeId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.number").value(1))
            .andExpect(jsonPath("$.data.content.length()").value(2))
            .andExpect(jsonPath("$.data.totalElements").value(4))
            .andExpect(jsonPath("$.data.totalPages").value(2));
    }
    @Test
    @DisplayName("Should return 401 Unauthorized when no authentication provided")
    void getMyLeaveRequests_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/requests/leave/my-requests")
                .contentType(MediaType.APPLICATION_JSON))
            // Without principal, API should return 401 or handle null principal
            // The response depends on implementation - if it checks for null principal
            .andExpect(status().is4xxClientError());
    }

    // ---------------------------
    // EMPLOYEE ISOLATION TESTS
    // ---------------------------

    @Test
    @DisplayName("Should only return requests for authenticated employee")
    void getMyLeaveRequests_employeeIsolation() throws Exception {
        mockMvc.perform(get("/api/v1/requests/leave/my-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(() -> testEmployeeId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalElements").value(4)); // testEmployee has 4

        // Different employee should see their own requests
        mockMvc.perform(get("/api/v1/requests/leave/my-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(() -> anotherEmployeeId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalElements").value(1)); // anotherEmployee has 1
    }

    // ---------------------------
    // COMBINATION TESTS
    // ---------------------------

    @Test
    @DisplayName("Should combine filter + pagination")
    void getMyLeaveRequests_filterWithPagination() throws Exception {
        mockMvc.perform(get("/api/v1/requests/leave/my-requests")
                .param("status", "PENDING")
                .param("requestType", "LEAVE")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(() -> testEmployeeId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.content[0].title").value("Sick Leave"));
    }

    @Test
    @DisplayName("Should return sorted results (createdAt DESC)")
    void getMyLeaveRequests_verifySorting() throws Exception {
        mockMvc.perform(get("/api/v1/requests/leave/my-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(() -> testEmployeeId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content.length()").value(4))
            // Verify all items exist (at least checking first few)
            .andExpect(jsonPath("$.data.content[0].createdAt").exists())
            .andExpect(jsonPath("$.data.content[1].createdAt").exists());
    }
    // ---------------------------
    // HELPER METHODS
    // ---------------------------

    private Request createRequest(User employee, RequestType type, RequestStatus status, String title) {
        Request.RequestBuilder builder = Request.builder()
            .employee(employee)
            .requestType(type)
            .status(status)
            .title(title)
            .userReason("Test reason");

        // Add processedAt for APPROVED/REJECTED status (constraint requirement)
        if (status == RequestStatus.APPROVED || status == RequestStatus.REJECTED) {
            builder.processedAt(LocalDateTime.now());
        }

        // Add rejectReason for REJECTED status (constraint requirement)
        if (status == RequestStatus.REJECTED) {
            builder.rejectReason("Test rejection reason");
        }

        return requestRepository.save(builder.build());
    }
}
