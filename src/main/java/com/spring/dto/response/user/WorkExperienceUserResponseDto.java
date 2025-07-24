package com.spring.dto.response.user;

import com.spring.dto.response.WorkExperienceAdminResponseDto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkExperienceUserResponseDto {
    private Long workId;
    private String period;
    private String companyName;
    private String jobResponsibility;

    public static WorkExperienceUserResponseDto fromEntity(WorkExperienceAdminResponseDto workAdminDto) {
        return WorkExperienceUserResponseDto.builder()
                .workId(workAdminDto.getWorkId())
                .period(workAdminDto.getPeriod())
                .companyName(workAdminDto.getCompanyName())
                .jobResponsibility(workAdminDto.getJobResponsibility())
                .build();
    }
}
