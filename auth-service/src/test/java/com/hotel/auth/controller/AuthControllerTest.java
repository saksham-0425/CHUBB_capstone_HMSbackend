package com.hotel.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.auth.dto.CreateReceptionistRequest;
import com.hotel.auth.dto.LoginRequest;
import com.hotel.auth.dto.RegisterRequest;
import com.hotel.auth.model.Role;
import com.hotel.auth.model.User;
import com.hotel.auth.service.AuthService;
import com.hotel.auth.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_success_returns201() throws Exception {
        RegisterRequest request =
                new RegisterRequest("user@mail.com", "password");

        when(authService.register(any(RegisterRequest.class)))
        .thenReturn(User.builder().build());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully"));
    }

    @Test
    void login_success_returnsTokenAndRole() throws Exception {
        LoginRequest request =
                new LoginRequest("user@mail.com", "password");

        User user = User.builder()
                .email("user@mail.com")
                .role(Role.GUEST)
                .enabled(true)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.role").value("GUEST"));
    }

    @Test
    void createReceptionist_withManagerRole_returns201() throws Exception {
        CreateReceptionistRequest request =
                new CreateReceptionistRequest("rec@mail.com", "pass");

        doNothing().when(authService).createReceptionist(any());

        mockMvc.perform(post("/auth/internal/receptionists")
                        .header("X-User-Role", "MANAGER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void createReceptionist_withNonManagerRole_returns401() throws Exception {
        CreateReceptionistRequest request =
                new CreateReceptionistRequest("rec@mail.com", "pass");

        mockMvc.perform(post("/auth/internal/receptionists")
                        .header("X-User-Role", "GUEST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
    }
}
