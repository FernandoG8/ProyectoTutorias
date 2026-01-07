import axios from "axios";
import { api, extractErrorMessage } from "@/lib/api-client";
import { API_URLS } from "@/lib/api-urls";
import type {
  ApiResponse,
  DashboardEstadisticas,
  DistribucionTutor,
  ProcesoReciente,
  SemestreActivoInfo,
  DashboardHealth,
} from "@/types";

/**
 * Obtiene estadísticas generales del dashboard
 * GET /api/dashboard/estadisticas
 */
export const getDashboardEstadisticas = async (
  semestreId?: number,
): Promise<DashboardEstadisticas> => {
  try {
    const { data } = await api.get<ApiResponse<DashboardEstadisticas>>(
      API_URLS.dashboard.estadisticas,
      {
        params: semestreId ? { semestreId } : undefined,
      },
    );
    return data.data;
  } catch (error) {
    throw new Error(`Error al obtener estadísticas: ${extractErrorMessage(error)}`);
  }
};

/**
 * Obtiene la distribución de alumnos por tutor
 * GET /api/dashboard/distribucion-tutores
 */
export const getDistribucionTutores = async (
  semestreId?: number,
): Promise<DistribucionTutor[]> => {
  try {
    const { data } = await api.get<ApiResponse<DistribucionTutor[]>>(
      API_URLS.dashboard.distribucionTutores,
      {
        params: semestreId ? { semestreId } : undefined,
      },
    );
    return data.data || [];
  } catch (error) {
    throw new Error(`Error al obtener distribución de tutores: ${extractErrorMessage(error)}`);
  }
};

/**
 * Obtiene los procesos de asignación más recientes
 * GET /api/dashboard/procesos-recientes
 */
export const getProcesosRecientes = async (
  limit: number = 5,
): Promise<ProcesoReciente[]> => {
  try {
    const { data } = await api.get<ApiResponse<ProcesoReciente[]>>(
      API_URLS.dashboard.procesosRecientes,
      { params: { limit } },
    );
    return data.data || [];
  } catch (error) {
    throw new Error(`Error al obtener procesos recientes: ${extractErrorMessage(error)}`);
  }
};

/**
 * Obtiene información del semestre activo
 * GET /api/dashboard/semestre-activo
 */
export const getSemestreActivoInfo = async (): Promise<SemestreActivoInfo | null> => {
  try {
    const { data } = await api.get<ApiResponse<SemestreActivoInfo>>(
      API_URLS.dashboard.semestreActivo,
    );
    return data.data;
  } catch (error) {
    // Si no hay semestre activo, el backend devuelve 400
    if (axios.isAxiosError(error) && error.response?.status === 400) {
      return null;
    }
    throw new Error(`Error al obtener semestre activo: ${extractErrorMessage(error)}`);
  }
};

/**
 * Health check del dashboard
 * GET /api/dashboard/health
 */
export const getDashboardHealth = async (): Promise<DashboardHealth> => {
  try {
    const { data } = await api.get<ApiResponse<DashboardHealth>>(
      API_URLS.dashboard.health,
    );
    return data.data;
  } catch (error) {
    throw new Error(`Error en health check: ${extractErrorMessage(error)}`);
  }
};

