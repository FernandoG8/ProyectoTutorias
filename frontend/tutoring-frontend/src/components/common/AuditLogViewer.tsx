import { useState, useMemo } from "react";
import { Search, Filter, Download, Calendar, User, Clock } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Select } from "@/components/ui/Select";
import { Badge } from "@/components/ui/Badge";
import { colors } from "@/constants/colors";

/**
 * AuditLogViewer Component
 *
 * Comprehensive audit logging UI for compliance with:
 * - Searchable/filterable log entries
 * - Date range filtering
 * - User action history
 * - Entity change tracking
 * - Severity indicators
 * - Export capabilities
 * - Pagination
 *
 * Part of Week 4 advanced features
 *
 * Decision Log:
 * - Detailed log entry display with timestamps
 * - Color-coded severity levels
 * - Multiple filter dimensions (user, action, entity, date)
 * - Export to CSV for compliance documentation
 * - Pagination for large datasets
 */

export interface AuditLogEntry {
  id: number;
  timestamp: string;
  user: {
    id: number;
    name: string;
    email: string;
  };
  action: "CREATE" | "UPDATE" | "DELETE" | "VIEW" | "EXPORT" | "LOGIN" | "LOGOUT";
  entityType: string;
  entityId?: number;
  entityName?: string;
  oldValue?: Record<string, any>;
  newValue?: Record<string, any>;
  ipAddress?: string;
  userAgent?: string;
  severity: "info" | "warning" | "critical";
  description?: string;
  status: "success" | "failure";
  errorMessage?: string;
}

interface AuditLogViewerProps {
  logs?: AuditLogEntry[];
  isLoading?: boolean;
  onExport?: (logs: AuditLogEntry[]) => Promise<void>;
  pageSize?: number;
}

export const AuditLogViewer = ({
  logs = [],
  isLoading = false,
  onExport,
  pageSize = 25,
}: AuditLogViewerProps) => {
  const [page, setPage] = useState(1);
  const [searchTerm, setSearchTerm] = useState("");
  const [filterAction, setFilterAction] = useState<string>("ALL");
  const [filterUser, setFilterUser] = useState<string>("ALL");
  const [filterSeverity, setFilterSeverity] = useState<string>("ALL");
  const [filterStatus, setFilterStatus] = useState<string>("ALL");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");

  // Get unique values for filters
  const uniqueUsers = useMemo(() => {
    const users = new Set(logs.map((log) => log.user.name));
    return Array.from(users).sort();
  }, [logs]);

  const uniqueActions = useMemo(() => {
    const actions = new Set(logs.map((log) => log.action));
    return Array.from(actions).sort();
  }, [logs]);

  // Filter logs
  const filteredLogs = useMemo(() => {
    return logs.filter((log) => {
      // Search filter
      if (searchTerm) {
        const term = searchTerm.toLowerCase();
        const matchesSearch =
          log.user.name.toLowerCase().includes(term) ||
          log.user.email.toLowerCase().includes(term) ||
          log.entityName?.toLowerCase().includes(term) ||
          log.entityType.toLowerCase().includes(term) ||
          log.description?.toLowerCase().includes(term) ||
          log.ipAddress?.includes(term);
        if (!matchesSearch) return false;
      }

      // Action filter
      if (filterAction !== "ALL" && log.action !== filterAction) return false;

      // User filter
      if (filterUser !== "ALL" && log.user.name !== filterUser) return false;

      // Severity filter
      if (filterSeverity !== "ALL" && log.severity !== filterSeverity) return false;

      // Status filter
      if (filterStatus !== "ALL" && log.status !== filterStatus) return false;

      // Date range filter
      const logDate = new Date(log.timestamp);
      if (startDate) {
        const start = new Date(startDate);
        if (logDate < start) return false;
      }
      if (endDate) {
        const end = new Date(endDate);
        end.setHours(23, 59, 59, 999); // Include the whole end day
        if (logDate > end) return false;
      }

      return true;
    });
  }, [logs, searchTerm, filterAction, filterUser, filterSeverity, filterStatus, startDate, endDate]);

  // Pagination
  const totalPages = Math.ceil(filteredLogs.length / pageSize);
  const paginatedLogs = useMemo(() => {
    const start = (page - 1) * pageSize;
    return filteredLogs.slice(start, start + pageSize);
  }, [filteredLogs, page, pageSize]);

  const getSeverityColor = (severity: string): string => {
    switch (severity) {
      case "critical":
        return colors.danger[500];
      case "warning":
        return colors.warning[500];
      default:
        return colors.info[500];
    }
  };

  const getSeverityBgColor = (severity: string): string => {
    switch (severity) {
      case "critical":
        return colors.danger[50];
      case "warning":
        return colors.warning[50];
      default:
        return colors.info[50];
    }
  };

  const getActionLabel = (action: string): string => {
    const labels: Record<string, string> = {
      CREATE: "Creado",
      UPDATE: "Actualizado",
      DELETE: "Eliminado",
      VIEW: "Visualizado",
      EXPORT: "Exportado",
      LOGIN: "Inicio de sesión",
      LOGOUT: "Cierre de sesión",
    };
    return labels[action] || action;
  };

  const handleResetFilters = () => {
    setSearchTerm("");
    setFilterAction("ALL");
    setFilterUser("ALL");
    setFilterSeverity("ALL");
    setFilterStatus("ALL");
    setStartDate("");
    setEndDate("");
    setPage(1);
  };

  const handleExport = async () => {
    if (onExport) {
      try {
        await onExport(filteredLogs);
      } catch (error) {
        console.error("Error exporting logs:", error);
      }
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h2
          className="text-2xl font-bold mb-1"
          style={{ color: colors.semantic.text.primary }}
        >
          Registro de Auditoría
        </h2>
        <p
          className="text-sm"
          style={{ color: colors.semantic.text.secondary }}
        >
          Historial completo de acciones y cambios en el sistema para fines de cumplimiento.
        </p>
      </div>

      {/* Filters Card */}
      <Card>
        <div className="space-y-4">
          {/* Search Bar */}
          <div>
            <label
              className="text-sm font-semibold mb-2 block"
              style={{ color: colors.semantic.text.primary }}
            >
              Buscar en logs
            </label>
            <div className="relative">
              <Search
                className="absolute left-3 top-3 h-5 w-5"
                style={{ color: colors.semantic.text.muted }}
              />
              <Input
                placeholder="Usuario, entidad, dirección IP..."
                value={searchTerm}
                onChange={(e) => {
                  setSearchTerm(e.target.value);
                  setPage(1);
                }}
                className="pl-10"
              />
            </div>
            <p
              className="text-xs mt-1"
              style={{ color: colors.semantic.text.muted }}
            >
              Busca por usuario, nombre de entidad, IP o descripción
            </p>
          </div>

          {/* Filter Grid */}
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-5">
            {/* Action Filter */}
            <div className="space-y-1">
              <label
                className="text-sm font-medium"
                style={{ color: colors.semantic.text.primary }}
              >
                Acción
              </label>
              <Select
                value={filterAction}
                onChange={(e) => {
                  setFilterAction(e.target.value);
                  setPage(1);
                }}
              >
                <option value="ALL">Todas las acciones</option>
                {uniqueActions.map((action) => (
                  <option key={action} value={action}>
                    {getActionLabel(action)}
                  </option>
                ))}
              </Select>
            </div>

            {/* User Filter */}
            <div className="space-y-1">
              <label
                className="text-sm font-medium"
                style={{ color: colors.semantic.text.primary }}
              >
                Usuario
              </label>
              <Select
                value={filterUser}
                onChange={(e) => {
                  setFilterUser(e.target.value);
                  setPage(1);
                }}
              >
                <option value="ALL">Todos los usuarios</option>
                {uniqueUsers.map((user) => (
                  <option key={user} value={user}>
                    {user}
                  </option>
                ))}
              </Select>
            </div>

            {/* Severity Filter */}
            <div className="space-y-1">
              <label
                className="text-sm font-medium"
                style={{ color: colors.semantic.text.primary }}
              >
                Severidad
              </label>
              <Select
                value={filterSeverity}
                onChange={(e) => {
                  setFilterSeverity(e.target.value);
                  setPage(1);
                }}
              >
                <option value="ALL">Todas</option>
                <option value="info">Información</option>
                <option value="warning">Advertencia</option>
                <option value="critical">Crítica</option>
              </Select>
            </div>

            {/* Status Filter */}
            <div className="space-y-1">
              <label
                className="text-sm font-medium"
                style={{ color: colors.semantic.text.primary }}
              >
                Estado
              </label>
              <Select
                value={filterStatus}
                onChange={(e) => {
                  setFilterStatus(e.target.value);
                  setPage(1);
                }}
              >
                <option value="ALL">Todos</option>
                <option value="success">Exitoso</option>
                <option value="failure">Fallido</option>
              </Select>
            </div>

            {/* Reset Button */}
            <div className="flex items-end">
              <Button
                variant="ghost"
                onClick={handleResetFilters}
                className="w-full"
              >
                <Filter className="h-4 w-4 mr-1" />
                Limpiar
              </Button>
            </div>
          </div>

          {/* Date Range */}
          <div className="grid gap-4 md:grid-cols-3">
            <div className="space-y-1">
              <label
                className="text-sm font-medium flex items-center gap-2"
                style={{ color: colors.semantic.text.primary }}
              >
                <Calendar className="h-4 w-4" />
                Desde
              </label>
              <Input
                type="date"
                value={startDate}
                onChange={(e) => {
                  setStartDate(e.target.value);
                  setPage(1);
                }}
              />
            </div>
            <div className="space-y-1">
              <label
                className="text-sm font-medium flex items-center gap-2"
                style={{ color: colors.semantic.text.primary }}
              >
                <Calendar className="h-4 w-4" />
                Hasta
              </label>
              <Input
                type="date"
                value={endDate}
                onChange={(e) => {
                  setEndDate(e.target.value);
                  setPage(1);
                }}
              />
            </div>
            {onExport && (
              <div className="flex items-end">
                <Button
                  onClick={handleExport}
                  className="w-full gap-2"
                  style={{ backgroundColor: colors.success[400] }}
                >
                  <Download className="h-4 w-4" />
                  Exportar CSV
                </Button>
              </div>
            )}
          </div>

          {/* Results Summary */}
          <div
            className="rounded-lg border p-3"
            style={{
              borderColor: colors.semantic.border,
              backgroundColor: colors.semantic.background,
            }}
          >
            <p
              className="text-sm font-medium"
              style={{ color: colors.semantic.text.primary }}
            >
              {filteredLogs.length} registros encontrados
              {filterAction !== "ALL" || filterUser !== "ALL" || filterSeverity !== "ALL" || filterStatus !== "ALL" ? " (filtrado)" : ""}
            </p>
          </div>
        </div>
      </Card>

      {/* Logs Table */}
      <Card>
        {isLoading ? (
          <div className="text-center py-8">
            <p
              className="text-sm"
              style={{ color: colors.semantic.text.muted }}
            >
              Cargando registros...
            </p>
          </div>
        ) : paginatedLogs.length === 0 ? (
          <div className="text-center py-8">
            <p
              className="text-sm"
              style={{ color: colors.semantic.text.muted }}
            >
              No se encontraron registros que coincidan con los filtros aplicados.
            </p>
          </div>
        ) : (
          <div className="space-y-3">
            {paginatedLogs.map((log) => (
              <div
                key={log.id}
                className="rounded-lg border p-4"
                style={{
                  borderColor: colors.semantic.border,
                  backgroundColor: getSeverityBgColor(log.severity),
                }}
              >
                <div className="flex items-start justify-between gap-4">
                  {/* Main Info */}
                  <div className="flex-1 space-y-2">
                    {/* Header Row */}
                    <div className="flex items-center gap-2 flex-wrap">
                      <User className="h-4 w-4" style={{ color: colors.semantic.text.muted }} />
                      <p className="font-semibold" style={{ color: colors.semantic.text.primary }}>
                        {log.user.name}
                      </p>
                      <Badge variant={log.action === "DELETE" ? "danger" : log.action === "CREATE" ? "success" : "info"}>
                        {getActionLabel(log.action)}
                      </Badge>
                      <Badge variant={log.status === "success" ? "success" : "danger"}>
                        {log.status === "success" ? "✓ Exitoso" : "✗ Fallido"}
                      </Badge>
                      <Badge
                        variant={
                          log.severity === "critical"
                            ? "danger"
                            : log.severity === "warning"
                              ? "warning"
                              : "info"
                        }
                      >
                        {log.severity.toUpperCase()}
                      </Badge>
                    </div>

                    {/* Entity Info */}
                    <p
                      className="text-sm"
                      style={{ color: colors.semantic.text.secondary }}
                    >
                      {log.entityType}
                      {log.entityName && ` • ${log.entityName}`}
                    </p>

                    {/* Description */}
                    {log.description && (
                      <p
                        className="text-sm"
                        style={{ color: colors.semantic.text.primary }}
                      >
                        {log.description}
                      </p>
                    )}

                    {/* Error Message */}
                    {log.errorMessage && (
                      <p className="text-xs p-2 rounded" style={{ color: colors.danger[700], backgroundColor: colors.danger[50] }}>
                        Error: {log.errorMessage}
                      </p>
                    )}

                    {/* Changes Preview */}
                    {(log.oldValue || log.newValue) && (
                      <div className="text-xs space-y-1 mt-2">
                        {Object.keys({ ...log.oldValue, ...log.newValue }).map((key) => (
                          <div key={key} className="flex gap-2">
                            <span style={{ color: colors.semantic.text.secondary }}>{key}:</span>
                            {log.oldValue?.[key] && (
                              <span style={{ color: colors.danger[600] }}>
                                {String(log.oldValue[key])} →
                              </span>
                            )}
                            {log.newValue?.[key] && (
                              <span style={{ color: colors.success[600] }}>
                                {String(log.newValue[key])}
                              </span>
                            )}
                          </div>
                        ))}
                      </div>
                    )}

                    {/* Metadata */}
                    <div className="flex flex-wrap gap-4 text-xs mt-2">
                      <div className="flex items-center gap-1">
                        <Clock className="h-3 w-3" />
                        <span style={{ color: colors.semantic.text.muted }}>
                          {new Date(log.timestamp).toLocaleString()}
                        </span>
                      </div>
                      {log.ipAddress && (
                        <span
                          className="font-mono"
                          style={{ color: colors.semantic.text.muted }}
                        >
                          IP: {log.ipAddress}
                        </span>
                      )}
                    </div>
                  </div>

                  {/* Severity Indicator */}
                  <div
                    className="h-12 w-1 rounded-full"
                    style={{ backgroundColor: getSeverityColor(log.severity) }}
                  />
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>

      {/* Pagination */}
      {totalPages > 1 && (
        <Card>
          <div className="flex items-center justify-between">
            <p
              className="text-sm"
              style={{ color: colors.semantic.text.secondary }}
            >
              Página {page} de {totalPages}
            </p>
            <div className="flex gap-2">
              <Button
                variant="ghost"
                onClick={() => setPage(Math.max(1, page - 1))}
                disabled={page === 1}
              >
                ← Anterior
              </Button>
              <Button
                variant="ghost"
                onClick={() => setPage(Math.min(totalPages, page + 1))}
                disabled={page === totalPages}
              >
                Siguiente →
              </Button>
            </div>
          </div>
        </Card>
      )}
    </div>
  );
};
