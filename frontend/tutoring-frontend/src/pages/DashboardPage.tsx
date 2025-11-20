import { useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { RefreshCw } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { DataTable } from "@/components/ui/DataTable";
import { DashboardSkeleton } from "@/components/common/DashboardSkeleton";
import { StatCard } from "@/components/common/StatCard";
import { CarreraDistributionChart } from "@/components/dashboard/CarreraDistributionChart";
import { TopSaturatedTutors } from "@/components/dashboard/TopSaturatedTutors";
import { TutoresCompleteTable } from "@/components/dashboard/TutoresCompleteTable";
import {
  getDashboardEstadisticas,
  getDistribucionTutores,
} from "@/services/dashboard-service";
import { listAssignmentProcesses } from "@/services/asignaciones-service";
import type { AssignmentProcessSummary, DistribucionTutor, CarreraDistribution, TutorSaturation } from "@/types";
import type { ColumnDef } from "@tanstack/react-table";
import { colors } from "@/constants/colors";
import { calculateSaturation, getSaturationState, CARRERA_COLORS } from "@/utils/dashboard-utils";

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
  const [lastRefresh, setLastRefresh] = useState<Date>(new Date());

  const {
    data: estadisticas,
    isLoading: estadisticasLoading,
    refetch: refetchEstadisticas,
    isFetching: isFetchingEstadisticas,
  } = useQuery({
    queryKey: ["dashboard-estadisticas"],
    queryFn: () => getDashboardEstadisticas(),
    refetchInterval: 5 * 60 * 1000, // 5 minutes auto-refresh
    staleTime: 2 * 60 * 1000, // 2 minutes
  });

  const {
    data: distribucionTutores = [],
    isLoading: distribucionLoading,
    refetch: refetchDistribucion,
    isFetching: isFetchingDistribucion,
  } = useQuery<DistribucionTutor[]>({
    queryKey: ["dashboard-distribucion-tutores"],
    queryFn: () => getDistribucionTutores(),
    refetchInterval: 5 * 60 * 1000,
    staleTime: 2 * 60 * 1000,
  });

  const { isLoading: procesosLoading } = useQuery({
    queryKey: ["dashboard-procesos-recientes"],
    queryFn: () => Promise.resolve([]),
  });

  const {
    data: allProcesses = [],
    isLoading: allProcessesLoading,
  } = useQuery<AssignmentProcessSummary[]>({
    queryKey: ["assignment-processes"],
    queryFn: () => listAssignmentProcesses(),
  });

  // Handle manual refresh
  const handleRefresh = async () => {
    setLastRefresh(new Date());
    await Promise.all([refetchEstadisticas(), refetchDistribucion()]);
  };

  // Transform distribucion data to carrera distribution
  const carreraDistribution = useMemo<CarreraDistribution[]>(() => {
    if (!distribucionTutores.length) return [];

    const grouped = distribucionTutores.reduce(
      (acc, tutor) => {
        const carrera = tutor.tutor_carrera;
        if (!acc[carrera]) {
          acc[carrera] = {
            carrera,
            nombreCompleto: CARRERA_COLORS[carrera]?.nombre || carrera,
            totalTutores: 0,
            totalAlumnos: 0,
            capacidadMaxima: 0,
            promedioAlumnos: 0,
            estado: "bajo" as const,
          };
        }
        acc[carrera].totalTutores += 1;
        acc[carrera].totalAlumnos += tutor.alumnos_asignados;
        acc[carrera].capacidadMaxima += tutor.capacidad_max;
        return acc;
      },
      {} as Record<string, CarreraDistribution>,
    );

    return Object.values(grouped).map((carrera) => ({
      ...carrera,
      promedioAlumnos: carrera.totalAlumnos / carrera.totalTutores,
      estado: getSaturationState(
        calculateSaturation(carrera.totalAlumnos, carrera.capacidadMaxima),
      ),
    }));
  }, [distribucionTutores]);

  // Get top saturated tutors
  const topSaturatedTutors = useMemo<TutorSaturation[]>(() => {
    return distribucionTutores
      .map((tutor) => {
        const porcentajeSaturacion = calculateSaturation(
          tutor.alumnos_asignados,
          tutor.capacidad_max,
        );
        return {
          id: tutor.tutor_id,
          nombre: tutor.tutor_nombre,
          carrera: tutor.tutor_carrera,
          alumnosActuales: tutor.alumnos_asignados,
          capacidadMaxima: tutor.capacidad_max,
          porcentajeSaturacion,
          estado: getSaturationState(porcentajeSaturacion),
        };
      })
      .sort((a, b) => b.porcentajeSaturacion - a.porcentajeSaturacion)
      .slice(0, 10);
  }, [distribucionTutores]);

  const isLoading = estadisticasLoading || distribucionLoading || procesosLoading;
  const isRefreshing = isFetchingEstadisticas || isFetchingDistribucion;

  if (isLoading) {
    return <DashboardSkeleton />;
  }

  const getRefreshStatus = () => {
    const now = new Date();
    const diffMs = now.getTime() - lastRefresh.getTime();
    const diffMins = Math.floor(diffMs / 60000);

    if (diffMins === 0) return "Recién actualizado";
    if (diffMins === 1) return "Hace 1 minuto";
    return `Hace ${diffMins} minutos`;
  };

  return (
    <div className="space-y-6">
      {/* Header with refresh */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold" style={{ color: colors.semantic.text.primary }}>
            Dashboard
          </h1>
          <p style={{ color: colors.semantic.text.secondary }} className="text-sm">
            Sistema de Gestión de Tutorías
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={handleRefresh}
            disabled={isRefreshing}
            className="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-gray-100 disabled:opacity-50 transition-colors"
          >
            <RefreshCw
              className={`h-4 w-4 ${isRefreshing ? "animate-spin" : ""}`}
              style={{ color: colors.semantic.text.muted }}
            />
            <span className="text-xs" style={{ color: colors.semantic.text.muted }}>
              {getRefreshStatus()}
            </span>
          </button>
        </div>
      </div>

      {/* Section 1: KPI Cards */}
      <section className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <StatCard
          label="Total Tutores"
          value={estadisticas?.total_tutores ?? "--"}
          variant="primary"
          isLoading={estadisticasLoading}
          trend={
            estadisticas && estadisticas.total_tutores > 0 ? "up" : undefined
          }
          comparison="activos"
        />
        <StatCard
          label="Total Alumnos"
          value={estadisticas?.alumnos_con_tutor ?? "--"}
          variant="success"
          isLoading={estadisticasLoading}
          comparison={
            estadisticas
              ? `vs ${estadisticas.alumnos_sin_tutor} sin tutor`
              : undefined
          }
        />
        <StatCard
          label="Promedio por Tutor"
          value={estadisticas?.promedio_alumnos_por_tutor?.toFixed(1) ?? "--"}
          variant="warning"
          isLoading={estadisticasLoading}
          comparison="alumnos/tutor"
        />
        <StatCard
          label="Desbalance"
          value={`${estadisticas?.desbalance_porcentaje?.toFixed(0) ?? "--"}%`}
          variant="danger"
          isLoading={estadisticasLoading}
          trend="up"
          comparison="respecto al ideal"
        />
      </section>

      {/* Section 2-3: Carrera Distribution & Top Tutores */}
      <section className="grid gap-6 lg:grid-cols-3">
        {/* Carrera Distribution Chart */}
        <Card className="lg:col-span-2">
          <div className="mb-6">
            <h2
              className="text-lg font-semibold mb-1"
              style={{ color: colors.semantic.text.primary }}
            >
              Distribución de Tutores por Carrera
            </h2>
            <p
              className="text-sm"
              style={{ color: colors.semantic.text.secondary }}
            >
              Carga de trabajo y capacidad por programa académico
            </p>
          </div>
          <CarreraDistributionChart
            data={carreraDistribution}
            isLoading={distribucionLoading}
          />
        </Card>

        {/* Top Saturated Tutors */}
        <Card>
          <div className="mb-6">
            <h2
              className="text-lg font-semibold mb-1"
              style={{ color: colors.semantic.text.primary }}
            >
              Top 10 Tutores Saturados
            </h2>
            <p
              className="text-sm"
              style={{ color: colors.semantic.text.secondary }}
            >
              Docentes con mayor carga de alumnos
            </p>
          </div>
          <TopSaturatedTutors
            data={topSaturatedTutors}
            isLoading={distribucionLoading}
          />
        </Card>
      </section>

      {/* Section 4: Complete Tutores Table */}
      <section className="space-y-3">
        <div>
          <h2 className="text-lg font-semibold" style={{ color: colors.semantic.text.primary }}>
            Tabla Completa de Tutores
          </h2>
          <p className="text-sm" style={{ color: colors.semantic.text.secondary }}>
            Detalle de todos los tutores, carga de alumnos y estado de saturación
          </p>
        </div>
        <TutoresCompleteTable
          data={distribucionTutores}
          isLoading={distribucionLoading}
        />
      </section>

      {/* Section 5: Assignment Processes (Legacy) */}
      <section className="space-y-3">
        <div>
          <h2 className="text-lg font-semibold" style={{ color: colors.semantic.text.primary }}>
            Historial de Procesos de Asignación
          </h2>
          <p className="text-sm" style={{ color: colors.semantic.text.secondary }}>
            Registro de ejecuciones de carga de asignaciones
          </p>
        </div>
        <div className="overflow-x-auto">
          <DataTable
            columns={processColumns}
            data={allProcesses}
            isLoading={allProcessesLoading}
            emptyMessage="Aún no se han registrado procesos."
          />
        </div>
      </section>
    </div>
  );
};
