import { api, extractErrorMessage } from "@/lib/api-client";
import { API_URLS } from "@/lib/api-urls";
import type {
  ApiResponse,
  AssignmentAlertsApiResponse,
  AssignmentAlertsFilter,
  AssignmentAlertsResponse,
  AssignmentProcessRecord,
  AssignmentProcessSummary,
  CambioTutorRequest,
  CambioTutorResponse,
  EstadoProcesoResponse,
  IniciarProcesoResponse,
  StartAssignmentProcessInput,
} from "@/types";

/**
 * Mapea un registro de proceso del backend al formato del frontend
 */
const mapAssignmentProcess = (
  record: AssignmentProcessRecord,
): AssignmentProcessSummary => ({
  id: record.id,
  estado: record.estado,
  archivoOrigen: record.archivo_origen ?? null,
  usuarioEjecutor: record.usuario_ejecutor ?? null,
  fechaInicio: record.fecha_inicio,
  fechaFin: record.fecha_fin ?? null,
  totalProcesados: record.total_procesados,
  totalAsignados: record.total_asignados,
  totalErrores: record.total_errores,
  totalWarnings: record.total_warnings,
  tiempoMs: record.tiempo_ms ?? null,
});

/**
 * Mapea la respuesta de alertas del backend al formato del frontend
 */
const mapAlertsResponse = (
  payload: AssignmentAlertsApiResponse,
): AssignmentAlertsResponse => ({
  procesoId: payload.proceso_id,
  totalAlertas: payload.total_alertas,
  alertas: payload.alertas,
});

/**
 * Inicia un proceso de asignación masiva
 * POST /api/asignaciones/iniciar
 */
export const startAssignmentProcess = async (
  input: StartAssignmentProcessInput,
): Promise<IniciarProcesoResponse> => {
  try {
    const formData = new FormData();
    formData.append("archivo", input.archivo);
    formData.append("semestreAcademico", input.semestreAcademico);
    formData.append("usuario", input.usuario);

    const { data } = await api.post<ApiResponse<IniciarProcesoResponse>>(
      API_URLS.asignaciones.iniciar,
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      },
    );

    return data.data;
  } catch (error) {
    throw new Error(`Error al iniciar proceso de asignación: ${extractErrorMessage(error)}`);
  }
};

/**
 * Consulta el estado de un proceso de asignación
 * GET /api/asignaciones/proceso/{procesoId}
 */
export const getAssignmentProcessStatus = async (
  procesoId: number,
): Promise<EstadoProcesoResponse> => {
  try {
    const { data } = await api.get<ApiResponse<EstadoProcesoResponse>>(
      API_URLS.asignaciones.proceso(procesoId),
    );
    return data.data;
  } catch (error) {
    throw new Error(`Error al consultar estado del proceso: ${extractErrorMessage(error)}`);
  }
};

/**
 * Lista todos los procesos de asignación
 * GET /api/asignaciones/procesos
 */
export const listAssignmentProcesses = async (): Promise<AssignmentProcessSummary[]> => {
  try {
    const { data } = await api.get<ApiResponse<AssignmentProcessRecord[]>>(
      API_URLS.asignaciones.procesos,
    );
    return data.data.map(mapAssignmentProcess);
  } catch (error) {
    throw new Error(`Error al listar procesos: ${extractErrorMessage(error)}`);
  }
};

/**
 * Obtiene las alertas de un proceso de asignación
 * GET /api/asignaciones/proceso/{procesoId}/alertas
 */
export const listAssignmentAlerts = async (
  procesoId: number,
  params?: AssignmentAlertsFilter,
): Promise<AssignmentAlertsResponse> => {
  try {
    const { data } = await api.get<ApiResponse<AssignmentAlertsApiResponse>>(
      API_URLS.asignaciones.alertas(procesoId),
      { params },
    );
    return mapAlertsResponse(data.data);
  } catch (error) {
    throw new Error(`Error al obtener alertas del proceso: ${extractErrorMessage(error)}`);
  }
};

/**
 * Solicita un cambio manual de tutor para un alumno
 * POST /api/asignaciones/cambio-tutor
 */
export const requestTutorChange = async (
  payload: CambioTutorRequest,
): Promise<CambioTutorResponse> => {
  try {
    const { data } = await api.post<ApiResponse<CambioTutorResponse>>(
      API_URLS.asignaciones.cambioTutor,
      payload,
    );
    return data.data;
  } catch (error) {
    throw new Error(`Error al solicitar cambio de tutor: ${extractErrorMessage(error)}`);
  }
};

// ============================================================
// NUEVOS ENDPOINTS - WORKFLOW MEJORADO (FASE 4B)
// ============================================================
// Estos endpoints permiten:
// 1. Validar Excel ANTES de ejecutar (para mostrar errores)
// 2. Ejecutar SOLO con datos validados
// 3. Mejor UX y control de flujo

/**
 * Valida un archivo Excel sin ejecutar la asignación
 * POST /api/asignaciones/validar-excel
 *
 * RESPONSABILIDADES:
 * - Leer y parsear Excel
 * - Validar estructura y datos
 * - Reportar TODOS los errores
 * - Ordenar alumnos por semestre
 * - NO modifica la BD
 *
 * RESPUESTA:
 * - status: "OK" (sin errores) | "PARTIAL" (con errores) | "ERROR" (validación fallida)
 * - data: [] (alumnos validados si status=OK)
 * - errors: [] (detalles de errores si existen)
 */
export const validateExcelFile = async (
  archivo: File,
  semestreId: number,
): Promise<any> => {
  try {
    const formData = new FormData();
    formData.append("archivo", archivo);
    formData.append("semestreId", semestreId.toString());

    const { data } = await api.post<ApiResponse<any>>(
      API_URLS.asignaciones.validarExcel,
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      },
    );

    return data.data;
  } catch (error) {
    throw new Error(`Error al validar Excel: ${extractErrorMessage(error)}`);
  }
};

/**
 * Ejecuta la asignación con datos previamente validados
 * POST /api/asignaciones/ejecutar
 *
 * PRECONDICIONES:
 * - Los datos DEBEN venir de validateExcelFile() con status="OK"
 * - Los alumnos DEBEN estar ordenados por semestre
 * - semestreId DEBE coincidir
 *
 * RESPONSABILIDADES:
 * - Crear registros de Asignacion
 * - Actualizar cargas de tutores
 * - Registrar auditoría
 * - Best-effort error handling (continúa si hay errores parciales)
 *
 * RESPUESTA:
 * - status: "OK" (todos OK) | "PARTIAL" (algunos con error) | "ERROR" (ninguno)
 * - totalAlumnos: Total procesado
 * - alumnosAsignados: Exitosos
 * - alumnosConError: Fallidos
 * - duracionMs: Tiempo total
 * - erroresDetalle: [] (lista de errores si hay)
 */
export const executeAssignment = async (
  semestreId: number,
  alumnosValidados: any[],
): Promise<any> => {
  try {
    const payload = {
      semestreId,
      alumnosValidados,
    };

    const { data } = await api.post<ApiResponse<any>>(
      API_URLS.asignaciones.ejecutar,
      payload,
    );

    return data.data;
  } catch (error) {
    throw new Error(`Error al ejecutar asignación: ${extractErrorMessage(error)}`);
  }
};
