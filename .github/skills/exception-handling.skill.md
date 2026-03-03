---
description: "Standardized exception handling patterns for Spring Boot applications using RFC 7807 ProblemDetail."
applyTo: '**/*.java'
---

# Exception Handling Skill

## Architecture

Use a layered exception strategy:

```
Controller → @ControllerAdvice (GlobalExceptionHandler)
Service    → Throws domain-specific exceptions
Repository → Wraps data access exceptions
```

## Custom Exception Hierarchy

```java
// Base exception — all domain exceptions extend this
public abstract class DomainException extends RuntimeException {
    private final String errorCode;

    protected DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected DomainException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

// 404 — Resource not found
public class ResourceNotFoundException extends DomainException {
    public ResourceNotFoundException(String resourceType, Object identifier) {
        super("RESOURCE_NOT_FOUND",
              String.format("%s not found with identifier: %s", resourceType, identifier));
    }
}

// 409 — Business rule violation or conflict
public class BusinessRuleException extends DomainException {
    public BusinessRuleException(String errorCode, String message) {
        super(errorCode, message);
    }
}

// 422 — Validation failure (beyond @Valid)
public class DomainValidationException extends DomainException {
    private final Map<String, String> fieldErrors;

    public DomainValidationException(String errorCode, String message, Map<String, String> fieldErrors) {
        super(errorCode, message);
        this.fieldErrors = Map.copyOf(fieldErrors);
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
```

## Global Exception Handler

Use `@ControllerAdvice` with RFC 7807 `ProblemDetail`:

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Resource Not Found");
        problem.setProperty("errorCode", ex.getErrorCode());
        return problem;
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleException ex) {
        log.warn("Business rule violation: {} - {}", ex.getErrorCode(), ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Business Rule Violation");
        problem.setProperty("errorCode", ex.getErrorCode());
        return problem;
    }

    @ExceptionHandler(DomainValidationException.class)
    public ProblemDetail handleDomainValidation(DomainValidationException ex) {
        log.warn("Domain validation failed: {} - {}", ex.getErrorCode(), ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setTitle("Validation Failed");
        problem.setProperty("errorCode", ex.getErrorCode());
        problem.setProperty("fieldErrors", ex.getFieldErrors());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleBeanValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid",
                (a, b) -> a));

        log.warn("Bean validation failed: {} errors", errors.size());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setTitle("Validation Failed");
        problem.setProperty("errorCode", "VALIDATION_ERROR");
        problem.setProperty("fieldErrors", errors);
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problem.setTitle("Internal Server Error");
        problem.setProperty("errorCode", "INTERNAL_ERROR");
        return problem;
    }
}
```

## Rules

1. **Never expose stack traces** in API responses — log them server-side only
2. **Use ProblemDetail** (RFC 7807) for all error responses — consistent JSON structure
3. **Include errorCode** in every response — machine-readable, stable across releases
4. **Log at appropriate levels**: `warn` for expected errors (404, 409, 422), `error` for unexpected
5. **Don't catch and swallow** — always log or rethrow
6. **Don't use generic Exception** in service/domain code — use specific domain exceptions
7. **Don't create exceptions for flow control** — exceptions are for exceptional conditions
8. **Include context** in exception messages — what was being done, what identifier was involved
9. **Use parameterized logging** — `log.warn("Order not found: {}", orderId)` not string concatenation
10. **Map external exceptions** at integration boundaries — wrap Feign, JPA, Kafka exceptions into domain exceptions
