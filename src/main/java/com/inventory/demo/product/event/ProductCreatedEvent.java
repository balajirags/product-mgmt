package com.inventory.demo.product.event;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Domain event published when a new product is created.
 */
@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Record DTO — payload is a snapshot of the event data")
public record ProductCreatedEvent(
        String eventType,
        UUID productId,
        Instant timestamp,
        Map<String, Object> payload
) implements ProductEvent {

    private static final String EVENT_TYPE = "product.created";

    /**
     * Factory method to create a ProductCreatedEvent.
     *
     * @param productId the ID of the created product
     * @param title     the product title
     * @param handle    the product handle
     * @param status    the product status
     * @return a new ProductCreatedEvent
     */
    public static ProductCreatedEvent create(UUID productId, String title, String handle, String status) {
        Map<String, Object> eventPayload = Map.of(
                "title", title,
                "handle", handle,
                "status", status
        );
        return new ProductCreatedEvent(EVENT_TYPE, productId, Instant.now(), eventPayload);
    }
}
