import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Select } from "@/components/ui/Select";
import { uploadAssignmentList } from "@/services/assignment-service";

const schema = z.object({
  tipoLista: z.string().min(1, "Selecciona un tipo de lista."),
  periodo: z.string().min(1, "Indica el período académico."),
  archivo: z
    .custom<FileList>(
      (file) => file instanceof FileList && file.length > 0,
      "Selecciona un archivo en formato CSV o Excel.",
    ),
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
      tipoLista: "",
      periodo: "",
    },
  });

  const { mutateAsync, isPending, isSuccess } = useMutation({
    mutationFn: uploadAssignmentList,
  });

  const onSubmit = async (values: FormValues) => {
    const formData = new FormData();
    formData.append("tipoLista", values.tipoLista);
    formData.append("periodo", values.periodo);
    formData.append("archivo", values.archivo[0]);
    await mutateAsync(formData);
    reset();
  };

  return (
    <div className="grid gap-6 lg:grid-cols-2">
      <Card>
        <h2 className="text-lg font-semibold text-text">
          Cargar listas oficiales
        </h2>
        <p className="text-sm text-slate-500">
          Sube la lista de estudiantes o tutores para iniciar el proceso de asignación.
        </p>

        <form className="mt-6 space-y-5" onSubmit={handleSubmit(onSubmit)}>
          <div className="space-y-1">
            <label className="text-sm font-medium text-text" htmlFor="tipoLista">
              Tipo de lista
            </label>
            <Select id="tipoLista" defaultValue="" {...register("tipoLista")}>
              <option value="" disabled>
                Selecciona una opción
              </option>
              <option value="ESTUDIANTES">Estudiantes matriculados</option>
              <option value="TUTORES">Tutores disponibles</option>
              <option value="REASIGNACIONES">Solicitudes especiales</option>
            </Select>
            {errors.tipoLista && (
              <p className="text-sm text-red-600">{errors.tipoLista.message}</p>
            )}
          </div>

          <div className="grid gap-4 md:grid-cols-2">
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
              <label className="text-sm font-medium text-text" htmlFor="archivo">
                Archivo
              </label>
              <Input id="archivo" type="file" accept=".csv,.xlsx,.xls" {...register("archivo")} />
              {errors.archivo && (
                <p className="text-sm text-red-600">{errors.archivo.message as string}</p>
              )}
            </div>
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
