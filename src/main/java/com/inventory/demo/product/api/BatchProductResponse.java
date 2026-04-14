package com.inventory.demo.product.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;

/**
 * Response DTO for batch product operations.
 * Contains results grouped by operation type: created, updated, deleted.
 */
@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "Record DTO — List.copyOf in compact constructor ensures immutability")
@SuppressWarnings("PMD.UnusedAssignment") // PMD false positive with record compact constructor
public record BatchProductResponse(

        @JsonProperty("created")
        List<BatchItemResult> created,

        @JsonProperty("updated")
        List<BatchItemResult> updated,

        @JsonProperty("deleted")
        List<BatchItemResult> deleted
) {

    /**
     * Compact constructor that creates defensive copies of mutable list fields.
     */
    public BatchProductResponse {
        created = created != null ? List.copyOf(created) : List.of();
        updated = updated != null ? List.copyOf(updated) : List.of();
        deleted = deleted != null ? List.copyOf(deleted) : List.of();
    }
}
