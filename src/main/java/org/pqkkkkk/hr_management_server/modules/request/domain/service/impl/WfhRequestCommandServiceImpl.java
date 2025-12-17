package org.pqkkkkk.hr_management_server.modules.request.domain.service.impl;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.service.ProfileQueryService;
import org.pqkkkkk.hr_management_server.modules.request.domain.dao.RequestDao;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.AdditionalWfhInfo;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.WfhDate;
import org.pqkkkkk.hr_management_server.modules.request.domain.event.RequestCreatedEvent;
import org.pqkkkkk.hr_management_server.modules.request.domain.service.WfhRequestCommandService;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
public class WfhRequestCommandServiceImpl implements WfhRequestCommandService {
    private final RequestDao requestDao;
    private final ProfileQueryService profileQueryService;
    private final ApplicationEventPublisher eventPublisher;

    public WfhRequestCommandServiceImpl(RequestDao requestDao, ProfileQueryService profileQueryService,
        ApplicationEventPublisher eventPublisher) {
        this.requestDao = requestDao;
        this.profileQueryService = profileQueryService;
        this.eventPublisher = eventPublisher;
    }


    private void validateRequestInfo(Request request) {
        // Validate request
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null.");
        }

        // validate employee
        if (request.getEmployee() == null || request.getEmployee().getUserId() == null) {
            throw new IllegalArgumentException("Employee information is required for WFH request creation.");
        }
        // validate WFH info
        AdditionalWfhInfo wfhInfo = request.getAdditionalWfhInfo();
        if (wfhInfo == null || wfhInfo.getWfhDates() == null || wfhInfo.getWfhDates().isEmpty()) {
            throw new IllegalArgumentException("WFH dates are required");
        }

        LocalDate today = LocalDate.now();
        Set<LocalDate> dateSet = new HashSet<>();
        
        for (WfhDate wfhDate : wfhInfo.getWfhDates()) {
            LocalDate date = wfhDate.getDate();
            // validate date is within next 30 days
            if (date == null || date.isBefore(today) || date.isAfter(today.plusDays(30))) {
                throw new IllegalArgumentException("Work from home date is invalid: " + date);
            }
            // validate no duplicate dates
            if (!dateSet.add(date)) {
                throw new IllegalArgumentException("Duplicate WFH date: " + date);
            }
        }
    }

    @Override
    @Transactional

    public Request createRequest(Request request) {
        validateRequestInfo(request);

        User employee = profileQueryService.getProfileById(request.getEmployee().getUserId());
        request.setEmployee(employee);
        request.setProcessor(employee.getManager());
        request.setApprover(employee.getManager());

        request.setStatus(RequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        request.setRequestType(RequestType.WFH);

        if (request.getAdditionalWfhInfo() != null) {
            request.getAdditionalWfhInfo().setRequest(request);
        }

        Request createdRequest = requestDao.createRequest(request);
        eventPublisher.publishEvent(new RequestCreatedEvent(this, createdRequest));
        return createdRequest;
    }

    

        @Override
    public Request approveRequest(String requestId, String approverId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'approveRequest'");
    }

    @Override
    public Request rejectRequest(String requestId, String approverId, String rejectionReason) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rejectRequest'");
    }

}
