package org.pqkkkkk.hr_management_server.modules.request.domain.entity;

import java.time.LocalDate;

import org.hibernate.annotations.UuidGenerator;
import org.pqkkkkk.hr_management_server.modules.request.domain.entity.Enums.ShiftType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Entity
@Table(
    name = "wfh_dates_table",
    uniqueConstraints = @UniqueConstraint(columnNames = {"request_id", "date"})
)
@Getter
@Setter
@ToString(exclude = {"additionalWfhInfo"})
@EqualsAndHashCode(exclude = {"additionalWfhInfo"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class WfhDate {
    
    @Id
    @Column(name = "wfh_date_id")
    @UuidGenerator
    String wfhDateId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    AdditionalWfhInfo additionalWfhInfo;
    
    @Column(name = "date", nullable = false)
    LocalDate date;
    
    @Column(name = "shift", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    ShiftType shift = ShiftType.FULL_DAY;
}
