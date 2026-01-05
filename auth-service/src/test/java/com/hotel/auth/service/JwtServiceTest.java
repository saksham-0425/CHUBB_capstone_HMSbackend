package com.hotel.auth.service;

import com.hotel.auth.model.Role;
import com.hotel.auth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setup() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(
                jwtService,
                "jwtSecret",
                "mysecretkeymysecretkeymysecretkey123456"
        );
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 3600000L);

        jwtService.init();
    }

    @Test
    void generateAndValidateToken_success() {
        User user = User.builder()
                .email("user@mail.com")
                .role(Role.ADMIN)
                .build();

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void validateToken_invalidToken() {
        assertFalse(jwtService.validateToken("invalid.token.value"));
    }

    @Test
    void extractEmailAndRole_success() {
        User user = User.builder()
                .email("user@mail.com")
                .role(Role.MANAGER)
                .build();

        String token = jwtService.generateToken(user);

        assertEquals("user@mail.com", jwtService.extractEmail(token));
        assertEquals("MANAGER", jwtService.extractRole(token));
    }
}
