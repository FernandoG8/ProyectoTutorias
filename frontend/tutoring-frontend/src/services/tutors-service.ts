import { api, extractErrorMessage } from "@/lib/api-client";
import { API_URLS } from "@/lib/api-urls";
import type {
  ApiResponse,
  TutorResponse,
  TutorCreateInput,
  TutorUpdateInput,
  TutorFilter,
  TutorConAlumnos,
} from "@/types";

/**
 * Parámetros para búsqueda avanzada de tutores
 * Coincide con el endpoint GET /api/tutores/search según documentación
 */
export interface SearchTutorsParams {
  q: string; // Término de búsqueda (nombre)
  carrera?: string; // Filtro por carrera (opcional)
  limit?: number; // Máx resultados (default: 10)
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
 * 
 * Algoritmo de ranking según documentación:
 * 1. Nombre comienza con: score=300
 * 2. Nombre contiene: score=150
 * 3. Carrera contiene: score=50
 */
export const searchTutors = async (
  params: SearchTutorsParams,
): Promise<TutorResponse[]> => {
  try {
    // Validar que el query tenga al menos 2 caracteres
    if (!params.q || params.q.length < 2) {
      throw new Error("El término de búsqueda debe tener al menos 2 caracteres");
    }

    const { data } = await api.get<ApiResponse<TutorResponse[] | { items: TutorResponse[] }>>(
      API_URLS.tutores.search,
      {
        params: {
          q: params.q,
          carrera: params.carrera,
          limit: params.limit || 10,
        },
      },
    );
    const payload = data.data as TutorResponse[] | { items?: TutorResponse[] };
    // Backend puede responder paginado (data.items) o arreglo directo (data)
    // Normalizamos para devolver siempre un array de tutores
    const list = Array.isArray(payload) ? payload : payload.items ?? [];

    type EnrichedTutor = TutorResponse & { _needsDetail?: boolean };

    // El endpoint /search no está devolviendo capacidad/carga; enriquecemos con detalle del tutor cuando falten
    const mapped: EnrichedTutor[] = list.map((tutor: any) => {
      const capacidadMaxRaw = tutor.capacidadMax ?? tutor.capacidad_max;
      const cargaActualRaw = tutor.cargaActual ?? tutor.carga_actual;
      const capacidadDisponibleRaw = tutor.capacidadDisponible ?? tutor.capacidad_disponible;
      const capacidadMax = capacidadMaxRaw ?? 0;
      const cargaActual = cargaActualRaw ?? 0;
      const capacidadDisponible =
        capacidadDisponibleRaw ?? Math.max(capacidadMax - cargaActual, 0);

      const needsDetail =
        capacidadMaxRaw == null &&
        cargaActualRaw == null &&
        capacidadDisponibleRaw == null;

      return {
        id: tutor.id,
        nombre: tutor.nombre,
        carrera: tutor.carrera,
        capacidadMax,
        cargaActual,
        capacidadDisponible,
        areaAtencion: tutor.areaAtencion ?? tutor.area_atencion ?? null,
        letraEdificio: tutor.letraEdificio ?? tutor.letra_edificio ?? null,
        activo: tutor.activo ?? true,
        _needsDetail: needsDetail,
      };
    });

    const needsEnrichment = mapped.some((tutor) => tutor._needsDetail);
    if (!needsEnrichment) {
      return mapped.map(({ _needsDetail, ...rest }) => rest);
    }

    const enriched = await Promise.all(
      mapped.map(async (tutor) => {
        if (!tutor._needsDetail) {
          const { _needsDetail, ...rest } = tutor;
          return rest;
        }
        try {
          const detail = await getTutor(tutor.id);
          const max = detail.capacidadMax ?? tutor.capacidadMax ?? 0;
          const actual = detail.cargaActual ?? tutor.cargaActual ?? 0;
          return {
            ...tutor,
            ...detail,
            capacidadMax: max,
            cargaActual: actual,
            capacidadDisponible:
              detail.capacidadDisponible ??
              Math.max(max - actual, 0),
          } as TutorResponse;
        } catch {
          const { _needsDetail, ...rest } = tutor;
          return rest as TutorResponse;
        }
      }),
    );

    return enriched;
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
