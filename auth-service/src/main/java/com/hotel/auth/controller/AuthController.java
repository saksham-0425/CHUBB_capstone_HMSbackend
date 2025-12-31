package com.hotel.auth.controller;

import com.hotel.auth.dto.AuthResponse;
import com.hotel.auth.dto.CreateReceptionistRequest;
import com.hotel.auth.dto.LoginRequest;
import com.hotel.auth.dto.RegisterRequest;
import com.hotel.auth.exception.UnauthorizedException;
import com.hotel.auth.model.User;
import com.hotel.auth.service.AuthService;
import com.hotel.auth.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

//register a new user
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request) {

        authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("User registered successfully");
    }

//login user and generate jwt
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        User user = authService.login(request);

        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(
                AuthResponse.builder()
                        .token(token)
                        .role(user.getRole())
                        .build()
        );
    }
    
    @PostMapping("/internal/receptionists")
    public ResponseEntity<Void> createReceptionist(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody CreateReceptionistRequest request
    ) {
        if (!"MANAGER".equals(role)) {
            throw new UnauthorizedException("Only MANAGER can create receptionist");
        }
        authService.createReceptionist(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    
}
