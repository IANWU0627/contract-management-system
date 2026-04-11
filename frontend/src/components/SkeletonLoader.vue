<template>
  <div class="skeleton-loader" :class="[`type-${type}`, { animated }]" :style="customStyle">
    <!-- 文本骨架 -->
    <template v-if="type === 'text'">
      <div 
        v-for="i in rows" 
        :key="i" 
        class="skeleton-row"
        :style="{ width: i === rows && lastRowWidth ? lastRowWidth : '100%' }"
      />
    </template>
    
    <!-- 圆形骨架（头像等） -->
    <div v-else-if="type === 'circle'" class="skeleton-circle" :style="{ width: size + 'px', height: size + 'px' }" />
    
    <!-- 矩形骨架（图片等） -->
    <div v-else-if="type === 'rect'" class="skeleton-rect" :style="{ width, height }" />
    
    <!-- 卡片骨架 -->
    <div v-else-if="type === 'card'" class="skeleton-card">
      <div class="skeleton-card-header">
        <div class="skeleton-circle" :style="{ width: '40px', height: '40px' }" />
        <div class="skeleton-card-info">
          <div class="skeleton-row" style="width: 60%" />
          <div class="skeleton-row" style="width: 40%" />
        </div>
      </div>
      <div class="skeleton-card-body">
        <div v-for="i in 3" :key="i" class="skeleton-row" :style="{ width: 90 - i * 10 + '%' }" />
      </div>
    </div>
    
    <!-- 表格骨架 -->
    <div v-else-if="type === 'table'" class="skeleton-table">
      <div class="skeleton-table-header">
        <div v-for="(col, idx) in columns" :key="idx" class="skeleton-cell" :style="{ flex: col.flex || 1 }">
          <div class="skeleton-row" style="width: 70%" />
        </div>
      </div>
      <div v-for="row in rowCount" :key="row" class="skeleton-table-row">
        <div v-for="col in columns" :key="col.key" class="skeleton-cell" :style="{ flex: col.flex || 1 }">
          <div class="skeleton-row" :style="{ width: Math.random() * 40 + 40 + '%' }" />
        </div>
      </div>
    </div>
    
    <!-- 列表骨架 -->
    <div v-else-if="type === 'list'" class="skeleton-list">
      <div v-for="i in rowCount" :key="i" class="skeleton-list-item">
        <div class="skeleton-circle" :style="{ width: '48px', height: '48px' }" />
        <div class="skeleton-list-content">
          <div class="skeleton-row" style="width: 60%" />
          <div class="skeleton-row" style="width: 80%" />
        </div>
      </div>
    </div>
    
    <!-- 统计卡片骨架 -->
    <div v-else-if="type === 'stat'" class="skeleton-stat">
      <div class="skeleton-circle" :style="{ width: '48px', height: '48px' }" />
      <div class="skeleton-stat-content">
        <div class="skeleton-row" style="width: 80%; height: 28px; margin-bottom: 8px;" />
        <div class="skeleton-row" style="width: 50%" />
      </div>
    </div>
    
    <!-- 表单骨架 -->
    <div v-else-if="type === 'form'" class="skeleton-form">
      <div v-for="i in rowCount" :key="i" class="skeleton-form-item">
        <div class="skeleton-row" style="width: 80px; margin-bottom: 8px;" />
        <div class="skeleton-row" style="width: 100%; height: 36px;" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Column {
  key: string
  flex?: number
}

interface Props {
  // 骨架类型
  type?: 'text' | 'circle' | 'rect' | 'card' | 'table' | 'list' | 'stat' | 'form'
  // 是否显示动画
  animated?: boolean
  // 文本行数
  rows?: number
  // 最后一行宽度
  lastRowWidth?: string
  // 圆形/矩形尺寸
  size?: number
  // 矩形宽度
  width?: string
  // 矩形高度
  height?: string
  // 表格/列表行数
  rowCount?: number
  // 表格列配置
  columns?: Column[]
}

const props = withDefaults(defineProps<Props>(), {
  type: 'text',
  animated: true,
  rows: 3,
  lastRowWidth: '60%',
  size: 40,
  width: '100%',
  height: '100px',
  rowCount: 5,
  columns: () => [
    { key: 'col1', flex: 2 },
    { key: 'col2', flex: 1 },
    { key: 'col3', flex: 1 },
    { key: 'col4', flex: 1 }
  ]
})

const customStyle = computed(() => {
  const style: Record<string, string> = {}
  if (props.width && props.type !== 'rect') {
    style.width = props.width
  }
  return style
})
</script>

<style scoped lang="scss">
.skeleton-loader {
  width: 100%;
}

/* 基础骨架元素 */
.skeleton-row {
  height: 16px;
  background: var(--skeleton-bg, #e5e7eb);
  border-radius: 4px;
  margin-bottom: 12px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.skeleton-circle {
  background: var(--skeleton-bg, #e5e7eb);
  border-radius: 50%;
  flex-shrink: 0;
}

.skeleton-rect {
  background: var(--skeleton-bg, #e5e7eb);
  border-radius: 8px;
}

/* 卡片骨架 */
.skeleton-card {
  background: var(--bg-card, #fff);
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 12px;
  padding: 16px;
  
  .skeleton-card-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
    
    .skeleton-card-info {
      flex: 1;
      
      .skeleton-row {
        margin-bottom: 8px;
        
        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }
  
  .skeleton-card-body {
    .skeleton-row {
      margin-bottom: 10px;
      
      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

/* 表格骨架 */
.skeleton-table {
  background: var(--bg-card, #fff);
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 8px;
  overflow: hidden;
  
  .skeleton-table-header {
    display: flex;
    padding: 12px 16px;
    background: var(--bg-hover, #f9fafb);
    border-bottom: 1px solid var(--border-color, #e5e7eb);
    
    .skeleton-cell {
      padding: 0 8px;
      
      .skeleton-row {
        margin-bottom: 0;
        height: 14px;
      }
    }
  }
  
  .skeleton-table-row {
    display: flex;
    padding: 12px 16px;
    border-bottom: 1px solid var(--border-color, #e5e7eb);
    
    &:last-child {
      border-bottom: none;
    }
    
    .skeleton-cell {
      padding: 0 8px;
      
      .skeleton-row {
        margin-bottom: 0;
        height: 14px;
      }
    }
  }
}

/* 列表骨架 */
.skeleton-list {
  .skeleton-list-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 0;
    border-bottom: 1px solid var(--border-color, #e5e7eb);
    
    &:last-child {
      border-bottom: none;
    }
    
    .skeleton-list-content {
      flex: 1;
      
      .skeleton-row {
        margin-bottom: 8px;
        
        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }
}

/* 统计卡片骨架 */
.skeleton-stat {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: var(--bg-card, #fff);
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 12px;
  
  .skeleton-stat-content {
    flex: 1;
    
    .skeleton-row {
      margin-bottom: 0;
    }
  }
}

/* 表单骨架 */
.skeleton-form {
  .skeleton-form-item {
    margin-bottom: 20px;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .skeleton-row {
      margin-bottom: 0;
    }
  }
}

/* 动画效果 */
.animated {
  .skeleton-row,
  .skeleton-circle,
  .skeleton-rect {
    background: linear-gradient(
      90deg,
      var(--skeleton-bg, #e5e7eb) 25%,
      var(--skeleton-highlight, #f3f4f6) 50%,
      var(--skeleton-bg, #e5e7eb) 75%
    );
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s ease-in-out infinite;
  }
}

@keyframes skeleton-loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .skeleton-loader {
    --skeleton-bg: #374151;
    --skeleton-highlight: #4b5563;
  }
}
</style>
