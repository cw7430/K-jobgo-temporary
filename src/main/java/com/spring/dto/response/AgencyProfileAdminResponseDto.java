package com.spring.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

import com.spring.entity.ProfileStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgencyProfileAdminResponseDto { // 최고권한 1,2 전용
    private Long profileId;
    private String agencyName;      // 관리자만 볼 수 있음
    private String visaType;
    private String jobCode;
    private String employeeNameEn;
    private String nationalityEn;
    private ProfileStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AgencyProfileFileResponseDto> files;
}
