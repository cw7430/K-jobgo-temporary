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
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
	
	  private final LoginCheckInterceptor loginCheckInterceptor;
	  private final ApprovedCompanyInterceptor approvedCompanyInterceptor;
	  
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
	     registry.addInterceptor(loginCheckInterceptor)
	         .addPathPatterns("/client/**")
	         .excludePathPatterns(
	             // ✅ 회원가입(공개)
	             "/client/joinPage", "/client/join-success", "/join",
	             // ✅ 클라이언트 로그인 API(공개)
	             "/api/client/login", "/api/client/logout",
	             // ✅ 정적/에러
	             "/css/**", "/js/**", "/img/**", "/images/**", "/favicon.ico", "/error"
	         );

	     registry.addInterceptor(approvedCompanyInterceptor)
	         .addPathPatterns("/client/**")
	         // ✅ 회원가입 화면은 승인검사 대상 아님
	         .excludePathPatterns("/client/joinPage", "/client/join-success");
	 }

	  @Override
	  public void addResourceHandlers(ResourceHandlerRegistry registry) {
	    registry.addResourceHandler("/uploads/**")
	            .addResourceLocations("file:/home/ec2-user/uploads/");
	  } 
}

