package com.example.resource_booking_system;

import com.example.resource_booking_system.dto.resource.ResourceRequest;
import com.example.resource_booking_system.dto.resource.ResourceResponse;
import com.example.resource_booking_system.service.ResourceService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ResourceSecurityTest {

    @Autowired
    private ResourceService resourceService;


    // ==========================================
    // TEST 1: ADMIN CAN CREATE RESOURCE
    // ==========================================

    @Test
    void testAdminCanCreateResource() {

        ResourceRequest request =
                new ResourceRequest(
                        "Test Meeting Room",
                        "ROOM",
                        "Testing resource",
                        new java.math.BigDecimal("500.00"),
                        true
                );

        ResourceResponse response =
                resourceService.createResource(request);

        assertNotNull(response);

        assertNotNull(response.getId());

        assertEquals(
                "Test Meeting Room",
                response.getName()
        );

        assertEquals(
                "ROOM",
                response.getType()
        );
    }


    // ==========================================
    // TEST 2: USER CAN READ RESOURCE
    // ==========================================

    @Test
    void testUserCanReadResources() {

        var resources = resourceService.getAllResources(
                org.springframework.data.domain.PageRequest.of(0, 10)
        );

        assertNotNull(resources);
    }


    // ==========================================
    // TEST 3: RESOURCE SERVICE EXISTS
    // ==========================================

    @Test
    void testResourceServiceIsAvailable() {

        assertNotNull(resourceService);
    }
}