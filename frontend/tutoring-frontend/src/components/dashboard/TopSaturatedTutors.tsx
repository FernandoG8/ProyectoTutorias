import { Link } from "react-router-dom";
import type { TutorSaturation } from "@/types/dashboard";
import { CARRERA_COLORS, SATURATION_COLORS } from "@/utils/dashboard-utils";
import { colors } from "@/constants/colors";
import { ChevronRight } from "lucide-react";

interface TopSaturatedTutorsProps {
  data: TutorSaturation[];
  isLoading?: boolean;
}

/**
 * TopSaturatedTutors Component
 *
 * Shows top 10 tutors with highest student load (saturation)
 *
 * Features:
 * - Rank badge (1-10)
 * - Tutor name with carrera color indicator
 * - Progress bar showing saturation percentage
 * - Saturation state badge with emoji
 * - Click to view tutor details
 * - "Ver todos" link to full tutores page
 */
export const TopSaturatedTutors = ({
  data,
  isLoading,
}: TopSaturatedTutorsProps) => {
  if (isLoading) {
    return (
      <div className="space-y-3">
        {[1, 2, 3, 4, 5].map((i) => (
          <div key={i} className="space-y-2 pb-3 border-b">
            <div className="h-4 bg-gray-200 rounded w-1/2 animate-pulse" />
            <div className="h-6 bg-gray-100 rounded animate-pulse" />
          </div>
        ))}
      </div>
    );
  }

  if (data.length === 0) {
    return (
      <div className="text-center py-8">
        <p style={{ color: colors.semantic.text.muted }}>
          No hay datos de saturación disponibles
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-1">
      {data.slice(0, 10).map((tutor, index) => {
        const carreraColor = CARRERA_COLORS[tutor.carrera];
        const saturationColor = SATURATION_COLORS[tutor.estado];
        const rank = index + 1;

        return (
          <div
            key={tutor.id}
            className="p-3 rounded-lg hover:bg-gray-50 transition-colors"
          >
            {/* Header with rank and name */}
            <div className="flex items-start gap-3 mb-2">
              {/* Rank badge */}
              <div
                className="flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center font-bold text-sm text-white"
                style={{ backgroundColor: carreraColor.hex }}
              >
                {rank}
              </div>

              {/* Tutor info */}
              <div className="flex-1 min-w-0">
                <p className="text-sm font-semibold" style={{ color: colors.semantic.text.primary }}>
                  {tutor.nombre}
                </p>
                <p className="text-xs" style={{ color: colors.semantic.text.muted }}>
                  {carreraColor.nombre}
                </p>
              </div>

              {/* Saturation badge */}
              <div className="flex-shrink-0">
                <span className="text-sm font-semibold" style={{ color: saturationColor.hex }}>
                  {saturationColor.emoji}
                </span>
              </div>
            </div>

            {/* Saturation info */}
            <div className="flex items-center justify-between gap-2 mb-2 text-xs">
              <span style={{ color: colors.semantic.text.muted }}>
                {tutor.alumnosActuales} / {tutor.capacidadMaxima}
              </span>
              <span
                className="font-semibold"
                style={{ color: saturationColor.hex }}
              >
                {tutor.porcentajeSaturacion}%
              </span>
            </div>

            {/* Progress bar */}
            <div
              className="h-2 rounded-full overflow-hidden border"
              style={{
                borderColor: saturationColor.border,
                backgroundColor: saturationColor.bg,
              }}
            >
              <div
                className="h-full transition-all duration-300"
                style={{
                  width: `${Math.min(tutor.porcentajeSaturacion, 100)}%`,
                  backgroundColor: saturationColor.hex,
                }}
              />
            </div>

            {/* State label */}
            <div className="mt-2 text-xs font-medium" style={{ color: saturationColor.text }}>
              {saturationColor.label}
            </div>
          </div>
        );
      })}

      {/* "Ver todos" link */}
      <Link
        to="/tutores"
        className="flex items-center justify-center gap-2 mt-4 py-2 rounded-lg hover:bg-gray-50 transition-colors text-sm font-medium"
        style={{ color: colors.primary[600] }}
      >
        Ver todos los tutores
        <ChevronRight className="h-4 w-4" />
      </Link>
    </div>
  );
};
