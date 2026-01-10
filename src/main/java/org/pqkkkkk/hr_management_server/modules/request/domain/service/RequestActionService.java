package org.pqkkkkk.hr_management_server.modules.request.domain.service;

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.BulkApproveResult;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Request;
import org.pqkkkkk.hr_management_server.modules.request.domain.filter.FilterCriteria.RequestFilter;

/**
 * Service interface for handling common request actions across all request
 * types.
 * <p>
 * This service provides unified methods for approve, reject actions that work
 * with any request type (CHECK_IN, CHECK_OUT, LEAVE, etc.).
 * <p>
 * It delegates the actual business logic to the appropriate
 * RequestCommandService
 * implementation based on the request type.
 */
public interface RequestActionService {

    /**
     * Approves a request regardless of its type.
     * <p>
     * This method will:
     * 1. Retrieve the request by ID to determine its type
     * 2. Find the appropriate RequestCommandService implementation
     * 3. Delegate the approval to that service
     * 
     * @param requestId  The ID of the request to approve
     * @param approverId The ID of the user approving the request
     * @return The approved request entity
     * @throws IllegalArgumentException if request not found or no service supports
     *                                  the request type
     * @throws IllegalStateException    if the request cannot be approved (invalid
     *                                  status, etc.)
     */
    Request approve(String requestId, String approverId);

    /**
     * Rejects a request regardless of its type.
     * <p>
     * This method will:
     * 1. Retrieve the request by ID to determine its type
     * 2. Find the appropriate RequestCommandService implementation
     * 3. Delegate the rejection to that service
     * 
     * @param requestId       The ID of the request to reject
     * @param rejecterId      The ID of the user rejecting the request
     * @param rejectionReason The reason for rejection
     * @return The rejected request entity
     * @throws IllegalArgumentException if request not found or no service supports
     *                                  the request type
     * @throws IllegalStateException    if the request cannot be rejected (invalid
     *                                  status, etc.)
     */
    Request reject(String requestId, String rejecterId, String rejectionReason);

    /**
     * Delegates a request to a new processor.
     * <p>
     * This allows managers/HR to reassign a pending request to another
     * authorized processor (ADMIN or HR role).
     * 
     * @param requestId      The ID of the request to delegate
     * @param newProcessorId The ID of the user who will process the request
     * @return The delegated request entity
     * @throws IllegalArgumentException if request or processor not found
     * @throws IllegalStateException    if request status is not PENDING
     * @throws IllegalArgumentException if processor does not have ADMIN or HR role
     */
    Request delegate(String requestId, String newProcessorId);

    /**
     * Bulk approve all pending requests matching the filter.
     * <p>
     * Uses partial success strategy - continues approving even if some fail.
     * The filter should have status=PENDING to only get pending requests.
     * 
     * @param filter      Filter criteria for selecting requests
     * @param approverId  The ID of the user approving
     * @param maxRequests Maximum number of requests to process (rate limiting)
     * @return BulkApproveResult containing success/failure details
     */
    BulkApproveResult bulkApprove(RequestFilter filter, String approverId, int maxRequests);
}
