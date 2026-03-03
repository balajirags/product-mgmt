---
description: 'How to write comprehensive unit tests for Java classes using JUnit 5, Mockito, and AssertJ with high coverage targets.'
---

# Unit Testing Skill (JUnit 5 + Mockito + AssertJ)

## Frameworks & Conventions

- Frameworks: `JUnit 5`, `Mockito`, `AssertJ`.
- One behavior per test.
- Use Given-When-Then sections.
- Mock external dependencies only.
- Avoid mocking the class under test.

---

## Code Conventions

- Class suffix: `*Test`
- Naming: `should<Behavior>_when<Condition>()`
- Assertions with `assertThat(...)`.
- Verify mocks: `verify(repo).save(any())`.
- Use `@ExtendWith(MockitoExtension.class)`.

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock InventoryClient inventoryClient;
    @Mock OrderRepository orderRepository;
    @InjectMocks OrderService orderService;

    @Test
    void shouldCalculateTotal_whenValidOrder() {
        // given
        when(inventoryClient.isAvailable(anyString())).thenReturn(true);

        // when
        var result = orderService.placeOrder("sku-123", 2);

        // then
        assertThat(result.total()).isEqualTo(200);
        verify(orderRepository).save(any());
    }
}
```

---

## Analysis First (Critical)

1. Read and analyze the entire class implementation before writing any tests
2. Identify all public methods, private methods called by public methods, and business logic branches
3. Map out all conditional paths (if/else, switch, loops, try/catch blocks)
4. Identify all dependencies and their interaction patterns
5. Note all validation rules, business constraints, and error scenarios

---

## Coverage Requirements

- Line Coverage: >95%
- Branch Coverage: >90%
- Method Coverage: 100% (all public methods)
- Exception Coverage: All custom exceptions and error paths

---

## Test Structure Requirements

- Use `@ExtendWith(MockitoExtension.class)`
- Create `@Nested` classes for logical grouping (Success, Failure, EdgeCases, Validation)
- Follow AAA pattern: Arrange (setup), Act (execute), Assert (verify)
- Use descriptive test names: `shouldDoWhatWhenCondition()`
- Use AssertJ for fluent assertions

---

## Mocking Strategy

- Mock ALL external dependencies (`@Mock` annotation)
- Stub ALL method calls with realistic return values
- Create separate mocks for different scenarios (success, failure, edge cases)

---

## Comprehensive Scenario Coverage

- Happy path scenarios with valid inputs
- All business logic branches and conditions
- All validation rules individually tested
- Error scenarios and exception handling
- Edge cases: null, empty, boundary values
- Complex workflows and method interactions
- All enum/constant values and switch cases

---

## Validation Testing

- Test each `@NotNull`, `@NotBlank`, `@Pattern`, `@Positive` annotation separately
- Test field combinations and cascading validation
- Test custom validation logic and business rules
- Test error message accuracy and field name conversion

---

## Business Logic Testing

- Test all calculation logic with various inputs
- Test all conditional business rules
- Test complex workflows end-to-end
- Test state changes and side effects
- Test integration between methods within the class

---

## Edge Case Requirements

- Empty collections and null values
- Boundary conditions (min/max values)
- Invalid data formats and types
- Concurrent access scenarios (if applicable)
- Resource exhaustion scenarios
- Network/database failure simulation

---

## Test Data Strategy

- Create realistic test data that matches production scenarios
- Use builder patterns or factory methods for complex objects
- Vary test data across different test methods
- Include both valid and invalid data combinations

---

## Assertion Strategy

- Use `assertThat()` for all assertions
- Check not just return values but also side effects
- Verify mock interactions with `verify()`
- Use `isEqualByComparingTo()` for BigDecimal comparisons
- Assert on collection sizes, contents, and order
- Verify exception types, messages, and causes

---

## Performance Considerations

- Keep test execution fast (<5 seconds total)
- Use `@MockitoSettings(strictness = Strictness.LENIENT)` if needed
- Avoid real database/network calls
- Use test slices (`@WebMvcTest`, `@DataJpaTest`) when appropriate

---

## Coverage Techniques

1. **Statement Coverage** — Every line executed at least once
2. **Branch Coverage** — Every if/else, switch case path executed
3. **Condition Coverage** — Every boolean sub-expression tested for true and false
4. **Path Coverage** — Every possible path through the code executed
5. **Function/Method Coverage** — Every method invoked during testing
6. **Loop Coverage** — Loops execute zero times, once, and multiple times
