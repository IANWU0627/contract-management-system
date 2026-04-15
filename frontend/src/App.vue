<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const appStore = useAppStore()

onMounted(() => {
  const isLoginPage = route.path === '/login' || route.path === '/'
  if (!isLoginPage) {
    appStore.initTheme()
  } else {
    document.body.classList.remove('dark')
    document.documentElement.classList.remove('dark')
    document.body.removeAttribute('data-theme')
  }
})
</script>

<style>
@import '@/assets/styles/variables.scss';

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

body {
  background-color: var(--bg-color);
  color: var(--text-primary);
  transition: background-color 0.3s ease, color 0.3s ease;
}

/* 暗色主题 - 仅在非登录页生效 */
body.dark,
html.dark {
  --el-bg-color: #1e293b;
  --el-bg-color-overlay: #334155;
  --el-text-color-primary: #f8fafc;
  --el-text-color-regular: #e2e8f0;
  --el-border-color: #475569;
  --el-border-color-light: #334155;
}

/* Element Plus 暗色主题适配 */
html.dark,
html[data-theme="dark"] {
  --el-bg-color: #1e293b;
  --el-bg-color-page: #0f172a;
  --el-text-color-primary: #f8fafc;
  --el-text-color-regular: #e2e8f0;
  --el-border-color: #475569;
  --el-border-color-light: #334155;
}

/* 全局表格优化 - 解决英文过长导致的变形问题 */
.el-table {
  .el-table__header-wrapper {
    th .cell {
      word-break: break-word;
      white-space: normal;
      line-height: 1.4;
      font-weight: 600;
    }
  }
  
  .el-table__body-wrapper {
    td .cell {
      word-break: break-word;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}

/* 通用表格标题链接样式 */
.table-title-link {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
