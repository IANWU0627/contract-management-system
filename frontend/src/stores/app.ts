import { defineStore } from 'pinia'
import { ref, watch, computed } from 'vue'

export type ThemeMode = 'light' | 'dark' | 'system'

// 检测系统是否偏好深色模式
const getSystemDarkPreference = (): boolean => {
  return window.matchMedia?.('(prefers-color-scheme: dark)').matches ?? false
}

export const useAppStore = defineStore('app', () => {
  // 主题：支持 light / dark / system
  const theme = ref<ThemeMode>((localStorage.getItem('theme') as ThemeMode) || 'light')

  // 语言
  const locale = ref(localStorage.getItem('locale') || 'zh')

  // 系统名称
  const systemName = ref(localStorage.getItem('systemName') || '企业合同管理系统')

  // 侧边栏折叠
  const sidebarCollapsed = ref(false)

  // 计算实际是否为暗色模式（考虑 system 模式）
  const isDark = computed((): boolean => {
    if (theme.value === 'system') {
      return getSystemDarkPreference()
    }
    return theme.value === 'dark'
  })

  // 计算当前实际主题模式（用于显示）
  const effectiveTheme = computed<'light' | 'dark'>(() => isDark.value ? 'dark' : 'light')

  // 应用主题到 DOM
  const applyThemeToDOM = (dark: boolean) => {
    document.body.classList.toggle('dark', dark)
    document.documentElement.classList.toggle('dark', dark)
    
    if (dark) {
      document.body.setAttribute('data-theme', 'dark')
      document.documentElement.setAttribute('data-theme', 'dark')
    } else {
      document.body.removeAttribute('data-theme')
      document.documentElement.removeAttribute('data-theme')
    }
  }

  // 设置主题
  const setTheme = (newTheme: string) => {
    theme.value = newTheme as ThemeMode
    localStorage.setItem('theme', newTheme)
    applyTheme(isDark.value)
  }

  // 应用主题（内部方法，不改变 store 值）
  const applyTheme = (dark: boolean) => {
    applyThemeToDOM(dark)
  }

  // 切换主题 (light → dark → system 循环)
  const toggleTheme = () => {
    if (theme.value === 'light') {
      setTheme('dark')
    } else if (theme.value === 'dark') {
      setTheme('system')
    } else {
      setTheme('light')
    }
  }

  // 设置语言
  const setLocale = (newLocale: string) => {
    locale.value = newLocale
    localStorage.setItem('locale', newLocale)
  }

  // 切换侧边栏
  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  // 设置系统名称
  const setSystemName = (newName: string) => {
    systemName.value = newName
    localStorage.setItem('systemName', newName)
  }

  // 初始化主题
  const initTheme = () => {
    applyTheme(isDark.value)
    
    // 如果是 system 模式，监听系统偏好变化
    if (theme.value === 'system' || !localStorage.getItem('theme')) {
      const mediaQuery = window.matchMedia?.('(prefers-color-scheme: dark)')
      if (mediaQuery) {
        const handler = (e: MediaQueryListEvent) => {
          if (theme.value === 'system') {
            applyTheme(e.matches)
          }
        }
        mediaQuery.addEventListener?.('change', handler)
        // 存储引用以便清理（可选）
        ;(window as any).__themeMediaHandler = handler
      }
    }
  }

  return {
    theme,
    locale,
    systemName,
    sidebarCollapsed,
    isDark,
    effectiveTheme,
    setTheme,
    toggleTheme,
    setLocale,
    setSystemName,
    toggleSidebar,
    initTheme,
    applyTheme
  }
})
