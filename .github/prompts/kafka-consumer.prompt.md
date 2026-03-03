GOAL: Create a @KafkaListener for given topic  with manual ack, retries, and DLT routing.
STACK: Spring Kafka ⟪version⟫. Error handler: DefaultErrorHandler with ExponentialBackOff.
REQUIREMENTS:
- ContainerFactory sets AckMode=MANUAL, concurrency=⟪n<=partitions⟫, value deserializer ⟪Avro/Protobuf⟫.
- Listener delegates to service layer method `handle(key, value, headers)` and acknowledges on success.
- Retry for transient exceptions (rethrow); do not retry `PoisonPillException` (route to DLT and ack).
- DeadLetterPublishingRecoverer publishes to `<topic>.DLT` and preserves headers + error details.
- Structured logging with topic, partition, offset, key, correlationId; no payload PII.
GENERATE: listener class, container factory, DefaultErrorHandler + recoverer beans.