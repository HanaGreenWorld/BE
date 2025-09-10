package com.kopo.hanagreenworld.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // challenge_images 폴더의 정적 파일 서빙 설정
        String uploadPath = Paths.get("challenge_images").toAbsolutePath().toString();
        registry.addResourceHandler("/challenge_images/**")
                .addResourceLocations("file:" + uploadPath + "/");
        
        // uploads 폴더도 지원 (기존 호환성)
        String uploadsPath = Paths.get("uploads").toAbsolutePath().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadsPath + "/");
    }
}
