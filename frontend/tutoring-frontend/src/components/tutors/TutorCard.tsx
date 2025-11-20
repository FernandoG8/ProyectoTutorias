import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Eye, Edit2 } from "lucide-react";
import { colors } from "@/constants/colors";
import type { TutorResponse } from "@/types";
import { CARRERA_COLORS, SATURATION_COLORS, calculateSaturation, getSaturationState } from "@/utils/dashboard-utils";

interface TutorCardProps {
  tutor: TutorResponse;
  onViewStudents?: () => void;
  onEdit?: () => void;
}

/**
 * TutorCard Component
 *
 * Tarjeta profesional para mostrar tutor
 * - Nombre y carrera con color específico
 * - Carga actual / capacidad máxima
 * - Progreso visual de saturación
 * - Estado con emoji (crítico/alerta/normal/bajo)
 * - Acciones (ver alumnos, editar)
 */
export const TutorCard = ({
  tutor,
  onViewStudents,
  onEdit,
}: TutorCardProps) => {
  const carreraColor = CARRERA_COLORS[tutor.carrera as keyof typeof CARRERA_COLORS] || {
    hex: "#666",
    nombre: tutor.carrera,
    bg: "bg-gray-50",
    border: "border-gray-300",
    text: "text-gray-900",
  };

  const porcentajeSaturacion = calculateSaturation(
    tutor.cargaActual || 0,
    tutor.capacidadMax || 1,
  );
  const estado = getSaturationState(porcentajeSaturacion);
  const saturationColor = SATURATION_COLORS[estado];

  return (
    <Card className="hover:shadow-lg transition-shadow">
      <div className="space-y-4">
        {/* Header */}
        <div>
          <div className="flex items-start justify-between mb-2">
            <div className="flex-1">
              <h3
                className="text-lg font-semibold"
                style={{ color: colors.semantic.text.primary }}
              >
                {tutor.nombre}
              </h3>
              <div className="flex items-center gap-2 mt-1">
                <div
                  className="w-2 h-2 rounded-full"
                  style={{ backgroundColor: carreraColor.hex }}
                />
                <span className="text-xs" style={{ color: colors.semantic.text.muted }}>
                  {carreraColor.nombre}
                </span>
              </div>
            </div>
            <span
              className="px-2 py-1 rounded text-xs font-semibold"
              style={{
                backgroundColor: saturationColor.bg,
                color: saturationColor.text,
              }}
            >
              {saturationColor.emoji} {estado}
            </span>
          </div>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-3 gap-2 text-center">
          <div>
            <p className="text-2xl font-bold" style={{ color: carreraColor.hex }}>
              {tutor.cargaActual || 0}
            </p>
            <p className="text-xs" style={{ color: colors.semantic.text.muted }}>
              Alumnos
            </p>
          </div>
          <div>
            <p className="text-2xl font-bold" style={{ color: colors.primary[600] }}>
              {tutor.capacidadMax || 0}
            </p>
            <p className="text-xs" style={{ color: colors.semantic.text.muted }}>
              Capacidad
            </p>
          </div>
          <div>
            <p className="text-2xl font-bold" style={{ color: saturationColor.hex }}>
              {porcentajeSaturacion}%
            </p>
            <p className="text-xs" style={{ color: colors.semantic.text.muted }}>
              Saturación
            </p>
          </div>
        </div>

        {/* Progress bar */}
        <div
          className="h-3 rounded-full overflow-hidden border"
          style={{
            borderColor: saturationColor.border,
            backgroundColor: saturationColor.bg,
          }}
        >
          <div
            className="h-full transition-all duration-300"
            style={{
              width: `${Math.min(porcentajeSaturacion, 100)}%`,
              backgroundColor: saturationColor.hex,
            }}
          />
        </div>

        {/* Status text */}
        <div className="flex items-center justify-between">
          <p className="text-sm" style={{ color: saturationColor.text }}>
            {saturationColor.label}
          </p>
          <p className="text-xs" style={{ color: colors.semantic.text.muted }}>
            {tutor.capacidadDisponible || 0} disponibles
          </p>
        </div>

        {/* Actions */}
        {(onViewStudents || onEdit) && (
          <div className="flex gap-2 pt-2 border-t" style={{ borderColor: colors.semantic.border }}>
            {onViewStudents && (
              <Button
                size="sm"
                variant="secondary"
                icon={<Eye className="h-4 w-4" />}
                onClick={onViewStudents}
                fullWidth
              >
                Ver alumnos
              </Button>
            )}
            {onEdit && (
              <Button
                size="sm"
                variant="tertiary"
                icon={<Edit2 className="h-4 w-4" />}
                onClick={onEdit}
                fullWidth
              >
                Editar
              </Button>
            )}
          </div>
        )}
      </div>
    </Card>
  );
};
