package org.example.usermanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        private static final String SECURITY_SCHEME_NAME = "bearerAuth";

        @Bean
        public OpenAPI userManagementOpenApi() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("User Management API")
                                                .version("1.0.0")
                                                .description(
                                                                "API quản lý người dùng, xác thực JWT, "
                                                                                + "quản lý hồ sơ và chức năng quản trị"))
                                .components(new Components()
                                                .addSecuritySchemes(
                                                                SECURITY_SCHEME_NAME,
                                                                new SecurityScheme()
                                                                                .name(SECURITY_SCHEME_NAME)
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")));
        }
}