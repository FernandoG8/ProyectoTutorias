import { Calendar } from "lucide-react";
import { useSemestreStore } from "@/store/semestre-store";

/**
 * Selector de semestre para el topbar (SOLO LECTURA)
 *
 * Muestra el semestre académico activo (ej: 2025-2026-F1)
 * Este semestre determina qué ciclo escolar está vigente
 */
export const SemestreSelector = () => {
  const { semestreActivo, isLoading } = useSemestreStore();

  if (isLoading) {
    return (
      <div className="flex items-center space-x-2 px-3 py-2 bg-gray-100 rounded-lg animate-pulse">
        <Calendar className="w-4 h-4 text-gray-400" />
        <span className="text-sm text-gray-500">Cargando...</span>
      </div>
    );
  }

  return (
    <div className="flex items-center space-x-2">
      <Calendar className="w-4 h-4 text-gray-500" />
      <div className="min-w-[200px] px-3 py-2 bg-gray-50 rounded-lg border border-gray-200">
        <div className="text-xs text-gray-500">Ciclo Académico:</div>
        <div className="text-sm font-medium text-gray-900">
          {semestreActivo ? `${semestreActivo.codigo}` : "Sin semestre activo"}
        </div>
      </div>
    </div>
  );
};