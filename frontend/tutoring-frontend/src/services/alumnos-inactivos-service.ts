import { api, extractErrorMessage } from "@/lib/api-client";
import { API_URLS } from "@/lib/api-urls";
import type {
  ApiResponse,
  AlumnoInactivo,
  AsignarMotivoInput,
  InactivoFilter,
} from "@/types";

/**
 * Construye parámetros de query para filtros
 */
const buildParams = (filters?: InactivoFilter) =>
  filters ? { ...filters } : undefined;

/**
 * Lista todos los alumnos inactivos con filtros opcionales
 * GET /api/alumnos-inactivos
 */
export const listInactiveStudents = async (
  filters?: InactivoFilter,
): Promise<AlumnoInactivo[]> => {
  try {
    const { data } = await api.get<ApiResponse<AlumnoInactivo[]>>(
      API_URLS.alumnosInactivos.root,
      {
        params: buildParams(filters),
      },
    );
    return data.data || [];
  } catch (error) {
    throw new Error(`Error al listar alumnos inactivos: ${extractErrorMessage(error)}`);
  }
};

/**
 * Lista alumnos inactivos pendientes de resolución
 * GET /api/alumnos-inactivos/pendientes
 */
export const listPendingInactiveStudents = async (
  filters?: InactivoFilter,
): Promise<AlumnoInactivo[]> => {
  try {
    const { data } = await api.get<ApiResponse<AlumnoInactivo[]>>(
      API_URLS.alumnosInactivos.pendientes,
      { params: buildParams(filters) },
    );
    return data.data || [];
  } catch (error) {
    throw new Error(
      `Error al listar alumnos inactivos pendientes: ${extractErrorMessage(error)}`,
    );
  }
};

/**
 * Asigna motivo de inactividad (método legacy)
 * PUT /api/alumnos-inactivos/{id}/motivo
 * @deprecated Usar resolverMotivoInactividad en su lugar
 */
export const assignInactiveReason = async (
  input: AsignarMotivoInput,
): Promise<AlumnoInactivo> => {
  try {
    const { data } = await api.put<ApiResponse<AlumnoInactivo>>(
      API_URLS.alumnosInactivos.motivo(input.alumnoInactivoId),
      null,
      {
        params: {
          motivo: input.motivo,
          usuario: input.usuario,
        },
      },
    );
    return data.data;
  } catch (error) {
    throw new Error(`Error al asignar motivo de inactividad: ${extractErrorMessage(error)}`);
  }
};

/**
 * Resuelve motivo de inactividad de forma inteligente
 * PATCH /api/alumnos-inactivos/{id}/resolver-motivo
 * Detecta automáticamente la disponibilidad de capacidad del tutor preservado
 */
export const resolverMotivoInactividad = async (
  alumnoInactivoId: number,
  motivo: string,
): Promise<any> => {
  try {
    const { data } = await api.patch<ApiResponse<any>>(
      API_URLS.alumnosInactivos.resolverMotivo(alumnoInactivoId),
      null,
      {
        params: {
          motivo,
        },
      },
    );
    return data.data;
  } catch (error) {
    throw new Error(`Error al resolver motivo de inactividad: ${extractErrorMessage(error)}`);
  }
};
