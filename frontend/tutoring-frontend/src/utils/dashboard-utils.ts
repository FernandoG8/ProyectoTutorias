import type { Carrera, SaturationState } from "@/types/dashboard";

// Carrera color mappings (Tailwind classes and hex values)
export const CARRERA_COLORS: Record<Carrera, {
  hex: string;
  bg: string;
  border: string;
  text: string;
  nombre: string;
}> = {
  ISC: {
    hex: "#10B981",
    bg: "bg-emerald-50",
    border: "border-emerald-300",
    text: "text-emerald-900",
    nombre: "Ing. Sistemas Computacionales",
  },
  IME: {
    hex: "#F59E0B",
    bg: "bg-amber-50",
    border: "border-amber-300",
    text: "text-amber-900",
    nombre: "Ing. Mecánica",
  },
  ITS: {
    hex: "#2563EB",
    bg: "bg-blue-50",
    border: "border-blue-300",
    text: "text-blue-900",
    nombre: "Ing. Tecnologías Software",
  },
  IE: {
    hex: "#8B5CF6",
    bg: "bg-purple-50",
    border: "border-purple-300",
    text: "text-purple-900",
    nombre: "Ing. Electrónica",
  },
  ICA: {
    hex: "#F472B6",
    bg: "bg-pink-50",
    border: "border-pink-300",
    text: "text-pink-900",
    nombre: "Ing. Automatización",
  },
  IMECA: {
    hex: "#EF4444",
    bg: "bg-red-50",
    border: "border-red-300",
    text: "text-red-900",
    nombre: "Ing. Mecatrónica",
  },
};

// Saturation state colors and emojis
export const SATURATION_COLORS: Record<SaturationState, {
  hex: string;
  bg: string;
  text: string;
  border: string;
  badge: string;
  emoji: string;
  label: string;
}> = {
  crítico: {
    hex: "#EF4444",
    bg: "bg-red-50",
    text: "text-red-900",
    border: "border-red-200",
    badge: "bg-red-500",
    emoji: "🔴",
    label: "Crítico",
  },
  alerta: {
    hex: "#F59E0B",
    bg: "bg-amber-50",
    text: "text-amber-900",
    border: "border-amber-200",
    badge: "bg-amber-500",
    emoji: "🟠",
    label: "Alerta",
  },
  normal: {
    hex: "#22C55E",
    bg: "bg-green-50",
    text: "text-green-900",
    border: "border-green-200",
    badge: "bg-green-500",
    emoji: "🟢",
    label: "Normal",
  },
  bajo: {
    hex: "#3B82F6",
    bg: "bg-blue-50",
    text: "text-blue-900",
    border: "border-blue-200",
    badge: "bg-blue-500",
    emoji: "🔵",
    label: "Bajo",
  },
};

/**
 * Determines saturation state based on percentage
 */
export function getSaturationState(percentage: number): SaturationState {
  if (percentage >= 95) return "crítico";
  if (percentage >= 80) return "alerta";
  if (percentage >= 50) return "normal";
  return "bajo";
}

/**
 * Calculates saturation percentage
 */
export function calculateSaturation(actual: number, maximum: number): number {
  if (maximum === 0) return 0;
  return Math.round((actual / maximum) * 100);
}

/**
 * Get carrera name from code
 */
export function getCarreraName(carrera: Carrera): string {
  return CARRERA_COLORS[carrera]?.nombre || carrera;
}

/**
 * Format saturation percentage for display
 */
export function formatSaturationPercentage(percentage: number): string {
  return `${Math.round(percentage)}%`;
}

/**
 * Calculate trend value for comparison
 */
export function calculateTrend(current: number, previous: number): {
  direction: "up" | "down" | "stable";
  value: number;
  percentage: number;
} {
  if (previous === 0) {
    return { direction: "stable", value: 0, percentage: 0 };
  }

  const difference = current - previous;
  const percentage = Math.round((Math.abs(difference) / previous) * 100);

  return {
    direction: difference > 0 ? "up" : difference < 0 ? "down" : "stable",
    value: Math.abs(difference),
    percentage,
  };
}
