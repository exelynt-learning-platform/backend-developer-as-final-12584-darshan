package com.example.resource_booking_system;

import com.example.resource_booking_system.entity.Reservation;
import com.example.resource_booking_system.entity.Resource;
import com.example.resource_booking_system.entity.User;
import com.example.resource_booking_system.enums.ReservationStatus;
import com.example.resource_booking_system.enums.Role;
import com.example.resource_booking_system.exception.BadRequestException;
import com.example.resource_booking_system.exception.ForbiddenException;
import com.example.resource_booking_system.repository.ReservationRepository;
import com.example.resource_booking_system.repository.ResourceRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservationCancellationTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ResourceRepository resourceRepository;

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
        // OWNER
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
        reservation.setUser(owner);
        reservation.setResource(resource);

        reservation.setStatus(
                ReservationStatus.PENDING
        );

        reservation.setTotalPrice(
                new BigDecimal("1200.00")
        );
    }


    // ==========================================
    // TEST 1:
    // OWNER CAN CANCEL RESERVATION
    // ==========================================

    @Test
    void testOwnerCanCancelReservation() {

        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.of(owner));

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.of(reservation));

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));


        var response =
                reservationService.cancelReservation(
                        100L,
                        "user"
                );


        assertNotNull(response);

        assertEquals(
                100L,
                response.getId()
        );

        assertEquals(
                ReservationStatus.CANCELLED,
                response.getStatus()
        );

        verify(
                reservationRepository,
                times(1)
        ).save(reservation);
    }


    // ==========================================
    // TEST 2:
    // OTHER USER CANNOT CANCEL
    // ==========================================

    @Test
    void testOtherUserCannotCancelReservation() {

        when(userRepository.findByUsername("other"))
                .thenReturn(Optional.of(otherUser));

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.of(reservation));


        assertThrows(
                ForbiddenException.class,
                () -> reservationService.cancelReservation(
                        100L,
                        "other"
                )
        );


        // Reservation must NOT be saved
        verify(
                reservationRepository,
                never()
        ).save(any(Reservation.class));
    }


    // ==========================================
    // TEST 3:
    // ALREADY CANCELLED RESERVATION
    // ==========================================

    @Test
    void testCannotCancelAlreadyCancelledReservation() {

        reservation.setStatus(
                ReservationStatus.CANCELLED
        );

        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.of(owner));

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.of(reservation));


        assertThrows(
                BadRequestException.class,
                () -> reservationService.cancelReservation(
                        100L,
                        "user"
                )
        );


        // Reservation must NOT be saved again
        verify(
                reservationRepository,
                never()
        ).save(any(Reservation.class));
    }
}