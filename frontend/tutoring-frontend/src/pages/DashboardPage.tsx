import { useMemo } from "react";
import { useQuery } from "@tanstack/react-query";
import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { Users, GraduationCap, TrendingUp, AlertCircle } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { DataTable } from "@/components/ui/DataTable";
import { DashboardSkeleton } from "@/components/common/DashboardSkeleton";
// import { StatCard } from "@/components/common/StatCard";
import { ProcessStatusCard } from "@/components/common/ProcessStatusCard";
import { MetricsCard } from "@/components/common/MetricsCard";
import {
  getDashboardEstadisticas,
  getDistribucionTutores,
  getProcesosRecientes,
} from "@/services/dashboard-service";
import { listAssignmentProcesses } from "@/services/asignaciones-service";
import type { AssignmentProcessSummary, DistribucionTutor, ProcesoReciente } from "@/types";
import type { ColumnDef } from "@tanstack/react-table";
import { colors } from "@/constants/colors";

const processColumns: ColumnDef<AssignmentProcessSummary>[] = [
  { header: "ID", accessorKey: "id" },
  {
    header: "Estado",
    accessorKey: "estado",
    cell: ({ getValue }) => (
      <span className="rounded-full bg-primary/10 px-2 py-1 text-xs font-semibold text-primary">
        {String(getValue())}
      </span>
    ),
  },
  {
    header: "Inicio",
    accessorKey: "fechaInicio",
  },
  {
    header: "Fin",
    accessorKey: "fechaFin",
    cell: ({ getValue }) => getValue() ?? "En curso",
  },
  {
    header: "Archivo",
    accessorKey: "archivoOrigen",
    cell: ({ getValue }) => getValue() ?? "-",
  },
  {
    header: "Usuario",
    accessorKey: "usuarioEjecutor",
    cell: ({ getValue }) => getValue() ?? "-",
  },
  {
    header: "Asignados",
    accessorKey: "totalAsignados",
  },
  {
    header: "Errores",
    accessorKey: "totalErrores",
  },
];

export const DashboardPage = () => {
  const {
    data: estadisticas,
    isLoading: estadisticasLoading,
  } = useQuery({
    queryKey: ["dashboard-estadisticas"],
    queryFn: () => getDashboardEstadisticas(),
  });

  const {
    data: distribucionTutores = [],
    isLoading: distribucionLoading,
  } = useQuery<DistribucionTutor[]>({
    queryKey: ["dashboard-distribucion-tutores"],
    queryFn: () => getDistribucionTutores(),
  });

  const {
    data: procesosRecientes = [],
    isLoading: procesosLoading,
  } = useQuery<ProcesoReciente[]>({
    queryKey: ["dashboard-procesos-recientes"],
    queryFn: () => getProcesosRecientes(5),
  });

  const {
    data: allProcesses = [],
    isLoading: allProcessesLoading,
  } = useQuery<AssignmentProcessSummary[]>({
    queryKey: ["assignment-processes"],
    queryFn: () => listAssignmentProcesses(),
  });

  const chartData = useMemo(
    () =>
      distribucionTutores.map((tutor) => ({
        nombre: tutor.tutor_nombre,
        alumnos: tutor.alumnos_asignados,
      })),
    [distribucionTutores],
  );

  const isLoading = estadisticasLoading || distribucionLoading || procesosLoading;

  if (isLoading) {
    return <DashboardSkeleton />;
  }

  return (
    <div className="space-y-6">
      {/* Key Metrics Section */}
      <section className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <MetricsCard
          label="Tutores Activos"
          value={estadisticas?.total_tutores ?? "--"}
          color="primary"
          icon={<Users className="h-5 w-5" />}
          isLoading={estadisticasLoading}
        />
        <MetricsCard
          label="Alumnos Asignados"
          value={estadisticas?.alumnos_con_tutor ?? "--"}
          color="success"
          icon={<GraduationCap className="h-5 w-5" />}
          subtitle={
            estadisticas
              ? `${estadisticas.alumnos_sin_tutor} sin tutor`
              : undefined
          }
          isLoading={estadisticasLoading}
        />
        <MetricsCard
          label="Promedio por Tutor"
          value={estadisticas?.promedio_alumnos_por_tutor?.toFixed(1) ?? "--"}
          color="info"
          icon={<TrendingUp className="h-5 w-5" />}
          isLoading={estadisticasLoading}
        />
        <MetricsCard
          label="Cobertura"
          value={estadisticas?.porcentaje_cobertura?.toFixed(1) ?? "--"}
          unit="%"
          color="warning"
          icon={<AlertCircle className="h-5 w-5" />}
          isLoading={estadisticasLoading}
          trend={
            estadisticas &&
            estadisticas.porcentaje_cobertura >= 85
              ? "up"
              : "down"
          }
          trendValue={
            estadisticas &&
            estadisticas.porcentaje_cobertura >= 85
              ? "Meta alcanzada"
              : "Necesita mejora"
          }
        />
      </section>

      <section className="grid gap-6 lg:grid-cols-5">
        <Card className="lg:col-span-3">
          <div className="mb-6">
            <h2
              className="text-lg font-semibold mb-1"
              style={{ color: colors.semantic.text.primary }}
            >
              Distribución de Tutores
            </h2>
            <p
              className="text-sm"
              style={{ color: colors.semantic.text.secondary }}
            >
              Alumnos asignados por docente en el período actual.
            </p>
          </div>
          <div className="h-80 -mx-4">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart
                data={chartData}
                margin={{ top: 20, right: 30, left: 0, bottom: 50 }}
              >
                <CartesianGrid
                  strokeDasharray="3 3"
                  stroke={colors.semantic.border}
                />
                <XAxis
                  dataKey="nombre"
                  hide={chartData.length > 8}
                  stroke={colors.semantic.text.muted}
                />
                <YAxis
                  allowDecimals={false}
                  stroke={colors.semantic.text.muted}
                />
                <Tooltip
                  cursor={{ fill: colors.primary[100] }}
                  contentStyle={{
                    borderRadius: 8,
                    border: `1px solid ${colors.semantic.border}`,
                    backgroundColor: "white",
                  }}
                />
                <Bar
                  dataKey="alumnos"
                  fill={colors.primary[400]}
                  radius={[8, 8, 0, 0]}
                />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Card>

        <Card className="lg:col-span-2">
          <h2
            className="text-lg font-semibold mb-1"
            style={{ color: colors.semantic.text.primary }}
          >
            Procesos recientes
          </h2>
          <p
            className="text-sm mb-4"
            style={{ color: colors.semantic.text.secondary }}
          >
            Seguimiento a los últimos procesos de asignación ejecutados.
          </p>
          <div className="space-y-3 max-h-96 overflow-y-auto">
            {procesosRecientes.length > 0 ? (
              procesosRecientes.map((proceso) => (
                <ProcessStatusCard
                  key={proceso.id}
                  id={proceso.id}
                  estado={proceso.estado as any}
                  fechaInicio={proceso.fecha_inicio}
                  fechaFin={proceso.fecha_fin ?? undefined}
                  totalProcesados={proceso.total_procesados}
                  totalAsignados={proceso.total_asignados}
                  totalErrores={proceso.total_errores}
                  archivo={proceso.archivo}
                  usuario={proceso.usuario}
                />
              ))
            ) : (
              <p
                className="text-sm py-4 text-center"
                style={{ color: colors.semantic.text.muted }}
              >
                No se registran procesos recientes
              </p>
            )}
          </div>
        </Card>
      </section>

      <section className="space-y-3">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-semibold text-text">
            Historial de procesos de asignación
          </h2>
          <p className="text-sm text-slate-500">
            Consulta el resultado de las ejecuciones realizadas.
          </p>
        </div>
        <DataTable
          columns={processColumns}
          data={allProcesses}
          isLoading={allProcessesLoading}
          emptyMessage="Aún no se han registrado procesos."
        />
      </section>
    </div>
  );
};
