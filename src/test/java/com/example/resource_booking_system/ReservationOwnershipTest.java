package com.example.resource_booking_system;

import com.example.resource_booking_system.entity.Reservation;
import com.example.resource_booking_system.entity.Resource;
import com.example.resource_booking_system.entity.User;
import com.example.resource_booking_system.enums.Role;
import com.example.resource_booking_system.exception.ForbiddenException;
import com.example.resource_booking_system.repository.ReservationRepository;
import com.example.resource_booking_system.repository.UserRepository;
import com.example.resource_booking_system.service.ReservationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationOwnershipTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationService reservationService;

    private User owner;
    private User otherUser;
    private Resource resource;
    private Reservation reservation;


    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        // ==========================================
        // OWNER USER
        // ==========================================

        owner = new User();

        owner.setId(1L);
        owner.setUsername("user");
        owner.setEmail("user@example.com");
        owner.setPassword("password");
        owner.setRole(Role.USER);


        // ==========================================
        // OTHER USER
        // ==========================================

        otherUser = new User();

        otherUser.setId(2L);
        otherUser.setUsername("other");
        otherUser.setEmail("other@example.com");
        otherUser.setPassword("password");
        otherUser.setRole(Role.USER);


        // ==========================================
        // RESOURCE
        // ==========================================

        resource = new Resource();

        resource.setId(1L);
        resource.setName("Meeting Room");
        resource.setType("ROOM");
        resource.setDescription("Test meeting room");
        resource.setPricePerUnit(
                new BigDecimal("600.00")
        );
        resource.setAvailable(true);


        // ==========================================
        // RESERVATION
        // ==========================================

        reservation = new Reservation();

        reservation.setId(100L);

        // Reservation belongs to owner
        reservation.setUser(owner);

        // Reservation uses this resource
        reservation.setResource(resource);
    }


    // ==========================================
    // TEST 1:
    // OWNER CAN ACCESS RESERVATION
    // ==========================================

    @Test
    void testOwnerCanAccessReservation() {

        // Find logged-in user
        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.of(owner));

        // Find reservation
        when(reservationRepository.findById(100L))
                .thenReturn(Optional.of(reservation));


        var response =
                reservationService.getReservationById(
                        100L,
                        "user"
                );


        // Response should exist
        assertNotNull(response);

        // Reservation ID
        assertEquals(
                100L,
                response.getId()
        );

        // Owner ID
        assertEquals(
                1L,
                response.getUserId()
        );

        // Resource ID
        assertEquals(
                1L,
                response.getResourceId()
        );

        // Resource name
        assertEquals(
                "Meeting Room",
                response.getResourceName()
        );
    }


    // ==========================================
    // TEST 2:
    // OTHER USER CANNOT ACCESS RESERVATION
    // ==========================================

    @Test
    void testOtherUserCannotAccessReservation() {

        // Logged-in user is "other"
        when(userRepository.findByUsername("other"))
                .thenReturn(Optional.of(otherUser));

        // Reservation belongs to "user"
        when(reservationRepository.findById(100L))
                .thenReturn(Optional.of(reservation));


        // Other user must be rejected
        assertThrows(
                ForbiddenException.class,
                () -> reservationService.getReservationById(
                        100L,
                        "other"
                )
        );
    }
}