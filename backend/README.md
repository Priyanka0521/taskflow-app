# SWYNEX Backend (Spring Boot)

REST API for the SWYNEX / Taskflow task manager — Java 17, Spring Boot 3.2,
Spring Security + JWT, Spring Data JPA. H2 in-memory by default (zero-config
dev-mode), MySQL-capable via one-line config swap.

## Tech Stack

- **Java 17** (LTS)
- **Spring Boot 3.2** (web starter)
- **Spring Security 6** + **JJWT 0.11** — stateless Bearer auth
- **Spring Data JPA** + **Hibernate 6**
- **H2** (default, in-memory) or **MySQL 8**
- **Jakarta Validation** — `@Valid` DTOs
- **GlobalExceptionHandler** — validation / not-found / auth errors → JSON
- **Maven** wrapper-free build

## Project Structure (as specified)

```
backend/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/com/taskflow/
        │   ├── TaskflowApplication.java
        │   ├── controller/
        │   │   ├── AuthController.java
        │   │   ├── TaskController.java
        │   │   └── HealthController.java
        │   ├── model/
        │   │   ├── User.java
        │   │   └── Task.java
        │   ├── repository/
        │   │   ├── UserRepository.java
        │   │   └── TaskRepository.java
        │   ├── service/
        │   │   ├── UserService.java
        │   │   └── TaskService.java
        │   ├── config/
        │   │   ├── SecurityConfig.java
        │   │   ├── JwtAuthenticationFilter.java
        │   │   └── JwtUtil.java
        │   ├── dto/
        │   │   ├── AuthResponse.java
        │   │   ├── LoginRequest.java
        │   │   ├── RegisterRequest.java
        │   │   ├── UserResponse.java
        │   │   ├── TaskRequest.java
        │   │   ├── TaskResponse.java
        │   │   ├── TaskListResponse.java
        │   │   └── ApiResponse.java
        │   └── exception/
        │       ├── ErrorResponse.java
        │       ├── ResourceNotFoundException.java
        │       └── GlobalExceptionHandler.java
        └── resources/
            └── application.properties
```

## Getting Started

### Prerequisites

- **JDK 17** (or higher)
- **Maven 3.8+** (or `mvnw` if you add the wrapper)
- *(Optional)* **MySQL 8** if you don't want H2 in-memory

### Run (default: H2 in-memory DB, server.port=5000)

```bash
cd backend
mvn spring-boot:run
```

API → `http://localhost:5000/api`
Health check: `GET http://localhost:5000/api/health` → `{ status: "ok" }`
H2 console (dev only): `http://localhost:5000/h2-console` (JDBC URL: `jdbc:h2:mem:taskflow`, user=`sa`, pw=``)

### Package JAR

```bash
mvn clean package
java -jar target/taskflow-1.0.0.jar
```

### Swap to MySQL

Edit `src/main/resources/application.properties`:
- Comment the H2 block, uncomment the MySQL block
- Create DB: `CREATE DATABASE taskflow;` (schema auto-generated because `spring.jpa.hibernate.ddl-auto=update`)

## JWT Token Setup

- `jwt.secret` in `application.properties` — **change this in production**
  (use a long random string, min 256 bits = 32+ bytes for HS256).
- `jwt.expiration-ms=604800000` = 7 days.
- Token is returned on `/auth/register` and `/auth/login` in the `token` field
  (same JSON shape as the architecture spec).

---

## API Contract

Base: `/api`. All errors return JSON `{ message, [errors] }`.

### Auth (public)

| Method | Path | Body | 2xx Body |
|---|---|---|---|
| POST | `/auth/register` | `{ name, email, password }` | `{ token, user: { _id, name, email } }` (201) |
| POST | `/auth/login`    | `{ email, password }` | `{ token, user: { _id, name, email } }` (200) |
| GET  | `/auth/profile`  | *(Bearer)* | `{ _id, name, email }` |

### Tasks (all require `Authorization: Bearer <token>`)

| Method | Path | Params / Body | 2xx Body |
|---|---|---|---|
| GET | `/tasks` | Query: `?status=pending\|in-progress\|completed&priority=low\|medium\|high&search=text` | `{ tasks: [...], count: n }` (newest first) |
| GET | `/tasks/{id}` | — | `{ task: { _id, title, description, status, priority, dueDate, createdAt, updatedAt } }` |
| POST | `/tasks` | `{ title*, description, status, priority, dueDate("yyyy-MM-dd") }` | 201 `{ task: {...} }` |
| PUT  | `/tasks/{id}` | partial TaskRequest (null `dueDate` clears it) | 200 `{ task: {...} }` |
| DELETE | `/tasks/{id}` | — | 200 `{ message: "Task deleted successfully", _id }` |

Auth failures (no token, bad token, expired): `401` (Spring Security entry point).
Validation errors (jakarta): `400 { message: "Validation failed", errors: { field: "msg" } }`.
Not-found / cross-user access (IDOR guard): `404 { message: "Task not found" }`.
Duplicate email on register: `400 { message: "Email already exists" }`.

---

## Authorization Model

- Each `Task.user` points at one `User` (FK + index).
- Every task query goes through the service layer and explicitly filters by
  `task.user.id == authenticatedUserId`, so IDOR is impossible at the API level.
- Passwords stored via `BCryptPasswordEncoder(strength=10)`.
- `User.email` lowercased on save + unique index to prevent dupes via casing.
- `SecurityConfig` marks only `/api/auth/register`, `/api/auth/login`,
  `/api/health`, and H2 console as public; everything else is STATELESS Bearer.
- CORS is permissive (`*` origins) for localhost/dev, tighten for prod.

---

## Integration with the Frontend

The React frontend (`/frontend`) already uses the exact JSON shapes above:
- JWT stored in `localStorage` as `token`
- Axios attaches `Authorization: Bearer <token>`
- Axios 401 interceptor wipes storage + redirects `/login`
- IDs are serialized as `_id` (see `@JsonProperty("_id")` on DTOs) for full
  compatibility with the frontend code.
