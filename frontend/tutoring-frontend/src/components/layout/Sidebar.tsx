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
      className={`flex h-screen flex-col bg-primary text-white transition-all duration-300 ${sidebarCollapsed ? "w-20" : "w-64"}`}
    >
      <div className="flex items-center gap-2 px-6 py-6">
        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-white/20 font-bold">
          TU
        </div>
        {!sidebarCollapsed && (
          <div>
            <p className="text-sm uppercase tracking-widest text-white/70">
              Tutorías
            </p>
            <p className="text-lg font-semibold">Gestión académica</p>
          </div>
        )}
      </div>

      <nav className="flex-1 space-y-1 px-2">
        {navigationItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) =>
              `group flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${isActive ? "bg-white text-primary" : "text-white/80 hover:bg-white/10"}`
            }
            title={sidebarCollapsed ? item.label : undefined}
          >
            <span
              className={`flex h-8 w-8 items-center justify-center rounded-md bg-white/10 text-xs font-semibold uppercase ${sidebarCollapsed ? "" : "hidden"}`}
            >
              {item.label.charAt(0)}
            </span>
            {!sidebarCollapsed && <span>{item.label}</span>}
          </NavLink>
        ))}
      </nav>

      <div className="px-4 py-6">
        <button
          onClick={handleLogout}
          className="w-full rounded-lg border border-white/20 bg-white/10 px-4 py-2 text-sm font-semibold text-white transition hover:bg-white/20"
          type="button"
        >
          Cerrar sesión
        </button>
      </div>
    </aside>
  );
};
