import type { ReactNode } from "react";
import { colors } from "@/constants/colors";

interface PageHeaderProps {
  title: string;
  description?: string;
  icon?: ReactNode;
  actions?: ReactNode;
  stats?: Array<{ label: string; value: string | number }>;
  breadcrumbs?: Array<{ label: string; href?: string }>;
}

/**
 * PageHeader Component
 *
 * Encabezado profesional para páginas
 * Incluye: título, descripción, breadcrumbs, acciones, estadísticas
 *
 * Part of SaaS Design System
 */
export const PageHeader = ({
  title,
  description,
  icon,
  actions,
  stats,
  breadcrumbs,
}: PageHeaderProps) => {
  return (
    <div className="space-y-4">
      {/* Breadcrumbs */}
      {breadcrumbs && breadcrumbs.length > 0 && (
        <div className="flex gap-2 text-sm">
          {breadcrumbs.map((crumb, i) => (
            <div key={i} className="flex items-center gap-2">
              {crumb.href ? (
                <a
                  href={crumb.href}
                  style={{ color: colors.primary[600] }}
                  className="hover:underline"
                >
                  {crumb.label}
                </a>
              ) : (
                <span style={{ color: colors.semantic.text.muted }}>
                  {crumb.label}
                </span>
              )}
              {i < breadcrumbs.length - 1 && (
                <span style={{ color: colors.semantic.text.muted }}>/</span>
              )}
            </div>
          ))}
        </div>
      )}

      {/* Header content */}
      <div className="flex items-start justify-between">
        <div className="flex items-start gap-3">
          {icon && <div className="text-2xl">{icon}</div>}
          <div>
            <h1
              className="text-3xl font-bold"
              style={{ color: colors.semantic.text.primary }}
            >
              {title}
            </h1>
            {description && (
              <p
                className="text-sm mt-1"
                style={{ color: colors.semantic.text.secondary }}
              >
                {description}
              </p>
            )}
          </div>
        </div>

        {/* Actions */}
        {actions && <div className="flex gap-2">{actions}</div>}
      </div>

      {/* Stats */}
      {stats && stats.length > 0 && (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
          {stats.map((stat, i) => (
            <div key={i} className="rounded-lg border p-3" style={{ borderColor: colors.semantic.border }}>
              <p
                className="text-xs font-medium"
                style={{ color: colors.semantic.text.muted }}
              >
                {stat.label}
              </p>
              <p
                className="text-xl font-semibold mt-1"
                style={{ color: colors.semantic.text.primary }}
              >
                {stat.value}
              </p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
