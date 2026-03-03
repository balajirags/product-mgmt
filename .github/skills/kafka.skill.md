---
description: 'How to build safe, observable, and resilient Kafka producers and consumers using Spring Kafka and Avro/Protobuf.'
---

# Kafka Publisher & Consumer Best Practices (Spring Kafka / Java)

> Purpose: Generate **safe, observable, and resilient** Kafka producers/consumers. Use with Spring Boot + Spring for Apache Kafka. Adapt values to your platform defaults.

---

## 0) TL;DR (non‑negotiables)

* **Topic design matters**: stable key → partitioning → ordering & scalability.
* **Producers**: enable idempotence, `acks=all`, bound in‑flight requests, set retries with backoff & jitter, use compression.
* **Consumers**: at‑least‑once with manual commits; use retry + DLQ topics; cap batch size & processing time.
* **Schema**: use Avro/Protobuf with Schema Registry; keep backward compatibility.
* **Observability**: correlation IDs in headers, Micrometer metrics, structured logs, tracing.
* **Security**: TLS everywhere; SASL auth; least‑privilege ACLs.

---

## 1) Topic conventions

* **Naming**: `domain.context.eventName.v1` (e.g., `orders.reservation.created.v1`), no environment in name (use clusters/namespaces).
* **Keys**: choose a key that captures ordering scope (e.g., `orderId`, `sku+location`).
* **Partitions**: start from traffic × p95 latency ÷ per‑partition capacity; keep a safety margin (2×). Prefer even numbers for rolling upgrades.
* **Retention**:

  * Event streams: delete policy with `retention.ms` (e.g., 7–30d).
  * Compact state topics (latest value per key): `cleanup.policy=compact` (optional +delete for tombstones).
* **Headers** (standardize): `x-correlation-id`, `x-causation-id`, `x-schema-id`, `x-producer`.

---

## 2) Producer (Publisher) — Spring Kafka

### 2.1 Config (idempotent, safe, fast)

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP}
    producer:
      acks: all
      enable-idempotence: true
      retries: 10
      delivery-timeout-ms: 120000       # hard ceiling
      request-timeout-ms: 30000
      max-in-flight-requests-per-connection: 5  # keep ≤5 with idempotence
      linger-ms: 5
      batch-size: 65536                 # 64KB (tune with throughput test)
      compression-type: zstd            # or snappy/lz4/gzip
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: io.confluent.kafka.serializers.KafkaAvroSerializer  # or Protobuf
    properties:
      partitions.auto: false
      client.id: ${HOSTNAME:unknown}-orders-service
      # Schema Registry
      schema.registry.url: ${SCHEMA_REGISTRY_URL}
      basic.auth.user.info: ${SR_API_KEY}:${SR_API_SECRET}
      basic.auth.credentials.source: USER_INFO
      # Observability
      interceptors.classes: io.opentelemetry.instrumentation.kafkaclients.TracingProducerInterceptor
```

### 2.2 Template bean

```java
@Bean
public KafkaTemplate<String, SpecificRecord> kafkaTemplate(ProducerFactory<String, SpecificRecord> pf) {
  KafkaTemplate<String, SpecificRecord> kt = new KafkaTemplate<>(pf);
  kt.setObservationEnabled(true); // Micrometer
  return kt;
}
```

### 2.3 Publishing pattern

```java
public void publishReservationCreated(ReservationCreated event) {
  var headers = new RecordHeaders()
      .add("x-correlation-id", corrId().getBytes(UTF_8))
      .add("x-causation-id", causeId().getBytes(UTF_8))
      .add("x-producer", "orders-service".getBytes(UTF_8));

  ProducerRecord<String, ReservationCreated> rec =
      new ProducerRecord<>(Topics.RESERVATION_CREATED_V1, event.getOrderId(), event);
  rec.headers().add(headers);

  kafkaTemplate.send(rec).whenComplete((res, ex) -> {
    if (ex != null) {
      log.error("kafka.publish.failed topic={} key={} err={}", Topics.RESERVATION_CREATED_V1, event.getOrderId(), ex.toString(), ex);
    } else {
      log.debug("kafka.publish.ok topic={} key={} partition={} offset={}",
          res.getRecordMetadata().topic(), res.getRecordMetadata().partition(), res.getRecordMetadata().offset(), event.getOrderId());
    }
  });
}
```

**Notes**

* Use **keyed** records for ordering per aggregate.
* Don't block on `.get()` unless your use case needs sync confirmation; use callbacks and meter failures.
* Consider **outbox pattern** + Debezium for DB‑to‑Kafka reliability when publishing inside transactions.

---

## 3) Consumer — Spring Kafka

### 3.1 Config (manual acks, backpressure)

```yaml
spring:
  kafka:
    consumer:
      group-id: orders-reservation-consumer
      enable-auto-commit: false
      max-poll-records: 100
      max-poll-interval-ms: 300000
      heartbeat-interval: 3000
      session-timeout-ms: 15000
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: io.confluent.kafka.serializers.KafkaAvroDeserializer
    listener:
      ack-mode: MANUAL
      concurrency: 3                 # <= partitions
    properties:
      specific.avro.reader: true
      # Observability
      interceptors.classes: io.opentelemetry.instrumentation.kafkaclients.TracingConsumerInterceptor
```

### 3.2 Listener pattern (retry/DLQ)

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationCreatedListener {
  private final ReservationService service;
  private final KafkaTemplate<String, SpecificRecord> dltTemplate;

  @KafkaListener(topics = Topics.RESERVATION_CREATED_V1, containerFactory = "kafkaListenerContainerFactory")
  public void onMessage(ConsumerRecord<String, ReservationCreated> rec, Acknowledgment ack) {
    try {
      service.handle(rec.key(), rec.value(), rec.headers());
      ack.acknowledge();
    } catch (TransientUpstreamException e) {
      throw e; // let error handler retry to retry-topic
    } catch (PoisonPillException e) {
      sendToDlt(rec, e);
      ack.acknowledge(); // skip bad message
    } catch (Exception e) {
      throw e; // generic retry path
    }
  }

  private void sendToDlt(ConsumerRecord<String, ReservationCreated> rec, Exception e) {
    ProducerRecord<String, SpecificRecord> dlt = new ProducerRecord<>(Topics.RESERVATION_CREATED_V1_DLT, rec.key(), rec.value());
    dlt.headers().add("x-error", e.getClass().getSimpleName().getBytes(UTF_8));
    dltTemplate.send(dlt);
  }
}
```

### 3.3 Container factory with DefaultErrorHandler & retry topics

```java
@Bean
public ConcurrentKafkaListenerContainerFactory<String, SpecificRecord> kafkaListenerContainerFactory(
    ConsumerFactory<String, SpecificRecord> cf, DeadLetterPublishingRecoverer recoverer) {
  var factory = new ConcurrentKafkaListenerContainerFactory<String, SpecificRecord>();
  factory.setConsumerFactory(cf);
  factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
  var backoff = new ExponentialBackOffWithMaxRetries(5);
  backoff.setInitialInterval(200);
  backoff.setMultiplier(2.0);
  backoff.setMaxInterval(5000);
  var errorHandler = new DefaultErrorHandler(recoverer, backoff);
  errorHandler.addNotRetryableExceptions(PoisonPillException.class);
  factory.setCommonErrorHandler(errorHandler);
  return factory;
}

@Bean
public DeadLetterPublishingRecoverer recoverer(KafkaTemplate<Object, Object> template) {
  return new DeadLetterPublishingRecoverer((r, e) -> new TopicPartition(r.topic() + ".DLT", r.partition()), template);
}
```

**Notes**

* Keep **processing idempotent**: if you update a DB, use unique constraints / idempotency keys to avoid duplicates.
* Keep listener work **bounded** (< `max.poll.interval.ms`) or use **batch listeners**.

---

## 4) Schema & Compatibility

* Use **Schema Registry** (Avro/Protobuf). Set compatibility to **BACKWARD** or **BACKWARD_TRANSITIVE**.
* Use **specific** record generation; avoid generic records in app code.
* Evolve by **adding optional fields with defaults**; avoid removing/renaming without new subject version (v2 topic).

---

## 5) Reliability patterns

* **Outbox/Inbox**: persist event with business change; CDC publisher (Debezium) emits to Kafka; consumer dedupes using inbox table.
* **Exactly‑once semantics (EOSv2)**: if doing read‑process‑write with Kafka input + Kafka output, use transactions (`transactional.id`) and consume/produce in a single transaction or use Kafka Streams with `processing.guarantee=exactly_once_v2`.
* **Retry topics**: `topic.retry.200ms`, `topic.retry.1s`, `topic.retry.5s`, `topic.DLT` with per‑attempt headers (`x-retry-attempt`).

---

## 6) Observability

* **Metrics**: enable Micrometer KafkaClient metrics; create SLOs for publish success rate, record lag, consumer liveness, DLQ rate.
* **Logs**: structured logs with topic, partition, offset, key, corrId.
* **Tracing**: OpenTelemetry interceptors for producer/consumer. Propagate trace/correlation headers.

---

## 7) Security

* TLS to brokers; require SASL (PLAIN/SCRAM/OAUTHBEARER per platform).
* Principle of least privilege: topic‑level ACLs for read/write.
* Don't log secrets; rotate credentials.

---

## 8) Anti‑patterns to avoid

* Auto‑commit consumer offsets with heavy processing.
* Unkeyed events when ordering matters.
* Consumer doing long, blocking I/O without backpressure (causes rebalances).
* Producer retries with unbounded in‑flight requests (breaks idempotence).
* Logging full payloads with PII or secrets.
* Breaking schema compatibility by removing/renaming fields without versioning topics.

---

## 9) Minimal topic admin (optional)

```java
@Bean
NewTopic reservationCreatedTopic() {
  return TopicBuilder.name("orders.reservation.created.v1")
      .partitions(12)
      .replicas(3)
      .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(7 * 24 * 60 * 60 * 1000L))
      .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
      .build();
}
```

---

## 10) Checklist before go‑live

* [ ] Key and partitioning strategy documented and load‑tested
* [ ] Producer idempotence on, retries bounded
* [ ] Consumer manual ack + retry/DLT verified
* [ ] Schema registered with backward compatibility
* [ ] Metrics dashboards and alerts
* [ ] ACLs applied and secrets rotated
