import { defineConfig, type ProxyOptions } from 'vite';
import react from '@vitejs/plugin-react';

// Keep the browser on the same origin in both local development and preview.
// Deployments need the equivalent reverse proxy (see deploy/nginx.conf).
const apiProxy: Record<string, ProxyOptions> = {
  '/api': {
    target: 'https://www.wanandroid.com',
    changeOrigin: true,
    secure: true,
    rewrite: (path) => path.replace(/^\/api(?=\/|$)/, ''),
    cookieDomainRewrite: '',
    cookiePathRewrite: '/api/',
  },
};

export default defineConfig({
  plugins: [react()],
  server: { port: 8080, strictPort: true, proxy: apiProxy },
  preview: { port: 8081, strictPort: true, proxy: apiProxy },
  build: { outDir: 'dist', emptyOutDir: true },
});
