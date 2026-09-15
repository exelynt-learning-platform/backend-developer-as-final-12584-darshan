package com.example.resource_booking_system;

import com.example.resource_booking_system.dto.reservation.ReservationRequest;
import com.example.resource_booking_system.entity.Resource;
import com.example.resource_booking_system.entity.User;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

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

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        user = new User();

        user.setId(1L);
        user.setUsername("user");
        user.setEmail("user@example.com");
        user.setPassword("password");
        user.setRole(Role.USER);

        resource = new Resource();

        resource.setId(1L);
        resource.setName("Meeting Room");
        resource.setType("ROOM");
        resource.setDescription("Test room");
        resource.setPricePerUnit(
                new BigDecimal("600.00")
        );
        resource.setAvailable(true);
    }


    // ==========================================
    // TEST 1: CREATE RESERVATION
    // ==========================================

    @Test
    void testCreateReservation() {

        LocalDateTime start =
                LocalDateTime.now().plusDays(1);

        LocalDateTime end =
                start.plusHours(2);

        ReservationRequest request =
                new ReservationRequest(
                        1L,
                        start,
                        end
                );

        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.of(user));

        when(resourceRepository.findById(1L))
                .thenReturn(Optional.of(resource));

        when(reservationRepository.save(any()))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        var response =
                reservationService.createReservation(
                        request,
                        "user"
                );

        assertNotNull(response);

        assertEquals(
                1L,
                response.getUserId()
        );

        assertEquals(
                1L,
                response.getResourceId()
        );

        assertEquals(
                0,
                new BigDecimal("1200.00")
                        .compareTo(response.getTotalPrice())
        );

        assertEquals(
                "PENDING",
                response.getStatus().name()
        );
    }


    // ==========================================
    // TEST 2: UNAVAILABLE RESOURCE
    // ==========================================

    @Test
    void testCreateReservationWhenResourceUnavailable() {

        resource.setAvailable(false);

        LocalDateTime start =
                LocalDateTime.now().plusDays(1);

        LocalDateTime end =
                start.plusHours(2);

        ReservationRequest request =
                new ReservationRequest(
                        1L,
                        start,
                        end
                );

        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.of(user));

        when(resourceRepository.findById(1L))
                .thenReturn(Optional.of(resource));

        assertThrows(
                RuntimeException.class,
                () -> reservationService.createReservation(
                        request,
                        "user"
                )
        );

        verify(
                reservationRepository,
                never()
        ).save(any());
    }


    // ==========================================
    // TEST 3: INVALID TIME
    // ==========================================

    @Test
    void testCreateReservationWithInvalidTime() {

        LocalDateTime start =
                LocalDateTime.now().plusDays(1);

        LocalDateTime end =
                start.minusHours(1);

        ReservationRequest request =
                new ReservationRequest(
                        1L,
                        start,
                        end
                );

        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.of(user));

        when(resourceRepository.findById(1L))
                .thenReturn(Optional.of(resource));

        assertThrows(
                RuntimeException.class,
                () -> reservationService.createReservation(
                        request,
                        "user"
                )
        );

        verify(
                reservationRepository,
                never()
        ).save(any());
    }
}