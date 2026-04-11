<template>
  <div v-if="hasError" class="error-boundary">
    <div class="error-content">
      <el-icon :size="80" color="#ef4444">
        <WarningFilled />
      </el-icon>
      <h2>{{ t('error.title') }}</h2>
      <p>{{ errorMessage }}</p>
      <div class="error-actions">
        <el-button type="primary" @click="handleReset">
          {{ t('error.retry') }}
        </el-button>
        <el-button @click="handleGoHome">
          {{ t('error.goHome') }}
        </el-button>
      </div>
    </div>
  </div>
  <slot v-else />
</template>

<script setup lang="ts">
import { ref, onErrorCaptured } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { WarningFilled } from '@element-plus/icons-vue'

const { t } = useI18n()
const router = useRouter()
const hasError = ref(false)
const errorMessage = ref('')

onErrorCaptured((err) => {
  console.error('Error caught by ErrorBoundary:', err)
  hasError.value = true
  errorMessage.value = err instanceof Error ? err.message : String(err)
  return false
})

const handleReset = () => {
  hasError.value = false
  errorMessage.value = ''
  window.location.reload()
}

const handleGoHome = () => {
  hasError.value = false
  errorMessage.value = ''
  router.push('/')
}
</script>

<style scoped lang="scss">
.error-boundary {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
  padding: 2rem;
  
  .error-content {
    text-align: center;
    max-width: 500px;
    
    h2 {
      font-size: 1.5rem;
      font-weight: 600;
      margin: 1rem 0 0.5rem;
      color: var(--text-primary);
    }
    
    p {
      color: var(--text-secondary);
      margin-bottom: 1.5rem;
      line-height: 1.6;
    }
    
    .error-actions {
      display: flex;
      gap: 0.75rem;
      justify-content: center;
    }
  }
}
</style>
