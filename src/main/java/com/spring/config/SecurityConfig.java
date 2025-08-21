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
        .requestMatchers("/", "/home", "/loginPage",
                         "/profileList", "/profileDetail/**",
                         "/css/**", "/js/**", "/img/**").permitAll()

        .requestMatchers(HttpMethod.POST, "/api/login").permitAll()

        // VISA
        .requestMatchers(HttpMethod.GET, "/admin/visa/my/**").hasRole("AGENT_VISA")
        .requestMatchers(HttpMethod.GET, "/admin/visa/**")
          .hasAnyRole("SUPERADMIN","ADMIN","AGENT_VISA")
        .requestMatchers(HttpMethod.POST,   "/admin/visa/**").hasRole("AGENT_VISA")
        .requestMatchers(HttpMethod.DELETE, "/admin/visa/**").hasRole("AGENT_VISA")

        .anyRequest().permitAll()
      )
      .formLogin(login -> login
        .loginPage("/loginPage")
        .loginProcessingUrl("/login")
        .successHandler((req, res, authn) -> {
          var auths = authn.getAuthorities().toString();
          if (auths.contains("ROLE_AGENT_VISA")) {
        	  res.sendRedirect("/admin/visa/my"); // ✅ 권한5는 내 전용 페이지
          } else if (auths.contains("ROLE_SUPERADMIN") || auths.contains("ROLE_ADMIN")) {
        	  res.sendRedirect("/admin/visa");    // 슈퍼/관리자: 전체 페이지
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
