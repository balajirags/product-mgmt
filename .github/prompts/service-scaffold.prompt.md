## Prompt 4 — Service Layer (Transactions, Validation, Conflicts)

```
GOAL: Implement ⟪AggregateName⟫Service orchestrating repository, validation, and mapping.
CONTEXT: Follow [../skills/service-repository.skill.md](../skills/service-repository.skill.md) (Service rules). Do not return entities to controllers.
METHODS:
- create(Create⟪AggregateName⟫Command) → ⟪AggregateName⟫Dto
- getById(⟪IdType⟫) → ⟪AggregateName⟫Dto (NotFound on missing)
- search(⟪AggregateName⟫Query, Pageable) → Page<⟪AggregateName⟫SummaryDto>
- update(⟪IdType⟫, Update⟪AggregateName⟫Command) with optimistic locking if entity has @Version
- domain ops: ⟪e.g., addLine / cancel⟫ with invariants

RULES:
- @Transactional(readOnly=true) for queries; explicit @Transactional for commands
- existsBy⟪BusinessKey⟫ to enforce uniqueness + DB unique constraint handling
- Parameterized logging with correlation IDs (assume MDC)
GENERATE NOW.
```