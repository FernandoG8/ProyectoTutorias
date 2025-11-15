import { useNotificationStore } from "@/store/notification-store";

/**
 * Custom Hook: useNotification
 *
 * Provides a simplified interface for displaying notifications throughout the app
 *
 * Usage:
 * ```tsx
 * const { success, error, warning, info } = useNotification();
 *
 * success("Cambios guardados");
 * error("Error al procesar");
 * warning("Verificar antes de continuar");
 * info("Información importante");
 * ```
 *
 * Decision Log:
 * - Extracted notification methods into separate hook for convenience
 * - Named exports (success, error, warning, info) for cleaner usage
 * - Supports optional duration parameter override
 */
export const useNotification = () => {
  const { addNotification } = useNotificationStore();

  return {
    success: (message: string, duration?: number) =>
      addNotification("success", message, { duration }),

    error: (message: string, duration?: number) =>
      addNotification("error", message, { duration }),

    warning: (message: string, duration?: number) =>
      addNotification("warning", message, { duration }),

    info: (message: string, duration?: number) =>
      addNotification("info", message, { duration }),
  };
};
