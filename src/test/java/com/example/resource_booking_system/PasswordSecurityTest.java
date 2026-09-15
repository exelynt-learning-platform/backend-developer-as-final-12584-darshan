package com.example.resource_booking_system;

import com.example.resource_booking_system.dto.auth.RegisterRequest;
import com.example.resource_booking_system.entity.User;
import com.example.resource_booking_system.repository.UserRepository;
import com.example.resource_booking_system.service.AuthService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * NO MOCKS.
 *
 * This test uses:
 * - Real AuthService
 * - Real UserRepository
 * - Real PasswordEncoder
 * - Real H2 database
 *
 * It verifies that passwords are actually stored as BCrypt hashes.
 */
@SpringBootTest
class PasswordSecurityTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registeredPasswordIsStoredAsBcryptHashNotPlainText() {

        String rawPassword = "MySecret123";

        RegisterRequest request = new RegisterRequest(
                "bcrypttestuser",
                "bcrypttest@example.com",
                rawPassword
        );

        authService.register(request);

        User savedUser = userRepository
                .findByUsername("bcrypttestuser")
                .orElseThrow();

        // Password must not be stored as plain text.
        assertNotEquals(
                rawPassword,
                savedUser.getPassword()
        );

        // BCrypt hashes start with $2.
        assertTrue(
                savedUser.getPassword().startsWith("$2")
        );

        // Correct password must match the stored hash.
        assertTrue(
                passwordEncoder.matches(
                        rawPassword,
                        savedUser.getPassword()
                )
        );

        // Incorrect password must not match.
        assertFalse(
                passwordEncoder.matches(
                        "WrongPassword",
                        savedUser.getPassword()
                )
        );
    }
}