<template>
  <nav class="mobile-nav" :class="{ 'is-hidden': isHidden }">
    <router-link
      v-for="item in navItems"
      :key="item.path"
      :to="item.path"
      class="nav-item"
      :class="{ active: isActive(item.path) }"
    >
      <div class="nav-icon">
        <el-icon :size="22">
          <component :is="item.icon" />
        </el-icon>
        <span v-if="item.badge" class="nav-badge">{{ item.badge }}</span>
      </div>
      <span class="nav-label">{{ $t(item.labelKey) }}</span>
    </router-link>
  </nav>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { Odometer, DocumentCopy, DataAnalysis, User } from '@element-plus/icons-vue'

const route = useRoute()

// 导航项配置
const navItems = [
  { path: '/dashboard', labelKey: 'menu.dashboard', icon: 'Odometer', badge: 0 },
  { path: '/contracts', labelKey: 'menu.contracts', icon: 'DocumentCopy', badge: 3 },
  { path: '/approvals', labelKey: 'menu.approvals', icon: 'DataAnalysis', badge: 0 },
  { path: '/profile', labelKey: 'menu.profile', icon: 'User', badge: 0 },
]

// 判断当前路由是否激活
function isActive(path: string) {
  return route.path.startsWith(path)
}

// 滚动隐藏/显示导航栏
const isHidden = ref(false)
let lastScrollY = 0
let ticking = false

function handleScroll() {
  if (!ticking) {
    window.requestAnimationFrame(() => {
      const currentScrollY = window.scrollY
      // 向下滚动超过 60px 时隐藏，向上滚动时显示
      if (currentScrollY > lastScrollY && currentScrollY > 60) {
        isHidden.value = true
      } else {
        isHidden.value = false
      }
      lastScrollY = currentScrollY
      ticking = false
    })
    ticking = true
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style lang="scss" scoped>
.mobile-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: none; // 默认隐藏，移动端显示
  grid-template-columns: repeat(4, 1fr);
  height: 64px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  z-index: 100;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &.is-hidden {
    transform: translateY(100%);
  }

  // 安全区域适配
  padding-bottom: env(safe-area-inset-bottom, 0);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: var(--text-secondary);
  text-decoration: none;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;

  &:active {
    transform: scale(0.92);
  }

  &.active {
    color: var(--primary);

    .nav-icon {
      transform: translateY(-2px);
    }

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 32px;
      height: 3px;
      background: var(--primary-gradient);
      border-radius: 0 0 4px 4px;
    }
  }
}

.nav-icon {
  position: relative;
  transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.nav-badge {
  position: absolute;
  top: -4px;
  right: -6px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background: var(--danger);
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: badgePulse 2s ease-in-out infinite;
}

@keyframes badgePulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.nav-label {
  font-size: 11px;
  font-weight: 500;
}

// 深色模式
.dark .mobile-nav {
  background: rgba(15, 22, 41, 0.95);
  border-top-color: rgba(255, 255, 255, 0.06);
}

// 移动端显示
@media (max-width: 768px) {
  .mobile-nav {
    display: grid;
  }
}
</style>
