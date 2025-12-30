package com.hotel.auth.controller;

import com.hotel.auth.dto.CreateManagerRequest;
import com.hotel.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/internal")
@RequiredArgsConstructor
public class AuthInternalController {

    private final AuthService authService;

    @PostMapping("/create-manager")
    public ResponseEntity<Void> createManager(
            @RequestBody @Valid CreateManagerRequest request) {

        authService.createOrPromoteManager(
                request.getEmail(),
                request.getPassword()
        );

        return ResponseEntity.ok().build();
    }
}
