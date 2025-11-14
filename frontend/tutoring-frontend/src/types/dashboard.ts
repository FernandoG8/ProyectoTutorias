export interface DashboardEstadisticas {
  semestre: string;
  semestre_id: number;
  total_alumnos: number;
  alumnos_con_tutor: number;
  alumnos_sin_tutor: number;
  total_tutores: number;
  total_asignaciones: number;
  promedio_alumnos_por_tutor: number;
  porcentaje_cobertura: number;
}

export interface DistribucionTutor {
  tutor_id: number;
  tutor_nombre: string;
  tutor_carrera: string;
  alumnos_asignados: number;
  capacidad_max: number;
  carga_utilizada: number;
}

export interface ProcesoReciente {
  id: number;
  estado: string;
  archivo: string;
  usuario: string;
  fecha_inicio: string;
  fecha_fin: string | null;
  total_procesados: number;
  total_asignados: number;
  total_errores: number;
  tiempo_segundos: number | null;
}

export interface DashboardHealth {
  status: string;
  database: string;
  semestre_activo: boolean;
  version: string;
}

