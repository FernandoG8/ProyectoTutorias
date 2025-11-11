import type { EstadoProceso, SeveridadAlerta } from "./enums";
import type { Alerta } from "./shared";

export interface Progreso {
  porcentaje: number;
  alumnosProcesados: number;
  alumnosAsignados: number;
  errores: number;
  warnings: number;
}

export interface IniciarProcesoResponse {
  procesoId?: number;
  estado?: EstadoProceso;
  mensaje?: string;
  timestamp: string;
}

export interface EstadoProcesoResponse {
  procesoId: number;
  estado: EstadoProceso;
  progreso: Progreso;
  fechaInicio: string;
  fechaFin: string | null;
  tiempoTranscurridoMs: number | null;
}

export interface AssignmentProcessRecord {
  id: number;
  estado: EstadoProceso;
  archivo_origen: string | null;
  usuario_ejecutor: string | null;
  fecha_inicio: string;
  fecha_fin: string | null;
  total_procesados: number;
  total_asignados: number;
  total_errores: number;
  total_warnings: number;
  tiempo_ms: number | null;
}

export interface AssignmentProcessSummary {
  id: number;
  estado: EstadoProceso;
  archivoOrigen: string | null;
  usuarioEjecutor: string | null;
  fechaInicio: string;
  fechaFin: string | null;
  totalProcesados: number;
  totalAsignados: number;
  totalErrores: number;
  totalWarnings: number;
  tiempoMs: number | null;
}

export interface AssignmentAlertsApiResponse {
  proceso_id: number;
  total_alertas: number;
  alertas: Alerta[];
}

export interface AssignmentAlertsResponse {
  procesoId: number;
  totalAlertas: number;
  alertas: Alerta[];
}

export interface AssignmentAlertsFilter {
  severidad?: SeveridadAlerta;
  resuelta?: boolean;
}

export interface CambioTutorRequest {
  alumnoId: number;
  tutorOrigenId: number;
  tutorDestinoId: number;
  motivo: string;
  usuario: string;
}

export interface CambioTutorResponse {
  alumnoId: number;
  tutorAnteriorId: number;
  tutorNuevoId: number;
  tutorAnteriorNombre: string;
  tutorNuevoNombre: string;
  fechaCambio: string;
  motivo: string;
}

export interface StartAssignmentProcessInput {
  archivo: File;
  semestreAcademico: string;
  usuario: string;
}
