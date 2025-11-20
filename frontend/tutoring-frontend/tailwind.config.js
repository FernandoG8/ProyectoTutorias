/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        casal: "#2f2b46",
        gallery: "#EFEFEF",
        primary: "#2C3E50",
        secondary: "#34495E",
        accent: "#1ABC9C",
        background: "#EFEFEF",
        surface: "#FFFFFF",
        border: "#E2E8F0",
        text: "#0F172A",
      },
      borderRadius: {
        xl: "1rem",
      },
    },
  },
  plugins: [],
};
