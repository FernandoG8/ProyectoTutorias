export interface Semestre {
  id: number;
  codigo: string;
  nombre: string;
  fechaInicio: string;
  fechaFin: string;
  activo: boolean;
  estaVigente: boolean;
  totalAsignaciones?: number;
  fechaCreacion?: string;
}

export interface SemestreCreateInput {
  codigo: string;
  nombre: string;
  fechaInicio: string;
  fechaFin: string;
}

export interface SemestreEstadisticas {
  semestreId: number;
  codigo: string;
  nombre: string;
  totalAsignaciones: number;
  alumnosActivos: number;
  alumnosInactivos: number;
  totalTutoresActivos: number;
  tutoresConAsignaciones: number;
  tutoresConCapacidadLlena: number;
  tutoresDisponibles: number;
  promedioAlumnosPorTutor: number;
  distribucionPorCarrera: Record<string, number>;
}

export interface SemestreActivoInfo {
  id: number;
  codigo: string;
  nombre: string;
  estaVigente: boolean;
  diasRestantes?: number;
  progreso?: number;
}

