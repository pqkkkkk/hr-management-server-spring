package org.pqkkkkk.hr_management_server.modules.request.domain.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.pqkkkkk.hr_management_server.modules.profile.domain.entity.User;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestStatus;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.RequestType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "request_table")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class Request {
    
    @Id
    @Column(name = "request_id")
    @UuidGenerator
    String requestId;
    
    @Column(name = "request_type", nullable = false)
    @Enumerated(EnumType.STRING)
    RequestType requestType;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    RequestStatus status = RequestStatus.PENDING;
    
    @Column(name = "title", nullable = false)
    String title;
    
    @Column(name = "user_reason", columnDefinition = "TEXT")
    String userReason;
    
    @Column(name = "reject_reason", columnDefinition = "TEXT")
    String rejectReason;
    
    @Column(name = "attachment_url", columnDefinition = "TEXT")
    String attachmentUrl;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    User employee;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    // User who has the authority to approve or reject the request
    User approver;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processor_id")
    // User who is delegated to process the request
    User processor;
    
    @Column(name = "processed_at")
    LocalDateTime processedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    LocalDateTime updatedAt;
    
    // One-to-one relationships with additional info tables
    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    AdditionalCheckInInfo additionalCheckInInfo;
    
    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    AdditionalCheckOutInfo additionalCheckOutInfo;
    
    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    AdditionalTimesheetInfo additionalTimesheetInfo;
    
    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    AdditionalLeaveInfo additionalLeaveInfo;
    
    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    AdditionalWfhInfo additionalWfhInfo;
}
