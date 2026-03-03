---
description: 'Guidelines for logging in Java classes.'
applyTo: '**/*.java'
---


### LOGGING CONTRACT (Java services)

When generating Java code, follow these rules:

1. Use parameterized logging (log.info("msg id={} sku={}", id, sku)).
   Never use string concatenation inside logs.

2. Respect log levels:
   - INFO: business events (order created, reservation made)
   - WARN: degraded but handled (fallback, timeout with fallback)
   - ERROR: operation failed and user/system will see impact. All Exception logs are ERROR.
   - DEBUG: developer troubleshooting details.
   Do not use TRACE unless explicitly asked.

3. Never log secrets (tokens, passwords, API keys) or PII (full addresses, phone numbers, card numbers).

4. Include correlation IDs / trace IDs.
   Assume MDC is used and X-Correlation-Id is propagated via interceptors.

5. Log exceptions once, at the correct level, with context.
   Do not swallow exceptions silently, and do not spam stack traces at INFO.

6. Do not spam logs inside loops or per-item. Prefer one summary log with counts and identifiers.

If generated code violates this LOGGING CONTRACT, regenerate it.