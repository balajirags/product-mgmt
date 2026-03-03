---
description: "Language profile for JavaScript/TypeScript projects using npm. Provides build, lint, test, and coverage commands."
applyTo: '**/*.{js,ts,jsx,tsx}'
---

# JavaScript / TypeScript Language Profile

## Detection

This profile applies when the project root contains:
- `package.json`
- `src/` directory with `.js`, `.ts`, `.jsx`, or `.tsx` files

## Build Tool

**npm** — all commands use `npm run` scripts defined in `package.json`.
If `yarn` or `pnpm` lockfiles exist, use the corresponding package manager.

---

## Phase 5 — VERIFY Commands

### Compile / Type Check (TypeScript only)
```bash
npx tsc --noEmit
```
- If the project uses JavaScript only, skip this step
- If compilation fails → read errors, fix type issues, re-run

### Static Analysis — ESLint
```bash
npm run lint
```
- If `lint` script is not defined, try: `npx eslint src/ --ext .ts,.tsx,.js,.jsx`
- Fix violations only in files you created or modified
- Auto-fix available: `npm run lint -- --fix` (use cautiously, review changes)

### Run Existing Tests
```bash
npm test
```

---

## Phase 6 — TEST Commands & Standards

### Test framework
Detect from `package.json` devDependencies:
- **Jest** (most common) — `*.test.ts`, `*.spec.ts`
- **Vitest** — `*.test.ts`, `*.spec.ts`
- Use the framework already configured in the project

### Test standards
- One behavior per test
- `describe` / `it` blocks with clear descriptions
- Arrange / Act / Assert pattern
- Mock external dependencies (API calls, database, file system)
- Use `jest.mock()` or `vi.mock()` for module mocking
- Descriptive names: `it('should return order when valid ID is provided')`

### Test source layout
- Co-located: `src/components/Order.tsx` → `src/components/Order.test.tsx`
- Or separate: `src/` → `__tests__/` — follow existing project convention

### Run tests
```bash
npm test
```

### Run tests with coverage
```bash
npm test -- --coverage
```
Or if using Vitest:
```bash
npx vitest run --coverage
```

### Coverage report location
- Console output (default)
- HTML: `coverage/lcov-report/index.html` (if configured)
- LCOV: `coverage/lcov.info`
- JSON: `coverage/coverage-summary.json` (if configured)

### Extracting per-file coverage from reports
If `coverage-summary.json` exists:
```bash
# List files below 85% line coverage
node -e "
  const r = require('./coverage/coverage-summary.json');
  Object.entries(r).forEach(([f, d]) => {
    if (f !== 'total' && d.lines.pct < 85)
      console.log(f, 'lines:', d.lines.pct + '%', 'branches:', d.branches.pct + '%');
  });
"
```
If only LCOV is available, parse `coverage/lcov.info`:
```bash
# Show per-file hit/miss summary from lcov
awk '/^SF:/{file=$0} /^LH:/{hit=$0} /^LF:/{found=$0; print file, found, hit}' coverage/lcov.info
```
- `LF` = lines found, `LH` = lines hit → coverage % = `LH / LF * 100`

Focus remediation on files with the **highest uncovered line count** first.

### Coverage targets
- Line coverage: ≥ 85%
- Branch coverage: ≥ 80%
- Function coverage: ≥ 90%

---

## Phase 7 — BUILD GATE Command

```bash
npm run build
```

If a full check script exists:
```bash
npm run lint && npx tsc --noEmit && npm test -- --coverage && npm run build
```

Ensure all steps pass before committing.

---

## JavaScript/TypeScript-specific coding rules

1. **Use TypeScript** when the project supports it — prefer strict mode
2. **Prefer `const`** over `let`; never use `var`
3. **Use async/await** over raw Promises or callbacks
4. **Export types separately** — `export type { MyType }` for type-only exports
5. **No `any`** — use `unknown` and narrow, or define proper types
6. **Null checks** — use optional chaining (`?.`) and nullish coalescing (`??`)
7. **No console.log in production** — use a structured logger
8. **Sanitize user input** — especially when rendering HTML (XSS prevention)
9. **Use environment variables** for configuration — never hardcode secrets

---

## On-demand Skills

JavaScript/TypeScript-specific skills should be created as needed for:
- React component patterns
- API client generation (Axios, fetch)
- State management (Redux, Zustand)
- Node.js backend patterns (Express, NestJS)

For now, follow existing project conventions and patterns found during reconnaissance.

---

## Instructions (always applied)

- Security: [../instructions/security-owasp.instructions.md](../instructions/security-owasp.instructions.md)
