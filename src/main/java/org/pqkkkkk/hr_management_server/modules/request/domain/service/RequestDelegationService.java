package org.pqkkkkk.hr_management_server.modules.request.domain.service;

import java.time.LocalDateTime;

import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.Enums.UserRole;
import org.pqkkkkk.hr_management_server.modules.profile.domain.service.ProfileQueryService;
import org.pqkkkkk.hr_management_server.modules.request.domain.dao.RequestDao;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

/**
 * Helper service for handling request delegation logic.
 * This service provides common delegation functionality that is shared across
 * all request types (CHECK_IN, CHECK_OUT, LEAVE, etc.).
 */
@Service
public class RequestDelegationService {
    private final RequestDao requestDao;
    private final ProfileQueryService profileQueryService;

    public RequestDelegationService(RequestDao requestDao, ProfileQueryService profileQueryService) {
        this.requestDao = requestDao;
        this.profileQueryService = profileQueryService;
    }

    /**
     * Delegates a request to a new processor.
     * 
     * @param requestId The ID of the request to delegate
     * @param newProcessorId The ID of the user who will process the request
     * @return The updated request with new processor
     * @throws IllegalArgumentException if request or processor not found
     * @throws IllegalStateException if request status is not PENDING
     * @throws IllegalArgumentException if processor does not have ADMIN or HR role
     */
    @Transactional
    public Request delegateRequest(String requestId, String newProcessorId) {
        // Validate request exists
        Request request = requestDao.getRequestById(requestId);
        if (request == null) {
            throw new IllegalArgumentException("Request not found with ID: " + requestId);
        }

        // Validate request status - only PENDING requests can be delegated
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException(
                "Only pending requests can be delegated. Current status: " + request.getStatus()
            );
        }

        // Validate new processor exists
        User newProcessor = profileQueryService.getProfileById(newProcessorId);
        if (newProcessor == null) {
            throw new IllegalArgumentException("Processor not found with ID: " + newProcessorId);
        }

        // Validate processor role - must be ADMIN or HR
        if (newProcessor.getRole() != UserRole.ADMIN && newProcessor.getRole() != UserRole.HR) {
            throw new IllegalArgumentException(
                "Processor must have ADMIN or HR role. Current role: " + newProcessor.getRole()
            );
        }

        // Update processor
        request.setProcessor(newProcessor);
        request.setUpdatedAt(LocalDateTime.now());

        return request;
    }
}
