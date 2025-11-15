import { Skeleton } from "@/components/ui/Skeleton";
import { colors } from "@/constants/colors";

/**
 * StatCard Component
 *
 * Compact stat display for:
 * - Dashboard metric cards
 * - KPI displays
 * - Quick statistics
 *
 * Simpler than MetricsCard - just value + label
 * Optimized for grid layouts
 *
 * Part of Week 3 Dashboard enhancements
 */

interface StatCardProps {
  label: string;
  value: string | number | null;
  suffix?: string;
  variant?: "primary" | "success" | "warning" | "danger";
  isLoading?: boolean;
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
      </div>
    );
  }

  return (
    <div
      className="rounded-lg border p-6"
      style={{
        borderColor: colors.semantic.border,
        backgroundColor: bg,
      }}
    >
      <p
        className="text-sm font-medium mb-2"
        style={{ color: colors.semantic.text.secondary }}
      >
        {label}
      </p>
      <div className="flex items-baseline gap-1">
        <p className="text-3xl font-bold" style={{ color: text }}>
          {value ?? "--"}
        </p>
        {suffix && (
          <p className="text-sm" style={{ color: colors.semantic.text.muted }}>
            {suffix}
          </p>
        )}
      </div>
    </div>
  );
};
