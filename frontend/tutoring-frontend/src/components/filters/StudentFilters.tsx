import { Card } from "@/components/ui/Card";
import { SearchInput } from "@/components/SearchInput";
import { colors } from "@/constants/colors";
import type { EstadoAlumno } from "@/types";

interface StudentFiltersProps {
  search: string;
  onSearchChange: (value: string) => void;
  estado: EstadoAlumno | "TODOS";
  onEstadoChange: (value: EstadoAlumno | "TODOS") => void;
  carrera: string;
  onCarreraChange: (value: string) => void;
  carreras: string[];
}

/**
 * StudentFilters Component
 *
 * Filtros profesionales para página de alumnos
 * - Búsqueda por nombre/matrícula
 * - Filtro por estado (Activo/Inactivo)
 * - Filtro por carrera
 */
export const StudentFilters = ({
  search,
  onSearchChange,
  estado,
  onEstadoChange,
  carrera,
  onCarreraChange,
  carreras,
}: StudentFiltersProps) => {
  return (
    <Card>
      <div className="grid gap-4 md:grid-cols-3">
        {/* Search */}
        <div>
          <label
            className="block text-sm font-medium mb-2"
            style={{ color: colors.semantic.text.primary }}
          >
            Búsqueda
          </label>
          <SearchInput
            placeholder="Nombre o matrícula..."
            value={search}
            onChange={onSearchChange}
          />
        </div>

        {/* Estado filter */}
        <div>
          <label
            className="block text-sm font-medium mb-2"
            style={{ color: colors.semantic.text.primary }}
          >
            Estado
          </label>
          <select
            value={estado}
            onChange={(e) => onEstadoChange(e.target.value as EstadoAlumno | "TODOS")}
            className="w-full rounded-lg border px-3 py-2 text-sm"
            style={{ borderColor: colors.semantic.border }}
          >
            <option value="TODOS">Todos</option>
            <option value="ACTIVO">Activos</option>
            <option value="INACTIVO">Inactivos</option>
          </select>
        </div>

        {/* Carrera filter */}
        <div>
          <label
            className="block text-sm font-medium mb-2"
            style={{ color: colors.semantic.text.primary }}
          >
            Carrera
          </label>
          <select
            value={carrera}
            onChange={(e) => onCarreraChange(e.target.value)}
            className="w-full rounded-lg border px-3 py-2 text-sm"
            style={{ borderColor: colors.semantic.border }}
          >
            <option value="TODAS">Todas</option>
            {carreras.map((c) => (
              <option key={c} value={c}>
                {c}
              </option>
            ))}
          </select>
        </div>
      </div>
    </Card>
  );
};
