import type { EstadoAlumno } from "./enums";
import type { PagedResponse } from "./api";
import type { TutorSimple, AlumnoSummary } from "./shared";

export interface AlumnoCreateInput {
  matricula: string;
  nombre: string;
  carrera: string;
  semestre: number;
  estado?: EstadoAlumno;
  tutorId?: number | null;
}

export interface AlumnoUpdateInput {
  nombre: string;
  carrera: string;
  semestre: number;
  estado: EstadoAlumno;
  tutorId?: number | null;
}

export interface AlumnoPatchInput {
  nombre?: string;
  carrera?: string;
  semestre?: number;
  estado?: EstadoAlumno;
  tutorId?: number | null;
}

export type AlumnoResponse = AlumnoSummary;

export type AlumnoPagedResponse = PagedResponse<AlumnoResponse>;

export interface AlumnoWithTutor extends AlumnoResponse {
  tutor: TutorSimple | null;
}
