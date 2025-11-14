import { useEffect, useMemo, useState } from "react";
import dayjs from "dayjs";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useNavigate } from "react-router-dom";
import {
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import type { ColumnDef } from "@tanstack/react-table";
import { AlertCircle, CheckCircle2, Calendar, Upload, ArrowRight } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { DataTable } from "@/components/ui/DataTable";
import { Skeleton } from "@/components/ui/Skeleton";
import { Modal } from "@/components/ui/Modal";
import {
  getAssignmentProcessStatus,
  listAssignmentProcesses,
  startAssignmentProcess,
} from "@/services/asignaciones-service";
import {
  listSemestres,
  createSemestre,
  activateSemestre,
} from "@/services/semestres-service";
import { useSemestreStore } from "@/store/semestre-store";
import { useAuthStore } from "@/store/auth-store";
import type {
  AssignmentProcessSummary,
  EstadoProceso,
  EstadoProcesoResponse,
  Semestre,
  SemestreCreateInput,
} from "@/types";

const schema = z.object({
  usuario: z.string().min(1, "Indica el usuario responsable."),
  archivo: z
    .custom<FileList>((file) => file instanceof FileList && file.length > 0)
    .refine((files) => files?.item(0) instanceof File, "Selecciona un archivo válido."),
});

type FormValues = z.infer<typeof schema>;

const estadoLabels: Record<EstadoProceso, { label: string; variant: "info" | "warning" | "success" | "danger" }> = {
  INICIADO: { label: "Iniciado", variant: "info" },
  COMPARANDO: { label: "Comparando", variant: "info" },
  LIBERANDO_CUPOS: { label: "Liberando cupos", variant: "warning" },
  ASIGNANDO: { label: "Asignando", variant: "info" },
  COMPLETADO: { label: "Completado", variant: "success" },
  FALLIDO: { label: "Fallido", variant: "danger" },
};

const formatStatus = (estado: EstadoProceso) => estadoLabels[estado] ?? estadoLabels.INICIADO;

export const AssignmentPage = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const user = useAuthStore((state) => state.user);
  const { semestreActivo, fetchSemestreActivo } = useSemestreStore();
  const [selectedProcessId, setSelectedProcessId] = useState<number | null>(null);
  const [showSemestreModal, setShowSemestreModal] = useState(false);
  const [semestreToActivate, setSemestreToActivate] = useState<Semestre | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      usuario: user?.username ?? "",
    },
  });

  // Cargar semestre activo al montar
  useEffect(() => {
    fetchSemestreActivo();
  }, [fetchSemestreActivo]);

  useEffect(() => {
    if (user?.username) {
      setValue("usuario", user.username);
    }
  }, [user?.username, setValue]);

  const { data: semestres = [] } = useQuery<Semestre[]>({
    queryKey: ["semestres"],
    queryFn: () => listSemestres(),
    enabled: showSemestreModal,
  });

  const processesQuery = useQuery<AssignmentProcessSummary[]>({
    queryKey: ["assignment-processes"],
    queryFn: () => listAssignmentProcesses(),
  });

  const statusQuery = useQuery<EstadoProcesoResponse>({
    queryKey: ["assignment-process-status", selectedProcessId],
    queryFn: () => getAssignmentProcessStatus(selectedProcessId as number),
    enabled: selectedProcessId !== null,
    refetchInterval: (query) => {
      const data = query.state.data;
      if (!data) return 3000;
      return data.estado === "COMPLETADO" || data.estado === "FALLIDO" ? false : 3000;
    },
  });

  const activateMutation = useMutation({
    mutationFn: activateSemestre,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["semestres"] });
      fetchSemestreActivo();
      setShowSemestreModal(false);
      setSemestreToActivate(null);
    },
  });

  const { mutateAsync, isPending, data: startResponse, reset: resetMutation } = useMutation({
    mutationFn: startAssignmentProcess,
    onSuccess: (result) => {
      queryClient.invalidateQueries({ queryKey: ["assignment-processes"] });
      if (result.procesoId) {
        setSelectedProcessId(result.procesoId);
      }
      reset({ usuario: user?.username ?? "", archivo: undefined });
    },
  });

  const onSubmit = async (values: FormValues) => {
    if (!semestreActivo) {
      setShowSemestreModal(true);
      return;
    }

    const file = values.archivo.item(0);
    if (!file) return;

    await mutateAsync({
      archivo: file,
      semestreAcademico: semestreActivo.codigo, // Usar formato completo YYYY-YYYY-F1
      usuario: values.usuario,
    });
  };

  const handleActivateSemestre = async (semestre: Semestre) => {
    if (window.confirm(`¿Activar el semestre ${semestre.codigo}? Se desactivarán todos los demás semestres.`)) {
      await activateMutation.mutateAsync(semestre.id);
    }
  };

  const processes = processesQuery.data ?? [];

  const columns: ColumnDef<AssignmentProcessSummary>[] = useMemo(
    () => [
      {
        header: "Proceso",
        accessorKey: "id",
        cell: ({ getValue }) => <span className="font-semibold text-text">#{getValue<number>()}</span>,
      },
      {
        header: "Estado",
        accessorKey: "estado",
        cell: ({ getValue }) => {
          const estado = getValue<EstadoProceso>();
          const { label, variant } = formatStatus(estado);
          return <Badge variant={variant}>{label}</Badge>;
        },
      },
      {
        header: "Inicio",
        accessorKey: "fechaInicio",
        cell: ({ getValue }) => dayjs(getValue<string>()).format("DD/MM/YYYY HH:mm"),
      },
      {
        header: "Fin",
        accessorKey: "fechaFin",
        cell: ({ getValue }) =>
          getValue<string | null>()
            ? dayjs(getValue<string>()).format("DD/MM/YYYY HH:mm")
            : "En curso",
      },
      {
        header: "Asignados",
        accessorKey: "totalAsignados",
      },
      {
        header: "Errores",
        accessorKey: "totalErrores",
        cell: ({ getValue }) => {
          const value = getValue<number>();
          return value > 0 ? (
            <span className="font-semibold text-rose-600">{value}</span>
          ) : (
            <span className="text-slate-600">{value}</span>
          );
        },
      },
      {
        header: "Responsable",
        accessorKey: "usuarioEjecutor",
        cell: ({ getValue }) => getValue<string | null>() ?? "-",
      },
      {
        header: "Seguimiento",
        cell: ({ row }) => (
          <Button
            type="button"
            variant="secondary"
            className="text-xs"
            onClick={() => setSelectedProcessId(row.original.id)}
          >
            Ver progreso
          </Button>
        ),
      },
    ],
    [],
  );

  const activeStatus = statusQuery.data;
  const statusMeta = activeStatus ? formatStatus(activeStatus.estado) : null;

  useEffect(() => {
    if (!activeStatus) return;
    if (activeStatus.estado === "COMPLETADO" || activeStatus.estado === "FALLIDO") {
      queryClient.invalidateQueries({ queryKey: ["assignment-processes"] });
    }
  }, [activeStatus, queryClient]);

  return (
    <div className="space-y-6">
      {/* Alerta de semestre activo */}
      {!semestreActivo && (
        <Card className="border-amber-300 bg-amber-50/50">
          <div className="flex items-start gap-3">
            <AlertCircle className="h-5 w-5 text-amber-600 mt-0.5" />
            <div className="flex-1">
              <p className="text-sm font-semibold text-amber-900">
                No hay semestre activo configurado
              </p>
              <p className="mt-1 text-xs text-amber-700">
                Debe crear y activar un semestre académico antes de iniciar un proceso de asignación.
              </p>
              <div className="mt-3 flex gap-2">
                <Button
                  variant="secondary"
                  size="sm"
                  onClick={() => navigate("/semestres")}
                >
                  <Calendar className="h-4 w-4 mr-1" />
                  Gestionar Semestres
                </Button>
                <Button
                  variant="primary"
                  size="sm"
                  onClick={() => setShowSemestreModal(true)}
                >
                  Activar Semestre Existente
                </Button>
              </div>
            </div>
          </div>
        </Card>
      )}

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

      <div className="grid gap-6 lg:grid-cols-[2fr,3fr]">
        <Card>
          <div className="space-y-2">
            <h2 className="text-lg font-semibold text-text">Iniciar proceso de asignación</h2>
            <p className="text-sm text-slate-600">
              Sube el archivo oficial de alumnos. El proceso utilizará el semestre activo configurado.
            </p>
          </div>

          <form className="mt-6 space-y-5" onSubmit={handleSubmit(onSubmit)}>
            {semestreActivo && (
              <div className="rounded-lg border border-border bg-slate-50 px-4 py-3">
                <p className="text-xs font-semibold text-slate-500 uppercase tracking-wide">Semestre Académico</p>
                <p className="mt-1 text-sm font-semibold text-text">{semestreActivo.codigo}</p>
                <p className="text-xs text-slate-500">{semestreActivo.nombre}</p>
              </div>
            )}

            <div className="space-y-1">
              <label className="text-sm font-medium text-text" htmlFor="usuario">
                Usuario responsable
              </label>
              <Input
                id="usuario"
                placeholder="coord_tutorias"
                {...register("usuario")}
              />
              <p className="text-xs text-slate-500">Este nombre se registrará en la auditoría del proceso.</p>
              {errors.usuario && (
                <p className="text-sm text-rose-600">{errors.usuario.message}</p>
              )}
            </div>

            <div className="space-y-1">
              <label className="text-sm font-medium text-text" htmlFor="archivo">
                Archivo de alumnos
              </label>
              <Input id="archivo" type="file" accept=".xlsx,.xls" {...register("archivo")} />
              <p className="text-xs text-slate-500">
                El archivo debe incluir las columnas: matrícula, nombre, carrera, semestre.
              </p>
              {errors.archivo && (
                <p className="text-sm text-rose-600">{errors.archivo.message as string}</p>
              )}
            </div>

            <div className="flex items-center gap-3">
              <Button
                loading={isPending}
                type="submit"
                disabled={!semestreActivo}
              >
                <Upload className="h-4 w-4 mr-1" />
                Ejecutar proceso
              </Button>
              {startResponse && (
                <button
                  type="button"
                  className="text-xs font-semibold text-primary underline"
                  onClick={() => {
                    if (startResponse.procesoId) {
                      setSelectedProcessId(startResponse.procesoId);
                      resetMutation();
                    }
                  }}
                >
                  Ver seguimiento
                </button>
              )}
            </div>

            {startResponse && (
              <p className="rounded-lg bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
                {startResponse.mensaje ?? "Proceso registrado. Puedes monitorear el estado en el panel de seguimiento."}
              </p>
            )}

            {!semestreActivo && (
              <p className="rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-700">
                Debe configurar un semestre activo antes de iniciar el proceso.
              </p>
            )}
          </form>
        </Card>

        <Card>
          <div className="flex flex-col gap-3">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-lg font-semibold text-text">Seguimiento en tiempo real</h2>
                <p className="text-sm text-slate-600">
                  Selecciona un proceso para conocer su estado y el avance de la ejecución.
                </p>
              </div>
              {selectedProcessId && (
                <Badge variant="info">Proceso #{selectedProcessId}</Badge>
              )}
            </div>

            {statusQuery.isLoading ? (
              <Skeleton className="h-32" />
            ) : activeStatus ? (
              <div className="rounded-xl border border-border bg-white p-4 shadow-sm">
                <div className="flex items-center justify-between">
                  <p className="text-sm font-medium text-text">Estado actual</p>
                  {statusMeta && <Badge variant={statusMeta.variant}>{statusMeta.label}</Badge>}
                </div>
                <div className="mt-4 space-y-3">
                  <div>
                    <div className="flex items-center justify-between text-xs text-slate-500">
                      <span>Avance general</span>
                      <span>{activeStatus.progreso.porcentaje}%</span>
                    </div>
                    <div className="mt-1 h-2 rounded-full bg-slate-200">
                      <div
                        className="h-2 rounded-full bg-accent transition-all"
                        style={{ width: `${activeStatus.progreso.porcentaje}%` }}
                      />
                    </div>
                  </div>
                  <div className="grid grid-cols-2 gap-3 text-xs">
                    <div className="rounded-lg bg-slate-100/70 p-3">
                      <p className="font-semibold text-text">{activeStatus.progreso.alumnosProcesados}</p>
                      <p className="text-slate-500">Alumnos procesados</p>
                    </div>
                    <div className="rounded-lg bg-slate-100/70 p-3">
                      <p className="font-semibold text-text">{activeStatus.progreso.alumnosAsignados}</p>
                      <p className="text-slate-500">Alumnos asignados</p>
                    </div>
                    <div className="rounded-lg bg-rose-50 p-3">
                      <p className="font-semibold text-rose-600">{activeStatus.progreso.errores}</p>
                      <p className="text-rose-600">Errores detectados</p>
                    </div>
                    <div className="rounded-lg bg-amber-50 p-3">
                      <p className="font-semibold text-amber-600">{activeStatus.progreso.warnings}</p>
                      <p className="text-amber-600">Alertas</p>
                    </div>
                  </div>
                  <div className="flex flex-wrap items-center gap-4 text-xs text-slate-500">
                    <span>
                      Inicio: {dayjs(activeStatus.fechaInicio).format("DD/MM/YYYY HH:mm")}
                    </span>
                    <span>
                      Última actualización: {dayjs().format("HH:mm:ss")}
                    </span>
                  </div>
                </div>
              </div>
            ) : (
              <div className="rounded-xl border border-dashed border-primary/40 bg-primary/5 p-6 text-sm text-primary">
                Selecciona un proceso del historial para visualizar su progreso.
              </div>
            )}
          </div>
        </Card>
      </div>

      <Card>
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-lg font-semibold text-text">Historial de ejecuciones</h2>
              <p className="text-sm text-slate-600">Revisa los procesos anteriores y vuelve a monitorear cualquiera de ellos.</p>
            </div>
            <Button
              type="button"
              variant="secondary"
              onClick={() => processesQuery.refetch()}
              disabled={processesQuery.isFetching}
            >
              Actualizar historial
            </Button>
          </div>

          {processesQuery.isLoading ? (
            <div className="grid gap-3">
              <Skeleton className="h-24" />
              <Skeleton className="h-24" />
            </div>
          ) : (
            <DataTable
              columns={columns}
              data={processes}
              isLoading={processesQuery.isFetching && !processesQuery.isLoading}
              emptyMessage="No se han registrado procesos todavía."
            />
          )}
        </div>
      </Card>

      {/* Modal para activar semestre */}
      <Modal
        open={showSemestreModal}
        title="Activar Semestre Académico"
        description="Selecciona un semestre para activar. Solo puede haber un semestre activo a la vez."
        onClose={() => {
          setShowSemestreModal(false);
          setSemestreToActivate(null);
        }}
      >
        <div className="space-y-3">
          {semestres.length === 0 ? (
            <p className="text-sm text-slate-600">No hay semestres disponibles. Crea uno primero.</p>
          ) : (
            semestres.map((semestre) => (
              <div
                key={semestre.id}
                className={`flex items-center justify-between rounded-lg border p-3 ${
                  semestre.activo
                    ? "border-emerald-300 bg-emerald-50"
                    : "border-border hover:border-primary/40"
                }`}
              >
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-semibold text-text">{semestre.codigo}</span>
                    {semestre.activo && <Badge variant="success" className="text-xs">Activo</Badge>}
                  </div>
                  <p className="text-xs text-slate-500">{semestre.nombre}</p>
                </div>
                {!semestre.activo && (
                  <Button
                    variant="secondary"
                    size="sm"
                    onClick={() => handleActivateSemestre(semestre)}
                    loading={activateMutation.isPending}
                  >
                    Activar
                  </Button>
                )}
              </div>
            ))
          )}
          <div className="pt-3 border-t">
            <Button
              variant="primary"
              onClick={() => navigate("/semestres")}
              className="w-full"
            >
              Crear Nuevo Semestre
              <ArrowRight className="h-4 w-4 ml-1" />
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
