import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Card } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import { Textarea } from "@/components/ui/Textarea";
import { Button } from "@/components/ui/Button";
import { DataTable } from "@/components/ui/DataTable";
import {
  fetchAssignmentProcesses,
  processAssignments,
} from "@/services/assignment-service";
import type { AssignmentProcess } from "@/types";
import type { ColumnDef } from "@tanstack/react-table";

const schema = z.object({
  periodo: z.string().min(1, "Ingresa el período académico."),
  observaciones: z.string().optional(),
});

type FormValues = z.infer<typeof schema>;

const columns: ColumnDef<AssignmentProcess>[] = [
  { header: "ID", accessorKey: "id" },
  { header: "Período", accessorKey: "periodo" },
  { header: "Estado", accessorKey: "estado" },
  { header: "Fecha", accessorKey: "fechaEjecucion" },
  {
    header: "Observaciones",
    accessorKey: "observaciones",
    cell: ({ getValue }) => (
      <span className="text-sm text-slate-600">
        {String(getValue() ?? "Sin observaciones")}
      </span>
    ),
  },
];

export const AssignmentPage = () => {
  const queryClient = useQueryClient();
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      periodo: "",
      observaciones: "",
    },
  });

  const {
    data: processes = [],
    isLoading,
  } = useQuery({
    queryKey: ["assignment-processes"],
    queryFn: fetchAssignmentProcesses,
  });

  const { mutateAsync, isPending, isSuccess } = useMutation({
    mutationFn: processAssignments,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["assignment-processes"] });
    },
  });

  const onSubmit = async (values: FormValues) => {
    await mutateAsync(values);
    reset();
  };

  return (
    <div className="grid gap-6 lg:grid-cols-[2fr_3fr]">
      <Card>
        <h2 className="text-lg font-semibold text-text">Lanzar asignación automática</h2>
        <p className="text-sm text-slate-500">
          Define el período académico y describe los ajustes antes de ejecutar el algoritmo de asignación.
        </p>

        <form className="mt-6 space-y-5" onSubmit={handleSubmit(onSubmit)}>
          <div className="space-y-1">
            <label className="text-sm font-medium text-text" htmlFor="periodo">
              Período académico
            </label>
            <Input id="periodo" placeholder="2025-1" {...register("periodo")} />
            {errors.periodo && (
              <p className="text-sm text-red-600">{errors.periodo.message}</p>
            )}
          </div>

          <div className="space-y-1">
            <label className="text-sm font-medium text-text" htmlFor="observaciones">
              Observaciones
            </label>
            <Textarea
              id="observaciones"
              rows={4}
              placeholder="Detalla si habrá prioridad para ciertos programas o tutores."
              {...register("observaciones")}
            />
          </div>

          <Button loading={isPending} type="submit">
            Ejecutar proceso
          </Button>

          {isSuccess && (
            <p className="rounded-lg bg-green-50 px-3 py-2 text-sm text-green-700">
              Proceso enviado correctamente. El historial se actualizará en cuanto finalice.
            </p>
          )}
        </form>
      </Card>

      <div className="space-y-4">
        <Card>
          <h2 className="text-lg font-semibold text-text">Historial de ejecuciones</h2>
          <p className="text-sm text-slate-500">
            Visualiza el estado y las observaciones de cada ejecución registrada.
          </p>
        </Card>
        <DataTable
          columns={columns}
          data={processes}
          isLoading={isLoading}
          emptyMessage="No se han registrado procesos todavía."
        />
      </div>
    </div>
  );
};
