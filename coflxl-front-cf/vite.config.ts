import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import tailwindcss from '@tailwindcss/vite';
import path from 'path';

export default defineConfig({
  plugins: [vue(), tailwindcss()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  build: {
    outDir: 'dist', // 默认输出目录
  },
  server: {
    proxy: {
      '/api/open': {
        target: 'http://localhost:8080', // 假设后端接口地址
        changeOrigin: true,
      },
    },
  },
});
