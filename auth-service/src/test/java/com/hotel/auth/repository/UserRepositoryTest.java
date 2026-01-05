package com.hotel.auth.repository;

import com.hotel.auth.model.Role;
import com.hotel.auth.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_existingUser_returnsUser() {
        User user = User.builder()
                .email("user@mail.com")
                .password("encoded")
                .role(Role.GUEST)
                .enabled(true)
                .build();

        userRepository.save(user);

        Optional<User> result = userRepository.findByEmail("user@mail.com");

        assertTrue(result.isPresent());
        assertEquals("user@mail.com", result.get().getEmail());
    }

    @Test
    void findByEmail_nonExistingUser_returnsEmpty() {
        Optional<User> result = userRepository.findByEmail("absent@mail.com");

        assertFalse(result.isPresent());
    }

    @Test
    void existsByEmail_existingUser_returnsTrue() {
        User user = User.builder()
                .email("exists@mail.com")
                .password("encoded")
                .role(Role.GUEST)
                .enabled(true)
                .build();

        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("exists@mail.com");

        assertTrue(exists);
    }

    @Test
    void existsByEmail_nonExistingUser_returnsFalse() {
        boolean exists = userRepository.existsByEmail("missing@mail.com");

        assertFalse(exists);
    }
}
