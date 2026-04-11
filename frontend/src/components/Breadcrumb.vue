<template>
  <div class="breadcrumb">
    <el-icon class="home-icon" @click="$router.push('/dashboard')"><HomeFilled /></el-icon>
    <el-icon class="separator"><ArrowRight /></el-icon>
    <span class="current">{{ currentTitle }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { HomeFilled, ArrowRight } from '@element-plus/icons-vue'

const route = useRoute()
const { t } = useI18n()

const currentTitle = computed(() => {
  const title = route.meta.title as string
  return title ? t(title) : '未命名页面'
})
</script>

<style scoped lang="scss">
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-secondary);
  
  .home-icon {
    font-size: 16px;
    cursor: pointer;
    color: var(--text-placeholder);
    transition: color 0.2s;
    
    &:hover {
      color: var(--el-color-primary);
    }
  }
  
  .separator {
    font-size: 12px;
    color: var(--text-muted);
  }
  
  .current {
    color: var(--text-primary);
    font-weight: 500;
  }
}
</style>
