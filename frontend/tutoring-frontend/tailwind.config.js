/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: "#315762",
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
