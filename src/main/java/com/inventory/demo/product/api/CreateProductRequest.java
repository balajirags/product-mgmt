package com.inventory.demo.product.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new product.
 */
public record CreateProductRequest(

        @NotBlank(message = "title is required")
        @JsonProperty("title")
        String title,

        @JsonProperty("handle")
        String handle,

        @JsonProperty("status")
        String status,

        @JsonProperty("description")
        String description,

        @JsonProperty("subtitle")
        String subtitle,

        @JsonProperty("weight")
        BigDecimal weight,

        @JsonProperty("height")
        BigDecimal height,

        @JsonProperty("width")
        BigDecimal width,

        @JsonProperty("length")
        BigDecimal length,

        @JsonProperty("metadata")
        String metadata,

        @JsonProperty("external_id")
        String externalId
) {
}
