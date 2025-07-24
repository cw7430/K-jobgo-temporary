package com.spring.dto.request;

import com.spring.entity.Profile;
import com.spring.entity.WorkExperience;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkExperienceRequestDto {
    private String period;
    private String companyName;
    private String jobResponsibility;

    public WorkExperience toEntity(Profile profile) {
        return WorkExperience.builder()
                .profile(profile)
                .period(this.period)
                .companyName(this.companyName)
                .jobResponsibility(this.jobResponsibility)
                .build();
    }

    public void updateEntity(WorkExperience workExperience) {
        workExperience.setPeriod(this.period);
        workExperience.setCompanyName(this.companyName);
        workExperience.setJobResponsibility(this.jobResponsibility);
    }
}
