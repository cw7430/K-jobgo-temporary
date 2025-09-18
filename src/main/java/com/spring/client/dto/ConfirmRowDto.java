package com.spring.client.dto;

import lombok.*;
import java.time.LocalDateTime;
import com.spring.client.enums.ApprStatus;

@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor
public class ConfirmRowDto {
    private Long cmpId;
    private String cmpName;
    private String bizNo;
    private String contactName;
    private String contactPhone;

    private Long licenseFileId;
    private String licenseMime;

    private Long cardFileId;
    private String cardMime;

    private Boolean prxJoin;      // cmp_info.prx_join : 대리가입 여부
    private ApprStatus apprStatus;    // 'PENDING','APPROVED','REJECTED' (enum)

    private String rejectReason;  // 최신 승인이력의 apprCmt
    private LocalDateTime createdAt;   // cmp_info.crt_dt
    private LocalDateTime processedAt; // 최신 승인/반려 시각
    
    private String proxyExecutor;        // ← 대리 가입 처리 직원명
}
