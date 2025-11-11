import { useMemo } from "react";
import { useLocation } from "react-router-dom";
import { navigationItems } from "@/constants/navigation";
import { useAuthStore } from "@/store/auth-store";
import { useUIStore } from "@/store/ui-store";

export const Topbar = () => {
  const location = useLocation();
  const toggleSidebar = useUIStore((state) => state.toggleSidebar);
  const role = useAuthStore((state) => state.role);

  const activeRoute = useMemo(() => {
    const current = navigationItems.find((item) =>
      location.pathname.startsWith(item.path),
    );
    return current?.label ?? "Panel principal";
  }, [location.pathname]);

  return (
    <header className="flex items-center justify-between bg-surface px-6 py-4 shadow-sm">
      <div className="flex items-center gap-3">
        <button
          onClick={toggleSidebar}
          className="rounded-lg border border-border bg-white px-3 py-2 text-sm font-semibold text-primary transition hover:bg-primary/10"
          type="button"
        >
          Menú
        </button>
        <h1 className="text-2xl font-semibold text-text">{activeRoute}</h1>
      </div>
      <div className="flex items-center gap-4">
        <div className="text-right">
          <p className="text-sm font-semibold text-text">{role ?? "Invitado"}</p>
          <p className="text-xs text-slate-500">Facultad de Ingeniería</p>
        </div>
        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10 font-semibold text-primary">
          {role ? role.charAt(0) : "I"}
        </div>
      </div>
    </header>
  );
};
