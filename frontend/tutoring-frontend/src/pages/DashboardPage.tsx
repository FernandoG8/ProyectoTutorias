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
import { listAssignmentProcesses } from "@/services/asignaciones-service";
import { listTutors } from "@/services/tutors-service";
import type { AssignmentProcessSummary, TutorResponse } from "@/types";
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
    data: tutors = [],
    isLoading: tutorsLoading,
  } = useQuery<TutorResponse[]>({
    queryKey: ["tutors"],
    queryFn: () => listTutors(),
  });

  const {
    data: processes = [],
    isLoading: processesLoading,
  } = useQuery<AssignmentProcessSummary[]>({
    queryKey: ["assignment-processes"],
    queryFn: () => listAssignmentProcesses(),
  });

  const totalStudents = useMemo<number>(
    () => tutors.reduce((acc, tutor) => acc + tutor.cargaActual, 0),
    [tutors],
  );

  const averageStudents = useMemo(() => {
    if (!tutors.length) return 0;
    return Math.round(totalStudents / tutors.length);
  }, [tutors.length, totalStudents]);

  const chartData = useMemo(
    () =>
      tutors.map((tutor) => ({
        nombre: tutor.nombre,
        alumnos: tutor.cargaActual,
      })),
    [tutors],
  );

  return (
    <div className="space-y-6">
      <section className="grid gap-4 md:grid-cols-4">
        <Card>
          <p className="text-sm text-slate-500">Total de tutores activos</p>
          <p className="text-3xl font-semibold text-text">
            {tutorsLoading ? "--" : tutors.length}
          </p>
        </Card>
        <Card>
          <p className="text-sm text-slate-500">Alumnos asignados</p>
          <p className="text-3xl font-semibold text-text">
            {tutorsLoading ? "--" : totalStudents}
          </p>
        </Card>
        <Card>
          <p className="text-sm text-slate-500">Promedio por tutor</p>
          <p className="text-3xl font-semibold text-text">
            {tutorsLoading ? "--" : averageStudents}
          </p>
        </Card>
        <Card>
          <p className="text-sm text-slate-500">Procesos registrados</p>
          <p className="text-3xl font-semibold text-text">
            {processesLoading ? "--" : processes.length}
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
          <h2 className="text-lg font-semibold text-text">Alertas recientes</h2>
          <p className="text-sm text-slate-500">
            Seguimiento a novedades reportadas en los últimos procesos.
          </p>
          <ul className="mt-4 space-y-3 text-sm text-slate-600">
            {processes.slice(0, 4).map((process) => (
              <li key={process.id} className="rounded-lg bg-slate-100 px-3 py-2">
                {process.estado} · {process.fechaInicio}
                <p className="text-xs text-slate-500">
                  Asignados: {process.totalAsignados} · Errores: {process.totalErrores}
                </p>
              </li>
            ))}
            {!processes.length && (
              <li className="rounded-lg bg-slate-100 px-3 py-2">
                No se registran alertas recientes.
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
          data={processes}
          isLoading={processesLoading}
          emptyMessage="Aún no se han registrado procesos."
        />
      </section>
    </div>
  );
};
