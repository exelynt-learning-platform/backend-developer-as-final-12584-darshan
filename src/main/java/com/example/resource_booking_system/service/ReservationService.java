package com.example.resource_booking_system.service;

import com.example.resource_booking_system.dto.reservation.ReservationRequest;
import com.example.resource_booking_system.dto.reservation.ReservationResponse;
import com.example.resource_booking_system.dto.reservation.ReservationUpdateRequest;
import com.example.resource_booking_system.entity.Reservation;
import com.example.resource_booking_system.entity.Resource;
import com.example.resource_booking_system.entity.User;
import com.example.resource_booking_system.enums.ReservationStatus;
import com.example.resource_booking_system.exception.*;
import com.example.resource_booking_system.repository.ReservationRepository;
import com.example.resource_booking_system.repository.ReservationSpecification;
import com.example.resource_booking_system.repository.ResourceRepository;
import com.example.resource_booking_system.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ResourceRepository resourceRepository,
            UserRepository userRepository) {

        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }


    // ==========================================
    // CREATE RESERVATION
    // ==========================================

    public ReservationResponse createReservation(
            ReservationRequest request,
            String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found: " + username
                        )
                );

        Resource resource = resourceRepository.findById(
                request.getResourceId()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Resource not found with id: "
                                + request.getResourceId()
                )
        );

        if (!resource.isAvailable()) {
            throw new BadRequestException(
                    "Resource is currently unavailable"
            );
        }

        validateReservationTime(
                request.getStartTime(),
                request.getEndTime()
        );

        BigDecimal totalPrice = calculateTotalPrice(
                resource,
                request.getStartTime(),
                request.getEndTime()
        );

        Reservation reservation = new Reservation();

        reservation.setUser(user);
        reservation.setResource(resource);
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setTotalPrice(totalPrice);

        Reservation savedReservation =
                reservationRepository.save(reservation);

        return mapToResponse(savedReservation);
    }


    // ==========================================
    // GET USER RESERVATIONS
    // ==========================================

    public Page<ReservationResponse> getUserReservations(
            String username,
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found: " + username
                        )
                );

        Specification<Reservation> specification =
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(
                                root.get("user").get("id"),
                                user.getId()
                        );

        if (status != null) {
            specification = specification.and(
                    ReservationSpecification.hasStatus(status)
            );
        }

        if (minPrice != null) {
            specification = specification.and(
                    ReservationSpecification.hasMinPrice(minPrice)
            );
        }

        if (maxPrice != null) {
            specification = specification.and(
                    ReservationSpecification.hasMaxPrice(maxPrice)
            );
        }

        return reservationRepository
                .findAll(specification, pageable)
                .map(this::mapToResponse);
    }


    // ==========================================
    // GET ALL RESERVATIONS - ADMIN
    // ==========================================

    public Page<ReservationResponse> getAllReservations(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {

        Specification<Reservation> specification =
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.conjunction();

        if (status != null) {
            specification = specification.and(
                    ReservationSpecification.hasStatus(status)
            );
        }

        if (minPrice != null) {
            specification = specification.and(
                    ReservationSpecification.hasMinPrice(minPrice)
            );
        }

        if (maxPrice != null) {
            specification = specification.and(
                    ReservationSpecification.hasMaxPrice(maxPrice)
            );
        }

        return reservationRepository
                .findAll(specification, pageable)
                .map(this::mapToResponse);
    }


    // ==========================================
    // GET RESERVATION BY ID
    // ==========================================

    public ReservationResponse getReservationById(
            Long reservationId,
            String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found: " + username
                        )
                );

        Reservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow(() ->
                                new ReservationNotFoundException(
                                        "Reservation not found with id: "
                                                + reservationId
                                )
                        );

        // USER can access only their own reservation
        if (user.getRole().name().equals("USER")
                && !reservation.getUser()
                .getId()
                .equals(user.getId())) {

            throw new ForbiddenException(
                    "You are not allowed to access this reservation"
            );
        }

        // ADMIN can access any reservation
        return mapToResponse(reservation);
    }


    // ==========================================
    // UPDATE RESERVATION - ADMIN
    // ==========================================

    public ReservationResponse updateReservation(
            Long reservationId,
            ReservationUpdateRequest request) {

        Reservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow(() ->
                                new ReservationNotFoundException(
                                        "Reservation not found with id: "
                                                + reservationId
                                )
                        );

        Resource resource = resourceRepository.findById(
                request.getResourceId()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Resource not found with id: "
                                + request.getResourceId()
                )
        );

        // A reservation cannot use an unavailable resource
        if (!resource.isAvailable()) {
            throw new BadRequestException(
                    "Resource is currently unavailable"
            );
        }

        // Validate start and end time
        validateReservationTime(
                request.getStartTime(),
                request.getEndTime()
        );

        // Recalculate price using the updated resource/time
        BigDecimal totalPrice = calculateTotalPrice(
                resource,
                request.getStartTime(),
                request.getEndTime()
        );

        reservation.setResource(resource);
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setStatus(request.getStatus());
        reservation.setTotalPrice(totalPrice);

        Reservation updatedReservation =
                reservationRepository.save(reservation);

        return mapToResponse(updatedReservation);
    }

    public ReservationResponse confirmReservation(
            Long reservationId,
            String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found: " + username
                        )
                );

        Reservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow(() ->
                                new ReservationNotFoundException(
                                        "Reservation not found with id: "
                                                + reservationId
                                )
                        );

        if (!reservation.getUser()
                .getId()
                .equals(user.getId())) {

            throw new ForbiddenException(
                    "You are not allowed to confirm this reservation"
            );
        }

        if (reservation.getStatus()
                == ReservationStatus.CANCELLED) {

            throw new BadRequestException(
                    "Cancelled reservation cannot be confirmed"
            );
        }

        if (reservation.getStatus()
                == ReservationStatus.CONFIRMED) {

            throw new BadRequestException(
                    "Reservation is already confirmed"
            );
        }

        reservation.setStatus(
                ReservationStatus.CONFIRMED
        );

        Reservation updatedReservation =
                reservationRepository.save(reservation);

        return mapToResponse(updatedReservation);
    }


    // ==========================================
    // CANCEL RESERVATION
    // ==========================================

    public ReservationResponse cancelReservation(
            Long reservationId,
            String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found: " + username
                        )
                );

        Reservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow(() ->
                                new ReservationNotFoundException(
                                        "Reservation not found with id: "
                                                + reservationId
                                )
                        );

        // USER can cancel only their own reservation
        if (user.getRole().name().equals("USER")
                && !reservation.getUser()
                .getId()
                .equals(user.getId())) {

            throw new ForbiddenException(
                    "You are not allowed to cancel this reservation"
            );
        }

        // Cannot cancel already cancelled reservation
        if (reservation.getStatus()
                == ReservationStatus.CANCELLED) {

            throw new BadRequestException(
                    "Reservation is already cancelled"
            );
        }

        reservation.setStatus(
                ReservationStatus.CANCELLED
        );

        Reservation updatedReservation =
                reservationRepository.save(reservation);

        return mapToResponse(updatedReservation);
    }


    // ==========================================
    // DELETE RESERVATION - ADMIN
    // ==========================================

    public void deleteReservation(Long reservationId) {

        Reservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow(() ->
                                new ReservationNotFoundException(
                                        "Reservation not found with id: "
                                                + reservationId
                                )
                        );

        reservationRepository.delete(reservation);
    }


    // ==========================================
    // VALIDATE RESERVATION TIME
    // ==========================================

    private void validateReservationTime(
            LocalDateTime startTime,
            LocalDateTime endTime) {

        if (startTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException(
                    "Start time cannot be in the past"
            );
        }

        if (!startTime.isBefore(endTime)) {
            throw new BadRequestException(
                    "Start time must be before end time"
            );
        }
    }


    // ==========================================
    // CALCULATE TOTAL PRICE
    // ==========================================

    private BigDecimal calculateTotalPrice(
            Resource resource,
            LocalDateTime startTime,
            LocalDateTime endTime) {

        long minutes = Duration.between(
                startTime,
                endTime
        ).toMinutes();

        BigDecimal hours = BigDecimal.valueOf(minutes)
                .divide(
                        BigDecimal.valueOf(60),
                        2,
                        RoundingMode.HALF_UP
                );

        return resource.getPricePerUnit()
                .multiply(hours);
    }


    // ==========================================
    // ENTITY → RESPONSE DTO
    // ==========================================

    private ReservationResponse mapToResponse(
            Reservation reservation) {

        return new ReservationResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getResource().getId(),
                reservation.getResource().getName(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getStatus(),
                reservation.getTotalPrice()
        );
    }
}