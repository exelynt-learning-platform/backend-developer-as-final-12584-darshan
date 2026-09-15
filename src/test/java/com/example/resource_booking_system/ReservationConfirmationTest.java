package com.example.resource_booking_system.service;

import com.example.resource_booking_system.entity.Reservation;
import com.example.resource_booking_system.entity.Resource;
import com.example.resource_booking_system.entity.User;
import com.example.resource_booking_system.enums.ReservationStatus;
import com.example.resource_booking_system.exception.BadRequestException;
import com.example.resource_booking_system.exception.ForbiddenException;
import com.example.resource_booking_system.repository.ReservationRepository;
import com.example.resource_booking_system.repository.ResourceRepository;
import com.example.resource_booking_system.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationConfirmationTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationService reservationService;

    private User user;
    private User anotherUser;
    private Resource resource;
    private Reservation reservation;

    @BeforeEach
    void setUp() {

        user = new User(
                3L,
                "darshan",
                "darshan@example.com",
                "password",
                com.example.resource_booking_system.enums.Role.USER
        );

        anotherUser = new User(
                2L,
                "user",
                "user@example.com",
                "password",
                com.example.resource_booking_system.enums.Role.USER
        );

        resource = new Resource(
                6L,
                "Final Test Room",
                "ROOM",
                "Final regression test resource",
                new BigDecimal("800.00"),
                true
        );

        reservation = new Reservation(
                4L,
                user,
                resource,
                java.time.LocalDateTime.of(2026, 9, 17, 10, 0),
                java.time.LocalDateTime.of(2026, 9, 17, 12, 0),
                ReservationStatus.PENDING,
                new BigDecimal("1600.00")
        );
    }

    @Test
    void ownerShouldBeAbleToConfirmOwnReservation() {

        when(userRepository.findByUsername("darshan"))
                .thenReturn(Optional.of(user));

        when(reservationRepository.findById(4L))
                .thenReturn(Optional.of(reservation));

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response =
                reservationService.confirmReservation(
                        4L,
                        "darshan"
                );

        assertEquals(
                ReservationStatus.CONFIRMED,
                response.getStatus()
        );

        verify(reservationRepository).save(reservation);
    }

    @Test
    void anotherUserShouldNotBeAbleToConfirmReservation() {

        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.of(anotherUser));

        when(reservationRepository.findById(4L))
                .thenReturn(Optional.of(reservation));

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> reservationService.confirmReservation(
                        4L,
                        "user"
                )
        );

        assertEquals(
                "You are not allowed to confirm this reservation",
                exception.getMessage()
        );

        verify(reservationRepository, never())
                .save(any(Reservation.class));
    }

    @Test
    void cancelledReservationShouldNotBeConfirmed() {

        reservation.setStatus(
                ReservationStatus.CANCELLED
        );

        when(userRepository.findByUsername("darshan"))
                .thenReturn(Optional.of(user));

        when(reservationRepository.findById(4L))
                .thenReturn(Optional.of(reservation));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> reservationService.confirmReservation(
                        4L,
                        "darshan"
                )
        );

        assertEquals(
                "Cancelled reservation cannot be confirmed",
                exception.getMessage()
        );

        verify(reservationRepository, never())
                .save(any(Reservation.class));
    }

    @Test
    void alreadyConfirmedReservationShouldNotBeConfirmedAgain() {

        reservation.setStatus(
                ReservationStatus.CONFIRMED
        );

        when(userRepository.findByUsername("darshan"))
                .thenReturn(Optional.of(user));

        when(reservationRepository.findById(4L))
                .thenReturn(Optional.of(reservation));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> reservationService.confirmReservation(
                        4L,
                        "darshan"
                )
        );

        assertEquals(
                "Reservation is already confirmed",
                exception.getMessage()
        );

        verify(reservationRepository, never())
                .save(any(Reservation.class));
    }
}