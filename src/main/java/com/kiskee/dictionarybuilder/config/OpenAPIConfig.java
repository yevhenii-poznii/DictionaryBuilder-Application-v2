package com.kiskee.dictionarybuilder.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Value("${swagger.server.url}")
    private String swaggerServerUrl;

    @Bean
    public OpenAPI openAPI() {
        final String bearerSecuritySchemeName = "Bearer";
        return new OpenAPI()
                .info(new Info().title("Dictionary Builder API").version("v1"))
                .servers(List.of(new Server().url(swaggerServerUrl)))
                .components(new Components()
                        .addSecuritySchemes(
                                bearerSecuritySchemeName,
                                new SecurityScheme()
                                        .name(bearerSecuritySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(bearerSecuritySchemeName));
    }
}
