package com.seein.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger(OpenAPI) 설정
 * API 문서 메타데이터 및 JWT 인증 스키마 구성
 */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, securityScheme()));
    }

    /**
     * API 메타데이터 정보
     */
    private Info apiInfo() {
        return new Info()
                .title("SEE-INSIGHT API")
                .description("AI 기반 뉴스 요약 및 키워드 구독 서비스 API")
                .version("v1.0")
                .contact(new Contact()
                        .name("SEE-INSIGHT Team")
                        .email("seein@example.com"));
    }

    /**
     * JWT Bearer 인증 스키마
     */
    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("JWT Access Token을 입력하세요. (Bearer 접두사 없이 토큰만 입력)");
    }
}
