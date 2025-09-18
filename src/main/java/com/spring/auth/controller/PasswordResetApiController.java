package com.spring.auth.controller;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.spring.web.ServerUrlProvider;
import com.spring.auth.service.PasswordResetService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/password")
@Validated
public class PasswordResetApiController {

  private final PasswordResetService passwordResetService;
  private final ServerUrlProvider urlProvider;

  public record ForgotReq(@NotBlank @Email String email) {}

  @PostMapping("/forgot")
  public Map<String, Object> forgot(@RequestBody @Valid ForgotReq req, HttpServletRequest httpReq) {
    // 접속한 도메인(Forwarded 헤더 포함) 기준으로 베이스 URL 계산
    String baseUrl = urlProvider.currentBaseUrl(httpReq);

    // 존재/미존재 여부와 무관하게 동일 응답 (사용자 열거 방지)
    passwordResetService.requestReset(req.email(), baseUrl);

    return Map.of("ok", true, "message", "입력하신 이메일로 재설정 링크를 보냈습니다.");
  }
}
