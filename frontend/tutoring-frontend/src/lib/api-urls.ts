export const API_URLS = {
  auth: {
    login: "/api/auth/login",
    me: "/api/auth/me",
  },
  tutores: {
    root: "/api/tutores",
    detalle: (id: number | string) => `/api/tutores/${id}`,
    alumnos: (id: number | string) => `/api/tutores/${id}/alumnos`,
    reasignar: "/api/tutores/reasignar",
  },
  asignacion: {
    procesar: "/api/asignacion/procesar",
    procesos: "/api/asignacion/procesos",
  },
  reportes: {
    tutores: (tutorId: number | string) =>
      `/api/reportes/tutores/${tutorId}/alumnos/exportar`,
    carreras: (codigo: string) =>
      `/api/reportes/carreras/${codigo}/exportar`,
  },
};
