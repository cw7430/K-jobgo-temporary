package com.spring.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgencyProfileFileResponseDto { // 프로필 등록 파일 첨부
    private Long fileId;
    private String originalName;
    private String storageKey;
    private String mimeType;
    private Long fileSize;
}
