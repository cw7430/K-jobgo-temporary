package com.spring.client.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmpContDto {
  
    /** 담당자 고유 ID */
    private Long empId;

    /** 소속 기업 ID */
    private Long cmpId;

    /** 담당자 성명 */
    private String empName;

    /** 담당자 직급/직함 */
    private String empTitle;

    /** 담당자 연락처 */
    private String empPhone;

    /** 레코드 생성 시각 */
    private LocalDateTime crtDt;

    /** 레코드 수정 시각 */
    private LocalDateTime updDt;
}
