package com.spring.client.dto;

import com.spring.client.enums.ApprStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmpInfoDto {
    private Long cmpId;
    private String cmpName;
    private String ceoName;
    private String bizNo;
    private String bizEmail;
    private String zipCode;
    private String cmpAddr;
    private String addrDt;
    private String cmpPhone;
    private boolean agrTerms;
    private boolean prxJoin;
    private boolean fileConfirm;
    private String proxyExecutor;
    private ApprStatus apprStatus;

    private String processedBy;        // 처리자
    private LocalDateTime processedAt; // 처리일

    private LocalDateTime crtDt;
    private LocalDateTime updDt;
}
