import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

const getPackageName = (id: string) => {
  const nodeModulesIndex = id.lastIndexOf('node_modules/')
  if (nodeModulesIndex < 0) return null
  const packagePath = id.slice(nodeModulesIndex + 'node_modules/'.length)
  const parts = packagePath.split('/')
  if (parts[0]?.startsWith('@') && parts.length > 1) {
    return `${parts[0]}/${parts[1]}`
  }
  return parts[0] || null
}

const manualChunks = (id: string) => {
  if (!id.includes('node_modules')) return

  if (id.includes('/node_modules/element-plus/es/components/')) {
    const seg = id.split('/node_modules/element-plus/es/components/')[1]
    const component = seg?.split('/')[0]
    if (component) return `el-${component}`
  }

  if (id.includes('/node_modules/@element-plus/icons-vue/')) {
    return 'el-icons'
  }

  if (id.includes('/node_modules/vue/') || id.includes('/node_modules/vue-router/') || id.includes('/node_modules/pinia/') || id.includes('/node_modules/vue-i18n/')) {
    return 'vue-vendor'
  }

  const pkg = getPackageName(id)
  if (!pkg) return

  if (pkg.startsWith('@antv/')) return `antv-${pkg.split('/')[1]}`
  if (pkg === 'xlsx') return 'xlsx'
  if (pkg === 'jspdf') return 'jspdf'
  if (pkg === 'html2canvas') return 'html2canvas'
  if (pkg === '@vueup/vue-quill' || pkg === 'quill' || pkg === 'parchment' || pkg === 'quill-delta') return 'quill'
  if (pkg === 'axios') return 'axios'
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
