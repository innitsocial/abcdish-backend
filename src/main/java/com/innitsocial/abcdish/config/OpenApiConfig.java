package com.innitsocial.abcdish.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI abcdishOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ABCDish API")
                        .description("Backend API for ABCDish cooking and recipe video app")
                        .version("v1.0.0"));
    }
}