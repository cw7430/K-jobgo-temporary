package com.spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.spring.client.entity.CmpInfo;
import com.spring.client.repository.CmpInfoRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
	
	 // ✅ 성공 핸들러에서 기업 세션 채우기 위해 주입
	  private final CmpInfoRepository cmpInfoRepository;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      // CSRF: 필요한 엔드포인트만 예외
      .csrf(csrf -> csrf.ignoringRequestMatchers(
          new AntPathRequestMatcher("/api/login",  HttpMethod.POST.name()),
          new AntPathRequestMatcher("/api/logout", HttpMethod.POST.name()),
          new AntPathRequestMatcher("/api/business/verify", HttpMethod.POST.name()),
          new AntPathRequestMatcher("/api/check-email", HttpMethod.POST.name()),
          new AntPathRequestMatcher("/api/client/login",  HttpMethod.POST.name()),
          new AntPathRequestMatcher("/api/client/logout", HttpMethod.POST.name())
      ))

      .authorizeHttpRequests(auth -> auth
          // 1) 공개 페이지 & 정적 리소스
          .requestMatchers("/", "/home", "/loginPage",
              "/profileList", "/profileDetail/**",
              "/css/**", "/js/**", "/img/**",
              "/favicon.ico", "/error").permitAll()
          
          // 🔠 링크 대소문자 혼용 대비
          .requestMatchers("/matchingPage", "/MatchingPage", "/companyInfo", "/terms", "/privacy").permitAll()

          // 2) 회원가입/비번 재설정 플로우 공개
          .requestMatchers(HttpMethod.GET,  "/client/joinPage", "/client/join-success").permitAll()
          .requestMatchers(HttpMethod.POST, "/join").permitAll()

          // ✅ 기업 로그인 API 공개
          .requestMatchers(HttpMethod.POST, "/api/client/login", "/api/client/logout").permitAll()

          // ✅ 기업 전용: 구직요청(작성/조회)
          .requestMatchers("/client/applyEmp/**", "/client/clientMyPage/**").hasRole("COMPANY")
          
          .requestMatchers(HttpMethod.POST, "/api/password/forgot").permitAll()
          .requestMatchers(HttpMethod.GET,  "/reset-password").permitAll()
          .requestMatchers(HttpMethod.POST, "/reset-password").permitAll()

          // 3) 공개 API
          .requestMatchers(HttpMethod.POST, "/api/login", "/api/logout").permitAll()
          .requestMatchers(HttpMethod.POST, "/api/business/verify").permitAll()
          .requestMatchers(HttpMethod.POST, "/api/check-email").permitAll()
          .requestMatchers(HttpMethod.GET,  "/api/keep-alive").permitAll()
          .requestMatchers(HttpMethod.GET,  "/api/check-email").permitAll()

          // 4) 송출업체: 등록 공개 / 목록 공개
          .requestMatchers(HttpMethod.GET,  "/agency/register").permitAll()
          .requestMatchers(HttpMethod.POST, "/agency/register").permitAll()
          .requestMatchers(HttpMethod.GET,  "/agency/List", "/agency/agencyList").permitAll()

          // 5-a) 관리자 포털 & 관리자 전용 페이지
          .requestMatchers("/adminMain").hasAnyRole("SUPERADMIN","ADMIN")
          .requestMatchers("/admin/profileList")
          .hasAnyRole("SUPERADMIN","ADMIN","STAFF","CALL-STAFF")

	      // 회원관리(확인/파일 미리보기 포함): 1/2/6
          .requestMatchers(
              "/admin/confirmClient/**",
              "/admin/files/**",
              "/admin/confirmClient/files/**"
          ).hasAnyRole("SUPERADMIN","ADMIN","CALL-STAFF")
          
          // 5-b) VISA 영역
          .requestMatchers(HttpMethod.GET, "/admin/visa/my/**").hasRole("AGENT_VISA")
          .requestMatchers(HttpMethod.GET, "/admin/visa/**")
              .hasAnyRole("SUPERADMIN","ADMIN","AGENT_VISA")
          .requestMatchers(HttpMethod.POST,   "/admin/visa/**").hasRole("AGENT_VISA")
          .requestMatchers(HttpMethod.DELETE, "/admin/visa/**").hasRole("AGENT_VISA")

          // 5-c) 그 외 /admin/** 전역 보호 (구체 매처가 우선)
          .requestMatchers("/admin/**").authenticated()

          // 🚨 마지막에 단 한 번만
          .anyRequest().authenticated()
      )

      .formLogin(login -> login
          .loginPage("/loginPage").permitAll()
          .loginProcessingUrl("/login")   // 폼 로그인 POST /login 사용 시 CSRF 토큰 필요
          .usernameParameter("bizEmail")   // ★ 폼 input name과 맞추기
          .passwordParameter("bizPwd")     // ★ 폼 input name과 맞추기
          .successHandler((req, res, authn) -> {
            var auths = authn.getAuthorities().toString();
            
            // ✅ 기업 로그인 세션값 주입 (인터셉터/헤더에서 사용)
            if (auths.contains("ROLE_COMPANY")) {
                String email = authn.getName(); // 입력한 이메일
                CmpInfo cmp = cmpInfoRepository.findByBizEmail(email).orElse(null);
                if (cmp != null) {
                  req.getSession().setAttribute("loggedInClient", cmp);
                  req.getSession().setAttribute("clientApprStatus",
                      cmp.getApprStatus() != null ? cmp.getApprStatus().name() : null);
                  req.getSession().setAttribute("clientName", cmp.getCmpName());
                  req.getSession().setAttribute("clientCmpId", cmp.getCmpId());
                }
              }
            
            // 리다이렉트 정책
            if (auths.contains("ROLE_AGENT_VISA")) {
              res.sendRedirect("/admin/visa/my");
            } else if (auths.contains("ROLE_SUPERADMIN") || auths.contains("ROLE_ADMIN")) {
              res.sendRedirect("/adminMain"); // ✅ 관리자(1,2)는 포털로
            } else {
              res.sendRedirect("/home");
            }
          })
          .failureHandler((req, res, ex) -> {
              // 현재 보던 페이지로 되돌려 모달에서 에러 안내
              String ref = req.getHeader("Referer");
              String to = (ref != null && !ref.isBlank()) ? ref : "/home";
              to += (to.contains("?") ? "&" : "?") + "clientLoginError=1";
              res.sendRedirect(to);
            })
      )
      .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/home"))
      .httpBasic(basic -> basic.disable());

    return http.build();
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
