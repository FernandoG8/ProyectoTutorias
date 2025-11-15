import type { ReactNode } from "react";
import { TrendingUp, TrendingDown, Minus } from "lucide-react";
import { colors } from "@/constants/colors";

/**
 * MetricsCard Component
 *
 * Displays a single metric with:
 * - Large value display
 * - Subtitle/label
 * - Trend indicator (up, down, neutral)
 * - Status color coding
 *
 * Part of Week 3 Dashboard enhancements
 *
 * Decision Log:
 * - Minimal, focused design
 * - Trend visualization for quick insights
 * - Color-coded by metric type
 * - Responsive sizing
 */

interface MetricsCardProps {
  label: string;
  value: string | number;
  unit?: string;
  trend?: "up" | "down" | "neutral";
  trendValue?: string | number;
  subtitle?: string;
  icon?: ReactNode;
  color?: "primary" | "success" | "warning" | "danger" | "info";
  onClick?: () => void;
  isLoading?: boolean;
}

const colorMap = {
  primary: colors.primary[400],
  success: colors.success[400],
  warning: colors.warning[400],
  danger: colors.danger[400],
  info: colors.info[400],
};

const bgColorMap = {
  primary: colors.primary[50],
  success: colors.success[50],
  warning: colors.warning[50],
  danger: colors.danger[50],
  info: colors.info[50],
};

export const MetricsCard = ({
  label,
  value,
  unit,
  trend,
  trendValue,
  subtitle,
  icon,
  color = "primary",
  onClick,
}: MetricsCardProps) => {
  const cardColor = colorMap[color];
  const bgColor = bgColorMap[color];

  return (
    <div
      className="rounded-lg border p-6 bg-white transition-all hover:shadow-md cursor-pointer"
      style={{
        borderColor: colors.semantic.border,
        ...(onClick && { cursor: "pointer" }),
      }}
      onClick={onClick}
    >
      <div className="space-y-4">
        {/* Header with Icon */}
        <div className="flex items-start justify-between">
          <p
            className="text-sm font-medium"
            style={{ color: colors.semantic.text.secondary }}
          >
            {label}
          </p>
          {icon && (
            <div
              className="p-2 rounded-lg"
              style={{ backgroundColor: bgColor, color: cardColor }}
            >
              {icon}
            </div>
          )}
        </div>

        {/* Value */}
        <div className="space-y-1">
          <div className="flex items-baseline gap-2">
            <p
              className="text-3xl font-bold"
              style={{ color: colors.semantic.text.primary }}
            >
              {value}
            </p>
            {unit && (
              <p
                className="text-sm"
                style={{ color: colors.semantic.text.secondary }}
              >
                {unit}
              </p>
            )}
          </div>

          {/* Subtitle */}
          {subtitle && (
            <p
              className="text-xs"
              style={{ color: colors.semantic.text.muted }}
            >
              {subtitle}
            </p>
          )}
        </div>

        {/* Trend */}
        {trend && trendValue && (
          <div
            className="flex items-center gap-1 pt-2"
            style={{
              color:
                trend === "up"
                  ? colors.success[600]
                  : trend === "down"
                    ? colors.danger[600]
                    : colors.semantic.text.muted,
            }}
          >
            {trend === "up" && <TrendingUp className="h-4 w-4" />}
            {trend === "down" && <TrendingDown className="h-4 w-4" />}
            {trend === "neutral" && <Minus className="h-4 w-4" />}
            <span className="text-xs font-medium">{trendValue}</span>
          </div>
        )}
      </div>
    </div>
  );
};
