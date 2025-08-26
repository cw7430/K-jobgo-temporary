// AgencyProfilePublicView.java
package com.spring.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Table(name = "v_agency_profile_public")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Immutable              // ★ 읽기 전용 (Hibernate)
public class AgencyProfilePublicView {

    @Id
    @Column(name = "profile_id")
    private Long profileId;

    @Column(name = "visa_type", nullable = false, length = 50)
    private String visaType;

    @Column(name = "job_code", nullable = false, length = 50)
    private String jobCode;

    @Column(name = "employee_name_en", nullable = false, length = 150)
    private String employeeNameEn;

    @Column(name = "nationality_en", nullable = false, length = 100)
    private String nationalityEn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProfileStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
