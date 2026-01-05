package com.hotel.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.auth.dto.CreateManagerRequest;
import com.hotel.auth.service.AuthService;
import com.hotel.auth.service.JwtService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthInternalController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthInternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createManager_success_returns200() throws Exception {
    	CreateManagerRequest request = CreateManagerRequest.builder()
    	        .email("manager@mail.com")
    	        .password("pass")
    	        .build();

        doNothing().when(authService)
                .createOrPromoteManager(anyString(), anyString());

        mockMvc.perform(post("/auth/internal/create-manager")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
