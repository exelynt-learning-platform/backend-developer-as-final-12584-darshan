package com.example.resource_booking_system.service;

import com.example.resource_booking_system.dto.auth.LoginRequest;
import com.example.resource_booking_system.dto.auth.LoginResponse;
import com.example.resource_booking_system.dto.auth.RegisterRequest;
import com.example.resource_booking_system.entity.User;
import com.example.resource_booking_system.enums.Role;
import com.example.resource_booking_system.exception.UserNotFoundException;
import com.example.resource_booking_system.repository.UserRepository;
import com.example.resource_booking_system.security.JwtService;
import com.example.resource_booking_system.exception.BadRequestException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder) {

        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new UserNotFoundException("User not found")
                );

        String token = jwtService.generateToken(
                new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        java.util.List.of(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                        "ROLE_" + user.getRole().name()
                                )
                        )
                )
        );

        return new LoginResponse(
                token,
                user.getUsername(),
                user.getRole().name()
        );
    }

    public void register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        // Every registered account is a USER.
        user.setRole(Role.USER);

        userRepository.save(user);
    }
}