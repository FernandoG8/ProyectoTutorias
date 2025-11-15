import { api, extractErrorMessage } from "@/lib/api-client";
import { API_URLS } from "@/lib/api-urls";
import type { ReporteCarrera } from "@/types";

/**
 * Parámetros para generar reporte por carrera
 */
export interface ReportePorCarreraParams {
  semestreAcademico: string;
  carrera?: string;
}

/**
 * Parámetros para exportar reportes
 */
export interface ExportReportParams {
  formato?: string; // "PDF" | "EXCEL"
  periodo?: string;
}

/**
 * Genera un reporte por carrera
 * GET /api/reportes/por-carrera
 */
export const fetchReportePorCarrera = async (
  params: ReportePorCarreraParams,
): Promise<ReporteCarrera> => {
  try {
    const { data } = await api.get<ReporteCarrera>(API_URLS.reportes.porCarrera, {
      params,
    });
    // El backend devuelve directamente ReporteCarrera, no ApiResponse
    return data;
  } catch (error) {
    throw new Error(`Error al generar reporte por carrera: ${extractErrorMessage(error)}`);
  }
};

/**
 * Exporta alumnos de un tutor en formato PDF o Excel
 * GET /api/reportes/tutores/{tutorId}/alumnos/exportar
 */
export const exportAlumnosPorTutor = async (
  tutorId: number,
  params?: ExportReportParams,
): Promise<Blob> => {
  try {
    const { data } = await api.get<Blob>(API_URLS.reportes.tutores(tutorId), {
      params,
      responseType: "blob",
    });
    return data;
  } catch (error) {
    throw new Error(`Error al exportar alumnos del tutor: ${extractErrorMessage(error)}`);
  }
};

/**
 * Exporta reporte de una carrera en formato PDF o Excel
 * GET /api/reportes/carreras/{codigo}/exportar
 */
export const exportCarrera = async (
  codigo: string,
  params?: ExportReportParams,
): Promise<Blob> => {
  try {
    const { data } = await api.get<Blob>(API_URLS.reportes.carreras(codigo), {
      params,
      responseType: "blob",
    });
    return data;
  } catch (error) {
    throw new Error(`Error al exportar carrera: ${extractErrorMessage(error)}`);
  }
};

/**
 * Exporta todas las carreras en un archivo ZIP
 * GET /api/reportes/carreras/exportar-todos
 */
export const exportTodasLasCarreras = async (
  params?: ExportReportParams,
): Promise<Blob> => {
  try {
    const { data } = await api.get<Blob>(API_URLS.reportes.carrerasTodos, {
      params,
      responseType: "blob",
    });
    return data;
  } catch (error) {
    throw new Error(`Error al exportar todas las carreras: ${extractErrorMessage(error)}`);
  }
};
