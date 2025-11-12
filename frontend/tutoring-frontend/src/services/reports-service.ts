import { api } from "@/lib/api-client";
import { API_URLS } from "@/lib/api-urls";
import type { ApiResponse, ReporteCarrera } from "@/types";

export interface ReportePorCarreraParams {
  semestreAcademico: string;
  carrera?: string;
}

export interface ExportReportParams {
  formato?: string;
  periodo?: string;
}

export const fetchReportePorCarrera = async (
  params: ReportePorCarreraParams,
): Promise<ReporteCarrera> => {
  const { data } = await api.get<ApiResponse<ReporteCarrera>>(API_URLS.reportes.porCarrera, {
    params,
  });
  return data.data;
};

export const exportAlumnosPorTutor = async (
  tutorId: number,
  params?: ExportReportParams,
): Promise<Blob> => {
  const { data } = await api.get<Blob>(API_URLS.reportes.tutores(tutorId), {
    params,
    responseType: "blob",
  });
  return data;
};

export const exportCarrera = async (
  codigo: string,
  params?: ExportReportParams,
): Promise<Blob> => {
  const { data } = await api.get<Blob>(API_URLS.reportes.carreras(codigo), {
    params,
    responseType: "blob",
  });
  return data;
};

export const exportTodasLasCarreras = async (
  params?: ExportReportParams,
): Promise<Blob> => {
  const { data } = await api.get<Blob>(API_URLS.reportes.carrerasTodos, {
    params,
    responseType: "blob",
  });
  return data;
};
