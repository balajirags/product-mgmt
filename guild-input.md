---
# guild-input.md — Guild configuration
# Commit this file. It is the shared Guild config for the whole team.
# Run `guild init` to auto-generate this file from your existing project.
---

## Project
name: Demo                     # REQUIRED
description: >                       # REQUIRED — one paragraph
  Currently this is purely a backend service implmenting, weather, location management and Product management
mode: new-app                        # REQUIRED: new-app | feature | refactor

## Input Artifact
type: openapi                            # REQUIRED: prd | openapi | er-diagram | capabilities | story | jira-key
file: /Users/gbalaji/projects/personal/ai-demo-jira/demo/src/main/resources/api-specs/product-api.yml                    # 

## App Type
type: frontend-spa                  # REQUIRED: backend-api | fullstack | frontend-spa | mobile | cli | microservice
                                     #           agentic-workflow | rag-api | ai-assistant

## AI Stack (only for agentic-workflow / rag-api / ai-assistant — omit for other app types)
# llm-provider: Anthropic            # Anthropic | OpenAI | Azure OpenAI | Google | Ollama
# llm-model: claude-3-5-sonnet-20241022
# embedding-provider: OpenAI         # if different from llm-provider
# embedding-model: text-embedding-3-small
# agent-framework: LangGraph         # LangGraph | CrewAI | AutoGen | LlamaIndex | Spring AI | Vercel AI SDK

## Vector Store (optional — for RAG patterns)
# vector-store: pgvector             # pgvector | Chroma | Pinecone | Weaviate | Qdrant

## LLM Observability (optional — for agentic apps)
# llm-tracing: LangSmith            # LangSmith | Langfuse | OpenTelemetry | none

## Workspace
local-path: /Users/gbalaji/projects/personal/ai-demo-jira
   # Absolute path on local machine (auto-detected by guild init)
git-url: https://github.com/balajirags/agent-location-demo  # Remote git URL (auto-detected from git remote)
default-branch: main                 # Default branch for PRs (default: main)

## Integrations
### GitHub
github-repo: balajirags/agent-location-demo        # owner/repo format — used for PR creation and review

### Jira (optional — omit section if not using Jira)
# jira-url: https://mycompany.atlassian.net   # Base URL of your Jira instance
# jira-project-key: INV                       # Project key (e.g. INV for INV-23)

## Tech Stack

### Core
language: TypeScript                       # REQUIRED: Java | Kotlin | Python | TypeScript
version: 5.x
framework: React+Vite              # REQUIRED: Spring Boot | Ktor | FastAPI | Flask | Express | Next.js | React+Vite
framework-version: 5.x

backend-api-base-url: http://localhost:8080/api/v1   # local dev URL
backend-auth: none  


### Messaging (optional — uncomment if needed)
# broker: Kafka                      # Kafka | RabbitMQ | SQS | Redis Streams | none
# schema-registry: true

### HTTP Client (optional)
# http-client: Feign                 # Feign | WebClient | httpx | axios | none

### Auth (optional)
# auth: JWT                          # JWT | OAuth2 | API Key | mTLS | none
# auth-provider: Keycloak            # Keycloak | Auth0 | Okta | AWS Cognito | none

### Observability (optional)
# tracing: OpenTelemetry
# metrics: Prometheus
# logging: structured                # structured (JSON to stdout) | plain

## Infrastructure

### Containerisation
docker: true
native-build: false                  # GraalVM native image — JVM stacks only

### Kubernetes (optional — omit if not deploying to K8s)
# cluster-name: prod-cluster
# kubeconfig: ~/.kube/prod-config    # Path to kubeconfig file
# registry: ghcr.io/myorg/my-service # Container image registry URL
# environments: [local, nonprod, prod]

## Quality Gates

### Coverage Thresholds
unit-line: 90                        # % minimum line coverage for unit tests
unit-branch: 85                      # % minimum branch coverage


### Static Analysis
strictness: standard                 # strict | standard | relaxed

### Performance (optional — triggers Architect to design for these targets)
# p99-latency-ms: 200
# throughput-rps: 1000

### Eval Thresholds (agentic apps only — scored against evaluation dataset with live LLM)
# eval-groundedness: 85             # % of answers grounded in retrieved documents
# eval-safety: 100                  # % pass safety/guardrail check
# eval-relevance: 80                # % of retrieved documents relevant to query

## Testing Tools
api-e2e: none                       # bruno | postman | rest-assured | none
ui-e2e: playwright                   # playwright | cypress | none  (required if fullstack/frontend)

## Guild
autonomy: autopilot                    # autopilot | standard | supervised
intake-mode: strict                   # strict (≤5 Qs) | balanced (≤3) | fast (0 Qs, use defaults)
mode-confidence-threshold: 0.80       # below this → confirm mode before starting
allow-prod-with-override: false       # true = permit prod deploy even if guardrail was overridden this session
release-profile: dev                  # dev (reduced checks) | prod-ready (security scan + full policy required)
evaluation-policy: stable             # stable (single-run pass) | strict (trend-based N-run) — agentic only
# stack-maturity: ga                  # READ-ONLY — resolved by Guild from maturity matrix
budget-usd: 15.00                     # maximum token spend for this pipeline run (optional)
budget-warn-at: 80                    # warn at 80% of budget (default: 80)

## Guardrails
# allow-force-push: false             # uncomment + set true to allow git push --force (logged + requires --ack-risk)
# allow-prod-write: false             # uncomment + set true to allow writes to k8s/overlays/prod
