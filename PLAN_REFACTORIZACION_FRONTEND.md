# 🎯 PLAN DE REFACTORIZACIÓN FRONTEND - TUTOLINK

## Versión 2.0 - Alineación con Nueva API Estandarizada

---

## 📋 Índice

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Fase 1: Infraestructura Base](#fase-1-infraestructura-base)
3. [Fase 2: Servicios API](#fase-2-servicios-api)
4. [Fase 3: Componentes UI](#fase-3-componentes-ui)
5. [Fase 4: Módulo Mantenimiento](#fase-4-módulo-mantenimiento)
6. [Fase 5: Integración y Testing](#fase-5-integración-y-testing)
7. [Métricas de Éxito](#métricas-de-éxito)

---

## 📊 Resumen Ejecutivo

### Objetivo Principal
Refactorizar el frontend de **Tutolink** para:
- ✅ Consumir correctamente la nueva API con `ApiResponse<T>`
- ✅ Mejorar UX con diseño limpio y amigable
- ✅ Implementar manejo centralizado de errores
- ✅ Crear módulo de mantenimiento con DTOs tipificados

### Cambios Principales
- **Servicios API**: Refactorizar para desempaquetar `ApiResponse<T>`
- **Sistema de Notificaciones**: Global, contextual y accesible
- **Layout**: Moderno con scroll en contenido, sidebar profesional
- **Login**: Rediseño con branding Tutolink
- **Pantallas**: Alineación con nuevos DTOs del backend

### Duración Estimada
- Total: **3-4 semanas** (iterativo, puede hacerse por fases)
- Fase 1-2: **1 semana** (fundación)
- Fase 3: **1 semana** (UI principal)
- Fase 4-5: **1-2 semanas** (features)

### Riesgos Identificados
- ❌ Breaking changes en API
- ❌ Pérdida de datos en transición
- ✅ Mitigación: Testing exhaustivo, rollback plan

---

## 🏗️ FASE 1: INFRAESTRUCTURA BASE

### 1.1 Actualizar Sistema de Notificaciones

**Ubicación**: `src/lib/notifications-system.ts` (nuevo)

```typescript
// Toast/Alert unificado que maneja todos los casos
type NotificationType = 'success' | 'error' | 'warning' | 'info';

interface Notification {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  details?: string[];
  duration?: number; // ms, 0 = persistente
  action?: {
    label: string;
    onClick: () => void;
  };
}

// Sistema de notificaciones con Zustand
export const useNotifications = create((set) => ({
  notifications: [],
  add: (notification: Notification) => {...},
  remove: (id: string) => {...},
  clear: () => {...},
}));
```

**Implementación de Toasts**:
- Esquina inferior derecha
- Máximo 3 toasts visibles
- Auto-cierre en 5s (excepto errores)
- Animación smooth

### 1.2 Crear Tipo Global para ApiResponse

**Ubicación**: `src/types/api.ts` (actualizar)

```typescript
interface ApiResponse<T> {
  status: 'success' | 'error';
  data?: T;
  message?: string;
  error?: {
    code: string;
    message: string;
    details?: string[];
  };
}

// Enums para códigos de error conocidos
export enum ErrorCode {
  CAPACIDAD_EXCEDIDA = 'CAPACIDAD_EXCEDIDA',
  RECURSO_DUPLICADO = 'RECURSO_DUPLICADO',
  SIN_TUTOR_DISPONIBLE = 'SIN_TUTOR_DISPONIBLE',
  ALUMNO_INACTIVO = 'ALUMNO_INACTIVO',
  VALIDACION_FALLIDA = 'VALIDACION_FALLIDA',
  ENTIDAD_NO_ENCONTRADA = 'ENTIDAD_NO_ENCONTRADA',
  ERROR_INTERNO = 'ERROR_INTERNO',
}
```

### 1.3 Interceptor Axios Centralizado

**Ubicación**: `src/lib/api-client.ts` (actualizar)

```typescript
// Desempaquetar ApiResponse
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  withCredentials: true,
});

// Response interceptor: desempaquetar y validar
api.interceptors.response.use(
  (response) => {
    const apiResponse: ApiResponse<any> = response.data;

    if (apiResponse.status === 'error') {
      // Lanzar error tipificado
      const error = new ApiError(
        apiResponse.error?.code || 'ERROR_INTERNO',
        apiResponse.error?.message || 'Error desconocido',
        apiResponse.error?.details
      );
      return Promise.reject(error);
    }

    // Retornar solo los datos (desempaquetados)
    return apiResponse.data;
  },
  (error) => {
    // Manejo de errores HTTP
    if (error.response?.status === 401) {
      // Token expirado - redirigir a login
      authStore.logout();
    }
    return Promise.reject(error);
  }
);
```

### 1.4 Custom Hook para API Calls

**Ubicación**: `src/hooks/useApi.ts` (nuevo)

```typescript
export function useApi<T>(
  queryFn: () => Promise<T>,
  options?: UseMutationOptions<T, ApiError>
) {
  const { add } = useNotifications();

  return useMutation(queryFn, {
    onError: (error: ApiError) => {
      // Mostrar error contextual
      const message = getHumanMessage(error.code);
      add({
        type: 'error',
        title: 'Error',
        message: message,
        details: error.details,
        duration: 0, // Persistente
      });
    },
    onSuccess: (data, variables, context) => {
      options?.onSuccess?.(data, variables, context);
    },
    ...options,
  });
}
```

---

## 🔌 FASE 2: SERVICIOS API

### 2.1 Refactorizar Servicios Existentes

**Servicios a actualizar**:

| Servicio | Ubicación | DTOs Principales |
|----------|-----------|------------------|
| AlumnosService | `src/services/alumnos-service.ts` | AlumnoResponseDTO, PagedResponse |
| TutoresService | `src/services/tutors-service.ts` | TutorResponseDTO |
| SemestresService | `src/services/semestres-service.ts` | SemestreDTO |
| AsignacionesService | `src/services/asignaciones-service.ts` | AsignacionResponseDTO |
| MantenimientoService | `src/services/mantenimiento-service.ts` | **[NUEVO - VER 2.2]** |

**Patrón de actualización**:

```typescript
// ANTES (anti-patrón)
export async function listarAlumnos() {
  const response = await api.get('/api/alumnos');
  return response.data; // Retorna Array directo
}

// AHORA (patrón correcto)
export async function listarAlumnos(page = 1, limit = 20) {
  const response = await api.get('/api/alumnos', {
    params: { page, limit }
  });
  // El interceptor ya desempaquetó ApiResponse<PagedResponse<AlumnoDTO>>
  return response as PagedResponse<AlumnoResponseDTO>;
}
```

### 2.2 Crear MantenimientoService (NUEVO)

**Ubicación**: `src/services/mantenimiento-service.ts` (nuevo)

```typescript
// Consumir nuevos endpoints de mantenimiento
export const mantenimientoService = {
  // Diagnosticar sistema
  diagnosticar: () =>
    api.get('/api/mantenimiento/diagnostico')
      .then((data) => data as DiagnosticoResponse),

  // Sincronizar tutores
  sincronizar: () =>
    api.post('/api/mantenimiento/sincronizar-tutores', {})
      .then((data) => data as SincronizacionResponse),

  // Listar pendientes
  listarPendientes: () =>
    api.get('/api/mantenimiento/pendientes-resolucion')
      .then((data) => data as PendienteResolucionResponse[]),

  // Resolver masivamente
  resolverMasivamente: (resoluciones: Record<Long, string>) =>
    api.post('/api/mantenimiento/resolver-masivamente', resoluciones)
      .then((data) => data as ResolucionMasivaResponse),

  // Liberar cupos
  liberarCupos: () =>
    api.post('/api/mantenimiento/liberar-cupos-seguro', {})
      .then((data) => data as LiberacionCuposResponse),

  // Validar integridad
  validarIntegridad: () =>
    api.get('/api/mantenimiento/validar-integridad')
      .then((data) => data as IntegridadResponse),
};
```

### 2.3 Crear DTOs para Respuestas

**Ubicación**: `src/types/maintenance.ts` (nuevo)

```typescript
// Copiar interfaces desde backend y adaptarlas
export interface DiagnosticoResponse {
  totalTutores: number;
  totalAlumnos: number;
  alumnosActivos: number;
  alumnosInactivos: number;
  inactivosSinMotivo: number;
  asignacionesTotales: number;
  tutoresConDiscrepancia: number;
  anomalias: string[];
  estadoGeneral: 'SALUDABLE' | 'ADVERTENCIA' | 'CRÍTICO';
}

// ... más DTOs ...
```

---

## 🎨 FASE 3: COMPONENTES UI

### 3.1 Nuevo AppLayout

**Ubicación**: `src/components/layout/AppLayout.tsx` (nuevo)

```tsx
export function AppLayout({ children }: { children: ReactNode }) {
  return (
    <div className="flex h-screen bg-background text-foreground">
      {/* Sidebar fijo */}
      <Sidebar />

      {/* Main content con flex */}
      <div className="flex flex-1 flex-col min-h-0">
        {/* Header fijo */}
        <Topbar />

        {/* Main scrollable */}
        <main className="flex-1 min-h-0 overflow-y-auto">
          <div className="px-8 py-6 max-w-7xl mx-auto">
            {children}
          </div>
        </main>
      </div>

      {/* Toast notifications */}
      <NotificationContainer />
    </div>
  );
}
```

### 3.2 Nuevo Login Screen

**Ubicación**: `src/pages/LoginPage.tsx` (rediseñar)

```tsx
// Pantalla con:
// - Imagen ilustrativa en lado izquierdo
// - Formulario minimalista en lado derecho
// - Logo/nombre TUTOLINK prominente
// - Botón "Recordarme" opcional
// - Link "¿Olvidaste tu contraseña?" (futuro)

export function LoginPage() {
  return (
    <div className="flex h-screen bg-background">
      {/* Left: Illustration */}
      <div className="hidden lg:flex lg:w-1/2 items-center justify-center">
        <img
          src="/illustrations/login-hero.svg"
          alt="Tutolink"
          className="max-w-md"
        />
      </div>

      {/* Right: Form */}
      <div className="flex w-full lg:w-1/2 items-center justify-center">
        <form className="w-full max-w-sm space-y-6">
          {/* Logo + Nombre */}
          <div className="text-center">
            <h1 className="text-3xl font-bold text-primary">
              TUTOLINK
            </h1>
            <p className="text-muted-foreground">
              Gestión Integral de Titorías
            </p>
          </div>

          {/* Email + Password */}
          {/* Submit button */}
          {/* Remember me */}
        </form>
      </div>
    </div>
  );
}
```

### 3.3 Sidebar Mejorado

**Ubicación**: `src/components/layout/Sidebar.tsx` (actualizar)

Cambios:
- Iconos consistentes (h-5 w-5)
- Items uniformes (h-12 con padding)
- Estado activo con indicador visual
- Tooltips en hover
- Fondo consistente

```tsx
const navItems = [
  { icon: Users, label: 'Alumnos', path: '/alumnos', roles: ['ADMIN', 'USER'] },
  { icon: BookOpen, label: 'Tutores', path: '/tutores', roles: ['ADMIN'] },
  { icon: Calendar, label: 'Semestres', path: '/semestres', roles: ['ADMIN'] },
  { icon: Link2, label: 'Asignaciones', path: '/asignaciones', roles: ['ADMIN'] },
  { icon: FileText, label: 'Reportes', path: '/reportes', roles: ['ADMIN', 'USER'] },
  { icon: Wrench, label: 'Mantenimiento', path: '/mantenimiento', roles: ['ADMIN'] },
];
```

---

## 🔧 FASE 4: MÓDULO MANTENIMIENTO

### 4.1 Dashboard de Diagnóstico

**Ubicación**: `src/pages/MaintenancePage.tsx` (nuevo)

```tsx
// Estructura:
// 1. Estado General (Card con color: verde/amarillo/rojo)
// 2. Estadísticas (4 cards: Tutores, Alumnos, Asignaciones, Sin motivo)
// 3. Tabla de Anomalías (si las hay)
// 4. Botones de acción (Sincronizar, Liberar Cupos, etc.)
```

### 4.2 Pantalla de Pendientes

**Ubicación**: `src/pages/MantenancePages/PendientesPage.tsx` (nuevo)

- Tabla con alumnos inactivos sin motivo resuelto
- Columnas: Matricula, Nombre, Carrera, Tutor Preservado, Acción
- Bulk resolver con dropdown de motivos
- Indicadores visuales (tutor activo/inactivo, cupos disponibles)

### 4.3 Pantalla de Integridad

**Ubicación**: `src/pages/MantenancePages/IntegridadPage.tsx` (nuevo)

- Card de estado general
- Lista de anomalías agrupadas por tipo
- Badges de severidad (CRÍTICO/ADVERTENCIA/INFO)
- Botón para resolver cada anomalía

---

## 🧪 FASE 5: INTEGRACIÓN Y TESTING

### 5.1 Actualizar React Query

**Ubicación**: `src/lib/query-client.ts` (actualizar)

```typescript
// Configurar defaults para la nueva API
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 5 * 60 * 1000, // 5 min
      cacheTime: 10 * 60 * 1000, // 10 min
    },
    mutations: {
      retry: 0,
    },
  },
});
```

### 5.2 Custom Hooks para Mantenim

```typescript
// Hook para diagnosticar
export function useDiagnostico() {
  return useQuery(['diagnostico'],
    () => mantenimientoService.diagnosticar()
  );
}

// Hook para sincronizar
export function useSincronizar() {
  return useApi(
    () => mantenimientoService.sincronizar(),
    {
      onSuccess: () => {
        // Invalidar caché
        queryClient.invalidateQueries(['diagnostico']);
      }
    }
  );
}
```

### 5.3 Testing

**Que probar**:
- [ ] Desempaquetación correcta de `ApiResponse<T>`
- [ ] Manejo de errores de API
- [ ] Mostrar notificaciones contextuales
- [ ] Login con token JWT
- [ ] Endpoints de mantenimiento
- [ ] Paginación en tablas

**Stack**: Vitest + React Testing Library

```bash
# Ejecutar tests
npm run test

# Coverage
npm run test:coverage
```

---

## 📋 CHECKLIST DE IMPLEMENTACIÓN

### Infraestructura
- [ ] Sistema de notificaciones
- [ ] Tipos ApiResponse globales
- [ ] Interceptor Axios mejorado
- [ ] Custom hook useApi
- [ ] MantenimientoService

### UI
- [ ] Nuevo AppLayout
- [ ] Login rediseñado
- [ ] Sidebar mejorado
- [ ] NotificationContainer
- [ ] Toast animations

### Mantenimiento
- [ ] Dashboard diagnóstico
- [ ] Pantalla pendientes
- [ ] Pantalla integridad
- [ ] Botones de acción
- [ ] Confirmaciones modales

### Testing & Deployment
- [ ] Tests unitarios
- [ ] Tests de integración
- [ ] Build de producción
- [ ] Verificación visual (QA)
- [ ] Documentación de cambios

---

## 🎨 Guía de Estilos

### Colores Principales
- **Primary**: `#3B82F6` (Azul)
- **Success**: `#10B981` (Verde)
- **Warning**: `#F59E0B` (Amarillo)
- **Error**: `#EF4444` (Rojo)
- **Background**: `#F9FAFB` (Gris claro)
- **Foreground**: `#1F2937` (Gris oscuro)

### Tipografía
- **Heading**: 24px, 700 (bold)
- **Subheading**: 18px, 600 (semibold)
- **Body**: 14px, 400 (regular)
- **Small**: 12px, 400 (regular)

### Espaciado
- Use TailwindCSS: px-8, py-6, gap-4, etc.
- Consistencia: 4px base units

---

## 📦 Dependencias Nuevas (Opcionales)

```json
{
  "react-hot-toast": "^2.4.1",        // Alternativa a custom toasts
  "framer-motion": "^10.16.4",        // Animations
  "recharts": "^2.10.3",              // Charts para dashboard
  "clsx": "^2.0.0",                   // Class merging utility
}
```

---

## 🚀 Orden de Ejecución Recomendado

### Semana 1
1. Fase 1 - Infraestructura (Notifications, API Client, Hooks)
2. Fase 2 - Servicios API (Refactorizar existentes, crear MantenimientoService)

### Semana 2
3. Fase 3 - UI (AppLayout, Login, Sidebar)
4. Comenzar Fase 4 - Módulo Mantenimiento

### Semana 3
5. Terminar Fase 4 - Todas las pantallas de mantenimiento
6. Fase 5 - Testing e integración

### Semana 4
7. Testing exhaustivo
8. Fix de bugs
9. Deploy a producción

---

## ⚠️ Consideraciones Importantes

### Breaking Changes
- Las respuestas API han cambiado de estructura
- **Impacto**: Todos los servicios deben actualizarse
- **Mitigation**: Actualizar de uno en uno, testear cada paso

### Retrocompatibilidad
- Mantener endpoints antiguos temporalmente (si es posible)
- Feature flag para nueva API vs antigua
- Migración gradual del frontend

### Performance
- Implementar lazy loading en tablas grandes
- Usar React Query para caché inteligente
- Optimizar re-renders con memo/useMemo

---

## 📊 Métricas de Éxito

| Métrica | Target | Cómo Medir |
|---------|--------|-----------|
| Tasa de errores | < 2% | Monitoreo de logs |
| Load time promedio | < 2s | DevTools, Lighthouse |
| User satisfaction | > 4/5 | Encuesta |
| Test coverage | > 80% | Coverage report |
| API calls exitosos | > 99% | API monitoring |

---

## 📞 Contacto y Soporte

- **Dudas arquitectura**: Revisar docs/arquitectura/
- **Dudas de API**: Revisar docs/api/
- **Errores conocidos**: docs/CHANGELOG.md
- **Contribuciones**: docs/guias/02-contribucion.md

---

**Última actualización**: 2025-11-14
**Status**: Listo para ser usado con Cursor/IDE
**Aprobación requerida**: SÍ
