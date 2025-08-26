package com.spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	 @Bean
	  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http
	      .csrf(csrf -> csrf.disable())
	      .authorizeHttpRequests(auth -> auth
	    		  // 공개 페이지 & 정적 리소스
	    		  .requestMatchers("/", "/home", "/loginPage",
	    		                   "/profileList", "/profileDetail/**",
	    		                   "/css/**", "/js/**", "/img/**").permitAll()

	    		  // 등록: Public 허용
	    		  .requestMatchers(HttpMethod.GET,  "/agency/register").permitAll()
	    		  .requestMatchers(HttpMethod.POST, "/agency/register").permitAll()

	    		  // 목록: 공개 (컨트롤러에서 registeredOnce로 가드)
	    		  .requestMatchers(HttpMethod.GET, "/agency/List", "/agency/agencyList").permitAll()

	    		  // ✅ 배정/다운로드: 1,2,5만
	    		  .requestMatchers(HttpMethod.POST, "/agency/*/assign")
	    		      .hasAnyRole("SUPERADMIN","ADMIN","AGENT_VISA")

	    		  // 파일 다운로드 엔드포인트가 있다면 동일하게 보호
	    		  .requestMatchers(HttpMethod.GET, "/agency/files/**")
	    		      .hasAnyRole("SUPERADMIN","ADMIN","AGENT_VISA")

	    		  // 삭제: 1,2만
	    		  .requestMatchers(HttpMethod.DELETE, "/agency/**")
	    		      .hasAnyRole("SUPERADMIN","ADMIN")
	    		  .requestMatchers(HttpMethod.POST, "/agency/*/delete")
	    		      .hasAnyRole("SUPERADMIN","ADMIN")

	    		  // 로그인 API
	    		  .requestMatchers(HttpMethod.POST, "/api/login").permitAll()

	    		  // VISA 영역 (기존 유지)
	    		  .requestMatchers(HttpMethod.GET, "/admin/visa/my/**").hasRole("AGENT_VISA")
	    		  .requestMatchers(HttpMethod.GET, "/admin/visa/**")
	    		      .hasAnyRole("SUPERADMIN","ADMIN","AGENT_VISA")
	    		  .requestMatchers(HttpMethod.POST,   "/admin/visa/**").hasRole("AGENT_VISA")
	    		  .requestMatchers(HttpMethod.DELETE, "/admin/visa/**").hasRole("AGENT_VISA")

	    		  .requestMatchers("/admin/**").authenticated()
	    		  .anyRequest().permitAll()
	    		)
	      .formLogin(login -> login
	        .loginPage("/loginPage")
	        .loginProcessingUrl("/login")
	        .successHandler((req, res, authn) -> {
	          var auths = authn.getAuthorities().toString();
	          if (auths.contains("ROLE_AGENT_VISA")) {
	            res.sendRedirect("/admin/visa/my");
	          } else if (auths.contains("ROLE_SUPERADMIN") || auths.contains("ROLE_ADMIN")) {
	            res.sendRedirect("/admin/visa");
	          } else {
	            res.sendRedirect("/home");
	          }
	        })
	        .permitAll()
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
