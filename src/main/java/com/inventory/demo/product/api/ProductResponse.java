package com.inventory.demo.product.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.inventory.demo.product.domain.Product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for a product.
 */
public record ProductResponse(
        @JsonProperty("id")
        UUID id,

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

        @JsonProperty("is_giftcard")
        boolean giftcard,

        @JsonProperty("discountable")
        boolean discountable,

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
        String externalId,

        @JsonProperty("created_at")
        Instant createdAt,

        @JsonProperty("updated_at")
        Instant updatedAt
) {

    /**
     * Maps a Product domain entity to a ProductResponse DTO.
     *
     * @param product the domain entity
     * @return the response DTO
     */
    public static ProductResponse fromEntity(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getTitle(),
                product.getHandle(),
                product.getStatus().name(),
                product.getDescription(),
                product.getSubtitle(),
                product.isGiftcard(),
                product.isDiscountable(),
                product.getWeight(),
                product.getHeight(),
                product.getWidth(),
                product.getLength(),
                product.getMetadata(),
                product.getExternalId(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
