import { User, LogOut, Settings } from "lucide-react";
import { useState } from "react";
import { useNotification } from "@/hooks/useNotification";
import { SemestreSelector } from "@/components/common/SemestreSelector";
import { colors } from "@/constants/colors";

/**
 * Topbar fijo del dashboard
 * 
 * Características:
 * - Altura fija
 * - Siempre visible en la parte superior
 * - Contiene: título, selector de semestre, notificaciones, usuario
 */
export const Topbar = () => {
  const [showUserMenu, setShowUserMenu] = useState(false);
  const { success } = useNotification();

  const handleLogout = () => {
    // TODO: Implementar logout
    success("Sesión cerrada correctamente");
  };

  return (
    <header 
      className="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-6 shadow-sm"
      style={{ borderBottomColor: colors.semantic.border }}
    >
      {/* Lado izquierdo - Título y breadcrumb */}
      <div className="flex items-center space-x-4">
        <div>
          <h1 className="text-xl font-semibold text-gray-900">
            Sistema de Tutorías
          </h1>
          <p className="text-sm text-gray-500">
            Gestión Académica
          </p>
        </div>
      </div>

      {/* Centro - Selector de semestre */}
      <div className="flex-1 flex justify-center max-w-md">
        <SemestreSelector />
      </div>

      {/* Lado derecho - Acciones y usuario */}
      <div className="flex items-center space-x-4">

        {/* Menú de usuario */}
        <div className="relative">
          <button
            onClick={() => setShowUserMenu(!showUserMenu)}
            className="flex items-center space-x-2 p-2 text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <div className="h-8 w-8 bg-blue-500 rounded-full flex items-center justify-center">
              <User className="h-4 w-4 text-white" />
            </div>
            <span className="text-sm font-medium">Coordinador</span>
          </button>

          {/* Dropdown del usuario */}
          {showUserMenu && (
            <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 py-1 z-50">
              <button className="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100 flex items-center space-x-2">
                <Settings className="h-4 w-4" />
                <span>Configuración</span>
              </button>
              <hr className="my-1 border-gray-200" />
              <button 
                onClick={handleLogout}
                className="w-full px-4 py-2 text-left text-sm text-red-600 hover:bg-red-50 flex items-center space-x-2"
              >
                <LogOut className="h-4 w-4" />
                <span>Cerrar sesión</span>
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};