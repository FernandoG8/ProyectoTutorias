import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { colors } from "@/constants/colors";
import { FileText, BarChart3, Users } from "lucide-react";

export const REPORT_TYPES = [
  {
    id: "distribution",
    label: "Distribución de Tutores",
    description: "Análisis de carga de alumnos por tutor",
    icon: <BarChart3 className="h-6 w-6" />,
  },
  {
    id: "saturation",
    label: "Análisis de Saturación",
    description: "Tutores con mayor carga y alertas",
    icon: <Users className="h-6 w-6" />,
  },
  {
    id: "coverage",
    label: "Cobertura de Alumnos",
    description: "Alumnos asignados vs sin tutor",
    icon: <FileText className="h-6 w-6" />,
  },
] as const;

interface ReportSelectorProps {
  onSelectReport: (reportId: string) => void;
  selectedReport?: string;
}

/**
 * ReportSelector Component
 *
 * Selector de reportes disponibles
 * Tipo tarjeta con iconos y descripción
 */
export const ReportSelector = ({
  onSelectReport,
  selectedReport,
}: ReportSelectorProps) => {
  return (
    <div className="grid gap-4 md:grid-cols-3">
      {REPORT_TYPES.map((report) => (
        <Card
          key={report.id}
          className={`cursor-pointer transition-all hover:shadow-lg ${
            selectedReport === report.id ? "ring-2" : ""
          }`}
          onClick={() => onSelectReport(report.id)}
        >
          <div className="space-y-3">
            <div
              className="w-12 h-12 rounded-lg flex items-center justify-center"
              style={{ backgroundColor: colors.primary[50], color: colors.primary[600] }}
            >
              {report.icon}
            </div>
            <div>
              <h3
                className="font-semibold text-sm"
                style={{ color: colors.semantic.text.primary }}
              >
                {report.label}
              </h3>
              <p
                className="text-xs mt-1"
                style={{ color: colors.semantic.text.muted }}
              >
                {report.description}
              </p>
            </div>
            <Button
              size="sm"
              variant={selectedReport === report.id ? "primary" : "secondary"}
              fullWidth
            >
              {selectedReport === report.id ? "Seleccionado" : "Seleccionar"}
            </Button>
          </div>
        </Card>
      ))}
    </div>
  );
};
