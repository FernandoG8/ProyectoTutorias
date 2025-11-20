import { NavLink, useNavigate } from "react-router-dom";
import { LogOut } from "lucide-react";
import { navigationItems } from "@/constants/navigation";
import { logout as logoutRequest } from "@/services/auth-service";
import { useAuthStore } from "@/store/auth-store";
import { useUIStore } from "@/store/ui-store";
import { useNotification } from "@/hooks/useNotification";
import { colors } from "@/constants/colors";

/**
 * Sidebar Component
 *
 * Main navigation component with:
 * - Collapsible state management
 * - Feature-grouped navigation items
 * - Logout functionality
 * - Accessibility features (ARIA labels, keyboard navigation)
 *
 * Decision Log:
 * - Uses Zustand stores (UI for collapse state, Auth for user session)
 * - Color system from constants/colors.ts for consistency
 * - Notification hook for feedback on logout
 * - Lucide icons for better icon library consistency
 */
export const Sidebar = () => {
  const navigate = useNavigate();
  const { sidebarCollapsed } = useUIStore();
  const clearSession = useAuthStore((state) => state.logout);
  const { error, info } = useNotification();

  const handleLogout = async () => {
    try {
      await logoutRequest();
      info("Sesión cerrada exitosamente");
    } catch (err) {
      console.error("Error al cerrar sesión", err);
      error("Error al cerrar sesión");
    } finally {
      clearSession();
      navigate("/login", { replace: true });
    }
  };

  return (
    <aside
      aria-label="Menú principal de navegación"
      className={`flex h-screen flex-col bg-casal text-white shadow-lg transition-all duration-300 border-r ${
        sidebarCollapsed ? "w-20" : "w-72"
      }`}
      style={{ borderColor: colors.primary[600] }}
    >
      {/* Logo Section */}
      <div className="flex items-center gap-3 px-5 py-6 border-b" style={{ borderColor: colors.primary[600] }}>
        <div
          className="flex h-12 w-12 items-center justify-center rounded-xl text-lg font-bold uppercase tracking-wide text-casal shadow-md"
          style={{ backgroundColor: '#EFEFEF' }}
        >
          TL
        </div>
        {!sidebarCollapsed && (
          <div className="space-y-1">
            <p className="text-xs font-semibold uppercase tracking-[0.3em] text-white/70">
              Tutolink
            </p>
            <p className="text-lg font-semibold text-white">
              Gestión académica
            </p>
          </div>
        )}
      </div>

      {/* Navigation */}
      <nav className="flex-1 space-y-1 px-3 py-4 overflow-y-auto">
        {navigationItems.map((item) => {
          const Icon = item.icon;

          return (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `group relative flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors duration-200 ${
                  isActive
                    ? "text-white shadow-sm"
                    : "text-white/70 hover:text-white hover:bg-white/10"
                }`
              }
              style={({ isActive }) => ({
                backgroundColor: isActive ? colors.primary[600] : undefined,
              })}
              title={sidebarCollapsed ? item.label : undefined}
            >
              <span className="flex h-10 w-10 items-center justify-center rounded-lg flex-shrink-0 transition-colors duration-200 group-hover:bg-gray-200" style={{
                backgroundColor: 'inherit',
              }}>
                <Icon aria-hidden="true" className="h-5 w-5" />
                <span className="sr-only">{item.label}</span>
              </span>
              {!sidebarCollapsed && (
                <span className="text-sm font-medium truncate">{item.label}</span>
              )}
            </NavLink>
          );
        })}
      </nav>

      {/* Logout Button */}
      <div className="border-t px-4 py-4" style={{ borderColor: colors.primary[600] }}>
        <button
          onClick={handleLogout}
          className="w-full flex items-center justify-center gap-2 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors duration-200 text-white hover:opacity-90 focus:outline-none focus:ring-2 focus:ring-offset-2 bg-red-600 hover:bg-red-700"
          type="button"
          title="Cerrar sesión"
        >
          <LogOut className="h-5 w-5" />
          {!sidebarCollapsed && <span>Cerrar sesión</span>}
        </button>
      </div>
    </aside>
  );
};
