import { useMemo } from "react";
import { useForm, type Resolver, type SubmitHandler } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery } from "@tanstack/react-query";
import { Card } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import { Textarea } from "@/components/ui/Textarea";
import { Button } from "@/components/ui/Button";
import { Select } from "@/components/ui/Select";
import { listTutors } from "@/services/tutors-service";
import { requestTutorChange } from "@/services/asignaciones-service";
import type { TutorResponse } from "@/types";
import { useAuthStore } from "@/store/auth-store";

const schema = z
  .object({
    alumnoId: z.coerce.number().min(1, "Ingresa un código válido."),
    tutorOrigenId: z.coerce.number().min(1, "Selecciona un tutor."),
    tutorDestinoId: z.coerce.number().min(1, "Selecciona un tutor."),
    motivo: z.string().min(5, "Describe el motivo de la reasignación."),
    usuario: z.string().min(1, "Ingresa tu usuario."),
  })
  .refine((data) => data.tutorDestinoId !== data.tutorOrigenId, {
    message: "El tutor destino debe ser distinto al tutor origen.",
    path: ["tutorDestinoId"],
  });

type FormValues = z.infer<typeof schema>;

export const TutorChangePage = () => {
  const canReassign = useAuthStore((state) => state.role === "COORDINADOR_TUTORIAS");

  const {
    data: tutors = [],
    isLoading: tutorsLoading,
  } = useQuery<TutorResponse[]>({
    queryKey: ["tutors"],
    queryFn: () => listTutors(),
  });

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema) as Resolver<FormValues>,
    defaultValues: {
      alumnoId: 0,
      tutorOrigenId: 0,
      tutorDestinoId: 0,
      motivo: "",
      usuario: "",
    },
  });

  const { mutateAsync, isPending, isSuccess, error } = useMutation({
    mutationFn: requestTutorChange,
  });

  const onSubmit: SubmitHandler<FormValues> = async (values) => {
    await mutateAsync(values);
    reset({ alumnoId: 0, tutorOrigenId: 0, tutorDestinoId: 0, motivo: "", usuario: "" });
  };

  const tutorOptions = useMemo(
    () =>
      tutors.map((tutor) => ({
        value: tutor.id,
        label: tutor.nombre,
      })),
    [tutors],
  );

  return (
    <div className="grid gap-6 lg:grid-cols-[3fr_2fr]">
      <Card>
        <h2 className="text-lg font-semibold text-text">Reasignación manual de tutor</h2>
        <p className="text-sm text-slate-500">
          Registra un cambio de tutor justificando el motivo de la solicitud.
        </p>

        <form className="mt-6 space-y-5" onSubmit={handleSubmit(onSubmit)}>
          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-1">
              <label className="text-sm font-medium text-text" htmlFor="alumnoId">
                Código del alumno
              </label>
              <Input id="alumnoId" type="number" min="1" {...register("alumnoId")} />
              {errors.alumnoId && (
                <p className="text-sm text-red-600">{errors.alumnoId.message}</p>
              )}
            </div>
            <div className="space-y-1">
              <label className="text-sm font-medium text-text" htmlFor="tutorOrigenId">
                Tutor actual
              </label>
              <Select id="tutorOrigenId" defaultValue="0" {...register("tutorOrigenId")}
                disabled={tutorsLoading}
              >
                <option value="0" disabled>
                  Selecciona un tutor
                </option>
                {tutorOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </Select>
              {errors.tutorOrigenId && (
                <p className="text-sm text-red-600">{errors.tutorOrigenId.message}</p>
              )}
            </div>
          </div>

          <div className="space-y-1">
            <label className="text-sm font-medium text-text" htmlFor="tutorDestinoId">
              Tutor destino
            </label>
            <Select id="tutorDestinoId" defaultValue="0" {...register("tutorDestinoId")}
              disabled={tutorsLoading}
            >
              <option value="0" disabled>
                Selecciona un tutor
              </option>
              {tutorOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </Select>
            {errors.tutorDestinoId && (
              <p className="text-sm text-red-600">{errors.tutorDestinoId.message}</p>
            )}
          </div>

          <div className="space-y-1">
            <label className="text-sm font-medium text-text" htmlFor="motivo">
              Motivo de la reasignación
            </label>
            <Textarea
              id="motivo"
              rows={4}
              placeholder="Describe la razón de la solicitud por parte del alumno, tutor o programa."
              {...register("motivo")}
            />
            {errors.motivo && (
              <p className="text-sm text-red-600">{errors.motivo.message}</p>
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

          {!canReassign && (
            <p className="rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-700">
              No tienes permisos para realizar la reasignación manual.
            </p>
          )}

          {error && (
            <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
              Ocurrió un problema al registrar la reasignación. Inténtalo nuevamente.
            </p>
          )}

          {isSuccess && (
            <p className="rounded-lg bg-green-50 px-3 py-2 text-sm text-green-700">
              La reasignación se registró correctamente.
            </p>
          )}

          <Button disabled={!canReassign} loading={isPending} type="submit">
            Registrar cambio
          </Button>
        </form>
      </Card>

      <Card>
        <h2 className="text-lg font-semibold text-text">Guía para la reasignación</h2>
        <ul className="mt-4 space-y-3 text-sm text-slate-600">
          <li>• Confirma que el estudiante esté activo en el período académico vigente.</li>
          <li>• Notifica al tutor actual y destino sobre la solicitud y la fecha de cambio.</li>
          <li>• Documenta en el expediente del alumno el motivo y la evidencia asociada.</li>
          <li>• Los cambios quedan registrados en el historial del módulo de reportes.</li>
        </ul>
      </Card>
    </div>
  );
};
