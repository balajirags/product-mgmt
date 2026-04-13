package com.inventory.demo.product.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Request DTO for creating or updating a product variant.
 */
@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2", "NP_LOAD_OF_KNOWN_NULL_VALUE"}, justification = "Record DTO — defensive copies in compact constructor ensure immutability; null-guard is intentional")
@SuppressWarnings("PMD.UnusedAssignment")
public record ProductVariantRequest(

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
        Boolean manageInventory,

        @JsonProperty("allow_backorder")
        Boolean allowBackorder,

        @JsonProperty("option_values")
        Map<String, String> optionValues
) {

    /**
     * Compact constructor that creates defensive copies of mutable fields.
     */
    public ProductVariantRequest {
        optionValues = optionValues != null ? Map.copyOf(optionValues) : optionValues;
    }
}
