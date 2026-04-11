<template>
  <div class="empty-state" :class="[`size-${size}`, `type-${type}`]">
    <div class="empty-illustration">
      <slot name="image">
        <el-empty :image="imageUrl" :image-size="imageSize" :description="description">
          <template #default>
            <div class="empty-content">
              <h3 v-if="title" class="empty-title">{{ title }}</h3>
              <p v-if="subtitle" class="empty-subtitle">{{ subtitle }}</p>
              <div v-if="$slots.action || actionText" class="empty-action">
                <slot name="action">
                  <el-button v-if="actionText" :type="actionType" @click="handleAction">
                    <el-icon v-if="actionIcon"><component :is="actionIcon" /></el-icon>
                    {{ actionText }}
                  </el-button>
                </slot>
              </div>
            </div>
          </template>
        </el-empty>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  // 空状态类型
  type?: 'default' | 'search' | 'data' | 'error' | 'network'
  // 尺寸
  size?: 'small' | 'medium' | 'large'
  // 标题
  title?: string
  // 副标题/描述
  subtitle?: string
  // Element Plus 的 description 属性
  description?: string
  // 自定义图片 URL
  image?: string
  // 图片尺寸
  imageSize?: number
  // 操作按钮文字
  actionText?: string
  // 操作按钮类型
  actionType?: 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'text'
  // 操作按钮图标
  actionIcon?: string
}

const props = withDefaults(defineProps<Props>(), {
  type: 'default',
  size: 'medium',
  title: '',
  subtitle: '',
  description: '',
  image: '',
  imageSize: 160,
  actionText: '',
  actionType: 'primary',
  actionIcon: ''
})

const emit = defineEmits<{
  action: []
}>()

// 内置图片映射
const builtInImages: Record<string, string> = {
  default: '', // 使用 Element Plus 默认
  search: '/images/empty-search.svg',
  data: '/images/empty-data.svg',
  error: '/images/empty-error.svg',
  network: '/images/empty-network.svg'
}

const imageUrl = computed(() => {
  if (props.image) return props.image
  return builtInImages[props.type] || ''
})

const handleAction = () => {
  emit('action')
}
</script>

<style scoped lang="scss">
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
  
  /* 尺寸变体 */
  &.size-small {
    padding: 24px 16px;
    
    :deep(.el-empty__image) {
      width: 80px;
      height: 80px;
    }
    
    .empty-title {
      font-size: 14px;
    }
    
    .empty-subtitle {
      font-size: 12px;
    }
  }
  
  &.size-medium {
    padding: 40px 20px;
    
    :deep(.el-empty__image) {
      width: 120px;
      height: 120px;
    }
    
    .empty-title {
      font-size: 16px;
    }
    
    .empty-subtitle {
      font-size: 14px;
    }
  }
  
  &.size-large {
    padding: 60px 40px;
    
    :deep(.el-empty__image) {
      width: 200px;
      height: 200px;
    }
    
    .empty-title {
      font-size: 20px;
    }
    
    .empty-subtitle {
      font-size: 16px;
    }
  }
  
  /* 类型变体 */
  &.type-search {
    :deep(.el-empty__description) {
      color: var(--text-secondary);
    }
  }
  
  &.type-error {
    :deep(.el-empty__image) {
      filter: hue-rotate(320deg);
    }
  }
  
  &.type-network {
    :deep(.el-empty__image) {
      filter: hue-rotate(180deg);
    }
  }
}

.empty-illustration {
  width: 100%;
  
  :deep(.el-empty) {
    padding: 0;
    
    .el-empty__image {
      margin-bottom: 16px;
      
      svg {
        width: 100%;
        height: 100%;
      }
    }
    
    .el-empty__description {
      margin-top: 0;
      
      p {
        margin: 0;
        font-size: 14px;
        color: var(--text-secondary);
        line-height: 1.6;
      }
    }
  }
}

.empty-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.empty-title {
  margin: 0;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
}

.empty-subtitle {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.5;
  max-width: 400px;
}

.empty-action {
  margin-top: 8px;
  
  .el-button {
    .el-icon {
      margin-right: 4px;
    }
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .empty-state {
    :deep(.el-empty__image) {
      opacity: 0.8;
    }
  }
}
</style>
