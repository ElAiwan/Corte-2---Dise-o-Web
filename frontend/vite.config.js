import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Puerto 5173: es el origen que permite el CORS del backend (C6 del contrato).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: true,
  },
});
