import { useEffect, useReducer, useCallback } from "react";
import type { ApiResponse } from "@/types/api";

const FetchStatus = {
  Idle: "idle",
  Loading: "loading", 
  Success: "success",
  Error: "error",
} as const;

type FetchStatus = typeof FetchStatus[keyof typeof FetchStatus];

interface FetchState<T> {
  status: FetchStatus;
  data?: T;
  error?: string;
}

type FetchAction<T> =
  | { type: "LOADING" }
  | { type: "SUCCESS"; payload: T }
  | { type: "ERROR"; payload: string }
  | { type: "RESET" };

/**
 * Custom Hook: useFetch
 *
 * Simplified API data fetching with caching support
 *
 * Usage:
 * ```tsx
 * const { data, status, error, refetch } = useFetch<Student[]>(
 *   `/api/students`
 * );
 *
 * if (status === "loading") return <Skeleton />;
 * if (status === "error") return <Error message={error} />;
 * return <StudentList students={data} />;
 * ```
 *
 * Decision Log:
 * - Built on top of axios which is already configured
 * - Simpler API than React Query for basic use cases
 * - Integrated with ApiResponse wrapper format
 * - Manual refetch control
 */
export const useFetch = <T,>(url: string, skip = false) => {
  const [state, dispatch] = useReducer(
    (state: FetchState<T>, action: FetchAction<T>): FetchState<T> => {
      switch (action.type) {
        case "LOADING":
          return { status: "loading" };
        case "SUCCESS":
          return { status: "success", data: action.payload };
        case "ERROR":
          return { status: "error", error: action.payload };
        case "RESET":
          return { status: "idle" };
        default:
          return state;
      }
    },
    { status: "idle" }
  );

  const fetchData = useCallback(async () => {
    if (skip) return;

    dispatch({ type: "LOADING" });
    try {
      const { api } = await import("@/lib/api-client");
      const response = await api.get<ApiResponse<T>>(url);

      // Handle ApiResponse wrapper
      const data = response.data.data ?? response.data;
      dispatch({ type: "SUCCESS", payload: data as T });
    } catch (error) {
      let message = "Error al cargar datos";

      // Distinguish between different error types
      if (error instanceof Error) {
        message = error.message;
      } else if (typeof error === "object" && error !== null && "message" in error) {
        message = (error as { message: string }).message;
      } else if (typeof error === "string") {
        message = error;
      }

      // More specific error handling based on error type
      if (message.includes("Network")) {
        message = "Error de conexión. Verifica tu internet e intenta de nuevo.";
      } else if (message.includes("401") || message.includes("Unauthorized")) {
        message = "No autorizado. Por favor inicia sesión de nuevo.";
      } else if (message.includes("403") || message.includes("Forbidden")) {
        message = "No tienes permiso para acceder a este recurso.";
      } else if (message.includes("404")) {
        message = "El recurso no fue encontrado.";
      } else if (message.includes("500")) {
        message = "Error en el servidor. Intenta más tarde.";
      }

      dispatch({ type: "ERROR", payload: message });
    }
  }, [url, skip]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  return {
    ...state,
    refetch: fetchData,
    FetchStatus,
  };
};
