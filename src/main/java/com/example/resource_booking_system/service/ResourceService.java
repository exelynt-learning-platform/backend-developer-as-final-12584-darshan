package com.example.resource_booking_system.service;

import com.example.resource_booking_system.dto.resource.ResourceRequest;
import com.example.resource_booking_system.dto.resource.ResourceResponse;
import com.example.resource_booking_system.entity.Resource;
import com.example.resource_booking_system.exception.ResourceNotFoundException;
import com.example.resource_booking_system.repository.ResourceRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    // Get all resources
    public Page<ResourceResponse> getAllResources(Pageable pageable) {

        return resourceRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    // Get resource by ID
    public ResourceResponse getResourceById(Long id) {

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found with id: " + id
                        )
                );

        return mapToResponse(resource);
    }

    // Create resource
    public ResourceResponse createResource(ResourceRequest request) {

        Resource resource = new Resource();

        resource.setName(request.getName());
        resource.setType(request.getType());
        resource.setDescription(request.getDescription());
        resource.setPricePerUnit(request.getPricePerUnit());
        resource.setAvailable(request.getAvailable());

        Resource savedResource = resourceRepository.save(resource);

        return mapToResponse(savedResource);
    }

    // Update resource
    public ResourceResponse updateResource(
            Long id,
            ResourceRequest request) {

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found with id: " + id
                        )
                );

        resource.setName(request.getName());
        resource.setType(request.getType());
        resource.setDescription(request.getDescription());
        resource.setPricePerUnit(request.getPricePerUnit());
        resource.setAvailable(request.getAvailable());

        Resource updatedResource = resourceRepository.save(resource);

        return mapToResponse(updatedResource);
    }

    // Delete resource
    public void deleteResource(Long id) {

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found with id: " + id
                        )
                );

        resourceRepository.delete(resource);
    }

    // Entity -> DTO
    private ResourceResponse mapToResponse(Resource resource) {

        return new ResourceResponse(
                resource.getId(),
                resource.getName(),
                resource.getType(),
                resource.getDescription(),
                resource.getPricePerUnit(),
                resource.isAvailable()
        );
    }
}