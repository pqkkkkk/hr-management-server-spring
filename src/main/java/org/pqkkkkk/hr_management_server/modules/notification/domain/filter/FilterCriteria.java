package org.pqkkkkk.hr_management_server.modules.notification.domain.filter;

import java.time.LocalDateTime;

import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Enums.NotificationReferenceType;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Enums.NotificationSortingField;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Enums.NotificationType;
import org.pqkkkkk.hr_management_server.shared.Constants;
import org.pqkkkkk.hr_management_server.shared.Constants.SortDirection;

public class FilterCriteria {

    public record NotificationFilter(
        Integer currentPage,
        Integer pageSize,
        NotificationSortingField sortBy,
        SortDirection sortDirection,
        String recipientId,
        Boolean isRead,
        NotificationType type,
        NotificationReferenceType referenceType,
        String referenceId,
        LocalDateTime fromDate,
        LocalDateTime toDate
    ) {
        // Constructor with default values
        public NotificationFilter {
            currentPage = currentPage != null ? currentPage : Constants.DEFAULT_PAGE_NUMBER - 1;
            pageSize = pageSize != null ? pageSize : Constants.DEFAULT_PAGE_SIZE;
            sortBy = sortBy != null ? sortBy : NotificationSortingField.CREATED_AT;
            sortDirection = sortDirection != null ? sortDirection : 
                SortDirection.valueOf(Constants.DEFAULT_SORT_DIRECTION);
        }
    }
}
