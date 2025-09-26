package com.spring.client.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class ApplyEmpAdminLogDTO {
    @NotBlank(message = "처리 상담자 이름은 필수입니다.")
    private String handledBy;

    @NotBlank(message = "상담내용을 입력해 주세요.")
    private String counselContent;

    private String referenceNote; // 선택
}
