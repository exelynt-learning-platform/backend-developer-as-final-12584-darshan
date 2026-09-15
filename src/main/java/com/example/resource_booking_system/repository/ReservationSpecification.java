package com.example.resource_booking_system.repository;

import com.example.resource_booking_system.entity.Reservation;
import com.example.resource_booking_system.enums.ReservationStatus;

import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ReservationSpecification {

    public static Specification<Reservation> hasStatus(
            ReservationStatus status) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("status"),
                        status
                );
    }

    public static Specification<Reservation> hasMinPrice(
            BigDecimal minPrice) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.get("totalPrice"),
                        minPrice
                );
    }

    public static Specification<Reservation> hasMaxPrice(
            BigDecimal maxPrice) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.get("totalPrice"),
                        maxPrice
                );
    }
}