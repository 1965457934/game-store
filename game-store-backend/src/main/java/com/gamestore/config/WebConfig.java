package com.gamestore.config;

import com.gamestore.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private AuthInterceptor authInterceptor;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/register",
                        "/user/login",
                        "/game/list",
                        "/game/detail/**",
                        "/game/category/**",
                        "/banner/list",
                        "/comment/list/**",
                        "/api/upload/**",
                        "/api/img/**",
                        "/api/file/upload"
                );
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 上传文件访问路径 - 允许无认证访问
        // Windows 路径需要使用 file:/// 协议
        registry.addResourceHandler("/api/upload/**")
                .addResourceLocations("file:///E:/game-store/upload/");
    }
}
