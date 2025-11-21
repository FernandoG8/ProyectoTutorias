import type { ComponentType, SVGProps } from "react";
import {
  ClipboardListIcon,
  FileBarChartIcon,
  GraduationCapIcon,
  HomeIcon,
  UploadCloudIcon,
  UserXIcon,
  UsersIcon,
} from "@/components/icons";
import { Calendar } from "lucide-react";

export interface NavigationItem {
  label: string;
  path: string;
  icon: ComponentType<SVGProps<SVGSVGElement>>;
}

export const navigationItems: NavigationItem[] = [
  {
    label: "Inicio",
    path: "/dashboard",
    icon: HomeIcon,
  },
  {
    label: "Alumnos",
    path: "/students",
    icon: GraduationCapIcon,
  },
  {
    label: "Tutores",
    path: "/tutors",
    icon: UsersIcon,
  },
  {
    label: "Asignaciones",
    path: "/assignment",
    icon: ClipboardListIcon,
  },
  {
    label: "Cambio de tutor",
    path: "/tutor-change",
    icon: UploadCloudIcon,
  },
  {
    label: "Reportes",
    path: "/reports",
    icon: FileBarChartIcon,
  },
  {
    label: "Alumnos inactivos",
    path: "/inactive-students",
    icon: UserXIcon,
  },
  {
    label: "Semestres",
    path: "/semestres",
    icon: Calendar,
  },
];
