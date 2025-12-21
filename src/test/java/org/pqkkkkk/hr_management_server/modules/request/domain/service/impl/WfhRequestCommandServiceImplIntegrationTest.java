package org.pqkkkkk.hr_management_server.modules.request.domain.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.AdditionalWfhInfo;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.WfhDate;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;

import java.time.LocalDate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WfhRequestCommandServiceImplIntegrationTest {
    @Autowired
    private WfhRequestCommandServiceImpl service;

    @Test
    @DisplayName("Tạo request thành công với status PENDING")
    void testCreateRemoteWorkRequest_Success() {
        // Arrange
        User employee = User.builder().userId("u1a2b3c4-e5f6-7890-abcd-ef1234567890").build();
        WfhDate wfhDate = WfhDate.builder().date(LocalDate.now().plusDays(1)).build();
        AdditionalWfhInfo wfhInfo = AdditionalWfhInfo.builder().wfhDates(List.of(wfhDate)).build();
        Request request = Request.builder()
                .employee(employee)
                .userReason("Làm việc remote do có việc cá nhân")
                .additionalWfhInfo(wfhInfo)
                .build();
        wfhInfo.setRequest(request);

        // Act
        Request created = service.createRequest(request);

        // Assert
        assertNotNull(created.getRequestId());
        assertEquals(RequestType.WFH, created.getRequestType());
        assertEquals(RequestStatus.PENDING, created.getStatus());
        assertEquals("Làm việc remote do có việc cá nhân", created.getUserReason());
    }

    @Test
    @DisplayName("Employee không tồn tại, throw IllegalArgumentException")
    void testCreateRemoteWorkRequest_EmployeeNotFound() {
        // Arrange
        User employee = User.builder().userId("not_exist").build();
        WfhDate wfhDate = WfhDate.builder().date(LocalDate.now().plusDays(1)).build();
        AdditionalWfhInfo wfhInfo = AdditionalWfhInfo.builder().wfhDates(List.of(wfhDate)).build();

        Request request = Request.builder()
                .employee(employee)
                .userReason("Làm việc remote do có việc cá nhân")
                .additionalWfhInfo(wfhInfo)
                .build();
        wfhInfo.setRequest(request);

        // Act & Assert
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            service.createRequest(request);
        });
        assertTrue(ex.getMessage().toLowerCase().contains("not exist") || ex.getMessage().toLowerCase().contains("not found"));
    }

    @Test
    @DisplayName("WFH date trong quá khứ, throw IllegalArgumentException")
    void testCreateRemoteWorkRequest_PastDate() {
        // Arrange
        User employee = User.builder().userId("u1a2b3c4-e5f6-7890-abcd-ef1234567890").build();
        WfhDate wfhDate = WfhDate.builder().date(LocalDate.now().minusDays(1)).build();
        AdditionalWfhInfo wfhInfo = AdditionalWfhInfo.builder().wfhDates(List.of(wfhDate)).build();
        Request request = Request.builder()
                .employee(employee)
                .userReason("Làm việc remote do có việc cá nhân")
                .additionalWfhInfo(wfhInfo)
                .build();
        wfhInfo.setRequest(request);

        // Act & Assert
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            service.createRequest(request);
        });
        assertTrue(ex.getMessage().toLowerCase().contains("invalid"));
    }

    @Test
    @DisplayName("WFH date bị trùng, throw IllegalArgumentException")
    void testCreateRemoteWorkRequest_OverlappingRequest() {
        // Arrange
        User employee = User.builder().userId("u1a2b3c4-e5f6-7890-abcd-ef1234567890").build();
        LocalDate date = LocalDate.now().plusDays(2);
        WfhDate wfhDate1 = WfhDate.builder().date(date).build();
        WfhDate wfhDate2 = WfhDate.builder().date(date).build();
        AdditionalWfhInfo wfhInfo = AdditionalWfhInfo.builder().wfhDates(List.of(wfhDate1, wfhDate2)).build();
        Request request = Request.builder()
                .employee(employee)
                .userReason("Làm việc remote do có việc cá nhân")
                .additionalWfhInfo(wfhInfo)
                .build();
        wfhInfo.setRequest(request);

        // Act & Assert
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            service.createRequest(request);
        });
        assertTrue(ex.getMessage().toLowerCase().contains("duplicate"));
    }
}
