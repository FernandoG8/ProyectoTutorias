import { useMemo } from "react";
import {
  LineChart,
  Line,
  AreaChart,
  Area,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  CartesianGrid,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from "recharts";
import { TrendingUp } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { colors } from "@/constants/colors";
import { MetricsCard } from "@/components/common/MetricsCard";

/**
 * AnalyticsDashboard Component
 *
 * Comprehensive analytics visualizations with:
 * - Trend analysis (line charts)
 * - Distribution analysis (pie charts)
 * - Performance metrics (bar charts)
 * - Key indicators with trend direction
 * - Interactive tooltips
 *
 * Part of Week 4 analytics module
 *
 * Decision Log:
 * - Separate charts for different data types
 * - Recharts for complex visualizations
 * - Color-coded trends (up/down)
 * - Responsive container for mobile
 */

interface TrendData {
  date: string;
  value: number;
  [key: string]: any;
}

interface DistributionData {
  name: string;
  value: number;
  [key: string]: any;
}

interface AnalyticsDashboardProps {
  title: string;
  description?: string;

  // Key metrics
  metrics?: Array<{
    label: string;
    value: number;
    unit?: string;
    trend?: "up" | "down" | "neutral";
    trendValue?: string;
    icon?: React.ReactNode;
    color?: "primary" | "success" | "warning" | "danger" | "info";
  }>;

  // Trend chart data
  trendData?: TrendData[];
  trendLabel?: string;
  trendMultiLine?: boolean;

  // Distribution data (pie chart)
  distributionData?: DistributionData[];
  distributionLabel?: string;

  // Performance data (bar chart)
  performanceData?: Array<{
    category: string;
    [key: string]: any;
  }>;
  performanceLabel?: string;

  // Custom colors
  chartColor?: string;
  isLoading?: boolean;
}

export const AnalyticsDashboard = ({
  title,
  description,
  metrics = [],
  trendData,
  trendLabel = "Tendencia",
  trendMultiLine = false,
  distributionData,
  distributionLabel = "Distribución",
  performanceData,
  performanceLabel = "Rendimiento",
  chartColor = colors.primary[400],
  isLoading = false,
}: AnalyticsDashboardProps) => {
  const chartColors = [
    colors.primary[400],
    colors.success[400],
    colors.warning[400],
    colors.danger[400],
    colors.info[400],
  ];

  const memoizedTrendData = useMemo(() => trendData || [], [trendData]);
  const memoizedDistributionData = useMemo(() => distributionData || [], [distributionData]);
  const memoizedPerformanceData = useMemo(() => performanceData || [], [performanceData]);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h2
          className="text-2xl font-bold mb-1"
          style={{ color: colors.semantic.text.primary }}
        >
          {title}
        </h2>
        {description && (
          <p
            className="text-sm"
            style={{ color: colors.semantic.text.secondary }}
          >
            {description}
          </p>
        )}
      </div>

      {/* Key Metrics Row */}
      {metrics.length > 0 && (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          {metrics.map((metric, idx) => (
            <MetricsCard
              key={idx}
              label={metric.label}
              value={metric.value}
              unit={metric.unit}
              trend={metric.trend}
              trendValue={metric.trendValue}
              icon={metric.icon}
              color={metric.color || "primary"}
            />
          ))}
        </div>
      )}

      {/* Charts Grid */}
      <div className="grid gap-6 lg:grid-cols-3">
        {/* Trend Chart */}
        {memoizedTrendData.length > 0 && (
          <Card className="lg:col-span-2">
            <div className="mb-6">
              <h3
                className="text-lg font-semibold mb-1"
                style={{ color: colors.semantic.text.primary }}
              >
                {trendLabel}
              </h3>
              <p
                className="text-sm"
                style={{ color: colors.semantic.text.secondary }}
              >
                Evolución temporal de los indicadores
              </p>
            </div>

            <div className="h-80 -mx-4">
              <ResponsiveContainer width="100%" height="100%">
                {trendMultiLine ? (
                  <LineChart
                    data={memoizedTrendData}
                    margin={{ top: 5, right: 30, left: 0, bottom: 5 }}
                  >
                    <CartesianGrid
                      strokeDasharray="3 3"
                      stroke={colors.semantic.border}
                    />
                    <XAxis
                      dataKey="date"
                      stroke={colors.semantic.text.muted}
                    />
                    <YAxis stroke={colors.semantic.text.muted} />
                    <Tooltip
                      contentStyle={{
                        backgroundColor: "white",
                        border: `1px solid ${colors.semantic.border}`,
                        borderRadius: 8,
                      }}
                    />
                    <Legend />
                    {Object.keys(memoizedTrendData[0] || {})
                      .filter((k) => k !== "date")
                      .map((key, idx) => (
                        <Line
                          key={key}
                          type="monotone"
                          dataKey={key}
                          stroke={chartColors[idx % chartColors.length]}
                          strokeWidth={2}
                          dot={false}
                        />
                      ))}
                  </LineChart>
                ) : (
                  <AreaChart
                    data={memoizedTrendData}
                    margin={{ top: 5, right: 30, left: 0, bottom: 5 }}
                  >
                    <CartesianGrid
                      strokeDasharray="3 3"
                      stroke={colors.semantic.border}
                    />
                    <XAxis
                      dataKey="date"
                      stroke={colors.semantic.text.muted}
                    />
                    <YAxis stroke={colors.semantic.text.muted} />
                    <Tooltip
                      contentStyle={{
                        backgroundColor: "white",
                        border: `1px solid ${colors.semantic.border}`,
                        borderRadius: 8,
                      }}
                    />
                    <Area
                      type="monotone"
                      dataKey="value"
                      fill={chartColor}
                      stroke={chartColor}
                      fillOpacity={0.2}
                    />
                  </AreaChart>
                )}
              </ResponsiveContainer>
            </div>
          </Card>
        )}

        {/* Distribution Chart */}
        {memoizedDistributionData.length > 0 && (
          <Card>
            <div className="mb-6">
              <h3
                className="text-lg font-semibold mb-1"
                style={{ color: colors.semantic.text.primary }}
              >
                {distributionLabel}
              </h3>
              <p
                className="text-sm"
                style={{ color: colors.semantic.text.secondary }}
              >
                Proporción de elementos
              </p>
            </div>

            <div className="h-80 -mx-4">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={memoizedDistributionData}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    label={({ name, percent = 0 }) =>
                      `${name}: ${(percent * 100).toFixed(0)}%`
                    }
                    outerRadius={80}
                    fill="#8884d8"
                    dataKey="value"
                  >
                    {memoizedDistributionData.map((_, index) => (
                      <Cell
                        key={`cell-${index}`}
                        fill={chartColors[index % chartColors.length]}
                      />
                    ))}
                  </Pie>
                  <Tooltip
                    contentStyle={{
                      backgroundColor: "white",
                      border: `1px solid ${colors.semantic.border}`,
                      borderRadius: 8,
                    }}
                  />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </Card>
        )}
      </div>

      {/* Performance Chart */}
      {memoizedPerformanceData.length > 0 && (
        <Card>
          <div className="mb-6">
            <h3
              className="text-lg font-semibold mb-1"
              style={{ color: colors.semantic.text.primary }}
            >
              {performanceLabel}
            </h3>
            <p
              className="text-sm"
              style={{ color: colors.semantic.text.secondary }}
            >
              Comparación de rendimiento por categoría
            </p>
          </div>

          <div className="h-80 -mx-4">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart
                data={memoizedPerformanceData}
                margin={{ top: 20, right: 30, left: 0, bottom: 50 }}
              >
                <CartesianGrid
                  strokeDasharray="3 3"
                  stroke={colors.semantic.border}
                />
                <XAxis
                  dataKey="category"
                  stroke={colors.semantic.text.muted}
                  angle={-45}
                  textAnchor="end"
                  height={80}
                />
                <YAxis stroke={colors.semantic.text.muted} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: "white",
                    border: `1px solid ${colors.semantic.border}`,
                    borderRadius: 8,
                  }}
                />
                <Legend />
                {Object.keys(memoizedPerformanceData[0] || {})
                  .filter((k) => k !== "category")
                  .map((key, idx) => (
                    <Bar
                      key={key}
                      dataKey={key}
                      fill={chartColors[idx % chartColors.length]}
                      radius={[8, 8, 0, 0]}
                    />
                  ))}
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Card>
      )}

      {/* Empty State */}
      {!isLoading &&
        !memoizedTrendData.length &&
        !memoizedDistributionData.length &&
        !memoizedPerformanceData.length && (
          <Card>
            <div className="text-center py-12">
              <TrendingUp
                className="h-12 w-12 mx-auto mb-4 opacity-30"
                style={{ color: colors.semantic.text.muted }}
              />
              <p
                className="text-sm"
                style={{ color: colors.semantic.text.muted }}
              >
                No hay datos disponibles para visualizar
              </p>
            </div>
          </Card>
        )}
    </div>
  );
};
