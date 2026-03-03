---
description: 'Clean code Guidelines.'
applyTo: '**/*.java'
---



### CLEAN CODE CONTRACT (Java)

When you generate Java in this repo, follow these rules:

- Methods must be small, single-purpose (< ~20 lines) and have ≤3 parameters.
- Do not return null. Use Optional<T> or empty collections.
- All domain objects are immutable (final fields, no public setters). Enforce invariants in constructors/builders.
- No Feign/HTTP/database calls inside methods whose names sound like pure computation.
- Use meaningful names:
  - Booleans start with is/has/should.
  - Collections are plural.
  - No abbrev like cfg/tmp/svc.
- Throw domain-specific runtime exceptions (e.g. InsufficientInventoryException), not generic RuntimeException.
- Never swallow exceptions silently. Never just `e.printStackTrace()`.
- Log business events at INFO with context. Never log secrets.Log events, decisions, failures.Do not log every single method call.
- Tests must assert business behavior, not implementation details or private methods.
- Constants Over Magic Numbers
- Replace hard-coded values with named constants
- Use descriptive constant names that explain the value's purpose
- Keep constants at the top of the file or in a dedicated constants file
- Meaningful Names
- Variables, functions, and classes should reveal their purpose
- Names should explain why something exists and how it's used
- Avoid abbreviations unless they're universally understood
- Single Responsibility
- Each function should do exactly one thing
- Functions should be small and focused
- If a function needs a comment to explain what it does, it should be split
- DRY (Don't Repeat Yourself)
- Extract repeated code into reusable functions
- Share common logic through proper abstraction
- Maintain single sources of truth
- Clean Structure
- Keep related code together
- Organize code in a logical hierarchy
- Use consistent file and folder naming conventions
- Encapsulation
- Hide implementation details
- Expose clear interfaces
- Move nested conditionals into well-named functions

If generated code violates these rules, regenerate and fix it.