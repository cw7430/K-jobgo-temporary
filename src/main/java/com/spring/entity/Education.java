package com.spring.entity;

import com.spring.dto.request.EducationRequestDto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "education")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Education extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long educationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Column(length = 50)
    private String period;

    @Column(length = 100)
    private String schoolName;

    @Column(length = 100)
    private String major;

    private Boolean graduated;

    public void updateFromDto(EducationRequestDto educationRequestDto) {
        this.schoolName = educationRequestDto.getSchoolName();
        this.major = educationRequestDto.getMajor();
        this.period = educationRequestDto.getPeriod();
        this.graduated = educationRequestDto.getGraduated();
    }
}
