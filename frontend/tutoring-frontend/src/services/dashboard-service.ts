import { api } from "@/lib/api-client";
import { API_URLS } from "@/lib/api-urls";
import type {
  ApiResponse,
  DashboardEstadisticas,
  DistribucionTutor,
  ProcesoReciente,
  SemestreActivoInfo,
  DashboardHealth,
} from "@/types";

export const getDashboardEstadisticas = async (): Promise<DashboardEstadisticas> => {
  const { data } = await api.get<ApiResponse<DashboardEstadisticas>>(
    API_URLS.dashboard.estadisticas,
  );
  return data.data;
};

export const getDistribucionTutores = async (): Promise<DistribucionTutor[]> => {
  const { data } = await api.get<ApiResponse<DistribucionTutor[]>>(
    API_URLS.dashboard.distribucionTutores,
  );
  return data.data;
};

export const getProcesosRecientes = async (
  limit: number = 5,
): Promise<ProcesoReciente[]> => {
  const { data } = await api.get<ApiResponse<ProcesoReciente[]>>(
    API_URLS.dashboard.procesosRecientes,
    { params: { limit } },
  );
  return data.data;
};

export const getSemestreActivoInfo = async (): Promise<SemestreActivoInfo> => {
  const { data } = await api.get<ApiResponse<SemestreActivoInfo>>(
    API_URLS.dashboard.semestreActivo,
  );
  return data.data;
};

export const getDashboardHealth = async (): Promise<DashboardHealth> => {
  const { data } = await api.get<ApiResponse<DashboardHealth>>(
    API_URLS.dashboard.health,
  );
  return data.data;
};

