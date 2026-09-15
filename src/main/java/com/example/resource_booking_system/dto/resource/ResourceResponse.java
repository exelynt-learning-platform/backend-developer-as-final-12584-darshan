package com.example.resource_booking_system.dto.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResourceResponse {

    private Long id;

    private String name;

    private String type;

    private String description;

    private BigDecimal pricePerUnit;

    private Boolean available;
}