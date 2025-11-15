import { api } from "@/lib/api-client";
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

const mapAlertsResponse = (
  payload: AssignmentAlertsApiResponse,
): AssignmentAlertsResponse => ({
  procesoId: payload.proceso_id,
  totalAlertas: payload.total_alertas,
  alertas: payload.alertas,
});

export const startAssignmentProcess = async (
  input: StartAssignmentProcessInput,
): Promise<IniciarProcesoResponse> => {
  const formData = new FormData();
  formData.append("archivo", input.archivo);
  formData.append("semestreAcademico", input.semestreAcademico);
  formData.append("usuario", input.usuario);

  const { data } = await api.post<ApiResponse<IniciarProcesoResponse>>(
    API_URLS.asignaciones.iniciar,
    formData,
  );

  return data.data;
};

export const getAssignmentProcessStatus = async (
  procesoId: number,
): Promise<EstadoProcesoResponse> => {
  const { data } = await api.get<ApiResponse<EstadoProcesoResponse>>(
    API_URLS.asignaciones.proceso(procesoId),
  );
  return data.data;
};

export const listAssignmentProcesses = async (): Promise<AssignmentProcessSummary[]> => {
  const { data } = await api.get<ApiResponse<AssignmentProcessRecord[]>>(API_URLS.asignaciones.procesos);
  return data.data.map(mapAssignmentProcess);
};

export const listAssignmentAlerts = async (
  procesoId: number,
  params?: AssignmentAlertsFilter,
): Promise<AssignmentAlertsResponse> => {
  const { data } = await api.get<ApiResponse<AssignmentAlertsApiResponse>>(
    API_URLS.asignaciones.alertas(procesoId),
    { params },
  );
  return mapAlertsResponse(data.data);
};

export const requestTutorChange = async (
  payload: CambioTutorRequest,
): Promise<CambioTutorResponse> => {
  const { data } = await api.post<ApiResponse<CambioTutorResponse>>(
    API_URLS.asignaciones.cambioTutor,
    payload,
  );
  return data.data;
};
