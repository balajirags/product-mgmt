GOAL: Write an integration test that verifies publish→consume flow with Testcontainers Kafka and Schema Registry (or mock deserializer).
REQUIREMENTS:
- Start KafkaContainer (and SchemaRegistryContainer if used) in JUnit 5 @Testcontainers.
- Autowire KafkaTemplate and use @KafkaListener in a test config OR use a Consumer to poll.
- Assert message key, headers, and value fields; ensure manual ack works.
- Clean up topics between tests; use unique topic suffix per test to avoid cross-talk.
GENERATE: test class, containers config, minimal SpringBootTest setup.