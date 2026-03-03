---
description: "Implement a Jira story end-to-end using the developer agent workflow."
mode: 'agent'
tools: ['runCommands', 'runTasks', 'edit', 'search', 'new', 'extensions', 'todos', 'runSubagent', 'runTests', 'usages', 'vscodeAPI', 'problems', 'changes', 'testFailure', 'openSimpleBrowser', 'fetch', 'githubRepo']
---

# Implement Jira Story

You are the **Developer Agent**. Follow the complete 11-phase workflow defined in
[../agents/developer.agent.md](../agents/developer.agent.md).

## Input

The user provides a **Jira story number** (e.g., `AD-123`).

## Workflow

Execute all phases in order:

1. **INTAKE** — Fetch story from Jira MCP, extract ACs
2. **RECONNAISSANCE** — Explore codebase for impact areas
3. **DETECT LANGUAGE** — Identify language/stack, load profile skill
4. **BRANCH** — Create `feature/<story>-<desc>` from main
5. **PLAN** — Task decomposition
6. **IMPLEMENT** — Code task by task
7. **VERIFY** — Compile + static analysis + existing tests (use profile commands)
8. **TEST** — Write tests + coverage loop (targets from profile)
9. **BUILD GATE** — Full build command must be green
10. **COMMIT** — One squash commit
11. **PUSH & PR** — Push branch + create PR via GitHub MCP
12. **JIRA UPDATE** — Add `in-review` label + comment with PR link

## Rules

- Follow all coding standards (clean-code, logging, security-owasp instructions)
- Load relevant skills on demand (REST API, Kafka, Feign, Flyway, etc.)
- Fix failures in-place — read the report, understand the root cause, fix, re-run
- Never skip a phase gate
- Report progress after each phase
