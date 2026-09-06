package com.procurement.fx.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Procurement FX API — Supplier Quote Comparison")
                        .version("1.0.0")
                        .description("RESTful service for comparing foreign supplier quotes with live exchange rate conversion (Frankfurter ECB rates), database caching, and budget limit evaluation (Case A-002).")
                        .contact(new Contact()
                                .name("Regi Purnama")
                                .email("regiprnm432@gmail.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
