package com.inventory.demo.product.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.inventory.demo.product.domain.Product;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for a product.
 */
@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2", "NP_LOAD_OF_KNOWN_NULL_VALUE"}, justification = "Record DTO — List.copyOf in compact constructor ensures immutability; null-guard is intentional")
@SuppressWarnings("PMD.UnusedAssignment") // PMD false positive with record compact constructor
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
        Instant updatedAt,

        @JsonProperty("options")
        List<ProductOptionResponse> options,

        @JsonProperty("variants")
        List<ProductVariantResponse> variants
) {

    /**
     * Compact constructor that creates defensive copies of mutable list fields.
     */
    public ProductResponse {
        options = options != null ? List.copyOf(options) : List.of();
        variants = variants != null ? List.copyOf(variants) : List.of();
    }

    /**
     * Maps a Product domain entity to a ProductResponse DTO.
     *
     * @param product the domain entity
     * @return the response DTO
     */
    public static ProductResponse fromEntity(Product product) {
        List<ProductOptionResponse> optionResponses = product.getOptions().stream()
                .filter(option -> option.getDeletedAt() == null)
                .map(ProductOptionResponse::fromEntity)
                .toList();

        List<ProductVariantResponse> variantResponses = product.getVariants().stream()
                .filter(variant -> variant.getDeletedAt() == null)
                .map(ProductVariantResponse::fromEntity)
                .toList();

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
                product.getUpdatedAt(),
                optionResponses,
                variantResponses
        );
    }
}
