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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservationPaginationTest {

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
    private Reservation reservation1;
    private Reservation reservation2;


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
        // RESERVATION 1
        // ==========================================

        reservation1 = new Reservation();

        reservation1.setId(100L);
        reservation1.setUser(user);
        reservation1.setResource(resource);
        reservation1.setStatus(
                ReservationStatus.PENDING
        );
        reservation1.setTotalPrice(
                new BigDecimal("1200.00")
        );


        // ==========================================
        // RESERVATION 2
        // ==========================================

        reservation2 = new Reservation();

        reservation2.setId(101L);
        reservation2.setUser(user);
        reservation2.setResource(resource);
        reservation2.setStatus(
                ReservationStatus.CONFIRMED
        );
        reservation2.setTotalPrice(
                new BigDecimal("2400.00")
        );
    }


    // ==========================================
    // TEST 1:
    // PAGE SIZE
    // ==========================================

    @Test
    void testPaginationPageSize() {

        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.of(user));

        Page<Reservation> page =
                new PageImpl<>(
                        List.of(reservation1),
                        PageRequest.of(0, 1),
                        2
                );

        when(reservationRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);


        Pageable pageable =
                PageRequest.of(0, 1);


        Page<ReservationResponse> result =
                reservationService.getUserReservations(
                        "user",
                        null,
                        null,
                        null,
                        pageable
                );


        assertNotNull(result);

        // One item returned on this page
        assertEquals(
                1,
                result.getContent().size()
        );

        // Total records are 2
        assertEquals(
                2,
                result.getTotalElements()
        );

        // Page size is 1
        assertEquals(
                1,
                result.getSize()
        );
    }


    // ==========================================
    // TEST 2:
    // PAGE NUMBER
    // ==========================================

    @Test
    void testPaginationPageNumber() {

        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.of(user));

        Page<Reservation> page =
                new PageImpl<>(
                        List.of(reservation2),
                        PageRequest.of(1, 1),
                        2
                );

        when(reservationRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);


        Pageable pageable =
                PageRequest.of(1, 1);


        Page<ReservationResponse> result =
                reservationService.getUserReservations(
                        "user",
                        null,
                        null,
                        null,
                        pageable
                );


        assertNotNull(result);

        // We requested page number 1
        assertEquals(
                1,
                result.getNumber()
        );

        // Page size = 1
        assertEquals(
                1,
                result.getSize()
        );

        // Total records = 2
        assertEquals(
                2,
                result.getTotalElements()
        );
    }


    // ==========================================
    // TEST 3:
    // SORTING
    // ==========================================

    @Test
    void testSorting() {

        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.of(user));

        Page<Reservation> page =
                new PageImpl<>(
                        List.of(
                                reservation2,
                                reservation1
                        ),
                        PageRequest.of(
                                0,
                                10,
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "totalPrice"
                                )
                        ),
                        2
                );

        when(reservationRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);


        Pageable pageable =
                PageRequest.of(
                        0,
                        10,
                        Sort.by(
                                Sort.Direction.DESC,
                                "totalPrice"
                        )
                );


        Page<ReservationResponse> result =
                reservationService.getUserReservations(
                        "user",
                        null,
                        null,
                        null,
                        pageable
                );


        assertNotNull(result);

        assertEquals(
                2,
                result.getTotalElements()
        );

        // First result should be the higher-priced reservation
        assertEquals(
                101L,
                result.getContent()
                        .get(0)
                        .getId()
        );

        assertEquals(
                new BigDecimal("2400.00"),
                result.getContent()
                        .get(0)
                        .getTotalPrice()
        );
    }
}