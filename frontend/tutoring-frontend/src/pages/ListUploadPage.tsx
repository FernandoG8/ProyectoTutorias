import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { startAssignmentProcess } from "@/services/asignaciones-service";

const schema = z.object({
  semestreAcademico: z
    .string()
    .min(1, "Indica el semestre académico.")
    .regex(/\d{4}-[12]/, "Usa el formato YYYY-1 o YYYY-2."),
  usuario: z.string().min(1, "Ingresa el usuario responsable."),
  archivo: z
    .custom<FileList>(
      (file) => file instanceof FileList && file.length > 0,
      "Selecciona un archivo en formato CSV o Excel.",
    )
    .refine((files) => files?.item(0) instanceof File, "Selecciona un archivo válido."),
});

type FormValues = z.infer<typeof schema>;

export const ListUploadPage = () => {
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
    },
  });

  const { mutateAsync, isPending, isSuccess } = useMutation({
    mutationFn: startAssignmentProcess,
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
    <div className="grid gap-6 lg:grid-cols-2">
      <Card>
        <h2 className="text-lg font-semibold text-text">Carga rápida de insumos</h2>
        <p className="text-sm text-slate-500">
          Utiliza esta opción para lanzar la asignación automática con un archivo prevalidado.
        </p>

        <form className="mt-6 space-y-5" onSubmit={handleSubmit(onSubmit)}>
          <div className="grid gap-4 md:grid-cols-2">
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
              <label className="text-sm font-medium text-text" htmlFor="archivo">
                Archivo
              </label>
              <Input id="archivo" type="file" accept=".csv,.xlsx,.xls" {...register("archivo")} />
              {errors.archivo && (
                <p className="text-sm text-red-600">{errors.archivo.message as string}</p>
              )}
            </div>
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

          <Button loading={isPending} type="submit">
            Procesar carga
          </Button>

          {isSuccess && (
            <p className="rounded-lg bg-green-50 px-3 py-2 text-sm text-green-700">
              Archivo cargado correctamente. Revisa el historial de procesos para verificar el resultado.
            </p>
          )}
        </form>
      </Card>

      <Card>
        <h2 className="text-lg font-semibold text-text">Recomendaciones</h2>
        <ul className="mt-4 space-y-3 text-sm text-slate-600">
          <li>
            • Utiliza las plantillas institucionales para garantizar el orden de las columnas.
          </li>
          <li>
            • El tamaño máximo del archivo es de 10 MB.
          </li>
          <li>
            • Asegúrate de que el período académico coincida con el seleccionado en el sistema.
          </li>
          <li>
            • Después de la carga, monitorea el avance en el módulo de asignación.
          </li>
        </ul>
      </Card>
    </div>
  );
};
