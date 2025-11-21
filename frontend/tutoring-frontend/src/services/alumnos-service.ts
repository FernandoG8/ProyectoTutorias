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
  estado?: string; // "ACTIVO" | "INACTIVO"
  carrera?: string; // Código de carrera (ICA, IE, etc.)
  semestreId?: number; // ID del semestre académico (ej: ID de "2025-2026-F1")
  semestre?: number; // Semestre cursante del alumno (1, 2, 3, etc.) - NUEVO
  soloActivo?: boolean; // DEPRECATED
}

/**
 * Parámetros para búsqueda avanzada de alumnos
 * Coincide con el endpoint GET /api/alumnos/search según documentación
 */
export interface SearchStudentsParams {
  q: string; // Término de búsqueda (nombre, matrícula)
  carrera?: string; // Filtro por carrera (opcional)
  estado?: "ACTIVO" | "INACTIVO"; // Estado del alumno (opcional)
  semestre?: number; // Semestre cursante del alumno (1, 2, 3...)
  page?: number; // Página solicitada (1-based)
  size?: number; // Tamaño de página (default backend: 20)
  sort?: "relevance" | "matricula" | "nombre"; // Ordenamiento opcional
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

    // Asegurar que siempre retornamos una estructura válida
    if (!data.data || !Array.isArray(data.data.items)) {
      return {
        items: [],
        page: params?.page || 1,
        size: params?.limit || 20,
        totalElements: 0,
        totalPages: 0,
      };
    }

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
 * 
 * Algoritmo de ranking según documentación:
 * 1. Coincidencia exacta de matrícula: score=1000
 * 2. Matrícula comienza con: score=500
 * 3. Nombre comienza con: score=300
 * 4. Nombre contiene: score=100
 * 5. Matrícula contiene: score=50
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
        carrera: params.carrera,
        estado: params.estado,
        semestre: params.semestre,
        page: params.page ?? 1,
        size: params.size ?? 20,
        sort: params.sort,
      },
    });

    const payload = data.data;
    if (!payload || !Array.isArray(payload.items)) {
      return {
        items: [],
        page: params.page ?? 1,
        size: params.size ?? 20,
        totalElements: 0,
        totalPages: 0,
      };
    }

    const mappedItems: AlumnoResponse[] = payload.items.map((item) => ({
      id: item.id,
      matricula: item.matricula,
      nombre: item.nombre,
      carrera: item.carrera,
      semestre: item.semestre ?? 0,
      estado: (item.estado as "ACTIVO" | "INACTIVO") || "ACTIVO",
      tutor: item.tutor ?? null,
      cambiosTutor: (item as any).cambiosTutor ?? 0,
    }));

    return {
      ...payload,
      items: mappedItems,
    };
  } catch (error) {
    throw new Error(`Error al buscar alumnos: ${extractErrorMessage(error)}`);
  }
};

// NOTA: El endpoint /autocomplete está mal implementado según especificaciones
// Se debe usar searchStudents con limit=10 para autocompletado

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
