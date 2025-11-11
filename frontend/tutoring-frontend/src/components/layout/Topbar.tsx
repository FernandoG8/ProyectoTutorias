import { useMemo } from "react";
import { useLocation } from "react-router-dom";
import { navigationItems } from "@/constants/navigation";
import { useAuthStore } from "@/store/auth-store";
import { useUIStore } from "@/store/ui-store";

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

  const primaryRole = user?.roles[0]?.replace("ROLE_", "") ?? "Invitado";

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
          <p className="text-sm font-semibold text-text">{user?.username ?? "Invitado"}</p>
          <p className="text-xs text-slate-500">{primaryRole}</p>
        </div>
        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10 font-semibold text-primary">
          {user ? user.username.charAt(0).toUpperCase() : "I"}
        </div>
      </div>
    </header>
  );
};
