export interface Tutor {
  id: number;
  nombre: string;
  email: string;
  telefono?: string;
  especialidad?: string;
  totalAlumnos?: number;
}

export interface TutorStudent {
  id: number;
  nombre: string;
  codigo: string;
  carrera: string;
}

export interface AssignmentProcess {
  id: number;
  periodo: string;
  estado: string;
  fechaEjecucion: string;
  observaciones?: string;
  creador?: string;
}

export interface ProcessAssignmentsPayload {
  periodo: string;
  observaciones?: string;
}

export interface TutorPayload {
  nombre: string;
  email: string;
  telefono?: string;
  especialidad?: string;
}

export interface TutorReassignmentPayload {
  alumnoId: number;
  tutorOrigenId: number;
  tutorDestinoId: number;
  motivo: string;
}

export interface AuthResponse {
  token: string;
  role: "COORDINADOR_TUTORIAS" | "SECRETARIO_ACADEMICO";
}

export interface UserProfile {
  id: number;
  nombre: string;
  correo: string;
  rol: string;
}
