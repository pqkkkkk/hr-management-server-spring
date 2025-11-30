package org.pqkkkkk.hr_management_server.modules.request.domain.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "additional_wfh_info_table")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AdditionalWfhInfo {
    
    @Id
    @Column(name = "request_id")
    String requestId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "request_id")
    Request request;
    
    @Column(name = "wfh_commitment", nullable = false)
    @Builder.Default
    Boolean wfhCommitment = false;
    
    @Column(name = "work_location", length = 500)
    String workLocation;
    
    @Column(name = "total_days", nullable = false, precision = 4, scale = 1)
    BigDecimal totalDays;
    
    @OneToMany(mappedBy = "additionalWfhInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<WfhDate> wfhDates = new ArrayList<>();
    
    // Helper methods
    public void addWfhDate(WfhDate wfhDate) {
        wfhDates.add(wfhDate);
        wfhDate.setAdditionalWfhInfo(this);
    }
    
    public void removeWfhDate(WfhDate wfhDate) {
        wfhDates.remove(wfhDate);
        wfhDate.setAdditionalWfhInfo(null);
    }
}
