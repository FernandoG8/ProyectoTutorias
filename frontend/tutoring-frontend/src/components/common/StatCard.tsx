import { Skeleton } from "@/components/ui/Skeleton";
import { colors } from "@/constants/colors";
import { ArrowUp, ArrowDown } from "lucide-react";

/**
 * StatCard Component
 *
 * Compact stat display for:
 * - Dashboard metric cards
 * - KPI displays
 * - Quick statistics
 * - Trending comparisons
 *
 * Features:
 * - Simple value + label
 * - Optional trending arrows
 * - Comparison with previous value
 * - Loading skeleton state
 * - Color variants
 *
 * Part of Dashboard rediseño profesional SaaS
 */

interface StatCardProps {
  label: string;
  value: string | number | null;
  suffix?: string;
  variant?: "primary" | "success" | "warning" | "danger";
  isLoading?: boolean;
  trend?: "up" | "down" | null;
  trendValue?: string | number;
  previousValue?: number;
  comparison?: string; // e.g., "vs 24 semana anterior"
}

const variantColors = {
  primary: {
    bg: colors.primary[50],
    text: colors.primary[700],
  },
  success: {
    bg: colors.success[50],
    text: colors.success[700],
  },
  warning: {
    bg: colors.warning[50],
    text: colors.warning[700],
  },
  danger: {
    bg: colors.danger[50],
    text: colors.danger[700],
  },
};

export const StatCard = ({
  label,
  value,
  suffix,
  variant = "primary",
  isLoading,
  trend,
  trendValue,
  comparison,
}: StatCardProps) => {
  const { bg, text } = variantColors[variant];

  if (isLoading) {
    return (
      <div
        className="rounded-lg border p-6 space-y-3"
        style={{ borderColor: colors.semantic.border }}
      >
        <Skeleton className="h-4 w-24" />
        <Skeleton className="h-8 w-32" />
        <Skeleton className="h-3 w-20" />
      </div>
    );
  }

  const getTrendColor = () => {
    if (trend === "up") return colors.success[600];
    if (trend === "down") return colors.warning[600];
    return colors.semantic.text.muted;
  };

  return (
    <div
      className="rounded-lg border p-6"
      style={{
        borderColor: colors.semantic.border,
        backgroundColor: bg,
      }}
    >
      <p
        className="text-sm font-medium mb-3"
        style={{ color: colors.semantic.text.secondary }}
      >
        {label}
      </p>
      <div className="flex items-baseline gap-1 mb-2">
        <p className="text-3xl font-bold" style={{ color: text }}>
          {value ?? "--"}
        </p>
        {suffix && (
          <p className="text-sm" style={{ color: colors.semantic.text.muted }}>
            {suffix}
          </p>
        )}
      </div>

      {/* Trending indicators */}
      {(trend || comparison) && (
        <div className="flex items-center gap-1 text-xs" style={{ color: getTrendColor() }}>
          {trend && (
            <>
              {trend === "up" && <ArrowUp className="h-3 w-3" />}
              {trend === "down" && <ArrowDown className="h-3 w-3" />}
            </>
          )}
          {trendValue && <span className="font-medium">{trendValue}</span>}
          {comparison && <span>{comparison}</span>}
        </div>
      )}
    </div>
  );
};
