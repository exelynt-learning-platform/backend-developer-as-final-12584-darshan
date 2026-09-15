package com.example.resource_booking_system.service;

import com.example.resource_booking_system.dto.auth.RegisterRequest;
import com.example.resource_booking_system.entity.User;
import com.example.resource_booking_system.enums.Role;
import com.example.resource_booking_system.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AuthRegistrationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;

    @Mock
    private com.example.resource_booking_system.security.JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest request;


    @BeforeEach
    void setUp() {

        request = new RegisterRequest(
                "darshan",
                "darshan@example.com",
                "darshan123"
        );
    }


    // ==========================================
    // TEST 1:
    // REGISTER NEW USER
    // ==========================================

    @Test
    void shouldRegisterNewUser() {

        when(userRepository.existsByUsername("darshan"))
                .thenReturn(false);

        when(userRepository.existsByEmail("darshan@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("darshan123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        authService.register(request);

        verify(userRepository)
                .save(any(User.class));

        verify(passwordEncoder)
                .encode("darshan123");
    }


    // ==========================================
    // TEST 2:
    // DUPLICATE USERNAME
    // ==========================================

    @Test
    void shouldRejectDuplicateUsername() {

        when(userRepository.existsByUsername("darshan"))
                .thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.register(request)
        );

        assertEquals(
                "Username already exists",
                exception.getMessage()
        );

        verify(userRepository, never())
                .save(any(User.class));
    }


    // ==========================================
    // TEST 3:
    // DUPLICATE EMAIL
    // ==========================================

    @Test
    void shouldRejectDuplicateEmail() {

        when(userRepository.existsByUsername("darshan"))
                .thenReturn(false);

        when(userRepository.existsByEmail("darshan@example.com"))
                .thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.register(request)
        );

        assertEquals(
                "Email already exists",
                exception.getMessage()
        );

        verify(userRepository, never())
                .save(any(User.class));
    }


    // ==========================================
    // TEST 4:
    // NEW USER ALWAYS GETS USER ROLE
    // ==========================================

    @Test
    void newlyRegisteredUserShouldAlwaysHaveUserRole() {

        when(userRepository.existsByUsername("darshan"))
                .thenReturn(false);

        when(userRepository.existsByEmail("darshan@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("darshan123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        authService.register(request);

        verify(userRepository).save(
                argThat(user ->
                        user.getRole() == Role.USER
                )
        );
    }
}