Goal
- Iteratively add or extend unit tests (only test files) so that the project (or a target class) reaches:
  - Line coverage >= 90% and
  - Branch coverage >= 90%
- Work inside the repository root at <WORKSPACE_ROOT>.

Constraints & repo rules (must follow)
- Do NOT modify production source files unless explicitly asked; only create/modify tests under src/test/java.
- Use JUnit 5, Mockito, and AssertJ as the project's convention.
- Follow the project's unit-test policy (.github/prompts/unit-test.prompt.md): one behavior per test, nested groups, clear Given/When/Then, use @ExtendWith(MockitoExtension.class) for mocks.
- Tests must be deterministic and fast (no network, DB, or disk writes).
- Avoid starting real threads or sleeping. If randomness is required, seed RNGs in tests.
- When production code requires configuration (Spring autowiring), prefer using constructors or creating instances directly in tests (avoid starting Spring contexts).
- If a needed behavior can't be tested without a minor safe production change, propose the change and wait for confirmation.

Agent instructions — do this loop until success or max iterations
1. Initialization
   - Set variables:
     - WORKSPACE_ROOT = <WORKSPACE_ROOT> (e.g., /path/to/repo/demo)
     - TARGET_CLASS = <CLASS_PATH> (optional — if empty, aim for project-wide coverage)
     - TARGET_LINE_COVERAGE = <TARGET_LINE_COVERAGE> (percent)
     - TARGET_BRANCH_COVERAGE = <TARGET_BRANCH_COVERAGE> (percent)
     - MAX_ITER = 6
     - ITER = 0

2. Run tests + coverage
   - Run in WORKSPACE_ROOT:
     ./gradlew test jacocoTestReport --no-daemon
   - On failure: if tests fail, stop and report failing tests and stack traces; do not attempt coverage until tests pass.

3. Parse coverage results
   - Read build/reports/jacoco/test/jacocoTestReport.xml
   - If TARGET_CLASS specified: extract LINE and BRANCH counters for that class
     - Branch% = covered / (covered + missed) * 100
     - Line% = covered / (covered + missed) * 100
   - If no TARGET_CLASS: compute project-wide counters from the XML.
   - Report current coverage numbers and list of missed branches/lines with context:
     - For each missed branch/line, list file, method, and approximate line numbers (use the XML sourcefile & <line> entries).
   - If both targets are met, stop and return a success summary.

4. Identify most valuable tests to add
   - From the missed items, pick 3–8 highest-impact branches or lines to target this iteration.
   - For each target, produce an explicit test plan:
     - target id: file:line or method
     - rationale (which branch/condition)
     - test inputs and mocks required
     - expected behavior/assertions
     - exact test file path to create/update (e.g., src/test/java/<same package>/FooTest.java)

5. Implement tests
   - Create/modify test files under src/test/java matching the production package.
   - Follow the repo style: class-level Javadoc, @ExtendWith(MockitoExtension.class) when needed, nested test classes for grouping, and assertThat(...) assertions.
   - If any dependency is non-trivial to instantiate, mock it with Mockito (do not mock the class under test).
   - For classes using SecureRandom or RNG, seed or replace RNG using test-only constructor or reflection if a test-only constructor exists; prefer constructor injection.

6. Run tests again
   - Re-run:
     ./gradlew test jacocoTestReport --no-daemon
   - Parse XML again and compute new coverage.
   - If progress is made but targets not yet met, iterate (back to step 4), increment ITER.
   - If ITER >= MAX_ITER, stop and produce a report of remaining missed branches/lines and recommended next tests (or request permission to modify production code).

7. Outputs and artifacts after each iteration
   - Shell output of tests (or failing tests list)
   - New/changed test files (patches or file contents)
   - Coverage summary (line% and branch%) for TARGET_CLASS and project-wide
   - A short list of remaining missed branches with file:line and suggested tests

Failure handling & safety
- If any new test is flaky (non-deterministic), mark it as such and either remove or rewrite it deterministic; prefer deterministic alternatives.
- If impl detail forces complex instrumentation, propose minimal, well-justified and reversible production change and wait for approval.
- Do not add integration or slow tests to cheat coverage. Keep unit tests fast.

Return format when finished
- If success: return "SUCCESS" plus:
  - Coverage summary (before/after)
  - List of files added/changed (paths + one-line purpose)
  - A single command to re-run tests and view HTML report:
    ./gradlew test jacocoTestReport --no-daemon
    open build/reports/jacoco/test/html/index.html
- If stopped due to MAX_ITER or blocking issue: return "INCOMPLETE" plus:
  - Current coverage numbers
  - Tests added this run
  - Remaining missed branches/lines (file:line)
  - Suggested next steps (small test ideas or permissible production code changes)

Example usage (fill placeholders)
- WORKSPACE_ROOT = /Users/gbalaji/projects/tw/target-aifsd-demo/demo
- TARGET_CLASS = src/main/java/com/target/demo/controller/RestExceptionHandler.java
- TARGET_LINE_COVERAGE = 95
- TARGET_BRANCH_COVERAGE = 90

One-liner to start agent run (optional wrapper)
- Start the agent with the above variables and let it iterate:
  (agent-run) — the agent should perform the loop above automatically.

Notes for implementer
- Use the JaCoCo XML counters. Look for <counter type="BRANCH" missed="X" covered="Y"> and sourcefile <line nr="N" mb="missedBranches" cb="coveredBranches"> to map missed branches to line numbers.
- For each missed branch, open the sourcefile and inspect the nearby code to craft the test input.
- Prefer unit-tests that construct production objects directly via constructors rather than using Spring context.