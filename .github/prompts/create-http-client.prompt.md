Goal
Create a new, production-ready HTTP client for a remote REST API in this Spring Boot repo. Follow the repo's HTTP-client conventions (use OpenFeign + OkHttp, timeouts from properties, no Feign retries, Resilience4j wrapper, typed DTOs, ErrorDecoder, RequestInterceptor, and WireMock tests).

Deliverables
1. A Feign client interface in package `com.example.demo.client` for the target service.
2. A Feign configuration class that:
   - Uses OkHttp as the transport.
   - Reads connect/read/call timeouts from typed `@ConfigurationProperties`.
   - Registers `feign.okhttp.OkHttpClient` and `Retryer.NEVER_RETRY`.
   - Provides a `RequestInterceptor` that propagates `traceparent` and `X-Correlation-Id` and (optionally) adds an Authorization Bearer token using a token provider bean.
   - Provides an `ErrorDecoder` that converts non-2xx responses into a typed `ExternalServiceException` including status and trimmed body.
3. A service class in `com.example.demo.service` that:
   - Calls the Feign client.
   - Is annotated with Resilience4j `@CircuitBreaker(name = "<service>", fallbackMethod = "<fallback>")` and `@Retry(name = "<service>")`.
   - Provides a deterministic fallback method returning an appropriate DTO or default.
4. DTO classes (request/response) in `com.example.demo.model`:
   - Use immutable patterns (record or `@Builder` + final fields).
   - Add `@JsonIgnoreProperties(ignoreUnknown = true)` on response DTOs.
5. `application.yml` entries (example keys) for base URL and timeouts:
   - `myclient.api.url`, `myclient.api.connect-timeout-ms`, `myclient.api.read-timeout-ms`, `myclient.api.call-timeout-ms`
6. WireMock integration test under `src/test/java/...` that:
   - Uses `wiremock-jre8-standalone`.
   - Starts `WireMockServer` on a dynamic port and overrides `myclient.api.url` with `@DynamicPropertySource`.
   - Stubs and asserts behavior for: 200 (happy path), 404 (client error -> fallback or domain exception), 500 (server error -> fallback), and a timeout case.
7. Javadoc on public client methods documenting status codes and the error model.

Constraints & best-practices
- Do not use Feign internal retries; set `Retryer.NEVER_RETRY`.
- Use OkHttp for connection pooling and gzip.
- Do not hardcode secrets; use environment variables or a token provider bean.
- Mask PII and secrets in logs.
- Convert HTTP errors to typed domain exceptions containing HTTP status + trimmed body.
- Externalize configuration to `application.yml` and support profile overrides.
- Add Micrometer tags for client name + endpoint (optional but recommended).

- Feign config beans: OkHttp client, feign okhttp wrapper, Retryer.NEVER_RETRY, RequestInterceptor, ErrorDecoder.
- ErrorDecoder: read body (if present), build message "External service error: status=XXX, body={...}", return `ExternalServiceException`.
- Resilience: annotate service method with `@CircuitBreaker(name="myClient", fallbackMethod="getResourceFallback")` and `@Retry(name="myClient")`.

Testing hints
- Use `wiremock-jre8-standalone` to avoid servlet/jetty version conflicts.
- Use `@DynamicPropertySource` to set `myclient.api.url` to `wireMock.baseUrl()`.
- Make fallbacks small and deterministic so tests can assert exact fallback payloads.
