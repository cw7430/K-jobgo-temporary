package com.spring.dto.response;

import com.spring.entity.Education;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationAdminResponseDto {
    private Long educationId;
    private String period;
    private String schoolName;
    private String major;
    private Boolean graduated;
    private String status;

    public static EducationAdminResponseDto fromEntity(Education education) {
        boolean graduated = Boolean.TRUE.equals(education.getGraduated());
        String statusStr = education.getGraduated() == null ? "" : (graduated ? "graduated" : "enrolled");

        return EducationAdminResponseDto.builder()
                .educationId(education.getEducationId())
                .period(education.getPeriod())
                .schoolName(education.getSchoolName())
                .major(education.getMajor())
                .graduated(graduated)
                .status(statusStr)
                .build();
    }
}
