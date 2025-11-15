import { NavLink, useNavigate } from "react-router-dom";
import { navigationItems } from "@/constants/navigation";
import { logout as logoutRequest } from "@/services/auth-service";
import { useAuthStore } from "@/store/auth-store";
import { useUIStore } from "@/store/ui-store";

export const Sidebar = () => {
  const navigate = useNavigate();
  const { sidebarCollapsed } = useUIStore();
  const clearSession = useAuthStore((state) => state.logout);

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
    <aside
      aria-label="Menú principal"
      className={`flex h-screen flex-col bg-gradient-to-b from-primary to-secondary text-white shadow-lg transition-all duration-300 ${sidebarCollapsed ? "w-20" : "w-72"}`}
    >
      <div className="flex items-center gap-3 px-5 py-6">
        <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-white/15 text-lg font-bold uppercase tracking-wide">
          TU
        </div>
        {!sidebarCollapsed && (
          <div className="space-y-1">
            <p className="text-xs font-semibold uppercase tracking-[0.3em] text-white/70">
              Tutorías
            </p>
            <p className="text-lg font-semibold">Gestión académica</p>
          </div>
        )}
      </div>

      <nav className="flex-1 space-y-1 px-3">
        {navigationItems.map((item) => {
          const Icon = item.icon;

          return (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `group relative flex items-center gap-3 rounded-xl px-3 py-2 text-sm font-medium transition-all ${
                  isActive
                    ? "bg-white text-primary shadow-sm"
                    : "text-white/80 hover:bg-white/15 hover:text-white"
                }`
              }
              title={sidebarCollapsed ? item.label : undefined}
            >
              <span
                className={`flex h-10 w-10 items-center justify-center rounded-lg ${
                  sidebarCollapsed ? "bg-white/15" : "bg-white/10"
                } transition group-hover:bg-white/20`}
              >
                <Icon aria-hidden="true" className="h-5 w-5 text-white" />
                <span className="sr-only">{item.label}</span>
              </span>
              {!sidebarCollapsed && (
                <span className="text-sm font-semibold tracking-wide">{item.label}</span>
              )}
            </NavLink>
          );
        })}
      </nav>

      <div className="px-4 py-6">
        <button
          onClick={handleLogout}
          className="w-full rounded-xl border border-white/20 bg-white/10 px-4 py-2 text-sm font-semibold text-white transition hover:bg-white/20 focus:outline-none focus:ring-2 focus:ring-white/30"
          type="button"
        >
          Cerrar sesión
        </button>
      </div>
    </aside>
  );
};
