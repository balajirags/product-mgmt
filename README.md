# Product Management

A full-stack product catalog application with a Spring Boot backend, React frontend, and PostgreSQL database — orchestrated via Docker Compose.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | React 19, TypeScript, Vite, Tailwind CSS, TanStack Query |
| Backend | Java 21, Spring Boot 3.4, Spring Data JPA, Flyway |
| Messaging | Apache Kafka (product domain events) |
| Database | PostgreSQL 16 |
| Containerization | Docker, Docker Compose |

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose v2+

For local development without Docker:
- Java 21 (e.g., via [SDKMAN](https://sdkman.io/))
- Node.js 20+ and npm
- PostgreSQL 16
- Kafka (optional — events fail silently without it)

## Quick Start (Docker)

```bash
git clone <repo-url> && cd product-mgmt
docker compose up --build
```

This starts all services and seeds 10 demo products automatically.

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080/api/v1/products |
| PostgreSQL | localhost:5432 (user: `postgres`, password: `postgres`, db: `inventory`) |
| Kafka | localhost:9092 |

To stop and remove volumes (full reset):

```bash
docker compose down -v
```

## Local Development (without Docker)

### 1. Start infrastructure

Start PostgreSQL and (optionally) Kafka locally, or use only the infra services from compose:

```bash
docker compose up db kafka -d
```

### 2. Run the backend

```bash
cd backend
./gradlew bootRun
```

The backend starts on http://localhost:8080. Environment variables (with defaults):

| Variable | Default |
|----------|---------|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/inventory` |
| `DATABASE_USERNAME` | `postgres` |
| `DATABASE_PASSWORD` | `postgres` |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |

### 3. Run the frontend

```bash
cd web
npm install
npm run dev
```

Runs on http://localhost:5173. The Vite dev server proxies `/api/` to `localhost:8080`.

### 4. Seed demo data

```bash
./scripts/seed-products.sh
```

Creates 10 sample products via the API. Safe to re-run (skips duplicates).

## Architecture

```
┌────────────┐       ┌────────────┐       ┌────────────┐
│  Frontend  │──────▶│  Backend   │──────▶│ PostgreSQL │
│ (React/Nginx)│  /api │(Spring Boot)│       │            │
└────────────┘       └─────┬──────┘       └────────────┘
                           │
                           ▼
                     ┌──────────┐
                     │  Kafka   │
                     └──────────┘
```

- **Frontend**: React SPA served by Nginx; proxies `/api/` requests to the backend
- **Backend**: Layered architecture (Controller → Service → Repository) with domain events published to Kafka
- **Database**: Schema managed by Flyway migrations

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/products` | List products (paginated, filterable by status) |
| GET | `/api/v1/products/{id}` | Get product by ID |
| POST | `/api/v1/products` | Create a product |
| PUT | `/api/v1/products/{id}` | Update a product |
| DELETE | `/api/v1/products/{id}` | Soft-delete a product |
| POST | `/api/v1/products/batch` | Batch create products |

## Testing

### Backend

```bash
cd backend
./gradlew test                  # Unit tests
./gradlew check                 # Full gate: tests + PMD + SpotBugs + JaCoCo coverage
```

Coverage thresholds: 80% line, 70% branch.

### Frontend

```bash
cd web
npm test                        # Unit tests (Vitest)
npm run test:e2e                # E2E tests (Playwright)
```

## Project Structure

```
├── docker-compose.yml          # Full-stack orchestration
├── scripts/
│   └── seed-products.sh        # Seed 10 demo products
├── backend/
│   ├── Dockerfile
│   ├── build.gradle
│   └── src/main/java/com/inventory/demo/
│       ├── product/            # Product domain (api, service, domain, repository, event)
│       └── exception/          # Global error handling
├── web/
│   ├── Dockerfile
│   ├── nginx.conf
│   └── src/                    # React application
└── README.md
```
