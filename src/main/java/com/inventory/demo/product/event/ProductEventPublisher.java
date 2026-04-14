package com.inventory.demo.product.event;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.nio.charset.StandardCharsets;

/**
 * Listens for product domain events and publishes them to Kafka topics.
 * Events are published after the database transaction commits to ensure data consistency.
 * Kafka failures are logged but do not fail the originating API request (fire-and-forget).
 */
@Component
@ConditionalOnBean(KafkaTemplate.class)
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring-managed KafkaTemplate is injected by the container")
public class ProductEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ProductEventPublisher.class);

    private static final String CORRELATION_ID_HEADER = "x-correlation-id";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ProductEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publishes a product.created event to Kafka after the transaction commits.
     *
     * @param event the product created event
     */
    @TransactionalEventListener
    public void handleProductCreated(ProductCreatedEvent event) {
        publishEvent(event);
    }

    /**
     * Publishes a product.updated event to Kafka after the transaction commits.
     *
     * @param event the product updated event
     */
    @TransactionalEventListener
    public void handleProductUpdated(ProductUpdatedEvent event) {
        publishEvent(event);
    }

    /**
     * Publishes a product.deleted event to Kafka after the transaction commits.
     *
     * @param event the product deleted event
     */
    @TransactionalEventListener
    public void handleProductDeleted(ProductDeletedEvent event) {
        publishEvent(event);
    }

    private void publishEvent(ProductEvent event) {
        String topic = event.eventType();
        String key = event.productId().toString();

        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, key, event);
        addCorrelationHeader(record);

        kafkaTemplate.send(record).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish Kafka event: topic={}, productId={}, error={}",
                        topic, event.productId(), ex.getMessage(), ex);
            } else {
                log.info("Published Kafka event: topic={}, productId={}, partition={}, offset={}",
                        topic, event.productId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    private void addCorrelationHeader(ProducerRecord<String, Object> record) {
        String correlationId = org.slf4j.MDC.get("X-Correlation-Id");
        if (correlationId != null) {
            record.headers().add(CORRELATION_ID_HEADER,
                    correlationId.getBytes(StandardCharsets.UTF_8));
        }
    }
}
