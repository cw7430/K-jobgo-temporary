/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.spring.config.WebConfig
 *  com.spring.interceptor.LoginCheckInterceptor
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.web.servlet.HandlerInterceptor
 *  org.springframework.web.servlet.config.annotation.CorsRegistry
 *  org.springframework.web.servlet.config.annotation.InterceptorRegistry
 *  org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
 *  org.springframework.web.servlet.config.annotation.WebMvcConfigurer
 */
package com.spring.config;

import com.spring.interceptor.ApprovedCompanyInterceptor;
import com.spring.interceptor.LoginCheckInterceptor;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
	
	  private final LoginCheckInterceptor loginCheckInterceptor;         // 관리자용
	  private final ApprovedCompanyInterceptor approvedCompanyInterceptor; // 기업 승인 체크용
	  
	 @Override
	  public void addCorsMappings(CorsRegistry registry) {
	    registry.addMapping("/api/**")
	        .allowedOrigins("https://kjobgo.com","https://www.kjobgo.com",
	                        "https://k-jobgo.com","https://www.k-jobgo.com",
	                        "http://localhost:8081")
	        .allowedMethods("GET","POST","PUT","DELETE")
	        .allowCredentials(true);
	  }

	 @Override
	 public void addInterceptors(InterceptorRegistry registry) {
	     // 1) 관리자 전용 가드: /admin/** 만
	     registry.addInterceptor(loginCheckInterceptor)
         .addPathPatterns("/admin/**")
         .excludePathPatterns(
             "/login", "/loginPage",
             "/css/**", "/js/**", "/img/**", "/images/**", "/favicon.ico", "/error"
         );

	     // 2) 기업 승인 필요 가드: 실제 서비스만 (/client/applyEmp/**)
	     registry.addInterceptor(approvedCompanyInterceptor)
	     .addPathPatterns("/client/**")  // 넓게 적용
	     .excludePathPatterns(
	         // 로그인/로그아웃/로그인 화면
	         "/login", "/loginPage", "/api/client/login", "/api/client/logout",

	         // 가입 플로우 (승인 전에도 접근 허용)
	         "/client/joinPage", "/client/join-success",

	         // 마이페이지(승인 전에도 안내 화면 보여줘야 함)
	         "/client/clientMyPage", "/client/clientMyPage/**",

	         // 정적/에러
	         "/css/**", "/js/**", "/img/**", "/images/**", "/favicon.ico", "/error"
	     );
	   }

	  @Override
	  public void addResourceHandlers(ResourceHandlerRegistry registry) {
	    registry.addResourceHandler("/uploads/**")
	            .addResourceLocations("file:/home/ec2-user/uploads/");
	  } 
}

