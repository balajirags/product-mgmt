# GOAL: Write comprehensive unit tests for the given class.
## 🧠 Guidelines

- Frameworks: `JUnit 5`, `Mockito`, `AssertJ`.
- One behavior per test.
- Use Given-When-Then sections.
- Mock external dependencies only.
- Avoid mocking the class under test.

## Write unit testcases for the class using JUnit:

### ANALYSIS FIRST - CRITICAL

1. Read and analyze the entire class implementation before writing any tests
2. Identify all public methods, private methods called by public methods, and business logic branches
3. Map out all conditional paths (if/else, switch, loops, try/catch blocks)
4. Identify all dependencies and their interaction patterns
5. Note all validation rules, business constraints, and error scenarios

### COVERAGE REQUIREMENTS

- Line Coverage: >95%
- Branch Coverage: >90%
- Method Coverage: 100% (all public methods)
- Exception Coverage: All custom exceptions and error paths

### TEST STRUCTURE REQUIREMENTS

Use @ExtendWith(MockitoExtension.class)
Create @Nested classes for logical grouping (Success, Failure, EdgeCases, Validation)
Follow AAA pattern: Arrange (setup), Act (execute), Assert (verify)
Use descriptive test names: shouldDoWhatWhenCondition()
Use AssertJ for fluent assertions

### MOCKING STRATEGY

Mock ALL external dependencies (@Mock annotation)
Stub ALL method calls with realistic return values
Create separate mocks for different scenarios (success, failure, edge cases)

### COMPREHENSIVE SCENARIO COVERAGE

Happy path scenarios with valid inputs
All business logic branches and conditions
All validation rules individually tested
Error scenarios and exception handling
Edge cases: null, empty, boundary values
Complex workflows and method interactions
All enum/constant values and switch cases

### VALIDATION TESTING

Test each @NotNull, @NotBlank, @Pattern, @Positive annotation separately
Test field combinations and cascading validation
Test custom validation logic and business rules
Test error message accuracy and field name conversion

### BUSINESS LOGIC TESTING

Test all calculation logic with various inputs
Test all conditional business rules
Test complex workflows end-to-end
Test state changes and side effects
Test integration between methods within the class

### EDGE CASE REQUIREMENTS

Empty collections and null values
Boundary conditions (min/max values)
Invalid data formats and types
Concurrent access scenarios (if applicable)
Resource exhaustion scenarios
Network/database failure simulation

### TEST DATA STRATEGY

Create realistic test data that matches production scenarios
Use builder patterns or factory methods for complex objects
Vary test data across different test methods
Include both valid and invalid data combinations

### ASSERTION STRATEGY

Use assertThat() for all assertions
Check not just return values but also side effects
Verify mock interactions with verify()
Use isEqualByComparingTo() for BigDecimal comparisons
Assert on collection sizes, contents, and order
Verify exception types, messages, and causes

### PERFORMANCE CONSIDERATIONS

Keep test execution fast (<5 seconds total)
Use @MockitoSettings(strictness = Strictness.LENIENT) if needed
Avoid real database/network calls
Use test slices (@WebMvcTest, @DataJpaTest) when appropriate

### CRITICAL SUCCESS FACTORS

1. UNDERSTAND the business logic completely before coding
2. IDENTIFY all code paths and branches systematically
3. CREATE comprehensive mock scenarios covering all dependencies
4. TEST each business rule and validation individually
5. VERIFY both positive and negative outcomes
6. ENSURE all exceptions and error paths are covered
7. VALIDATE that mocks represent realistic scenarios
8. CONFIRM that tests would catch real bugs

Generate tests that are comprehensive, maintainable, and bulletproof.

## Common Test Case Coverage Techniques

1. Statement Coverage
   - Ensures every line of code is executed at least once.
   - Use in Cursor: Generate test cases via AI or Copilot that touch every line.
2. Branch Coverage (Decision Coverage)
   - Ensures every possible branch (if/else, switch cases) is executed.
   - Use in Cursor: Ask the AI to generate tests covering all logical paths.
3. Condition Coverage
   - Ensures every boolean sub-expression is tested for both true and false.
   - Use in Cursor: Useful when testing complex if conditions.
4. Path Coverage
   - Ensures every possible path through the code is executed.
   - More exhaustive, can be impractical for large functions with many branches.
5. Function/Method Coverage
   - Ensures every method or function is invoked during testing.
   - Basic, but important in modular codebases.
6. Loop Coverage
   - Ensures loops execute zero times, once, and multiple times.
