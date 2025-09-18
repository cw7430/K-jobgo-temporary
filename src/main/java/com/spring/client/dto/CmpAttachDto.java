package com.spring.client.dto;

import com.spring.client.enums.FileCategory;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmpAttachDto {
    /** 파일 고유 ID */
    private Long fId;

    /** 소속 기업 ID */
    private Long cmpId;

    /** 파일 종류 (BUSINESS_LICENSE, BUSINESS_CARD) */
    private FileCategory fileCategory;

    /** 원본 파일명 */
    private String origName;

    /** 서버 저장 경로 */
    private String fPath;

    /** 확장자 (예: pdf, jpg) */
    private String fExt;

    /** MIME 타입 (예: application/pdf) */
    private String fMime;

    /** 파일 크기(바이트) */
    private Long fSize;

    /** 업로드 시각 */
    private LocalDateTime upldDt;
}