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
  semestreAcademico: string;
}

export interface CambioTutorResponse {
  alumnoId: number;
  tutorAnteriorId: number;
  tutorNuevoId: number;
  tutorAnteriorNombre: string;
  tutorNuevoNombre: string;
  fechaCambio: string;
  motivo: string;
  semestreAcademico: string;
}

export interface StartAssignmentProcessInput {
  archivo: File;
  semestreAcademico: string;
  usuario: string;
}

// ============================================================
// NUEVOS DTOs PARA NUEVA LÓGICA DE ASIGNACIONES (FASE 4C)
// ============================================================
// Estos DTOs corresponden exactamente a los del backend.
// IMPORTANTE: No modificar estruturas, deben coincidir perfectamente.

/**
 * Alumno validado, limpio y listo para asignación.
 * Salida del endpoint POST /validar-excel (si status="OK")
 * Entrada del endpoint POST /ejecutar
 */
export interface AlumnoValidadoDTO {
  alumnoId: number | null;              // ID en BD (null si nuevo ingreso)
  matricula: string;                     // PK desde Excel (validado)
  nombre: string;                        // Nombre (limpiado)
  carrera: string;                       // Código carrera (ICA, IE, etc)
  semestreId: number;                    // ID del semestre (NOT string)
  semestreCodigo: string;               // Display: "2025-2026-F1"
  semestreNumerico: number;             // 1-12 (para ordenamiento)
  ordenPriority: number;                // Mayor = asignar primero
  validado: boolean;                    // Siempre true
  tipoAsignacionPrevisto: string;       // "NUEVO_INGRESO" | "REINGRESO"
}

/**
 * Error encontrado durante validación de Excel
 */
export interface ExcelErrorDTO {
  filaExcel: number;                    // Número de fila en Excel
  campo: string;                        // Nombre del campo afectado
  valor: string;                        // Valor que causó el error
  descripcion: string;                  // Mensaje legible para usuario
  tipoError: string;                    // MATRICULA_INVALIDA, etc
  severidad: "ERROR" | "WARNING";       // Nivel de severidad
}

/**
 * Respuesta de validación de Excel
 * POST /api/asignaciones/validar-excel
 */
export interface ExcelValidacionResponse {
  status: "OK" | "ERROR" | "WARNING";   // Estado de validación
  message: string;                      // Mensaje descriptivo
  timestamp: string;                    // ISO DateTime
  totalFilas: number;                   // Filas procesadas
  totalValidas: number;                 // Filas sin errores
  totalErrores: number;                 // Filas con error
  porcentajeExito: number;              // (totalValidas/totalFilas)*100
  errors: ExcelErrorDTO[];              // Lista de errores (si hay)
  data: AlumnoValidadoDTO[] | null;    // Datos limpios (solo si OK)
  resumen?: string;                     // Metadata (opcional)
}

/**
 * Request para ejecutar asignación
 * POST /api/asignaciones/ejecutar
 */
export interface EjecutarAsignacionRequest {
  semestreId: number;                   // ID del semestre
  alumnosValidados: AlumnoValidadoDTO[]; // Del endpoint anterior (SIN modificar)
  simular?: boolean;                    // Optional: true = no guardar
  reporteDetallado?: boolean;           // Optional: true = reporte completo
}

/**
 * Error ocurrido durante ejecución
 */
export interface AsignacionErrorDTO {
  alumnoId: number | null;              // ID del alumno (null si error general)
  matricula?: string;                   // Matrícula del alumno
  descripcion: string;                  // Descripción del error
}

/**
 * Respuesta de ejecución de asignación
 * POST /api/asignaciones/ejecutar
 */
export interface EjecucionAsignacionResponse {
  status: "OK" | "PARTIAL" | "ERROR";   // Estado de ejecución
  message: string;                      // Mensaje descriptivo
  timestamp: string;                    // ISO DateTime
  totalAlumnos: number;                 // Total a procesar
  alumnosAsignados: number;             // Exitosos
  alumnosConError: number;              // Fallidos
  duracionMs: number;                   // Tiempo en milisegundos
  detalles: string;                     // Resumen textual
  porcentajeExito: number;              // (alumnosAsignados/totalAlumnos)*100
  erroresDetalle: AsignacionErrorDTO[]; // Lista de errores
}
