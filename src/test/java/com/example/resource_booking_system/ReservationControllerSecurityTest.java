package com.example.resource_booking_system;

import com.example.resource_booking_system.dto.reservation.ReservationResponse;
import com.example.resource_booking_system.service.ReservationService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class ReservationControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;


    // ==========================================
    // TEST 1:
    // USER CAN CREATE RESERVATION
    // ==========================================

    @Test
    @WithMockUser(
            username = "user",
            roles = "USER"
    )
    void userCanCreateReservation() throws Exception {

        ReservationResponse response =
                new ReservationResponse(
                        100L,
                        1L,
                        1L,
                        "Meeting Room",
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(1).plusHours(2),
                        com.example.resource_booking_system.enums.ReservationStatus.PENDING,
                        new BigDecimal("1200.00")
                );

        when(reservationService.createReservation(
                any(),
                eq("user")
        )).thenReturn(response);


        String requestBody = """
                {
                    "resourceId": 1,
                    "startTime": "2026-10-01T10:00:00",
                    "endTime": "2026-10-01T12:00:00"
                }
                """;


        mockMvc.perform(
                        post("/reservations")
                                .contentType(APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(
                        status().isCreated()
                );
    }


    // ==========================================
    // TEST 2:
    // USER CANNOT ACCESS ANOTHER USER'S
    // RESERVATION
    // ==========================================

    @Test
    @WithMockUser(
            username = "other",
            roles = "USER"
    )
    void userCannotAccessAnotherUsersReservation()
            throws Exception {

        when(reservationService.getReservationById(
                eq(100L),
                eq("other")
        )).thenThrow(
                new com.example.resource_booking_system.exception
                        .ForbiddenException(
                        "You are not allowed to access this reservation"
                )
        );


        mockMvc.perform(
                        get("/reservations/100")
                )
                .andExpect(
                        status().isForbidden()
                );
    }


    // ==========================================
    // TEST 3:
    // ADMIN CAN ACCESS ALL RESERVATIONS
    // ==========================================

    @Test
    @WithMockUser(
            username = "admin",
            roles = "ADMIN"
    )
    void adminCanAccessAllReservations()
            throws Exception {

        when(reservationService.getAllReservations(
                any(),
                any(),
                any(),
                any()
        )).thenReturn(
                new org.springframework.data.domain.PageImpl<>(
                        List.of()
                )
        );


        mockMvc.perform(
                        get("/reservations/all")
                )
                .andExpect(
                        status().isOk()
                );
    }
}