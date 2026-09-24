# SWYNEX — Full-Stack Project Architecture

> TASK 1: Define the product idea, data models, API routes, and frontend screens for a small web application.

## 1. Product Idea

### SWYNEX Task Manager
A clean, modern, personal task-management web app for individuals and small teams.
Users can create an account, log in, and manage their daily work by organizing tasks
with status, priority, and due dates. An overview dashboard summarizes progress so the
user can see exactly where they stand.

### Problem Statement
Most todo apps are either too simple (no priority/status) or too heavy (complex boards,
pricing walls). SWYNEX lands in the sweet spot: fast to set up, enough structure to be
useful, and fully self-hostable.

### Target User
- Freelancers, students, and engineers managing personal workloads
- Small teams (2–10 people) that just need shared task lists (later scope)

### Core Value
1. **Zero-friction onboarding** — sign up with email + password in 10 seconds.
2. **At-a-glance progress** — dashboard shows counts per status.
3. **Filterable task list** — search by title/description, filter by status.
4. **Own your data** — open-source, deployable locally or to any cloud.

### Feature Scope (v1)
| # | Feature | Included |
|---|---------|----------|
| 1 | Email/password registration & login | ✅ |
| 2 | JWT-based protected routes | ✅ |
| 3 | Dashboard with task counts by status | ✅ |
| 4 | CRUD tasks (title, description, status, priority, due date) | ✅ |
| 5 | Filter tasks by status | ✅ |
| 6 | Search tasks by keyword | ✅ |
| 7 | Responsive mobile layout | ✅ |
| 8 | Collaborative workspaces | ❌ v2 |
| 9 | Teams / roles / invites | ❌ v2 |
| 10 | File attachments on tasks | ❌ v2 |
| 11 | Email notifications (due-date reminders) | ❌ v2 |

---

## 2. Technology Stack

### Frontend
- **React 18** — component library
- **Vite 5** — dev server & build
- **React Router 6** — client-side routing with route guards
- **Axios** — HTTP client with interceptors
- **Plain CSS** (design tokens + utility classes) — zero-config styling

### Backend
- **Java 17 (LTS)** + Spring Boot 3.2**
- **Spring Web** — HTTP framework
- **Spring Security 6 + JJWT** — stateless Bearer auth
- **Spring Data JPA / Hibernate 6** — ORM
- **H2** (default, in-memory, zero-config) **or MySQL 8** — RDBMS
- **Jakarta Validation** — request validation on DTOs
- **Maven** — build

### Architecture Style
**Single-page application (SPA) + REST API.**
- Client (Vite dev server in dev, static build in prod)
- Backend (Spring Boot) exposed on `/api`
- Local dev: Vite proxies `/api` → `http://localhost:5000` to avoid CORS in UI code

### Deployment Targets (recommended)
| Layer | Service |
|-------|---------|
| Frontend | Vercel / Netlify / any static host |
| Backend  | Railway / Render / Fly.io / any JVM host / AWS ECS |
| Database | H2 file / MySQL on any VM or MySQL instance |

---

## 3. Repository Layout

```
taskflow-app/
├── README.md                        ← monorepo quick-start guide
├── .gitignore                     ← Java + Node ignores
├── docs/
│   └── architecture.md            ← this document (SWYNEX architecture)
│
├── frontend/                       ← React + Vite SPA
│   ├── README.md
│   ├── index.html
│   ├── vite.config.js            (proxies /api to backend :5000)
│   ├── package.json
│   ├── .env.example
│   └── src/
│       ├── main.jsx             (React root)
│       ├── App.jsx              (Router + route guards)
│       ├── index.css             (design tokens + components)
│       ├── services/
│       │   └── api.js            (Axios client, auth + task API modules)
│       ├── components/
│       │   ├── Navbar.jsx
│       │   ├── TaskCard.jsx
│       │   └── TaskForm.jsx
│       └── pages/
│           ├── Login.jsx
│           ├── Register.jsx
│           ├── Dashboard.jsx
│           └── Tasks.jsx
│
└── backend/                      ← Spring Boot + JPA + JWT
    ├── README.md
    ├── pom.xml
    └── src/main/
        ├── java/com/taskflow/
        │   ├── TaskflowApplication.java
        │   ├── config/           (SecurityConfig, JWT filter, JwtUtil)
        │   ├── controller/       (Auth, Task, Health)
        │   ├── dto/              (Request/Response DTOs)
        │   ├── exception/       (GlobalExceptionHandler)
        │   ├── model/            (User, Task JPA entities + enums)
        │   ├── repository/     (UserRepository, TaskRepository)
        │   └── service/          (UserService, TaskService)
        └── resources/
            └── application.properties   (H2 default, MySQL swap, JWT config)
```

---

## 4. Data Models (JPA / RDBMS)

### 4.1 `users` table (entity `User` )

| Column | Java type | Constraints | Notes |
|---|---|---|---|
| `id` | Long | PK, auto-generated | serialized in JSON as `_id` |
| `name` | String | NOT NULL, 2–50 chars | user's display name |
| `email` | String | NOT NULL, UNIQUE, lowercase | login identity |
| `password` | String | NOT NULL, min 6 chars | BCrypt hash (strength 10) |
| `created_at` | LocalDateTime | auto | when account was created |
| `updated_at` | LocalDateTime | auto | last profile change |

**Indexes:** unique on `email`

**JSON shape (API response):**
```json
{
  "_id": 1,
  "name": "Ada Lovelace",
  "email": "ada@swynex.dev"
}
```

---

### 4.2 `tasks` table (entity `Task`)

Each task is **owned by exactly one user** (row-level isolation via `task.user_id` FK).

| Column | Java type | Constraints | Default | Notes |
|---|---|---|---|---|
| `id` | Long | PK, auto | — | serialized in JSON as `_id` |
| `user_id` | Long | FK → users.id, NOT NULL | — | owner, indexed |
| `title` | String | NOT NULL, max 200 | — | short summary |
| `description` | String | max 2000 | NULL | long-form notes |
| `status` | enum (VARCHAR) | `pending` \| `in-progress` \| `completed` | `pending` | workflow state |
| `priority` | enum (VARCHAR) | `low` \| `medium` \| `high` | `medium` | urgency |
| `due_date` | LocalDate | optional | NULL | |
| `created_at` | LocalDateTime | auto | — | |
| `updated_at` | LocalDateTime | auto | — | |

**Indexes:**
- `(user_id)` — user's list
- `(user_id, status)` — status filter
- `(user_id, created_at DESC)` — ordering

**JSON shape (in `GET /tasks/:id and list entries):**

```json
{
  "_id": 42,
  "title": "Write architecture.md",
  "description": "Cover product, data models, API routes, and screens.",
  "status": "in-progress",
  "priority": "high",
  "dueDate": "2026-09-25",
  "createdAt": "2026-09-24T08:10:00",
  "updatedAt": "2026-09-24T10:30:00"
}
```

---

### 4.3 Relationships & Security Model
```
User ──< Task   (1:N via tasks.user_id → users.id)
```

- Task queries **always** include `user = currentUser` (service-layer filter). Users cannot read or modify
  each other's tasks, even if they guess IDs (authorization at the query layer).
- JWT subject = email + `uid` claim = user's `id`; tokens expire after 7 days by default.
- 401 responses trigger the Axios response interceptor → clears `localStorage` and redirects to `/login`.

---

## 5. Authentication & Authorization Flow

```
┌────────────┐  POST /api/auth/register   ┌──────────────┐
│   Client   │ ───────────────────────────▶│ Spring Boot│
│ (Register) │  { name, email, password }│            │
│            │◀───────────────────────────│            │
│            │   { token, user } (201)  │  Create   │
└────────────┘                             │  User +    │
                                           │  BCrypt   │
┌────────────┐  POST /api/auth/login      └──────────────┘
│   Client   │ ───────────────────────────▶   │
│  (Login)   │  { email, password }          │ DaoAuthProvider
│            │◀───────────────────────────│ bcrypt compare
│            │   { token, user } (200)          │ + sign JWT
└────────────┘
        │
        ▼ Store token + user in localStorage
        │
        ▼ All subsequent requests:
          Authorization: Bearer <token>
                      │
                      ▼
                JwtAuthenticationFilter (once-per-request)
                      │
            ┌─────────┴──────────┐
            │ valid?             │ invalid / missing
            ▼                    ▼
        SecurityContext    401 (HttpStatusEntryPoint)
        + @AuthenticationPrincipal  →  Axios 401 interceptor
        available to controllers               →  wipes token + redirect /login
```

---

## 6. REST API Specification

Base URL in dev: `http://localhost:5000/api`
All `2xx` responses JSON. Errors: `{ message, [errors], [timestamp] }`.

### 6.1 Auth Endpoints (public)

| Method | Path | Description | Request Body | Response (2xx) |
|--------|------|-------------|--------------|----------------|
| POST | `/auth/register` | Create account + token | `{ name, email, password }` | 201 `{ token, user: {_id,name,email} }` |
| POST | `/auth/login` | Login + issue token | `{ email, password }` | 200 `{ token, user: {_id,name,email} }` |
| GET | `/auth/profile` | Current user info | bearer auth | 200 `{ _id, name, email }` |

**Auth errors:** 400 on validation / duplicate email; 401 on bad credentials; 401 on expired/invalid token.

---

### 6.2 Task Endpoints (all require `Authorization: Bearer <token>`)

| Method | Path | Description | Query / Body | Response (2xx) |
|--------|------|-------------|--------------|----------------|
| GET | `/tasks` | List user's tasks | Query: `status`, `priority`, `search` (all optional) | 200 `{ tasks: [...], count: n }` |
| GET | `/tasks/:id` | Get one task | — | 200 `{ task: {...} }` |
| POST | `/tasks` | Create task | `{ title*, description, status, priority, dueDate }` | 201 `{ task: {...} }` |
| PUT | `/tasks/:id` | Update task | partial task JSON | 200 `{ task: {...} }` |
| DELETE | `/tasks/:id` | Delete task | — | 200 `{ message, _id }` |
| GET | `/health` | Public liveness probe | — | 200 `{ status: "ok", message: "SWYNEX API is running" }` |

**Query behavior for `GET /tasks`:**
- `?status=pending` (or `in-progress`, `completed`) — enum filter
- `?priority=high` (or `low`, `medium`) — enum filter
- `?search=architecture` — case-insensitive `LIKE %search%` on `title` OR `description`
- Combined: `?status=in-progress&search=report`
- Results sorted by `created_at DESC (newest first) via Spring Data `Sort`.

**Task errors:** 400 if `title` missing on create; 404 if task not found for the user;
401 if no valid token.

---

## 7. Frontend Screens & Navigation

### Route Map

| Path | Guard | Page Component | Purpose |
|------|-------|----------------|---------|
| `/` | Private | Dashboard | Default landing when logged in |
| `/login` | Public (guest) | Login | Email + password form; redirects to `/dashboard` if already logged in |
| `/register` | Public (guest) | Register | Signup form; same redirect |
| `/dashboard` | Private | Dashboard | Welcome + stat cards + recent tasks |
| `/tasks` | Private | Tasks | Full CRUD + search/filter grid |
| `*` | — | Navigate `/` | Catch-all redirect |

### Route Guard Behavior
- **PrivateRoute** — no `localStorage.token` → `<Navigate to="/login" />`
- **PublicRoute**  — has token → `<Navigate to="/dashboard" />` (no double-login)
- **401 from API** — Axios response interceptor clears storage and forces redirect

---

### 7.1 Login Screen (`/login`)

**Layout:** Centered auth card on the left side of the viewport (desktop) or full-width on mobile.

**UI elements:**
- Title: "Login" + subtitle: "Welcome back!"
- Form:
  - Email (input[type=email], required)
  - Password (input[type=password], required, min 6)
  - Submit button with loading state "Signing in…"
- Error banner above form on 400/401 (message from API)
- Footer link: "Don't have an account? **Register here**" → `/register`

---

### 7.2 Register Screen (`/register`)

**Layout:** Same auth card template as login.

**UI elements:**
- Title: "Register" + subtitle: "Create your account to get started."
- Form:
  - Full Name (text, required, 2–50)
  - Email (email, required)
  - Password (password, min 6)
  - Confirm Password (password, must match — client-side check)
  - Submit button with loading state
- Client-side validation errors rendered in the shared error banner.
- Footer link: "Already have an account? **Sign in here**" → `/login`

---

### 7.3 Dashboard Screen (`/dashboard`)

**Layout:** Two rows. Top = header + CTA. Bottom = stats grid + recent tasks.

**Header row:**
- Greeting: "Welcome, {user.name}!" with subtitle
- Primary button "View All Tasks" → `/tasks`

**Stats grid (4 cards, colored left-border):**

| Card | Value source | Color |
|------|--------------|-------|
| Total Tasks | `tasks.length` | Indigo |
| Pending | filter `status==='pending'` | Amber |
| In Progress | filter `status==='in-progress'` | Blue |
| Completed | filter `status==='completed'` | Green |

**Recent tasks section:**
- Heading "Recent Tasks" + "See all →" link
- Max 5 tasks, ordered `createdAt desc`
- Each row: title + status pill. Empty state → "Create your first task!" button.

---

### 7.4 Tasks Screen (`/tasks`)

**Layout:** Header → inline form (when active) → toolbar → grid.

**Header:**
- Title "Tasks" + subtitle
- "+ New Task" button → toggles `TaskForm` inline

**TaskForm (create / edit modes):**
- Title (text, required)
- Description (textarea, 4 rows)
- Status (select: pending / in-progress / completed)
- Priority (select: low / medium / high)
- Due date (input[type=date])
- Submit: "Create Task" or "Update Task"
- Cancel: resets back to list

**Toolbar:**
- Search input (controlled) → filters by title OR description via API `?search=`
- Status select: All | Pending | In Progress | Completed → `?status=`

**Tasks grid:** responsive CSS grid, cards = `TaskCard` component.
- Hover lift effect
- Priority pill (top-right, color coded)
- Status pill (footer, color coded)
- Due date if set
- "Edit" → loads card into `TaskForm`; "Delete" → confirm dialog then API DELETE

**Empty states:**
- Zero tasks → "No tasks yet. Click 'New Task' to create one."
- Zero tasks after filter → "No tasks match your filters."

---

### 7.5 Shared: Navbar

Pinned to the top of every screen.
- Brand: **SWYNEX** → links to `/` (dashboard when logged in, login when not)
- When **anonymous**: `Login` link + `Register` filled button
- When **authenticated**: `Dashboard`, `Tasks`, text "Welcome, {name}", `Logout` button (clears localStorage → navigates `/login`)

---

## 8. Frontend ↔ Backend Contract

### Axios Client (`frontend/src/services/api.js`)
- `baseURL` read from `VITE_API_URL` (defaults to `http://localhost:5000/api`)
- **Request interceptor** — attaches `Authorization: Bearer <token>` if token in `localStorage`
- **Response interceptor** — on any 401, wipes storage + redirects `/login`

### Named API modules
```js
authAPI.register(data)   → POST /auth/register
authAPI.login(data)      → POST /auth/login
authAPI.getProfile()     → GET  /auth/profile

taskAPI.getAll()         → GET  /tasks
taskAPI.getById(id)      → GET  /tasks/:id
taskAPI.create(data)     → POST /tasks
taskAPI.update(id,data)  → PUT  /tasks/:id
taskAPI.delete(id)       → DELETE /tasks/:id
```

### Expected Response Shapes (so frontend won't break)
- Auth endpoints MUST return `{ token, user: { _id, name, email } }` — frontend stores both, renders name in Navbar/Dashboard. Note: Spring Boot DTOs use `@JsonProperty("_id") so Java `id` field serializes as `_id` to match.
- `GET /tasks` MUST return `{ tasks: [...] }` OR a plain array — frontend tries both
  (see Tasks.jsx fallback `response.data.tasks || response.data || []`).
- Task objects MUST include `_id, title, status, priority, description?, dueDate?`.

---

## 9. Error Handling Strategy

| Layer | Mechanism | What it does |
|-------|-----------|--------------|
| Spring Web | `GlobalExceptionHandler` (@RestControllerAdvice) | Catches validation / auth / 404 / dup-key → { message, errors } JSON |
| Validation | Jakarta `@Valid` on controller DTOs → MethodArgumentNotValidException | produces `{ message: "Validation failed", errors: {field:msg } }` |
| Duplicate email | `DataIntegrityViolationException` → "Email already exists" | |
| 404 resources | Service throws `ResourceNotFoundException` → 404 JSON | Prevents IDOR by returning "Task not found" both for missing rows and cross-user access |
| Auth | Spring Security `HttpStatusEntryPoint` → 401 + Axios interceptor redirect | Clean logout loop |
| Frontend forms | Client-side pre-flight (match passwords, min length) | Fast UX, saves API round-trips |

---

## 10. Security Considerations (v1 checklist)

- [x] Passwords never stored in plaintext — BCryptPasswordEncoder strength 10
- [x] Row-level isolation: every single-task operation re-checks `task.user_id == currentUser`
- [x] CORS permissive for localhost; tighten allowed-origins for prod
- [x] Email lowercased on `setEmail` → avoids dup accounts via casing
- [x] Task endpoints closed by default; only `/auth/register`, `/auth/login`, `/health`, H2 console public
- [x] JWT secret configurable via `application.properties`; change default in prod
- [x] JWT expiry default 7d via `jwt.expiration-ms`
- [x] Axios interceptor wipes localStorage on ANY 401 → prevents replay of stale tokens
- [ ] *(deploy step)* Enforce HTTPS; rotate to short-lived access + HttpOnly refresh cookies (v2)
- [ ] *(deploy step)* Disable H2 console in prod; switch datasource to MySQL

---

## 11. Local Run Instructions

Prerequisites: **JDK 17**, **Maven 3.8+**, **Node.js ≥16** (MySQL optional — H2 runs out-of-the-box).

```bash
# --- Terminal 1: Backend (Spring Boot) ---
cd backend
mvn spring-boot:run
# → http://localhost:5000  (H2 console: /h2-console  user=sa pw= <blank>)

# --- Terminal 2: Frontend (Vite + React) ---
cd ../frontend
cp .env.example .env          # VITE_API_URL=http://localhost:5000/api
npm install
npm run dev
# → http://localhost:5173
```

Smoke test:
1. Register → redirected to dashboard
2. Create 3 tasks with varied status/priority
3. Verify stats counts in dashboard match tasks grid
4. Log out → back to login; attempting `/tasks` bounces to `/login`

To use **MySQL** instead of H2: edit `backend/src/main/resources/application.properties`
(comment the H2 block, uncomment the MySQL block) and create a database named `taskflow`.

---

## 12. Future / v2 Roadmap (out of scope for TASK 1)

1. **Workspaces / Teams** — `Workspace` entity, members with `role`, multi-user tasks
2. **Comments / activity log** — `Comment` table, task timeline
3. **Kanban view** — drag & drop columns by status
4. **Due-date reminders** — Spring `@Scheduled` worker + transactional email
5. **Pagination** — offset/limit or cursor on `GET /tasks` for >1,000 tasks
6. **Refresh tokens** — rotate short-lived access tokens + HttpOnly refresh cookie
7. **Rate limiting** — Bucket4j / Spring Cloud Gateway on `/auth/*`

---

## 13. Starter Repository URL (submission)

> Local starter path:
> `c:\Users\kpbal\OneDrive\Desktop\SWYNEX-Project-Architecture`
>
> Rename/copy the whole folder to `taskflow-app/` (as the repo root) when uploading.
>
> For submission, create a public repo on GitHub/GitLab from this folder and paste the URL here.
> This is a ready-to-push monorepo:
> - `frontend/` → React/Vite SPA (runnable after `npm install && npm run dev`)
> - `backend/` → Spring Boot JAR (runnable after `mvn spring-boot:run`), plus an embedded DB).
> - `docs/architecture.md` → this architecture document
