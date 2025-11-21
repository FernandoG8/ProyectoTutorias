import { useState } from "react";
import { useForm, type Resolver, type SubmitHandler } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { ColumnDef } from "@tanstack/react-table";
import { Calendar, Plus, Trash2, TrendingUp, CheckCircle2, XCircle } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Badge } from "@/components/ui/Badge";
import { DataTable } from "@/components/ui/DataTable";
import { Modal } from "@/components/ui/Modal";
import { Skeleton } from "@/components/ui/Skeleton";
import { TableSkeleton } from "@/components/common/TableSkeleton";
import { EmptyState } from "@/components/common/EmptyState";
import {
  listSemestres,
  createSemestre,
  activateSemestre,
  deleteSemestre,
  getSemestreEstadisticas,
} from "@/services/semestres-service";
import { useSemestreStore } from "@/store/semestre-store";
import type { Semestre, SemestreEstadisticas } from "@/types";
import dayjs from "dayjs";

const semestreSchema = z
  .object({
    codigo: z
      .string()
      .min(1, "El código es obligatorio")
      .regex(/^\d{4}-\d{4}-F[12]$/, "Formato inválido. Use: YYYY-YYYY-F1 o YYYY-YYYY-F2"),
    nombre: z.string().min(5, "Mínimo 5 caracteres"),
    fechaInicio: z.string().min(1, "La fecha de inicio es obligatoria"),
    fechaFin: z.string().min(1, "La fecha de fin es obligatoria"),
  })
  .refine((data) => data.fechaFin > data.fechaInicio, {
    message: "La fecha fin debe ser mayor a la fecha inicio",
    path: ["fechaFin"],
  });

type SemestreForm = z.infer<typeof semestreSchema>;

const columns = (
  onActivate: (id: number) => void,
  onDelete: (id: number) => void,
  onViewStats: (id: number) => void,
): ColumnDef<Semestre>[] => [
  {
    header: "Código",
    accessorKey: "codigo",
    cell: ({ getValue, row }) => {
      const codigo = String(getValue());
      const activo = row.original.activo;
      return (
        <div className="flex items-center gap-2">
          <span className="font-semibold text-text">{codigo}</span>
          {activo && <Badge variant="success" className="text-xs">Activo</Badge>}
        </div>
      );
    },
  },
  {
    header: "Nombre",
    accessorKey: "nombre",
  },
  {
    header: "Período",
    cell: ({ row }) => {
      const inicio = dayjs(row.original.fechaInicio).format("DD/MM/YYYY");
      const fin = dayjs(row.original.fechaFin).format("DD/MM/YYYY");
      return (
        <span className="text-sm text-slate-600">
          {inicio} - {fin}
        </span>
      );
    },
  },
  {
    header: "Estado",
    cell: ({ row }) => {
      const vigente = row.original.estaVigente;
      return vigente ? (
        <Badge variant="info">Vigente</Badge>
      ) : (
        <Badge variant="default">Finalizado</Badge>
      );
    },
  },
  {
    header: "Asignaciones",
    accessorKey: "totalAsignaciones",
    cell: ({ getValue }) => {
      const total = getValue() as number | undefined;
      return <span className="text-sm text-slate-600">{total ?? 0}</span>;
    },
  },
  {
    header: "Acciones",
    cell: ({ row }) => {
      const semestre = row.original;
      return (
        <div className="flex items-center gap-2">
          <Button
            variant="ghost"
            className="h-8 px-2"
            onClick={() => onViewStats(semestre.id)}
            title="Ver estadísticas"
          >
            <TrendingUp className="h-4 w-4" />
          </Button>
          {!semestre.activo && (
            <Button
              variant="ghost"
              className="h-8 px-2"
              onClick={() => onActivate(semestre.id)}
              title="Activar semestre"
            >
              <CheckCircle2 className="h-4 w-4 text-emerald-600" />
            </Button>
          )}
          {!semestre.activo && semestre.totalAsignaciones === 0 && (
            <Button
              variant="ghost"
              className="h-8 px-2 text-rose-600 hover:text-rose-700"
              onClick={() => onDelete(semestre.id)}
              title="Eliminar semestre"
            >
              <Trash2 className="h-4 w-4" />
            </Button>
          )}
        </div>
      );
    },
  },
];

export const SemestresPage = () => {
  const queryClient = useQueryClient();
  const { semestreActivo, fetchSemestreActivo, resetFetchAttempt, setSemestreActivo } = useSemestreStore();
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [selectedSemestreId, setSelectedSemestreId] = useState<number | null>(null);
  const [statsModalOpen, setStatsModalOpen] = useState(false);
  const [_stats, _setStats] = useState<SemestreEstadisticas | null>(null);

  const {
    data: semestres = [],
    isLoading,
    error,
  } = useQuery<Semestre[]>({
    queryKey: ["semestres"],
    queryFn: () => listSemestres(),
  });

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<SemestreForm>({
    resolver: zodResolver(semestreSchema) as Resolver<SemestreForm>,
    defaultValues: {
      codigo: "",
      nombre: "",
      fechaInicio: "",
      fechaFin: "",
    },
  });

  const invalidateSemesterDependentQueries = () => {
    const keys = [
      ["semestres"],
      ["students"],
      ["inactive-students"],
      ["tutors"],
      ["tutor-students"],
      ["dashboard-estadisticas"],
      ["dashboard-distribucion-tutores"],
      ["dashboard-procesos-recientes"],
      ["assignment-processes"],
      ["reporte-por-carrera"],
      ["semestre-estadisticas"],
    ] as const;

    keys.forEach((key) => queryClient.invalidateQueries({ queryKey: key }));
  };

  const createMutation = useMutation({
    mutationFn: createSemestre,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["semestres"] });
      // Reset fetch attempt to allow re-fetching the active semester
      resetFetchAttempt();
      setIsCreateModalOpen(false);
      reset();
    },
  });

  const activateMutation = useMutation({
    mutationFn: activateSemestre,
    onSuccess: (nuevoSemestre) => {
      setSemestreActivo(nuevoSemestre);
      invalidateSemesterDependentQueries();
      fetchSemestreActivo();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteSemestre,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["semestres"] });
    },
  });

  const statsQuery = useQuery<SemestreEstadisticas>({
    queryKey: ["semestre-estadisticas", selectedSemestreId],
    queryFn: () => getSemestreEstadisticas(selectedSemestreId!),
    enabled: !!selectedSemestreId && statsModalOpen,
  });

  const onSubmit: SubmitHandler<SemestreForm> = async (data) => {
    await createMutation.mutateAsync(data);
  };

  const handleActivate = async (id: number) => {
    if (
      window.confirm(
        "¿Está seguro de activar este semestre? Se desactivarán todos los demás semestres.",
      )
    ) {
      await activateMutation.mutateAsync(id);
    }
  };

  const handleDelete = async (id: number) => {
    if (
      window.confirm(
        "⚠️ ADVERTENCIA: Esta acción eliminará el semestre y todas sus asignaciones asociadas. Esta acción no se puede deshacer. ¿Desea continuar?",
      )
    ) {
      await deleteMutation.mutateAsync(id);
    }
  };

  const handleViewStats = (id: number) => {
    setSelectedSemestreId(id);
    setStatsModalOpen(true);
  };

  if (error) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="text-center">
          <XCircle className="mx-auto h-12 w-12 text-rose-500" />
          <p className="mt-4 text-sm text-rose-600">
            Error al cargar los semestres. Intente nuevamente.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-text">Gestión de Semestres</h1>
          <p className="mt-1 text-sm text-slate-600">
            Administra los períodos académicos del sistema
          </p>
        </div>
        <Button onClick={() => setIsCreateModalOpen(true)}>
          <Plus className="h-4 w-4" />
          Nuevo Semestre
        </Button>
      </div>

      {semestreActivo && (
        <Card className="border-emerald-200 bg-emerald-50/50">
          <div className="flex items-center gap-3">
            <CheckCircle2 className="h-5 w-5 text-emerald-600" />
            <div>
              <p className="text-sm font-semibold text-emerald-900">
                Semestre Activo: {semestreActivo.codigo}
              </p>
              <p className="text-xs text-emerald-700">{semestreActivo.nombre}</p>
            </div>
          </div>
        </Card>
      )}

      {isLoading ? (
        <Card>
          <TableSkeleton rows={5} columns={6} />
        </Card>
      ) : semestres.length === 0 ? (
        <Card>
          <EmptyState
            icon={Calendar}
            title="No hay semestres registrados"
            description="Comienza creando tu primer semestre académico para gestionar las asignaciones de tutorías."
            action={{
              label: "Crear Semestre",
              onClick: () => setIsCreateModalOpen(true),
            }}
          />
        </Card>
      ) : (
        <Card>
          <div className="overflow-x-auto">
            <DataTable
              columns={columns(handleActivate, handleDelete, handleViewStats)}
              data={semestres}
              isLoading={isLoading}
              emptyMessage="No hay semestres registrados"
            />
          </div>
        </Card>
      )}

      {/* Create Modal */}
      <Modal
        open={isCreateModalOpen}
        title="Crear Nuevo Semestre"
        description="Completa los datos del nuevo período académico"
        onClose={() => {
          setIsCreateModalOpen(false);
          reset();
        }}
        footer={
          <div className="flex gap-2">
            <Button
              variant="secondary"
              onClick={() => {
                setIsCreateModalOpen(false);
                reset();
              }}
            >
              Cancelar
            </Button>
            <Button
              onClick={handleSubmit(onSubmit)}
              loading={createMutation.isPending}
              disabled={createMutation.isPending}
            >
              Crear Semestre
            </Button>
          </div>
        }
      >
        <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <label className="text-sm font-medium text-text" htmlFor="codigo">
                Código <span className="text-rose-600">*</span>
              </label>
              <Input
                id="codigo"
                placeholder="2025-2026-F1"
                {...register("codigo")}
                className={errors.codigo ? "border-rose-500" : ""}
              />
              {errors.codigo && (
                <p className="text-sm text-rose-600">{errors.codigo.message}</p>
              )}
              <p className="text-xs text-slate-500">
                Formato: YYYY-YYYY-F1 o YYYY-YYYY-F2
              </p>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium text-text" htmlFor="nombre">
                Nombre <span className="text-rose-600">*</span>
              </label>
              <Input
                id="nombre"
                placeholder="Semestre Agosto 2025 - Enero 2026"
                {...register("nombre")}
                className={errors.nombre ? "border-rose-500" : ""}
              />
              {errors.nombre && (
                <p className="text-sm text-rose-600">{errors.nombre.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium text-text" htmlFor="fechaInicio">
                Fecha de Inicio <span className="text-rose-600">*</span>
              </label>
              <Input
                id="fechaInicio"
                type="date"
                {...register("fechaInicio")}
                className={errors.fechaInicio ? "border-rose-500" : ""}
              />
              {errors.fechaInicio && (
                <p className="text-sm text-rose-600">{errors.fechaInicio.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium text-text" htmlFor="fechaFin">
                Fecha de Fin <span className="text-rose-600">*</span>
              </label>
              <Input
                id="fechaFin"
                type="date"
                {...register("fechaFin")}
                className={errors.fechaFin ? "border-rose-500" : ""}
              />
              {errors.fechaFin && (
                <p className="text-sm text-rose-600">{errors.fechaFin.message}</p>
              )}
            </div>
          </div>
        </form>
      </Modal>

      {/* Statistics Modal */}
      <Modal
        open={statsModalOpen}
        title="Estadísticas del Semestre"
        onClose={() => {
          setStatsModalOpen(false);
          setSelectedSemestreId(null);
          _setStats(null);
        }}
      >
        {statsQuery.isLoading ? (
          <div className="space-y-3">
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-3/4" />
            <Skeleton className="h-4 w-1/2" />
          </div>
        ) : statsQuery.data ? (
          <div className="space-y-4">
            <div className="grid gap-4 md:grid-cols-2">
              <div className="rounded-lg border border-border bg-slate-50 p-3">
                <p className="text-xs text-slate-500">Total Asignaciones</p>
                <p className="text-2xl font-semibold text-text">
                  {statsQuery.data.totalAsignaciones}
                </p>
              </div>
              <div className="rounded-lg border border-border bg-slate-50 p-3">
                <p className="text-xs text-slate-500">Alumnos Activos</p>
                <p className="text-2xl font-semibold text-text">
                  {statsQuery.data.alumnosActivos}
                </p>
              </div>
              <div className="rounded-lg border border-border bg-slate-50 p-3">
                <p className="text-xs text-slate-500">Tutores Activos</p>
                <p className="text-2xl font-semibold text-text">
                  {statsQuery.data.totalTutoresActivos}
                </p>
              </div>
              <div className="rounded-lg border border-border bg-slate-50 p-3">
                <p className="text-xs text-slate-500">Promedio por Tutor</p>
                <p className="text-2xl font-semibold text-text">
                  {statsQuery.data.promedioAlumnosPorTutor.toFixed(1)}
                </p>
              </div>
            </div>
            {Object.keys(statsQuery.data.distribucionPorCarrera).length > 0 && (
              <div>
                <p className="mb-2 text-sm font-semibold text-text">
                  Distribución por Carrera
                </p>
                <div className="space-y-2">
                  {Object.entries(statsQuery.data.distribucionPorCarrera).map(
                    ([carrera, count]) => (
                      <div key={carrera} className="flex items-center justify-between">
                        <span className="text-sm text-slate-600">{carrera}</span>
                        <Badge variant="info">{count}</Badge>
                      </div>
                    ),
                  )}
                </div>
              </div>
            )}
          </div>
        ) : (
          <p className="text-sm text-slate-600">No se pudieron cargar las estadísticas</p>
        )}
      </Modal>
    </div>
  );
};
