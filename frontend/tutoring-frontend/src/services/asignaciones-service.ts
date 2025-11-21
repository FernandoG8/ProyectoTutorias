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
  // Nuevos DTOs para nueva lógica (FASE 4C)
  AlumnoValidadoDTO,
  ExcelValidacionResponse,
  EjecutarAsignacionRequest,
  EjecucionAsignacionResponse,
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
 * - Ordenar alumnos por semestre (mayores primero)
 * - NO modifica la BD
 *
 * PRECONDICIONES:
 * - archivo debe ser Excel (.xlsx, .xls)
 * - semestreId debe ser número (NOT string)
 * - semestreId debe existir en BD
 *
 * RESPUESTA:
 * - status: "OK" (sin errores) | "ERROR" (validación fallida) | "WARNING" (advertencias)
 * - data: AlumnoValidadoDTO[] (alumnos validados y ORDENADOS si status=OK)
 * - errors: ExcelErrorDTO[] (detalles de errores si status=ERROR)
 *
 * EJEMPLO DE USO:
 * ```typescript
 * const response = await validateExcelFile(file, 5); // 5 = semestreId
 * if (response.status === "OK") {
 *   // Guardar response.data para usar en executeAssignment()
 *   setValidationData(response.data);
 * } else if (response.status === "ERROR") {
 *   // Mostrar tabla de errores: response.errors
 * }
 * ```
 */
export const validateExcelFile = async (
  archivo: File,
  semestreId: number,
): Promise<ExcelValidacionResponse> => {
  try {
    // Validar precondiciones localmente
    if (!archivo) {
      throw new Error("Archivo es requerido");
    }
    if (typeof semestreId !== "number" || semestreId <= 0) {
      throw new Error("Semestre debe ser un número válido");
    }
    if (!["application/vnd.ms-excel",
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"]
          .includes(archivo.type)) {
      throw new Error("El archivo debe ser Excel (.xlsx, .xls)");
    }

    const formData = new FormData();
    formData.append("archivo", archivo);
    formData.append("semestreId", semestreId.toString());

    const { data } = await api.post<ApiResponse<ExcelValidacionResponse>>(
      API_URLS.asignaciones.validarExcel,
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      },
    );

    // Validar respuesta
    if (!data || !data.data) {
      throw new Error("Respuesta del servidor inválida");
    }

    return data.data;
  } catch (error) {
    // El error ahora incluye la estructura estandarizada del backend
    // Los errores se manejan en el componente usando extractExcelErrors, extractFieldErrors, etc.
    throw error;
  }
};

/**
 * Ejecuta la asignación con datos previamente validados
 * POST /api/asignaciones/ejecutar
 *
 * PRECONDICIONES (CRÍTICAS):
 * - Los datos DEBEN venir de validateExcelFile() con status="OK"
 * - Los alumnos YA están validados (NO revalidar)
 * - Los alumnos YA están limpios (NO normalizar)
 * - Los alumnos YA están ORDENADOS por semestre (NO reordenar)
 * - semestreId debe ser número y coincidir con los alumnos
 * - La lista NO puede estar vacía
 *
 * RESPONSABILIDADES:
 * - Crear registros de Asignacion
 * - Actualizar cargas de tutores
 * - Registrar auditoría
 * - Best-effort error handling (continúa si hay errores parciales)
 *
 * RESPUESTA:
 * - status: "OK" (todos) | "PARTIAL" (algunos con error) | "ERROR" (ninguno)
 * - totalAlumnos: Total procesado
 * - alumnosAsignados: Exitosos
 * - alumnosConError: Fallidos
 * - duracionMs: Tiempo total
 * - erroresDetalle: Lista de errores (si status != "OK")
 *
 * EJEMPLO DE USO:
 * ```typescript
 * const response = await executeAssignment(semestreId, validationData);
 * if (response.status === "OK" || response.status === "PARTIAL") {
 *   showResults(response);
 * } else {
 *   error("Fallo la asignación: " + response.message);
 * }
 * ```
 */
export const executeAssignment = async (
  semestreId: number,
  alumnosValidados: AlumnoValidadoDTO[],
): Promise<EjecucionAsignacionResponse> => {
  try {
    // Validar precondiciones localmente
    if (typeof semestreId !== "number" || semestreId <= 0) {
      throw new Error("Semestre debe ser un número válido");
    }
    if (!Array.isArray(alumnosValidados) || alumnosValidados.length === 0) {
      throw new Error("Lista de alumnos no puede estar vacía");
    }

    // Validar que todos los alumnos tengan semestreId
    const allValid = alumnosValidados.every(
      (a) => a.semestreId && typeof a.semestreId === "number"
    );
    if (!allValid) {
      throw new Error("Algunos alumnos no tienen semestre válido");
    }

    const payload: EjecutarAsignacionRequest = {
      semestreId,
      alumnosValidados,                    // DIRECTO, SIN MODIFICAR
      simular: false,                      // Ejecutar de verdad
      reporteDetallado: false,             // Solo resumen
    };

    const { data } = await api.post<ApiResponse<EjecucionAsignacionResponse>>(
      API_URLS.asignaciones.ejecutar,
      payload,
    );

    // Validar respuesta
    if (!data || !data.data) {
      throw new Error("Respuesta del servidor inválida");
    }

    return data.data;
  } catch (error) {
    const mensaje = error instanceof Error ? error.message : "Error desconocido";
    throw new Error(`Error al ejecutar asignación: ${extractErrorMessage(error) || mensaje}`);
  }
};

/**
 * Ejecuta un cambio de tutor
 * POST /api/asignaciones/cambio-tutor
 *
 * Maneja el cambio de asignación de tutor para un alumno específico:
 * - Valida que el alumno exista y tenga tutor asignado
 * - Verifica disponibilidad del nuevo tutor
 * - Actualiza las cargas de ambos tutores
 * - Registra auditoría del cambio
 */
export const changeTutor = async (
  request: CambioTutorRequest
): Promise<CambioTutorResponse> => {
  try {
    const { data } = await api.post<ApiResponse<CambioTutorResponse>>(
      API_URLS.asignaciones.cambioTutor,
      request,
    );
    return data.data;
  } catch (error) {
    throw new Error(`Error al cambiar tutor: ${extractErrorMessage(error)}`);
  }
};
