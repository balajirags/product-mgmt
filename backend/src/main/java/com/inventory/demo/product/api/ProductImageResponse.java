package com.inventory.demo.product.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.inventory.demo.product.domain.ProductImage;

/**
 * Response DTO for a product gallery image.
 */
public record ProductImageResponse(

        @JsonProperty("url")
        String url,

        @JsonProperty("rank")
        int rank
) {

    /**
     * Maps a ProductImage domain entity to a ProductImageResponse DTO.
     *
     * @param image the domain entity
     * @return the response DTO
     */
    public static ProductImageResponse fromEntity(ProductImage image) {
        return new ProductImageResponse(image.getUrl(), image.getRank());
    }
}
