import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

const manualChunks = (id: string) => {
  if (id.includes('node_modules')) {
    if (id.includes('element-plus') || id.includes('@element-plus/icons-vue')) {
      return 'element-plus'
    }
    if (id.includes('@antv/g2plot')) {
      return 'chart-lib'
    }
    if (id.includes('axios') || id.includes('xlsx') || id.includes('jspdf') || id.includes('html2canvas') || id.includes('@vueup/vue-quill')) {
      return 'utils-lib'
    }
    if (id.includes('vue') || id.includes('vue-router') || id.includes('pinia') || id.includes('vue-i18n')) {
      return 'vue-vendor'
    }
  }
}

export default defineConfig({
  plugins: [
    vue()
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8081',
        changeOrigin: true
      },
      '/ws': {
        target: 'ws://127.0.0.1:8081',
        ws: true,
        changeOrigin: true
      }
    },
    open: true,
    cors: true
  },
  build: {
    target: 'es2020',
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    minify: 'esbuild',
    rollupOptions: {
      output: {
        manualChunks,
        chunkFileNames: 'assets/js/[name]-[hash].js',
        entryFileNames: 'assets/js/[name]-[hash].js',
        assetFileNames: 'assets/[ext]/[name]-[hash].[ext]'
      }
    }
  }
})
