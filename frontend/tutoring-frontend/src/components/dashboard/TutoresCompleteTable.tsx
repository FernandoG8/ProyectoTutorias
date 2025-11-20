import { useMemo } from "react";
import { DataTable } from "@/components/ui/DataTable";
import type { ColumnDef } from "@tanstack/react-table";
import type { DistribucionTutor } from "@/types/dashboard";
import { CARRERA_COLORS, SATURATION_COLORS, calculateSaturation, getSaturationState } from "@/utils/dashboard-utils";
import { colors } from "@/constants/colors";
import { Eye, Edit2 } from "lucide-react";

interface TutoresCompleteTableProps {
  data: DistribucionTutor[];
  isLoading?: boolean;
  onViewStudents?: (tutorId: number, tutorName: string) => void;
  onEdit?: (tutorId: number) => void;
}

/**
 * TutoresCompleteTable Component
 *
 * Professional DataTable showing all tutors with:
 * - Carrera color indicator
 * - Student load / capacity
 * - Saturation percentage and state
 * - Saturation badge with emoji
 * - Action buttons (view students, edit)
 *
 * Features:
 * - Sortable columns (name, students, saturation)
 * - Filterable by carrera/state
 * - Pagination
 * - Sticky headers
 * - Responsive
 */
export const TutoresCompleteTable = ({
  data,
  isLoading,
  onViewStudents,
  onEdit,
}: TutoresCompleteTableProps) => {
  // Enrich data with calculated saturation if not present
  const enrichedData = useMemo(
    () =>
      data.map((tutor) => {
        const porcentajeSaturacion = tutor.porcentaje_saturacion || calculateSaturation(
          tutor.alumnos_asignados,
          tutor.capacidad_max,
        );
        const estado = tutor.estado || getSaturationState(porcentajeSaturacion);

        return {
          ...tutor,
          porcentaje_saturacion: porcentajeSaturacion,
          estado,
        };
      }),
    [data],
  );

  const columns: ColumnDef<(typeof enrichedData)[0]>[] = [
    {
      id: "nombre",
      header: ({ column }) => (
        <button
          onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
          className="flex items-center gap-1 hover:bg-gray-100 px-2 py-1 rounded"
        >
          Nombre
          {column.getIsSorted() === "asc" && " ↑"}
          {column.getIsSorted() === "desc" && " ↓"}
        </button>
      ),
      accessorKey: "tutor_nombre",
      cell: ({ row }) => {
        const tutor = row.original;
        const carreraColor = CARRERA_COLORS[tutor.tutor_carrera];

        return (
          <div className="flex items-center gap-2">
            <div
              className="w-2 h-2 rounded-full flex-shrink-0"
              style={{ backgroundColor: carreraColor.hex }}
            />
            <div className="flex-1">
              <p className="text-sm font-medium">{tutor.tutor_nombre}</p>
              <p className="text-xs" style={{ color: colors.semantic.text.muted }}>
                {carreraColor.nombre}
              </p>
            </div>
          </div>
        );
      },
    },

    {
      id: "alumnos",
      header: ({ column }) => (
        <button
          onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
          className="flex items-center gap-1 hover:bg-gray-100 px-2 py-1 rounded"
        >
          Alumnos
          {column.getIsSorted() === "asc" && " ↑"}
          {column.getIsSorted() === "desc" && " ↓"}
        </button>
      ),
      accessorKey: "alumnos_asignados",
      cell: ({ row }) => {
        const tutor = row.original;
        return (
          <span className="text-sm font-medium">
            {tutor.alumnos_asignados} / {tutor.capacidad_max}
          </span>
        );
      },
    },

    {
      id: "saturacion",
      header: ({ column }) => (
        <button
          onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
          className="flex items-center gap-1 hover:bg-gray-100 px-2 py-1 rounded"
        >
          Saturación
          {column.getIsSorted() === "asc" && " ↑"}
          {column.getIsSorted() === "desc" && " ↓"}
        </button>
      ),
      accessorKey: "porcentaje_saturacion",
      cell: ({ row }) => {
        const tutor = row.original;
        const saturationColor = SATURATION_COLORS[tutor.estado];

        return (
          <div className="flex items-center gap-2">
            {/* Progress bar */}
            <div
              className="flex-1 h-2 rounded-full border"
              style={{
                minWidth: "80px",
                borderColor: saturationColor.border,
                backgroundColor: saturationColor.bg,
              }}
            >
              <div
                className="h-full transition-all"
                style={{
                  width: `${Math.min(tutor.porcentaje_saturacion, 100)}%`,
                  backgroundColor: saturationColor.hex,
                }}
              />
            </div>
            {/* Badge */}
            <span
              className="text-xs font-semibold px-2 py-1 rounded"
              style={{
                backgroundColor: saturationColor.bg,
                color: saturationColor.text,
              }}
            >
              {saturationColor.emoji} {tutor.porcentaje_saturacion}%
            </span>
          </div>
        );
      },
    },

    {
      id: "estado",
      header: "Estado",
      accessorKey: "estado",
      cell: ({ row }) => {
        const tutor = row.original;
        const saturationColor = SATURATION_COLORS[tutor.estado];

        return (
          <div
            className="text-xs font-semibold px-2 py-1 rounded"
            style={{
              backgroundColor: saturationColor.bg,
              color: saturationColor.text,
            }}
          >
            {saturationColor.label}
          </div>
        );
      },
    },

    {
      id: "acciones",
      header: "Acciones",
      cell: ({ row }) => {
        const tutor = row.original;

        return (
          <div className="flex items-center gap-2">
            {onViewStudents && (
              <button
                onClick={() => onViewStudents(tutor.tutor_id, tutor.tutor_nombre)}
                className="p-1 hover:bg-blue-100 rounded transition-colors"
                title="Ver alumnos"
              >
                <Eye
                  className="h-4 w-4"
                  style={{ color: colors.primary[600] }}
                />
              </button>
            )}
            {onEdit && (
              <button
                onClick={() => onEdit(tutor.tutor_id)}
                className="p-1 hover:bg-gray-200 rounded transition-colors"
                title="Editar"
              >
                <Edit2 className="h-4 w-4" style={{ color: colors.semantic.text.muted }} />
              </button>
            )}
          </div>
        );
      },
    },
  ];

  return (
    <div className="rounded-lg border" style={{ borderColor: colors.semantic.border }}>
      <DataTable
        columns={columns}
        data={enrichedData}
        isLoading={isLoading}
        emptyMessage="No hay tutores registrados"
      />
    </div>
  );
};
