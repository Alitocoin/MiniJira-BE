package com.minijira.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI miniJiraOpenAPI() {
        final String bearerSchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("MiniJira API")
                        .description("API REST del sistema MiniJira. Gestiona tareas, proyectos y usuarios con autenticación JWT. " +
                                "Usá el botón **Authorize** para ingresar tu token Bearer y probar los endpoints protegidos.")
                        .version("v0.5.0")
                        .contact(new Contact()
                                .name("Equipo DevForce")
                                .url("https://github.com/Alitocoin/MiniJira-BE")))
                .addSecurityItem(new SecurityRequirement().addList(bearerSchemeName))
                .components(new Components()
                        .addSecuritySchemes(bearerSchemeName, new SecurityScheme()
                                .name(bearerSchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingresá el token JWT obtenido en POST /api/auth/login. Formato: <token> (sin el prefijo Bearer)")));
    }
}
