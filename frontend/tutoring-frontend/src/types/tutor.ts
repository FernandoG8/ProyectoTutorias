import type { AlumnoDetalle } from "./shared";

export interface TutorResponse {
  id: number;
  nombre: string;
  carrera: string;
  capacidadMax: number;
  cargaActual: number;
  capacidadDisponible: number;
  areaAtencion: string | null;
  letraEdificio: string | null;
  activo: boolean;
}

export interface TutorCreateInput {
  nombre: string;
  carrera: string;
  capacidadMax: number;
  areaAtencion?: string | null;
  letraEdificio?: string | null;
  activo?: boolean;
}

export interface TutorUpdateInput {
  nombre: string;
  carrera: string;
  capacidadMax: number;
  areaAtencion?: string | null;
  letraEdificio?: string | null;
  activo: boolean;
}

export interface TutorFilter {
  carrera?: string;
  disponibles?: boolean;
  sobrecargados?: boolean;
  activos?: boolean;
}

export interface TutorConAlumnos {
  id: number;
  nombre: string;
  cargaActual: number;
  capacidadMax: number;
  areaAtencion: string | null;
  letraEdificio: string | null;
  alumnos: AlumnoDetalle[];
}
