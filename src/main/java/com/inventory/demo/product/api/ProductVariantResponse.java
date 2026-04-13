package com.inventory.demo.product.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.inventory.demo.product.domain.ProductOptionValue;
import com.inventory.demo.product.domain.ProductVariant;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for a product variant.
 */
@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2", "NP_LOAD_OF_KNOWN_NULL_VALUE"}, justification = "Record DTO — Map.copyOf in compact constructor ensures immutability; null-guard is intentional")
@SuppressWarnings("PMD.UnusedAssignment")
public record ProductVariantResponse(
        @JsonProperty("id")
        UUID id,

        @JsonProperty("title")
        String title,

        @JsonProperty("sku")
        String sku,

        @JsonProperty("barcode")
        String barcode,

        @JsonProperty("weight")
        BigDecimal weight,

        @JsonProperty("height")
        BigDecimal height,

        @JsonProperty("width")
        BigDecimal width,

        @JsonProperty("length")
        BigDecimal length,

        @JsonProperty("manage_inventory")
        boolean manageInventory,

        @JsonProperty("allow_backorder")
        boolean allowBackorder,

        @JsonProperty("option_values")
        Map<String, String> optionValues,

        @JsonProperty("created_at")
        Instant createdAt,

        @JsonProperty("updated_at")
        Instant updatedAt
) {

    /**
     * Compact constructor that creates a defensive copy of the optionValues map.
     */
    public ProductVariantResponse {
        optionValues = optionValues != null ? Map.copyOf(optionValues) : Map.of();
    }

    /**
     * Maps a ProductVariant domain entity to a ProductVariantResponse DTO.
     *
     * @param variant the domain entity
     * @return the response DTO
     */
    public static ProductVariantResponse fromEntity(ProductVariant variant) {
        Map<String, String> optionValueMap = new LinkedHashMap<>();
        for (ProductOptionValue ov : variant.getOptionValues()) {
            if (ov.getDeletedAt() == null && ov.getOption() != null) {
                optionValueMap.put(ov.getOption().getTitle(), ov.getValue());
            }
        }

        return new ProductVariantResponse(
                variant.getId(),
                variant.getTitle(),
                variant.getSku(),
                variant.getBarcode(),
                variant.getWeight(),
                variant.getHeight(),
                variant.getWidth(),
                variant.getLength(),
                variant.isManageInventory(),
                variant.isAllowBackorder(),
                optionValueMap,
                variant.getCreatedAt(),
                variant.getUpdatedAt()
        );
    }
}
