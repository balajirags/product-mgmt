GOAL: Implement a Kafka publisher method for given topic using ⟪Avro SpecificRecord | Protobuf message⟫.
STACK: Spring Boot ⟪version⟫, Spring Kafka, Schema Registry ⟪URL⟫, Micrometer, OpenTelemetry.
REQUIREMENTS:
- Idempotent producer: acks=all, enable-idempotence=true, max-in-flight-requests-per-connection<=5, retries with backoff.
- Keyed by ⟪keyField⟫ to preserve per-key ordering; partitioning is default hash.
- Add headers: x-correlation-id, x-causation-id, x-producer, x-schema-id (if available).
- Async send with callback; log success at DEBUG and failures at ERROR with topic/partition/offset/key.
- No `.get()` blocking; method returns `ListenableFuture`/`CompletableFuture` or is fire-and-forget with metrics.
GENERATE: producer bean/method with template usage, headers, structured logs, and observation enabled.