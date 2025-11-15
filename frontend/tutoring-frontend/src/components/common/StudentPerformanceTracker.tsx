import { useMemo } from "react";
import { AreaChart, Area, BarChart, Bar, CartesianGrid, XAxis, YAxis, Tooltip, ResponsiveContainer, Line } from "recharts";
import { TrendingUp, Award, AlertCircle, BookOpen } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { colors } from "@/constants/colors";

/**
 * StudentPerformanceTracker Component
 *
 * Comprehensive student performance analytics with:
 * - GPA trends over time
 * - Course performance breakdown
 * - Risk indicators and alerts
 * - Comparison with cohort averages
 * - Engagement metrics
 *
 * Part of Week 4 advanced features
 *
 * Decision Log:
 * - Trend visualization for GPA history
 * - Multiple performance indicators
 * - Risk-based color coding (green/yellow/red)
 * - Cohort comparison for context
 */

interface PerformanceMetric {
  label: string;
  value: number;
  target?: number;
  unit?: string;
  riskLevel?: "low" | "medium" | "high";
}

interface CoursePerformance {
  courseCode: string;
  courseName: string;
  grade: string;
  creditHours: number;
  instructor?: string;
  semester: string;
}

interface TrendDataPoint {
  semester: string;
  gpa: number;
  targetGPA?: number;
  coursesPassed?: number;
  coursesEnrolled?: number;
  [key: string]: any;
}

interface StudentPerformanceTrackerProps {
  studentName?: string;

  // Key metrics
  metrics?: PerformanceMetric[];

  // GPA trend data
  trendData?: TrendDataPoint[];
  currentGPA?: number;
  targetGPA?: number;

  // Course performance
  courses?: CoursePerformance[];

  // Cohort comparison
  cohortAverageGPA?: number;
  cohortAverageCoursePass?: number;

  // Risk indicators
  isAtRisk?: boolean;
  riskFactors?: string[];

  // Loading state
  isLoading?: boolean;
}

export const StudentPerformanceTracker = ({
  studentName = "Estudiante",
  metrics = [],
  trendData = [],
  currentGPA = 3.5,
  targetGPA = 3.0,
  courses = [],
  cohortAverageGPA = 3.2,
  cohortAverageCoursePass = 85,
  isAtRisk = false,
  riskFactors = [],
}: StudentPerformanceTrackerProps) => {
  const performanceStatus = useMemo(() => {
    if (currentGPA >= targetGPA + 0.5) return "excellent";
    if (currentGPA >= targetGPA) return "good";
    if (currentGPA >= targetGPA - 0.5) return "fair";
    return "at-risk";
  }, [currentGPA, targetGPA]);

  const getStatusLabel = (status: string) => {
    switch (status) {
      case "excellent":
        return "Excelente";
      case "good":
        return "Bueno";
      case "fair":
        return "Regular";
      default:
        return "En riesgo";
    }
  };

  const coursesByGrade = useMemo(() => {
    if (!courses.length) return {};
    const grouped = courses.reduce(
      (acc, course) => {
        const grade = course.grade.charAt(0);
        if (!acc[grade]) acc[grade] = 0;
        acc[grade]++;
        return acc;
      },
      {} as Record<string, number>
    );
    return grouped;
  }, [courses]);

  const courseDistribution = useMemo(() => {
    return Object.entries(coursesByGrade).map(([grade, count]) => ({
      grade: `Calificación ${grade}`,
      count,
    }));
  }, [coursesByGrade]);

  const passRate = useMemo(() => {
    if (!courses.length) return 0;
    const passed = courses.filter((c) => parseFloat(c.grade) >= 3.0).length;
    return Math.round((passed / courses.length) * 100);
  }, [courses]);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h2
          className="text-2xl font-bold mb-1"
          style={{ color: colors.semantic.text.primary }}
        >
          Desempeño Académico - {studentName}
        </h2>
        <p
          className="text-sm"
          style={{ color: colors.semantic.text.secondary }}
        >
          Análisis integral del rendimiento académico con tendencias y comparativas.
        </p>
      </div>

      {/* Risk Alert */}
      {isAtRisk && (
        <div
          className="rounded-lg border p-4 flex gap-3"
          style={{
            borderColor: colors.danger[200],
            backgroundColor: colors.danger[50],
          }}
        >
          <AlertCircle
            className="h-5 w-5 flex-shrink-0 mt-0.5"
            style={{ color: colors.danger[500] }}
          />
          <div>
            <p
              className="font-semibold text-sm"
              style={{ color: colors.danger[900] }}
            >
              Estudiante en riesgo académico
            </p>
            {riskFactors.length > 0 && (
              <ul className="text-xs mt-2 space-y-1" style={{ color: colors.danger[700] }}>
                {riskFactors.map((factor, idx) => (
                  <li key={idx}>• {factor}</li>
                ))}
              </ul>
            )}
          </div>
        </div>
      )}

      {/* Key Metrics */}
      {metrics.length > 0 && (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          {metrics.map((metric, idx) => {
            const statusColor =
              metric.riskLevel === "high"
                ? colors.danger[400]
                : metric.riskLevel === "medium"
                  ? colors.warning[400]
                  : colors.success[400];

            return (
              <Card key={idx}>
                <div className="space-y-2">
                  <p
                    className="text-xs font-semibold uppercase tracking-wide"
                    style={{ color: colors.semantic.text.secondary }}
                  >
                    {metric.label}
                  </p>
                  <div className="flex items-baseline gap-1">
                    <p
                      className="text-3xl font-bold"
                      style={{ color: statusColor }}
                    >
                      {metric.value.toFixed(2)}
                    </p>
                    {metric.unit && (
                      <p
                        className="text-sm font-medium"
                        style={{ color: colors.semantic.text.secondary }}
                      >
                        {metric.unit}
                      </p>
                    )}
                  </div>
                  {metric.target && (
                    <p
                      className="text-xs"
                      style={{ color: colors.semantic.text.muted }}
                    >
                      Meta: {metric.target.toFixed(2)}
                    </p>
                  )}
                </div>
              </Card>
            );
          })}
        </div>
      )}

      {/* GPA Status Card */}
      <Card>
        <div className="flex items-start justify-between">
          <div>
            <h3
              className="text-lg font-semibold mb-1"
              style={{ color: colors.semantic.text.primary }}
            >
              Promedio Ponderado Acumulado
            </h3>
            <p
              className="text-sm"
              style={{ color: colors.semantic.text.secondary }}
            >
              Comparación con meta institucional
            </p>
          </div>
          <Badge variant={performanceStatus === "excellent" ? "success" : performanceStatus === "at-risk" ? "danger" : "warning"}>
            {getStatusLabel(performanceStatus)}
          </Badge>
        </div>

        <div className="mt-6 grid gap-6 md:grid-cols-3">
          {/* Current GPA */}
          <div className="text-center p-4 rounded-lg" style={{ backgroundColor: colors.primary[50] }}>
            <p
              className="text-xs font-semibold uppercase mb-2"
              style={{ color: colors.semantic.text.secondary }}
            >
              GPA Actual
            </p>
            <p className="text-4xl font-bold" style={{ color: colors.primary[600] }}>
              {currentGPA.toFixed(2)}
            </p>
          </div>

          {/* Target GPA */}
          <div className="text-center p-4 rounded-lg" style={{ backgroundColor: colors.success[50] }}>
            <p
              className="text-xs font-semibold uppercase mb-2"
              style={{ color: colors.semantic.text.secondary }}
            >
              Meta Institucional
            </p>
            <p className="text-4xl font-bold" style={{ color: colors.success[600] }}>
              {targetGPA.toFixed(2)}
            </p>
          </div>

          {/* Cohort Average */}
          <div className="text-center p-4 rounded-lg" style={{ backgroundColor: colors.info[50] }}>
            <p
              className="text-xs font-semibold uppercase mb-2"
              style={{ color: colors.semantic.text.secondary }}
            >
              Promedio Cohorte
            </p>
            <p className="text-4xl font-bold" style={{ color: colors.info[600] }}>
              {cohortAverageGPA.toFixed(2)}
            </p>
          </div>
        </div>

        {/* Performance Indicator */}
        <div className="mt-6">
          <p
            className="text-xs font-semibold mb-2"
            style={{ color: colors.semantic.text.secondary }}
          >
            Posición relativa
          </p>
          <div className="flex h-2 gap-1 rounded-full overflow-hidden" style={{ backgroundColor: colors.semantic.background }}>
            {currentGPA >= cohortAverageGPA ? (
              <>
                <div
                  className="flex-1"
                  style={{ backgroundColor: colors.success[400] }}
                />
                <div className="flex-1" style={{ backgroundColor: colors.semantic.background }} />
              </>
            ) : (
              <>
                <div className="flex-1" style={{ backgroundColor: colors.warning[400] }} />
                <div
                  className="flex-1"
                  style={{ backgroundColor: colors.semantic.background }}
                />
              </>
            )}
          </div>
          <p
            className="text-xs mt-2"
            style={{ color: colors.semantic.text.muted }}
          >
            {currentGPA >= cohortAverageGPA
              ? `${(currentGPA - cohortAverageGPA).toFixed(2)} puntos arriba del promedio`
              : `${(cohortAverageGPA - currentGPA).toFixed(2)} puntos abajo del promedio`}
          </p>
        </div>
      </Card>

      {/* Trend Chart */}
      {trendData.length > 0 && (
        <Card>
          <div className="mb-6">
            <h3
              className="text-lg font-semibold mb-1"
              style={{ color: colors.semantic.text.primary }}
            >
              Tendencia de GPA
            </h3>
            <p
              className="text-sm"
              style={{ color: colors.semantic.text.secondary }}
            >
              Evolución del promedio a lo largo de los semestres
            </p>
          </div>

          <div className="h-80 -mx-4">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={trendData} margin={{ top: 5, right: 30, left: 0, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" stroke={colors.semantic.border} />
                <XAxis dataKey="semester" stroke={colors.semantic.text.muted} />
                <YAxis domain={[0, 4]} stroke={colors.semantic.text.muted} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: "white",
                    border: `1px solid ${colors.semantic.border}`,
                    borderRadius: 8,
                  }}
                />
                <Area
                  type="monotone"
                  dataKey="gpa"
                  fill={colors.primary[400]}
                  stroke={colors.primary[600]}
                  fillOpacity={0.2}
                  name="GPA"
                />
                {trendData[0]?.targetGPA && (
                  <Line
                    type="monotone"
                    dataKey="targetGPA"
                    stroke={colors.success[400]}
                    strokeDasharray="5 5"
                    name="Meta"
                  />
                )}
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </Card>
      )}

      {/* Course Distribution */}
      {courseDistribution.length > 0 && (
        <div className="grid gap-6 lg:grid-cols-2">
          <Card>
            <div className="mb-6">
              <h3
                className="text-lg font-semibold mb-1"
                style={{ color: colors.semantic.text.primary }}
              >
                Distribución de Calificaciones
              </h3>
              <p
                className="text-sm"
                style={{ color: colors.semantic.text.secondary }}
              >
                Número de cursos por rango de calificación
              </p>
            </div>

            <div className="h-64 -mx-4">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={courseDistribution} margin={{ top: 20, right: 30, left: 0, bottom: 50 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke={colors.semantic.border} />
                  <XAxis
                    dataKey="grade"
                    stroke={colors.semantic.text.muted}
                    angle={-45}
                    textAnchor="end"
                    height={80}
                  />
                  <YAxis allowDecimals={false} stroke={colors.semantic.text.muted} />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: "white",
                      border: `1px solid ${colors.semantic.border}`,
                      borderRadius: 8,
                    }}
                  />
                  <Bar dataKey="count" fill={colors.primary[400]} radius={[8, 8, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </Card>

          {/* Metrics Summary */}
          <Card>
            <h3
              className="text-lg font-semibold mb-4"
              style={{ color: colors.semantic.text.primary }}
            >
              Resumen Académico
            </h3>

            <div className="space-y-4">
              {/* Total Courses */}
              <div className="flex items-center justify-between p-3 rounded-lg" style={{ backgroundColor: colors.semantic.background }}>
                <div className="flex items-center gap-3">
                  <div
                    className="p-2 rounded-lg"
                    style={{ backgroundColor: colors.primary[100] }}
                  >
                    <BookOpen
                      className="h-5 w-5"
                      style={{ color: colors.primary[600] }}
                    />
                  </div>
                  <div>
                    <p
                      className="text-xs text-gray-500"
                      style={{ color: colors.semantic.text.secondary }}
                    >
                      Cursos Aprobados
                    </p>
                    <p
                      className="font-semibold"
                      style={{ color: colors.semantic.text.primary }}
                    >
                      {courses.filter((c) => parseFloat(c.grade) >= 3.0).length} de {courses.length}
                    </p>
                  </div>
                </div>
                <Badge variant="success">{passRate}%</Badge>
              </div>

              {/* Credit Hours */}
              <div className="flex items-center justify-between p-3 rounded-lg" style={{ backgroundColor: colors.semantic.background }}>
                <div className="flex items-center gap-3">
                  <div
                    className="p-2 rounded-lg"
                    style={{ backgroundColor: colors.info[100] }}
                  >
                    <Award
                      className="h-5 w-5"
                      style={{ color: colors.info[600] }}
                    />
                  </div>
                  <div>
                    <p
                      className="text-xs"
                      style={{ color: colors.semantic.text.secondary }}
                    >
                      Créditos Completados
                    </p>
                    <p
                      className="font-semibold"
                      style={{ color: colors.semantic.text.primary }}
                    >
                      {courses.reduce((sum, c) => sum + c.creditHours, 0)} horas
                    </p>
                  </div>
                </div>
              </div>

              {/* Course Engagement */}
              <div className="flex items-center justify-between p-3 rounded-lg" style={{ backgroundColor: colors.semantic.background }}>
                <div className="flex items-center gap-3">
                  <div
                    className="p-2 rounded-lg"
                    style={{ backgroundColor: colors.success[100] }}
                  >
                    <TrendingUp
                      className="h-5 w-5"
                      style={{ color: colors.success[600] }}
                    />
                  </div>
                  <div>
                    <p
                      className="text-xs"
                      style={{ color: colors.semantic.text.secondary }}
                    >
                      Tasa de Aprobación vs Cohorte
                    </p>
                    <p
                      className="font-semibold"
                      style={{ color: colors.semantic.text.primary }}
                    >
                      {passRate}% vs {cohortAverageCoursePass}%
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </Card>
        </div>
      )}

      {/* Recent Courses Table */}
      {courses.length > 0 && (
        <Card>
          <h3
            className="text-lg font-semibold mb-4"
            style={{ color: colors.semantic.text.primary }}
          >
            Cursos Recientes
          </h3>

          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr style={{ borderBottomColor: colors.semantic.border, borderBottomWidth: 1 }}>
                  <th
                    className="text-left py-3 px-4 font-semibold"
                    style={{ color: colors.semantic.text.secondary }}
                  >
                    Código
                  </th>
                  <th
                    className="text-left py-3 px-4 font-semibold"
                    style={{ color: colors.semantic.text.secondary }}
                  >
                    Curso
                  </th>
                  <th
                    className="text-left py-3 px-4 font-semibold"
                    style={{ color: colors.semantic.text.secondary }}
                  >
                    Calificación
                  </th>
                  <th
                    className="text-left py-3 px-4 font-semibold"
                    style={{ color: colors.semantic.text.secondary }}
                  >
                    Créditos
                  </th>
                  <th
                    className="text-left py-3 px-4 font-semibold"
                    style={{ color: colors.semantic.text.secondary }}
                  >
                    Semestre
                  </th>
                </tr>
              </thead>
              <tbody>
                {courses.slice(0, 10).map((course, idx) => (
                  <tr
                    key={idx}
                    style={{
                      borderBottomColor: colors.semantic.border,
                      borderBottomWidth: 1,
                      backgroundColor: idx % 2 === 0 ? "transparent" : colors.semantic.background,
                    }}
                  >
                    <td className="py-3 px-4" style={{ color: colors.semantic.text.primary }}>
                      <span className="font-semibold">{course.courseCode}</span>
                    </td>
                    <td className="py-3 px-4" style={{ color: colors.semantic.text.primary }}>
                      {course.courseName}
                    </td>
                    <td className="py-3 px-4">
                      <Badge
                        variant={
                          parseFloat(course.grade) >= 3.5
                            ? "success"
                            : parseFloat(course.grade) >= 3.0
                              ? "info"
                              : "danger"
                        }
                      >
                        {course.grade}
                      </Badge>
                    </td>
                    <td className="py-3 px-4" style={{ color: colors.semantic.text.secondary }}>
                      {course.creditHours}
                    </td>
                    <td className="py-3 px-4" style={{ color: colors.semantic.text.secondary }}>
                      {course.semester}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {courses.length > 10 && (
              <p
                className="text-xs py-3 text-center"
                style={{ color: colors.semantic.text.muted }}
              >
                +{courses.length - 10} cursos más
              </p>
            )}
          </div>
        </Card>
      )}
    </div>
  );
};
