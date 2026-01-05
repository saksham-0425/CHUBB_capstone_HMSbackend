package com.hotel.auth.service;

import com.hotel.auth.dto.CreateReceptionistRequest;
import com.hotel.auth.dto.LoginRequest;
import com.hotel.auth.dto.RegisterRequest;
import com.hotel.auth.exception.InvalidCredentialsException;
import com.hotel.auth.exception.UserAlreadyExistsException;
import com.hotel.auth.model.Role;
import com.hotel.auth.model.User;
import com.hotel.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setup() {
        registerRequest = new RegisterRequest("test@mail.com", "password");
        loginRequest = new LoginRequest("test@mail.com", "password");
    }

    @Test
    void register_success() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User user = authService.register(registerRequest);

        assertEquals(Role.GUEST, user.getRole());
        assertTrue(user.isEnabled());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsException() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(registerRequest));

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success() {
        User user = User.builder()
                .email("test@mail.com")
                .password("encoded")
                .enabled(true)
                .build();

        when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded"))
                .thenReturn(true);

        User result = authService.login(loginRequest);

        assertEquals("test@mail.com", result.getEmail());
    }

    @Test
    void login_invalidPassword_throwsException() {
        User user = User.builder()
                .email("test@mail.com")
                .password("encoded")
                .enabled(true)
                .build();

        when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any()))
                .thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(loginRequest));
    }

    @Test
    void login_disabledAccount_throwsException() {
        User user = User.builder()
                .email("test@mail.com")
                .password("encoded")
                .enabled(false)
                .build();

        when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any()))
                .thenReturn(true);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(loginRequest));
    }

    @Test
    void promoteExistingUserToManager() {
        User user = User.builder()
                .email("manager@mail.com")
                .role(Role.GUEST)
                .build();

        when(userRepository.findByEmail("manager@mail.com"))
                .thenReturn(Optional.of(user));

        authService.createOrPromoteManager("manager@mail.com", "pass");

        assertEquals(Role.MANAGER, user.getRole());
        verify(userRepository).save(user);
    }

    @Test
    void createNewManager() {
        when(userRepository.findByEmail("manager@mail.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass"))
                .thenReturn("encoded");

        authService.createOrPromoteManager("manager@mail.com", "pass");

        verify(userRepository).save(any(User.class));
    }

    @Test
    void createReceptionist_success() {
        CreateReceptionistRequest request =
                new CreateReceptionistRequest("rec@mail.com", "pass");

        when(userRepository.existsByEmail("rec@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");

        authService.createReceptionist(request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void createReceptionist_duplicateEmail_throwsException() {
        CreateReceptionistRequest request =
                new CreateReceptionistRequest("rec@mail.com", "pass");

        when(userRepository.existsByEmail("rec@mail.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> authService.createReceptionist(request));
    }
}
