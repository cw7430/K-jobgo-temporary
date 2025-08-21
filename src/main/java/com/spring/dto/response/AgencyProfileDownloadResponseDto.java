package com.spring.dto.response;

import com.spring.entity.ProfileStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgencyProfileDownloadResponseDto { // 권한 번호 5 행정사 전용
    private Long profileId;
    private String visaType;
    private String jobCode;
    private String employeeNameEn;
    private String nationalityEn;
    private ProfileStatus status;
    private LocalDateTime createdAt;
    private List<AgencyProfileFileResponseDto> files;
}
