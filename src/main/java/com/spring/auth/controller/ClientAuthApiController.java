// src/main/java/com/spring/controller/ClientAuthApiController.java
package com.spring.auth.controller;

import java.util.List;
import java.util.Map;

import com.spring.client.dto.request.ClientLoginRequest;
import com.spring.client.service.ClientAuthService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor            // ✅ final 필드 주입
@Validated
public class ClientAuthApiController {

  private final ClientAuthService clientAuthService; // cmp_info 조회

  /**
   * 기업 회원 로그인
   * - 본문 JSON: { "email": "...", "password": "..." }
   * - 성공: 세션/시큐리티 컨텍스트 설정 후 200 OK(JSON)
   * - 실패: 401(자격 없음) 또는 400(형식 오류)
   */
  @PostMapping("/api/client/login")
  public ResponseEntity<?> clientLogin(@RequestBody @Valid ClientLoginRequest req, HttpSession session) {
    var member = clientAuthService.authenticate(req.getEmail(), req.getPassword());
    if (member == null) {
      return ResponseEntity.status(401).body(Map.of(
          "success", false,
          "message", "이메일 또는 비밀번호가 올바르지 않습니다."
      ));
    }

    // 세션에 기업 회원 정보 저장
    session.setAttribute("loggedInClient", member);                             // CmpInfo 엔티티/DTO
    session.setAttribute("clientApprStatus", member.getApprStatus().name());    // "APPROVED" | "PENDING" | "REJECTED"
    session.setAttribute("clientName", member.getCmpName());

    // (선택) 세션 타임아웃 지정
    // session.setMaxInactiveInterval(60 * 60 * 2); // 2시간

    // 스프링시큐리티 컨텍스트 - ROLE_COMPANY 부여
    var auth = new UsernamePasswordAuthenticationToken(
        member, null, List.of(new SimpleGrantedAuthority("ROLE_COMPANY")));
    SecurityContextHolder.getContext().setAuthentication(auth);
    session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

    return ResponseEntity.ok(Map.of(
        "success", true,
        "apprStatus", member.getApprStatus().name(),
        "displayName", member.getCmpName()
    ));
  }

  /**
   * 기업 회원 로그아웃
   * - 세션/시큐리티 정리
   */
  @PostMapping("/api/client/logout")
  public ResponseEntity<?> clientLogout(HttpSession session){
    // 세션 속성 제거
    session.removeAttribute("loggedInClient");
    session.removeAttribute("clientApprStatus");
    session.removeAttribute("clientName");

    // 시큐리티 컨텍스트 정리
    SecurityContextHolder.clearContext();

    // (권장) 세션 무효화
    // session.invalidate();

    return ResponseEntity.ok(Map.of("success", true));
  }
}
