package com.inventory.demo.product.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.inventory.demo.product.domain.ProductOption;
import com.inventory.demo.product.domain.ProductOptionValue;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for a product option.
 */
@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2", "NP_LOAD_OF_KNOWN_NULL_VALUE"}, justification = "Record DTO — List.copyOf in compact constructor ensures immutability; null-guard is intentional")
@SuppressWarnings("PMD.UnusedAssignment") // PMD false positive with record compact constructor
public record ProductOptionResponse(

        @JsonProperty("id")
        UUID id,

        @JsonProperty("title")
        String title,

        @JsonProperty("values")
        List<String> values,

        @JsonProperty("created_at")
        Instant createdAt,

        @JsonProperty("updated_at")
        Instant updatedAt
) {

    /**
     * Compact constructor that creates a defensive copy of the values list.
     */
    public ProductOptionResponse {
        values = values != null ? List.copyOf(values) : values;
    }

    /**
     * Maps a ProductOption entity to a response DTO.
     *
     * @param option the domain entity
     * @return the response DTO
     */
    public static ProductOptionResponse fromEntity(ProductOption option) {
        List<String> valueStrings = option.getValues().stream()
                .map(ProductOptionValue::getValue)
                .toList();

        return new ProductOptionResponse(
                option.getId(),
                option.getTitle(),
                valueStrings,
                option.getCreatedAt(),
                option.getUpdatedAt()
        );
    }
}
