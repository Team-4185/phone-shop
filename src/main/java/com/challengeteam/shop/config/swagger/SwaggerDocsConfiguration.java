package com.challengeteam.shop.config.swagger;

import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!prod")
public class SwaggerDocsConfiguration {

    @Bean
    public OpenApiCustomizer jwtAuthCustomizer() {
        return openApi -> {
            SecurityScheme securityScheme = new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Authorization approach with JWT");

            openApi.getComponents().addSecuritySchemes("bearer-jwt", securityScheme);
        };
    }

}
