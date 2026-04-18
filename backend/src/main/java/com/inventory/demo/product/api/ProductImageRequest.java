package com.inventory.demo.product.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for a product gallery image.
 */
public record ProductImageRequest(

        @NotBlank(message = "image url is required")
        @JsonProperty("url")
        String url
) {
}
