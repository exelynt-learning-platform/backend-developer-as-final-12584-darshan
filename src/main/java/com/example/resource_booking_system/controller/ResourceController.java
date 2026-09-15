package com.example.resource_booking_system.controller;

import com.example.resource_booking_system.dto.resource.ResourceRequest;
import com.example.resource_booking_system.dto.resource.ResourceResponse;
import com.example.resource_booking_system.service.ResourceService;

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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/resources")
@Tag(
        name = "Resources",
        description = "Resource management APIs"
)
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }


    // ==========================================
    // USER + ADMIN
    // ==========================================

    @GetMapping
    @Operation(
            summary = "Get all resources",
            description = "Returns a paginated list of resources"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Resources retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    public ResponseEntity<Page<ResourceResponse>> getAllResources(
            @ParameterObject Pageable pageable) {

        return ResponseEntity.ok(
                resourceService.getAllResources(pageable)
        );
    }


    // ==========================================
    // USER + ADMIN
    // ==========================================

    @GetMapping("/{id}")
    @Operation(
            summary = "Get resource by ID",
            description = "Returns a single resource using its ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Resource retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    public ResponseEntity<ResourceResponse> getResourceById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                resourceService.getResourceById(id)
        );
    }


    // ==========================================
    // ADMIN ONLY
    // ==========================================

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "Create resource",
            description = "Creates a new resource. ADMIN only."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Resource created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid resource data"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied. ADMIN role required."
            )
    })
    public ResponseEntity<ResourceResponse> createResource(
            @Valid @RequestBody ResourceRequest request) {

        ResourceResponse response =
                resourceService.createResource(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // ==========================================
    // ADMIN ONLY
    // ==========================================

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "Update resource",
            description = "Updates an existing resource. ADMIN only."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Resource updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid resource data"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied. ADMIN role required."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found"
            )
    })
    public ResponseEntity<ResourceResponse> updateResource(
            @PathVariable Long id,
            @Valid @RequestBody ResourceRequest request) {

        ResourceResponse response =
                resourceService.updateResource(id, request);

        return ResponseEntity.ok(response);
    }


    // ==========================================
    // ADMIN ONLY
    // ==========================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "Delete resource",
            description = "Deletes a resource. ADMIN only."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Resource deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied. ADMIN role required."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found"
            )
    })
    public ResponseEntity<Void> deleteResource(
            @PathVariable Long id) {

        resourceService.deleteResource(id);

        return ResponseEntity.noContent().build();
    }
}