package com.example.resource_booking_system.controller;

import com.example.resource_booking_system.dto.reservation.ReservationRequest;
import com.example.resource_booking_system.dto.reservation.ReservationResponse;
import com.example.resource_booking_system.dto.reservation.ReservationUpdateRequest;
import com.example.resource_booking_system.enums.ReservationStatus;
import com.example.resource_booking_system.service.ReservationService;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(
            ReservationService reservationService) {

        this.reservationService = reservationService;
    }


    // ==========================================
    // CREATE RESERVATION
    // USER + ADMIN
    // ==========================================

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request,
            Authentication authentication) {

        String username = authentication.getName();

        ReservationResponse response =
                reservationService.createReservation(
                        request,
                        username
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // ==========================================
    // GET MY RESERVATIONS
    // USER
    // ==========================================

    @GetMapping
    public ResponseEntity<Page<ReservationResponse>> getMyReservations(
            Authentication authentication,

            @RequestParam(required = false)
            ReservationStatus status,

            @RequestParam(required = false)
            BigDecimal minPrice,

            @RequestParam(required = false)
            BigDecimal maxPrice,

            @ParameterObject Pageable pageable) {

        String username = authentication.getName();

        Page<ReservationResponse> response =
                reservationService.getUserReservations(
                        username,
                        status,
                        minPrice,
                        maxPrice,
                        pageable
                );

        return ResponseEntity.ok(response);
    }


    // ==========================================
    // GET ALL RESERVATIONS
    // ADMIN ONLY
    // ==========================================

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<ReservationResponse>> getAllReservations(

            @RequestParam(required = false)
            ReservationStatus status,

            @RequestParam(required = false)
            BigDecimal minPrice,

            @RequestParam(required = false)
            BigDecimal maxPrice,

            @ParameterObject Pageable pageable) {

        Page<ReservationResponse> response =
                reservationService.getAllReservations(
                        status,
                        minPrice,
                        maxPrice,
                        pageable
                );

        return ResponseEntity.ok(response);
    }


    // ==========================================
    // GET RESERVATION BY ID
    // USER + ADMIN
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservationById(
            @PathVariable Long id,
            Authentication authentication) {

        String username = authentication.getName();

        ReservationResponse response =
                reservationService.getReservationById(
                        id,
                        username
                );

        return ResponseEntity.ok(response);
    }


    // ==========================================
    // UPDATE RESERVATION
    // ADMIN ONLY
    // ==========================================

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequest request) {

        ReservationResponse response =
                reservationService.updateReservation(
                        id,
                        request
                );

        return ResponseEntity.ok(response);
    }


    // ==========================================
    // CANCEL RESERVATION
    // USER + ADMIN
    // ==========================================

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponse> cancelReservation(
            @PathVariable Long id,
            Authentication authentication) {

        String username = authentication.getName();

        ReservationResponse response =
                reservationService.cancelReservation(
                        id,
                        username
                );

        return ResponseEntity.ok(response);
    }


    // ==========================================
    // CONFIRM RESERVATION
    // USER + ADMIN
    // ==========================================

    @PutMapping("/{id}/confirm")
    public ResponseEntity<ReservationResponse> confirmReservation(
            @PathVariable Long id,
            Authentication authentication) {

        String username = authentication.getName();

        ReservationResponse response =
                reservationService.confirmReservation(
                        id,
                        username
                );

        return ResponseEntity.ok(response);
    }


    // ==========================================
    // DELETE RESERVATION
    // ADMIN ONLY
    // ==========================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id) {

        reservationService.deleteReservation(id);

        return ResponseEntity.noContent().build();
    }
}