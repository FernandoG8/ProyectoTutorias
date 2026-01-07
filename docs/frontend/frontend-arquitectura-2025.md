# Frontend – Guía de Arquitectura y Componentes (2025)

## 1. Tecnologías clave
- **Vite + React + TypeScript**: build rápido (Vite), tipado estricto (TS) y componentes funcionales (React).
- **TailwindCSS**: utilidades de estilo; se combinan con clases personalizadas y tokens de color (`src/constants/colors`).
- **React Query (@tanstack/react-query)**: control de datos remotos, caché y estados de carga/error.
- **React Hook Form + Zod**: formularios tipados y validación declarativa (ej. reportes, cambio de tutor).
- **Recharts**: gráficas (distribución de tutores, alumnos vs tutor).
- **Axios**: cliente HTTP centralizado en `src/lib/api-client.ts` con interceptores y helpers (`extractErrorMessage`, `extractExcelErrors`).

## 2. Estructura de carpetas (principal)
```
src/
├── components/           # UI y features
│   ├── common/           # Stepper, tablas, layout base
│   ├── dashboard/        # Gráficas y widgets del dashboard
│   ├── features/         # Flujos completos (asignaciones, cambio de tutor)
│   └── layout/           # Sidebar, contenedores principales
├── pages/                # Páginas enrutadas (ReportsPage, DashboardPage, AssignmentPage)
├── services/             # Llamadas a API por dominio (reports-service, tutors-service, asignaciones-service)
├── lib/                  # Utilidades (api-urls, api-client, export-utils)
├── store/                # Zonas de estado simple (ej. semestre activo)
└── types/                # Tipos compartidos (DTOs frontend)
```

## 3. Componentes destacados
- **Layout**
  - `Sidebar`: navegación principal y branding (Tutorías, Facultad de Ingeniería). Custom icon en `public/TutoLinkIcon.png`.
  - `DashboardLayout` / `MainLayout`: contenedores que ensamblan sidebar + contenido.
- **Stepper**
  - `Stepper` y `StepNavigation`: base para flujos guiados (asignaciones, cambio de tutor). Maneja estados activo/completado y navegación.
- **Asignaciones (flujo guiado)**
  - `StepperAsignaciones`: orquesta los pasos de cargar/validar Excel, revisar errores y ejecutar.
  - Servicios: `validateExcelFile` y `executeAssignment` (sin polling, con manejo de errores 400 y `excelErrors`).
  - `PasoValidacion`: muestra errores de Excel y bloquea avance si no es `OK`.
- **Cambio de Tutor**
  - `StepperCambioTutor`: 3 pasos (buscar alumno, elegir tutor, confirmar).
  - Pasos: `PasoBusquedaAlumno`, `PasoSeleccionTutor`, `PasoConfirmacionCambio`.
- **Reportes**
  - `ReportsPage`: descarga de reportes por tutor/carrera + ZIP masivo. Autocomplete de tutores con capacidad actual/máxima.
  - `SearchInput`: componente de sugerencias con badges y capacidades (carga/metros disponibles).
- **Dashboard**
  - `CarreraDistributionChart`: barras horizontales por carrera (capacidad, alumnos, estado).
  - `TutorAlumnosChart`: barras por tutor con paleta por carrera.
  - `StatCard`: KPIs pequeños (tutores, alumnos, promedio).

## 4. Datos y servicios
- **API URLs** centralizadas en `src/lib/api-urls.ts`.
- **Servicios por dominio**:
  - `reports-service.ts`: exportes y ZIPs (carreras y tutores).
  - `tutors-service.ts`: búsqueda avanzada; enriquece capacidad cuando el endpoint no la envía.
  - `asignaciones-service.ts`: validar/ejecutar asignaciones, cambio de tutor, procesos históricos.
  - `dashboard-service.ts`: KPIs y distribuciones.

## 5. Errores y manejo de estado
- Errores HTTP centralizados en `api-client` con mensajes amigables y redirección a login en 401.
- Excel: `extractExcelErrors` mapea la forma `{rowNumber, column, message}` a un formato legible en UI.
- React Query gestiona `isLoading`/`isFetching` para skeletons y deshabilitar botones.

## 6. Estilo y branding
- Tokens de color en `src/constants/colors`.
- Tipografía y espaciado via Tailwind.
- Favicon y branding: `TutoLinkIconWindow.png` (pestaña) y `TutoLinkIcon.png` (sidebar).

## 7. Build y ejecución
```bash
npm install
npm run dev       # Desarrollo
npm run build     # Producción (Vite + tsc)
```
Salida de build en `frontend/tutoring-frontend/dist/`.

## 8. Puntos de extensión
- Agregar más gráficas: reutilizar `Recharts` y la paleta `CARRERA_COLORS`.
- Nuevos flujos: componer con `Stepper` + `StepNavigation`.
- Nuevas llamadas: declarar en `api-urls.ts`, añadir servicio en `services/`, tipar DTO en `types/`.
