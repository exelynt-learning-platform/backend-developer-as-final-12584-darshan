package com.example.resource_booking_system;

import com.example.resource_booking_system.dto.auth.LoginResponse;
import com.example.resource_booking_system.service.AuthService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;


    // ==========================================
    // TEST 1:
    // VALID LOGIN
    // ==========================================

    @Test
    void testValidLogin() throws Exception {

        LoginResponse response =
                new LoginResponse(
                        "test-jwt-token",
                        "admin",
                        "ADMIN"
                );

        when(authService.login(any()))
                .thenReturn(response);


        String requestBody = """
                {
                    "username": "admin",
                    "password": "admin123"
                }
                """;


        mockMvc.perform(
                        post("/auth/login")
                                .contentType(APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(
                        status().isOk()
                );
    }


    // ==========================================
    // TEST 2:
    // WRONG PASSWORD
    // ==========================================

    @Test
    void testLoginWithWrongPassword() throws Exception {

        when(authService.login(any()))
                .thenThrow(
                        new org.springframework.security.authentication
                                .BadCredentialsException(
                                "Bad credentials"
                        )
                );


        String requestBody = """
                {
                    "username": "admin",
                    "password": "wrongpassword"
                }
                """;


        mockMvc.perform(
                        post("/auth/login")
                                .contentType(APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }


    // ==========================================
    // TEST 3:
    // MISSING USERNAME
    // ==========================================

    @Test
    void testLoginWithMissingUsername() throws Exception {

        String requestBody = """
                {
                    "username": "",
                    "password": "admin123"
                }
                """;


        mockMvc.perform(
                        post("/auth/login")
                                .contentType(APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(
                        status().isBadRequest()
                );
    }
}