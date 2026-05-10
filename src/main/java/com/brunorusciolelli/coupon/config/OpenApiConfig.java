package com.brunorusciolelli.coupon.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI couponOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Coupon API")
                        .description("REST API for coupon management — Outforce technical challenge")
                        .version("1.0.0"));
    }
}