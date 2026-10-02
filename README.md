# Sistema de Gestión de Tutorías Académicas

Plataforma para administrar el programa de tutorías de una facultad: alumnos, tutores, semestres
y la **asignación automática de tutores**, con reportes exportables a PDF, Excel y Google Drive.

Proyecto universitario, 11/2025 - 01/2026.

## Stack

| Capa | Tecnologías |
|---|---|
| Backend | Java 21, Spring Boot 3.3, Spring Security, Spring Data JPA, MapStruct, Lombok |
| Autenticación | JWT (access y refresh token), OAuth 2.0 con Google |
| Base de datos | MySQL 8 (H2 en pruebas) |
| Reportes | OpenPDF (PDF), Apache POI (Excel), Google Drive API |
| Frontend | React 18, TypeScript, Vite, TanStack Query, React Router, Tailwind CSS, Recharts, Zod |
| Calidad | JUnit 5, Mockito, JaCoCo, OpenAPI (springdoc) |
| Infraestructura | Docker, Docker Compose, Maven Wrapper |

## Funcionalidades

- **Alumnos y tutores:** CRUD, búsqueda, inactivación y reingreso, y cambio de tutor con auditoría de cada cambio.
- **Carga masiva desde Excel:** validación por archivo con reporte de errores antes de importar.
- **Asignación automática de tutores** por semestre, con seguimiento del progreso del proceso.
- **Reportes** por carrera y por tutor en PDF o Excel; exportación masiva a Google Drive en segundo plano tras vincular la cuenta de Google con OAuth 2.0.
- **Dashboard** con indicadores del semestre y **mantenimiento**: diagnóstico del sistema y verificación de integridad de datos.

## Arquitectura

El backend se organiza por capas (`domain`, `application`, `infrastructure`):

```
backend/src/main/java/com/universidad/tutorias/
├── domain/           entidades, repositorios y excepciones de dominio
├── application/      servicios de aplicación y DTOs
└── infrastructure/   controladores REST, seguridad, configuración e integración con Google
```

| Ruta base | Recurso |
|---|---|
| `/auth`, `/auth/google` | Inicio de sesión, refresh token y vinculación con Google |
| `/api/alumnos`, `/api/alumnos-inactivos` | Alumnos |
| `/api/tutores` | Tutores |
| `/api/semestres` | Semestres |
| `/api/asignaciones`, `/api/cleanup` | Asignación de tutores y limpieza de asignaciones |
| `/api/reportes`, `/api/reportes/drive` | Reportes y exportación a Drive |
| `/api/dashboard`, `/api/mantenimiento` | Indicadores y mantenimiento |

Con el backend en marcha, la documentación interactiva de la API está en `http://localhost:8080/swagger-ui.html`.

## Cómo ejecutarlo

### Con Docker (backend y MySQL)

```bash
cd backend
docker compose up -d     # MySQL 8 + backend en http://localhost:8080
```

Las credenciales de `docker-compose.yml` son solo para desarrollo local.

### Backend en local

Requisitos: Java 21 y una base MySQL.

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Las credenciales (base de datos, `JWT_SECRET`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`) se leen de
variables de entorno; ver `backend/.env.prod.example`.

### Frontend

Requisitos: Node.js 20 o superior.

```bash
cd frontend/tutoring-frontend
cp .env.example .env     # VITE_API_BASE_URL apunta al backend
npm install
npm run dev
```

### Pruebas y cobertura

```bash
cd backend
./mvnw test              # el reporte de JaCoCo queda en target/site/jacoco
```

## Documentación

Toda la documentación vive en [`docs/`](./docs):

- [`docs/api`](./docs/api) y [`docs/arquitectura`](./docs/arquitectura): endpoints y mapa de la arquitectura
- [`docs/asignaciones`](./docs/asignaciones): diseño del módulo de asignación de tutores
- [`docs/frontend`](./docs/frontend): componentes y servicios del frontend
- [`docs/despliegue`](./docs/despliegue): Docker, Railway y Render + Vercel + Supabase
- [`docs/pruebas`](./docs/pruebas): reportes de cobertura con JaCoCo
- [`docs/historial`](./docs/historial): bitácora de fases, auditorías y decisiones del desarrollo
