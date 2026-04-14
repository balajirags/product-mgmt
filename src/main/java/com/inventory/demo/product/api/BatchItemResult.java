package com.inventory.demo.product.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.UUID;

/**
 * Represents the result of a single item in a batch operation.
 * Includes either the product data on success or an error on failure.
 */
@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Record DTO — immutable fields")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BatchItemResult(

        @JsonProperty("id")
        UUID id,

        @JsonProperty("success")
        boolean success,

        @JsonProperty("product")
        ProductResponse product,

        @JsonProperty("error_code")
        String errorCode,

        @JsonProperty("error_message")
        String errorMessage
) {

    /**
     * Creates a successful result with the product data.
     *
     * @param product the created or updated product
     * @return a success result
     */
    public static BatchItemResult success(ProductResponse product) {
        return new BatchItemResult(product.id(), true, product, null, null);
    }

    /**
     * Creates a successful result for a delete operation.
     *
     * @param id the deleted product ID
     * @return a success result with only the ID
     */
    public static BatchItemResult deleted(UUID id) {
        return new BatchItemResult(id, true, null, null, null);
    }

    /**
     * Creates a failure result with error details.
     *
     * @param id           the product ID (may be null for create failures)
     * @param errorCode    the machine-readable error code
     * @param errorMessage the human-readable error message
     * @return a failure result
     */
    public static BatchItemResult failure(UUID id, String errorCode, String errorMessage) {
        return new BatchItemResult(id, false, null, errorCode, errorMessage);
    }
}
