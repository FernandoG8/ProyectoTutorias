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
  try {
    const response = await api.get<Semestre[]>(API_URLS.semestres.root);
    // El backend devuelve directamente el array, no envuelto en ApiResponse
    const data = response.data;
    if (!data) {
      console.warn("Backend devolvió respuesta vacía para semestres");
      return [];
    }
    return Array.isArray(data) ? data : [];
  } catch (error: any) {
    console.error("Error en listSemestres:", error);
    // Si es un error de red o 500, relanzar
    if (error?.response?.status >= 500 || !error?.response) {
      throw new Error("Error de conexión con el servidor. Verifique que el backend esté ejecutándose.");
    }
    // Si es 404 o 400, puede que simplemente no hay semestres
    if (error?.response?.status === 404) {
      return [];
    }
    throw error;
  }
};

export const getActiveSemester = async (): Promise<Semestre> => {
  const response = await api.get<Semestre>(API_URLS.semestres.activo);
  // El backend devuelve directamente el SemestreDTO, no envuelto en ApiResponse
  return response.data;
};

export const getSemestre = async (id: number): Promise<Semestre> => {
  const response = await api.get<Semestre>(API_URLS.semestres.detail(id));
  return response.data;
};

export const createSemestre = async (
  payload: SemestreCreateInput,
): Promise<Semestre> => {
  const response = await api.post<Semestre>(
    API_URLS.semestres.root,
    payload,
  );
  return response.data;
};

export const activateSemestre = async (id: number): Promise<Semestre> => {
  // El backend usa POST y devuelve SemestreDTO directamente
  const response = await api.post<Semestre>(
    API_URLS.semestres.activar(id),
  );
  return response.data;
};

export const getSemestreEstadisticas = async (
  id: number,
): Promise<SemestreEstadisticas> => {
  const response = await api.get<SemestreEstadisticas>(
    API_URLS.semestres.estadisticas(id),
  );
  return response.data;
};

export const deleteSemestre = async (id: number): Promise<void> => {
  await api.delete<ApiResponse<void>>(API_URLS.semestres.detail(id));
};

