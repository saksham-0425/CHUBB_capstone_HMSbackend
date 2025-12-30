package com.hotel.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

	    http
	        .csrf(csrf -> csrf.disable())
	        .sessionManagement(session ->
	            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
	        )
	        .authorizeHttpRequests(auth -> auth
	            // PUBLIC
	            .requestMatchers(
	                "/auth/login",
	                "/auth/register"
	            ).permitAll()

	            // INTERNAL (gateway already enforces ADMIN)
	            .requestMatchers("/auth/internal/**").permitAll()

	            // Swagger
	            .requestMatchers(
	                "/v3/api-docs/**",
	                "/swagger-ui/**",
	                "/swagger-ui.html"
	            ).permitAll()

	            .requestMatchers("/actuator/health").permitAll()

	            .anyRequest().authenticated()
	        );

	    return http.build();
	}

}
