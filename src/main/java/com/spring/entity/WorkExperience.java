package com.spring.entity;

import com.spring.dto.request.WorkExperienceRequestDto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "work_experience")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkExperience extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "work_id")
    private Long workId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Column(name = "period", length = 50)
    private String period;

    @Column(name = "company_name", length = 100)
    private String companyName;

    @Column(name = "job_responsibility", columnDefinition = "TEXT")
    private String jobResponsibility;

    public void updateFromDto(WorkExperienceRequestDto dto) {
        this.companyName = dto.getCompanyName();
        this.period = dto.getPeriod();
        this.jobResponsibility = dto.getJobResponsibility();
    }
}
