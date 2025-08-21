package com.spring.controller;

import com.spring.dto.LoginRequestDto;
import com.spring.dto.LoginResponseDto;
import com.spring.entity.Admin;
import com.spring.service.AdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AuthApiController {

    private final AdminService adminService;

    public AuthApiController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/api/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginDTO, HttpSession session) {
        System.out.println("✅ 로그인 컨트롤러 진입함");
        System.out.println("✅ 전달받은 admin_login_id: [" + loginDTO.getAdminLoginId() + "]");
        System.out.println("✅ 전달받은 password: [" + loginDTO.getAdminPassword() + "]");

        Admin admin = adminService.authenticate(loginDTO.getAdminLoginId(), loginDTO.getAdminPassword());

        if (admin != null) {
            // 세션에 관리자 정보 저장
            session.setAttribute("loggedInAdmin", admin);

            // ✅ 권한번호 → ROLE 매핑
            int authId = admin.getAuthorityType().getAuthorityId();
            List<GrantedAuthority> authorities = switch (authId) {
                case 1 -> List.of(new SimpleGrantedAuthority("ROLE_SUPERADMIN"));
                case 2 -> List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
                case 5 -> List.of(new SimpleGrantedAuthority("ROLE_AGENT_VISA"));
                default -> List.of(new SimpleGrantedAuthority("ROLE_ADMIN_GENERIC")); // 나머지 관리용 기본 롤(선택)
            };
            
            // Spring Security에 인증 정보 설정
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(admin, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            // 권한 제한 (예: 4번 권한은 접근 불가)
            if (admin.getAuthorityType().getAuthorityId() == 4) {
                return ResponseEntity.status(403).body(null);
            }

            // 로그인 응답 생성
            LoginResponseDto responseDto = new LoginResponseDto(
                    admin.getAdminId(),
                    admin.getAdminName(),
                    admin.getAuthorityType().getAuthorityName(),
                    session.getId()
            );
            System.out.println("LoginResponseDto 생성 후 반환: " + responseDto);
            return ResponseEntity.ok(responseDto);
        }

        // 인증 실패
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/api/keep-alive")
    @ResponseBody
    public String keepSessionAlive(HttpSession session) {
        session.getAttribute("loggedInAdmin"); // 세션 유지 확인
        return "alive";
    }

    @PostMapping("/api/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();  // ✅ 세션 완전 종료
        SecurityContextHolder.clearContext();  // ✅ 인증 정보 제거 (Spring Security)
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/admin-test")
    public ResponseEntity<List<Admin>> testAdminSelect() {
        List<Admin> allAdmins = adminService.findAll();
        return ResponseEntity.ok(allAdmins);
    }
}
