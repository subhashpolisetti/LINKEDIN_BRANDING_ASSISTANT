/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#6B46C1',
          dark: '#553C9A',
          light: '#9F7AEA'
        },
        secondary: '#4A5568',
        background: '#F7FAFC',
        error: '#E53E3E',
        success: '#48BB78'
      },
      fontFamily: {
        sans: [
          '-apple-system',
          'system-ui',
          'BlinkMacSystemFont',
          'Segoe UI',
          'Roboto',
          'Helvetica Neue',
          'Fira Sans',
          'Ubuntu',
          'Oxygen',
          'sans-serif'
        ]
      }
    },
  },
  plugins: [],
}
