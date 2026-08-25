package com.example.validation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI validationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Validation Service API")
                        .description("API Documentation for Validation Service")
                        .version("v1.0.0"));
    }
}
