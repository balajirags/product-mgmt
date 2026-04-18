package com.inventory.demo.product.event;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Domain event published when a product is soft-deleted.
 */
@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "Record DTO — payload is an immutable snapshot")
public record ProductDeletedEvent(
        String eventType,
        UUID productId,
        Instant timestamp,
        Map<String, Object> payload
) implements ProductEvent {

    private static final String EVENT_TYPE = "product.deleted";

    /**
     * Factory method to create a ProductDeletedEvent.
     *
     * @param productId the ID of the deleted product
     * @return a new ProductDeletedEvent
     */
    public static ProductDeletedEvent create(UUID productId) {
        return new ProductDeletedEvent(EVENT_TYPE, productId, Instant.now(), Map.of());
    }
}
