import { useMemo } from "react";
import type { Carrera, DistribucionTutor } from "@/types/dashboard";
import { CARRERA_COLORS, CARRERA_DISPLAY_ORDER } from "@/utils/dashboard-utils";
import { colors } from "@/constants/colors";
import {
  BarChart,
  Bar,
  CartesianGrid,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  Cell,
} from "recharts";

interface TutorAlumnosChartProps {
  data: DistribucionTutor[];
  isLoading?: boolean;
  height?: number;
}

/**
 * Gráfica de barras verticales alumnos vs tutor.
 * Eje X: tutores; Eje Y: alumnos asignados. Mantiene la paleta por carrera.
 */
export const TutorAlumnosChart = ({ data, isLoading, height }: TutorAlumnosChartProps) => {
  const orderIndex = useMemo(
    () => new Map<Carrera, number>(CARRERA_DISPLAY_ORDER.map((carrera, idx) => [carrera, idx])),
    [],
  );

  const sortedData = useMemo(() => {
    return [...data]
      .sort((a, b) => {
        const carreraDiff =
          (orderIndex.get(a.tutor_carrera) ?? CARRERA_DISPLAY_ORDER.length) -
          (orderIndex.get(b.tutor_carrera) ?? CARRERA_DISPLAY_ORDER.length);
        if (carreraDiff !== 0) return carreraDiff;
        return a.tutor_nombre.localeCompare(b.tutor_nombre);
      })
      .map((tutor) => ({
        ...tutor,
        color: CARRERA_COLORS[tutor.tutor_carrera]?.hex ?? colors.primary[400],
        carreraNombre: CARRERA_COLORS[tutor.tutor_carrera]?.nombre ?? tutor.tutor_carrera,
      }));
  }, [data, orderIndex]);

  const legendCarreras = useMemo(
    () =>
      CARRERA_DISPLAY_ORDER.filter((carrera) =>
        sortedData.some((tutor) => tutor.tutor_carrera === carrera),
      ),
    [sortedData],
  );

  const maxAlumnos = useMemo(
    () => Math.max(...sortedData.map((tutor) => tutor.alumnos_asignados), 1),
    [sortedData],
  );

  const chartMinWidth = Math.max(sortedData.length * 40, 820);
  const chartHeight = height ?? 380;

  if (isLoading) {
    return (
      <div className="space-y-4">
        <div className="h-4 w-40 bg-gray-200 rounded animate-pulse" />
        <div className="flex items-end gap-3">
          {[1, 2, 3, 4, 5, 6, 7].map((i) => (
            <div
              key={i}
              className="w-10 bg-gray-100 rounded-t animate-pulse"
              style={{ height: `${60 + i * 10}px` }}
            />
          ))}
        </div>
      </div>
    );
  }

  if (!data.length) {
    return (
      <div className="text-center py-8">
        <p style={{ color: colors.semantic.text.muted }}>
          No hay datos de alumnos por tutor disponibles
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap gap-4">
        {legendCarreras.map((carrera) => (
          <div key={carrera} className="flex items-center gap-2 text-xs">
            <span
              className="w-3 h-3 rounded-sm border"
              style={{
                backgroundColor: CARRERA_COLORS[carrera].hex,
                borderColor: colors.semantic.border,
              }}
            />
            <span style={{ color: colors.semantic.text.secondary }}>
              {carrera}
            </span>
          </div>
        ))}
      </div>

      <div className="w-full overflow-x-auto pb-2">
        <div style={{ minWidth: chartMinWidth, height: chartHeight }}>
          <ResponsiveContainer width="100%" height="100%">
            <BarChart
              data={sortedData}
              margin={{ top: 10, right: 20, left: 0, bottom: 70 }}
              barCategoryGap={6}
            >
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke={colors.neutral[200]} />
              <XAxis
                dataKey="tutor_nombre"
                interval={0}
                angle={-55}
                textAnchor="end"
                height={80}
                tickLine={false}
                tick={{ fill: colors.semantic.text.secondary, fontSize: 11 }}
              />
              <YAxis
                allowDecimals={false}
                tickLine={false}
                axisLine={{ stroke: colors.semantic.border }}
                tick={{ fill: colors.semantic.text.secondary, fontSize: 11 }}
                label={{
                  value: "Alumnos asignados",
                  angle: -90,
                  position: "insideLeft",
                  fill: colors.semantic.text.secondary,
                  offset: 12,
                }}
                domain={[0, Math.max(maxAlumnos, 1)]}
              />
              <Tooltip
                cursor={{ fill: colors.neutral[100] }}
              contentStyle={{
                  borderRadius: 8,
                  borderColor: colors.semantic.border,
                  backgroundColor: colors.semantic.surface,
                }}
                formatter={(value?: number) => [`${value ?? 0} alumnos`, "Alumnos"]}
                labelFormatter={(label, payload) => {
                  const carrera = payload?.[0]?.payload?.carreraNombre;
                  return carrera ? `${label} • ${carrera}` : label;
                }}
              />
              <Bar
                dataKey="alumnos_asignados"
                radius={[8, 8, 0, 0]}
                barSize={20}
              >
                {sortedData.map((entry) => (
                  <Cell key={entry.tutor_id} fill={entry.color} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="text-xs text-right" style={{ color: colors.semantic.text.muted }}>
        Eje Y: alumnos asignados • Eje X: tutores agrupados por carrera
      </div>
    </div>
  );
};
