package com.atirath.trading.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tradingPlatformOpenAPI() {
        final String securitySchemeName = "BearerAuthentication";

        return new OpenAPI()
                .info(new Info()
                        .title("ATIRATH GROUP - Internal Share Market Platform API")
                        .description("REST API Backend for Web & Mobile Applications: Authentication, 2FA OTP, Biometric Auth, and Session Management.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Atirath Engineering Team")
                                .email("dev@atirathgroup.com")
                                .url("https://atirathgroup.com"))
                        .license(new License()
                                .name("Proprietary - Atirath Group")
                                .url("https://atirathgroup.com/terms")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token in the format: Bearer <token>")));
    }
}
