// src/main/java/com/spring/controller/ClientAuthApiController.java
package com.spring.auth.controller;

import java.util.List;
import java.util.Map;

import com.spring.client.dto.request.ClientLoginRequest;
import com.spring.client.service.ClientAuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
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
  public ResponseEntity<?> clientLogin(@RequestBody @Valid ClientLoginRequest req,
          HttpServletRequest request,
          HttpServletResponse response,
          HttpSession session) {
	var member = clientAuthService.authenticate(req.getEmail(), req.getPassword());
	if (member == null) {
	return ResponseEntity.status(401).body(Map.of(
	"success", false,
	"message", "이메일 또는 비밀번호가 올바르지 않습니다."
	));
	}
	
	var auth = new UsernamePasswordAuthenticationToken(
	member, null, List.of(new SimpleGrantedAuthority("ROLE_COMPANY")));
	var ctx = SecurityContextHolder.createEmptyContext();
	ctx.setAuthentication(auth);
	SecurityContextHolder.setContext(ctx);
	
	// ✅ 표준 키 + 저장소 save로 영속화 보장
	session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, ctx);
	new HttpSessionSecurityContextRepository().saveContext(ctx, request, response);
	
	// 편의 세션
	session.setAttribute("loggedInClient", member);
	session.setAttribute("clientApprStatus", member.getApprStatus().name());
	session.setAttribute("clientName", member.getCmpName());
	
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
