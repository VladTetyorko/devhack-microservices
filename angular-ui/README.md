# DevHack Angular UI

This is the Angular frontend for the DevHack application that consumes the REST APIs from the Java backend.

## Setup and Running

### Prerequisites

- Node.js (v14 or later)
- npm (v6 or later)
- Angular CLI (v16)

### Installation

```bash
npm install
```

### Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The application will automatically reload if you
change any of the source files.

```bash
ng serve
```

### Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory.

```bash
ng build
```

## Features

- User management (list, view, edit, register)
- Note management (view my notes, view notes by question)
- Vacancy response management (view my responses, view response details)
- Responsive design with Bootstrap 5

## Backend Integration

This Angular application is designed to work with the DevHack Java backend. The application uses a proxy configuration
to forward API requests to the backend server. Make sure the backend server is running on `http://localhost:8080`.

## Project Structure

- `src/app/models`: Data models that mirror the Java DTOs
- `src/app/services`: Services for API communication
- `src/app/components`: UI components organized by feature
    - `user`: User-related components
    - `note`: Note-related components
    - `vacancy-response`: Vacancy response components
    - `shared`: Shared components like navbar and footer
