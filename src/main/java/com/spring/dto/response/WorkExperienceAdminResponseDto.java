package com.spring.dto.response;

import com.spring.entity.WorkExperience;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkExperienceAdminResponseDto {

    private Long workId;
    private String period;
    private String companyName;
    private String jobResponsibility;

    public static WorkExperienceAdminResponseDto fromEntity(WorkExperience workExperience) {
        return WorkExperienceAdminResponseDto.builder()
                .workId(workExperience.getWorkId())
                .period(workExperience.getPeriod())
                .companyName(workExperience.getCompanyName())
                .jobResponsibility(workExperience.getJobResponsibility())
                .build();
    }
}
