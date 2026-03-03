package com.inventory.demo.exception;

/**
 * Thrown when a requested resource cannot be found.
 * Maps to HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String resourceType, Object identifier) {
        super("RESOURCE_NOT_FOUND",
                String.format("%s not found with identifier: %s", resourceType, identifier));
    }
}
