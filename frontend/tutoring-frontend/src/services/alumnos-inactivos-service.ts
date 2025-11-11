import { api } from "@/lib/api-client";
import { API_URLS } from "@/lib/api-urls";
import type {
  ApiResponse,
  AlumnoInactivo,
  AsignarMotivoInput,
  InactivoFilter,
} from "@/types";

const buildParams = (filters?: InactivoFilter) =>
  filters ? { ...filters } : undefined;

export const listInactiveStudents = async (
  filters?: InactivoFilter,
): Promise<AlumnoInactivo[]> => {
  const { data } = await api.get<ApiResponse<AlumnoInactivo[]>>(API_URLS.alumnosInactivos.root, {
    params: buildParams(filters),
  });
  return data.data;
};

export const listPendingInactiveStudents = async (
  filters?: InactivoFilter,
): Promise<AlumnoInactivo[]> => {
  const { data } = await api.get<ApiResponse<AlumnoInactivo[]>>(
    API_URLS.alumnosInactivos.pendientes,
    { params: buildParams(filters) },
  );
  return data.data;
};

export const assignInactiveReason = async (
  input: AsignarMotivoInput,
): Promise<AlumnoInactivo> => {
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
};
