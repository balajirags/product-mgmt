package com.inventory.demo.product.event;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductEventTest {

    @Nested
    class ProductCreatedEventTests {

        @Test
        void shouldCreateEventWithCorrectFields() {
            UUID productId = UUID.randomUUID();

            ProductCreatedEvent event = ProductCreatedEvent.create(productId, "Widget", "widget", "DRAFT");

            assertThat(event.eventType()).isEqualTo("product.created");
            assertThat(event.productId()).isEqualTo(productId);
            assertThat(event.timestamp()).isNotNull();
            assertThat(event.payload())
                    .containsEntry("title", "Widget")
                    .containsEntry("handle", "widget")
                    .containsEntry("status", "DRAFT");
        }

        @Test
        void shouldImplementProductEventInterface() {
            ProductCreatedEvent event = ProductCreatedEvent.create(UUID.randomUUID(), "T", "h", "DRAFT");

            assertThat(event).isInstanceOf(ProductEvent.class);
        }
    }

    @Nested
    class ProductUpdatedEventTests {

        @Test
        void shouldCreateEventWithCorrectFields() {
            UUID productId = UUID.randomUUID();

            ProductUpdatedEvent event = ProductUpdatedEvent.create(productId, "Updated", "updated", "PUBLISHED");

            assertThat(event.eventType()).isEqualTo("product.updated");
            assertThat(event.productId()).isEqualTo(productId);
            assertThat(event.timestamp()).isNotNull();
            assertThat(event.payload())
                    .containsEntry("title", "Updated")
                    .containsEntry("handle", "updated")
                    .containsEntry("status", "PUBLISHED");
        }
    }

    @Nested
    class ProductDeletedEventTests {

        @Test
        void shouldCreateEventWithCorrectFields() {
            UUID productId = UUID.randomUUID();

            ProductDeletedEvent event = ProductDeletedEvent.create(productId);

            assertThat(event.eventType()).isEqualTo("product.deleted");
            assertThat(event.productId()).isEqualTo(productId);
            assertThat(event.timestamp()).isNotNull();
            assertThat(event.payload()).isEqualTo(Map.of());
        }
    }
}
