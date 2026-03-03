GOAL: Create ⟪AggregateName⟫Repository for entity ⟪com.example.domain.⟪Aggregate⟫⟫ with id ⟪UUID/Long⟫.
CONTEXT: Follow [../skills/service-repository.skill.md](../skills/service-repository.skill.md) (Repository rules). Prefer latest migrations if entities disagree with DDL.
STACK: Spring Boot ⟪version⟫, Spring Data JPA, Postgres. QueryDSL ⟪yes/no⟫.

REQUIREMENTS:
- extends JpaRepository<⟪Aggregate⟫, ⟪IdType⟫> and JpaSpecificationExecutor<⟪Aggregate⟫>
- finders: findBy⟪BusinessKey⟫(...), existsBy⟪BusinessKey⟫(...)
- paged query by status/date using Page<T> + Pageable
- @EntityGraph for required associations to avoid N+1
- projection interface ⟪AggregateName⟫Summary { … } for list views
- column annotations aligned with migrations (nullable, length, unique)

GENERATE NOW.
