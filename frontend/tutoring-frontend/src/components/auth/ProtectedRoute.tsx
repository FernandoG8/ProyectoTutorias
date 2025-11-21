import { useEffect, type ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { fetchCurrentUser } from "@/services/auth-service";
import { useAuthStore } from "@/store/auth-store";

interface ProtectedRouteProps {
  children: ReactNode;
}

export const ProtectedRoute = ({ children }: ProtectedRouteProps) => {
  const location = useLocation();
  const user = useAuthStore((state) => state.user);
  const setUser = useAuthStore((state) => state.setUser);

  const { data, isLoading, isError } = useQuery({
    queryKey: ["current-user"],
    queryFn: fetchCurrentUser,
    enabled: !user, // Solo fetch si no hay usuario en store
    retry: false,
    staleTime: 1000 * 60 * 5, // 5 minutos
  });

  useEffect(() => {
    if (data && !user) {
      setUser(data);
    }
  }, [data, user, setUser]);

  // CASO 1: Usuario existe en store (después de login)
  if (user) {
    return <>{children}</>;
  }

  // CASO 2: No hay user en store, query está habilitado
  // Si está cargando, mostrar loading
  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background">
        <p className="text-sm font-medium text-slate-600">Validando sesión...</p>
      </div>
    );
  }

  // CASO 3: Query completó con éxito, hay data
  if (data) {
    return <>{children}</>;
  }

  // CASO 4: Query falló o no hay sesión válida
  if (isError || (!user && !data && !isLoading)) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  // Fallback: redirigir a login
  return <Navigate to="/login" replace state={{ from: location }} />;
};
