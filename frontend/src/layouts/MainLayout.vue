<template>
  <div class="app-container" :class="{ dark: isDark }">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="logo">
        <div class="logo-icon">C</div>
        <span v-if="!isCollapsed" class="logo-text">Toy Contract</span>
      </div>
      
      <div class="menu-wrapper">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapsed"
          :router="true"
          :collapse-transition="false"
          background-color="transparent"
          text-color="#ffffff"
          active-text-color="#ffffff"
        >
          <!-- 工作台 -->
          <el-menu-item index="/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <template #title>
              <span class="menu-title">{{ $t('menu.dashboard') }}</span>
            </template>
          </el-menu-item>
          
          <!-- ==================== 合同管理（一级菜单） ==================== -->
          <el-menu-item index="/contracts">
            <el-icon><Document /></el-icon>
            <template #title>
              <span class="menu-title">{{ $t('menu.contracts') }}</span>
            </template>
          </el-menu-item>
          
          <!-- ==================== 模板管理（一级菜单） ==================== -->
          <el-menu-item index="/templates">
            <el-icon><FolderOpened /></el-icon>
            <template #title>
              <span class="menu-title">{{ $t('menu.templates') }}</span>
            </template>
          </el-menu-item>
          
          <!-- ==================== 工作流（子菜单） ==================== -->
          <el-sub-menu index="/workflow" v-if="hasPermission('CONTRACT_APPROVE') || hasPermission('RENEWAL_MANAGE')">
            <template #title>
              <el-icon><Guide /></el-icon>
              <span>{{ $t('menu.workflow') }}</span>
            </template>
            <el-menu-item index="/approvals" v-if="hasPermission('CONTRACT_APPROVE')">
              <el-icon><Checked /></el-icon>
              <span>{{ $t('menu.approvals') }}</span>
            </el-menu-item>
            <el-menu-item index="/renewals" v-if="hasPermission('RENEWAL_MANAGE')">
              <el-icon><RefreshRight /></el-icon>
              <span>{{ $t('menu.renewals') }}</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- ==================== 数据管理（子菜单） ==================== -->
          <el-sub-menu index="/data">
            <template #title>
              <el-icon><Box /></el-icon>
              <span>{{ $t('menu.dataManagement') }}</span>
            </template>
            <el-menu-item index="/folders">
              <el-icon><FolderOpened /></el-icon>
              <span>{{ $t('menu.folderManagement') }}</span>
            </el-menu-item>
            <el-menu-item index="/tags">
              <el-icon><Collection /></el-icon>
              <span>{{ $t('menu.tagManagement') }}</span>
            </el-menu-item>
            <el-menu-item index="/categories" v-if="hasRole(['ADMIN'])">
              <el-icon><Tickets /></el-icon>
              <span>{{ $t('contract.categoryManagement') }}</span>
            </el-menu-item>
            <el-menu-item index="/quick-codes" v-if="hasRole(['ADMIN'])">
              <el-icon><Grid /></el-icon>
              <span>{{ $t('quickCode.management') }}</span>
            </el-menu-item>
            <el-menu-item index="/reminders">
              <el-icon><Bell /></el-icon>
              <span>{{ $t('menu.reminders') }}</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- ==================== 统计分析（子菜单） ==================== -->
          <el-sub-menu index="/analytics" v-if="hasPermission('REPORT_VIEW') || hasPermission('FAVORITE_MANAGE')">
            <template #title>
              <el-icon><TrendCharts /></el-icon>
              <span>{{ $t('menu.analytics') }}</span>
            </template>
            <el-menu-item index="/statistics" v-if="hasPermission('REPORT_VIEW')">
              <el-icon><DataAnalysis /></el-icon>
              <span>{{ $t('menu.statistics') }}</span>
            </el-menu-item>
            <el-menu-item index="/favorites" v-if="hasPermission('FAVORITE_MANAGE')">
              <el-icon><Star /></el-icon>
              <span>{{ $t('menu.favorites') }}</span>
            </el-menu-item>
            <el-menu-item index="/change-logs">
              <el-icon><Memo /></el-icon>
              <span>{{ $t('contract.changeLog') }}</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- ==================== 系统管理（子菜单，仅管理员） ==================== -->
          <el-sub-menu index="/system" v-if="hasRole(['ADMIN'])">
            <template #title>
              <el-icon><Management /></el-icon>
              <span>{{ $t('menu.systemManagement') }}</span>
            </template>
            <el-menu-item index="/users">
              <el-icon><User /></el-icon>
              <span>{{ $t('menu.users') }}</span>
            </el-menu-item>
            <el-menu-item index="/roles">
              <el-icon><UserFilled /></el-icon>
              <span>{{ $t('menu.roleManagement') }}</span>
            </el-menu-item>
            <el-menu-item index="/reminder-rules">
              <el-icon><Timer /></el-icon>
              <span>{{ $t('menu.reminderRules') }}</span>
            </el-menu-item>
            <el-menu-item index="/type-field-config">
              <el-icon><Tickets /></el-icon>
              <span>{{ $t('menu.typeFieldConfig') }}</span>
            </el-menu-item>
            <el-menu-item index="/variable-management">
              <el-icon><DocumentCopy /></el-icon>
              <span>{{ $t('menu.variableManagement') }}</span>
            </el-menu-item>
            <el-menu-item index="/settings">
              <el-icon><Tools /></el-icon>
              <span>{{ $t('menu.settings') }}</span>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </div>
      
      <!-- 底部用户信息 -->
      <div class="sidebar-footer" v-if="!isCollapsed">
        <el-dropdown @command="handleUserCommand" placement="top">
          <div class="user-mini">
              <el-avatar :size="28" :src="userInfo?.avatar" class="user-avatar">
                {{ userInfo?.nickname?.[0] || userInfo?.username?.[0] || 'U' }}
              </el-avatar>
              <span class="user-name">{{ userInfo?.nickname || userInfo?.username }}</span>
              <el-icon><ArrowUp /></el-icon>
            </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                {{ $t('menu.profile') }}
              </el-dropdown-item>
              <el-dropdown-item command="settings">
                <el-icon><Setting /></el-icon>
                {{ $t('menu.settings') }}
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><Close /></el-icon>
                {{ $t('common.logout') }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </aside>

    <!-- 右侧内容 -->
    <div class="main-container">
      <!-- 顶部导航 -->
      <header class="navbar">
        <div class="navbar-left">
          <div class="toggle-btn" @click="toggleSidebar">
            <el-icon><Fold v-if="!isCollapsed" /><Expand v-else /></el-icon>
          </div>
          <breadcrumb />
        </div>
        
        <div class="navbar-right">
          <!-- 工具按钮组 - 卡片式横向排列 -->
          <div class="tool-group">
            <!-- 搜索 -->
            <div class="tool-btn" @click="showSearchPanel" :title="t('common.search')">
              <el-icon :size="18"><Search /></el-icon>
            </div>
            
            <!-- 全屏 -->
            <div class="tool-btn" @click="toggleFullscreen" :title="isFullscreen ? t('common.exitFullscreen') : t('common.fullscreen')">
              <el-icon v-if="!isFullscreen" :size="18"><FullScreen /></el-icon>
              <el-icon v-else :size="18"><Close /></el-icon>
            </div>
            
            <!-- 语言 -->
            <div class="tool-btn" @click="toggleLocale" :title="t('settings.language')">
              <el-icon :size="18"><MapLocation /></el-icon>
            </div>
            
            <!-- 主题 - 支持亮色/暗色/跟随系统 -->
            <div class="tool-btn" @click="toggleTheme" :title="themeTitle">
              <el-icon v-if="appStore.effectiveTheme === 'dark'" :size="18"><Moon /></el-icon>
              <el-icon v-else :size="18"><Sunny /></el-icon>
              <span v-if="appStore.theme === 'system'" class="system-indicator">A</span>
            </div>
            
            <!-- 通知 -->
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notification-badge">
              <div class="tool-btn" @click="showNotificationPanel" :title="t('common.notification')">
                <el-icon :size="18"><Bell /></el-icon>
              </div>
            </el-badge>
            
            <!-- WebSocket 连接状态 -->
            <div class="ws-status" :class="{ connected: wsConnected }" :title="wsConnected ? t('notification.wsConnected') : t('notification.wsDisconnected')">
              <span class="ws-dot"></span>
              <span class="ws-text">{{ wsConnected ? 'Live' : 'Offline' }}</span>
            </div>
          </div>
        </div>
      </header>

      <!-- 页面标签 -->
      <div class="tags-view">
        <div class="tags-list">
          <div 
            v-for="tag in visitedViews" 
            :key="tag.path"
            class="tag-item"
            :class="{ active: tag.path === route.path }"
            @click="$router.push(tag.path)"
          >
            {{ t(tag.title) }}
            <el-icon class="close-icon" @click.stop="closeTag(tag)"><Close /></el-icon>
          </div>
        </div>
      </div>

      <!-- 页面内容 -->
      <main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
    
    <!-- 搜索弹窗 -->
    <el-dialog
      v-model="showSearch"
      title=""
      width="540px"
      :show-close="false"
      class="search-dialog"
      align-center
    >
      <div class="search-panel">
        <div class="search-input-wrapper">
          <div class="search-type-selector">
            <div 
              v-for="type in searchTypes" 
              :key="type.value"
              class="search-type-btn"
              :class="{ active: searchType === type.value }"
              @click="searchType = type.value"
            >
              <el-icon><component :is="type.icon" /></el-icon>
              <span>{{ type.label }}</span>
            </div>
          </div>
          <div class="search-input-row">
            <el-icon :size="18" class="search-icon" :class="{ searching: searching }"><Search /></el-icon>
            <input 
              v-model="searchKeyword"
              type="text" 
              :placeholder="searchPlaceholder" 
              @keyup.enter="doSearch"
              ref="searchInputRef"
            />
            <el-button 
              v-if="!searchKeyword" 
              type="primary" 
              @click="doSearch" 
              size="small" 
              class="search-btn"
              :loading="searching"
            >
              {{ t('common.search') }}
            </el-button>
            <el-button 
              v-else 
              type="info" 
              text 
              @click="clearSearch" 
              size="small" 
              class="clear-btn"
            >
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
        
        <!-- 搜索历史 -->
        <div class="search-history" v-if="!searchKeyword && searchHistory.length > 0">
          <div class="history-header">
            <span class="history-title">{{ t('common.searchHistory') }}</span>
            <el-button type="info" text size="small" @click="clearHistory">
              <el-icon><Delete /></el-icon>
              {{ t('common.clearAll') }}
            </el-button>
          </div>
          <div class="history-list">
            <span 
              v-for="(item, idx) in searchHistory" 
              :key="idx"
              class="history-item"
              @click="searchFromHistory(item)"
            >
              <el-icon><Clock /></el-icon>
              {{ item }}
            </span>
          </div>
        </div>
        
        <div class="search-results" v-if="searchResults.length > 0">
          <div class="results-header">
            <span class="results-count">{{ t('common.totalCount', { count: searchResults.length, item: t('common.searchResults') }) }}</span>
          </div>
          <div 
            v-for="item in searchResults" 
            :key="item.id" 
            class="search-item"
            @click="goToResult(item)"
          >
            <div class="item-icon" :class="item.type">
              <el-icon v-if="item.type === 'contract'" :size="18"><Document /></el-icon>
              <el-icon v-else-if="item.type === 'template'" :size="18"><FolderOpened /></el-icon>
              <el-icon v-else :size="18"><User /></el-icon>
            </div>
            <div class="item-info">
              <div class="item-title">{{ item.title }}</div>
              <div class="item-meta">
                <el-tag size="small" type="info" class="type-tag">{{ item.typeLabel }}</el-tag>
                <span class="meta-text">{{ item.meta }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="search-empty" v-else-if="searchKeyword && !searching">
          <div class="empty-content">
            <el-icon :size="56"><Search /></el-icon>
            <p class="empty-text">{{ t('common.none') }}</p>
            <p class="empty-hint">{{ t('common.tryDifferentKeyword') }}</p>
          </div>
        </div>
        
        <div class="search-tips" v-else-if="!searchKeyword && searchHistory.length === 0">
          <div class="tips-title">{{ t('dashboard.quickActions') }}</div>
          <div class="tips-list">
            <span @click="quickSearch('待审批')" class="quick-tip">
              <el-icon><Clock /></el-icon>
              {{ t('contract.statuses.pending') }}
            </span>
            <span @click="quickSearch('即将到期')" class="quick-tip">
              <el-icon><Bell /></el-icon>
              {{ t('statistics.expiringSoon') }}
            </span>
            <span @click="quickSearch('草稿')" class="quick-tip">
              <el-icon><Document /></el-icon>
              {{ t('contract.statuses.draft') }}
            </span>
          </div>
        </div>
      </div>
    </el-dialog>
    
    <!-- 通知弹窗 -->
    <el-dialog
      v-model="showNotifications"
      :title="t('common.notification')"
      width="460px"
      class="notification-dialog"
    >
      <div class="notification-panel">
        <!-- 设置栏 -->
        <div class="notification-settings">
          <div class="notification-tabs">
            <span 
              class="tab" 
              :class="{ active: notifTab === 'all' }" 
              @click="notifTab = 'all'"
            >{{ t('common.all') }}</span>
            <span 
              class="tab" 
              :class="{ active: notifTab === 'important' }" 
              @click="notifTab = 'important'"
            >
              <el-icon><Star /></el-icon>{{ t('notification.important') }}
            </span>
            <span 
              class="tab" 
              :class="{ active: notifTab === 'approval' }" 
              @click="notifTab = 'approval'"
            >{{ t('contract.approval') }}</span>
            <span 
              class="tab" 
              :class="{ active: notifTab === 'system' }" 
              @click="notifTab = 'system'"
            >{{ t('notification.system') }}</span>
          </div>
          <div class="settings-right">
            <el-tooltip :content="notificationSoundEnabled ? t('notification.soundOn') : t('notification.soundOff')">
              <span class="sound-toggle" @click="toggleSound">
                <el-icon v-if="notificationSoundEnabled" :size="18"><Bell /></el-icon>
                <el-icon v-else :size="18"><Close /></el-icon>
              </span>
            </el-tooltip>
            <el-dropdown trigger="click" @command="handleNotificationSettings">
              <span class="settings-btn">
                <el-icon :size="18"><Setting /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="markAll">{{ t('reminder.markRead') }}</el-dropdown-item>
                  <el-dropdown-item command="clearAll">{{ t('notification.clearAll') }}</el-dropdown-item>
                  <el-dropdown-item command="settings">{{ t('settings.appearance') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
        
        <div class="notification-list" @contextmenu.prevent="handleContextMenu">
          <div 
            v-for="notif in filteredNotifications" 
            :key="notif.id" 
            class="notification-item"
            :class="{ unread: notif.unread, [notif.priority]: true }"
            @click="handleNotificationClick(notif)"
            @contextmenu.prevent="showContextMenu($event, notif)"
            @touchstart="handleTouchStart($event, notif)"
            @touchmove="handleTouchMove($event)"
            @touchend="handleTouchEnd(notif)"
          >
            <div class="notif-indicator" :class="notif.priority"></div>
            <div class="notif-icon" :class="notif.type">
              <el-icon v-if="notif.type === 'approval'" :size="18"><Checked /></el-icon>
              <el-icon v-else-if="notif.type === 'system'" :size="18"><Bell /></el-icon>
              <el-icon v-else :size="18"><Bell /></el-icon>
            </div>
            <div class="notif-content">
              <div class="notif-header">
                <span class="notif-title">{{ notif.title }}</span>
                <span v-if="notif.unread" class="unread-dot"></span>
              </div>
              <div class="notif-desc">{{ notif.desc }}</div>
              <div class="notif-time">{{ formatTime(notif.time) }}</div>
            </div>
            <div class="notif-actions">
              <span class="delete-btn" @click.stop="deleteNotification(notif.id)">
                <el-icon :size="16"><Delete /></el-icon>
              </span>
            </div>
            <!-- Swipe delete hint -->
            <div class="swipe-delete-hint" :class="{ show: notif.swiping }">
              <el-icon :size="20"><Delete /></el-icon>
              <span>{{ t('common.delete') }}</span>
            </div>
          </div>
          
          <div class="notification-empty" v-if="filteredNotifications.length === 0">
            <el-icon :size="48"><Bell /></el-icon>
            <p>{{ t('notification.empty') }}</p>
          </div>
        </div>
        
        <div class="notification-footer">
          <span v-if="unreadCount > 0" @click="markAllRead">{{ t('reminder.markRead') }}</span>
          <span @click="$router.push('/notifications')">{{ t('common.viewAll') }}</span>
        </div>
      </div>
    </el-dialog>

    <!-- 右键菜单 -->
    <div 
      v-if="contextMenuVisible" 
      class="context-menu"
      :style="{ left: contextMenuX + 'px', top: contextMenuY + 'px' }"
      @click.stop
    >
      <div class="context-item" @click="markAsRead(contextMenuNotif)">
        <el-icon :size="16"><Checked /></el-icon>
        <span>{{ contextMenuNotif?.unread ? t('reminder.markRead') : t('notification.alreadyRead') }}</span>
      </div>
      <div class="context-item" @click="toggleImportant(contextMenuNotif)">
        <el-icon :size="16"><Star /></el-icon>
        <span>{{ contextMenuNotif?.important ? t('notification.removeImportant') : t('notification.setImportant') }}</span>
      </div>
      <div class="context-item danger" @click="deleteNotification(contextMenuNotif?.id)">
        <el-icon :size="16"><Delete /></el-icon>
        <span>{{ t('common.delete') }}</span>
      </div>
    </div>
    
    <!-- 点击遮罩关闭右键菜单 -->
    <div v-if="contextMenuVisible" class="context-overlay" @click="closeContextMenu"></div>
    
    <!-- 移动端底部导航 -->
    <MobileNav />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { webSocketService } from '@/services/websocket'
import Breadcrumb from '@/components/Breadcrumb.vue'
import MobileNav from '@/components/MobileNav.vue'
import { 
  User, UserFilled, Setting, Fold, Expand, Sunny, Moon,
  Search, FullScreen, Close, ArrowUp,
  Star, Collection, Timer, RefreshRight, Tools, Operation, Grid, Memo,
  Guide, Tickets, Box, Management, DocumentCopy,
  Connection, Loading, MapLocation, Document, FolderOpened, Clock, Bell
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const { locale, t } = useI18n()
const userStore = useUserStore()
const appStore = useAppStore()

const isCollapsed = computed(() => appStore.sidebarCollapsed)
const isDark = computed(() => appStore.isDark)
const themeTitle = computed(() => {
  if (appStore.theme === 'system') return t('settings.themeSystem') || '跟随系统'
  return appStore.isDark ? (t('settings.themeLight') || '切换到亮色') : (t('settings.themeDark') || '切换到暗色')
})
const userInfo = computed(() => userStore.userInfo)
const isAdmin = computed(() => userStore.isAdmin)
const isLoggedIn = computed(() => userStore.isLoggedIn)

const hasPermission = (permission: string): boolean => {
  return userStore.permissions.includes(permission)
}

const hasRole = (rolesToCheck: string[]): boolean => {
  return userStore.roles.some(role => rolesToCheck.includes(role))
}

// 待办数量（模拟）
const pendingCount = ref(2)
const expireCount = ref(1)
const showNotifications = ref(false)
const showSearch = ref(false)
const searchKeyword = ref('')
const searchResults = ref<any[]>([])
const searching = ref(false)
const notifTab = ref('all')
const searchType = ref('all')
const searchInputRef = ref<HTMLInputElement>()
const searchHistory = ref<string[]>([])

// 搜索类型配置
const searchTypes = computed(() => [
  { value: 'all', label: t('common.all'), icon: Search },
  { value: 'contract', label: t('menu.contracts'), icon: Document },
  { value: 'template', label: t('menu.templates'), icon: FolderOpened },
  { value: 'user', label: t('menu.users'), icon: User }
])

// 搜索占位符
const searchPlaceholder = computed(() => {
  switch (searchType.value) {
    case 'contract': return t('contract.name')
    case 'template': return t('template.name')
    case 'user': return t('user.username')
    default: return t('common.search')
  }
})

// 监听搜索弹窗打开，自动聚焦搜索框
watch(showSearch, (newVal) => {
  if (newVal) {
    setTimeout(() => {
      searchInputRef.value?.focus()
    }, 100)
  } else {
    searchKeyword.value = ''
    searchResults.value = []
  }
})

const showNotificationPanel = () => { showNotifications.value = true }
const showSearchPanel = () => { showSearch.value = true }
const toggleLocale = () => {
  const newLocale = locale.value === 'zh' ? 'en' : 'zh'
  locale.value = newLocale
  appStore.setLocale(newLocale)
}

// 加载搜索历史
const loadSearchHistory = () => {
  try {
    const saved = localStorage.getItem('contract-search-history')
    if (saved) {
      searchHistory.value = JSON.parse(saved)
    }
  } catch (e) {
    console.error('Failed to load search history:', e)
  }
}

// 保存搜索历史
const saveSearchHistory = (keyword: string) => {
  if (!keyword.trim()) return
  
  // 移除已存在的相同关键词
  searchHistory.value = searchHistory.value.filter(item => item !== keyword)
  
  // 添加到开头
  searchHistory.value.unshift(keyword)
  
  // 最多保存10条
  if (searchHistory.value.length > 10) {
    searchHistory.value = searchHistory.value.slice(0, 10)
  }
  
  // 保存到localStorage
  try {
    localStorage.setItem('contract-search-history', JSON.stringify(searchHistory.value))
  } catch (e) {
    console.error('Failed to save search history:', e)
  }
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
  searchResults.value = []
  searchInputRef.value?.focus()
}

// 清除历史记录
const clearHistory = () => {
  searchHistory.value = []
  try {
    localStorage.removeItem('contract-search-history')
  } catch (e) {
    console.error('Failed to clear search history:', e)
  }
}

// 从历史记录搜索
const searchFromHistory = (keyword: string) => {
  searchKeyword.value = keyword
  doSearch()
}

const doSearch = async () => {
  if (!searchKeyword.value) return
  searching.value = true
  
  // 保存搜索历史
  saveSearchHistory(searchKeyword.value)
  
  // 根据搜索类型过滤结果
  const filterResults = (results: any[]) => {
    if (searchType.value === 'all') return results
    return results.filter(r => r.type === searchType.value)
  }
  
  // 模拟搜索结果
  setTimeout(() => {
    const allResults = [
      { id: 1, type: 'contract', title: t('contract.types.purchase') + '-Supplier A', meta: t('contract.no') + ': CT-2024-001', typeLabel: t('menu.contracts') },
      { id: 2, type: 'template', title: t('contract.types.purchase') + ' Template', meta: t('template.category') + ': ' + t('contract.types.purchase'), typeLabel: t('menu.templates') },
      { id: 3, type: 'user', title: 'Admin User', meta: t('user.role') + ': ' + t('user.roles.admin'), typeLabel: t('menu.users') },
    ]
    searchResults.value = filterResults(allResults)
    searching.value = false
  }, 500)
}

const quickSearch = (keyword: string) => {
  searchKeyword.value = keyword
  doSearch()
}

const goToResult = (item: any) => {
  showSearch.value = false
  if (item.type === 'contract') {
    router.push(`/contracts/${item.id}`)
  } else if (item.type === 'template') {
    router.push(`/templates/${item.id}`)
  } else if (item.type === 'user') {
    router.push('/users')
  }
}

// 通知列表 - 增强版
interface Notification {
  id: number
  type: 'approval' | 'reminder' | 'system' | 'renewal'
  priority: 'important' | 'normal' | 'system'
  title: string
  desc: string
  time: Date
  unread: boolean
  important: boolean
  link?: string
  swiping?: boolean
}

// 初始化通知列表（合并模拟数据和WebSocket数据）
const notifications = ref<Notification[]>([])

// 合并 WebSocket 通知到本地列表
const mergeWebSocketNotifications = () => {
  const wsNotifications = webSocketService.notifications.value
  wsNotifications.forEach(wsNotif => {
    // 检查是否已存在
    const exists = notifications.value.some(n => 
      n.title === wsNotif.title && 
      n.desc === wsNotif.content &&
      (Date.now() - n.time.getTime()) < 60000 // 1分钟内的不重复
    )
    if (!exists) {
      notifications.value.unshift({
        id: wsNotif.id,
        type: wsNotif.type || 'system',
        priority: wsNotif.priority || 'normal',
        title: wsNotif.title,
        desc: wsNotif.content,
        time: new Date(wsNotif.timestamp) || new Date(),
        unread: !wsNotif.read,
        important: wsNotif.priority === 'important',
        link: wsNotif.link
      })
    }
  })
}

// 监听 WebSocket 新通知
watch(() => webSocketService.notifications.value.length, () => {
  mergeWebSocketNotifications()
})

// 初始化默认通知（用于展示）
onMounted(() => {
  // 加载搜索历史
  loadSearchHistory()
  
  // 添加一些默认通知
  notifications.value = [
    { id: 1, type: 'approval', priority: 'important', title: t('contract.statuses.pending'), desc: t('contract.statuses.pending') + ' 3', time: new Date(Date.now() - 10 * 60 * 1000), unread: true, important: true, link: '/approvals' },
    { id: 2, type: 'reminder', priority: 'normal', title: t('statistics.expiringSoon'), desc: t('statistics.expiringSoon') + ' 7', time: new Date(Date.now() - 30 * 60 * 1000), unread: true, important: false, link: '/reminders' },
    { id: 3, type: 'approval', priority: 'normal', title: t('contract.statuses.approved'), desc: t('contract.statuses.approved'), time: new Date(Date.now() - 2 * 60 * 60 * 1000), unread: false, important: false, link: '/approvals' },
    { id: 4, type: 'system', priority: 'system', title: t('notification.systemUpdate'), desc: t('notification.systemUpdateDesc'), time: new Date(Date.now() - 24 * 60 * 60 * 1000), unread: false, important: false, link: '/settings' },
  ]
  mergeWebSocketNotifications()
})

const unreadCount = computed(() => notifications.value.filter(n => n.unread).length)

// WebSocket 连接状态
const wsConnected = computed(() => webSocketService.connected.value)

const filteredNotifications = computed(() => {
  if (notifTab.value === 'all') return notifications.value
  if (notifTab.value === 'important') return notifications.value.filter(n => n.important)
  if (notifTab.value === 'approval') return notifications.value.filter(n => n.type === 'approval')
  if (notifTab.value === 'system') return notifications.value.filter(n => n.type === 'system')
  return notifications.value
})

const markAllRead = () => {
  notifications.value.forEach(n => n.unread = false)
}

const deleteNotification = (id?: number) => {
  if (id) {
    notifications.value = notifications.value.filter(n => n.id !== id)
  }
  closeContextMenu()
}

// 格式化时间
const formatTime = (date: Date) => {
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return locale.value === 'zh' ? '刚刚' : 'Just now'
  if (minutes < 60) return locale.value === 'zh' ? `${minutes}分钟前` : `${minutes}m ago`
  if (hours < 24) return locale.value === 'zh' ? `${hours}小时前` : `${hours}h ago`
  if (days < 7) return locale.value === 'zh' ? `${days}天前` : `${days}d ago`
  return date.toLocaleDateString()
}

// 声音开关
const notificationSoundEnabled = ref(localStorage.getItem('notificationSound') !== 'false')

const toggleSound = () => {
  notificationSoundEnabled.value = !notificationSoundEnabled.value
  localStorage.setItem('notificationSound', String(notificationSoundEnabled.value))
}

// 播放提示音
const playNotificationSound = () => {
  if (notificationSoundEnabled.value) {
    // 使用 Web Audio API 播放简单提示音
    try {
      const audioContext = new (window.AudioContext || (window as any).webkitAudioContext)()
      const oscillator = audioContext.createOscillator()
      const gainNode = audioContext.createGain()
      oscillator.connect(gainNode)
      gainNode.connect(audioContext.destination)
      oscillator.frequency.value = 800
      oscillator.type = 'sine'
      gainNode.gain.value = 0.1
      oscillator.start()
      setTimeout(() => oscillator.stop(), 100)
    } catch (e) {
      console.log('Audio not supported')
    }
  }
}

// WebSocket 模拟推送
let wsTimer: ReturnType<typeof setInterval> | null = null
const startWebSocket = () => {
  // 模拟 WebSocket 推送，每 30-60 秒推送一条新通知
  const simulatePush = () => {
    const types: Array<{type: 'approval' | 'reminder' | 'system', priority: 'important' | 'normal' | 'system', title: string, desc: string, link: string}> = [
      { type: 'approval', priority: 'important', title: t('contract.statuses.pending'), desc: t('contract.statuses.pending') + ' 1', link: '/approvals' },
      { type: 'reminder', priority: 'normal', title: t('statistics.expiringSoon'), desc: t('statistics.expiringSoon') + ' 2', link: '/reminders' },
      { type: 'system', priority: 'system', title: t('notification.newFeature'), desc: t('notification.newFeatureDesc'), link: '/settings' },
    ]
    const random = types[Math.floor(Math.random() * types.length)]
    
    const newNotif: Notification = {
      id: Date.now(),
      ...random,
      time: new Date(),
      unread: true,
      important: random.priority === 'important'
    }
    
    notifications.value.unshift(newNotif)
    playNotificationSound()
  }
  
  // 启动定时推送（仅开发环境模拟）
  wsTimer = setInterval(simulatePush, 45000)
}

const stopWebSocket = () => {
  if (wsTimer) {
    clearInterval(wsTimer)
    wsTimer = null
  }
}

// 点击通知
const handleNotificationClick = (notif: Notification) => {
  notif.unread = false
  showNotifications.value = false
  if (notif.link) {
    router.push(notif.link)
  }
}

// 右键菜单
const contextMenuVisible = ref(false)
const contextMenuX = ref(0)
const contextMenuY = ref(0)
const contextMenuNotif = ref<Notification | null>(null)

const showContextMenu = (event: MouseEvent, notif: Notification) => {
  contextMenuNotif.value = notif
  contextMenuX.value = event.clientX
  contextMenuY.value = event.clientY
  contextMenuVisible.value = true
}

const closeContextMenu = () => {
  contextMenuVisible.value = false
}

const markAsRead = (notif: Notification | null) => {
  if (notif) notif.unread = false
  closeContextMenu()
}

const toggleImportant = (notif: Notification | null) => {
  if (notif) notif.important = !notif.important
  closeContextMenu()
}

const handleContextMenu = () => {}

// 滑动删除
const touchStartX = ref(0)
const touchStartY = ref(0)
const swipingNotifId = ref<number | null>(null)

const handleTouchStart = (event: TouchEvent, notif: Notification) => {
  touchStartX.value = event.touches[0].clientX
  touchStartY.value = event.touches[0].clientY
}

const handleTouchMove = (event: TouchEvent) => {
  const diffX = touchStartX.value - event.touches[0].clientX
  const diffY = Math.abs(touchStartY.value - event.touches[0].clientY)
  
  // 只处理水平滑动
  if (diffY < 10 && diffX > 50 && swipingNotifId.value) {
    event.preventDefault()
  }
}

const handleTouchEnd = (notif: Notification) => {
  const diffX = touchStartX.value - 0 // reset
  if (diffX > 100) {
    deleteNotification(notif.id)
  }
}

// 通知设置
const handleNotificationSettings = (command: string) => {
  if (command === 'markAll') {
    markAllRead()
  } else if (command === 'clearAll') {
    notifications.value = []
  } else if (command === 'settings') {
    router.push('/settings')
    showNotifications.value = false
  }
}

// 启动时连接 WebSocket (graceful fallback)
onMounted(() => {
  // Only simulate notifications in development
  if (import.meta.env.DEV) {
    startWebSocket()
  }
  // WebSocket connection will silently fail if server doesn't support it
  // App will work normally without real-time notifications
  if (userStore.isLoggedIn && userStore.userInfo?.id) {
    try {
      webSocketService.connect(userStore.userInfo.id)
    } catch (e) {
      // Silently ignore WebSocket connection failures
    }
  }
})

onUnmounted(() => {
  stopWebSocket()
  webSocketService.disconnect()
})

// 访问过的页面标签
const visitedViews = ref([
  { path: '/dashboard', title: 'menu.dashboard' },
  { path: '/contracts', title: 'menu.contracts' }
])

const activeMenu = computed(() => route.path)

const toggleSidebar = () => appStore.toggleSidebar()
const toggleTheme = () => appStore.toggleTheme()

const isFullscreen = ref(false)

const toggleFullscreen = async () => {
  try {
    if (!document.fullscreenElement) {
      await document.documentElement.requestFullscreen()
      isFullscreen.value = true
    } else {
      await document.exitFullscreen()
      isFullscreen.value = false
    }
  } catch (e) { }
}

const handleLocaleChange = (lang: string) => {
  locale.value = lang
  appStore.setLocale(lang)
}

const handleUserCommand = (command: string) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'settings') {
    router.push('/settings')
  }
}

const closeTag = (tag: any) => {
  const index = visitedViews.value.findIndex(v => v.path === tag.path)
  if (index > -1) {
    visitedViews.value.splice(index, 1)
  }
}

// 监听路由变化添加标签
watch(() => route.path, (newPath) => {
  const exists = visitedViews.value.find(v => v.path === newPath)
  if (!exists) {
    visitedViews.value.push({
      path: newPath,
      title: route.meta.title as string || '未命名'
    })
  }
}, { immediate: true })
</script>

<style lang="scss">
/* 全局样式：折叠菜单的弹出子菜单 */
.el-menu--popup {
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  box-shadow: 4px 0 16px rgba(102, 126, 234, 0.2) !important;
  
  .el-menu-item {
    color: rgba(255, 255, 255, 0.75) !important;
    
    &:hover {
      background: rgba(255, 255, 255, 0.1) !important;
      color: #fff !important;
    }
    
    &.is-active {
      background: rgba(255, 255, 255, 0.25) !important;
      color: #fff !important;
    }
  }
}
</style>

<style scoped lang="scss">
/* ── CSS Variables ─────────────────────────────────── */
:root {
  --primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --secondary-gradient: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  --bg-light: #f8f9fc;
  --bg-dark: #0f0c29;
  --card-shadow: 0 8px 24px rgba(102, 126, 234, 0.12);
  --card-shadow-hover: 0 12px 32px rgba(102, 126, 234, 0.18);
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* ── App Container ─────────────────────────────────── */
.app-container {
  display: flex;
  height: 100vh;
  background: var(--bg-page);
  overflow: hidden;
  
  &.dark {
    background: var(--bg-page);
  }
}

/* ── Sidebar ───────────────────────────────────────── */
.sidebar {
  width: 220px;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  box-shadow: 4px 0 16px rgba(102, 126, 234, 0.2);
  
  &.collapsed {
    width: 64px;
    
    .logo {
      padding: 16px 0;
      
      .logo-icon {
        width: 40px;
        height: 40px;
        font-size: 20px;
      }
    }
  }
  
  .logo {
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    padding: 0 20px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.15);
    
    .logo-icon {
      width: 36px;
      height: 36px;
      background: rgba(255, 255, 255, 0.2);
      backdrop-filter: blur(10px);
      border: 1px solid rgba(255, 255, 255, 0.3);
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 18px;
      font-weight: 700;
      flex-shrink: 0;
      transition: all 0.3s;
      
      &:hover {
        background: rgba(255, 255, 255, 0.3);
        transform: scale(1.05);
      }
    }
    
    .logo-text {
      color: #fff;
      font-size: 18px;
      font-weight: 700;
      letter-spacing: -0.5px;
    }
  }
  
  .menu-wrapper {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;
    padding: 16px 8px;
    
    &::-webkit-scrollbar {
      width: 4px;
    }
    
    &::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.2);
      border-radius: 2px;
      
      &:hover {
        background: rgba(255, 255, 255, 0.3);
      }
    }
  }
  
  .el-menu {
    background: transparent;
    border-right: none;
    
    .el-menu-item {
      margin: 6px 0;
      padding: 0 12px !important;
      border-radius: 12px;
      height: auto;
      min-height: 48px;
      line-height: 20px;
      padding-top: 10px !important;
      padding-bottom: 10px !important;
      color: rgba(255, 255, 255, 0.75);
      font-weight: 500;
      transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
      display: flex;
      align-items: center;
      position: relative;
      overflow: hidden;
      
      .el-icon {
        font-size: 20px;
        color: rgba(255, 255, 255, 0.75);
        transition: all 0.3s;
      }
      
      .menu-title {
        font-weight: 500;
        margin-left: 12px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        flex: 1;
        cursor: pointer;
      }
      
      .menu-badge {
        margin-left: auto;
        margin-right: 8px;
      }
      
      &::before {
        content: '';
        position: absolute;
        inset: 0;
        background: linear-gradient(90deg, rgba(255,255,255,0.2) 0%, transparent 100%);
        opacity: 0;
        transition: opacity 0.3s;
      }
      
      &:hover {
        background: rgba(255, 255, 255, 0.1);
        color: #fff;
        transform: translateX(4px);
        
        .el-icon {
          color: #fff;
          transform: scale(1.1);
        }
        
        &::before {
          opacity: 1;
        }
      }
      
      &.is-active {
        background: rgba(255, 255, 255, 0.25);
        color: #fff;
        font-weight: 600;
        
        .el-icon {
          color: #fff;
          transform: scale(1.1);
        }
        
        &::before {
          opacity: 1;
        }
        
        &::after {
          content: '';
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          width: 4px;
          height: 24px;
          background: #fff;
          border-radius: 0 4px 4px 0;
          box-shadow: 0 0 12px rgba(255, 255, 255, 0.5);
        }
      }
    }
    
    // 折叠菜单的弹出子菜单样式
    :deep(.el-menu--popup) {
      background: linear-gradient(180deg, #667eea 0%, #764ba2 100%) !important;
      border: none !important;
      box-shadow: 4px 0 16px rgba(102, 126, 234, 0.2) !important;
      
      .el-menu-item {
        color: rgba(255, 255, 255, 0.75) !important;
        
        &:hover {
          background: rgba(255, 255, 255, 0.1) !important;
          color: #fff !important;
        }
        
        &.is-active {
          background: rgba(255, 255, 255, 0.25) !important;
          color: #fff !important;
        }
      }
    }
  }
  
  .sidebar-footer {
    padding: 12px 8px;
    border-top: 1px solid rgba(255, 255, 255, 0.15);
    margin-top: auto;
    
    .user-mini {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 10px 12px;
      border-radius: 10px;
      cursor: pointer;
      transition: all 0.3s;
      color: rgba(255, 255, 255, 0.8);
      
      &:hover {
        background: rgba(255, 255, 255, 0.15);
        color: #fff;
      }
      
      .user-avatar {
        flex-shrink: 0;
      }
      
      .user-name {
        flex: 1;
        font-size: 14px;
        font-weight: 500;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .el-icon {
        font-size: 12px;
        transition: transform 0.3s;
      }
    }
  }
}

/* ── Main Container ────────────────────────────────── */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

/* ── Navbar ────────────────────────────────────────── */
.navbar {
  height: 56px;
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
  
  .navbar-left {
    display: flex;
    align-items: center;
    gap: 16px;
    flex: 1;
    
    .toggle-btn {
      width: 34px;
      height: 34px;
      border-radius: 8px;
      border: none;
      background: #f4f4f5;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s;
      
      &:hover {
        background: #667eea;
        color: white;
      }
    }
  }
  
  .navbar-right {
    display: flex;
    align-items: center;
    gap: 8px;
    
    /* 工具按钮组 - 卡片式 */
    .tool-group {
      display: flex;
      align-items: center;
      gap: 2px;
      padding: 4px 8px;
      background: transparent;
      border: none;
      border-radius: 10px;
      box-shadow: none;
    }
    
    .tool-btn {
      width: 36px;
      height: 36px;
      border-radius: 10px;
      border: none;
      background: transparent;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
      position: relative;
      
      svg {
        width: 18px;
        height: 18px;
        color: #52525b;
        transition: all 0.3s ease;
      }
      
      &:hover {
        background: linear-gradient(135deg, rgba(102, 126, 234, 0.12) 0%, rgba(118, 75, 162, 0.12) 100%);
        
        svg {
          color: #667eea;
          transform: scale(1.1);
        }
        
        &::after {
          content: '';
          position: absolute;
          bottom: 2px;
          left: 50%;
          transform: translateX(-50%);
          width: 4px;
          height: 4px;
          background: #667eea;
          border-radius: 50%;
        }
      }
      
      &.active {
        background: var(--primary-gradient);
        
        svg {
          color: white;
        }
        
        &::after {
          display: none;
        }
      }
    }
    
    .notification-badge {
      :deep(.el-badge__content) {
        top: 2px;
        right: 2px;
        background: linear-gradient(135deg, #ff6b6b, #ee5a24);
        border: none;
      }
    }
  }
}

/* ── Content Area ──────────────────────────────────── */
.content-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  
  .breadcrumb-bar {
    padding: 16px 32px;
    background: #fff;
    border-bottom: 1px solid #e4e4e7;
    flex-shrink: 0;
  }
  
  .main-content {
    flex: 1;
    overflow-y: auto;
    padding: 24px 32px;
    
    &::-webkit-scrollbar {
      width: 8px;
    }
    
    &::-webkit-scrollbar-track {
      background: transparent;
    }
    
    &::-webkit-scrollbar-thumb {
      background: #d4d4d8;
      border-radius: 4px;
      
      &:hover {
        background: #a1a1aa;
      }
    }
  }
}

/* ── Tags Bar ──────────────────────────────────────── */
.tags-bar {
  height: 44px;
  background: #fff;
  border-bottom: 1px solid #e4e4e7;
  padding: 0 32px;
  display: flex;
  align-items: center;
  gap: 8px;
  overflow-x: auto;
  flex-shrink: 0;
  
  &::-webkit-scrollbar {
    height: 4px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #d4d4d8;
    border-radius: 2px;
  }
  
  .tag {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 6px 12px;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
    border: 1px solid rgba(102, 126, 234, 0.2);
    border-radius: 8px;
    font-size: 13px;
    color: #667eea;
    cursor: pointer;
    transition: all 0.3s;
    white-space: nowrap;
    
    &:hover {
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
      border-color: rgba(102, 126, 234, 0.3);
    }
    
    &.active {
      background: var(--primary-gradient);
      color: white;
      border-color: transparent;
    }
    
    .close-btn {
      width: 16px;
      height: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      opacity: 0.6;
      transition: opacity 0.2s;
      
      &:hover {
        opacity: 1;
      }
    }
  }
}

/* ── Dark Mode ─────────────────────────────────────── */
.dark {
  .navbar {
    background: var(--bg-card);
    border-bottom-color: var(--border-color);
    
    .tool-group {
      background: var(--bg-hover);
      border-color: var(--border-color);
    }
    
    .tool-btn {
      svg {
        color: var(--text-secondary);
      }
      
      &:hover {
        background: var(--info-bg);
        
        svg {
          color: var(--primary);
        }
      }
    }
    
    .ws-status {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 6px 12px;
      border-radius: 16px;
      background: rgba(255, 255, 255, 0.1);
      font-size: 12px;
      color: var(--text-secondary);
      transition: all 0.3s;
      
      .ws-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #f87171;
        transition: background 0.3s;
      }
      
      .ws-text {
        font-weight: 500;
      }
      
      &.connected {
        background: rgba(34, 197, 94, 0.15);
        color: #22c55e;
        
        .ws-dot {
          background: #22c55e;
          box-shadow: 0 0 8px rgba(34, 197, 94, 0.5);
          animation: pulse 2s infinite;
        }
      }
      
      @keyframes pulse {
        0%, 100% {
          opacity: 1;
        }
        50% {
          opacity: 0.5;
        }
      }
    }
  }
  
  .content-wrapper {
    .breadcrumb-bar {
      background: var(--bg-card);
      border-bottom-color: var(--border-color);
    }
    
    .main-content {
      background: var(--bg-page);
    }
  }
  
  .tags-view {
    background: var(--bg-card);
    border-bottom-color: var(--border-color);
    
    .tag-item {
      background: var(--bg-hover);
      color: var(--text-regular);
      
      &.active {
        background: rgba(129, 140, 248, 0.2);
        color: var(--primary-light);
        border-color: rgba(129, 140, 248, 0.3);
      }
      
      &:hover {
        background: var(--bg-hover);
        color: var(--primary-light);
      }
    }
  }
}

.tags-view {
  height: 44px;
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-color);
  padding: 0 24px;
  display: flex;
  align-items: center;
  flex-shrink: 0;
  
  .tags-list {
    display: flex;
    gap: 8px;
    overflow-x: auto;
    
    &::-webkit-scrollbar {
      display: none;
    }
    
    .tag-item {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 6px 12px;
      background: var(--bg-hover);
      border-radius: 6px;
      font-size: 13px;
      color: var(--text-regular);
      cursor: pointer;
      white-space: nowrap;
      transition: all 0.2s;
      border: 1px solid transparent;
      
      &:hover {
        background: var(--bg-color);
      }
      
      &.active {
        background: var(--el-color-primary-light-9);
        color: var(--el-color-primary);
        border-color: var(--el-color-primary-light-5);
        font-weight: 500;
      }
      
      .close-icon {
        font-size: 12px;
        opacity: 0;
        transition: opacity 0.2s;
        
        &:hover {
          color: var(--el-color-danger);
        }
      }
      
      &:hover .close-icon {
        opacity: 1;
      }
    }
  }
}

.main-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  overflow-x: hidden;
}

// 语言下拉菜单样式
:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  
  .lang-flag {
    font-size: 16px;
  }
  
  &.active {
    color: var(--el-color-primary);
    font-weight: 500;
    background: var(--el-color-primary-light-9);
  }
}

// 页面切换动画
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s ease;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

// 暗色主题适配
.dark {
  .navbar {
    background: var(--bg-card);
    border-color: var(--border-color);
    
    .toggle-btn {
      background: var(--bg-hover);
      
      &:hover {
        background: var(--primary);
      }
    }
    
    .breadcrumb-bar {
      background: var(--bg-card);
      border-color: var(--border-color);
    }
  }
  
  .tags-view {
    background: var(--bg-card);
    border-color: var(--border-color);
    
    .tag-item {
      background: var(--bg-hover);
      color: var(--text-secondary);
      
      &:hover {
        background: var(--bg-input);
        color: var(--text-primary);
      }
      
      &.active {
        background: rgba(129, 140, 248, 0.2);
        color: var(--primary-light);
        border-color: rgba(129, 140, 248, 0.3);
      }
    }
  }
}

/* 搜索弹窗 */
.search-dialog {
  :deep(.el-dialog) {
    border-radius: var(--radius-lg);
    overflow: hidden;
    border: none;
    box-shadow: 0 20px 60px rgba(102, 126, 234, 0.2);
  }
  
  :deep(.el-dialog__header) {
    background: var(--primary-gradient);
    padding: 20px 24px;
    margin: 0;
    border-bottom: none;
    
    .el-dialog__title {
      color: white;
      font-weight: 600;
      font-size: 16px;
    }
    
    .el-dialog__headerbtn {
      .el-icon {
        color: rgba(255, 255, 255, 0.8);
        
        &:hover {
          color: white;
        }
      }
    }
  }
}

.search-panel {
  .search-input-wrapper {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 16px;
    background: var(--bg-hover);
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
    margin-bottom: 16px;
    
    .search-type-selector {
      display: flex;
      gap: 8px;
      justify-content: space-between;
      
      .search-type-btn {
        display: flex;
        align-items: center;
        gap: 4px;
        padding: 8px 8px;
        border-radius: 12px;
        font-size: 12px;
        color: var(--text-secondary);
        background: var(--bg-color);
        cursor: pointer;
        transition: all 0.2s;
        border: 1px solid transparent;
        flex: 1;
        min-width: 0;
        justify-content: center;
        white-space: nowrap;
        
        .el-icon {
          font-size: 14px;
          flex-shrink: 0;
        }
        
        &:hover {
          background: var(--bg-hover);
          color: var(--primary);
        }
        
        &.active {
          background: var(--primary-gradient);
          color: white;
          border-color: transparent;
        }
      }
    }
    
    .search-input-row {
      display: flex;
      align-items: center;
      gap: 10px;
      
      svg {
        width: 20px;
        height: 20px;
        color: var(--text-secondary);
        flex-shrink: 0;
      }
      
      input {
        flex: 1;
        border: none;
        background: transparent;
        font-size: 15px;
        outline: none;
        color: var(--text-primary);
        
        &::placeholder {
          color: var(--text-placeholder);
        }
      }
      
      .search-btn {
        flex-shrink: 0;
      }
      
      .clear-btn {
        flex-shrink: 0;
        padding: 6px;
        border-radius: 50%;
        
        &:hover {
          background: var(--bg-hover);
        }
      }
      
      .search-icon {
        transition: all 0.3s;
        
        &.searching {
          animation: spin 1s linear infinite;
        }
      }
    }
  }
  
  .search-history {
    margin-bottom: 16px;
    
    .history-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      
      .history-title {
        font-size: 13px;
        font-weight: 600;
        color: var(--text-secondary);
      }
    }
    
    .history-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      
      .history-item {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 6px 12px;
        background: var(--bg-hover);
        border-radius: 16px;
        font-size: 13px;
        color: var(--text-secondary);
        cursor: pointer;
        transition: all 0.2s;
        
        .el-icon {
          font-size: 14px;
        }
        
        &:hover {
          background: var(--primary-gradient);
          color: #fff;
          transform: translateY(-1px);
        }
      }
    }
  }
  
  .search-results {
    max-height: 400px;
    overflow-y: auto;
    
    .results-header {
      padding: 0 12px 8px;
      border-bottom: 1px solid var(--border-color);
      margin-bottom: 8px;
      
      .results-count {
        font-size: 13px;
        color: var(--text-secondary);
      }
    }
    
    .search-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px;
      border-radius: var(--radius);
      cursor: pointer;
      transition: all 0.2s;
      
      &:hover {
        background: var(--bg-hover);
        transform: translateX(4px);
      }
      
      .item-icon {
        width: 36px;
        height: 36px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        
        svg {
          width: 18px;
          height: 18px;
          color: #fff;
        }
        
        &.contract { background: var(--primary-gradient); }
        &.template { background: var(--gradient-success); }
        &.user { background: var(--gradient-warning); }
      }
      
      .item-info {
        flex: 1;
        min-width: 0;
        
        .item-title {
          font-size: 14px;
          font-weight: 500;
          color: var(--text-primary);
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        .item-meta {
          font-size: 12px;
          color: var(--text-secondary);
          margin-top: 4px;
          display: flex;
          align-items: center;
          gap: 8px;
          
          .type-tag {
            flex-shrink: 0;
          }
          
          .meta-text {
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
      }
    }
  }
  
  .search-empty {
    text-align: center;
    padding: 40px 20px;
    color: var(--text-secondary);
    
    .empty-content {
      .el-icon {
        color: var(--text-placeholder);
        margin-bottom: 16px;
      }
      
      .empty-text {
        font-size: 15px;
        font-weight: 500;
        color: var(--text-secondary);
        margin-bottom: 4px;
      }
      
      .empty-hint {
        font-size: 13px;
        color: var(--text-placeholder);
      }
    }
  }
  
  .search-tips {
    text-align: center;
    padding: 30px;
    color: var(--text-secondary);
    
    p { margin: 0; }
    
    .tips-title {
      font-size: 13px;
      margin-bottom: 12px;
      color: var(--text-secondary);
    }
    
    .tips-list {
      display: flex;
      gap: 10px;
      justify-content: center;
      flex-wrap: wrap;
      
      .quick-tip {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 8px 14px;
        background: var(--bg-hover);
        border-radius: 20px;
        font-size: 13px;
        cursor: pointer;
        transition: all 0.2s;
        color: var(--text-regular);
        
        .el-icon {
          font-size: 14px;
        }
        
        &:hover {
          background: var(--primary-gradient);
          color: #fff;
          transform: translateY(-1px);
          box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
        }
      }
    }
  }
}

/* 通知弹窗 */
.notification-dialog {
  :deep(.el-dialog) {
    border-radius: 16px;
    border: none;
    box-shadow: 0 20px 60px rgba(102, 126, 234, 0.2);
  }
  
  :deep(.el-dialog__header) {
    background: var(--primary-gradient);
    padding: 16px 20px;
    border-bottom: none;
    margin: 0;
    
    .el-dialog__title {
      color: white;
      font-weight: 600;
      font-size: 16px;
    }
    
    .el-dialog__headerbtn {
      .el-icon {
        color: rgba(255, 255, 255, 0.8);
        
        &:hover {
          color: white;
        }
      }
    }
  }
  
  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.notification-panel {
  .notification-settings {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    border-bottom: 1px solid var(--border-color);
    
    .notification-tabs {
      display: flex;
      gap: 16px;
      
      .tab {
        font-size: 13px;
        color: var(--text-secondary);
        cursor: pointer;
        padding-bottom: 8px;
        border-bottom: 2px solid transparent;
        transition: all 0.2s;
        display: flex;
        align-items: center;
        gap: 4px;
        
        &.active {
          color: var(--primary);
          border-bottom-color: var(--primary);
          font-weight: 500;
        }
        
        .el-icon {
          font-size: 12px;
        }
      }
    }
    
    .settings-right {
      display: flex;
      gap: 8px;
      align-items: center;
      
      .sound-toggle, .settings-btn {
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: var(--radius);
        cursor: pointer;
        color: var(--text-secondary);
        transition: all 0.2s;
        
        &:hover {
          background: var(--bg-hover);
          color: var(--primary);
        }
      }
    }
  }
  
  .notification-list {
    max-height: 400px;
    overflow-y: auto;
    position: relative;
    
    .notification-item {
      display: flex;
      gap: 12px;
      padding: 14px 16px;
      padding-left: 20px;
      border-bottom: 1px solid var(--border-color);
      transition: background 0.2s;
      position: relative;
      cursor: pointer;
      
      &:hover {
        background: var(--bg-hover);
        
        .notif-actions {
          opacity: 1;
        }
      }
      
      &.unread {
        background: rgba(102, 126, 234, 0.05);
      }
      
      .notif-indicator {
        position: absolute;
        left: 0;
        top: 0;
        bottom: 0;
        width: 4px;
        border-radius: 0 2px 2px 0;
        
        &.important { background: #ef4444; }
        &.normal { background: var(--primary); }
        &.system { background: #8b5cf6; }
      }
      
      .notif-icon {
        width: 38px;
        height: 38px;
        border-radius: var(--radius);
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        
        svg {
          width: 18px;
          height: 18px;
          color: #fff;
        }
        
        &.approval { background: var(--primary-gradient); }
        &.reminder { background: var(--gradient-danger); }
        &.system { background: var(--gradient-info); }
      }
      
      .notif-content {
        flex: 1;
        min-width: 0;
        
        .notif-header {
          display: flex;
          align-items: center;
          gap: 8px;
          
          .notif-title {
            font-size: 14px;
            font-weight: 500;
            color: var(--text-primary);
          }
          
          .unread-dot {
            width: 8px;
            height: 8px;
            background: var(--primary);
            border-radius: 50%;
            flex-shrink: 0;
          }
        }
        
        .notif-desc {
          font-size: 13px;
          color: var(--text-regular);
          margin-top: 4px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
        
        .notif-time {
          font-size: 12px;
          color: var(--text-secondary);
          margin-top: 6px;
        }
      }
      
      .notif-actions {
        display: flex;
        align-items: flex-start;
        opacity: 0;
        transition: opacity 0.2s;
        
        .delete-btn {
          width: 28px;
          height: 28px;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: var(--radius);
          color: var(--text-secondary);
          cursor: pointer;
          transition: all 0.2s;
          
          &:hover {
            background: rgba(239, 68, 68, 0.1);
            color: #ef4444;
          }
        }
      }
      
      .swipe-delete-hint {
        position: absolute;
        right: -80px;
        top: 50%;
        transform: translateY(-50%);
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 4px;
        color: #ef4444;
        font-size: 12px;
        opacity: 0;
        transition: opacity 0.2s;
        
        &.show {
          opacity: 1;
        }
      }
    }
    
    .notification-empty {
      padding: 50px 20px;
      text-align: center;
      color: var(--text-secondary);
      
      svg {
        margin-bottom: 12px;
        opacity: 0.5;
      }
      
      p {
        margin: 0;
        font-size: 14px;
      }
    }
  }
  
  .notification-footer {
    display: flex;
    justify-content: space-between;
    padding: 12px 16px;
    border-top: 1px solid var(--border-color);
    
    span {
      font-size: 13px;
      color: var(--primary);
      cursor: pointer;
      
      &:hover {
        text-decoration: underline;
      }
    }
  }
}

/* 右键菜单 */
.context-menu {
  position: fixed;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  padding: 6px 0;
  z-index: 9999;
  min-width: 160px;
  
  .context-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 16px;
    font-size: 13px;
    color: var(--text-primary);
    cursor: pointer;
    transition: background 0.15s;
    
    &:hover {
      background: var(--bg-hover);
    }
    
    &.danger {
      color: #ef4444;
      
      &:hover {
        background: rgba(239, 68, 68, 0.1);
      }
    }
    
    svg {
      width: 16px;
      height: 16px;
      flex-shrink: 0;
    }
  }
}

.context-overlay {
  position: fixed;
  inset: 0;
  z-index: 9998;
}

/* ── Responsive Design ─────────────────────────────── */
@media (max-width: 1200px) {
  .sidebar {
    width: 200px;
  }
}

@media (max-width: 992px) {
  .sidebar {
    width: 180px;
  }
  
  .sidebar .logo .logo-text {
    font-size: 16px;
  }
  
  .sidebar .el-menu-item {
    font-size: 13px;
  }
  
  .sidebar .el-menu-item .menu-title {
    margin-left: 6px;
  }
}

@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    height: 100vh;
    z-index: 1000;
    transform: translateX(-100%);
    
    &.open {
      transform: translateX(0);
    }
  }
  
  .main-container {
    margin-left: 0;
  }
  
  .navbar .toggle-btn {
    display: block;
  }
}

/* 侧边栏收起时子菜单样式修复 */
.sidebar.collapsed {
  :deep(.el-menu--horizontal),
  :deep(.el-menu--popup),
  :deep(.el-popper.is-light),
  :deep(.el-menu--popup-container) {
    min-width: 180px !important;
    background: linear-gradient(180deg, #667eea 0%, #764ba2 100%) !important;
    border: none !important;
    box-shadow: 4px 4px 16px rgba(102, 126, 234, 0.3) !important;
    padding: 8px 0 !important;
    
    .el-menu-item {
      color: rgba(255, 255, 255, 0.9) !important;
      background: transparent !important;
      border-radius: 8px;
      margin: 4px 8px;
      padding: 10px 16px !important;
      height: auto !important;
      min-height: 44px;
      line-height: 1.5;
      
      .el-icon {
        color: rgba(255, 255, 255, 0.8) !important;
        font-size: 18px;
      }
      
      span:not(.el-menu-item__content) {
        color: rgba(255, 255, 255, 0.9) !important;
      }
      
      &:hover {
        background: rgba(255, 255, 255, 0.15) !important;
        color: #fff !important;
        
        .el-icon {
          color: #fff !important;
        }
      }
      
      &.is-active {
        background: rgba(255, 255, 255, 0.25) !important;
        color: #fff !important;
        font-weight: 600;
        
        .el-icon {
          color: #fff !important;
        }
      }
    }
    
    .el-sub-menu__title {
      color: rgba(255, 255, 255, 0.9) !important;
      background: transparent !important;
      
      .el-icon {
        color: rgba(255, 255, 255, 0.8) !important;
      }
      
      &:hover {
        background: rgba(255, 255, 255, 0.15) !important;
        color: #fff !important;
      }
    }
  }
  
  :deep(.el-popper.is-light .el-popper__arrow::before),
  :deep(.el-menu--popup .el-popper__arrow::before) {
    background: #764ba2 !important;
    border-color: #764ba2 !important;
  }
}

/* 全局修复 - Element Plus 子菜单弹窗背景 */
:deep(.el-menu--popup) {
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%) !important;
  
  .el-menu-item {
    color: rgba(255, 255, 255, 0.9) !important;
    background: transparent !important;
    
    &:hover {
      background: rgba(255, 255, 255, 0.15) !important;
      color: #fff !important;
    }
  }
}
</style>
