package com.example.resource_booking_system;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class UnauthenticatedSecurityTest {

    @Autowired
    private MockMvc mockMvc;


    // ==========================================
    // TEST:
    // UNAUTHENTICATED USER CANNOT ACCESS
    // PROTECTED RESOURCE
    // ==========================================

    @Test
    void testUnauthenticatedUserGets401() throws Exception {

        mockMvc.perform(
                        get("/resources")
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }
}