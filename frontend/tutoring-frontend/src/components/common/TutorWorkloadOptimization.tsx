import { useMemo } from "react";
import { BarChart, Bar, CartesianGrid, XAxis, YAxis, Tooltip, ResponsiveContainer, Legend, ScatterChart, Scatter } from "recharts";
import { Zap, TrendingUp, AlertTriangle} from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { colors } from "@/constants/colors";

/**
 * TutorWorkloadOptimization Component
 *
 * Advanced tutor workload analysis and optimization tools with:
 * - Workload distribution visualization
 * - Capacity utilization metrics
 * - Rebalancing recommendations
 * - Performance impact analysis
 * - Optimization suggestions
 *
 * Part of Week 4 advanced features
 *
 * Decision Log:
 * - Scatter plot for workload vs performance correlation
 * - Bar chart for current distribution
 * - Recommendations based on capacity and performance
 * - Color-coded utilization levels
 */

interface TutorWorkload {
  tutorId: number;
  tutorName: string;
  carrera: string;
  currentLoad: number;
  capacity: number;
  performance?: number; // 0-100 scale
  area?: string;
  averageStudentGPA?: number;
  satisfactionRating?: number; // 1-5
}

interface OptimizationRecommendation {
  type: "overloaded" | "underutilized" | "imbalance" | "risk";
  severity: "low" | "medium" | "high";
  tutorId: number;
  tutorName: string;
  message: string;
  suggestedAction: string;
}

interface TutorWorkloadOptimizationProps {
  tutors?: TutorWorkload[];
  recommendations?: OptimizationRecommendation[];
  targetUtilization?: number; // 0-1, e.g., 0.85 for 85%
  isLoading?: boolean;
  onOptimize?: () => Promise<void>;
  onRebalance?: () => Promise<void>;
}

export const TutorWorkloadOptimization = ({
  tutors = [],
  recommendations = [],
  targetUtilization = 0.85,
  onOptimize,
  onRebalance,
}: TutorWorkloadOptimizationProps) => {
  const workloadMetrics = useMemo(() => {
    if (tutors.length === 0) {
      return {
        totalLoad: 0,
        totalCapacity: 0,
        averageUtilization: 0,
        maxLoad: 0,
        minLoad: 0,
        overloadedTutors: 0,
        underutilizedTutors: 0,
      };
    }

    const totalLoad = tutors.reduce((sum, t) => sum + t.currentLoad, 0);
    const totalCapacity = tutors.reduce((sum, t) => sum + t.capacity, 0);
    const utilizations = tutors.map((t) => t.currentLoad / t.capacity);
    const averageUtilization = utilizations.reduce((a, b) => a + b, 0) / tutors.length;

    return {
      totalLoad,
      totalCapacity,
      averageUtilization,
      maxLoad: Math.max(...tutors.map((t) => t.currentLoad)),
      minLoad: Math.min(...tutors.map((t) => t.currentLoad)),
      overloadedTutors: tutors.filter((t) => t.currentLoad / t.capacity > 1).length,
      underutilizedTutors: tutors.filter((t) => t.currentLoad / t.capacity < 0.5).length,
    };
  }, [tutors]);

  const chartData = useMemo(() => {
    return tutors.map((tutor) => ({
      name: tutor.tutorName.split(" ")[0], // First name for space
      current: tutor.currentLoad,
      capacity: tutor.capacity,
      utilization: Math.round((tutor.currentLoad / tutor.capacity) * 100),
    }));
  }, [tutors]);

  const scatterData = useMemo(() => {
    return tutors.map((tutor) => ({
      tutorId: tutor.tutorId,
      tutorName: tutor.tutorName,
      utilization: Math.round((tutor.currentLoad / tutor.capacity) * 100),
      performance: tutor.performance || 75,
      gpa: tutor.averageStudentGPA || 3.2,
      satisfaction: tutor.satisfactionRating || 4,
    }));
  }, [tutors]);

  const balanceScore = useMemo(() => {
    const utilizations = tutors.map((t) => t.currentLoad / t.capacity);
    const variance = utilizations.reduce((sum, u) => sum + Math.pow(u - workloadMetrics.averageUtilization, 2), 0) / tutors.length;
    const stdDev = Math.sqrt(variance);
    // Score: 0-100, where lower variance = higher score
    return Math.round(Math.max(0, 100 - stdDev * 100));
  }, [tutors, workloadMetrics.averageUtilization]);

  const getUtilizationColor = (utilization: number): string => {
    if (utilization > 1.1) return colors.danger[400]; // >110% (overloaded)
    if (utilization > 1) return colors.danger[300]; // >100% (over capacity)
    if (utilization >= targetUtilization) return colors.success[400]; // Good
    if (utilization >= 0.5) return colors.warning[400]; // Underutilized
    return colors.info[400]; // Low utilization
  };

  const getUtilizationLabel = (utilization: number): string => {
    if (utilization > 1.1) return "Sobrecargado";
    if (utilization > 1) return "Sobre capacidad";
    if (utilization >= targetUtilization) return "Óptimo";
    if (utilization >= 0.5) return "Infrautilizado";
    return "Muy bajo";
  };

  const highPriorityRecommendations = useMemo(() => {
    return recommendations.filter((r) => r.severity === "high").slice(0, 3);
  }, [recommendations]);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h2
          className="text-2xl font-bold mb-1"
          style={{ color: colors.semantic.text.primary }}
        >
          Optimización de Cargas de Tutores
        </h2>
        <p
          className="text-sm"
          style={{ color: colors.semantic.text.secondary }}
        >
          Análisis de distribución de carga, recomendaciones de rebalance y métricas de desempeño.
        </p>
      </div>

      {/* Key Metrics */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {/* Average Utilization */}
        <Card>
          <div className="space-y-2">
            <p
              className="text-xs font-semibold uppercase"
              style={{ color: colors.semantic.text.secondary }}
            >
              Utilización Promedio
            </p>
            <p
              className="text-3xl font-bold"
              style={{
                color: getUtilizationColor(workloadMetrics.averageUtilization),
              }}
            >
              {Math.round(workloadMetrics.averageUtilization * 100)}%
            </p>
            <p
              className="text-xs"
              style={{ color: colors.semantic.text.muted }}
            >
              Meta: {Math.round(targetUtilization * 100)}%
            </p>
          </div>
        </Card>

        {/* Total Workload */}
        <Card>
          <div className="space-y-2">
            <p
              className="text-xs font-semibold uppercase"
              style={{ color: colors.semantic.text.secondary }}
            >
              Carga Total
            </p>
            <p className="text-3xl font-bold" style={{ color: colors.primary[600] }}>
              {workloadMetrics.totalLoad}
            </p>
            <p
              className="text-xs"
              style={{ color: colors.semantic.text.muted }}
            >
              de {workloadMetrics.totalCapacity} disponibles
            </p>
          </div>
        </Card>

        {/* Balance Score */}
        <Card>
          <div className="space-y-2">
            <p
              className="text-xs font-semibold uppercase"
              style={{ color: colors.semantic.text.secondary }}
            >
              Puntuación de Balance
            </p>
            <p className="text-3xl font-bold" style={{ color: colors.success[600] }}>
              {balanceScore}
            </p>
            <p
              className="text-xs"
              style={{ color: colors.semantic.text.muted }}
            >
              Distribución uniforme
            </p>
          </div>
        </Card>

        {/* Problem Tutors */}
        <Card>
          <div className="space-y-2">
            <p
              className="text-xs font-semibold uppercase"
              style={{ color: colors.semantic.text.secondary }}
            >
              Tutores en Riesgo
            </p>
            <p className="text-3xl font-bold" style={{ color: colors.danger[600] }}>
              {workloadMetrics.overloadedTutors}
            </p>
            <p
              className="text-xs"
              style={{ color: colors.semantic.text.muted }}
            >
              Requieren rebalance
            </p>
          </div>
        </Card>
      </div>

      {/* High Priority Recommendations */}
      {highPriorityRecommendations.length > 0 && (
        <Card>
          <div className="flex items-center gap-2 mb-4">
            <AlertTriangle className="h-5 w-5" style={{ color: colors.danger[500] }} />
            <h3
              className="text-lg font-semibold"
              style={{ color: colors.danger[900] }}
            >
              Recomendaciones Prioritarias
            </h3>
          </div>

          <div className="space-y-3">
            {highPriorityRecommendations.map((rec, idx) => (
              <div
                key={idx}
                className="rounded-lg border p-4"
                style={{
                  borderColor: colors.danger[200],
                  backgroundColor: colors.danger[50],
                }}
              >
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <p
                      className="font-semibold text-sm"
                      style={{ color: colors.danger[900] }}
                    >
                      {rec.tutorName}
                    </p>
                    <p
                      className="text-sm mt-1"
                      style={{ color: colors.danger[800] }}
                    >
                      {rec.message}
                    </p>
                    <p
                      className="text-xs mt-2 p-2 rounded bg-white"
                      style={{ color: colors.danger[700] }}
                    >
                      Acción: {rec.suggestedAction}
                    </p>
                  </div>
                  <Badge
                    variant={
                      rec.type === "overloaded"
                        ? "danger"
                        : rec.type === "risk"
                          ? "warning"
                          : "info"
                    }
                  >
                    {rec.type}
                  </Badge>
                </div>
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* Workload Distribution Chart */}
      {chartData.length > 0 && (
        <Card>
          <div className="mb-6">
            <h3
              className="text-lg font-semibold mb-1"
              style={{ color: colors.semantic.text.primary }}
            >
              Distribución de Cargas
            </h3>
            <p
              className="text-sm"
              style={{ color: colors.semantic.text.secondary }}
            >
              Carga actual vs capacidad por tutor
            </p>
          </div>

          <div className="h-80 -mx-4">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart
                data={chartData}
                margin={{ top: 20, right: 30, left: 0, bottom: 50 }}
              >
                <CartesianGrid strokeDasharray="3 3" stroke={colors.semantic.border} />
                <XAxis
                  dataKey="name"
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
                <Bar dataKey="current" fill={colors.primary[400]} radius={[8, 8, 0, 0]} name="Carga actual" />
                <Bar dataKey="capacity" fill={colors.success[300]} radius={[8, 8, 0, 0]} name="Capacidad" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Card>
      )}

      {/* Utilization vs Performance Scatter */}
      {scatterData.length > 0 && (
        <Card>
          <div className="mb-6">
            <h3
              className="text-lg font-semibold mb-1"
              style={{ color: colors.semantic.text.primary }}
            >
              Análisis: Utilización vs Desempeño
            </h3>
            <p
              className="text-sm"
              style={{ color: colors.semantic.text.secondary }}
            >
              Correlación entre carga de trabajo y métricas de desempeño
            </p>
          </div>

          <div className="h-80 -mx-4">
            <ResponsiveContainer width="100%" height="100%">
              <ScatterChart
                margin={{ top: 20, right: 20, bottom: 20, left: 20 }}
              >
                <CartesianGrid strokeDasharray="3 3" stroke={colors.semantic.border} />
                <XAxis
                  dataKey="utilization"
                  name="Utilización %"
                  stroke={colors.semantic.text.muted}
                />
                <YAxis
                  dataKey="performance"
                  name="Desempeño"
                  stroke={colors.semantic.text.muted}
                />
                <Tooltip
                  cursor={{ fill: colors.primary[100] }}
                  contentStyle={{
                    backgroundColor: "white",
                    border: `1px solid ${colors.semantic.border}`,
                    borderRadius: 8,
                  }}
                  formatter={(value) => Math.round(Number(value))}
                />
                <Scatter
                  name="Tutores"
                  data={scatterData}
                  fill={colors.primary[400]}
                  fillOpacity={0.7}
                />
              </ScatterChart>
            </ResponsiveContainer>
          </div>
        </Card>
      )}

      {/* Tutor Status Grid */}
      {tutors.length > 0 && (
        <Card>
          <h3
            className="text-lg font-semibold mb-4"
            style={{ color: colors.semantic.text.primary }}
          >
            Estado de Tutores
          </h3>

          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr style={{ borderBottomColor: colors.semantic.border, borderBottomWidth: 1 }}>
                  <th
                    className="text-left py-3 px-4 font-semibold"
                    style={{ color: colors.semantic.text.secondary }}
                  >
                    Tutor
                  </th>
                  <th
                    className="text-left py-3 px-4 font-semibold"
                    style={{ color: colors.semantic.text.secondary }}
                  >
                    Carrera
                  </th>
                  <th
                    className="text-center py-3 px-4 font-semibold"
                    style={{ color: colors.semantic.text.secondary }}
                  >
                    Carga
                  </th>
                  <th
                    className="text-center py-3 px-4 font-semibold"
                    style={{ color: colors.semantic.text.secondary }}
                  >
                    Utilización
                  </th>
                  <th
                    className="text-center py-3 px-4 font-semibold"
                    style={{ color: colors.semantic.text.secondary }}
                  >
                    GPA Promedio
                  </th>
                  <th
                    className="text-center py-3 px-4 font-semibold"
                    style={{ color: colors.semantic.text.secondary }}
                  >
                    Estado
                  </th>
                </tr>
              </thead>
              <tbody>
                {tutors.map((tutor, idx) => {
                  const utilization = tutor.currentLoad / tutor.capacity;
                  return (
                    <tr
                      key={tutor.tutorId}
                      style={{
                        borderBottomColor: colors.semantic.border,
                        borderBottomWidth: 1,
                        backgroundColor: idx % 2 === 0 ? "transparent" : colors.semantic.background,
                      }}
                    >
                      <td className="py-3 px-4">
                        <p className="font-semibold" style={{ color: colors.semantic.text.primary }}>
                          {tutor.tutorName}
                        </p>
                        {tutor.area && (
                          <p
                            className="text-xs"
                            style={{ color: colors.semantic.text.secondary }}
                          >
                            {tutor.area}
                          </p>
                        )}
                      </td>
                      <td className="py-3 px-4" style={{ color: colors.semantic.text.secondary }}>
                        {tutor.carrera}
                      </td>
                      <td className="py-3 px-4 text-center">
                        <p
                          className="font-semibold"
                          style={{ color: colors.semantic.text.primary }}
                        >
                          {tutor.currentLoad}/{tutor.capacity}
                        </p>
                      </td>
                      <td className="py-3 px-4 text-center">
                        <Badge variant={utilization > 1 ? "danger" : utilization >= targetUtilization ? "success" : "warning"}>
                          {Math.round(utilization * 100)}%
                        </Badge>
                      </td>
                      <td className="py-3 px-4 text-center" style={{ color: colors.semantic.text.primary }}>
                        {tutor.averageStudentGPA?.toFixed(2) || "N/A"}
                      </td>
                      <td className="py-3 px-4 text-center">
                        <Badge variant={utilization > 1 ? "danger" : "success"}>
                          {getUtilizationLabel(utilization)}
                        </Badge>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </Card>
      )}

      {/* Actions */}
      {(onOptimize || onRebalance) && (
        <Card>
          <div className="flex flex-col sm:flex-row gap-3">
            {onOptimize && (
              <Button
                onClick={onOptimize}
                className="flex-1 gap-2"
                style={{ backgroundColor: colors.primary[400] }}
              >
                <Zap className="h-4 w-4" />
                Ejecutar Optimización
              </Button>
            )}
            {onRebalance && (
              <Button
                onClick={onRebalance}
                variant="secondary"
                className="flex-1 gap-2"
              >
                <TrendingUp className="h-4 w-4" />
                Rebalancear Cargas
              </Button>
            )}
          </div>
        </Card>
      )}
    </div>
  );
};
