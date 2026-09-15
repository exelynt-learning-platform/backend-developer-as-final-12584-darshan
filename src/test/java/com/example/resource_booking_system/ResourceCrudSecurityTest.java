package com.example.resource_booking_system;

import com.example.resource_booking_system.dto.resource.ResourceResponse;
import com.example.resource_booking_system.service.ResourceService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ResourceCrudSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResourceService resourceService;

    private static final String REQUEST_BODY = """
            {
                "name": "Updated Room",
                "type": "ROOM",
                "description": "Updated description",
                "pricePerUnit": 750.00,
                "available": true
            }
            """;

    /*
     * TEST 1:
     * ADMIN CAN UPDATE RESOURCE
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanUpdateResource() throws Exception {

        ResourceResponse response = new ResourceResponse(
                1L,
                "Updated Room",
                "ROOM",
                "Updated description",
                new BigDecimal("750.00"),
                true
        );

        when(resourceService.updateResource(anyLong(), any()))
                .thenReturn(response);

        mockMvc.perform(
                        put("/resources/1")
                                .contentType(APPLICATION_JSON)
                                .content(REQUEST_BODY)
                )
                .andExpect(status().isOk());
    }

    /*
     * TEST 2:
     * USER CANNOT UPDATE RESOURCE
     */
    @Test
    @WithMockUser(username = "user", roles = "USER")
    void userCannotUpdateResource() throws Exception {

        mockMvc.perform(
                        put("/resources/1")
                                .contentType(APPLICATION_JSON)
                                .content(REQUEST_BODY)
                )
                .andExpect(status().isForbidden());
    }

    /*
     * TEST 3:
     * ADMIN CAN DELETE RESOURCE
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanDeleteResource() throws Exception {

        mockMvc.perform(
                        delete("/resources/1")
                )
                .andExpect(status().isNoContent());
    }

    /*
     * TEST 4:
     * USER CANNOT DELETE RESOURCE
     */
    @Test
    @WithMockUser(username = "user", roles = "USER")
    void userCannotDeleteResource() throws Exception {

        mockMvc.perform(
                        delete("/resources/1")
                )
                .andExpect(status().isForbidden());
    }
}