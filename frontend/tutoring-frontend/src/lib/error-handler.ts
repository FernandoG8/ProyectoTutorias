import { AxiosError } from "axios";

/**
 * Tipos de errores del sistema
 */
export interface AppError {
  type: "validation" | "network" | "server" | "auth" | "unknown";
  message: string;
  details?: any;
  code?: string | number;
}

/**
 * Maneja errores de forma consistente en toda la aplicación
 */
export class ErrorHandler {
  /**
   * Convierte cualquier error en un AppError estructurado
   */
  static handle(error: unknown): AppError {
    // Error de Axios (HTTP)
    if (error instanceof AxiosError) {
      return this.handleAxiosError(error);
    }

    // Error nativo de JavaScript
    if (error instanceof Error) {
      return {
        type: "unknown",
        message: error.message,
        details: error.stack
      };
    }

    // Error desconocido
    return {
      type: "unknown",
      message: "Ha ocurrido un error inesperado",
      details: error
    };
  }

  /**
   * Maneja errores específicos de Axios
   */
  private static handleAxiosError(error: AxiosError): AppError {
    const response = error.response;
    
    // Error de red (sin respuesta del servidor)
    if (!response) {
      return {
        type: "network",
        message: "Error de conexión. Verifica tu conexión a internet.",
        code: error.code
      };
    }

    // Error de autenticación
    if (response.status === 401) {
      return {
        type: "auth",
        message: "Sesión expirada. Por favor, inicia sesión nuevamente.",
        code: response.status
      };
    }

    // Error de validación
    if (response.status === 400) {
      const data = response.data as any;
      return {
        type: "validation",
        message: data?.message || "Datos inválidos",
        details: data?.errors || data?.data,
        code: response.status
      };
    }

    // Error de permisos
    if (response.status === 403) {
      return {
        type: "auth",
        message: "No tienes permisos para realizar esta acción",
        code: response.status
      };
    }

    // Error de recurso no encontrado
    if (response.status === 404) {
      return {
        type: "server",
        message: "El recurso solicitado no fue encontrado",
        code: response.status
      };
    }

    // Error del servidor
    if (response.status >= 500) {
      return {
        type: "server",
        message: "Error interno del servidor. Intenta nuevamente más tarde.",
        code: response.status,
        details: response.data
      };
    }

    // Otros errores HTTP
    const data = response.data as any;
    return {
      type: "server",
      message: data?.message || `Error HTTP ${response.status}`,
      code: response.status,
      details: data
    };
  }

  /**
   * Obtiene un mensaje de error amigable para el usuario
   */
  static getUserMessage(error: AppError): string {
    switch (error.type) {
      case "network":
        return "Problema de conexión. Verifica tu internet e intenta nuevamente.";
      case "auth":
        return error.message;
      case "validation":
        return error.message;
      case "server":
        return error.code === 500 
          ? "Error del servidor. Intenta más tarde."
          : error.message;
      default:
        return "Ha ocurrido un error inesperado.";
    }
  }

  /**
   * Determina si el error es recuperable (el usuario puede reintentar)
   */
  static isRetryable(error: AppError): boolean {
    return error.type === "network" || 
           (error.type === "server" && error.code === 500);
  }

  /**
   * Determina si el error requiere logout
   */
  static requiresLogout(error: AppError): boolean {
    return error.type === "auth" && error.code === 401;
  }
}

/**
 * Hook para manejo de errores en componentes
 */
export const useErrorHandler = () => {
  const handleError = (error: unknown) => {
    const appError = ErrorHandler.handle(error);
    
    // Log del error para debugging
    console.error("App Error:", appError);
    
    return appError;
  };

  return { handleError };
};
