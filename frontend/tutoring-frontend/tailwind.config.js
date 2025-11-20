/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Mantienes estos si ya los usas en otros lados
        casal: "#2f2b46",
        gallery: "#EFEFEF",

        // Reajustados a la nueva identidad
        primary: "#1C3A4B",      // Azul petróleo (navbar, sidebar, headers)
        secondary: "#3AAFA9",    // Verde institucional (resaltos, badges, acciones)
        accent: "#22C55E",       // Verde éxito (estados, métricas OK)
        background: "#F4F7F9",   // Fondo general del dashboard
        surface: "#FFFFFF",      // Cards y paneles
        border: "#E2E8F0",       // Bordes suaves
        text: "#1F2937",         // Texto principal
        textMuted: "#6B7280",    // Texto secundario
      },
      borderRadius: {
        xl: "1rem",
      },
    },
  },
  plugins: [],
};
