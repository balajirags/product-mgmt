package com.inventory.demo.product.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Request DTO for updating an existing product.
 * All fields are optional — only provided (non-null) fields are applied (partial update).
 */
public record UpdateProductRequest(

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
