import { api, extractErrorMessage } from "@/lib/api-client";
import { API_URLS } from "@/lib/api-urls";
import type {
  ApiResponse,
  AlumnoCreateInput,
  AlumnoPagedResponse,
  AlumnoPatchInput,
  AlumnoResponse,
  AlumnoUpdateInput,
} from "@/types";

/**
 * Parámetros para listar alumnos
 * Coincide con los parámetros del endpoint GET /api/alumnos
 */
export interface ListStudentsParams {
  page?: number;
  limit?: number;
  estado?: string;
  carrera?: string;
  semestreId?: number;
  soloActivo?: boolean;
}

/**
 * Parámetros para búsqueda avanzada de alumnos
 * Coincide con el endpoint GET /api/alumnos/search
 */
export interface SearchStudentsParams {
  q: string; // Requerido, mínimo 2 caracteres
  page?: number;
  size?: number; // Backend usa 'size' en lugar de 'limit'
  sort?: "relevance" | "matricula" | "nombre";
  estado?: string;
  carrera?: string;
  semestre?: number;
}

/**
 * Lista alumnos con paginación y filtros
 * GET /api/alumnos
 */
export const listStudents = async (
  params?: ListStudentsParams,
): Promise<AlumnoPagedResponse> => {
  try {
    const { data } = await api.get<ApiResponse<AlumnoPagedResponse>>(API_URLS.alumnos.root, {
      params,
    });
    return data.data;
  } catch (error) {
    throw new Error(`Error al listar alumnos: ${extractErrorMessage(error)}`);
  }
};

/**
 * Lista alumnos del semestre actual (solo activos)
 * GET /api/alumnos/semestre-actual
 */
export const listStudentsCurrentSemester = async (
  params?: { page?: number; limit?: number; carrera?: string },
): Promise<AlumnoPagedResponse> => {
  try {
    const { data } = await api.get<ApiResponse<AlumnoPagedResponse>>(
      API_URLS.alumnos.semestreActual,
      { params },
    );
    return data.data;
  } catch (error) {
    throw new Error(`Error al listar alumnos del semestre actual: ${extractErrorMessage(error)}`);
  }
};

/**
 * Búsqueda avanzada de alumnos con ranking
 * GET /api/alumnos/search
 */
export const searchStudents = async (
  params: SearchStudentsParams,
): Promise<AlumnoPagedResponse> => {
  try {
    // Validar que el query tenga al menos 2 caracteres
    if (!params.q || params.q.length < 2) {
      throw new Error("El término de búsqueda debe tener al menos 2 caracteres");
    }

    const { data } = await api.get<ApiResponse<AlumnoPagedResponse>>(API_URLS.alumnos.search, {
      params: {
        q: params.q,
        page: params.page || 1,
        size: params.size || 20,
        sort: params.sort || "relevance",
        estado: params.estado && params.estado !== "TODOS" ? params.estado : undefined,
        carrera: params.carrera && params.carrera !== "TODAS" ? params.carrera : undefined,
        semestre: params.semestre,
      },
    });
    return data.data;
  } catch (error) {
    throw new Error(`Error al buscar alumnos: ${extractErrorMessage(error)}`);
  }
};

/**
 * Autocomplete de alumnos (hasta 10 sugerencias)
 * GET /api/alumnos/autocomplete
 */
export const autocompleteStudents = async (
  query: string,
  estado?: string,
  carrera?: string,
): Promise<AlumnoResponse[]> => {
  try {
    if (!query || query.length < 2) {
      return [];
    }

    const { data } = await api.get<ApiResponse<AlumnoResponse[]>>(API_URLS.alumnos.autocomplete, {
      params: {
        q: query,
        estado: estado && estado !== "TODOS" ? estado : undefined,
        carrera: carrera && carrera !== "TODAS" ? carrera : undefined,
      },
    });
    return data.data || [];
  } catch (error) {
    console.error("Error en autocomplete de alumnos:", error);
    return [];
  }
};

/**
 * Busca un alumno por matrícula exacta
 * GET /api/alumnos/by-matricula/{matricula}
 */
export const getStudentByMatricula = async (
  matricula: string,
): Promise<AlumnoResponse> => {
  try {
    const { data } = await api.get<ApiResponse<AlumnoResponse>>(
      API_URLS.alumnos.byMatricula(matricula),
    );
    return data.data;
  } catch (error) {
    throw new Error(`Error al buscar alumno por matrícula: ${extractErrorMessage(error)}`);
  }
};

/**
 * Obtiene un alumno por ID
 * GET /api/alumnos/{id}
 */
export const getStudent = async (id: number): Promise<AlumnoResponse> => {
  try {
    const { data } = await api.get<ApiResponse<AlumnoResponse>>(API_URLS.alumnos.detail(id));
    return data.data;
  } catch (error) {
    throw new Error(`Error al obtener alumno: ${extractErrorMessage(error)}`);
  }
};

/**
 * Crea un nuevo alumno
 * POST /api/alumnos
 */
export const createStudent = async (
  payload: AlumnoCreateInput,
): Promise<AlumnoResponse> => {
  try {
    const { data } = await api.post<ApiResponse<AlumnoResponse>>(
      API_URLS.alumnos.root,
      payload,
    );
    return data.data;
  } catch (error) {
    throw new Error(`Error al crear alumno: ${extractErrorMessage(error)}`);
  }
};

/**
 * Actualiza completamente un alumno (PUT)
 * PUT /api/alumnos/{id}
 */
export const updateStudent = async (
  id: number,
  payload: AlumnoUpdateInput,
): Promise<AlumnoResponse> => {
  try {
    const { data } = await api.put<ApiResponse<AlumnoResponse>>(
      API_URLS.alumnos.detail(id),
      payload,
    );
    return data.data;
  } catch (error) {
    throw new Error(`Error al actualizar alumno: ${extractErrorMessage(error)}`);
  }
};

/**
 * Actualiza parcialmente un alumno (PATCH)
 * PATCH /api/alumnos/{id}
 */
export const patchStudent = async (
  id: number,
  payload: AlumnoPatchInput,
): Promise<AlumnoResponse> => {
  try {
    const { data } = await api.patch<ApiResponse<AlumnoResponse>>(
      API_URLS.alumnos.detail(id),
      payload,
    );
    return data.data;
  } catch (error) {
    throw new Error(`Error al actualizar parcialmente alumno: ${extractErrorMessage(error)}`);
  }
};

/**
 * Elimina un alumno
 * DELETE /api/alumnos/{id}
 */
export const deleteStudent = async (id: number): Promise<void> => {
  try {
    await api.delete<ApiResponse<void>>(API_URLS.alumnos.detail(id));
  } catch (error) {
    throw new Error(`Error al eliminar alumno: ${extractErrorMessage(error)}`);
  }
};
