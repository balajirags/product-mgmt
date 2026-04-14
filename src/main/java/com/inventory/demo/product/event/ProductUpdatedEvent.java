package com.inventory.demo.product.event;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Domain event published when an existing product is updated.
 */
@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Record DTO — payload is a snapshot of the event data")
public record ProductUpdatedEvent(
        String eventType,
        UUID productId,
        Instant timestamp,
        Map<String, Object> payload
) implements ProductEvent {

    private static final String EVENT_TYPE = "product.updated";

    /**
     * Factory method to create a ProductUpdatedEvent.
     *
     * @param productId the ID of the updated product
     * @param title     the product title
     * @param handle    the product handle
     * @param status    the product status
     * @return a new ProductUpdatedEvent
     */
    public static ProductUpdatedEvent create(UUID productId, String title, String handle, String status) {
        Map<String, Object> eventPayload = Map.of(
                "title", title,
                "handle", handle,
                "status", status
        );
        return new ProductUpdatedEvent(EVENT_TYPE, productId, Instant.now(), eventPayload);
    }
}
