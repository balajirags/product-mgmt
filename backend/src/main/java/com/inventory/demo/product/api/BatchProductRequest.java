package com.inventory.demo.product.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for batch product operations.
 * Supports a mix of create, update, and delete operations in a single call.
 */
@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "Record DTO — List.copyOf in compact constructor ensures immutability")
@SuppressWarnings("PMD.UnusedAssignment") // PMD false positive with record compact constructor
public record BatchProductRequest(

        @Valid
        @JsonProperty("create")
        List<CreateProductRequest> create,

        @Valid
        @JsonProperty("update")
        List<BatchUpdateItem> update,

        @JsonProperty("delete")
        List<UUID> delete
) {

    /**
     * Compact constructor that creates defensive copies of mutable list fields.
     */
    public BatchProductRequest {
        create = create != null ? List.copyOf(create) : List.of();
        update = update != null ? List.copyOf(update) : List.of();
        delete = delete != null ? List.copyOf(delete) : List.of();
    }

    /**
     * Wrapper for an update item that includes the product ID.
     */
    @SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "Record DTO — immutable")
    public record BatchUpdateItem(
            @JsonProperty("id")
            UUID id,

            @Valid
            @JsonProperty("data")
            UpdateProductRequest data
    ) { }
}
