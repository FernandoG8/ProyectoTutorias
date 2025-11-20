import { useMemo } from "react";
import { useLocation } from "react-router-dom";
import { Menu } from "lucide-react";
import { navigationItems } from "@/constants/navigation";
import { useAuthStore } from "@/store/auth-store";
import { useUIStore } from "@/store/ui-store";
import { SemestreSelector } from "@/components/common/SemestreSelector";
import { colors } from "@/constants/colors";

/**
 * Topbar Component
 *
 * Header component with:
 * - Sidebar toggle button
 * - Current page title/breadcrumb
 * - Semester selector
 * - User profile information
 * - Sticky positioning for persistent access
 *
 * Decision Log:
 * - Uses semantic color system from constants/colors.ts
 * - Lucide icons for consistency
 * - Memoized route detection to prevent unnecessary re-renders
 * - Improved accessibility with ARIA labels
 * - Responsive layout with flexbox
 */
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

  const initials = user?.username ? user.username.substring(0, 2).toUpperCase() : "IN";

  return (
    <header
      className="sticky top-0 z-10 flex items-center justify-between border-b bg-casal text-white px-6 py-4 shadow-sm"
      style={{ borderColor: colors.primary[600] }}
    >
      {/* Left Section: Menu Toggle & Title */}
      <div className="flex items-center gap-4 flex-1">
        <button
          onClick={toggleSidebar}
          className="inline-flex h-10 w-10 items-center justify-center rounded-lg border transition-colors duration-200 hover:bg-white/10 focus:outline-none focus:ring-2 focus:ring-offset-2 text-white border-white/30"
          type="button"
          aria-label="Alternar menú"
          title="Mostrar/ocultar menú"
        >
          <Menu className="h-5 w-5" aria-hidden="true" />
        </button>
        <div className="min-w-0">
          <p
            className="text-xs font-semibold uppercase tracking-wider text-white/70"
          >
            Módulo actual
          </p>
          <h1
            className="text-xl font-bold truncate text-white"
          >
            {activeRoute}
          </h1>
        </div>
      </div>

      {/* Right Section: Semester Selector & User Profile */}
      <div className="flex items-center gap-6">
        <SemestreSelector />

        {/* User Profile Card */}
        <div
          className="flex items-center gap-3 rounded-lg border px-4 py-2 bg-white/10 border-white/20"
        >
          <div className="text-right">
            <p
              className="text-sm font-semibold text-white"
            >
              {user?.username ?? "Invitado"}
            </p>
            <p
              className="text-xs text-white/70"
            >
              {roleLabel}
            </p>
          </div>
          <div
            className="flex h-10 w-10 items-center justify-center rounded-lg text-sm font-semibold text-casal bg-white"
            title={user?.username ?? "Usuario"}
          >
            {initials}
          </div>
        </div>
      </div>
    </header>
  );
};
