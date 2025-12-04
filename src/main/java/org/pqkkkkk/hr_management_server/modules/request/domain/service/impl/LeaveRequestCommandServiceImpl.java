package org.pqkkkkk.hr_management_server.modules.request.domain.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.infrastructure.dao.jpa_repository.ProfileRepository;
import org.pqkkkkk.hr_management_server.modules.request.domain.command.CreateLeaveRequestCommand;
import org.pqkkkkk.hr_management_server.modules.request.domain.dao.EmployeeDao;
import org.pqkkkkk.hr_management_server.modules.request.domain.dao.LeaveRequestDao;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.AdditionalLeaveInfo;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.LeaveType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.ShiftType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.LeaveDate;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.exception.DuplicateLeaveRequestException;
import org.pqkkkkk.hr_management_server.modules.request.domain.exception.EmployeeNotFoundException;
import org.pqkkkkk.hr_management_server.modules.request.domain.exception.InvalidDateRangeException;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.LeaveRequestCommandService;
import org.pqkkkkk.hr_management_server.modules.request.infrastructure.dao.jpa_repository.RequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaveRequestCommandServiceImpl implements LeaveRequestCommandService {

    private final RequestRepository requestRepository;
    private final LeaveRequestDao leaveRequestDao;
    private final EmployeeDao employeeDao;
    private final ProfileRepository profileRepository;

    public LeaveRequestCommandServiceImpl(
            RequestRepository requestRepository,
            LeaveRequestDao leaveRequestDao,
            EmployeeDao employeeDao,
            ProfileRepository profileRepository) {
        this.requestRepository = requestRepository;
        this.leaveRequestDao = leaveRequestDao;
        this.employeeDao = employeeDao;
        this.profileRepository = profileRepository;
    }

    @Override
    @Transactional
    public Request createLeaveRequest(CreateLeaveRequestCommand command) {
        // Validate input
        validateCommand(command);
        validateDateRange(command.getStartDate(), command.getEndDate());
        validateEmployeeExists(command.getEmployeeId());
        checkOverlappingRequests(command.getEmployeeId(), command.getStartDate(), command.getEndDate());

        // Fetch employee
        User employee = profileRepository.findById(command.getEmployeeId())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        // Create Request entity
        Request request = Request.builder()
                .requestType(RequestType.LEAVE)
                .status(RequestStatus.PENDING)
                .title("Leave Request: " + command.getLeaveType())
                .userReason(command.getReason())
                .employee(employee)
                .build();

        // Calculate total days and create leave dates
        List<LeaveDate> leaveDates = new ArrayList<>();
        double totalDays = 0.0;
        LocalDate current = command.getStartDate();
        int shiftIndex = 0;

        while (!current.isAfter(command.getEndDate())) {
            ShiftType shift = (command.getShifts() != null && shiftIndex < command.getShifts().size())
                    ? command.getShifts().get(shiftIndex)
                    : ShiftType.FULL_DAY;

            totalDays += (shift == ShiftType.FULL_DAY) ? 1.0 : 0.5;

            LeaveDate leaveDate = LeaveDate.builder()
                    .date(current)
                    .shift(shift)
                    .build();
            leaveDates.add(leaveDate);

            current = current.plusDays(1);
            shiftIndex++;
        }

        // Create AdditionalLeaveInfo
        AdditionalLeaveInfo leaveInfo = AdditionalLeaveInfo.builder()
                .request(request)
                .leaveType(LeaveType.valueOf(command.getLeaveType()))
                .totalDays(BigDecimal.valueOf(totalDays))
                .leaveDates(new ArrayList<>())
                .build();

        // Link leave dates to additional info
        for (LeaveDate leaveDate : leaveDates) {
            leaveInfo.addLeaveDate(leaveDate);
        }

        // Set additional info to request
        request.setAdditionalLeaveInfo(leaveInfo);

        // Save (cascade will save additional info and leave dates)
        return requestRepository.save(request);
    }

    private void validateCommand(CreateLeaveRequestCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        if (isBlank(command.getEmployeeId())) {
            throw new IllegalArgumentException("Employee ID is required");
        }
        if (isBlank(command.getLeaveType())) {
            throw new IllegalArgumentException("Leave type is required");
        }
        if (command.getStartDate() == null) {
            throw new IllegalArgumentException("Start date is required");
        }
        if (command.getEndDate() == null) {
            throw new IllegalArgumentException("End date is required");
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException("Start date must be before or equal to end date");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new InvalidDateRangeException("Start date cannot be in the past");
        }
    }

    private void validateEmployeeExists(String employeeId) {
        if (!employeeDao.existsById(employeeId)) {
            throw new EmployeeNotFoundException("Employee with ID " + employeeId + " not found");
        }
    }

    private void checkOverlappingRequests(String employeeId, LocalDate startDate, LocalDate endDate) {
        List<String> overlappingIds = leaveRequestDao.findOverlappingRequestIds(employeeId, startDate, endDate);
        if (!overlappingIds.isEmpty()) {
            throw new DuplicateLeaveRequestException("Employee already has a leave request for the selected date range");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
