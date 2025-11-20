/**
 * TUTOLINK Color System
 * Refined palette maintaining base colors, improved for consistency and accessibility
 */

export const colors = {
  // Primary Brand Color - CASAL Palette
  primary: {
    50: '#F0F5F7',
    100: '#D4E3E8',
    200: '#A8C7D0',
    300: '#7CABBA',
    400: '#315762', // Main - CASAL
    500: '#2A4B52',
    600: '#1F3840',
    700: '#152530',
    800: '#0C1318',
    900: '#060A0D',
  },

  // Success / Positive
  success: {
    50: '#DCFCE7',
    100: '#BBF7D0',
    200: '#86EFAC',
    300: '#4ADE80',
    400: '#22C55E', // Main
    500: '#16A34A',
    600: '#15803D',
    700: '#166534',
    800: '#166534',
    900: '#15803D',
  },

  // Warning / Attention
  warning: {
    50: '#FFFBEB',
    100: '#FEF3C7',
    200: '#FDE68A',
    300: '#FCD34D',
    400: '#FBBF24', // Main
    500: '#F59E0B',
    600: '#D97706',
    700: '#B45309',
    800: '#92400E',
    900: '#78350F',
  },

  // Danger / Error
  danger: {
    50: '#FEE2E2',
    100: '#FECACA',
    200: '#FCA5A5',
    300: '#F87171',
    400: '#EF4444', // Main
    500: '#DC2626',
    600: '#B91C1C',
    700: '#991B1B',
    800: '#7F1D1D',
    900: '#7F1D1D',
  },

  // Info / Cyan
  info: {
    50: '#CFFAFE',
    100: '#A5F3FC',
    200: '#67E8F9',
    300: '#22D3EE',
    400: '#06B6D4', // Main
    500: '#0891B2',
    600: '#0E7490',
    700: '#164E63',
    800: '#164E63',
    900: '#164E63',
  },

  // Neutral / Grayscale
  neutral: {
    50: '#F8FAFC',   // Background
    100: '#F1F5F9',
    200: '#E2E8F0',
    300: '#CBD5E1',
    400: '#94A3B8',  // Light text
    500: '#64748B',  // Medium text
    600: '#475569',
    700: '#334155',
    800: '#1E293B',  // Dark text
    900: '#0F172A',  // Darkest
  },

  // Semantic Colors
  semantic: {
    background: '#EFEFEF', // Gallery
    surface: '#FFFFFF',
    border: '#E2E8F0',
    text: {
      primary: '#0F172A',
      secondary: '#475569',
      muted: '#94A3B8',
      light: '#CBD5E1',
    },
    success: '#10B981',
    warning: '#F59E0B',
    danger: '#EF4444',
    info: '#0EA5E9',
  },

  // Gradient combinations
  gradients: {
    primary: 'from-casal to-casal/80',
    success: 'from-green-400 to-emerald-600',
    warning: 'from-amber-400 to-orange-600',
    danger: 'from-red-400 to-rose-600',
    info: 'from-cyan-400 to-blue-600',
  },
} as const;

// Tailwind class mappings for common patterns
export const colorVariants = {
  // Status badges
  statusBadge: {
    success: 'bg-green-100 text-green-800 border border-green-300',
    warning: 'bg-amber-100 text-amber-800 border border-amber-300',
    danger: 'bg-red-100 text-red-800 border border-red-300',
    info: 'bg-cyan-100 text-cyan-800 border border-cyan-300',
    pending: 'bg-gray-100 text-gray-800 border border-gray-300',
  },

  // Buttons
  button: {
    primary: 'bg-casal hover:bg-casal/90 text-white',
    secondary: 'bg-gray-200 hover:bg-gray-300 text-gray-900',
    success: 'bg-green-500 hover:bg-green-600 text-white',
    danger: 'bg-red-500 hover:bg-red-600 text-white',
    outline: 'border border-casal text-casal hover:bg-casal/5',
  },

  // Alerts
  alert: {
    success: 'bg-green-50 border-l-4 border-green-500 text-green-900',
    warning: 'bg-amber-50 border-l-4 border-amber-500 text-amber-900',
    danger: 'bg-red-50 border-l-4 border-red-500 text-red-900',
    info: 'bg-cyan-50 border-l-4 border-cyan-500 text-cyan-900',
  },

  // Form inputs with error
  input: {
    default: 'border-gray-300 focus:border-casal focus:ring-casal',
    error: 'border-red-500 focus:border-red-500 focus:ring-red-500 bg-red-50',
    success: 'border-green-500 focus:border-green-500 focus:ring-green-500',
  },
} as const;

// Export type for TypeScript
export type ColorVariant = keyof typeof colors;
export type ColorLevel = keyof typeof colors.primary;
