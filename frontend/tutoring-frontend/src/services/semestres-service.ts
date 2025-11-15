import axios from "axios";
import { api, extractErrorMessage } from "@/lib/api-client";
import { API_URLS } from "@/lib/api-urls";
import type {
  ApiResponse,
  Semestre,
  SemestreCreateInput,
  SemestreEstadisticas,
} from "@/types";

/**
 * Lista todos los semestres ordenados por fecha de inicio descendente
 * GET /api/semestres
 */
export const listSemestres = async (): Promise<Semestre[]> => {
  try {
    const { data } = await api.get<ApiResponse<Semestre[]>>(API_URLS.semestres.root);
    return data.data || [];
  } catch (error) {
    throw new Error(`Error al listar semestres: ${extractErrorMessage(error)}`);
  }
};

/**
 * Obtiene los últimos N semestres
 * GET /api/semestres/ultimos?cantidad=N
 */
export const listUltimosSemestres = async (cantidad: number = 5): Promise<Semestre[]> => {
  try {
    const { data } = await api.get<ApiResponse<Semestre[]>>(API_URLS.semestres.ultimos, {
      params: { cantidad },
    });
    return data.data || [];
  } catch (error) {
    throw new Error(`Error al listar últimos semestres: ${extractErrorMessage(error)}`);
  }
};

/**
 * Obtiene el semestre activo actual
 * GET /api/semestres/activo
 */
export const getActiveSemester = async (): Promise<Semestre | null> => {
  try {
    const { data } = await api.get<ApiResponse<Semestre>>(API_URLS.semestres.activo);
    return data.data;
  } catch (error) {
    // Si no hay semestre activo, el backend devuelve 404
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      return null;
    }
    throw new Error(`Error al obtener semestre activo: ${extractErrorMessage(error)}`);
  }
};

/**
 * Obtiene un semestre por ID
 * GET /api/semestres/{id}
 */
export const getSemestre = async (id: number): Promise<Semestre> => {
  try {
    const { data } = await api.get<ApiResponse<Semestre>>(API_URLS.semestres.detail(id));
    return data.data;
  } catch (error) {
    throw new Error(`Error al obtener semestre: ${extractErrorMessage(error)}`);
  }
};

/**
 * Obtiene un semestre por código
 * GET /api/semestres/codigo/{codigo}
 */
export const getSemestreByCodigo = async (codigo: string): Promise<Semestre> => {
  try {
    const { data } = await api.get<ApiResponse<Semestre>>(API_URLS.semestres.porCodigo(codigo));
    return data.data;
  } catch (error) {
    throw new Error(`Error al obtener semestre por código: ${extractErrorMessage(error)}`);
  }
};

/**
 * Verifica si existe un semestre con el código proporcionado
 * GET /api/semestres/existe/codigo/{codigo}
 */
export const verificarCodigoExistente = async (codigo: string): Promise<boolean> => {
  try {
    const { data } = await api.get<ApiResponse<{ existe: boolean }>>(
      API_URLS.semestres.existeCodigo(codigo),
    );
    return data.data.existe;
  } catch (error) {
    throw new Error(`Error al verificar código: ${extractErrorMessage(error)}`);
  }
};

/**
 * Crea un nuevo semestre
 * POST /api/semestres
 */
export const createSemestre = async (
  payload: SemestreCreateInput,
): Promise<Semestre> => {
  try {
    const { data } = await api.post<ApiResponse<Semestre>>(
      API_URLS.semestres.root,
      payload,
    );
    return data.data;
  } catch (error) {
    throw new Error(`Error al crear semestre: ${extractErrorMessage(error)}`);
  }
};

/**
 * Activa un semestre (desactiva todos los demás)
 * POST /api/semestres/{id}/activar
 */
export const activateSemestre = async (id: number): Promise<Semestre> => {
  try {
    const { data } = await api.post<ApiResponse<Semestre>>(
      API_URLS.semestres.activar(id),
    );
    return data.data;
  } catch (error) {
    throw new Error(`Error al activar semestre: ${extractErrorMessage(error)}`);
  }
};

/**
 * Desactiva un semestre
 * POST /api/semestres/{id}/desactivar
 */
export const deactivateSemestre = async (id: number): Promise<Semestre> => {
  try {
    const { data } = await api.post<ApiResponse<Semestre>>(
      API_URLS.semestres.desactivar(id),
    );
    return data.data;
  } catch (error) {
    throw new Error(`Error al desactivar semestre: ${extractErrorMessage(error)}`);
  }
};

/**
 * Actualiza un semestre existente
 * PUT /api/semestres/{id}
 */
export const updateSemestre = async (
  id: number,
  payload: Partial<SemestreCreateInput>,
): Promise<Semestre> => {
  try {
    const { data } = await api.put<ApiResponse<Semestre>>(
      API_URLS.semestres.detail(id),
      payload,
    );
    return data.data;
  } catch (error) {
    throw new Error(`Error al actualizar semestre: ${extractErrorMessage(error)}`);
  }
};

/**
 * Obtiene estadísticas detalladas del semestre
 * GET /api/semestres/{id}/estadisticas
 */
export const getSemestreEstadisticas = async (
  id: number,
): Promise<SemestreEstadisticas> => {
  try {
    const { data } = await api.get<ApiResponse<SemestreEstadisticas>>(
      API_URLS.semestres.estadisticas(id),
    );
    return data.data;
  } catch (error) {
    throw new Error(`Error al obtener estadísticas del semestre: ${extractErrorMessage(error)}`);
  }
};

/**
 * Elimina un semestre (con validaciones previas)
 * DELETE /api/semestres/{id}
 */
export const deleteSemestre = async (id: number): Promise<void> => {
  try {
    await api.delete<ApiResponse<void>>(API_URLS.semestres.detail(id));
  } catch (error) {
    throw new Error(`Error al eliminar semestre: ${extractErrorMessage(error)}`);
  }
};

