/**
 * URLs de la API centralizadas y tipadas.
 * Todas las URLs coinciden exactamente con los endpoints del backend.
 */
export const API_URLS = {
  auth: {
    login: "/auth/login",
    register: "/auth/register",
    refresh: "/auth/refresh",
    me: "/auth/me",
    logout: "/auth/logout",
  },
  alumnos: {
    root: "/api/alumnos",
    detail: (id: number | string) => `/api/alumnos/${id}`,
    search: "/api/alumnos/search",
    autocomplete: "/api/alumnos/autocomplete",
    byMatricula: (matricula: string) => `/api/alumnos/by-matricula/${matricula}`,
    semestreActual: "/api/alumnos/semestre-actual",
  },
  alumnosInactivos: {
    root: "/api/alumnos-inactivos",
    pendientes: "/api/alumnos-inactivos/pendientes",
    motivo: (id: number | string) => `/api/alumnos-inactivos/${id}/motivo`,
    resolverMotivo: (id: number | string) => `/api/alumnos-inactivos/${id}/resolver-motivo`,
  },
  tutores: {
    root: "/api/tutores",
    detail: (id: number | string) => `/api/tutores/${id}`,
    alumnos: (id: number | string) => `/api/tutores/${id}/alumnos`,
    search: "/api/tutores/search",
    autocomplete: "/api/tutores/autocomplete",
  },
  asignaciones: {
    // Workflow actual (mantener para compatibilidad)
    iniciar: "/api/asignaciones/iniciar",
    procesos: "/api/asignaciones/procesos",
    proceso: (id: number | string) => `/api/asignaciones/proceso/${id}`,
    alertas: (id: number | string) => `/api/asignaciones/proceso/${id}/alertas`,
    cambioTutor: "/api/asignaciones/cambio-tutor",
    // Nuevos endpoints para workflow mejorado (FASE 4B)
    validarExcel: "/api/asignaciones/validar-excel",
    ejecutar: "/api/asignaciones/ejecutar",
  },
  reportes: {
    porCarrera: "/api/reportes/por-carrera",
    tutores: (tutorId: number | string) =>
      `/api/reportes/tutores/${tutorId}/alumnos/exportar`,
    carreras: (codigo: string) => `/api/reportes/carreras/${codigo}/exportar`,
    carrerasTodos: "/api/reportes/carreras/exportar-todos",
    tutoresTodos: "/api/reportes/tutores/exportar-todos",
  },
  semestres: {
    root: "/api/semestres",
    activo: "/api/semestres/activo",
    ultimos: "/api/semestres/ultimos",
    activar: (id: number | string) => `/api/semestres/${id}/activar`,
    desactivar: (id: number | string) => `/api/semestres/${id}/desactivar`,
    estadisticas: (id: number | string) => `/api/semestres/${id}/estadisticas`,
    detail: (id: number | string) => `/api/semestres/${id}`,
    porCodigo: (codigo: string) => `/api/semestres/codigo/${codigo}`,
    existeCodigo: (codigo: string) => `/api/semestres/existe/codigo/${codigo}`,
  },
  dashboard: {
    estadisticas: "/api/dashboard/estadisticas",
    distribucionTutores: "/api/dashboard/distribucion-tutores",
    procesosRecientes: "/api/dashboard/procesos-recientes",
    semestreActivo: "/api/dashboard/semestre-activo",
    health: "/api/dashboard/health",
  },
  mantenimiento: {
    diagnostico: "/api/mantenimiento/diagnostico",
    sincronizarTutores: "/api/mantenimiento/sincronizar-tutores",
    pendientesResolucion: "/api/mantenimiento/pendientes-resolucion",
    resolverMasivamente: "/api/mantenimiento/resolver-masivamente",
    liberarCuposSeguro: "/api/mantenimiento/liberar-cupos-seguro",
    validarIntegridad: "/api/mantenimiento/validar-integridad",
  },
} as const;
