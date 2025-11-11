import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import { useNavigate, useLocation } from "react-router-dom";
import { login, fetchCurrentUser } from "@/services/auth-service";
import { useAuthStore } from "@/store/auth-store";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";

const loginSchema = z.object({
  username: z.string().min(1, "Ingresa tu usuario institucional."),
  password: z.string().min(1, "Ingresa tu contraseña."),
});

type LoginForm = z.infer<typeof loginSchema>;

export const LoginPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const setUser = useAuthStore((state) => state.setUser);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      username: "",
      password: "",
    },
  });

  const { mutateAsync, isPending, error } = useMutation({
    mutationFn: login,
  });

  const onSubmit = async (values: LoginForm) => {
    try {
      await mutateAsync(values);
      const profile = await fetchCurrentUser();
      setUser(profile);
      const from =
        (location.state as { from?: { pathname: string } })?.from?.pathname ??
        "/dashboard";
      navigate(from, { replace: true });
    } catch (mutationError) {
      console.error(mutationError);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-background px-4">
      <div className="w-full max-w-md space-y-8 rounded-3xl border border-border bg-white px-8 py-10 shadow-xl">
        <div className="space-y-2 text-center">
          <p className="text-sm font-semibold uppercase tracking-[0.3em] text-primary/70">
            Sistema de Tutorías
          </p>
          <h1 className="text-2xl font-semibold text-text">
            Iniciar sesión en la plataforma
          </h1>
          <p className="text-sm text-slate-500">
            Ingresa tus credenciales institucionales para continuar.
          </p>
        </div>

        <form className="space-y-5" onSubmit={handleSubmit(onSubmit)}>
          <div className="space-y-1">
            <label className="text-sm font-medium text-text" htmlFor="username">
              Usuario
            </label>
            <Input id="username" placeholder="usuario@universidad.edu" {...register("username")} />
            {errors.username && (
              <p className="text-sm text-red-600">{errors.username.message}</p>
            )}
          </div>

          <div className="space-y-1">
            <label className="text-sm font-medium text-text" htmlFor="password">
              Contraseña
            </label>
            <Input id="password" type="password" placeholder="********" {...register("password")} />
            {errors.password && (
              <p className="text-sm text-red-600">{errors.password.message}</p>
            )}
          </div>

          {error && (
            <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
              Ocurrió un error al iniciar sesión. Verifica tus credenciales.
            </p>
          )}

          <Button className="w-full" loading={isPending} type="submit">
            Acceder
          </Button>
        </form>

        <p className="text-center text-xs text-slate-500">
          Universidad Nacional · Coordinación de Tutorías Académicas
        </p>
      </div>
    </div>
  );
};
