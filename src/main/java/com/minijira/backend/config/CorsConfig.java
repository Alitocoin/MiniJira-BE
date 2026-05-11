package com.minijira.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Orígenes permitidos (Vite y CRA)
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173"
        ));

        // Métodos HTTP permitidos
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // Headers permitidos
        config.setAllowedHeaders(List.of("*"));

        // Permite enviar cookies/credenciales
        config.setAllowCredentials(true);

        // Cache de preflight en segundos
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        return new CorsFilter(source);
    }
}
