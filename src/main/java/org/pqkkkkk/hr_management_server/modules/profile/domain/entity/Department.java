package org.pqkkkkk.hr_management_server.modules.profile.domain.entity;

import java.util.List;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "department_table")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class Department {
    @Id
    @UuidGenerator
    @Column(name = "department_id")
    String departmentId;

    @Column(name = "department_name")
    String departmentName;

    @OneToMany(mappedBy = "department",cascade = CascadeType.ALL, orphanRemoval = true)
    List<User> members;
}
