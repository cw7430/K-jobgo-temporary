package com.spring.auth.controller;

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
import java.util.Map;

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
        // ⛔ 아이디/비번 불일치 → 401
        if (admin == null) {
            return ResponseEntity.status(401)
                    .body(new LoginResponseDto(null, null, null, session.getId(), List.of()));
        }
        int authId = admin.getAuthorityType().getAuthorityId();
        String authName = admin.getAuthorityType().getAuthorityName(); // CEO, MANAGER, STAFF, RETIRE, AGENT

        // ⛔ 퇴사/비활성만 403로 차단
        if ("RETIRE".equalsIgnoreCase(authName) || authId == 4) {
            return ResponseEntity.status(403)
                    .body(new LoginResponseDto(admin.getAdminId(), admin.getAdminName(), authName, session.getId(), List.of()));
        }

        // 세션 키 저장 (AgencyController에서 사용)
        session.setAttribute("loggedInAdmin", admin);
        session.setAttribute("adminId", admin.getAdminId());
        session.setAttribute("adminName", admin.getAdminName());
        session.setAttribute("authorityId", authId);
        session.setAttribute("authorityName", authName);

        // ✅ ROLE 매핑 (STAFF 포함)
        List<GrantedAuthority> authorities = switch (authName) {
            case "CEO"     -> List.of(new SimpleGrantedAuthority("ROLE_SUPERADMIN"));
            case "MANAGER" -> List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
            case "AGENT"   -> List.of(new SimpleGrantedAuthority("ROLE_AGENT_VISA"));
            case "STAFF"   -> List.of(new SimpleGrantedAuthority("ROLE_STAFF"));
            case "CALL-STAFF" -> List.of(new SimpleGrantedAuthority("ROLE_CALL-STAFF"));
            default        -> List.of(new SimpleGrantedAuthority("ROLE_USER"));
        };

        var authentication = new UsernamePasswordAuthenticationToken(admin, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        // ✅ 프런트 분기용 roles도 내려주기
        List<String> roles = authorities.stream().map(GrantedAuthority::getAuthority).toList();

        return ResponseEntity.ok(
            new LoginResponseDto(
                admin.getAdminId(),
                admin.getAdminName(),
                authName,
                session.getId(),
                roles
            )
        );
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
