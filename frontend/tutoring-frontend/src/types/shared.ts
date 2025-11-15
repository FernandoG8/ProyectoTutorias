import type { EstadoAlumno, MotivoInactividad, TipoAlerta, SeveridadAlerta } from "./enums";

export interface TutorSimple {
  id: number;
  nombre: string;
  carrera: string;
}

export interface AlumnoSimple {
  id: number;
  matricula: string;
  nombre: string;
  carrera: string;
}

export interface AlumnoDetalle {
  id: number;
  matricula: string;
  nombre: string;
  semestre: number;
  fechaAsignacion: string;
}

export interface Alerta {
  id: number;
  tipo: TipoAlerta;
  severidad: SeveridadAlerta;
  descripcion: string;
  alumno: AlumnoSimple | null;
  tutor: TutorSimple | null;
  resuelta: boolean;
  fechaCreacion: string;
}

export interface AlumnoInactivoSummary {
  id: number;
  alumnoId: number;
  nombre: string;
  matricula: string;
  carrera: string;
  semestre: number;
  motivo: MotivoInactividad;
  fechaDeteccion: string;
  cupoLiberado: boolean;
}

export interface AlumnoSummary {
  id: number;
  matricula: string;
  nombre: string;
  carrera: string;
  semestre: number;
  estado: EstadoAlumno;
  tutor: TutorSimple | null;
  cambiosTutor: number;
}
