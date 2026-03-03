---
applyTo: '*'
description: "Project-level context: what we are building, the tech stack, and architectural conventions."
---
# Project Context

## What We Are Building

The Inventory Service is the **source of truth** for product stock quantities, movement history, and order reservations across a multi-location fulfillment network. It exposes REST APIs consumed by an admin frontend and (via Kafka events) by downstream systems.


## Functional Modules

| Module | Key Class(es) | Business Capability |
|---|---|---|
| **Movements** | `MovementService`, `MovementLedgerRepository` | Record and query all stock changes; update inventory state accordingly |
| **Inventory State** | `InventoryService`, `InventoryStateRepository` | Query current stock levels; shipment processing; reconciliation snapshot |
| **Reservations** | `ReservationService`, `ReservationRepository`, `ReservedAggRepository` | Two-phase reservation lifecycle: soft-hold (Redis) → confirm (Postgres) → release/cancel/ship |
| **Availability** | `AvailabilityService` | ATP computation: `on_hand − reserved − safety_stock` |
| **Safety Stock** | `SafetyStockService`, `SafetyStockPolicyRepository` | CRUD for time-bounded minimum stock thresholds |
| **Locations** | `LocationService`, `LocationRepository` | CRUD for physical fulfillment nodes (warehouse, store, darkstore, 3PL) |


## Tech Stack

| Layer           | Technology                        |
|-----------------|-----------------------------------|
| Language        | Java 21                           |
| Framework       | Spring Boot 3.5.x                 |
| Build tool      | Gradle (Groovy DSL)               |
| Persistence     | Spring Data JPA + PostgreSQL      |
| Migrations      | Flyway                            |
| Messaging       | Spring Kafka                      |
| HTTP clients    | Spring Cloud OpenFeign             |
| Resilience      | Resilience4j (circuit breakers, retries) |
| Testing         | JUnit 5, Mockito, AssertJ         |
| Integration tests | Testcontainers, WireMock        |
| Static analysis | PMD, SpotBugs                     |
| Code coverage   | JaCoCo                            |

## Architecture

<!-- TODO: Replace or extend with your actual architecture description -->
- **Layering**: Controller → Service → Repository (classic Spring layered architecture)
- **Package structure**: `com.example.<module>.{api,service,domain,repository,config}`
- **Error handling**: Domain-specific exceptions mapped via `@ControllerAdvice`
- **Configuration**: Environment-specific via Spring profiles (`application-{env}.yml`)

## Source Layout

<!-- TODO: Adjust paths to match your actual project structure -->

| Area               | Path                                          |
|--------------------|-----------------------------------------------|
| **Workspace repo for backend** | `/Users/gbalaji/projects/tw/ai-demo-jira/demo`         |
| **Backend source** | `src/main/java/com/inventory/demo/`         |
| **Backend tests**  | `src/test/java/com/inventory/demo/`         |
| **Resources**      | `src/main/resources/`                         |
| **Test resources**  | `src/test/resources/`                        |
| **Flyway migrations** | `src/main/resources/db/migration/`         |
| **API configs**    | `src/main/resources/application.yml`          |
| **Frontend source** | `<!-- TODO: e.g., frontend/src/ or remove if backend-only -->` |
| **Frontend tests** | `<!-- TODO: e.g., frontend/src/__tests__/ or remove if backend-only -->` |
| **Build config**   | `build.gradle`                                |

> **Rule**: Always create new files in the path matching their layer.
> For example, a new `OrderService` goes in `src/main/java/com/example/orders/service/`,
> NOT in the root or in an unrelated package.

## Key Domain Concepts

This module manages location entities (physical sites) with address and geolocation information. It exposes create, list and update endpoints.

<!-- TODO: List the main domain entities / bounded contexts for your project -->
- **Location** — represents a physical fulfillment node (warehouse, store, darkstore, 3PL) with address and geolocation information
- **Address** — encapsulates the address details of a location
- **Geolocation** — stores the latitude and longitude coordinates of a location

## Conventions

- REST endpoints follow `kebab-case` paths (e.g., `/api/v1/order-items`)
- DTOs are Java records; domain models are JPA `@Entity` classes
- All public service methods have Javadoc
- Database columns use `snake_case`; Java fields use `camelCase`
- Kafka topics follow `<domain>.<event-type>` naming (e.g., `orders.created`)

## Quality Thresholds

| Metric            | Minimum |
|-------------------|---------|
| Line coverage     | 80%     |
| Branch coverage   | 70%     |

> Code that falls below these thresholds MUST NOT be committed.
> The language profile skills define how to measure and extract coverage.
