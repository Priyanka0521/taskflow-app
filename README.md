# taskflow-app — SWYNEX Task Manager (Full-Stack Starter)

> Deliverable for **TASK 1: Project Architecture**. A fully runnable full-stack personal task
> manager: product spec, data models, REST API, and frontend screens in one monorepo.
> See [docs/architecture.md](docs/architecture.md) for the complete architecture document.

## Repository Structure (matches SWYNEX instruction)

```
taskflow-app/
│
├── frontend/             ← React + Vite SPA
│   ├── src/
│   ├── package.json
│   └── README.md
│
├── backend/              ← Spring Boot + JPA + JWT
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── docs/
│   └── architecture.md   ← SWYNEX architecture document
│
├── .gitignore
└── README.md             ← you are here
```

## Quick Start

Prerequisites:
- **JDK 17** (or higher) + **Maven 3.8+** (for the backend)
- **Node.js ≥ 16** (for the frontend)
- *(Optional)* **MySQL 8** — otherwise the backend uses an embedded H2 in-memory DB
  out of the box (zero setup).

### 1) Backend (Spring Boot 3.2 + JPA + JWT)

```bash
cd backend
mvn spring-boot:run
```

→ API running on `http://localhost:5000`

Health check: `GET http://localhost:5000/api/health` returns `{ status: "ok" }`.

H2 console (dev only): `http://localhost:5000/h2-console`
- JDBC URL: `jdbc:h2:mem:taskflow`
- User: `sa`
- Password: *(empty)*

To swap to MySQL, edit `backend/src/main/resources/application.properties`
(comment the H2 block, uncomment the MySQL block, create the `taskflow` schema).

### 2) Frontend (React + Vite)

```bash
cd ../frontend
cp .env.example .env          # VITE_API_URL=http://localhost:5000/api
npm install
npm run dev
```

→ Open `http://localhost:5173` in your browser.

Vite proxies `/api` → `http://localhost:5000`, so no extra CORS setup is needed in dev.

---

## Smoke Test

1. Open the app. You should land on `/login` (auto-redirect because no token yet).
2. Click **Register** → create an account (any email, min 6-char password).
3. You're redirected to `/dashboard` with four empty stat cards.
4. Navigate to **Tasks** → **+ New Task** → create 3 tasks with different statuses:
   - `pending` / `medium`
   - `in-progress` / `high`
   - `completed` / `low`
5. Return to **Dashboard** — stat counts should update (Total=3, Pending=1, etc.) and "Recent Tasks" should show the 3 entries.
6. Back in **Tasks**, use the **search** (find part of a title) and **status filter**.
7. Edit a task → change status. Delete another → confirm. Counts stay consistent.
8. **Logout** → `/login`. Attempt to visit `/tasks` manually → bounces to `/login`.

---

## What's Inside

| Part | Location | Purpose |
|------|----------|---------|
| Architecture document | [docs/architecture.md](docs/architecture.md) | Product idea, data models, API spec, screen designs, security, roadmap |
| Backend API | [backend/](backend/README.md) | Spring Boot 3.2 (Java 17), Spring Security + JWT, Spring Data JPA (H2 or MySQL). Auth + full Tasks CRUD + filter/search. |
| Frontend SPA | [frontend/](frontend/README.md) | React 18 + Vite 5. Login/Register, Dashboard, Tasks (full CRUD grid w/ search + filter), Navbar w/ auth guards. |

### Backend layout (Maven / Spring Boot structure)

```
backend/
├── pom.xml
└── src/main/
    ├── java/com/taskflow/
    │   ├── TaskflowApplication.java   # Spring Boot entry
    │   ├── controller/                # AuthController, TaskController, HealthController
    │   ├── service/                   # UserService, TaskService (business logic)
    │   ├── repository/                # UserRepository, TaskRepository (Spring Data JPA)
    │   ├── model/                     # User, Task entities + enums
    │   ├── config/                    # SecurityConfig, JwtAuthenticationFilter, JwtUtil
    │   ├── dto/                       # Request/Response DTOs w/ jakarta-@Valid
    │   └── exception/                 # GlobalExceptionHandler + ErrorResponse
    └── resources/
        └── application.properties     # H2 by default, MySQL swap + JWT config
```

### Frontend layout

- Entry: [frontend/src/main.jsx](frontend/src/main.jsx)
- Routes + guards (`PrivateRoute` / `PublicRoute`): [frontend/src/App.jsx](frontend/src/App.jsx)
- Pages: [frontend/src/pages/](frontend/src/pages/) — Login, Register, Dashboard, Tasks
- Components: [frontend/src/components/](frontend/src/components/) — Navbar, TaskCard, TaskForm
- Axios client (JWT attach + 401 auto-logout): [frontend/src/services/api.js](frontend/src/services/api.js)
- Design tokens + responsive CSS: [frontend/src/index.css](frontend/src/index.css)

---

## Environment Variables

### Backend (`backend/src/main/resources/application.properties`)

No `.env` file needed — everything lives in the Spring props. Main knobs:

```
server.port=5000
# H2 (default, no install needed)
spring.datasource.url=jdbc:h2:mem:taskflow;DB_CLOSE_DELAY=-1
# ...or MySQL (commented out by default)

jwt.secret=<change me to a 32+ byte random string>
jwt.expiration-ms=604800000   # 7 days
```

### Frontend (`frontend/.env`)

```
VITE_API_URL=http://localhost:5000/api
```

---

## Project Architecture (TASK 1 summary)

For the full write-up, see [docs/architecture.md](docs/architecture.md). The required TASK 1 items are:

| Requirement | Covered in docs/architecture.md |
|---|---|
| **Product idea** (problem, users, scope, feature list) | Section 1 |
| **Data models** (User + Task, fields, indexes, relationships) | Section 4 |
| **API routes** (Auth + Tasks endpoints, request/response shapes, queries) | Section 6 |
| **Frontend screens** (route map, Login / Register / Dashboard / Tasks / Navbar UI) | Section 7 |
| **Architecture document** | `docs/architecture.md` |
| **Starter repository URL** (folder you can push to any git host) | Section 13 & this repo |

---

## Scripts

### Backend (Maven)
```
mvn spring-boot:run          # dev mode on :5000
mvn clean package            # builds target/taskflow-1.0.0.jar
java -jar target/taskflow-1.0.0.jar   # run built JAR
```

### Frontend (npm / Vite)
```
npm run dev      # Vite dev server on :5173
npm run build    # production build to dist/
npm run preview  # serve built dist/
```

---

## Stack

- **Frontend:** React 18, Vite 5, React Router 6, Axios, plain CSS with design tokens
- **Backend:** Java 17, Spring Boot 3.2, Spring Security 6 + JJWT, Spring Data JPA / Hibernate 6, H2 (dev) + MySQL (prod)

## License

MIT
