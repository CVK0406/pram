# Project Resource Allocation Management System (PRAMS)

A Spring Boot REST API for managing employee resource allocations across projects in an outsourcing company. Enforces business rules: total allocation per employee ≤ 100%, no allocation to COMPLETED projects, and date boundary validation.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.1.0 |
| Database | PostgreSQL 15 |
| ORM | Spring Data JPA / Hibernate |
| Validation | Jakarta Bean Validation |
| Documentation | springdoc-openapi 3.0.3 (Swagger UI) |
| Testing | JUnit 5, Mockito |
| Build | Maven |
| Deployment | Docker Compose |

---

## Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (with Compose)
- Java 25 + Maven (for local development only)

---

## Running with Docker Compose (Recommended)

```bash
# Clone the repository
git clone <repo-url>
cd RA

# Build image and start all services (app + PostgreSQL)
docker compose up --build -d

# Check status
docker compose ps

# View live app logs
docker logs -f prams-backend
```

The application will be available at **http://localhost:8080**

To stop all services:
```bash
docker compose down
```

---

## Running Locally (Development)

> Requires a local PostgreSQL instance. Update `src/main/resources/application.properties` with your DB credentials.

```bash
mvn spring-boot:run
```

---

## Running Tests

```bash
mvn test
```

Expected output: **22 tests, 0 failures** (includes unit tests + concurrency integration test).

---

## API Documentation

### Swagger UI

```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON spec:
```
http://localhost:8080/v3/api-docs
```

### Postman Collection

Import [`docs/postman_collection.json`](docs/postman_collection.json) into Postman.  
Set the `baseUrl` variable to `http://localhost:8080`.

---

## API Summary

### Employee (`/employees`)

| Method | Path | Description |
|---|---|---|
| `POST` | `/employees` | Create employee |
| `GET` | `/employees/{id}` | Get employee by ID |
| `GET` | `/employees` | Get all employees (paginated) |
| `GET` | `/employees/{id}/workload` | Get workload detail |

### Project (`/projects`)

| Method | Path | Description |
|---|---|---|
| `POST` | `/projects` | Create project |
| `GET` | `/projects/{id}` | Get project by ID |
| `GET` | `/projects` | Get all projects (paginated, filter by `?status=`) |
| `PUT` | `/projects/{id}/status` | Update project status |

### Allocation (`/allocations`)

| Method | Path | Description |
|---|---|---|
| `POST` | `/allocations` | Create allocation |
| `PUT` | `/allocations/{id}` | Update allocation |
| `DELETE` | `/allocations/{id}` | Soft-delete allocation |
| `GET` | `/allocations?employeeId=` | Get allocations by employee |

### Reports (`/reports`)

| Method | Path | Description |
|---|---|---|
| `GET` | `/reports/utilization` | All employees with total allocation % |
| `GET` | `/reports/available` | Employees with remaining capacity (filter: `?minAvailable=`) |
| `GET` | `/reports/overloaded` | Employees with allocation > 90% |

---

## Business Rules

| Rule | Description |
|---|---|
| **Rule 1** | `allocationPercent` must be between 1 and 100 (Bean Validation `@Min(1) @Max(100)`) |
| **Rule 2** | Total overlapping allocations for an employee must not exceed 100% |
| **Rule 3** | Cannot allocate to a project with status `COMPLETED` |
| **Concurrency** | Pessimistic write locks (`SELECT FOR UPDATE`) prevent race conditions when two requests allocate concurrently |

---

## Error Response Format

All errors return a consistent `ErrorResponse` JSON:

```json
{
  "timestamp": "2026-07-15T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Employee allocation exceeds 100%",
  "path": "/allocations",
  "details": null
}
```

Validation errors include a `details` map with per-field messages:

```json
{
  "timestamp": "2026-07-15T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/employees",
  "details": {
    "fullName": "Full name must not be blank",
    "email": "Email must be a valid email address"
  }
}
```

---

## Project Structure

```
src/main/java/com/company/pram/
├── config/          # OpenAPI config
├── controller/      # REST controllers
├── dto/
│   ├── request/     # Request DTOs (validated)
│   └── response/    # Response DTOs
├── entity/          # JPA entities
├── exception/       # Custom exceptions + GlobalExceptionHandler
├── mapper/          # Entity ↔ DTO mappers
├── repository/      # Spring Data JPA repositories
└── service/
    └── impl/        # Service implementations
```
