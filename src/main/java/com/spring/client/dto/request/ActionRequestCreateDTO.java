// src/main/java/com/spring/client/dto/request/ActionRequestCreateDTO.java
package com.spring.client.dto.request;

import com.spring.client.enums.ReqType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ActionRequestCreateDTO {

    @NotNull(message = "요청 유형은 필수입니다.")
    private ReqType reqType;     // WITHDRAW or JOB_CANCEL

    // JOB_CANCEL일 때만 필요 (WITHDRAW인 경우 null이어야 함)
    private Long jobId;

    @NotBlank(message = "사유를 입력해 주세요.")
    private String reason;

    // 서버에서 세션/시큐리티 컨텍스트로 채움 (UI에서 숨김)
    private String requestedBy;

    // 서버에서 세션으로 채움
    private Long cmpId;
}
