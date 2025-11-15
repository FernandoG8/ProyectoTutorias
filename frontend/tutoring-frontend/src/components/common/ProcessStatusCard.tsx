import { Check, AlertCircle, Clock } from "lucide-react";
import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";
import "dayjs/locale/es";
import { Badge } from "@/components/ui/Badge";
import { colors } from "@/constants/colors";

dayjs.extend(relativeTime);
dayjs.locale("es");

/**
 * ProcessStatusCard Component
 *
 * Displays assignment process status with:
 * - Current state badge
 * - Key metrics (processed, assigned, errors)
 * - Timeline info
 * - Status indicator icon
 *
 * Part of Week 3 Dashboard enhancements
 */

type ProcessState = "INICIADO" | "COMPARANDO" | "LIBERANDO_CUPOS" | "ASIGNANDO" | "COMPLETADO" | "FALLIDO";

interface ProcessStatusCardProps {
  id: number;
  estado: ProcessState;
  fechaInicio: string;
  fechaFin?: string;
  totalProcesados: number;
  totalAsignados: number;
  totalErrores: number;
  archivo?: string;
  usuario?: string;
  onClick?: () => void;
}

const estadoConfig: Record<
  ProcessState,
  { label: string; variant: "info" | "warning" | "success" | "danger"; icon: React.ReactNode }
> = {
  INICIADO: {
    label: "Iniciado",
    variant: "info",
    icon: <Clock className="h-5 w-5" />,
  },
  COMPARANDO: {
    label: "Comparando",
    variant: "info",
    icon: <Clock className="h-5 w-5" />,
  },
  LIBERANDO_CUPOS: {
    label: "Liberando cupos",
    variant: "warning",
    icon: <Clock className="h-5 w-5" />,
  },
  ASIGNANDO: {
    label: "Asignando",
    variant: "info",
    icon: <Clock className="h-5 w-5" />,
  },
  COMPLETADO: {
    label: "Completado",
    variant: "success",
    icon: <Check className="h-5 w-5" />,
  },
  FALLIDO: {
    label: "Error",
    variant: "danger",
    icon: <AlertCircle className="h-5 w-5" />,
  },
};

export const ProcessStatusCard = ({
  id,
  estado,
  fechaInicio,
  fechaFin,
  totalProcesados,
  totalAsignados,
  totalErrores,
  archivo,
  usuario,
  onClick,
}: ProcessStatusCardProps) => {
  const config = estadoConfig[estado];
  const startDate = dayjs(fechaInicio);
  const endDate = fechaFin ? dayjs(fechaFin) : null;
  const timeAgo = startDate.fromNow();

  return (
    <div
      className="rounded-lg border p-4 bg-white hover:shadow-md transition-all"
      style={{
        borderColor: colors.semantic.border,
        ...(onClick && { cursor: "pointer" }),
      }}
      onClick={onClick}
    >
      {/* Header */}
      <div className="flex items-start justify-between mb-3">
        <div className="flex items-center gap-2">
          <Badge variant={config.variant}>{config.label}</Badge>
          <p
            className="text-xs"
            style={{ color: colors.semantic.text.muted }}
          >
            #{id}
          </p>
        </div>
      </div>

      {/* Metadata */}
      {(archivo || usuario) && (
        <p
          className="text-xs mb-3"
          style={{ color: colors.semantic.text.secondary }}
        >
          {archivo && <span>📄 {archivo}</span>}
          {usuario && <span className="ml-3">👤 {usuario}</span>}
        </p>
      )}

      {/* Stats Grid */}
      <div className="grid grid-cols-3 gap-3 mb-3 pb-3 border-b" style={{ borderColor: colors.semantic.border }}>
        <div>
          <p className="text-xs" style={{ color: colors.semantic.text.muted }}>
            Procesados
          </p>
          <p
            className="text-sm font-semibold"
            style={{ color: colors.primary[600] }}
          >
            {totalProcesados}
          </p>
        </div>
        <div>
          <p className="text-xs" style={{ color: colors.semantic.text.muted }}>
            Asignados
          </p>
          <p
            className="text-sm font-semibold"
            style={{ color: colors.success[600] }}
          >
            {totalAsignados}
          </p>
        </div>
        <div>
          <p className="text-xs" style={{ color: colors.semantic.text.muted }}>
            Errores
          </p>
          <p
            className="text-sm font-semibold"
            style={{ color: colors.danger[600] }}
          >
            {totalErrores}
          </p>
        </div>
      </div>

      {/* Timeline */}
      <div className="space-y-1">
        <p
          className="text-xs"
          style={{ color: colors.semantic.text.muted }}
        >
          Inicio: {startDate.format("DD/MM/YYYY HH:mm")} ({timeAgo})
        </p>
        {endDate && (
          <p
            className="text-xs"
            style={{ color: colors.semantic.text.muted }}
          >
            Fin: {endDate.format("DD/MM/YYYY HH:mm")}
          </p>
        )}
      </div>
    </div>
  );
};
