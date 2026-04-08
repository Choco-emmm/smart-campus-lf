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

                .components(new Components()
                        .addSecuritySchemes("token", new SecurityScheme()
                                .name("token") // 这里的名字必须和拦截器中读取的一致
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                // 🌟 增加下面这一行描述，解决“没有信息”的问题
                                .description("请输入登录接口返回的 token。注意：不要加 'Bearer ' 前缀，直接粘贴字符串即可。")
                        ))

                .addSecurityItem(new SecurityRequirement().addList("token"));
    }
}