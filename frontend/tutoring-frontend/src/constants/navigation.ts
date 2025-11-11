import type { ReactNode } from "react";

export interface NavigationItem {
  label: string;
  path: string;
  icon?: ReactNode;
}

export const navigationItems: NavigationItem[] = [
  { label: "Dashboard", path: "/dashboard" },
  { label: "Carga de listas", path: "/list-upload" },
  { label: "Asignación", path: "/assignment" },
  { label: "Gestión de tutores", path: "/tutors" },
  { label: "Cambio de tutor", path: "/tutor-change" },
  { label: "Reportes", path: "/reports" },
  { label: "Configuración", path: "/settings" },
];
