package com.proyectmanager.demo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Configura Swagger UI con soporte para JWT.
 * El botón "Authorize" en Swagger permite ingresar el token
 * para probar endpoints protegidos directamente desde el navegador.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "SaaS Project Manager API",
                version = "1.0",
                description = "MVP de gestión de proyectos con workspaces y roles"
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig {
}