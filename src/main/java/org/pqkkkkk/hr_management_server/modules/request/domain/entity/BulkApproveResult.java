package org.pqkkkkk.hr_management_server.modules.request.domain.entity;

import java.util.List;

/**
 * Domain result record to hold the bulk approve operation results.
 * Used by RequestActionService to return results to the controller.
 */
public record BulkApproveResult(
        int totalProcessed,
        int successCount,
        List<String> approvedRequestIds,
        List<FailedApproval> failedApprovals) {

    /**
     * Record for a single failed approval
     */
    public record FailedApproval(
            String requestId,
            String employeeName,
            String reason) {
    }

    /**
     * Get the count of failed approvals
     */
    public int failedCount() {
        return failedApprovals != null ? failedApprovals.size() : 0;
    }
}
