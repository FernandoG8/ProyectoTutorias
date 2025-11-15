/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: "#2C3E50",
        secondary: "#34495E",
        accent: "#1ABC9C",
        background: "#F5F6F8",
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
