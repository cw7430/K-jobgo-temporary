/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.spring.config.LocalWebConfig
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.context.annotation.Profile
 *  org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
 *  org.springframework.web.servlet.config.annotation.WebMvcConfigurer
 */
package com.spring.config;

import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile(value={"local"})
public class LocalWebConfig
implements WebMvcConfigurer {
    @Value(value="${file.upload-dir}")
    private String uploadDir;

    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolute = Paths.get(this.uploadDir, new String[0]).toAbsolutePath().toString();
        registry.addResourceHandler(new String[]{"/uploads/**"}).addResourceLocations(new String[]{"file:///" + absolute + "/"});
    }
}

