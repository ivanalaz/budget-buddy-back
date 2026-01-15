package com.example.financeapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI financeAppOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Finance App API")
                        .description("Personal Finance Tracking Backend API - Manage categories, entries, monthly overviews, and reports")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Finance App")
                                .email("finance-app@example.com")));
    }
}
