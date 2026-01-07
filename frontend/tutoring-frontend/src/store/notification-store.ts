import { create } from "zustand";
import { toast } from "react-toastify";

export type NotificationType = "success" | "error" | "warning" | "info";

export interface Notification {
  id: string;
  type: NotificationType;
  message: string;
  title?: string;
  duration?: number;
  action?: {
    label: string;
    onClick: () => void;
  };
}

interface NotificationState {
  notifications: Notification[];
  addNotification: (
    type: NotificationType,
    message: string,
    options?: Partial<Notification>
  ) => void;
  removeNotification: (id: string) => void;
  clearAll: () => void;
}

/**
 * Notification Store
 *
 * Manages application-wide notifications/toasts using Zustand + React Toastify
 *
 * Usage:
 * ```tsx
 * const { addNotification } = useNotificationStore();
 * addNotification("success", "Cambios guardados exitosamente");
 * addNotification("error", "Error al cargar datos", { title: "Error" });
 * ```
 *
 * Decision Log:
 * - Zustand for state management (lightweight, performant)
 * - React Toastify for UI rendering (battle-tested, accessible)
 * - Separated concerns: store handles state, toastify handles rendering
 * - ID-based tracking for potential future undo/redo functionality
 */
export const useNotificationStore = create<NotificationState>((set) => ({
  notifications: [],

  addNotification: (type, message, options = {}) => {
    const id = `${Date.now()}-${Math.random()}`;
    const notification: Notification = {
      id,
      type,
      message,
      duration: 3000,
      ...options,
    };

    // Add to store for state tracking
    set((state) => ({
      notifications: [...state.notifications, notification],
    }));

    // Show toast with appropriate type
    const toastOptions = {
      position: "bottom-right" as const,
      autoClose: notification.duration,
      hideProgressBar: false,
      closeOnClick: true,
      pauseOnHover: true,
      draggable: true,
    };

    switch (type) {
      case "success":
        toast.success(message, toastOptions);
        break;
      case "error":
        toast.error(message, toastOptions);
        break;
      case "warning":
        toast.warning(message, toastOptions);
        break;
      case "info":
        toast.info(message, toastOptions);
        break;
    }

    // Auto-remove from store after duration
    if (notification.duration) {
      setTimeout(() => {
        set((state) => ({
          notifications: state.notifications.filter((n) => n.id !== id),
        }));
      }, notification.duration);
    }
  },

  removeNotification: (id) => {
    set((state) => ({
      notifications: state.notifications.filter((n) => n.id !== id),
    }));
  },

  clearAll: () => {
    set({ notifications: [] });
  },
}));
