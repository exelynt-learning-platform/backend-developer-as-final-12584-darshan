package com.example.resource_booking_system;

import com.example.resource_booking_system.dto.auth.LoginRequest;
import com.example.resource_booking_system.dto.auth.LoginResponse;
import com.example.resource_booking_system.service.AuthService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthControllerTest {

    @Autowired
    private AuthService authService;


    // ==========================================
    // TEST 1: VALID LOGIN
    // ==========================================

    @Test
    void testLoginWithValidCredentials() {

        LoginRequest request =
                new LoginRequest(
                        "admin",
                        "admin123"
                );

        LoginResponse response =
                authService.login(request);

        assertNotNull(response);

        assertNotNull(response.getToken());

        assertFalse(
                response.getToken().isBlank()
        );

        assertEquals(
                "admin",
                response.getUsername()
        );

        assertEquals(
                "ADMIN",
                response.getRole()
        );
    }


    // ==========================================
    // TEST 2: WRONG PASSWORD
    // ==========================================

    @Test
    void testLoginWithWrongPassword() {

        LoginRequest request =
                new LoginRequest(
                        "admin",
                        "wrongpassword"
                );

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );
    }


    // ==========================================
    // TEST 3: MISSING USERNAME
    // ==========================================

    @Test
    void testLoginWithMissingUsername() {

        LoginRequest request =
                new LoginRequest(
                        "",
                        "admin123"
                );

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );
    }
}