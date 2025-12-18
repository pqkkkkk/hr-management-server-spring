package org.pqkkkkk.hr_management_server.modules.request.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "additional_checkout_info_table")
@Getter
@Setter
@ToString(exclude = {"request"})
@EqualsAndHashCode(exclude = {"request"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AdditionalCheckOutInfo {
    
    @Id
    @Column(name = "request_id")
    String requestId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "request_id")
    Request request;
    
    @Column(name = "desired_check_out_time", nullable = false)
    LocalDateTime desiredCheckOutTime;
    
    @Column(name = "current_check_out_time", nullable = false)
    LocalDateTime currentCheckOutTime;
}
