import { useMemo } from "react";
import type { CarreraDistribution } from "@/types/dashboard";
import { CARRERA_COLORS, SATURATION_COLORS } from "@/utils/dashboard-utils";
import { colors } from "@/constants/colors";

interface CarreraDistributionChartProps {
  data: CarreraDistribution[];
  isLoading?: boolean;
}

/**
 * CarreraDistributionChart Component
 *
 * Visual representation of tutor distribution by carrera (program)
 * Shows:
 * - Horizontal bar chart with carrera colors
 * - Number of tutors per carrera
 * - Student load per carrera
 * - Saturation state badge
 *
 * Features:
 * - Color-coded bars by carrera
 * - Status badges (balanceado/crítico)
 * - Responsive layout
 * - Loading state
 */
export const CarreraDistributionChart = ({
  data,
  isLoading,
}: CarreraDistributionChartProps) => {
  // Calculate max for scaling
  const maxAlumnos = useMemo(
    () => Math.max(...data.map((d) => d.totalAlumnos), 1),
    [data],
  );

  if (isLoading) {
    return (
      <div className="space-y-4">
        {[1, 2, 3, 4, 5, 6].map((i) => (
          <div key={i} className="space-y-2">
            <div className="h-4 bg-gray-200 rounded w-1/3 animate-pulse" />
            <div className="h-8 bg-gray-100 rounded animate-pulse" />
          </div>
        ))}
      </div>
    );
  }

  if (data.length === 0) {
    return (
      <div className="text-center py-8">
        <p style={{ color: colors.semantic.text.muted }}>
          No hay datos de distribución disponibles
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {data.map((carrera) => {
        const carreraColor = CARRERA_COLORS[carrera.carrera];
        const saturationColor = SATURATION_COLORS[carrera.estado];
        const percentage = (carrera.totalAlumnos / maxAlumnos) * 100;

        return (
          <div key={carrera.carrera} className="space-y-2">
            {/* Header with name and badges */}
            <div className="flex items-center justify-between gap-2">
              <div className="flex items-center gap-2 flex-1">
                <div
                  className="w-3 h-3 rounded-full flex-shrink-0"
                  style={{ backgroundColor: carreraColor.hex }}
                />
                <span className="text-sm font-medium" style={{ color: colors.semantic.text.primary }}>
                  {carrera.carrera}
                </span>
                <span className="text-xs" style={{ color: colors.semantic.text.muted }}>
                  {carrera.nombreCompleto}
                </span>
              </div>
              <div className="flex items-center gap-2">
                {carrera.estado === "crítico" && (
                  <span className="text-xs font-semibold text-red-700 bg-red-100 px-2 py-1 rounded">
                    ⚠️ CRÍTICO
                  </span>
                )}
              </div>
            </div>

            {/* Progress bar */}
            <div className="space-y-1">
              <div
                className="h-8 rounded-full overflow-hidden border"
                style={{
                  borderColor: carreraColor.border,
                  backgroundColor: carreraColor.bg,
                }}
              >
                <div
                  className="h-full transition-all duration-300"
                  style={{
                    width: `${percentage}%`,
                    backgroundColor: carreraColor.hex,
                  }}
                />
              </div>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-3 gap-2 text-xs">
              <div>
                <p style={{ color: colors.semantic.text.muted }}>Tutores</p>
                <p className="font-semibold">{carrera.totalTutores}</p>
              </div>
              <div>
                <p style={{ color: colors.semantic.text.muted }}>Alumnos</p>
                <p className="font-semibold">{carrera.totalAlumnos}</p>
              </div>
              <div>
                <p style={{ color: colors.semantic.text.muted }}>Promedio</p>
                <p className="font-semibold">{carrera.promedioAlumnos.toFixed(1)}</p>
              </div>
            </div>

            {/* Saturation badge */}
            <div className="flex items-center gap-1 text-xs">
              <span>{saturationColor.emoji}</span>
              <span style={{ color: saturationColor.text }}>
                {saturationColor.label}
              </span>
            </div>
          </div>
        );
      })}
    </div>
  );
};
