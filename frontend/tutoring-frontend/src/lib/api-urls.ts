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
  },
  alumnosInactivos: {
    root: "/api/alumnos-inactivos",
    pendientes: "/api/alumnos-inactivos/pendientes",
    motivo: (id: number | string) => `/api/alumnos-inactivos/${id}/motivo`,
  },
  tutores: {
    root: "/api/tutores",
    detail: (id: number | string) => `/api/tutores/${id}`,
    alumnos: (id: number | string) => `/api/tutores/${id}/alumnos`,
  },
  asignaciones: {
    iniciar: "/api/asignaciones/iniciar",
    procesos: "/api/asignaciones/procesos",
    proceso: (id: number | string) => `/api/asignaciones/proceso/${id}`,
    alertas: (id: number | string) => `/api/asignaciones/proceso/${id}/alertas`,
    cambioTutor: "/api/asignaciones/cambio-tutor",
  },
  reportes: {
    porCarrera: "/api/reportes/por-carrera",
    tutores: (tutorId: number | string) =>
      `/api/reportes/tutores/${tutorId}/alumnos/exportar`,
    carreras: (codigo: string) => `/api/reportes/carreras/${codigo}/exportar`,
    carrerasTodos: "/api/reportes/carreras/exportar-todos",
    tutorPdf: (tutorId: number | string) => `/api/reportes/tutor/${tutorId}/pdf`,
    carreraExcel: (codigo: string) => `/api/reportes/carrera/${codigo}/excel`,
    listar: "/api/reportes",
  },
  semestres: {
    root: "/api/semestres",
    activo: "/api/semestres/activo",
    activar: (id: number | string) => `/api/semestres/${id}/activar`,
    estadisticas: (id: number | string) => `/api/semestres/${id}/estadisticas`,
    detail: (id: number | string) => `/api/semestres/${id}`,
  },
  dashboard: {
    estadisticas: "/api/dashboard/estadisticas",
    distribucionTutores: "/api/dashboard/distribucion-tutores",
    procesosRecientes: "/api/dashboard/procesos-recientes",
    semestreActivo: "/api/dashboard/semestre-activo",
    health: "/api/dashboard/health",
  },
};
