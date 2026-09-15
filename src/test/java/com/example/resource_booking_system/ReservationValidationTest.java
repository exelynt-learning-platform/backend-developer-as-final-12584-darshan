package com.example.resource_booking_system;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.resource_booking_system.service.ReservationService;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class ReservationValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;


    // ==========================================
    // TEST 1:
    // REQUIRED FIELDS ARE MISSING
    // ==========================================

    @Test
    @WithMockUser(
            username = "user",
            roles = "USER"
    )
    void shouldRejectMissingRequiredFields() throws Exception {

        String requestBody = """
                {
                }
                """;

        mockMvc.perform(
                        post("/reservations")
                                .contentType(APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(
                        status().isBadRequest()
                );
    }


    // ==========================================
    // TEST 2:
    // START TIME CANNOT BE IN THE PAST
    // ==========================================

    @Test
    @WithMockUser(
            username = "user",
            roles = "USER"
    )
    void shouldRejectPastStartTime() throws Exception {

        String requestBody = """
                {
                    "resourceId": 1,
                    "startTime": "2020-01-01T10:00:00",
                    "endTime": "2020-01-01T11:00:00"
                }
                """;

        mockMvc.perform(
                        post("/reservations")
                                .contentType(APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(
                        status().isBadRequest()
                );
    }


    // ==========================================
    // TEST 3:
    // USER CANNOT DELETE RESERVATION
    // ==========================================

    @Test
    @WithMockUser(
            username = "user",
            roles = "USER"
    )
    void userCannotDeleteReservation() throws Exception {

        mockMvc.perform(
                        delete("/reservations/1")
                )
                .andExpect(
                        status().isForbidden()
                );
    }


    // ==========================================
    // TEST 4:
    // USER CANNOT UPDATE RESERVATION
    // ==========================================

    @Test
    @WithMockUser(
            username = "user",
            roles = "USER"
    )
    void userCannotUpdateReservation() throws Exception {

        String requestBody = """
        {
            "resourceId": 1,
            "startTime": "2030-01-01T10:00:00",
            "endTime": "2030-01-01T11:00:00",
            "status": "CONFIRMED"
        }
        """;

        mockMvc.perform(
                        put("/reservations/1")
                                .contentType(APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(
                        status().isForbidden()
                );
    }
}