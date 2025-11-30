package org.pqkkkkk.hr_management_server.modules.request.domain.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.LeaveType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "additional_leave_info_table")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AdditionalLeaveInfo {
    
    @Id
    @Column(name = "request_id")
    String requestId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "request_id")
    Request request;
    
    @Column(name = "leave_type", nullable = false)
    @Enumerated(EnumType.STRING)
    LeaveType leaveType;
    
    @Column(name = "total_days", nullable = false, precision = 4, scale = 1)
    BigDecimal totalDays;
    
    @OneToMany(mappedBy = "additionalLeaveInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<LeaveDate> leaveDates = new ArrayList<>();
    
    // Helper methods
    public void addLeaveDate(LeaveDate leaveDate) {
        leaveDates.add(leaveDate);
        leaveDate.setAdditionalLeaveInfo(this);
    }
    
    public void removeLeaveDate(LeaveDate leaveDate) {
        leaveDates.remove(leaveDate);
        leaveDate.setAdditionalLeaveInfo(null);
    }
}
