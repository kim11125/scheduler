package com.scheduler.backend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.profile-image-dir:./uploads/profile-images}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = uploadDir.startsWith("./")
                ? "file:" + uploadDir.substring(2) + "/"
                : "file:" + uploadDir + "/";

        registry.addResourceHandler("/uploads/profile-images/**")
                .addResourceLocations(location);
    }
}
