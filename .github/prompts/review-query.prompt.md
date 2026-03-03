----------------------------
PROMPT: Generate playbook.md from EXPLAIN ANALYZE
----------------------------

System / Instruction:
You are an expert PostgreSQL DBA and runbook author. Your job is to take raw PostgreSQL EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT) output as input, analyze the plan and runtime statistics, and produce a short, practical runbook file named playbook.md. The playbook must be actionable, prioritized, and safe to run in production (use CONCURRENTLY and non-blocking operations when possible). Output only valid Markdown (UTF-8). Do not include the raw EXPLAIN text in the output—only the generated playbook.md content.

Inputs:
- A single text blob containing the exact output of:
  EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT) <SQL>;
- You can assume the SQL is a single SELECT/UPDATE/DELETE/INSERT that was explained.

Parsing goals (what to extract from the EXPLAIN):
1. Top-level plan node type(s) used (Seq Scan, Index Scan, Index Only Scan, Bitmap Heap Scan, Aggregate, Hash Join, Merge Join, Nested Loop, Sort).
2. Whether the plan used indexes or scanned the heap.
3. Estimated rows vs actual rows for the top node and any filtering nodes.
4. Total execution time and per-node timing if present.
5. Buffer usage: shared hit/read/write counts (if BUFFERS present).
6. Any warnings: unexpected high actual rows vs estimate, large buffers read, heavy sort on disk, hash spill, repeated nested loops.
7. Presence of parallel workers and whether they were used.

Output requirements — playbook.md structure and content:
Produce a concise file with these sections (use these exact headings):

---
# playbook.md

## 1) One-line summary
- Single short sentence summarizing the key issue (e.g., "Sequential scan on table X for filter A; no index used — query scanned N rows and took T ms").

## 2) Quick actionable steps (top 3)
- Three prioritized commands/steps (one-liners) to try immediately, in order. Each should be a single line with the exact SQL to run (or precise shell/psql command). Mark if the command is non-blocking (CONCURRENTLY) or disruptive.

## 3) Diagnosis (2-4 bullets)
- Short bullets that explain why the planner chose the current plan and what the metrics suggest (e.g., "estimated rows << actual rows → stale stats", "seq scan chosen because no index on (pid,cid)", "heap blocks read >> index reads").

## 4) Recommended fixes (priority list)
- 4-6 concrete actions in priority order. For each action include:
  - one-line rationale,
  - exact SQL command(s) to run,
  - any flags/notes (e.g., "run in maintenance window", "CREATE INDEX CONCURRENTLY to avoid locks", "VACUUM may be I/O heavy").

## 5) Safety & rollback
- For any potentially dangerous step (VACUUM FULL, CLUSTER, dropping large indexes, schema changes) provide a one-line rollback or mitigation command and a short note on risks.

## 6) Verification & metrics to capture
- Exact EXPLAIN/queries or monitoring metrics to run after each change to prove improvement. Include commands:
  - EXPLAIN ANALYZE command to compare,
  - queries to measure row counts and index usage (e.g., pg_stat_user_indexes, pg_relation_size),
  - monitoring checks (replication lag, CPU, I/O).

## 7) When to consider advanced measures
- Short bullets: partitioning, materialized counters, re-clustering, schema redesign, partial indexes. For each give a one-sentence trigger condition (e.g., "if filtered rows > 60% of table or index doesn't help").

## 8) Minimal "playbook run" script
- Provide a small bash script (psql commands) that: collects baseline, creates index CONCURRENTLY (if recommended), analyzes, runs EXPLAIN ANALYZE again, and optionally drops index if performance regresses. Use placeholders for DBNAME/USER/HOST. Keep it idempotent and safe.

Formatting & tone:
- Be concise, imperative, and practical. Use backticked SQL in code blocks for commands.
- Do not speculate beyond what the EXPLAIN supports; if something is unknown, state "unknown — recommend running ANALYZE and EXPLAIN ANALYZE" and include the command.
- Keep playbook.md under ~300–600 words if possible.

Example prompt footer to run:
Now produce playbook.md for the following EXPLAIN output:
<INSERT EXPLAIN OUTPUT HERE>

----------------------------
END PROMPT
----------------------------

Example expected playbook.md (trimmed)
----------------------------
# playbook.md

## 1) One-line summary
Sequential scan on table `c` for filter (pid = 1 AND cid > 200): scanned ~9,800 rows and took 350ms; no index used.

## 2) Quick actionable steps (top 3)
1. `ANALYZE c;` — refresh stats (non-blocking).  
2. `EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT) SELECT count(*) FROM c WHERE pid = 1 AND cid > 200;` — baseline measurement.  
3. `CREATE INDEX CONCURRENTLY ix_c_pid_cid ON c (pid, cid);` — add composite index (non-blocking).

## 3) Diagnosis
- Planner used Seq Scan because no appropriate index was found or planner expected low selectivity.  
- Estimated rows 9,800 vs actual NNNN → stats may be stale.  
- High heap blocks read indicates many page visits — index beneficial if selectivity low.

## 4) Recommended fixes (priority)
- Create composite index: `CREATE INDEX CONCURRENTLY ix_c_pid_cid ON c (pid, cid);` — rationale: supports equality+range.
- ANALYZE after index: `ANALYZE c;`
- Run EXPLAIN ANALYZE to compare.
- If pid=1 is hot, create partial: `CREATE INDEX CONCURRENTLY ix_c_cid_pid1 ON c (cid) WHERE pid = 1;`
- If index-only scans needed: `VACUUM (VERBOSE, ANALYZE) c;`

## 5) Safety & rollback
- To drop index if issues: `DROP INDEX CONCURRENTLY ix_c_pid_cid;`
- Avoid `CLUSTER` in peak times—requires exclusive lock.

## 6) Verification & metrics to capture
- Baseline and after EXPLAIN ANALYZE and record total runtime and buffers.  
- Run: `SELECT count(*) FROM c WHERE pid = 1 AND cid > 200;` to validate counts.  
- Check index stats: `SELECT * FROM pg_stat_user_indexes WHERE indexrelname = 'ix_c_pid_cid';`

## 7) When to consider advanced measures
- If filtered rows ≈ table size → consider partitioning by `pid` or materialized counters.

## 8) Playbook run script (safe)
```bash
#!/usr/bin/env bash
DB=DBNAME
psql -d "$DB" -c "ANALYZE c;"
psql -d "$DB" -c "EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT) SELECT count(*) FROM c WHERE pid = 1 AND cid > 200;" > baseline.txt
psql -d "$DB" -c "CREATE INDEX CONCURRENTLY IF NOT EXISTS ix_c_pid_cid ON c (pid, cid);"
psql -d "$DB" -c "ANALYZE c;"
psql -d "$DB" -c "EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT) SELECT count(*) FROM c WHERE pid = 1 AND cid > 200;" > after_index.txt
```
