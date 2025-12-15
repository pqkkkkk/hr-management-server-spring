package org.pqkkkkk.hr_management_server.modules.notification.infrastructure;

import java.util.ArrayList;
import java.util.List;

import org.pqkkkkk.hr_management_server.modules.notification.domain.dao.NotificationDao;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Notification;
import org.pqkkkkk.hr_management_server.modules.notification.domain.entity.Enums.NotificationSortingField;
import org.pqkkkkk.hr_management_server.modules.notification.domain.filter.FilterCriteria.NotificationFilter;
import org.pqkkkkk.hr_management_server.modules.notification.infrastructure.dao.NotificationRepository;
import org.pqkkkkk.hr_management_server.shared.Constants.SortDirection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.Predicate;

@Repository
public class NotificationJpaDao implements NotificationDao {
    private final NotificationRepository notificationRepository;

    public NotificationJpaDao(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    public Notification getNotificationById(String notificationId) {
        return notificationRepository.findById(notificationId).orElse(null);
    }

    @Override
    public Page<Notification> getNotifications(NotificationFilter filter) {
        Specification<Notification> spec = buildSpecification(filter);
        Pageable pageable = buildPageable(filter);
        return notificationRepository.findAll(spec, pageable);
    }

    @Override
    public Long countUnreadNotifications(String recipientId) {
        return notificationRepository.countByRecipientIdAndIsRead(recipientId, false);
    }

    @Override
    public Integer markAllAsRead(String recipientId) {
        return notificationRepository.markAllAsReadByRecipientId(recipientId);
    }

    // Build Specification for filtering
    private Specification<Notification> buildSpecification(NotificationFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Required: recipientId
            if (filter.recipientId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("recipientId"), filter.recipientId()));
            }

            // Optional: isRead
            if (filter.isRead() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isRead"), filter.isRead()));
            }

            // Optional: type
            if (filter.type() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), filter.type()));
            }

            // Optional: referenceType
            if (filter.referenceType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("referenceType"), filter.referenceType()));
            }

            // Optional: referenceId
            if (filter.referenceId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("referenceId"), filter.referenceId()));
            }

            // Optional: fromDate
            if (filter.fromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.fromDate()));
            }

            // Optional: toDate
            if (filter.toDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), filter.toDate()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Build Pageable for pagination and sorting
    private Pageable buildPageable(NotificationFilter filter) {
        Sort sort = buildSort(filter.sortBy(), filter.sortDirection());
        return PageRequest.of(filter.currentPage(), filter.pageSize(), sort);
    }

    // Build Sort object
    private Sort buildSort(NotificationSortingField sortBy, SortDirection sortDirection) {
        String fieldName = switch (sortBy) {
            case CREATED_AT -> "createdAt";
            case TYPE -> "type";
            case IS_READ -> "isRead";
        };

        return sortDirection == SortDirection.ASC 
            ? Sort.by(fieldName).ascending() 
            : Sort.by(fieldName).descending();
    }
}
