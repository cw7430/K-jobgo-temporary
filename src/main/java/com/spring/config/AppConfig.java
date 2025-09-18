/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.spring.config.AppConfig
 *  com.spring.service.FileService
 *  com.spring.service.LocalFileService
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 */
package com.spring.config;

import com.spring.service.FileService;
import com.spring.service.LocalFileService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    @Bean
    public FileService fileService() {
        return new LocalFileService();
    }
    
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

