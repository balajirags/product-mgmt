package com.inventory.demo.product.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for creating or updating a product option inline with a product.
 */
@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2", "NP_LOAD_OF_KNOWN_NULL_VALUE"}, justification = "Record DTO — List.copyOf in compact constructor ensures immutability; null-guard is intentional")
@SuppressWarnings("PMD.UnusedAssignment") // PMD false positive with record compact constructor
public record ProductOptionRequest(

        @NotBlank(message = "option title is required")
        @JsonProperty("title")
        String title,

        @NotEmpty(message = "at least one option value is required")
        @JsonProperty("values")
        List<String> values
) {

    /**
     * Compact constructor that creates a defensive copy of the values list.
     */
    public ProductOptionRequest {
        values = values != null ? List.copyOf(values) : values;
    }
}
