import { api } from "@/lib/api-client";
import { API_URLS } from "@/lib/api-urls";
import type {
  ApiResponse,
  Semestre,
  SemestreCreateInput,
  SemestreEstadisticas,
  SemestreActivoInfo,
} from "@/types";

export const listSemestres = async (): Promise<Semestre[]> => {
  const { data } = await api.get<ApiResponse<Semestre[]>>(API_URLS.semestres.root);
  return data.data;
};

export const getActiveSemester = async (): Promise<Semestre> => {
  const { data } = await api.get<ApiResponse<Semestre>>(API_URLS.semestres.activo);
  return data.data;
};

export const getSemestre = async (id: number): Promise<Semestre> => {
  const { data } = await api.get<ApiResponse<Semestre>>(API_URLS.semestres.detail(id));
  return data.data;
};

export const createSemestre = async (
  payload: SemestreCreateInput,
): Promise<Semestre> => {
  const { data } = await api.post<ApiResponse<Semestre>>(
    API_URLS.semestres.root,
    payload,
  );
  return data.data;
};

export const activateSemestre = async (id: number): Promise<string> => {
  const { data } = await api.put<ApiResponse<string>>(
    API_URLS.semestres.activar(id),
  );
  return data.data;
};

export const getSemestreEstadisticas = async (
  id: number,
): Promise<SemestreEstadisticas> => {
  const { data } = await api.get<ApiResponse<SemestreEstadisticas>>(
    API_URLS.semestres.estadisticas(id),
  );
  return data.data;
};

export const deleteSemestre = async (id: number): Promise<void> => {
  await api.delete<ApiResponse<void>>(API_URLS.semestres.detail(id));
};

