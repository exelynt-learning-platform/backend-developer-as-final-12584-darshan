package com.example.resource_booking_system.controller;

import com.example.resource_booking_system.dto.auth.LoginRequest;
import com.example.resource_booking_system.dto.auth.LoginResponse;
import com.example.resource_booking_system.dto.auth.RegisterRequest;
import com.example.resource_booking_system.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(
        name = "Authentication",
        description = "User registration and login APIs"
)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticates a user and returns a JWT token"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid username or password"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            )
    })
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new USER account"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or duplicate username/email"
            )
    })
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request) {

        authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("User registered successfully");
    }
}