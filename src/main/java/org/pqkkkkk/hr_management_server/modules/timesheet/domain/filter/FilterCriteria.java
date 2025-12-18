package org.pqkkkkk.hr_management_server.modules.timesheet.domain.filter;

import org.pqkkkkk.hr_management_server.modules.timesheet.domain.entity.Enums.TimeSheetSortingField;
import org.pqkkkkk.hr_management_server.shared.Constants;

public class FilterCriteria {
    public record TimeSheetFilter (
        String employeeId,
        String dateFrom,
        String dateTo,
        String morningStatus,    
        String afternoonStatus, 
        Boolean isFinalized,
        Boolean morningWfh,  
        Boolean afternoonWfh, 
        Integer currentPage,
        Integer pageSize,
        String sortBy,
        String sortDirection

    ) {
        public TimeSheetFilter {
            pageSize = (pageSize == null || pageSize <= 0) ? Constants.DEFAULT_PAGE_SIZE : pageSize;
            currentPage = (currentPage == null || currentPage < 0) ? 0 : currentPage;
            sortBy = (sortBy == null || sortBy.isBlank()) ? TimeSheetSortingField.DATE.getFieldName() : sortBy;
            sortDirection = (sortDirection == null || sortDirection.isBlank()) ? Constants.DEFAULT_SORT_DIRECTION : sortDirection;
        }
    }
}
