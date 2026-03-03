---
description: "Language profile for Kotlin Spring Boot projects using Gradle. Provides build, lint, test, and coverage commands."
applyTo: '**/*.kt'
---

# Kotlin Language Profile

## Detection

This profile applies when the project root contains:
- `build.gradle.kts` with Kotlin plugin and Spring Boot dependencies
- `src/main/kotlin/` directory structure

## Build Tool

**Gradle** — all commands use `./gradlew` with `--no-daemon` flag.

---

## Phase 5 — VERIFY Commands

### Compile
```bash
./gradlew compileKotlin --no-daemon
```

### Static Analysis — detekt
```bash
./gradlew detekt --no-daemon
```
- Report location: `build/reports/detekt/detekt.html`
- Fix violations only in files you created or modified
- If detekt is not configured, skip this step and note it

### Static Analysis — ktlint (formatting)
```bash
./gradlew ktlintCheck --no-daemon
```
- Auto-fix available: `./gradlew ktlintFormat --no-daemon`
- Fix issues only in files you created or modified
- If ktlint is not configured, skip this step and note it

### Run Existing Tests
```bash
./gradlew test --no-daemon
```

---

## Phase 6 — TEST Commands & Standards

### Test framework
- JUnit 5 + MockK (preferred over Mockito for Kotlin) + AssertJ or kotlin.test assertions
- If MockK is not available, use Mockito with `mockito-kotlin` extension
- Use `@ExtendWith(MockKExtension::class)` for MockK
- Do NOT mock the class under test
- One behavior per test method
- Descriptive names: `` `should do what when condition` `` (backtick names)
- Nested test classes via `@Nested` inner classes

### Test source layout
- Production: `src/main/kotlin/<package>/`
- Tests: `src/test/kotlin/<package>/` — mirror the production package structure

### Run tests
```bash
./gradlew test --no-daemon
```

### Run tests with coverage
```bash
./gradlew test jacocoTestReport --no-daemon
```
Note: JaCoCo works with Kotlin but may report lower branch coverage due to compiler-generated
branches (null checks, when expressions). Focus on line coverage and logical branch coverage.

### Coverage report location
- XML: `build/reports/jacoco/test/jacocoTestReport.xml`
- HTML: `build/reports/jacoco/test/html/index.html`

### Extracting per-file coverage from JaCoCo XML
To find files below the coverage threshold, parse the XML report:
```bash
# List all source files with their line coverage (missed / total)
grep -E '<sourcefile' build/reports/jacoco/test/jacocoTestReport.xml \
  | sed 's/.*name="\([^"]*\)".*/\1/' \
  | while read f; do
      echo "=== $f ==="
      grep -A20 "name=\"$f\"" build/reports/jacoco/test/jacocoTestReport.xml \
        | grep 'type="LINE"' | head -1
    done
```
Each `<counter type="LINE" missed="X" covered="Y"/>` gives:
- Line coverage % = `Y / (X + Y) * 100`

For branch coverage use `type="BRANCH"` instead of `type="LINE"`.
Note: Kotlin compiler-generated branches may inflate missed branch counts — focus on line coverage.

### Coverage targets
- Line coverage: ≥ 90%
- Branch coverage: ≥ 85% (slightly lower due to Kotlin compiler-generated branches)

---

## Phase 7 — BUILD GATE Command

```bash
./gradlew clean build --no-daemon
```

This runs: `compileKotlin`, `compileTestKotlin`, `detekt`, `ktlintCheck`, `test`, `jacocoTestReport`, and any other configured checks.

---

## Kotlin-specific coding rules

1. **Prefer `val` over `var`** — immutability by default
2. **Use data classes** for DTOs and value objects
3. **Use sealed classes** for domain exception hierarchies
4. **Use `when` expressions** instead of if/else chains
5. **Use extension functions** judiciously — don't overuse
6. **Null safety** — avoid `!!` (non-null assertion); use `?.`, `?:`, or `let`
7. **Coroutines** — if using, prefer structured concurrency with proper scope

---

## On-demand Skills (load when task requires it)

- REST API: [../skills/java-rest-api.skill.md](../skills/java-rest-api.skill.md) (patterns apply to Kotlin too)
- Service/Repository: [../skills/service-repository.skill.md](../skills/service-repository.skill.md)
- JPA Queries: [../skills/postgres-queries.skill.md](../skills/postgres-queries.skill.md)
- Kafka: [../skills/kafka.skill.md](../skills/kafka.skill.md)
- HTTP Clients (Feign): [../skills/feign-client.skill.md](../skills/feign-client.skill.md)
- Flyway Migrations: [../skills/flyway-migrations.skill.md](../skills/flyway-migrations.skill.md)
- Exception Handling: [../skills/exception-handling.skill.md](../skills/exception-handling.skill.md)

---

## Instructions (always applied for *.kt files)

- Clean code: [../instructions/clean-code.instructions.md](../instructions/clean-code.instructions.md)
- Logging: [../instructions/logging.instructions.md](../instructions/logging.instructions.md)
- Security: [../instructions/security-owasp.instructions.md](../instructions/security-owasp.instructions.md)
