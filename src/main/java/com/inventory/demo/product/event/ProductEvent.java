package com.inventory.demo.product.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Base record for product lifecycle domain events.
 * All product events share a common structure with event type, product ID, and timestamp.
 */
public sealed interface ProductEvent permits ProductCreatedEvent, ProductUpdatedEvent, ProductDeletedEvent {

    /**
     * Returns the event type identifier (e.g., "product.created").
     */
    String eventType();

    /**
     * Returns the product ID associated with this event.
     */
    UUID productId();

    /**
     * Returns the timestamp when the event occurred.
     */
    Instant timestamp();

    /**
     * Returns the event payload as a map.
     */
    Map<String, Object> payload();
}
