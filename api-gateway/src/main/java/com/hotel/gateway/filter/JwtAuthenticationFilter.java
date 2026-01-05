package com.hotel.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import static com.hotel.gateway.filter.RoleConstants.*;
import com.hotel.gateway.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter {


    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();
        
        if (exchange.getRequest().getMethod().name().equals("OPTIONS")) {
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            return exchange.getResponse().setComplete();
        }
        
        if (path.equals("/hotels/internal/manager")) {
            return authorize(exchange, chain, MANAGER);
        }
        
        if (path.equals("/hotels/internal/receptionist")) {
            return authorize(exchange, chain, RECEPTIONIST);
        }


        if (path.equals("/auth/internal/create-manager")) {
            return authorize(exchange, chain, ADMIN);
        }

        if (path.equals("/auth/internal/receptionists")) {
            return authorize(exchange, chain, MANAGER);
        }
        
        if (path.matches("/hotels/.*/availability")) {
            return chain.filter(exchange);
        }
        
        if (path.matches("/hotels/.*/bookings")) {
            return authorize(exchange, chain, ADMIN);
        }
        
        if (path.equals("/bookings/manager")) {
            return authorize(exchange, chain, MANAGER);
        }
        
        if (path.matches("/hotels/\\d+/rooms") && "GET".equals(method)) {
            return authorizeAndForward(exchange, chain);
        }
        
        // PUBLIC APIs
        if (
            path.startsWith("/auth/") ||
            ("GET".equals(method) &&
                (
                    path.equals("/hotels") ||
                    path.startsWith("/hotels/search") ||
                    path.matches("/hotels/\\d+") ||
                    path.matches("/hotels/.+/categories")
                )
            )
        ) {
            return chain.filter(exchange);
        }

        // PROTECTED APIs
        return authorizeAndForward(exchange, chain);
    }

    private Mono<Void> authorize(
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            String requiredRole
    ) {
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            return unauthorized(exchange);
        }

        if (!requiredRole.equals(jwtUtil.extractRole(token))) {
            return unauthorized(exchange);
        }

        // Forward headers
        ServerHttpRequest modifiedRequest = exchange.getRequest()
                .mutate()
                .header("X-User-Email", jwtUtil.extractEmail(token))
                .header("X-User-Role", jwtUtil.extractRole(token))
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private Mono<Void> authorizeAndForward(
            ServerWebExchange exchange,
            GatewayFilterChain chain
    ) {
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            return unauthorized(exchange);
        }

        ServerHttpRequest modifiedRequest = exchange.getRequest()
                .mutate()
                .header("X-User-Email", jwtUtil.extractEmail(token))
                .header("X-User-Role", jwtUtil.extractRole(token))
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
