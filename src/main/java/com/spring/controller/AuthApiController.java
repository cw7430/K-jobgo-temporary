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
        if (admin == null) return ResponseEntity.badRequest().build();

        int authId = admin.getAuthorityType().getAuthorityId();
        // ✅ 1,2,5만 허용
        if (!(authId == 1 || authId == 2 || authId == 5)) {
            return ResponseEntity.status(403).build();
        }

        // 세션 키 저장 (AgencyController에서 사용)
        session.setAttribute("loggedInAdmin", admin);
        session.setAttribute("adminId", admin.getAdminId());
        session.setAttribute("adminName", admin.getAdminName());
        session.setAttribute("authorityId", authId);
        session.setAttribute("authorityName", admin.getAuthorityType().getAuthorityName());

        // ROLE 매핑
        List<GrantedAuthority> authorities = switch (authId) {
            case 1 -> List.of(new SimpleGrantedAuthority("ROLE_SUPERADMIN"));
            case 2 -> List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
            case 5 -> List.of(new SimpleGrantedAuthority("ROLE_AGENT_VISA"));
            default -> List.of();
        };

        var authentication = new UsernamePasswordAuthenticationToken(admin, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        var resp = new LoginResponseDto(
            admin.getAdminId(), admin.getAdminName(), admin.getAuthorityType().getAuthorityName(), session.getId()
        );
        return ResponseEntity.ok(resp);
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
