package com.inventory.demo.product.event;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Captor
    private ArgumentCaptor<ProducerRecord<String, Object>> recordCaptor;

    private ProductEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new ProductEventPublisher(kafkaTemplate);
    }

    @Nested
    class HandleProductCreatedTests {

        @Test
        void shouldPublishToCorrectTopic() {
            UUID productId = UUID.randomUUID();
            ProductCreatedEvent event = ProductCreatedEvent.create(productId, "Widget", "widget", "DRAFT");
            when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(successFuture("product.created"));

            publisher.handleProductCreated(event);

            verify(kafkaTemplate).send(recordCaptor.capture());
            ProducerRecord<String, Object> captured = recordCaptor.getValue();
            assertThat(captured.topic()).isEqualTo("product.created");
            assertThat(captured.key()).isEqualTo(productId.toString());
            assertThat(captured.value()).isEqualTo(event);
        }
    }

    @Nested
    class HandleProductUpdatedTests {

        @Test
        void shouldPublishToCorrectTopic() {
            UUID productId = UUID.randomUUID();
            ProductUpdatedEvent event = ProductUpdatedEvent.create(productId, "Updated", "updated", "PUBLISHED");
            when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(successFuture("product.updated"));

            publisher.handleProductUpdated(event);

            verify(kafkaTemplate).send(recordCaptor.capture());
            ProducerRecord<String, Object> captured = recordCaptor.getValue();
            assertThat(captured.topic()).isEqualTo("product.updated");
            assertThat(captured.key()).isEqualTo(productId.toString());
        }
    }

    @Nested
    class HandleProductDeletedTests {

        @Test
        void shouldPublishToCorrectTopic() {
            UUID productId = UUID.randomUUID();
            ProductDeletedEvent event = ProductDeletedEvent.create(productId);
            when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(successFuture("product.deleted"));

            publisher.handleProductDeleted(event);

            verify(kafkaTemplate).send(recordCaptor.capture());
            ProducerRecord<String, Object> captured = recordCaptor.getValue();
            assertThat(captured.topic()).isEqualTo("product.deleted");
            assertThat(captured.key()).isEqualTo(productId.toString());
        }
    }

    @Nested
    class CorrelationHeaderTests {

        @Test
        void shouldAddCorrelationHeaderWhenMdcHasValue() {
            UUID productId = UUID.randomUUID();
            ProductCreatedEvent event = ProductCreatedEvent.create(productId, "Widget", "widget", "DRAFT");
            when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(successFuture("product.created"));

            MDC.put("X-Correlation-Id", "test-correlation-123");
            try {
                publisher.handleProductCreated(event);
            } finally {
                MDC.clear();
            }

            verify(kafkaTemplate).send(recordCaptor.capture());
            ProducerRecord<String, Object> captured = recordCaptor.getValue();
            assertThat(captured.headers().lastHeader("x-correlation-id")).isNotNull();
            assertThat(new String(captured.headers().lastHeader("x-correlation-id").value()))
                    .isEqualTo("test-correlation-123");
        }

        @Test
        void shouldNotAddCorrelationHeaderWhenMdcIsEmpty() {
            UUID productId = UUID.randomUUID();
            ProductCreatedEvent event = ProductCreatedEvent.create(productId, "Widget", "widget", "DRAFT");
            when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(successFuture("product.created"));

            MDC.clear();
            publisher.handleProductCreated(event);

            verify(kafkaTemplate).send(recordCaptor.capture());
            ProducerRecord<String, Object> captured = recordCaptor.getValue();
            assertThat(captured.headers().lastHeader("x-correlation-id")).isNull();
        }
    }

    @Nested
    class KafkaFailureTests {

        @Test
        void shouldNotThrowWhenKafkaSendFails() {
            UUID productId = UUID.randomUUID();
            ProductCreatedEvent event = ProductCreatedEvent.create(productId, "Widget", "widget", "DRAFT");
            CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new RuntimeException("Kafka unavailable"));
            when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(failedFuture);

            publisher.handleProductCreated(event);

            verify(kafkaTemplate).send(any(ProducerRecord.class));
        }
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<SendResult<String, Object>> successFuture(String topic) {
        RecordMetadata metadata = new RecordMetadata(new TopicPartition(topic, 0), 0, 0, 0, 0, 0);
        SendResult<String, Object> result = new SendResult<>(null, metadata);
        return CompletableFuture.completedFuture(result);
    }
}
