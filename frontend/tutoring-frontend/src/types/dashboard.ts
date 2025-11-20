export type Carrera = "ITS" | "ISC" | "IME" | "IMECA" | "IE" | "ICA";
export type SaturationState = "crítico" | "alerta" | "normal" | "bajo";

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
  desbalance_porcentaje?: number;
  desbalance_rango?: { min: number; max: number };
}

export interface DistribucionTutor {
  tutor_id: number;
  tutor_nombre: string;
  tutor_carrera: Carrera;
  alumnos_asignados: number;
  capacidad_max: number;
  carga_utilizada: number;
  porcentaje_saturacion?: number;
  estado?: SaturationState;
}

export interface CarreraDistribution {
  carrera: Carrera;
  nombreCompleto: string;
  totalTutores: number;
  totalAlumnos: number;
  promedioAlumnos: number;
  capacidadMaxima: number;
  estado: SaturationState;
}

export interface TutorSaturation {
  id: number;
  nombre: string;
  carrera: Carrera;
  alumnosActuales: number;
  capacidadMaxima: number;
  porcentajeSaturacion: number;
  estado: SaturationState;
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

