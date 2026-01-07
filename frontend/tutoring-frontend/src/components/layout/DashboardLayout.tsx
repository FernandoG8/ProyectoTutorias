import type { ReactNode } from "react";
import { Topbar } from "./Topbar";
import { Sidebar } from "./Sidebar";

interface DashboardLayoutProps {
  children: ReactNode;
}

/**
 * Layout principal del dashboard con topbar y sidebar fijos
 * 
 * Estructura:
 * - Contenedor principal: h-screen flex
 * - Sidebar: ancho fijo, siempre visible
 * - Área principal: flex-1 con topbar fijo y contenido scrolleable
 * 
 * IMPORTANTE: Solo el área de contenido debe tener scroll
 */
export const DashboardLayout = ({ children }: DashboardLayoutProps) => {
  return (
    <div className="h-screen flex bg-gray-50">
      {/* Sidebar fijo */}
      <Sidebar />
      
      {/* Área principal */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Topbar fijo */}
        <Topbar />
        
        {/* Contenido scrolleable */}
        <main className="flex-1 overflow-y-auto">
          <div className="h-full">
            {children}
          </div>
        </main>
      </div>
    </div>
  );
};
