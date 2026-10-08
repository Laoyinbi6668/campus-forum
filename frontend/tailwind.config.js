/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,ts,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#172033',
        mist: '#f6f4ee',
        ember: '#d65a31',
        moss: '#3f6c51',
        gold: '#e4b363',
        slate: '#5f6c7b',
      },
      boxShadow: {
        soft: '0 18px 45px rgba(23, 32, 51, 0.12)',
      },
      fontFamily: {
        display: ['"Segoe UI Variable Display"', '"PingFang SC"', '"Microsoft YaHei"', 'sans-serif'],
        body: ['"Segoe UI Variable Text"', '"PingFang SC"', '"Microsoft YaHei"', 'sans-serif'],
      },
    },
  },
  plugins: [],
};
