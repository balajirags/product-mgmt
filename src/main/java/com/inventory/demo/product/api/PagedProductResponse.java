package com.inventory.demo.product.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;

/**
 * Paginated response wrapper for product listings.
 */
public record PagedProductResponse(
        @JsonProperty("content")
        List<ProductResponse> content,

        @JsonProperty("page")
        int page,

        @JsonProperty("size")
        int size,

        @JsonProperty("total_elements")
        long totalElements,

        @JsonProperty("total_pages")
        int totalPages
) {

    /**
     * Creates a PagedProductResponse from a Spring Data Page of ProductResponse.
     *
     * @param productPage the page of product responses
     * @return the paginated response DTO
     */
    /**
     * Compact constructor that wraps the content list in an unmodifiable copy.
     */
    public PagedProductResponse {
        content = Collections.unmodifiableList(content);
    }

    /**
     * Creates a PagedProductResponse from a Spring Data Page of ProductResponse.
     *
     * @param productPage the page of product responses
     * @return the paginated response DTO
     */
    public static PagedProductResponse fromPage(Page<ProductResponse> productPage) {
        return new PagedProductResponse(
                productPage.getContent(),
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages()
        );
    }
}
