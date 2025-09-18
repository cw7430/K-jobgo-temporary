package com.spring.client.dto;

import com.spring.client.enums.EmailStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmpApprHistDto {

    /** 승인 이력 고유 ID */
    private Long apprId;

    /** 소속 기업 ID */
    private Long cmpId;

    /** 승인 여부 (true=승인, false=반려) */
    private boolean isAppr;

    /** 승인/반려 처리 일시 */
    private LocalDateTime apprDt;

    /** 처리 관리자 ID/이름 */
    private String apprBy;

    /** 반려 사유 또는 메모 */
    private String apprCmt;

    /** 이메일 발송 상태 (PENDING, SENT, FAILED) */
    private EmailStatus emailStatus;

    /** 이메일 발송 시각 */
    private LocalDateTime emailSentAt;

    /** 이메일 발송 실패 시 에러 메시지 */
    private String emailErrorMsg;

    /** 레코드 생성 시각 */
    private LocalDateTime crtDt;

    /** 레코드 수정 시각 */
    private LocalDateTime updDt;
}