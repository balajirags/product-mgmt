---
description: 'How to write Spring Boot integration tests using Testcontainers, WebTestClient, and real Postgres.'
---

# Integration Testing Skill (Spring Boot + Testcontainers)

## Guidelines

- Annotate test classes with:
  - `@SpringBootTest(webEnvironment = RANDOM_PORT)`
  - `@AutoConfigureWebTestClient`
  - `@Testcontainers`
- Use real Postgres via `Testcontainers`.
- Use `WebTestClient` for HTTP calls.
- Clear DB between tests (`@Sql` or `DbCleaner`).

---

## Code Conventions

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureWebTestClient
class OrderControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired WebTestClient client;
    @Autowired OrderRepository repo;

    @Test
    void shouldPersistOrder_whenValidRequest() {
        var request = new OrderRequest("sku-123", 2);

        client.post().uri("/orders")
              .bodyValue(request)
              .exchange()
              .expectStatus().isCreated();

        assertThat(repo.findAll()).hasSize(1);
    }
}
```
