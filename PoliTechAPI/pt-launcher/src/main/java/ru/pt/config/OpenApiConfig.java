package ru.pt.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger конфигурация
 */
@Configuration
public class OpenApiConfig {

    /**
     * Конфигурация OpenAPI документации
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .components(new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT token для авторизации. " +
                            "Получить токен можно через /api/auth/login или /api/auth/token")
                )
            )
            .info(new Info()
                .title("PoliTech API")
                .version("1.0.0")
                .description("REST API для PoliTech системы управления")
                .contact(new Contact()
                    .name("PoliTech Support")
                    .url("https://politech.ru")
                    .email("support@politech.ru")
                )
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")
                )
            );
    }
}

