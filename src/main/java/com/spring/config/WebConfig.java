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

import com.spring.interceptor.LoginCheckInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig
implements WebMvcConfigurer {
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins(new String[]{"https://kjobgo.com", "https://www.kjobgo.com", "https://k-jobgo.com", "https://www.k-jobgo.com", "http://localhost:8081"}).allowedMethods(new String[]{"GET", "POST", "PUT", "DELETE"}).allowCredentials(true);
    }

    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor((HandlerInterceptor)new LoginCheckInterceptor()).addPathPatterns(new String[]{"/admin/**"}).excludePathPatterns(new String[]{"/home", "/loginPage", "/logout", "/api/login", "/css/**", "/js/**", "/img/**", "/companyInfo", "/"});
    }

    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(new String[]{"/uploads/**"}).addResourceLocations(new String[]{"file:/home/ec2-user/uploads/"});
    }
}

