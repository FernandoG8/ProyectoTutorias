import { 
  Home, 
  Users, 
  UserCheck, 
  BookOpen, 
  Calendar,
  UserX,
  BarChart3,
  Settings,
  FileText,
  ArrowRightLeft
} from "lucide-react";
import { NavLink, useLocation } from "react-router-dom";
import { colors } from "@/constants/colors";

/**
 * Sidebar fijo del dashboard
 * 
 * Características:
 * - Ancho fijo (w-64)
 * - Siempre visible
 * - Navegación principal del sistema
 * - Indicador visual de página activa
 */

interface NavItem {
  to: string;
  icon: React.ComponentType<{ className?: string }>;
  label: string;
  badge?: string;
}

const navigationItems: NavItem[] = [
  {
    to: "/dashboard",
    icon: Home,
    label: "Dashboard"
  },
  {
    to: "/alumnos",
    icon: Users,
    label: "Alumnos"
  },
  {
    to: "/tutores",
    icon: UserCheck,
    label: "Tutores"
  },
  {
    to: "/asignaciones",
    icon: ArrowRightLeft,
    label: "Asignaciones"
  },
  {
    to: "/cambio-tutor",
    icon: UserX,
    label: "Cambio de Tutor"
  },
  {
    to: "/semestres",
    icon: Calendar,
    label: "Semestres"
  },
  {
    to: "/alumnos-inactivos",
    icon: UserX,
    label: "Alumnos Inactivos"
  },
  {
    to: "/reportes",
    icon: FileText,
    label: "Reportes"
  },
  {
    to: "/estadisticas",
    icon: BarChart3,
    label: "Estadísticas"
  }
];

const adminItems: NavItem[] = [
  {
    to: "/mantenimiento",
    icon: Settings,
    label: "Mantenimiento"
  }
];

export const Sidebar = () => {
  const location = useLocation();

  const isActive = (path: string) => {
    return location.pathname === path || location.pathname.startsWith(path + '/');
  };

  return (
    <aside className="w-64 bg-white border-r border-gray-200 flex flex-col shadow-sm">
      {/* Logo/Header */}
      <div className="h-16 flex items-center px-6 border-b border-gray-200">
        <div className="flex items-center space-x-3">
          <div 
            className="h-8 w-8 rounded-lg flex items-center justify-center"
            style={{ backgroundColor: colors.primary[500] }}
          >
            <BookOpen className="h-5 w-5 text-white" />
          </div>
          <div>
            <h2 className="text-lg font-semibold text-gray-900">Tutorías</h2>
            <p className="text-xs text-gray-500">UABC</p>
          </div>
        </div>
      </div>

      {/* Navegación principal */}
      <nav className="flex-1 px-4 py-6 space-y-1">
        <div className="space-y-1">
          {navigationItems.map((item) => {
            const Icon = item.icon;
            const active = isActive(item.to);
            
            return (
              <NavLink
                key={item.to}
                to={item.to}
                className={`
                  flex items-center space-x-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors
                  ${active 
                    ? 'bg-blue-50 text-blue-700 border-r-2 border-blue-700' 
                    : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'
                  }
                `}
              >
                <Icon className={`h-5 w-5 ${active ? 'text-blue-700' : 'text-gray-400'}`} />
                <span>{item.label}</span>
                {item.badge && (
                  <span className="ml-auto bg-red-100 text-red-600 text-xs px-2 py-0.5 rounded-full">
                    {item.badge}
                  </span>
                )}
              </NavLink>
            );
          })}
        </div>

        {/* Separador */}
        <div className="border-t border-gray-200 my-6"></div>

        {/* Sección de administración */}
        <div className="space-y-1">
          <h3 className="px-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">
            Administración
          </h3>
          {adminItems.map((item) => {
            const Icon = item.icon;
            const active = isActive(item.to);
            
            return (
              <NavLink
                key={item.to}
                to={item.to}
                className={`
                  flex items-center space-x-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors
                  ${active 
                    ? 'bg-blue-50 text-blue-700 border-r-2 border-blue-700' 
                    : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'
                  }
                `}
              >
                <Icon className={`h-5 w-5 ${active ? 'text-blue-700' : 'text-gray-400'}`} />
                <span>{item.label}</span>
              </NavLink>
            );
          })}
        </div>
      </nav>

      {/* Footer del sidebar */}
      <div className="p-4 border-t border-gray-200">
        <div className="text-xs text-gray-500 text-center">
          <p>Sistema de Tutorías v2.0</p>
          <p>Universidad Autónoma de BC</p>
        </div>
      </div>
    </aside>
  );
};