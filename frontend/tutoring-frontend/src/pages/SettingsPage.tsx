import { useEffect, useState } from "react";
import { useForm, type Resolver, type SubmitHandler } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useNavigate } from "react-router-dom";
import { Card } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import { Button } from "@/components/ui/Button";
import { fetchCurrentUser, logout as logoutRequest } from "@/services/auth-service";
import { useAuthStore } from "@/store/auth-store";

const schema = z.object({
  correoAlternativo: z
    .string()
    .email("Ingresa un correo válido.")
    .or(z.literal("")),
  recibirNotificaciones: z.coerce.boolean(),
  resumenSemanal: z.coerce.boolean(),
});

type SettingsForm = z.infer<typeof schema>;

export const SettingsPage = () => {
  const navigate = useNavigate();
  const clearSession = useAuthStore((state) => state.logout);
  const setUser = useAuthStore((state) => state.setUser);
  const user = useAuthStore((state) => state.user);
  const [feedback, setFeedback] = useState<string | null>(null);

  useEffect(() => {
    const loadProfile = async () => {
      try {
        const profile = await fetchCurrentUser();
        setUser(profile);
      } catch (error) {
        console.error("No se pudo actualizar el perfil", error);
      }
    };

    if (!user) {
      void loadProfile();
    }
  }, [user, setUser]);

  const profile = user;

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<SettingsForm>({
    resolver: zodResolver(schema) as Resolver<SettingsForm>,
    defaultValues: {
      correoAlternativo: "",
      recibirNotificaciones: true,
      resumenSemanal: false,
    },
  });

  const onSubmit: SubmitHandler<SettingsForm> = (values) => {
    console.info("Preferencias guardadas", values);
    setFeedback("Preferencias actualizadas correctamente.");
  };

  const handleLogout = async () => {
    try {
      await logoutRequest();
    } catch (error) {
      console.error("Error al cerrar sesión", error);
    } finally {
      clearSession();
      navigate("/login", { replace: true });
    }
  };

  return (
    <div className="grid gap-6 lg:grid-cols-2">
      <Card>
        <h2 className="text-lg font-semibold text-text">Perfil institucional</h2>
        <p className="text-sm text-slate-500">
          Información obtenida del sistema de autenticación centralizada.
        </p>

        <div className="mt-6 space-y-3 text-sm text-slate-600">
          <div>
            <p className="text-xs uppercase tracking-wide text-slate-400">Nombre</p>
            <p className="text-base font-medium text-text">{profile?.username ?? "--"}</p>
          </div>
          <div>
            <p className="text-xs uppercase tracking-wide text-slate-400">Correo</p>
            <p className="text-base font-medium text-text">{profile ? `${profile.username}@tutorias.edu` : "--"}</p>
          </div>
          <div>
            <p className="text-xs uppercase tracking-wide text-slate-400">Rol asignado</p>
            <p className="text-base font-medium text-text">
              {profile?.roles[0]?.replace("ROLE_", "") ?? "--"}
            </p>
          </div>
          <Button type="button" variant="secondary" onClick={handleLogout}>
            Cerrar sesión
          </Button>
        </div>
      </Card>

      <Card>
        <h2 className="text-lg font-semibold text-text">Preferencias personales</h2>
        <p className="text-sm text-slate-500">
          Ajusta cómo deseas recibir notificaciones y resúmenes.
        </p>

        <form className="mt-6 space-y-4" onSubmit={handleSubmit(onSubmit)}>
          <div className="space-y-1">
            <label className="text-sm font-medium text-text" htmlFor="correoAlternativo">
              Correo alternativo
            </label>
            <Input
              id="correoAlternativo"
              placeholder="coordinacion@universidad.edu"
              {...register("correoAlternativo")}
            />
            {errors.correoAlternativo && (
              <p className="text-sm text-red-600">{errors.correoAlternativo.message}</p>
            )}
          </div>

          <div className="flex items-center justify-between rounded-xl border border-border bg-white px-4 py-3">
            <div>
              <p className="text-sm font-semibold text-text">Notificaciones inmediatas</p>
              <p className="text-xs text-slate-500">
                Recibe alertas al instante cuando haya cambios en las asignaciones.
              </p>
            </div>
            <input
              type="checkbox"
              className="h-5 w-5 rounded border-border text-primary focus:ring-primary/50"
              {...register("recibirNotificaciones")}
            />
          </div>

          <div className="flex items-center justify-between rounded-xl border border-border bg-white px-4 py-3">
            <div>
              <p className="text-sm font-semibold text-text">Resumen semanal</p>
              <p className="text-xs text-slate-500">
                Recibe un resumen cada viernes con el estado de las tutorías.
              </p>
            </div>
            <input
              type="checkbox"
              className="h-5 w-5 rounded border-border text-primary focus:ring-primary/50"
              {...register("resumenSemanal")}
            />
          </div>

          {feedback && (
            <p className="rounded-lg bg-green-50 px-3 py-2 text-sm text-green-700">
              {feedback}
            </p>
          )}

          <Button type="submit">Guardar preferencias</Button>
        </form>
      </Card>
    </div>
  );
};
