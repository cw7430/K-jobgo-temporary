package com.spring.dto.request;

import com.spring.entity.Education;
import com.spring.entity.Profile;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationRequestDto {
    private String period;
    private String schoolName;
    private String major;
    private Boolean graduated;
    private String status;

    public Education toEntity(Profile profile) {
        boolean graduated = "graduated".equals(this.status);
        return Education.builder()
                .profile(profile)
                .period(this.period)
                .schoolName(this.schoolName)
                .major(this.major)
                .graduated(graduated)
                .build();
    }

    public void updateEntity(Education education) {
        education.setPeriod(this.period);
        education.setSchoolName(this.schoolName);
        education.setMajor(this.major);
        education.setGraduated("graduated".equals(this.status));
    }
}
