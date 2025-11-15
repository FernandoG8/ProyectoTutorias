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
import { Card } from "@/components/ui/Card";
import { DataTable } from "@/components/ui/DataTable";
import { DashboardSkeleton } from "@/components/common/DashboardSkeleton";
import {
  getDashboardEstadisticas,
  getDistribucionTutores,
  getProcesosRecientes,
} from "@/services/dashboard-service";
import { listAssignmentProcesses } from "@/services/asignaciones-service";
import type { AssignmentProcessSummary, DistribucionTutor, ProcesoReciente } from "@/types";
import type { ColumnDef } from "@tanstack/react-table";

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
      <section className="grid gap-4 md:grid-cols-4">
        <Card>
          <p className="text-sm text-slate-500">Total de tutores activos</p>
          <p className="text-3xl font-semibold text-text">
            {estadisticas?.total_tutores ?? "--"}
          </p>
        </Card>
        <Card>
          <p className="text-sm text-slate-500">Alumnos asignados</p>
          <p className="text-3xl font-semibold text-text">
            {estadisticas?.alumnos_con_tutor ?? "--"}
          </p>
          {estadisticas && (
            <p className="mt-1 text-xs text-slate-500">
              {estadisticas.alumnos_sin_tutor} sin tutor
            </p>
          )}
        </Card>
        <Card>
          <p className="text-sm text-slate-500">Promedio por tutor</p>
          <p className="text-3xl font-semibold text-text">
            {estadisticas?.promedio_alumnos_por_tutor.toFixed(1) ?? "--"}
          </p>
        </Card>
        <Card>
          <p className="text-sm text-slate-500">Cobertura</p>
          <p className="text-3xl font-semibold text-text">
            {estadisticas?.porcentaje_cobertura.toFixed(1) ?? "--"}%
          </p>
        </Card>
      </section>

      <section className="grid gap-6 lg:grid-cols-5">
        <Card className="lg:col-span-3">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-lg font-semibold text-text">
                Tutores vs alumnos asignados
              </h2>
              <p className="text-sm text-slate-500">
                Distribución de tutorías por docente para el período actual.
              </p>
            </div>
          </div>
          <div className="mt-6 h-80">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#E2E8F0" />
                <XAxis dataKey="nombre" hide={chartData.length > 8} />
                <YAxis allowDecimals={false} />
                <Tooltip
                  cursor={{ fill: "rgba(49, 87, 98, 0.08)" }}
                  contentStyle={{ borderRadius: 12, borderColor: "#E2E8F0" }}
                />
                <Bar dataKey="alumnos" fill="#315762" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Card>

        <Card className="lg:col-span-2">
          <h2 className="text-lg font-semibold text-text">Procesos recientes</h2>
          <p className="text-sm text-slate-500">
            Seguimiento a los últimos procesos de asignación ejecutados.
          </p>
          <ul className="mt-4 space-y-3 text-sm text-slate-600">
            {procesosRecientes.map((proceso) => (
              <li key={proceso.id} className="rounded-lg bg-slate-100 px-3 py-2">
                <div className="flex items-center justify-between">
                  <span className="font-semibold">{proceso.estado}</span>
                  <span className="text-xs text-slate-500">
                    {new Date(proceso.fecha_inicio).toLocaleDateString()}
                  </span>
                </div>
                <p className="mt-1 text-xs text-slate-500">
                  Archivo: {proceso.archivo} · Usuario: {proceso.usuario}
                </p>
                <p className="mt-1 text-xs text-slate-500">
                  Procesados: {proceso.total_procesados} · Asignados: {proceso.total_asignados} · Errores: {proceso.total_errores}
                </p>
              </li>
            ))}
            {!procesosRecientes.length && (
              <li className="rounded-lg bg-slate-100 px-3 py-2">
                No se registran procesos recientes.
              </li>
            )}
          </ul>
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
