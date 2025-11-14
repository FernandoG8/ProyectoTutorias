import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { AlertCircle, CheckCircle2 } from "lucide-react";
import { startAssignmentProcess } from "@/services/asignaciones-service";
import { useSemestreStore } from "@/store/semestre-store";
import { useAuthStore } from "@/store/auth-store";

const schema = z.object({
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
  const user = useAuthStore((state) => state.user);
  const { semestreActivo, fetchSemestreActivo } = useSemestreStore();

  useEffect(() => {
    fetchSemestreActivo();
  }, [fetchSemestreActivo]);

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

  useEffect(() => {
    if (user?.username) {
      setValue("usuario", user.username);
    }
  }, [user?.username, setValue]);

  const { mutateAsync, isPending, isSuccess } = useMutation({
    mutationFn: startAssignmentProcess,
    onSuccess: () => {
      reset({ usuario: user?.username ?? "", archivo: undefined });
    },
  });

  const onSubmit = async (values: FormValues) => {
    if (!semestreActivo) {
      alert("Debe configurar un semestre activo antes de iniciar el proceso.");
      return;
    }

    const file = values.archivo.item(0);
    if (!file) return;

    await mutateAsync({
      archivo: file,
      semestreAcademico: semestreActivo.codigo, // Formato YYYY-YYYY-F1
      usuario: values.usuario,
    });
  };

  return (
    <div className="grid gap-6 lg:grid-cols-2">
      <Card>
        <h2 className="text-lg font-semibold text-text">Carga rápida de insumos</h2>
        <p className="text-sm text-slate-500">
          Utiliza esta opción para lanzar la asignación automática con un archivo prevalidado.
        </p>

        {!semestreActivo && (
          <div className="mt-4 rounded-lg border border-amber-300 bg-amber-50 px-4 py-3">
            <div className="flex items-start gap-2">
              <AlertCircle className="h-5 w-5 text-amber-600 mt-0.5" />
              <div>
                <p className="text-sm font-semibold text-amber-900">No hay semestre activo</p>
                <p className="text-xs text-amber-700 mt-1">
                  Debe configurar un semestre activo antes de iniciar el proceso.
                </p>
              </div>
            </div>
          </div>
        )}

        {semestreActivo && (
          <div className="mt-4 rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3">
            <div className="flex items-center gap-2">
              <CheckCircle2 className="h-5 w-5 text-emerald-600" />
              <div>
                <p className="text-sm font-semibold text-emerald-900">
                  Semestre Activo: {semestreActivo.codigo}
                </p>
                <p className="text-xs text-emerald-700">{semestreActivo.nombre}</p>
              </div>
            </div>
          </div>
        )}

        <form className="mt-6 space-y-5" onSubmit={handleSubmit(onSubmit)}>
          <div className="space-y-1">
            <label className="text-sm font-medium text-text" htmlFor="archivo">
              Archivo
            </label>
            <Input id="archivo" type="file" accept=".xlsx,.xls" {...register("archivo")} />
            <p className="text-xs text-slate-500">
              El archivo debe incluir las columnas: matrícula, nombre, carrera, semestre.
            </p>
            {errors.archivo && (
              <p className="text-sm text-red-600">{errors.archivo.message as string}</p>
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
