package com.hotel.auth.service;

import com.hotel.auth.dto.CreateReceptionistRequest;
import com.hotel.auth.dto.LoginRequest;
import com.hotel.auth.dto.RegisterRequest;
import com.hotel.auth.exception.InvalidCredentialsException;
import com.hotel.auth.exception.UserAlreadyExistsException;
import com.hotel.auth.model.Role;
import com.hotel.auth.model.User;
import com.hotel.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "User already exists with email: " + request.getEmail()
            );
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.GUEST)  
                .enabled(true)
                .build();

        return userRepository.save(user);
    }


    @Override
    public User login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password"));

   
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Check if account is active
        if (!user.isEnabled()) {
            throw new InvalidCredentialsException("User account is disabled");
        }

        return user;
    }
    
    @Override
    public void createOrPromoteManager(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user != null) {
            // User exists -> promote to MANAGER
            user.setRole(Role.MANAGER);
            userRepository.save(user);
        } else {
            // User does not exist -> create MANAGER
            User manager = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .role(Role.MANAGER)
                    .enabled(true)
                    .build();

            userRepository.save(manager);
        }
    }
    @Override
    public void createReceptionist(CreateReceptionistRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("User already exists");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.RECEPTIONIST)
                .enabled(true)
                .build();

        userRepository.save(user);
    }


}
