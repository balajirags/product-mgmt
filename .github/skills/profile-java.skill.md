---
description: "Language profile for Java Spring Boot projects using Gradle. Provides build, lint, test, and coverage commands."
applyTo: '**/*.java'
---

# Java Language Profile

## Detection

This profile applies when the project root contains:
- `build.gradle` or `build.gradle.kts` with Spring Boot dependencies
- `src/main/java/` directory structure

## Build Tool

**Gradle** — all commands use `./gradlew` with `--no-daemon` flag.

---

## Phase 5 — VERIFY Commands

### Compile
```bash
./gradlew compileJava --no-daemon
```

### Static Analysis — PMD
```bash
./gradlew pmdMain --no-daemon
```
- Report location: `build/reports/pmd/main.html`
- Fix violations only in files you created or modified

### Static Analysis — SpotBugs
```bash
./gradlew spotbugsMain --no-daemon
```
- Report location: `build/reports/spotbugs/main.html`
- Fix bugs only in files you created or modified

### Run Existing Tests
```bash
./gradlew test --no-daemon
```

---

## Phase 6 — TEST Commands & Standards

### Test framework
- JUnit 5 + Mockito + AssertJ
- `@ExtendWith(MockitoExtension.class)` for mocks
- Do NOT mock the class under test
- One behavior per test method
- Given/When/Then comments
- Nested test classes: `SuccessCases`, `FailureCases`, `EdgeCases`, `Validation`
- Descriptive names: `shouldDoWhatWhenCondition()`

### Test source layout
- Production: `src/main/java/<package>/`
- Tests: `src/test/java/<package>/` — mirror the production package structure

### Run tests
```bash
./gradlew test --no-daemon
```

### Run tests with coverage
```bash
./gradlew test jacocoTestReport --no-daemon
```

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

Focus remediation on files with the **highest missed count** first.

### Coverage targets
- Line coverage: ≥ 90%
- Branch coverage: ≥ 90%

---

## Phase 7 — BUILD GATE Command

```bash
./gradlew clean build --no-daemon
```

This runs: `compileJava`, `compileTestJava`, `pmdMain`, `spotbugsMain`, `test`, `jacocoTestReport`, and any other configured checks.

---

## On-demand Skills (load when task requires it)

- REST API: [../skills/java-rest-api.skill.md](../skills/java-rest-api.skill.md)
- Service/Repository: [../skills/service-repository.skill.md](../skills/service-repository.skill.md)
- JPA Queries: [../skills/postgres-queries.skill.md](../skills/postgres-queries.skill.md)
- Kafka: [../skills/kafka.skill.md](../skills/kafka.skill.md)
- HTTP Clients (Feign): [../skills/feign-client.skill.md](../skills/feign-client.skill.md)
- Flyway Migrations: [../skills/flyway-migrations.skill.md](../skills/flyway-migrations.skill.md)
- Exception Handling: [../skills/exception-handling.skill.md](../skills/exception-handling.skill.md)

---

## Instructions (always applied for *.java files)

- Clean code: [../instructions/clean-code.instructions.md](../instructions/clean-code.instructions.md)
- Logging: [../instructions/logging.instructions.md](../instructions/logging.instructions.md)
- Security: [../instructions/security-owasp.instructions.md](../instructions/security-owasp.instructions.md)
