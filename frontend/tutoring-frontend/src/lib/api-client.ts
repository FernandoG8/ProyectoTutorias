import axios from "axios";
import type { AxiosError, InternalAxiosRequestConfig } from "axios";

const baseURL =
  import.meta.env.VITE_API_BASE_URL ?? import.meta.env.VITE_API_URL ?? "http://localhost:8080";

/**
 * Cliente API configurado con interceptores para manejo centralizado de errores
 * y autenticación mediante cookies HttpOnly.
 */
export const api = axios.create({
  baseURL,
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 30000, // 30 segundos
});

/**
 * Interceptor de solicitudes: agrega headers comunes y maneja configuración
 */
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Log de solicitudes en desarrollo
    if (import.meta.env.DEV) {
      console.debug(`[API] ${config.method?.toUpperCase()} ${config.url}`, {
        params: config.params,
        data: config.data,
      });
    }
    return config;
  },
  (error) => {
    console.error("[API] Error en interceptor de solicitud:", error);
    return Promise.reject(error);
  }
);

/**
 * Interceptor de respuestas: maneja errores comunes y transforma respuestas
 */
api.interceptors.response.use(
  (response) => {
    // Log de respuestas exitosas en desarrollo
    if (import.meta.env.DEV) {
      console.debug(`[API] ${response.config.method?.toUpperCase()} ${response.config.url} - OK`, {
        status: response.status,
        data: response.data,
      });
    }
    return response;
  },
  (error: unknown) => {
    const axiosError = error as AxiosError;
    // Manejo centralizado de errores
    if (axiosError.response) {
      // El servidor respondió con un código de estado fuera del rango 2xx
      const status = axiosError.response.status;
      const data = axiosError.response.data as any;

      switch (status) {
        case 401:
          // No autorizado - redirigir a login
          console.warn("[API] No autorizado - sesión expirada");
          if (window.location.pathname !== "/login") {
            window.location.href = "/login";
          }
          break;
        case 403:
          console.error("[API] Acceso prohibido");
          break;
        case 404:
          console.error("[API] Recurso no encontrado");
          break;
        case 422:
          console.error("[API] Error de validación:", data);
          break;
        case 500:
          console.error("[API] Error interno del servidor");
          break;
        default:
          console.error(`[API] Error ${status}:`, data);
      }

      // Extraer mensaje de error del backend si está disponible
      const errorMessage =
        data?.message || data?.error || axiosError.message || "Error desconocido";
      axiosError.message = errorMessage;
    } else if (axiosError.request) {
      // La solicitud se hizo pero no se recibió respuesta
      console.error("[API] Sin respuesta del servidor - verifica tu conexión");
      axiosError.message = "No se pudo conectar con el servidor. Verifica tu conexión a internet.";
    } else {
      // Algo pasó al configurar la solicitud
      console.error("[API] Error al configurar la solicitud:", axiosError.message);
    }

    return Promise.reject(axiosError);
  }
);

/**
 * Tipos de error personalizados para mejor manejo
 */
export class ApiError extends Error {
  status?: number;
  data?: any;

  constructor(message: string, status?: number, data?: any) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.data = data;
  }
}

/**
 * Tipo para respuesta de error estandarizada del backend
 */
export interface BackendErrorResponse {
  status: number;
  error: string;
  message: string;
  path?: string;
  timestamp?: string;
  fieldErrors?: Array<{
    field: string;
    message: string;
  }>;
  excelErrors?: Array<{
    rowNumber?: number;
    column?: string;
    value?: string;
    message: string;
  }>;
}

/**
 * Helper para extraer mensajes de error de las respuestas del backend
 * Ahora soporta la estructura estandarizada de errores
 */
export const extractErrorMessage = (error: unknown): string => {
  if (error instanceof ApiError) {
    return error.message;
  }
  if (axios.isAxiosError(error)) {
    if (error.response?.data) {
      const data = error.response.data as any;
      // Soporta la estructura estandarizada de error del backend
      return data?.message || data?.error || error.message;
    }
    return error.message || "Error desconocido";
  }
  if (error instanceof Error) {
    return error.message;
  }
  return "Error desconocido";
};

/**
 * Helper para extraer errores de validación de Excel
 * Mapea los errores del backend (snake_case) al formato del frontend (camelCase)
 * Retorna array de errores formateados o null si no hay
 */
export const extractExcelErrors = (error: unknown): Array<{
  filaExcel: number;
  campo: string;
  valor: string;
  descripcion: string;
}> | null => {
  if (axios.isAxiosError(error)) {
    if (error.response?.data) {
      const data = error.response.data as BackendErrorResponse;
      if (data?.excelErrors && data.excelErrors.length > 0) {
        // Mapear de backend format a frontend format
        return data.excelErrors.map((e) => ({
          filaExcel: e.rowNumber ?? 0,
          campo: e.column ?? "desconocido",
          valor: e.value ?? "",
          descripcion: e.message,
        }));
      }
    }
  }
  return null;
};

/**
 * Helper para extraer errores de validación de campos
 * Retorna los fieldErrors si están disponibles
 */
export const extractFieldErrors = (error: unknown): BackendErrorResponse["fieldErrors"] | null => {
  if (axios.isAxiosError(error)) {
    if (error.response?.data) {
      const data = error.response.data as BackendErrorResponse;
      return data?.fieldErrors || null;
    }
  }
  return null;
};

/**
 * Helper para obtener el tipo de error (error code)
 */
export const extractErrorCode = (error: unknown): string | null => {
  if (axios.isAxiosError(error)) {
    if (error.response?.data) {
      const data = error.response.data as BackendErrorResponse;
      return data?.error || null;
    }
  }
  return null;
};
