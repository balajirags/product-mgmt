---
description: 'How to build safe, production-ready OpenFeign HTTP clients with resilience, observability, and proper error handling.'
---

# Best Practices for Using OpenFeign

> Purpose: Generate safe, production‑ready Feign clients with proper resilience patterns.

---

## 0) TL;DR Checklist

* Always set **connect/read timeouts** and **max connections**.
* Use **`OkHttpClient`** for connection pooling + **gzip**.
* Use feign-gson for json serialisation/deserialisation.
* Add **`Retryer.NEVER_RETRY`** (prefer app‑level retries with backoff via Resilience4j).
* Add **`resilience4j`** (circuit breaker + retry + bulkhead) around Feign calls.
* Implement a reusable **`ErrorDecoder`** mapping HTTP → domain exceptions.
* Prefer **DTOs** (no entities) and **explicit Jackson config**.
* Add **`RequestInterceptor`** for auth/trace headers.
* Enable **metrics + tracing** (Micrometer, OpenTelemetry).
* Write **tests** with **WireMock** (or MockWebServer) and contract‑like fixtures.
* Document client behavior (timeouts, retries, error model) in **Javadoc**.
* Externalise all timeouts and circuit breaker configuration

---

## DTO Guidelines

* Make DTOs **immutable** (`@Builder`, `@JsonCreator`, or records in Java 17+).
* Include **`@JsonIgnoreProperties(ignoreUnknown = true)`** on responses.
* Use **specific types** for money, dates (`Instant`, `OffsetDateTime`), quantities.

---

## Error Handling Principles

* Convert HTTP errors into **typed domain exceptions**.
* Do **not** swallow body content—include **error code/message** when present.
* Distinguish **idempotent safe retries** (GET) from **non‑idempotent** (POST without idempotency key).

---

## Resilience Guidance

* Use **bulkhead** for isolation (`ThreadPoolBulkhead` when calls block).
* Set **timeouts** at HTTP client + circuit breaker **`timeoutDuration`** if using TimeLimiter.
* Prefer **no Feign internal retries**; centralize in Resilience4j with **backoff + jitter**.

---

## Pagination, Sorting, Filtering

* Define a **`PageRequest`** with `page`, `size`, `sort` parameters.
* Define a **`PageResponse<T>`** with `content`, `page`, `size`, `totalElements`.
* Keep client methods **explicit** (no `Map<String,String>` for core params).

---

## Idempotency & Safe Writes

* For **POST that create resources**, require a **`Idempotency-Key`** header and **client‑generated IDs** when possible.
* Server should handle **dedupe**; client should **retry only** when previous outcome is unknown (timeouts, 5xx).

---

## Versioning & Compatibility

* Prefix **base path** with version `/v1`.
* Avoid breaking changes; if needed, create a **new client interface** `CatalogClientV2`.

---

## Observability

* **Logging**: `Logger.Level.BASIC` in prod, `FULL` only in debug.
* **Tracing**: propagate `traceparent`/`b3` headers via interceptor.
* **Metrics**: count calls, latency, errors by **client + endpoint** (Micrometer tags).

---

## Testing Strategy

* **Unit**: mock `CatalogClient` interface; verify gateway logic.
* **Contract/Integration**: **WireMock** or **MockWebServer** to simulate HTTP.
* **Resilience**: test CB open/half‑open, retries, timeouts.

---

## Security

* Never log **PII** or **secrets**; mask sensitive headers.
* Pin TLS versions/ciphers if required; validate certificates when using mTLS.
* For OAuth2, prefer **client‑credentials** and short‑lived tokens; cache safely.

---

## Anti‑Patterns to Avoid

* Using default Feign **infinite retries** → can cause request storms.
* Missing **timeouts** → thread exhaustion under latency.
* Returning **`ResponseEntity<byte[]>`** for large payloads without streaming.
* Mixing **entities** with DTOs; leaking persistence concerns.
* Implicit **`Map<String,String>`** params → brittle APIs.
* Global **`FULL`** logging in prod → sensitive data exposure.

---

## Advanced Notes

* For very high‑throughput, prefer **pooled OkHttp** and keep‑alive tuning.
* Consider **`feign-hc5`** only when Apache features are required.
* Reactive? OpenFeign is sync; for reactive stacks prefer **WebClient**; you can still wrap in reactive types at the boundary.
* Use **`feign-form`** for advanced multipart scenarios.

---

## Documentation Template (copy per client)

```java
/**
 * Client: Catalog API
 * Base URL: ${catalog.base-url}
 * Timeouts: connect=2s, read=3s, call=5s
 * Retries: none (Resilience4j: maxAttempts=3, backoff=200ms)
 * CircuitBreaker: failureRateThreshold=50%, window=50
 * Auth: OAuth2 client-credentials (Authorization: Bearer <token>)
 * Headers: X-Correlation-Id propagated
 * Error model: 4xx/5xx mapped via DomainErrorDecoder
 */
```
