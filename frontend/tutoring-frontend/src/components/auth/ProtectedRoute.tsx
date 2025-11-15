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
    enabled: !user,
    retry: false,
  });

  useEffect(() => {
    if (data && !user) {
      setUser(data);
    }
  }, [data, user, setUser]);

  const activeUser = user ?? data ?? null;

  if (activeUser) {
    return <>{children}</>;
  }

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background">
        <p className="text-sm font-medium text-slate-600">Validando sesión...</p>
      </div>
    );
  }

  if (isError) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  return null;
};
