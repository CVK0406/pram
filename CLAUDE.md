# CLAUDE.md — PRAMS (Project Resource Allocation Management System)

## Build & Run

```bash
# Build (skip test for speed)
./mvnw clean install -DskipTests

# Run app (needs PostgreSQL on localhost:5432, db=prams)
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run single test class
./mvnw test -Dtest=AllocationServiceTest

# Run single test method
./mvnw test -Dtest=AllocationServiceTest#testCreateAllocation_Exceeds100Percent_ThrowsException

# Full stack via Docker
docker compose up --build

# Compose down + clean volumes
docker compose down -v
```

## Architecture

Typical layered Spring Boot REST API, no web frontend.

| Layer | Role | Rules |
|---|---|---|
| **Controller** | REST endpoints, `@Valid` on DTOs | No business logic |
| **Service** | Interface + impl, `@Transactional` | Business rules, entity lookup |
| **Repository** | Spring Data JPA interfaces | Queries only |
| **Entity** | JPA `@Entity` mapped to DB tables | No serialization to API |
| **DTO** | Request/Response POJOs | Bean Validation annotations |
| **Exception** | Custom exceptions + `GlobalExceptionHandler` | Consistent error response format |
| **Mapper** | Static methods, entity ↔ DTO | No mapping framework |

### Data flow
```
HTTP → Controller (@Valid DTO) → Service (interface) → ServiceImpl (business rules)
  → Repository (JPA) → PostgreSQL
  → Response DTO ← Mapper ← Entity
```

### Key business rules (AllocationService)
1. `0 < allocationPercent <= 100`
2. Sum of overlapping allocations per employee **never exceeds 100%** — checked via JPQL overlap query, guarded by `PESSIMISTIC_WRITE` lock for concurrency
3. No allocation to `COMPLETED` projects
4. Soft-delete (`deleted_at`) on allocations; all aggregate queries filter `WHERE deleted_at IS NULL`

### Reporting queries
- `/reports/utilization` — GROUP BY employee, COALESCE SUM
- `/reports/available` — HAVING `100 - SUM > minAvailable`
- `/reports/overloaded` — HAVING `SUM > 90`
- `/employees/{id}/workload` — detail with allocation list

## Conventions

- **Commit messages**: [Conventional Commits](https://www.conventionalcommits.org/) — `feat:`, `fix:`, `chore:`, `refactor:`, `test:`
- **Error response format**: `{ timestamp, status, error, message, path }` via `ErrorResponse` DTO
- **Interface pattern**: `EmployeeService` interface + `EmployeeServiceImpl` impl class in `service.impl` package
- **Branch**: work on `main` (single dev), commit per phase

## Project structure

```
com.company.pram
├── config/          # (placeholder) Spring config classes
├── controller/      # REST controllers
├── dto/
│   ├── request/     # Inbound DTOs with @Valid annotations
│   └── response/    # Outbound DTOs
├── entity/          # JPA entities
├── exception/       # Custom exceptions + GlobalExceptionHandler
├── mapper/          # Static entity↔DTO mappers
├── repository/      # JPA repositories
└── service/         # Interfaces + impl/
```

## Naming conventions

- DB columns: `snake_case`
- Java: camelCase, PascalCase classes
- Allocations: soft-delete via `deleted_at` column
- Sorting: default by `employee_id` / `project_id` / `allocation_id`

## Phase status

- [x] Phase 0 — Setup (project init, DB, Docker, ping endpoint)
- [x] Phase 1 — Employee module (CRUD, duplicate validation, pagination)
- [x] Phase 2 — Project module (CRUD, status transitions)
- [x] Phase 3 — Allocation happy path
- [x] Phase 4 — Allocation business rules (2/3)
- [x] Phase 5 — Concurrency handling (pessimistic lock)
- [x] Phase 6 — Reporting APIs
- [x] Phase 7 — Exception & validation polish
- [x] Phase 8 — Unit tests
- [x] Phase 9 — Swagger, Postman, README
- [ ] Phase 10 — Docker Compose
- [ ] Phase 11 — AI Bonus
- [ ] Phase 12 — Final review

## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

Rules:
- For codebase questions, first run `graphify query "<question>"` when graphify-out/graph.json exists. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
