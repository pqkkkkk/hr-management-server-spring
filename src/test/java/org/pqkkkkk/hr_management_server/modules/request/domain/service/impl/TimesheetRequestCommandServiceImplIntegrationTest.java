package org.pqkkkkk.hr_management_server.modules.request.domain.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.AdditionalTimesheetInfo;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TimesheetRequestCommandServiceImplIntegrationTest {

    @Autowired
    private TimesheetRequestCommandServiceImpl timesheetRequestCommandService;

    @Test
    @DisplayName("Tạo request thành công")
    void testCreateTimesheetUpdateRequest_Success() {
        // Arrange
        User employee = User.builder().userId("u1a2b3c4-e5f6-7890-abcd-ef1234567890").build();
        AdditionalTimesheetInfo info = AdditionalTimesheetInfo.builder()
                .targetDate(LocalDate.now())
                .desiredCheckInTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 0)))
                .desiredCheckOutTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(17, 0)))
                .build();
        Request request = Request.builder()
                .employee(employee)
                .userReason("Lý do hợp lệ cho timesheet update")
                .additionalTimesheetInfo(info)
                .build();
        info.setRequest(request);

        // Act
        Request created = timesheetRequestCommandService.createRequest(request);

        // Assert
        assertNotNull(created.getRequestId());
        // Assert
        assertEquals(RequestType.TIMESHEET, created.getRequestType());
        // Assert
        assertEquals(RequestStatus.PENDING, created.getStatus());
        // Assert
        assertEquals("Lý do hợp lệ cho timesheet update", created.getUserReason());
    }

    @Test
    @DisplayName("Work date quá 7 ngày → throw exception")
    void testCreateTimesheetUpdateRequest_WorkDateTooOld() {
        // Arrange
        User employee = User.builder().userId("u1a2b3c4-e5f6-7890-abcd-ef1234567890").build();
        AdditionalTimesheetInfo info = AdditionalTimesheetInfo.builder()
                .targetDate(LocalDate.now().minusDays(8))
                .desiredCheckInTime(LocalDateTime.of(LocalDate.now().minusDays(8), LocalTime.of(8, 0)))
                .desiredCheckOutTime(LocalDateTime.of(LocalDate.now().minusDays(8), LocalTime.of(17, 0)))
                .build();
        Request request = Request.builder()
                .employee(employee)
                .userReason("Lý do hợp lệ cho timesheet update")
                .additionalTimesheetInfo(info)
                .build();
        info.setRequest(request);

                // Act
                Exception ex = assertThrows(IllegalArgumentException.class, () -> {
                        timesheetRequestCommandService.createRequest(request);
                });
                // Assert
                assertTrue(ex.getMessage().contains("Work date is invalid"));
    }

    @Test
    @DisplayName("requestedCheckIn > requestedCheckOut hoặc ngoài giờ làm việc")
    void testCreateTimesheetUpdateRequest_InvalidTimes() {
        // Arrange
        User employee = User.builder().userId("u1a2b3c4-e5f6-7890-abcd-ef1234567890").build();
        // Check-in sau check-out
        AdditionalTimesheetInfo info1 = AdditionalTimesheetInfo.builder()
                .targetDate(LocalDate.now())
                .desiredCheckInTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(18, 0)))
                .desiredCheckOutTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 0)))
                .build();
        Request request1 = Request.builder()
                .employee(employee)
                .userReason("Lý do hợp lệ cho timesheet update")
                .additionalTimesheetInfo(info1)
                .build();
        info1.setRequest(request1);

                // Act
                Exception ex1 = assertThrows(IllegalArgumentException.class, () -> {
                        timesheetRequestCommandService.createRequest(request1);
                });
                // Assert
                assertTrue(ex1.getMessage().contains("Check-in must be before check-out"));

                // Arrange (Ngoài giờ làm việc: check-in < 6:00)
                AdditionalTimesheetInfo info2 = AdditionalTimesheetInfo.builder()
                        .targetDate(LocalDate.now())
                        .desiredCheckInTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(5, 0)))
                        .desiredCheckOutTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 0)))
                        .build();
                Request request2 = Request.builder()
                        .employee(employee)
                        .userReason("Lý do hợp lệ cho timesheet update")
                        .additionalTimesheetInfo(info2)
                        .build();
                info2.setRequest(request2);

                // Act
                Exception ex2 = assertThrows(IllegalArgumentException.class, () -> {
                    timesheetRequestCommandService.createRequest(request2);
                });
                // Assert
                assertTrue(ex2.getMessage().contains("Requested times must be within working hours"));

                // Arrange (Ngoài giờ làm việc: check-out > 22:00)
                AdditionalTimesheetInfo info3 = AdditionalTimesheetInfo.builder()
                        .targetDate(LocalDate.now())
                        .desiredCheckInTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 0)))
                        .desiredCheckOutTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 0)))
                        .build();
                Request request3 = Request.builder()
                        .employee(employee)
                        .userReason("Lý do hợp lệ cho timesheet update")
                        .additionalTimesheetInfo(info3)
                        .build();
                info3.setRequest(request3);

                // Act
                Exception ex3 = assertThrows(IllegalArgumentException.class, () -> {
                    timesheetRequestCommandService.createRequest(request3);
                });
                // Assert
                assertTrue(ex3.getMessage().contains("Requested times must be within working hours"));
    }

    @Test
    @DisplayName("Không có timesheet record cho ngày đó")
    void testCreateTimesheetUpdateRequest_TimesheetNotFound() {
        // Arrange
        // Giả sử service sẽ throw exception nếu không tìm thấy timesheet
        User employee = User.builder().userId("not_exist").build();
        AdditionalTimesheetInfo info = AdditionalTimesheetInfo.builder()
                .targetDate(LocalDate.now())
                .desiredCheckInTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 0)))
                .desiredCheckOutTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(17, 0)))
                .build();
        Request request = Request.builder()
                .employee(employee)
                .userReason("Lý do hợp lệ cho timesheet update")
                .additionalTimesheetInfo(info)
                .build();
        info.setRequest(request);

                // Act
                Exception ex = assertThrows(Exception.class, () -> {
                        timesheetRequestCommandService.createRequest(request);
                });
                // Assert
                assertTrue(ex.getMessage().contains("not found") || ex.getMessage().contains("does not exist"));
    }
}
