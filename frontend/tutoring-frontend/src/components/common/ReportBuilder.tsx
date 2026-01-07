import { useState } from "react";
import { Download, Filter, RotateCcw } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { FormField } from "@/components/ui/FormField";
import { Badge } from "@/components/ui/Badge";
import { colors } from "@/constants/colors";

/**
 * ReportBuilder Component
 *
 * Advanced report configuration with:
 * - Multi-field filtering
 * - Format selection (PDF, Excel, CSV)
 * - Date range selection
 * - Preview before download
 * - Export scheduling
 *
 * Part of Week 4 advanced reporting
 *
 * Decision Log:
 * - Flexible filter architecture (add/remove filters dynamically)
 * - Multiple export formats
 * - Schedule exports for off-peak times
 * - Real-time field count tracking
 */

export interface ReportFilter {
  field: string;
  operator: "equals" | "contains" | "gte" | "lte" | "between";
  value: string | string[];
}

export interface ReportConfig {
  title: string;
  filters: ReportFilter[];
  format: "PDF" | "Excel" | "CSV";
  includeTimestamp: boolean;
  scheduleExport?: {
    enabled: boolean;
    time: string; // HH:MM format
    frequency: "once" | "daily" | "weekly" | "monthly";
  };
}

interface ReportBuilderProps {
  onGenerate: (config: ReportConfig) => Promise<void>;
  onExport: (config: ReportConfig) => Promise<Blob>;
  isLoading?: boolean;
  title: string;
  description?: string;
  availableFields?: Array<{
    name: string;
    label: string;
    type: "text" | "select" | "date" | "number";
    options?: Array<{ value: string; label: string }>;
  }>;
}

export const ReportBuilder = ({
  onGenerate,
  onExport,
  isLoading = false,
  title,
  description,
  availableFields = [],
}: ReportBuilderProps) => {
  const [reportTitle, setReportTitle] = useState(title);
  const [filters, setFilters] = useState<ReportFilter[]>([]);
  const [format, setFormat] = useState<"PDF" | "Excel" | "CSV">("Excel");
  const [includeTimestamp, setIncludeTimestamp] = useState(true);
  const [showSchedule, setShowSchedule] = useState(false);
  const [scheduleTime, setScheduleTime] = useState("02:00");
  const [scheduleFrequency, setScheduleFrequency] = useState<"once" | "daily" | "weekly" | "monthly">("once");

  const handleAddFilter = () => {
    setFilters([
      ...filters,
      {
        field: availableFields[0]?.name || "",
        operator: "equals",
        value: "",
      },
    ]);
  };

  const handleRemoveFilter = (index: number) => {
    setFilters(filters.filter((_, i) => i !== index));
  };

  const handleGenerateReport = async () => {
    const config: ReportConfig = {
      title: reportTitle,
      filters,
      format,
      includeTimestamp,
      scheduleExport: showSchedule
        ? { enabled: true, time: scheduleTime, frequency: scheduleFrequency }
        : undefined,
    };

    try {
      const blob = await onExport(config);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", `${reportTitle}-${Date.now()}.${format === "PDF" ? "pdf" : format === "Excel" ? "xlsx" : "csv"}`);
      document.body.appendChild(link);
      link.click();
      link.parentNode?.removeChild(link);
      window.URL.revokeObjectURL(url);

      await onGenerate(config);
    } catch (error) {
      console.error("Error generating report:", error);
    }
  };

  return (
    <Card>
      <div className="space-y-6">
        {/* Header */}
        <div>
          <h2
            className="text-lg font-semibold mb-1"
            style={{ color: colors.semantic.text.primary }}
          >
            {title}
          </h2>
          {description && (
            <p
              className="text-sm"
              style={{ color: colors.semantic.text.secondary }}
            >
              {description}
            </p>
          )}
        </div>

        {/* Report Title */}
        <FormField
          label="Nombre del reporte"
          value={reportTitle}
          onChange={(e) => setReportTitle(e.target.value)}
          placeholder="Ej: Reporte de Tutores - Nov 2025"
        />

        {/* Filters Section */}
        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <p
              className="text-sm font-semibold"
              style={{ color: colors.semantic.text.primary }}
            >
              Filtros ({filters.length})
            </p>
            <Button
              variant="ghost"
              onClick={handleAddFilter}
              type="button"
            >
              <Filter className="h-4 w-4 mr-1" />
              Agregar filtro
            </Button>
          </div>

          <div className="space-y-2">
            {filters.length === 0 ? (
              <p
                className="text-sm py-3 text-center"
                style={{ color: colors.semantic.text.muted }}
              >
                Sin filtros. El reporte incluirá todos los datos.
              </p>
            ) : (
              filters.map((filter, index) => (
                <div
                  key={index}
                  className="flex gap-2 p-3 rounded-lg border items-end"
                  style={{ borderColor: colors.semantic.border }}
                >
                  <div className="flex-1 space-y-1">
                    <label
                      className="text-xs font-medium"
                      style={{ color: colors.semantic.text.secondary }}
                    >
                      Campo
                    </label>
                    <select
                      value={filter.field}
                      onChange={(e) => {
                        const newFilters = [...filters];
                        newFilters[index].field = e.target.value;
                        setFilters(newFilters);
                      }}
                      className="w-full px-2 py-1.5 text-sm rounded border"
                      style={{ borderColor: colors.semantic.border }}
                    >
                      {availableFields.map((f) => (
                        <option key={f.name} value={f.name}>
                          {f.label}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="flex-1 space-y-1">
                    <label
                      className="text-xs font-medium"
                      style={{ color: colors.semantic.text.secondary }}
                    >
                      Operador
                    </label>
                    <select
                      value={filter.operator}
                      onChange={(e) => {
                        const newFilters = [...filters];
                        newFilters[index].operator = e.target.value as ReportFilter["operator"];
                        setFilters(newFilters);
                      }}
                      className="w-full px-2 py-1.5 text-sm rounded border"
                      style={{ borderColor: colors.semantic.border }}
                    >
                      <option value="equals">Es igual a</option>
                      <option value="contains">Contiene</option>
                      <option value="gte">Mayor o igual</option>
                      <option value="lte">Menor o igual</option>
                      <option value="between">Entre</option>
                    </select>
                  </div>

                  <div className="flex-1 space-y-1">
                    <label
                      className="text-xs font-medium"
                      style={{ color: colors.semantic.text.secondary }}
                    >
                      Valor
                    </label>
                    <input
                      type="text"
                      value={typeof filter.value === "string" ? filter.value : ""}
                      onChange={(e) => {
                        const newFilters = [...filters];
                        newFilters[index].value = e.target.value;
                        setFilters(newFilters);
                      }}
                      className="w-full px-2 py-1.5 text-sm rounded border"
                      style={{ borderColor: colors.semantic.border }}
                      placeholder="Ingresa el valor"
                    />
                  </div>

                  <button
                    onClick={() => handleRemoveFilter(index)}
                    className="px-3 py-1.5 text-sm font-medium rounded border text-red-600 hover:bg-red-50 transition-colors"
                    style={{ borderColor: colors.danger[200] }}
                    type="button"
                  >
                    Eliminar
                  </button>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Format Selection */}
        <div className="grid gap-4 md:grid-cols-3">
          <div className="space-y-2">
            <label
              className="text-sm font-semibold"
              style={{ color: colors.semantic.text.primary }}
            >
              Formato
            </label>
            <div className="flex gap-2">
              {["PDF", "Excel", "CSV"].map((fmt) => (
                <button
                  key={fmt}
                  onClick={() => setFormat(fmt as any)}
                  className="flex-1 px-3 py-2 rounded text-sm font-medium transition-colors border-2"
                  style={{
                    borderColor: format === fmt ? colors.primary[400] : colors.semantic.border,
                    backgroundColor: format === fmt ? colors.primary[50] : "transparent",
                    color: format === fmt ? colors.primary[700] : colors.semantic.text.primary,
                  }}
                  type="button"
                >
                  {fmt}
                </button>
              ))}
            </div>
          </div>

          <div className="space-y-2">
            <label
              className="text-sm font-semibold flex items-center gap-2"
              style={{ color: colors.semantic.text.primary }}
            >
              <input
                type="checkbox"
                checked={includeTimestamp}
                onChange={(e) => setIncludeTimestamp(e.target.checked)}
                className="rounded"
              />
              Incluir fecha/hora
            </label>
            <p
              className="text-xs"
              style={{ color: colors.semantic.text.muted }}
            >
              Agrega timestamp al reporte
            </p>
          </div>

          <div className="space-y-2">
            <label
              className="text-sm font-semibold flex items-center gap-2"
              style={{ color: colors.semantic.text.primary }}
            >
              <input
                type="checkbox"
                checked={showSchedule}
                onChange={(e) => setShowSchedule(e.target.checked)}
                className="rounded"
              />
              Programar exportación
            </label>
            <p
              className="text-xs"
              style={{ color: colors.semantic.text.muted }}
            >
              Ejecutar en horario específico
            </p>
          </div>
        </div>

        {/* Schedule Options */}
        {showSchedule && (
          <div
            className="rounded-lg border p-4"
            style={{ borderColor: colors.info[200], backgroundColor: colors.info[50] }}
          >
            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-1">
                <label
                  className="text-sm font-semibold"
                  style={{ color: colors.info[900] }}
                >
                  Hora de ejecución
                </label>
                <input
                  type="time"
                  value={scheduleTime}
                  onChange={(e) => setScheduleTime(e.target.value)}
                  className="w-full px-3 py-2 rounded border text-sm"
                  style={{ borderColor: colors.info[300] }}
                />
              </div>
              <div className="space-y-1">
                <label
                  className="text-sm font-semibold"
                  style={{ color: colors.info[900] }}
                >
                  Frecuencia
                </label>
                <select
                  value={scheduleFrequency}
                  onChange={(e) => setScheduleFrequency(e.target.value as any)}
                  className="w-full px-3 py-2 rounded border text-sm"
                  style={{ borderColor: colors.info[300] }}
                >
                  <option value="once">Una vez</option>
                  <option value="daily">Diariamente</option>
                  <option value="weekly">Semanalmente</option>
                  <option value="monthly">Mensualmente</option>
                </select>
              </div>
            </div>
          </div>
        )}

        {/* Summary */}
        <div
          className="rounded-lg border p-3 bg-white"
          style={{ borderColor: colors.semantic.border }}
        >
          <p
            className="text-xs font-medium mb-2"
            style={{ color: colors.semantic.text.secondary }}
          >
            Resumen del reporte:
          </p>
          <div className="flex flex-wrap gap-2">
            <Badge variant="info">{format}</Badge>
            {filters.length > 0 && (
              <Badge variant="warning">{filters.length} filtros</Badge>
            )}
            {includeTimestamp && (
              <Badge variant="success">Con timestamp</Badge>
            )}
            {showSchedule && (
              <Badge variant="info">
                {scheduleFrequency} a las {scheduleTime}
              </Badge>
            )}
          </div>
        </div>

        {/* Actions */}
        <div className="flex gap-3 pt-4 border-t" style={{ borderColor: colors.semantic.border }}>
          <button
            onClick={() => {
              setReportTitle(title);
              setFilters([]);
              setFormat("Excel");
              setIncludeTimestamp(true);
              setShowSchedule(false);
            }}
            className="px-4 py-2 rounded-lg border flex items-center gap-2 transition-colors hover:bg-gray-50"
            style={{
              borderColor: colors.semantic.border,
              color: colors.semantic.text.primary,
            }}
            type="button"
          >
            <RotateCcw className="h-4 w-4" />
            Restablecer
          </button>
          <Button
            onClick={handleGenerateReport}
            loading={isLoading}
            disabled={isLoading}
            className="flex items-center gap-2 flex-1"
            style={{ backgroundColor: colors.primary[400] }}
          >
            <Download className="h-4 w-4" />
            Generar y descargar
          </Button>
        </div>
      </div>
    </Card>
  );
};
