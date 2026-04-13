package com.inventory.demo.location.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.inventory.demo.location.domain.Location;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for a location.
 */
public record LocationResponse(
        @JsonProperty("id")
        UUID id,

        @JsonProperty("location_code")
        String locationCode,

        @JsonProperty("location_name")
        String locationName,

        @JsonProperty("location_type")
        String locationType,

        @JsonProperty("lifecycle_state")
        String lifecycleState,

        @JsonProperty("address")
        AddressResponse address,

        @JsonProperty("contact_person")
        String contactPerson,

        @JsonProperty("created_at")
        Instant createdAt,

        @JsonProperty("updated_at")
        Instant updatedAt
) {

    /**
     * Nested address response DTO.
     */
    public record AddressResponse(
            @JsonProperty("street")
            String street,

            @JsonProperty("city")
            String city,

            @JsonProperty("state")
            String state,

            @JsonProperty("postal_code")
            String postalCode,

            @JsonProperty("country")
            String country
    ) {}

    /**
     * Maps a Location domain entity to a LocationResponse DTO.
     *
     * @param location the domain entity
     * @return the response DTO
     */
    public static LocationResponse fromEntity(Location location) {
        return new LocationResponse(
                location.getId(),
                location.getLocationCode(),
                location.getLocationName(),
                location.getLocationType().name(),
                location.getLifecycleState().name(),
                new AddressResponse(
                        location.getStreet(),
                        location.getCity(),
                        location.getState(),
                        location.getPostalCode(),
                        location.getCountry()
                ),
                location.getContactPerson(),
                location.getCreatedAt(),
                location.getUpdatedAt()
        );
    }
}
