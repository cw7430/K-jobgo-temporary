package com.spring.dto.response;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgencyRowDto {
    private Long profileId;       // 체크박스 value
    private String agencyName;    // 송출업체명 (1,2 권한만 노출)
    private String visaType;
    private String jobCode;
    private String employeeNameEn;
    private String nationalityEn;
    private String statusLabel;   // 상태(권한 5에서만 표시, 없으면 "미배정")
    private LocalDateTime createdAt;
}