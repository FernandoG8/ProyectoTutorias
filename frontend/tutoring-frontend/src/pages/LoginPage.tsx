import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import { useNavigate, useLocation } from "react-router-dom";
import { Lock, Mail } from "lucide-react";
import { login, fetchCurrentUser } from "@/services/auth-service";
import { useAuthStore } from "@/store/auth-store";
import { useNotification } from "@/hooks/useNotification";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { colors } from "@/constants/colors";

const loginSchema = z.object({
  username: z
    .string()
    .min(1, "Usuario requerido")
    .email("Ingresa un email válido"),
  password: z
    .string()
    .min(1, "Contraseña requerida")
    .min(6, "La contraseña debe tener al menos 6 caracteres"),
});

type LoginForm = z.infer<typeof loginSchema>;

/**
 * LoginPage Component
 *
 * Authentication entry point with:
 * - Tutolink branding
 * - Form validation with Zod
 * - Real-time feedback via notifications
 * - Loading states
 * - Institutional messaging
 *
 * Decision Log:
 * - Email validation to ensure proper institutional credentials
 * - Notification system integration for better UX
 * - Lucide icons for visual clarity
 * - Modern, clean design aligned with Tutolink brand
 * - Accessibility features (ARIA labels, semantic HTML)
 */
export const LoginPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const setUser = useAuthStore((state) => state.setUser);
  const { error: showError } = useNotification();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
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
      const errorMessage =
        mutationError instanceof Error
          ? mutationError.message
          : "Error al iniciar sesión. Verifica tus credenciales.";
      showError(errorMessage);
    }
  };

  const loading = isPending || isSubmitting;

  return (
    <div
      className="flex min-h-screen items-center justify-center px-4 py-8"
      style={{ backgroundColor: colors.semantic.background }}
    >
      <div className="w-full max-w-md space-y-8">
        {/* Logo & Branding */}
        <div className="space-y-4 text-center">
          <div className="flex items-center justify-center">
            <div
              className="flex h-16 w-16 items-center justify-center rounded-2xl text-2xl font-bold text-white shadow-lg"
              style={{ backgroundColor: colors.primary[400] }}
            >
              TL
            </div>
          </div>
          <div className="space-y-2">
            <h1
              className="text-3xl font-bold"
              style={{ color: colors.semantic.text.primary }}
            >
              Tutolink
            </h1>
            <p
              className="text-sm font-medium"
              style={{ color: colors.semantic.text.secondary }}
            >
              Sistema de Gestión Académica
            </p>
            <p
              className="text-xs"
              style={{ color: colors.semantic.text.muted }}
            >
              Plataforma de Coordinación de Tutorías
            </p>
          </div>
        </div>

        {/* Login Card */}
        <div
          className="space-y-6 rounded-2xl border bg-white px-8 py-10 shadow-xl"
          style={{ borderColor: colors.semantic.border }}
        >
          <div className="space-y-2">
            <h2
              className="text-xl font-semibold"
              style={{ color: colors.semantic.text.primary }}
            >
              Iniciar sesión
            </h2>
            <p
              className="text-sm"
              style={{ color: colors.semantic.text.secondary }}
            >
              Ingresa tus credenciales institucionales
            </p>
          </div>

          {/* Login Form */}
          <form className="space-y-5" onSubmit={handleSubmit(onSubmit)}>
            {/* Username Field */}
            <div className="space-y-2">
              <label
                className="text-sm font-semibold"
                htmlFor="username"
                style={{ color: colors.semantic.text.primary }}
              >
                Usuario (Email)
              </label>
              <div className="relative">
                <Mail
                  className="absolute left-3 top-3 h-5 w-5"
                  style={{ color: colors.semantic.text.muted }}
                />
                <Input
                  id="username"
                  placeholder="usuario@universidad.edu"
                  className="pl-10"
                  {...register("username")}
                />
              </div>
              {errors.username && (
                <p
                  className="text-xs font-medium"
                  style={{ color: colors.danger[500] }}
                >
                  {errors.username.message}
                </p>
              )}
            </div>

            {/* Password Field */}
            <div className="space-y-2">
              <label
                className="text-sm font-semibold"
                htmlFor="password"
                style={{ color: colors.semantic.text.primary }}
              >
                Contraseña
              </label>
              <div className="relative">
                <Lock
                  className="absolute left-3 top-3 h-5 w-5"
                  style={{ color: colors.semantic.text.muted }}
                />
                <Input
                  id="password"
                  type="password"
                  placeholder="••••••••"
                  className="pl-10"
                  {...register("password")}
                />
              </div>
              {errors.password && (
                <p
                  className="text-xs font-medium"
                  style={{ color: colors.danger[500] }}
                >
                  {errors.password.message}
                </p>
              )}
            </div>

            {/* API Error Alert */}
            {error && (
              <div
                className="rounded-lg border-l-4 px-4 py-3 text-sm"
                style={{
                  backgroundColor: colors.danger[50],
                  borderColor: colors.danger[400],
                  color: colors.danger[900],
                }}
              >
                <p className="font-medium">Error de autenticación</p>
                <p className="text-xs">
                  {error instanceof Error
                    ? error.message
                    : "Verifica tus credenciales e intenta de nuevo"}
                </p>
              </div>
            )}

            {/* Submit Button */}
            <Button
              className="w-full"
              loading={loading}
              disabled={loading}
              type="submit"
              style={{
                backgroundColor: colors.primary[400],
              }}
            >
              {loading ? "Verificando..." : "Acceder"}
            </Button>
          </form>
        </div>

        {/* Footer */}
        <div className="space-y-3 text-center">
          <p
            className="text-xs"
            style={{ color: colors.semantic.text.muted }}
          >
            Universidad Nacional de Ingeniería
            <br />
            Coordinación de Tutorías Académicas
          </p>
          <p
            className="text-xs"
            style={{ color: colors.semantic.text.secondary }}
          >
            © 2025 Tutolink. Todos los derechos reservados.
          </p>
        </div>
      </div>
    </div>
  );
};
