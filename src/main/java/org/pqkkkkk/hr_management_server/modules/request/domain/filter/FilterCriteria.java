package org.pqkkkkk.hr_management_server.modules.request.domain.filter;

import java.time.LocalDate;

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;
import org.pqkkkkk.hr_management_server.shared.Constants;
import org.pqkkkkk.hr_management_server.shared.Constants.SortDirection;

public class FilterCriteria {

    public record RequestFilter(
            String employeeId,
            String approverId,
            String processorId,
            String departmentId,
            String nameTerm,
            RequestStatus status,
            RequestType type,
            LocalDate startDate,
            LocalDate endDate,
            Integer currentPage,
            Integer pageSize,
            String sortBy,
            SortDirection sortDirection) {
        public RequestFilter {
            currentPage = currentPage != null ? currentPage : Constants.DEFAULT_PAGE_NUMBER;
            pageSize = pageSize != null ? pageSize : Constants.DEFAULT_PAGE_SIZE;
            sortBy = sortBy != null ? sortBy : Constants.DEFAULT_SORT_BY;
            sortDirection = sortDirection != null ? sortDirection
                    : SortDirection.valueOf(Constants.DEFAULT_SORT_DIRECTION);
        }
    }
}
