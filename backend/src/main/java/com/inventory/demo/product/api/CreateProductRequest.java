package com.inventory.demo.product.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Request DTO for creating a new product.
 */
@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2", "NP_LOAD_OF_KNOWN_NULL_VALUE"}, justification = "Record DTO — List.copyOf in compact constructor ensures immutability; null-guard is intentional")
@SuppressWarnings("PMD.UnusedAssignment") // PMD false positive with record compact constructor
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
        Map<String, Object> metadata,

        @JsonProperty("external_id")
        String externalId,

        @JsonProperty("thumbnail")
        String thumbnail,

        @Valid
        @JsonProperty("images")
        List<ProductImageRequest> images,

        @Valid
        @JsonProperty("options")
        List<ProductOptionRequest> options,

        @Valid
        @JsonProperty("variants")
        List<ProductVariantRequest> variants
) {

    /**
     * Compact constructor that creates defensive copies of mutable list fields.
     */
    public CreateProductRequest {
        images = images != null ? List.copyOf(images) : images;
        options = options != null ? List.copyOf(options) : options;
        variants = variants != null ? List.copyOf(variants) : variants;
    }
}
