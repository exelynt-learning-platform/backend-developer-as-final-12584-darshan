package com.example.resource_booking_system;

import com.example.resource_booking_system.dto.resource.ResourceResponse;
import com.example.resource_booking_system.service.ResourceService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class ResourceControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResourceService resourceService;


    // ==========================================
    // TEST 1:
    // USER CAN READ RESOURCES
    // ==========================================

    @Test
    @WithMockUser(
            username = "user",
            roles = "USER"
    )
    void userCanGetResources() throws Exception {

        when(resourceService.getAllResources(any()))
                .thenReturn(
                        new PageImpl<>(
                                List.of(),
                                PageRequest.of(0, 10),
                                0
                        )
                );

        mockMvc.perform(
                        get("/resources")
                )
                .andExpect(
                        status().isOk()
                );
    }


    // ==========================================
    // TEST 2:
    // USER CANNOT CREATE RESOURCE
    // ==========================================

    @Test
    @WithMockUser(
            username = "user",
            roles = "USER"
    )
    void userCannotCreateResource() throws Exception {

        String requestBody = """
                {
                    "name": "Test Room",
                    "type": "ROOM",
                    "description": "Test resource",
                    "pricePerUnit": 500.00,
                    "available": true
                }
                """;

        mockMvc.perform(
                        post("/resources")
                                .contentType(APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(
                        status().isForbidden()
                );
    }


    // ==========================================
    // TEST 3:
    // ADMIN CAN CREATE RESOURCE
    // ==========================================

    @Test
    @WithMockUser(
            username = "admin",
            roles = "ADMIN"
    )
    void adminCanCreateResource() throws Exception {

        ResourceResponse response =
                new ResourceResponse(
                        100L,
                        "Test Room",
                        "ROOM",
                        "Test resource",
                        new BigDecimal("500.00"),
                        true
                );

        when(resourceService.createResource(any()))
                .thenReturn(response);


        String requestBody = """
                {
                    "name": "Test Room",
                    "type": "ROOM",
                    "description": "Test resource",
                    "pricePerUnit": 500.00,
                    "available": true
                }
                """;


        mockMvc.perform(
                        post("/resources")
                                .contentType(APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(
                        status().isCreated()
                );
    }
}