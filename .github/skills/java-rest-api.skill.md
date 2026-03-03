---
description: 'How to structure Java Spring Boot REST APIs with controller/service/repository layers, DDD, and JSON handling.'
---

# Java REST API Skill

## Architecture Guidelines

Follow domain-driven design, TDD, clean code practises.

---

## Controller classes

- Expose only REST API endpoints.
- Always use snake_case in api request and response.
- Do not implement business logic in controllers.
- Only call the corresponding service method for each endpoint.

---

## Service classes

- Implement all business logic here.
- Always handle exceptions within service methods using custom exception classes.
- Use custom exceptions for domain-specific error scenarios.
- Add both success and error logs in each service method.
- Use constants for all hardcoded values (e.g., error messages, status codes, default values).
- Prefer built-in Java and framework functions (e.g., Streams, Collections) over manual or
  traditional approaches (e.g., avoid manual loops when map/filter can be used).

---

## Data Transformation

- Use functional transformation (map/flatMap) for converting between DTOs and Entities.
- Avoid using setters or builders for field-by-field updates.
- Create pure mapping functions that take input and return transformed output.
- Keep transformations immutable and free of side effects.
- Use Stream API operators for collection transformations.
- Maintain a clear separation of concerns: controllers only delegate, services handle logic and error management.
- Follow RESTful conventions for endpoint naming and HTTP method usage.
- Ensure that error responses do not expose internal implementation details.

---

## Domain-Driven Design (DDD)

- **Entities**: `Product`, `ProductVariant` (domain models)
- **Value Objects**: `ProductDimensions`, `PriceRange`
- **Repositories**: Abstract data access patterns
- **Services**: Business logic implementation
- **DTOs**: Request/Response data transfer objects

---

## Clean Code Practices

- **Single Responsibility**: Each class has one reason to change
- **Dependency Inversion**: Depend on abstractions, not concretions
- **Open/Closed**: Open for extension, closed for modification
- **Interface Segregation**: Small, focused interfaces

---

## Controller Guidelines

- **No Business Logic**: Controllers only delegate to services
- **Validation**: Use `@Valid` on request bodies
- **Logging**: Log entry/exit points and errors
- **Error Handling**: Use `onErrorResume` for error mapping
- **HTTP Status**: Use appropriate HTTP status codes

---

## Service Guidelines

- **Validation**: Validate all inputs at service entry points
- **Business Logic**: Implement all business rules in service layer
- **Transaction Management**: Use `@Transactional` for multi-step operations
- **Error Handling**: Map exceptions to domain-specific exceptions
- **Logging**: Log business operations and outcomes

---

## Repository Guidelines

- **SQL Building**: Build dynamic SQL safely with parameterized queries
- **JSON Handling**: Store JSON as strings, parse in service layer
- **Connection Management**: Let Spring manage db connections
- **Error Handling**: Let exceptions bubble up to service layer

---

## CRITICAL: JSON/JSONB Data Handling Architecture

### The JSON Parsing Problem

**MAJOR ISSUE**: Applications storing JSON data in databases often create escaped JSON strings instead of proper JSON objects, causing parsing failures and application errors.

**Root Cause**: Improper serialization creates `'"{\"amount\":500}"'` (escaped string) instead of `'{"amount": 500}'` (proper JSON object).

**Core Responsibilities**:

- Parse JSON strings safely (handle both proper and escaped JSON)
- Serialize objects to clean JSON format
- Validate JSON structure
- Provide graceful error handling with fallback values
- Never allow JSON errors to crash the application
