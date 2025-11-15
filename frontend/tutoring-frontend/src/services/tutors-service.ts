import { api, extractErrorMessage } from "@/lib/api-client";
import { API_URLS } from "@/lib/api-urls";
import type {
  ApiResponse,
  TutorResponse,
  TutorCreateInput,
  TutorUpdateInput,
  TutorFilter,
  TutorConAlumnos,
  PagedResponse,
} from "@/types";

/**
 * Parámetros para búsqueda avanzada de tutores
 * Coincide con el endpoint GET /api/tutores/search
 */
export interface SearchTutorsParams {
  q: string; // Requerido, mínimo 2 caracteres
  page?: number;
  size?: number; // Backend usa 'size' en lugar de 'limit'
  sort?: "relevance" | "nombre";
  carrera?: string;
}

/**
 * Lista tutores con filtros opcionales
 * GET /api/tutores
 */
export const listTutors = async (
  params?: TutorFilter,
): Promise<TutorResponse[]> => {
  try {
    const { data } = await api.get<ApiResponse<TutorResponse[]>>(API_URLS.tutores.root, {
      params,
    });
    return data.data || [];
  } catch (error) {
    throw new Error(`Error al listar tutores: ${extractErrorMessage(error)}`);
  }
};

/**
 * Búsqueda avanzada de tutores con ranking
 * GET /api/tutores/search
 */
export const searchTutors = async (
  params: SearchTutorsParams,
): Promise<PagedResponse<TutorResponse>> => {
  try {
    // Validar que el query tenga al menos 2 caracteres
    if (!params.q || params.q.length < 2) {
      throw new Error("El término de búsqueda debe tener al menos 2 caracteres");
    }

    const { data } = await api.get<ApiResponse<PagedResponse<TutorResponse>>>(
      API_URLS.tutores.search,
      {
        params: {
          q: params.q,
          page: params.page || 1,
          size: params.size || 20,
          sort: params.sort || "relevance",
          carrera: params.carrera && params.carrera !== "TODAS" ? params.carrera : undefined,
        },
      },
    );
    return data.data;
  } catch (error) {
    throw new Error(`Error al buscar tutores: ${extractErrorMessage(error)}`);
  }
};

/**
 * Autocomplete de tutores (hasta 10 sugerencias)
 * GET /api/tutores/autocomplete
 */
export const autocompleteTutors = async (
  query: string,
  carrera?: string,
): Promise<TutorResponse[]> => {
  try {
    if (!query || query.length < 2) {
      return [];
    }

    const { data } = await api.get<ApiResponse<TutorResponse[]>>(API_URLS.tutores.autocomplete, {
      params: {
        q: query,
        carrera: carrera && carrera !== "TODAS" ? carrera : undefined,
      },
    });
    return data.data || [];
  } catch (error) {
    console.error("Error en autocomplete de tutores:", error);
    return [];
  }
};

/**
 * Obtiene un tutor por ID
 * GET /api/tutores/{id}
 */
export const getTutor = async (id: number): Promise<TutorResponse> => {
  try {
    const { data } = await api.get<ApiResponse<TutorResponse>>(API_URLS.tutores.detail(id));
    return data.data;
  } catch (error) {
    throw new Error(`Error al obtener tutor: ${extractErrorMessage(error)}`);
  }
};

/**
 * Crea un nuevo tutor
 * POST /api/tutores
 */
export const createTutor = async (
  payload: TutorCreateInput,
): Promise<TutorResponse> => {
  try {
    const { data } = await api.post<ApiResponse<TutorResponse>>(
      API_URLS.tutores.root,
      payload,
    );
    return data.data;
  } catch (error) {
    throw new Error(`Error al crear tutor: ${extractErrorMessage(error)}`);
  }
};

/**
 * Actualiza un tutor
 * PUT /api/tutores/{id}
 */
export const updateTutor = async (
  id: number,
  payload: TutorUpdateInput,
): Promise<TutorResponse> => {
  try {
    const { data } = await api.put<ApiResponse<TutorResponse>>(
      API_URLS.tutores.detail(id),
      payload,
    );
    return data.data;
  } catch (error) {
    throw new Error(`Error al actualizar tutor: ${extractErrorMessage(error)}`);
  }
};

/**
 * Elimina un tutor
 * DELETE /api/tutores/{id}
 */
export const deleteTutor = async (id: number): Promise<void> => {
  try {
    await api.delete<ApiResponse<void>>(API_URLS.tutores.detail(id));
  } catch (error) {
    throw new Error(`Error al eliminar tutor: ${extractErrorMessage(error)}`);
  }
};

/**
 * Obtiene un tutor con sus alumnos asignados
 * GET /api/tutores/{tutorId}/alumnos
 */
export const getTutorWithStudents = async (
  tutorId: number,
  semestreAcademico?: string,
): Promise<TutorConAlumnos> => {
  try {
    const { data } = await api.get<ApiResponse<TutorConAlumnos>>(
      API_URLS.tutores.alumnos(tutorId),
      {
        params: semestreAcademico ? { semestreAcademico } : undefined,
      },
    );
    return data.data;
  } catch (error) {
    throw new Error(`Error al obtener tutor con alumnos: ${extractErrorMessage(error)}`);
  }
};

/**
 * Alias para compatibilidad hacia atrás
 */
export const fetchTutors = listTutors;
