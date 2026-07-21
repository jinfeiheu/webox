import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      // dev: forward API calls to Spring Boot on :8080 (primary path; CORS is the fallback)
      '/api': 'http://localhost:8080',
      // dish images are served by the backend's static resources
      '/images': 'http://localhost:8080',
    },
  },
})
