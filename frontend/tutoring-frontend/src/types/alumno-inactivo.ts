import type { MotivoInactividad } from "./enums";
import type { AlumnoInactivoSummary } from "./shared";

export interface InactivoFilter {
  carrera?: string;
  semestre?: number;
  motivo?: MotivoInactividad;
}

export type AlumnoInactivo = AlumnoInactivoSummary;

export interface AsignarMotivoInput {
  alumnoInactivoId: number;
  motivo: MotivoInactividad;
  usuario: string;
}
