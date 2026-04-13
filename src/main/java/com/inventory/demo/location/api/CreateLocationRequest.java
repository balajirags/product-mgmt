package com.inventory.demo.location.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for registering a new location.
 */
public record CreateLocationRequest(

        @NotBlank(message = "location_code is required")
        @JsonProperty("location_code")
        String locationCode,

        @NotBlank(message = "location_name is required")
        @JsonProperty("location_name")
        String locationName,

        @NotBlank(message = "location_type is required")
        @JsonProperty("location_type")
        String locationType,

        @NotNull(message = "address is required")
        @Valid
        @JsonProperty("address")
        AddressRequest address,

        @NotBlank(message = "contact_person is required")
        @JsonProperty("contact_person")
        String contactPerson
) {

    /**
     * Nested address DTO with validation.
     */
    public record AddressRequest(
            @NotBlank(message = "street is required")
            @JsonProperty("street")
            String street,

            @NotBlank(message = "city is required")
            @JsonProperty("city")
            String city,

            @NotBlank(message = "state is required")
            @JsonProperty("state")
            String state,

            @NotBlank(message = "postal_code is required")
            @JsonProperty("postal_code")
            String postalCode,

            @NotBlank(message = "country is required")
            @JsonProperty("country")
            String country
    ) {}
}
