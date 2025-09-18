// src/main/java/com/spring/client/dto/request/ClientLoginRequest.java
package com.spring.client.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClientLoginRequest {
    @NotBlank @Email
    private String email;     // 로그인용 이메일 (DB의 CmpInfo.bizEmail 매칭)
    @NotBlank
    private String password;  // 평문 비밀번호 (서버에서 matches로 검증)
}
