package com.spring.dto.response.user;

import com.spring.dto.response.EducationAdminResponseDto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationUserResponseDto {
    private Long educationId;
    private String period;
    private String schoolName;
    private String major;
    private Boolean graduated;
    private String status;

    public static EducationUserResponseDto fromEntity(EducationAdminResponseDto educationAdminDto) {
        boolean graduated = Boolean.TRUE.equals(educationAdminDto.getGraduated());
        String statusStr = educationAdminDto.getGraduated() == null ? "" : (graduated ? "graduated" : "enrolled");

        return EducationUserResponseDto.builder()
                .educationId(educationAdminDto.getEducationId())
                .period(educationAdminDto.getPeriod())
                .schoolName(educationAdminDto.getSchoolName())
                .major(educationAdminDto.getMajor())
                .graduated(graduated)
                .status(statusStr)
                .build();
    }
}
