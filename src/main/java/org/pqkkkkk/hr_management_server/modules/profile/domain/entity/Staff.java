package org.pqkkkkk.hr_management_server.modules.profile.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "name")
    private String name;
    @Column(name = "dob")
    private String dateOfBirth;
    @Column(name = "phone")
    private String phoneNumber;
    @Column(name = "email")
    private String email;
    @ManyToOne
    @JoinColumn(name = "id_department")
    private Department department;
    @Column(name = "position")
    private String position;
}
