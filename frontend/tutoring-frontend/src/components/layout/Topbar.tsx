import { useMemo } from "react";
import { useLocation } from "react-router-dom";
import { navigationItems } from "@/constants/navigation";
import { useAuthStore } from "@/store/auth-store";
import { useUIStore } from "@/store/ui-store";
import { MenuIcon } from "@/components/icons";
import { SemestreSelector } from "@/components/common/SemestreSelector";

export const Topbar = () => {
  const location = useLocation();
  const toggleSidebar = useUIStore((state) => state.toggleSidebar);
  const user = useAuthStore((state) => state.user);

  const activeRoute = useMemo(() => {
    const current = navigationItems.find((item) =>
      location.pathname.startsWith(item.path),
    );
    return current?.label ?? "Panel principal";
  }, [location.pathname]);

  const roleLabel = useMemo(() => {
    const role = user?.roles[0];
    if (!role) return "Invitado";
    const dictionary: Record<string, string> = {
      ROLE_COORDINADOR_TUTORIAS: "Coordinador de tutorías",
      ROLE_SECRETARIO_ACADEMICO: "Secretario académico",
    };
    return dictionary[role] ?? role.replace("ROLE_", "").toLowerCase();
  }, [user?.roles]);

  return (
    <header className="sticky top-0 z-10 flex items-center justify-between border-b border-border/60 bg-surface/95 px-6 py-4 backdrop-blur">
      <div className="flex items-center gap-4">
        <button
          onClick={toggleSidebar}
          className="inline-flex h-10 w-10 items-center justify-center rounded-xl border border-border bg-white text-primary transition hover:-translate-y-0.5 hover:border-primary/50 hover:text-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
          type="button"
          aria-label="Alternar menú"
        >
          <MenuIcon className="h-5 w-5" aria-hidden="true" />
        </button>
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.32em] text-primary/70">
            Módulo activo
          </p>
          <h1 className="text-2xl font-semibold text-text">{activeRoute}</h1>
        </div>
      </div>
      <div className="flex items-center gap-4">
        <SemestreSelector />
        <div className="flex items-center gap-4 rounded-2xl border border-border bg-white px-4 py-2 shadow-sm">
          <div className="text-right">
            <p className="text-sm font-semibold text-text">{user?.username ?? "Invitado"}</p>
            <p className="text-xs text-slate-500">{roleLabel}</p>
          </div>
          <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-primary/10 text-base font-semibold text-primary">
            {user ? user.username.charAt(0).toUpperCase() : "I"}
          </div>
        </div>
      </div>
    </header>
  );
};
