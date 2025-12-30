package com.hotel.hotelservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    public static final String USER_EMAIL_HEADER = "X-User-Email";
    public static final String USER_ROLE_HEADER = "X-User-Role";

    @Bean
    public OpenAPI openAPI() {

        // header-based security scheme
        SecurityScheme userEmailScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name(USER_EMAIL_HEADER);

        SecurityScheme userRoleScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name(USER_ROLE_HEADER);

        return new OpenAPI()
                .info(new Info()
                        .title("Hotel Service API")
                        .description(
                                "Hotel Service for Hotel Reservation System\n\n" +
                                "Authentication is handled by API Gateway.\n" +
                                "This service expects user details via headers:\n" +
                                "- X-User-Email\n" +
                                "- X-User-Role"
                        )
                        .version("1.0.0")
                )
                .addSecurityItem(new SecurityRequirement()
                        .addList(USER_EMAIL_HEADER)
                        .addList(USER_ROLE_HEADER)
                )
                .components(
                        new io.swagger.v3.oas.models.Components()
                                .addSecuritySchemes(USER_EMAIL_HEADER, userEmailScheme)
                                .addSecuritySchemes(USER_ROLE_HEADER, userRoleScheme)
                );
    }
}
