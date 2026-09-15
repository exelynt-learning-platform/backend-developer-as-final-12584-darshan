package com.example.resource_booking_system.controller;

import com.example.resource_booking_system.dto.reservation.ReservationRequest;
import com.example.resource_booking_system.dto.reservation.ReservationResponse;
import com.example.resource_booking_system.dto.reservation.ReservationUpdateRequest;
import com.example.resource_booking_system.enums.ReservationStatus;
import com.example.resource_booking_system.service.ReservationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(
        name = "Reservations",
        description = "Reservation management APIs"
)
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
    @Operation(
            summary = "Create reservation",
            description = "Creates a reservation for the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Reservation created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid reservation data"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
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
    @Operation(
            summary = "Get my reservations",
            description = "Returns paginated reservations belonging to the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservations retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
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
    @Operation(
            summary = "Get all reservations",
            description = "Returns all reservations with optional filtering and pagination. ADMIN only."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservations retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied. ADMIN role required."
            )
    })
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
    @Operation(
            summary = "Get reservation by ID",
            description = "Returns a reservation accessible to the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservation retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reservation not found"
            )
    })
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
    @Operation(
            summary = "Update reservation",
            description = "Updates an existing reservation. ADMIN only."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservation updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid reservation data"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied. ADMIN role required."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reservation not found"
            )
    })
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
    @Operation(
            summary = "Cancel reservation",
            description = "Cancels a reservation belonging to the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservation cancelled successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reservation not found"
            )
    })
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
    @Operation(
            summary = "Confirm reservation",
            description = "Confirms a reservation belonging to the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservation confirmed successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reservation not found"
            )
    })
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
    @Operation(
            summary = "Delete reservation",
            description = "Deletes a reservation. ADMIN only."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Reservation deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied. ADMIN role required."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reservation not found"
            )
    })
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id) {

        reservationService.deleteReservation(id);

        return ResponseEntity.noContent().build();
    }
}