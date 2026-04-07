package com.choco.smartlf.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("校园AI失物招领平台 - API文档")
                        .description("基于Spring Boot 3 + Spring AI + Redis的校园风控失物招领平台")
                        .version("v1.0")
                        .contact(new Contact().name("Choco").email("renpei233@gmail.com")))

                // 1. 定义全局 Token 规则：告诉 Swagger 我们用的是放置在 Header 中的字段，名字叫 "token"
                .components(new Components()
                        .addSecuritySchemes("token", new SecurityScheme()
                                .name("token")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                        ))

                // 2. 将这个规则应用到全局所有的接口上
                .addSecurityItem(new SecurityRequirement().addList("token"));
    }
}