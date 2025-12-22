package com.example.demo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@SecurityScheme(
        name = "bearerAuth", // Имя схемы безопасности
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT" // Формат токена
)
@OpenAPIDefinition(
        info = @Info(
                title = "API Documentation",
                version = "1.0",
                description = "API Documentation"
        ),
        security = @SecurityRequirement(name = "bearerAuth") // Подключение схемы безопасности
)
@Configuration
public class SwaggerConfig {
}
