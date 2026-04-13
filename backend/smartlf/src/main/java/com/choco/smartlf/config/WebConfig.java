package com.choco.smartlf.config;

import com.choco.smartlf.interceptor.TokenInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TokenInterceptor tokenInterceptor;

    // 自动读取环境变量或 yaml 里的文件存储的前缀
    @Value("${system.file-prefix:D:/drms/upload/}")
    private String filePrefix;


    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // 允许携带 Cookie/Token
        config.addAllowedOriginPattern("*"); // 允许所有来源
        config.addAllowedHeader("*"); // 允许所有请求头
        config.addAllowedMethod("*"); // 允许所有方法 (GET, POST, OPTIONS 等)
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/user/login", "/user/register", "/user/is*","/images/**","/ws/**", // 给 WebSocket 放行
                        // --- 下面是给 Swagger 和 Knife4j 放行的路径 ---
                        "/doc.html",               // Knife4j 页面
                        "/swagger-ui/**",          // Swagger UI 页面
                        "/v3/api-docs/**",         // OpenAPI 3.0 的 JSON 数据接口
                        "/webjars/**"  );            // 前端静态资源);
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 当浏览器访问 http://localhost:8080/images/xxx.jpg 时
        // SpringBoot 自动去目录找 xxx.jpg
        registry.addResourceHandler("images/**")
                .addResourceLocations("file:" + filePrefix);
    }
}
