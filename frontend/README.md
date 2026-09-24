# SWYNEX Frontend

A React + Vite frontend for the SWYNEX project architecture task management application.

## Tech Stack

- **React 18** - UI library
- **Vite** - Build tool and dev server
- **React Router 6** - Client-side routing
- **Axios** - HTTP client for API communication

## Project Structure

```
frontend/
├── src/
│   ├── components/
│   │   ├── Navbar.jsx      - Navigation bar with auth state
│   │   ├── TaskCard.jsx    - Individual task display card
│   │   └── TaskForm.jsx    - Form for creating/editing tasks
│   ├── pages/
│   │   ├── Login.jsx       - Login page
│   │   ├── Register.jsx    - Registration page
│   │   ├── Dashboard.jsx   - User dashboard with stats
│   │   └── Tasks.jsx       - Task management page
│   ├── services/
│   │   └── api.js          - Axios API client with interceptors
│   ├── App.jsx             - Main app component with routes
│   ├── main.jsx            - React entry point
│   └── index.css           - Global styles
├── package.json
└── README.md
```

## Getting Started

### Prerequisites

- Node.js >= 16.0.0
- npm or yarn

### Installation

```bash
cd frontend
npm install
```

### Development

```bash
npm run dev
```

The dev server will start on `http://localhost:5173`.

### Environment Variables

Create a `.env` file in the `frontend` directory:

```
VITE_API_URL=http://localhost:5000/api
```

### Production Build

```bash
npm run build
```

The optimized build will be in the `dist` directory.

### Preview Production Build

```bash
npm run preview
```

## Features

### Authentication
- User registration (name, email, password)
- User login (email, password)
- JWT token-based authentication
- Protected routes for authenticated users
- Public routes redirect to dashboard if already logged in
- Auto-logout on 401 (token expiry)

### Dashboard
- Welcome message with user name
- Task statistics (total, pending, in-progress, completed)
- Recent tasks list

### Task Management
- Create tasks with title, description, status, priority, due date
- Edit existing tasks
- Delete tasks with confirmation
- Filter tasks by status (all, pending, in-progress, completed)
- Search tasks by title or description
- Visual indicators for priority and status

## API Endpoints

The frontend expects the following backend API endpoints at `VITE_API_URL`:

### Auth
- `POST /api/auth/register` - Register user
- `POST /api/auth/login` - Login user
- `GET /api/auth/profile` - Get user profile

### Tasks
- `GET /api/tasks` - Get all tasks
- `GET /api/tasks/:id` - Get task by ID
- `POST /api/tasks` - Create task
- `PUT /api/tasks/:id` - Update task
- `DELETE /api/tasks/:id` - Delete task

### Response Format

```json
{
  "token": "jwt-token",
  "user": {
    "_id": "user-id",
    "name": "User Name",
    "email": "user@example.com"
  },
  "tasks": [...],
  "message": "Response message"
}
```
