package com.example.resource_booking_system;

import com.example.resource_booking_system.dto.reservation.ReservationResponse;
import com.example.resource_booking_system.entity.Reservation;
import com.example.resource_booking_system.entity.Resource;
import com.example.resource_booking_system.entity.User;
import com.example.resource_booking_system.enums.ReservationStatus;
import com.example.resource_booking_system.enums.Role;
import com.example.resource_booking_system.repository.ReservationRepository;
import com.example.resource_booking_system.repository.ResourceRepository;
import com.example.resource_booking_system.repository.UserRepository;
import com.example.resource_booking_system.service.ReservationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservationFilterTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationService reservationService;

    private User user;
    private Resource resource;
    private Reservation pendingReservation;
    private Reservation confirmedReservation;


    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        // ==========================================
        // USER
        // ==========================================

        user = new User();

        user.setId(1L);
        user.setUsername("user");
        user.setEmail("user@example.com");
        user.setPassword("password");
        user.setRole(Role.USER);


        // ==========================================
        // RESOURCE
        // ==========================================

        resource = new Resource();

        resource.setId(1L);
        resource.setName("Meeting Room");
        resource.setType("ROOM");
        resource.setDescription("Test room");
        resource.setPricePerUnit(
                new BigDecimal("600.00")
        );
        resource.setAvailable(true);


        // ==========================================
        // PENDING RESERVATION
        // ==========================================

        pendingReservation = new Reservation();

        pendingReservation.setId(100L);
        pendingReservation.setUser(user);
        pendingReservation.setResource(resource);

        pendingReservation.setStatus(
                ReservationStatus.PENDING
        );

        pendingReservation.setTotalPrice(
                new BigDecimal("1200.00")
        );


        // ==========================================
        // CONFIRMED RESERVATION
        // ==========================================

        confirmedReservation = new Reservation();

        confirmedReservation.setId(101L);
        confirmedReservation.setUser(user);
        confirmedReservation.setResource(resource);

        confirmedReservation.setStatus(
                ReservationStatus.CONFIRMED
        );

        confirmedReservation.setTotalPrice(
                new BigDecimal("2400.00")
        );
    }


    // ==========================================
    // TEST 1:
    // FILTER BY STATUS
    // ==========================================

    @Test
    void testFilterByStatus() {

        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.of(user));

        Page<Reservation> page =
                new PageImpl<>(
                        List.of(pendingReservation)
                );

        when(reservationRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);


        Pageable pageable =
                PageRequest.of(0, 10);


        Page<ReservationResponse> result =
                reservationService.getUserReservations(
                        "user",
                        ReservationStatus.PENDING,
                        null,
                        null,
                        pageable
                );


        assertNotNull(result);

        assertEquals(
                1,
                result.getTotalElements()
        );

        assertEquals(
                ReservationStatus.PENDING,
                result.getContent()
                        .get(0)
                        .getStatus()
        );
    }


    // ==========================================
    // TEST 2:
    // FILTER BY MINIMUM PRICE
    // ==========================================

    @Test
    void testFilterByMinimumPrice() {

        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.of(user));

        Page<Reservation> page =
                new PageImpl<>(
                        List.of(confirmedReservation)
                );

        when(reservationRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);


        Pageable pageable =
                PageRequest.of(0, 10);


        Page<ReservationResponse> result =
                reservationService.getUserReservations(
                        "user",
                        null,
                        new BigDecimal("2000.00"),
                        null,
                        pageable
                );


        assertNotNull(result);

        assertEquals(
                1,
                result.getTotalElements()
        );

        assertTrue(
                result.getContent()
                        .get(0)
                        .getTotalPrice()
                        .compareTo(
                                new BigDecimal("2000.00")
                        ) >= 0
        );
    }


    // ==========================================
    // TEST 3:
    // FILTER BY MAXIMUM PRICE
    // ==========================================

    @Test
    void testFilterByMaximumPrice() {

        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.of(user));

        Page<Reservation> page =
                new PageImpl<>(
                        List.of(pendingReservation)
                );

        when(reservationRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);


        Pageable pageable =
                PageRequest.of(0, 10);


        Page<ReservationResponse> result =
                reservationService.getUserReservations(
                        "user",
                        null,
                        null,
                        new BigDecimal("1500.00"),
                        pageable
                );


        assertNotNull(result);

        assertEquals(
                1,
                result.getTotalElements()
        );

        assertTrue(
                result.getContent()
                        .get(0)
                        .getTotalPrice()
                        .compareTo(
                                new BigDecimal("1500.00")
                        ) <= 0
        );
    }
}