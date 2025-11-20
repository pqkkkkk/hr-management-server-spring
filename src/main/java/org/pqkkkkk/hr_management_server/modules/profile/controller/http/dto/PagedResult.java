package org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto;

import java.util.List;

/**
 * Simple paginated response wrapper used by controller to return items + pagination metadata.
 */
public record PagedResult<T>(
        List<T> items,
        int currentPage,
        int pageSize,
        long totalElements,
        int totalPages
) {

}
