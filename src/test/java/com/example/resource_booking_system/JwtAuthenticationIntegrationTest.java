package com.example.resource_booking_system;

import com.example.resource_booking_system.dto.auth.LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void validJwtGrantsAccessToProtectedEndpoint() throws Exception {

        String loginBody = """
                {
                    "username": "admin",
                    "password": "admin123"
                }
                """;

        String responseJson = mockMvc.perform(
                        post("/auth/login")
                                .contentType(APPLICATION_JSON)
                                .content(loginBody)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoginResponse loginResponse =
                objectMapper.readValue(responseJson, LoginResponse.class);

        String token = loginResponse.getToken();

        mockMvc.perform(
                        get("/resources")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk());
    }

    @Test
    void malformedJwtIsRejected() throws Exception {

        mockMvc.perform(
                        get("/resources")
                                .header(
                                        "Authorization",
                                        "Bearer this.is.not.a.jwt"
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tamperedJwtIsRejected() throws Exception {

        String loginBody = """
                {
                    "username": "admin",
                    "password": "admin123"
                }
                """;

        String responseJson = mockMvc.perform(
                        post("/auth/login")
                                .contentType(APPLICATION_JSON)
                                .content(loginBody)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoginResponse loginResponse =
                objectMapper.readValue(responseJson, LoginResponse.class);

        String token = loginResponse.getToken();

        String tamperedToken =
                token.substring(0, token.length() - 1) + "X";

        mockMvc.perform(
                        get("/resources")
                                .header(
                                        "Authorization",
                                        "Bearer " + tamperedToken
                                )
                )
                .andExpect(status().isUnauthorized());
    }
}