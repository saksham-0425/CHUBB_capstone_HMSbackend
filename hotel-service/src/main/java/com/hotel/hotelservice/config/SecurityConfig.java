package com.hotel.hotelservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    "/hotels",
                    "/hotels/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/hotels/*/categories"
                ).permitAll()
                // Everything else is allowed because
                // RBAC is handled in service layer
                .anyRequest().permitAll()
            );

        return http.build();
    }
}
