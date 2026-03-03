# Flyway Migration — Prompt Pack for Copilot (PostgreSQL)

Use these prompts to make Copilot generate safe, zero‑downtime‑friendly Flyway migrations and companion DDL/DML scripts with strong conventions.

> Replace ⟪placeholders⟫ before use. Keep these blocks at the top of the file while Copilot generates, then remove them.

---

## 0) Conventions & Best Practices (Copilot must follow)

* **Versioned migrations**: `V⟪yyyyMMddHHmm⟫__⟪short_kebab_title⟫.sql` (UTC timestamp for ordering). One change per file.
* **Repeatable migrations**: `R__⟪object⟫.sql` only for views/functions/materialized views; include `-- checksum:` marker.
* **Schema**: default `public` unless otherwise specified.
* **Idempotency**: Use `IF NOT EXISTS` / `IF EXISTS` where safe. For destructive changes use expand‑migrate‑contract pattern.
* **Zero downtime** (expand/contract):

  1. **Expand**: add new tables/columns/indexes concurrently, allow nulls.
  2. **Migrate data** in batches; backfill using stable keys, no exclusive locks.
  3. **Switch traffic** (app release uses new structures).
  4. **Contract**: drop old columns/indexes in a separate migration.
* **Indexes**: create `CONCURRENTLY` when possible; never inside a transaction. Use a separate migration if needed.
* **Constraints**: add with `NOT VALID`, then `VALIDATE CONSTRAINT` to avoid table scans and long locks.
* **Defaults**: prefer setting default first, then backfill, then make `NOT NULL`.
* **Audit columns**: `created_at TIMESTAMPTZ NOT NULL DEFAULT now()`, `updated_at TIMESTAMPTZ NOT NULL DEFAULT now()`. Use triggers only if required.
* **Naming**:

  * PK: `pk_<table>`
  * FK: `fk_<from_table>__<to_table>__<column>`
  * Unique: `uq_<table>__<columns>`
  * Index: `idx_<table>__<columns>`
  * Check: `ck_<table>__<rule>`
* **Data types**: prefer `UUID` for surrogate keys; `NUMERIC(19,2)` for money; `JSONB` for schemaless blobs; `TEXT` over varying small varchar.
* **Seed/reference data**: DML must be idempotent using `INSERT ... ON CONFLICT ... DO UPDATE`.
* **Safety**: never drop or rename columns used by running code in the same deploy. Use shadow columns + backfill + application switch.

---

## 1) Prompt — Create New Table (DDL)

```
GOAL: Create a Flyway migration to add new table ⟪table_name⟫ for ⟪purpose⟫.
CONTEXT: PostgreSQL 14+, Flyway. Follow conventions in flyway-migration-prompts.md (naming, zero-downtime).
OUTPUT FILE NAME: V⟪yyyyMMddHHmm⟫__⟪short_kebab_title⟫.sql

REQUIREMENTS:
- Table: ⟪schema⟫.⟪table_name⟫ with columns:
  - id UUID PRIMARY KEY DEFAULT gen_random_uuid()
  - business_key TEXT NOT NULL UNIQUE
  - status ⟪enum/text⟫ NOT NULL DEFAULT '⟪default⟫'
  - payload JSONB NOT NULL DEFAULT '{}'::jsonb
  - created_at TIMESTAMPTZ NOT NULL DEFAULT now()
  - updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
- Indexes: create needed indexes CONCURRENTLY in a separate block (outside transaction).
- Constraints: name with pk_/uq_/ck_/fk_ prefixes.
- Comments for table and columns explaining purpose.
- Wrap transactional DDL in a transaction; create indexes concurrently in a second, non-transactional section.

GENERATE:
1) -- transactional section (CREATE TABLE, constraints, comments)
2) -- non-transactional section (CREATE INDEX CONCURRENTLY)
3) -- verification queries (SELECT to confirm shape) as comments
```

---

## 2) Prompt — Add Column Safely (Expand Phase)

```
GOAL: Add column ⟪column_name⟫ to ⟪schema⟫.⟪table⟫ with minimal locking and prepare for backfill.
RULES:
- Step 1: add nullable column with default if applicable.
- Step 2: if default required, set default first, backfill in batches, then set NOT NULL.
- Indexes: create CONCURRENTLY in separate non-transactional block.

GENERATE a versioned migration with:
- ALTER TABLE ... ADD COLUMN ⟪column⟫ ⟪type⟫ NULL;
- Optional ALTER TABLE ... ALTER COLUMN SET DEFAULT ⟪expr⟫;
- Comment on column; add CK/UNIQUE if needed (NOT VALID then VALIDATE).
- Separate section: CREATE INDEX CONCURRENTLY idx_⟪table⟫__⟪column⟫ ON ⟪schema⟫.⟪table⟫(⟪column⟫);
- Commented verification queries.
```

---

## 3) Prompt — Backfill in Batches (DML, Idempotent)

```
GOAL: Backfill ⟪schema⟫.⟪table⟫ set ⟪target_column⟫ = ⟪expression/derived_value⟫ using batches to avoid long locks.
CONTEXT: Large table; use PK pagination with small batches (e.g., 5k rows). Idempotent; safe to re-run.

GENERATE a versioned migration containing:
- DO $$ DECLARE ... $$ LANGUAGE plpgsql; block that loops in batches
  - SELECT min(id), max(id) windows or use OFFSET/LIMIT free pattern
  - UPDATE ... WHERE id > last_id ORDER BY id ASC LIMIT ⟪batch_size⟫;
  - PERFORM pg_sleep(0.01) between batches (tiny pause)
- Update only rows where ⟪target_column⟫ IS NULL to be idempotent
- Final verification SELECT count(*) WHERE target_column IS NULL
```

---

## 4) Prompt — Enforce NOT NULL After Backfill (Contract Phase)

```
GOAL: After application uses new column, enforce NOT NULL constraint.
GENERATE a small versioned migration with:
- ALTER TABLE ⟪schema⟫.⟪table⟫ ALTER COLUMN ⟪column⟫ SET NOT NULL;
- Add CHECK/UNIQUE/FOREIGN KEY constraints if applicable (NOT VALID then VALIDATE).
- Commented verification queries.
```

---

## 5) Prompt — Create/Validate Foreign Key Without Full Table Lock

```
GOAL: Add FK from ⟪from_table⟫(⟪from_col⟫) → ⟪to_table⟫(⟪to_col⟫) safely.
RULES: Use NOT VALID, then VALIDATE CONSTRAINT.
GENERATE:
- ALTER TABLE ... ADD CONSTRAINT fk_⟪from⟫__⟪to⟫__⟪from_col⟫ FOREIGN KEY (⟪from_col⟫) REFERENCES ⟪to⟫(⟪to_col⟫) NOT VALID;
- ALTER TABLE ... VALIDATE CONSTRAINT fk_⟪from⟫__⟪to⟫__⟪from_col⟫;
- Verification SELECTs for invalid rows as comments.
```

---

## 6) Prompt — Concurrent Index Creation

```
GOAL: Create index concurrently on ⟪schema⟫.⟪table⟫(⟪columns⟫) with a where-clause if partial.
CONSTRAINTS: Must be outside of a transaction.
GENERATE a migration that:
- Starts with `-- flyway:cleanDisabled=true` comment
- Contains only `CREATE INDEX CONCURRENTLY idx_⟪table⟫__⟪cols⟫ ON ⟪schema⟫.⟪table⟫(⟪columns⟫) ⟪WHERE condition⟫;`
- Provides a commented alternative name if index already exists (idempotency check using `to_regclass`).
```

---

## 7) Prompt — Reference/Seed Data (Idempotent DML)

```
GOAL: Seed/update reference data in ⟪schema⟫.⟪table⟫ with idempotent upserts.
RULES: Use natural keys, not surrogate IDs; avoid hard-coded UUIDs unless stable.
GENERATE a migration with:
- INSERT INTO ⟪schema⟫.⟪table⟫ (⟪cols⟫) VALUES
  (⟪vals1⟫), (⟪vals2⟫)
  ON CONFLICT (⟪natural_key_cols⟫) DO UPDATE SET ⟪mutable_cols⟫ = EXCLUDED.⟪mutable_cols⟫;
- Include comments about why values were chosen and how they’re used.
- Verification SELECTs.
```

---

## 8) Prompt — Enum Pattern (Lookup Table or CHECK)

```
GOAL: Introduce enum ⟪name⟫ for column ⟪table.column⟫ using a lookup table (recommended) or PostgreSQL enum type.
GENERATE (lookup table approach):
- CREATE TABLE ⟪schema⟫.⟪name⟫_enum (code TEXT PRIMARY KEY, label TEXT NOT NULL);
- Seed with ON CONFLICT upsert (see seed prompt).
- Add FK from ⟪table⟫(⟪column⟫) to enum table (NOT VALID + VALIDATE).
- Optional CHECK to restrict values.

(If using PostgreSQL ENUM):
- DO $$ BEGIN CREATE TYPE ⟪name⟫ AS ENUM ('⟪v1⟫','⟪v2⟫'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
- ALTER TABLE ... ADD COLUMN ... ⟪name⟫ NOT NULL DEFAULT '⟪v1⟫';
- Comment on type and column.
```

---

## 9) Prompt — JSONB Column with GIN Index

```
GOAL: Add JSONB column ⟪column⟫ to ⟪table⟫ with default and create GIN index.
GENERATE:
- ALTER TABLE ... ADD COLUMN ⟪column⟫ JSONB NOT NULL DEFAULT '{}'::jsonb;
- CREATE INDEX CONCURRENTLY idx_⟪table⟫__⟪column⟫__gin ON ⟪table⟫ USING GIN (⟪column⟫ jsonb_path_ops);
- Comments and verification SELECT using `?`/`@>` operators.
```

---

## 10) Prompt — Safe Column Rename (Shadow + Swap)

```
GOAL: Rename ⟪old_col⟫ → ⟪new_col⟫ without breaking running app.
STRATEGY (multi-migration):
1) Expand: ADD COLUMN ⟪new_col⟫ with type of ⟪old_col⟫ NULL; backfill from ⟪old_col⟫ in batches; write app to use ⟪new_col⟫.
2) Contract: Drop ⟪old_col⟫ in a later migration once unused.
GENERATE the expand migration with:
- ALTER TABLE ADD COLUMN ⟪new_col⟫ ... NULL;
- DO $$ plpgsql batch backfill from ⟪old_col⟫ WHERE ⟪new_col⟫ IS NULL $$;
- Optional trigger to keep cols in sync during rollout (and separate migration to drop trigger later).
```

---

## 11) Prompt — Verification Only Migration (Guardrail)

```
GOAL: Add a verification migration that asserts invariants after a deploy.
GENERATE:
- SELECT assertions wrapped in DO $$ BEGIN ... RAISE EXCEPTION ... END $$ when violated, e.g.:
  - orphan FKs count must be 0
  - NOT NULL candidates with no nulls before setting constr
```
