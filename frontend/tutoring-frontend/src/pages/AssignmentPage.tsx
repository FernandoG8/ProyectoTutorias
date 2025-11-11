import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Card } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import { Button } from "@/components/ui/Button";
import { DataTable } from "@/components/ui/DataTable";
import {
  listAssignmentProcesses,
  startAssignmentProcess,
} from "@/services/asignaciones-service";
import type { AssignmentProcessSummary } from "@/types";
import type { ColumnDef } from "@tanstack/react-table";

const schema = z.object({
  semestreAcademico: z
    .string()
    .min(1, "Ingresa el semestre académico.")
    .regex(/\d{4}-[12]/, "Usa el formato YYYY-1 o YYYY-2."),
  usuario: z.string().min(1, "Indica el usuario responsable."),
  archivo: z
    .custom<FileList>((file) => file instanceof FileList && file.length > 0)
    .refine((files) => files?.item(0) instanceof File, "Selecciona un archivo válido."),
});

type FormValues = z.infer<typeof schema>;

const columns: ColumnDef<AssignmentProcessSummary>[] = [
  { header: "ID", accessorKey: "id" },
  { header: "Estado", accessorKey: "estado" },
  { header: "Inicio", accessorKey: "fechaInicio" },
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
  { header: "Asignados", accessorKey: "totalAsignados" },
  { header: "Errores", accessorKey: "totalErrores" },
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
      semestreAcademico: "",
      usuario: "",
      archivo: undefined,
    },
  });

  const {
    data: processes = [],
    isLoading,
  } = useQuery<AssignmentProcessSummary[]>({
    queryKey: ["assignment-processes"],
    queryFn: () => listAssignmentProcesses(),
  });

  const { mutateAsync, isPending, data: response } = useMutation({
    mutationFn: startAssignmentProcess,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["assignment-processes"] });
    },
  });

  const onSubmit = async (values: FormValues) => {
    const file = values.archivo.item(0);
    if (!file) return;
    await mutateAsync({
      archivo: file,
      semestreAcademico: values.semestreAcademico,
      usuario: values.usuario,
    });
    reset();
  };

  return (
    <div className="grid gap-6 lg:grid-cols-[2fr_3fr]">
      <Card>
        <h2 className="text-lg font-semibold text-text">Iniciar proceso de asignación</h2>
        <p className="text-sm text-slate-500">
          Carga el archivo oficial de alumnos y define el semestre académico que deseas procesar.
        </p>

        <form className="mt-6 space-y-5" onSubmit={handleSubmit(onSubmit)}>
          <div className="space-y-1">
            <label className="text-sm font-medium text-text" htmlFor="semestreAcademico">
              Semestre académico
            </label>
            <Input
              id="semestreAcademico"
              placeholder="2025-1"
              {...register("semestreAcademico")}
            />
            {errors.semestreAcademico && (
              <p className="text-sm text-red-600">{errors.semestreAcademico.message}</p>
            )}
          </div>

          <div className="space-y-1">
            <label className="text-sm font-medium text-text" htmlFor="usuario">
              Usuario responsable
            </label>
            <Input id="usuario" placeholder="coord_tutorias" {...register("usuario")} />
            {errors.usuario && (
              <p className="text-sm text-red-600">{errors.usuario.message}</p>
            )}
          </div>

          <div className="space-y-1">
            <label className="text-sm font-medium text-text" htmlFor="archivo">
              Archivo de alumnos
            </label>
            <Input id="archivo" type="file" accept=".csv,.xlsx,.xls" {...register("archivo")} />
            {errors.archivo && (
              <p className="text-sm text-red-600">{errors.archivo.message as string}</p>
            )}
          </div>

          <Button loading={isPending} type="submit">
            Ejecutar proceso
          </Button>

          {response && (
            <p className="rounded-lg bg-green-50 px-3 py-2 text-sm text-green-700">
              {response.mensaje ?? "Proceso registrado. Consulta el estado en el historial."}
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
