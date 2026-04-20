<template>
  <div class="app-container" :class="{ dark: isDark }">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="logo">
        <div class="logo-icon">C</div>
        <span v-if="!isCollapsed" class="logo-text">{{ appStore.systemName }}</span>
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
          
          <!-- ==================== 合同管理（一级常用） ==================== -->
          <el-menu-item index="/contracts">
            <el-icon><Document /></el-icon>
            <template #title>
              <span class="menu-title">{{ $t('menu.contracts') }}</span>
            </template>
          </el-menu-item>

          <!-- ==================== 合同协同（子菜单） ==================== -->
          <el-sub-menu index="/contract-workbench">
            <template #title>
              <el-icon><Guide /></el-icon>
              <span>{{ $t('menu.contractCollaboration') }}</span>
            </template>
            <el-menu-item index="/templates">
              <el-icon><FolderOpened /></el-icon>
              <span>{{ $t('menu.templates') }}</span>
            </el-menu-item>
            <el-menu-item index="/clauses" v-if="hasPermission('TEMPLATE_MANAGE')">
              <el-icon><DocumentCopy /></el-icon>
              <span>{{ $t('menu.clauses') }}</span>
            </el-menu-item>
            <el-menu-item index="/approvals" v-if="hasPermission('CONTRACT_APPROVE')">
              <el-icon><Checked /></el-icon>
              <span>{{ $t('menu.approvals') }}</span>
            </el-menu-item>
            <el-menu-item index="/renewals" v-if="hasPermission('RENEWAL_MANAGE')">
              <el-icon><RefreshRight /></el-icon>
              <span>{{ $t('menu.renewals') }}</span>
            </el-menu-item>
            <el-menu-item index="/reminders" v-if="hasPermission('REMINDER_MANAGE')">
              <el-icon><Bell /></el-icon>
              <span>{{ $t('menu.reminders') }}</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- ==================== 数据字典（子菜单） ==================== -->
          <el-sub-menu index="/data-dictionary">
            <template #title>
              <el-icon><Box /></el-icon>
              <span>{{ $t('menu.dataDictionary') }}</span>
            </template>
            <el-menu-item index="/folders" v-if="hasPermission('FOLDER_MANAGE')">
              <el-icon><FolderOpened /></el-icon>
              <span>{{ $t('menu.folderManagement') }}</span>
            </el-menu-item>
            <el-menu-item index="/tags" v-if="hasPermission('TAG_MANAGE')">
              <el-icon><Collection /></el-icon>
              <span>{{ $t('menu.tagManagement') }}</span>
            </el-menu-item>
            <el-menu-item index="/categories" v-if="hasPermission('CATEGORY_MANAGE')">
              <el-icon><Tickets /></el-icon>
              <span>{{ $t('contract.categoryManagement') }}</span>
            </el-menu-item>
            <el-menu-item index="/quick-codes" v-if="hasPermission('QUICK_CODE_MANAGE')">
              <el-icon><Grid /></el-icon>
              <span>{{ $t('quickCode.management') }}</span>
            </el-menu-item>
            <el-menu-item index="/type-field-config" v-if="hasPermission('CATEGORY_MANAGE')">
              <el-icon><Tickets /></el-icon>
              <span>{{ $t('menu.typeFieldConfig') }}</span>
            </el-menu-item>
            <el-menu-item index="/variable-management" v-if="hasPermission('VARIABLE_MANAGE')">
              <el-icon><DocumentCopy /></el-icon>
              <span>{{ $t('menu.variableManagement') }}</span>
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
          </el-sub-menu>

          <!-- ==================== 系统管理（子菜单，仅管理员） ==================== -->
          <el-sub-menu index="/system" v-if="hasRole(['ADMIN'])">
            <template #title>
              <el-icon><Management /></el-icon>
              <span>{{ $t('menu.systemManagement') }}</span>
            </template>
            <el-menu-item index="/users">
              <el-icon><UserIcon /></el-icon>
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
            <el-menu-item index="/settings">
              <el-icon><Tools /></el-icon>
              <span>{{ $t('menu.settings') }}</span>
            </el-menu-item>
            <el-menu-item index="/change-logs">
              <el-icon><Memo /></el-icon>
              <span>{{ $t('contract.changeLog') }}</span>
            </el-menu-item>
            <el-menu-item index="/logs">
              <el-icon><Operation /></el-icon>
              <span>{{ $t('menu.operationLogs') }}</span>
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
                <el-icon><UserIcon /></el-icon>
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
        
        <div class="navbar-right" :class="{ 'theme-switching': disableToolbarTransition }">
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
            <div class="tool-btn locale-btn" @click="toggleLocale" :title="t('settings.language')">
              <el-icon :size="18"><MapLocation /></el-icon>
              <span class="locale-indicator">{{ localeLabel }}</span>
            </div>
            
            <!-- 主题 - 支持亮色/暗色/跟随系统 -->
            <div class="tool-btn" @click="toggleTheme" :title="themeTitle">
              <el-icon v-if="appStore.theme === 'system'" :size="18"><Monitor /></el-icon>
              <el-icon v-else-if="appStore.effectiveTheme === 'dark'" :size="18"><Moon /></el-icon>
              <el-icon v-else :size="18"><Sunny /></el-icon>
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
      :title="t('common.search')"
      width="680px"
      :show-close="true"
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
            <el-input
              v-model="searchKeyword"
              class="search-input"
              :placeholder="searchPlaceholder"
              clearable
              @clear="clearSearch"
              @keydown.up.prevent="focusPrevResult"
              @keydown.down.prevent="focusNextResult"
              @keyup.enter="handleSearchEnter"
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
              <span class="history-type">{{ getHistoryTypeLabel(item.type) }}</span>
              <span class="history-keyword">{{ item.keyword }}</span>
              <el-icon class="history-remove" @click.stop="removeHistoryItem(idx)"><Close /></el-icon>
            </span>
          </div>
        </div>
        
        <div class="search-results" v-if="searchResults.length > 0">
          <div class="results-header">
            <span class="results-count">{{ t('common.totalCount', { count: searchResults.length, item: t('common.searchResults') }) }}</span>
            <span v-if="searchCoverageSummary" class="results-note">
              {{ searchCoverageSummary }}
              <el-button
                v-if="retryableFailedSources.length > 0"
                type="primary"
                link
                class="retry-failed-btn"
                @click="retryFailedSourcesSearch"
              >
                {{ t('common.retryFailedSources') }}
              </el-button>
              <el-tooltip v-if="searchSkipDetails.length > 0" placement="top" effect="dark">
                <template #content>
                  <div v-for="item in searchSkipDetails" :key="item.type">{{ item.label }}：{{ item.reason }}</div>
                </template>
                <span class="results-note-info">ⓘ</span>
              </el-tooltip>
            </span>
          </div>
          <div v-for="group in groupedSearchResults" :key="group.type" class="search-group">
            <div class="group-header">
              <span class="group-title">{{ group.label }}</span>
              <span class="group-count">{{ group.items.length }}</span>
            </div>
            <div 
              v-for="item in group.items" 
              :key="item.id" 
              class="search-item"
              :class="{ active: focusedResultId === item.id }"
              :ref="(el) => setSearchItemRef(item.id, el as HTMLElement | null)"
              @click="goToResult(item)"
            >
              <div class="item-icon" :class="item.type">
                <el-icon v-if="item.type === 'contract'" :size="18"><Document /></el-icon>
                <el-icon v-else-if="item.type === 'template'" :size="18"><FolderOpened /></el-icon>
                <el-icon v-else :size="18"><UserIcon /></el-icon>
              </div>
              <div class="item-info">
                <div class="item-title" v-html="highlightMatch(item.title)"></div>
                <div class="item-meta">
                  <el-tag size="small" type="info" class="type-tag">{{ item.typeLabel }}</el-tag>
                  <span class="meta-text" v-html="highlightMatch(item.meta)"></span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="search-empty" v-else-if="searchKeyword && !searching">
          <div class="empty-content">
            <el-icon :size="56"><Search /></el-icon>
            <p class="empty-text">{{ t('common.none') }}</p>
            <p class="empty-hint">{{ t('common.tryDifferentKeyword') }}</p>
            <p v-if="searchCoverageSummary" class="empty-note">
              {{ searchCoverageSummary }}
              <el-button
                v-if="retryableFailedSources.length > 0"
                type="primary"
                link
                class="retry-failed-btn"
                @click="retryFailedSourcesSearch"
              >
                {{ t('common.retryFailedSources') }}
              </el-button>
              <el-tooltip v-if="searchSkipDetails.length > 0" placement="top" effect="dark">
                <template #content>
                  <div v-for="item in searchSkipDetails" :key="item.type">{{ item.label }}：{{ item.reason }}</div>
                </template>
                <span class="results-note-info">ⓘ</span>
              </el-tooltip>
            </p>
          </div>
        </div>
        
        <div class="search-tips" v-else-if="!searchKeyword && searchHistory.length === 0">
          <div class="tips-title">{{ t('dashboard.quickActions') }}</div>
          <div class="tips-list">
            <span @click="quickSearch(t('contract.statuses.pending'))" class="quick-tip">
              <el-icon><Clock /></el-icon>
              {{ t('contract.statuses.pending') }}
            </span>
            <span @click="quickSearch(t('dashboard.expiringSoon'))" class="quick-tip">
              <el-icon><Bell /></el-icon>
              {{ t('statistics.expiringSoon') }}
            </span>
            <span @click="quickSearch(t('contract.statuses.draft'))" class="quick-tip">
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
            <el-tooltip :content="onlyUnread ? t('reminder.unread') : t('common.all')">
              <span class="sound-toggle" @click="toggleUnreadOnly">
                <el-icon :size="18"><View /></el-icon>
              </span>
            </el-tooltip>
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
        
        <div class="notification-list" @contextmenu.prevent @scroll.passive="handleNotificationScroll">
          <div v-if="notificationLoading && filteredNotifications.length === 0" class="notification-empty">
            <p>{{ t('common.loading') }}</p>
          </div>
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
          <div v-if="notificationLoading && filteredNotifications.length > 0" class="notification-load-more">
            <el-button link type="primary" loading>
              {{ t('common.loading') }}
            </el-button>
          </div>
          <div v-else-if="hasMoreNotifications && filteredNotifications.length > 0" class="notification-load-more">
            <el-button link type="primary" :loading="notificationLoading" @click="loadMoreNotifications">
              {{ loadMoreText }}
            </el-button>
          </div>
        </div>
        
        <div class="notification-footer">
          <span v-if="unreadCount > 0" @click="markAllRead">{{ markAllReadText }}</span>
          <span class="notification-progress" v-if="notificationTotal > 0">{{ notificationProgressText }}</span>
          <span @click="$router.push('/reminders')">{{ openReminderCenterText }}</span>
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
import { pageList } from '@/api/types'
import { getContractList } from '@/api/contract'
import { getTemplateList } from '@/api/template'
import { getUserList } from '@/api/user'
import { clearMyNotifications, deleteNotificationById, getMyNotifications, markAllNotificationRead, markNotificationRead, setNotificationImportant } from '@/api/notification'
import Breadcrumb from '@/components/Breadcrumb.vue'
import MobileNav from '@/components/MobileNav.vue'
import {
  User as UserIcon,
  UserFilled,
  Setting,
  Fold,
  Expand,
  Sunny,
  Moon,
  Search,
  View,
  FullScreen,
  Close,
  ArrowUp,
  Star,
  Collection,
  Timer,
  RefreshRight,
  Tools,
  Operation,
  Grid,
  Memo,
  Guide,
  Tickets,
  Box,
  Management,
  DocumentCopy,
  Document,
  FolderOpened,
  Clock,
  Bell,
  Monitor,
  MapLocation,
  DataAnalysis,
  Checked,
  TrendCharts
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const { locale, t } = useI18n()
const userStore = useUserStore()
const appStore = useAppStore()

const isCollapsed = computed(() => appStore.sidebarCollapsed)
const isDark = computed(() => appStore.isDark)
const localeLabel = computed(() => (locale.value || 'zh').toUpperCase())
const themeTitle = computed(() => {
  if (appStore.theme === 'system') return t('settings.themeSystem')
  return appStore.isDark ? t('settings.themeLight') : t('settings.themeDark')
})
const userInfo = computed(() => userStore.userInfo)

const hasPermission = (permission: string): boolean => {
  return userStore.permissions.includes(permission)
}

const hasRole = (rolesToCheck: string[]): boolean => {
  return userStore.roles.some(role => rolesToCheck.includes(role))
}

const showNotifications = ref(false)
const showSearch = ref(false)
const searchKeyword = ref('')
const searchResults = ref<SearchResultItem[]>([])
const searching = ref(false)
const focusedResultId = ref<number | null>(null)
const notifTab = ref('all')
const searchType = ref('all')
const disableToolbarTransition = ref(false)
const searchInputRef = ref<any>(null)
type SearchHistoryType = 'all' | 'contract' | 'template' | 'user'
type SearchSourceType = 'contract' | 'template' | 'user'
interface SearchHistoryItem {
  keyword: string
  type: SearchHistoryType
}
interface SearchSkipDetail {
  type: SearchSourceType
  label: string
  reason: string
}
type SearchSkipReasonCode = 'noPermission' | 'timeout' | 'sourceError' | 'sessionExpired'
interface SearchResultItem {
  id: number
  bizId?: number
  type: SearchSourceType
  title: string
  meta: string
  typeLabel: string
}
type ContractListRes = Awaited<ReturnType<typeof getContractList>> | null
type TemplateListRes = Awaited<ReturnType<typeof getTemplateList>> | null
type UserListRes = Awaited<ReturnType<typeof getUserList>> | null
const searchHistory = ref<SearchHistoryItem[]>([])
let searchDebounceTimer: number | null = null
const searchItemRefs = ref<Record<number, HTMLElement | null>>({})
let activeSearchAbortController: AbortController | null = null
let themeSwitchTimer: number | null = null
const searchRuntimeSkippedReasons = ref<Partial<Record<SearchSourceType, SearchSkipReasonCode>>>({})
const canSearchContract = computed(() => hasPermission('CONTRACT_MANAGE'))
const canSearchTemplate = computed(() => hasPermission('TEMPLATE_MANAGE'))
const canSearchUser = computed(() => hasPermission('USER_MANAGE'))
const allowedSearchTypeValues = computed<SearchHistoryType[]>(() => {
  const values: SearchHistoryType[] = ['all']
  if (canSearchContract.value) values.push('contract')
  if (canSearchTemplate.value) values.push('template')
  if (canSearchUser.value) values.push('user')
  return values
})

// 搜索类型配置
const searchTypes = computed(() => {
  const options = [{ value: 'all', label: t('common.all'), icon: Search }]
  if (canSearchContract.value) {
    options.push({ value: 'contract', label: t('menu.contracts'), icon: Document })
  }
  if (canSearchTemplate.value) {
    options.push({ value: 'template', label: t('menu.templates'), icon: FolderOpened })
  }
  if (canSearchUser.value) {
    options.push({ value: 'user', label: t('menu.usersShort'), icon: UserIcon })
  }
  return options
})

const groupedSearchResults = computed(() => {
  const groups = [
    { type: 'contract' as const, label: t('menu.contracts'), items: [] as SearchResultItem[] },
    { type: 'template' as const, label: t('menu.templates'), items: [] as SearchResultItem[] },
    { type: 'user' as const, label: t('menu.usersShort'), items: [] as SearchResultItem[] }
  ]
  for (const item of searchResults.value) {
    const g = groups.find(group => group.type === item.type)
    if (g) g.items.push(item)
  }
  return groups.filter(group => group.items.length > 0)
})
const getSearchSourceLabel = (type: SearchSourceType) => {
  if (type === 'contract') return t('menu.contracts')
  if (type === 'template') return t('menu.templates')
  return t('menu.usersShort')
}
const resolveSearchFailureReasonCode = (error: unknown): SearchSkipReasonCode => {
  const err = error as { response?: { status?: number }; code?: string; message?: string }
  const status = err?.response?.status
  const code = err?.code
  const message = String(err?.message || '').toLowerCase()
  if (status === 401) return 'sessionExpired'
  if (status === 403) return 'noPermission'
  if (code === 'ECONNABORTED' || message.includes('timeout')) return 'timeout'
  return 'sourceError'
}
const resolveSearchFailureReasonText = (reasonCode: SearchSkipReasonCode) => {
  if (reasonCode === 'sessionExpired') return t('error.sessionExpired')
  if (reasonCode === 'noPermission') return t('common.searchSkippedNoPermission')
  if (reasonCode === 'timeout') return t('common.searchSkippedTimeout')
  return t('common.searchSkippedSourceError')
}
const searchSkipDetails = computed(() => {
  if (searchType.value !== 'all') return [] as SearchSkipDetail[]
  const details: SearchSkipDetail[] = []
  if (!canSearchContract.value) {
    details.push({ type: 'contract', label: t('menu.contracts'), reason: t('common.searchSkippedNoPermission') })
  }
  if (!canSearchTemplate.value) {
    details.push({ type: 'template', label: t('menu.templates'), reason: t('common.searchSkippedNoPermission') })
  }
  if (!canSearchUser.value) {
    details.push({ type: 'user', label: t('menu.usersShort'), reason: t('common.searchSkippedNoPermission') })
  }
  (Object.entries(searchRuntimeSkippedReasons.value) as Array<[SearchSourceType, SearchSkipReasonCode | undefined]>).forEach(([type, reason]) => {
    if (!reason) return
    if (!details.some(item => item.type === type)) {
      details.push({ type, label: getSearchSourceLabel(type), reason: resolveSearchFailureReasonText(reason) })
    }
  })
  return details
})
const retryableFailedSources = computed(() => {
  const retryableCodes: SearchSkipReasonCode[] = ['timeout', 'sourceError']
  return (Object.entries(searchRuntimeSkippedReasons.value) as Array<[SearchSourceType, SearchSkipReasonCode | undefined]>)
    .filter(([, reason]) => !!reason && retryableCodes.includes(reason))
    .map(([type]) => type)
})
const searchCoverageSummary = computed(() => {
  if (searchType.value !== 'all') return ''
  const total = 3
  const searched = total - searchSkipDetails.value.length
  return t('common.searchCoverageSummary', { searched, total })
})

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
      searchInputRef.value?.focus?.()
    }, 100)
  } else {
    searchKeyword.value = ''
    searchResults.value = []
    focusedResultId.value = null
    searchItemRefs.value = {}
  }
})

watch(searchType, () => {
  if (searchKeyword.value.trim().length >= 2) {
    triggerSearchDebounced()
  } else {
    searchResults.value = []
    focusedResultId.value = null
  }
})

watch(allowedSearchTypeValues, (values) => {
  if (!values.includes(searchType.value as SearchHistoryType)) {
    searchType.value = 'all'
  }
})

watch(searchKeyword, (value) => {
  const keyword = value.trim()
  if (keyword.length < 2) {
    searchResults.value = []
    focusedResultId.value = null
    if (searchDebounceTimer) {
      window.clearTimeout(searchDebounceTimer)
      searchDebounceTimer = null
    }
    return
  }
  triggerSearchDebounced()
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
      const parsed = JSON.parse(saved)
      if (Array.isArray(parsed)) {
        searchHistory.value = parsed
          .map((item: any) => {
            if (typeof item === 'string') {
              return { keyword: item, type: 'all' as SearchHistoryType }
            }
            if (item && typeof item.keyword === 'string') {
              const validType: SearchHistoryType = ['all', 'contract', 'template', 'user'].includes(item.type)
                ? item.type
                : 'all'
              return { keyword: item.keyword, type: validType }
            }
            return null
          })
          .filter(Boolean) as SearchHistoryItem[]
      }
    }
  } catch (e) {
    console.error('Failed to load search history:', e)
  }
}

// 保存搜索历史
const saveSearchHistory = (keyword: string, type: SearchHistoryType) => {
  if (!keyword.trim()) return
  
  // 移除已存在的相同关键词+类型
  searchHistory.value = searchHistory.value.filter(item => !(item.keyword === keyword && item.type === type))
  
  // 添加到开头
  searchHistory.value.unshift({ keyword, type })
  
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
  focusedResultId.value = null
  searchInputRef.value?.focus?.()
  if (searchDebounceTimer) {
    window.clearTimeout(searchDebounceTimer)
    searchDebounceTimer = null
  }
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
const searchFromHistory = (item: SearchHistoryItem) => {
  searchType.value = allowedSearchTypeValues.value.includes(item.type) ? item.type : 'all'
  searchKeyword.value = item.keyword
  doSearch()
}

const removeHistoryItem = (idx: number) => {
  searchHistory.value.splice(idx, 1)
  try {
    localStorage.setItem('contract-search-history', JSON.stringify(searchHistory.value))
  } catch (e) {
    console.error('Failed to remove search history item:', e)
  }
}

const getHistoryTypeLabel = (type: SearchHistoryType) => {
  if (type === 'contract') return t('menu.contracts')
  if (type === 'template') return t('menu.templates')
  if (type === 'user') return t('menu.usersShort')
  return t('common.all')
}

const triggerSearchDebounced = () => {
  if (searchDebounceTimer) {
    window.clearTimeout(searchDebounceTimer)
  }
  searchDebounceTimer = window.setTimeout(() => {
    doSearch()
  }, 350)
}

const escapeRegExp = (value: string) => value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
const escapeHtml = (value: string) =>
  value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')

const highlightMatch = (raw: string) => {
  const text = String(raw ?? '')
  const keyword = searchKeyword.value.trim()
  const escaped = escapeHtml(text)
  if (!keyword) return escaped
  const regex = new RegExp(`(${escapeRegExp(keyword)})`, 'ig')
  return escaped.replace(regex, '<mark>$1</mark>')
}

const focusPrevResult = () => {
  if (!searchResults.value.length) return
  const currentIndex = searchResults.value.findIndex(item => item.id === focusedResultId.value)
  const prevIndex = currentIndex <= 0 ? searchResults.value.length - 1 : currentIndex - 1
  focusedResultId.value = searchResults.value[prevIndex].id
  scrollFocusedResultIntoView()
}

const focusNextResult = () => {
  if (!searchResults.value.length) return
  const currentIndex = searchResults.value.findIndex(item => item.id === focusedResultId.value)
  const nextIndex = currentIndex < 0 || currentIndex >= searchResults.value.length - 1 ? 0 : currentIndex + 1
  focusedResultId.value = searchResults.value[nextIndex].id
  scrollFocusedResultIntoView()
}

const setSearchItemRef = (id: number, el: HTMLElement | null) => {
  if (el) searchItemRefs.value[id] = el
  else delete searchItemRefs.value[id]
}

const scrollFocusedResultIntoView = () => {
  if (focusedResultId.value == null) return
  const node = searchItemRefs.value[focusedResultId.value]
  node?.scrollIntoView({ block: 'nearest', behavior: 'smooth' })
}

const handleSearchEnter = () => {
  const focused = searchResults.value.find(item => item.id === focusedResultId.value)
  if (focused) {
    goToResult(focused)
    return
  }
  doSearch()
}

const buildContractItems = (contractRes: Awaited<ReturnType<typeof getContractList>> | null) =>
  pageList(contractRes).map((c): SearchResultItem => ({
  id: Number(`1${c.id}`),
  bizId: c.id,
  type: 'contract',
  title: c.title || c.contractNo || '-',
  meta: `${t('contract.no')}: ${c.contractNo || '-'}`,
  typeLabel: t('menu.contracts')
}))
const buildTemplateItems = (templateRes: Awaited<ReturnType<typeof getTemplateList>> | null) =>
  pageList(templateRes).map((tpl): SearchResultItem => ({
  id: Number(`2${tpl.id}`),
  bizId: tpl.id,
  type: 'template',
  title: tpl.name || '-',
  meta: `${t('template.category')}: ${tpl.category || '-'}`,
  typeLabel: t('menu.templates')
}))
const buildUserItems = (userRes: Awaited<ReturnType<typeof getUserList>> | null) =>
  pageList(userRes).map((u): SearchResultItem => ({
  id: Number(`3${u.id}`),
  bizId: u.id,
  type: 'user',
  title: u.nickname || u.username || '-',
  meta: `${t('user.role')}: ${u.role || '-'}`,
  typeLabel: t('menu.users')
}))

const executeSearchBySources = async (sources: SearchSourceType[], replaceAll: boolean) => {
  const keyword = searchKeyword.value.trim()
  if (!keyword || sources.length === 0) return

  if (activeSearchAbortController) {
    activeSearchAbortController.abort()
  }
  activeSearchAbortController = new AbortController()
  searching.value = true

  const existingByType: Record<SearchSourceType, SearchResultItem[]> = {
    contract: replaceAll ? [] : searchResults.value.filter(item => item.type === 'contract'),
    template: replaceAll ? [] : searchResults.value.filter(item => item.type === 'template'),
    user: replaceAll ? [] : searchResults.value.filter(item => item.type === 'user')
  }

  try {
    const sourceSet = new Set(sources)
    const [contractTask, templateTask, userTask] = (await Promise.allSettled([
      sourceSet.has('contract')
        ? getContractList({ keyword, page: 1, pageSize: 8 }, { signal: activeSearchAbortController.signal })
        : Promise.resolve<ContractListRes>(null),
      sourceSet.has('template')
        ? getTemplateList({ keyword, page: 1, pageSize: 8 }, { signal: activeSearchAbortController.signal })
        : Promise.resolve<TemplateListRes>(null),
      sourceSet.has('user')
        ? getUserList({ keyword, page: 1, pageSize: 8 }, { signal: activeSearchAbortController.signal })
        : Promise.resolve<UserListRes>(null)
    ])) as [
      PromiseSettledResult<ContractListRes>,
      PromiseSettledResult<TemplateListRes>,
      PromiseSettledResult<UserListRes>
    ]

    const applySettled = <T>(task: PromiseSettledResult<T>, source: SearchSourceType, buildItems: (res: T) => SearchResultItem[]) => {
      if (!sourceSet.has(source)) return
      if (task.status === 'fulfilled') {
        delete searchRuntimeSkippedReasons.value[source]
        existingByType[source] = buildItems(task.value)
        return
      }
      const error = task.reason as { name?: string; response?: { status?: number }; code?: string; message?: string }
      if (error?.name === 'CanceledError' || error?.name === 'AbortError') return
      searchRuntimeSkippedReasons.value[source] = resolveSearchFailureReasonCode(error)
      if (replaceAll) existingByType[source] = []
    }

    applySettled<ContractListRes>(contractTask, 'contract', buildContractItems)
    applySettled<TemplateListRes>(templateTask, 'template', buildTemplateItems)
    applySettled<UserListRes>(userTask, 'user', buildUserItems)

    searchResults.value = [...existingByType.contract, ...existingByType.template, ...existingByType.user]
    focusedResultId.value = searchResults.value.length > 0 ? searchResults.value[0].id : null
    setTimeout(() => scrollFocusedResultIntoView(), 0)
  } catch (error: unknown) {
    const err = error as { name?: string }
    if (err?.name !== 'CanceledError' && err?.name !== 'AbortError') {
      throw error
    }
  } finally {
    activeSearchAbortController = null
    searching.value = false
  }
}

const doSearch = async () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword || keyword.length < 2) return
  searchRuntimeSkippedReasons.value = {}

  if (searchType.value !== 'all' && !allowedSearchTypeValues.value.includes(searchType.value as SearchHistoryType)) {
    searchType.value = 'all'
  }

  const shouldSearchContract = canSearchContract.value && (searchType.value === 'all' || searchType.value === 'contract')
  const shouldSearchTemplate = canSearchTemplate.value && (searchType.value === 'all' || searchType.value === 'template')
  const shouldSearchUser = canSearchUser.value && (searchType.value === 'all' || searchType.value === 'user')

  if (!shouldSearchContract && !shouldSearchTemplate && !shouldSearchUser) {
    searchResults.value = []
    focusedResultId.value = null
    return
  }
  
  saveSearchHistory(keyword, searchType.value as SearchHistoryType)
  const sources: SearchSourceType[] = []
  if (shouldSearchContract) sources.push('contract')
  if (shouldSearchTemplate) sources.push('template')
  if (shouldSearchUser) sources.push('user')
  await executeSearchBySources(sources, true)
}

const retryFailedSourcesSearch = async () => {
  if (retryableFailedSources.value.length === 0) return
  await executeSearchBySources(retryableFailedSources.value, false)
}

const quickSearch = (keyword: string) => {
  searchKeyword.value = keyword
  doSearch()
}

const goToResult = (item: SearchResultItem) => {
  showSearch.value = false
  if (item.type === 'contract') {
    router.push(`/contracts/${item.bizId ?? item.id}`)
  } else if (item.type === 'template') {
    router.push(`/templates/${item.bizId ?? item.id}`)
  } else if (item.type === 'user') {
    router.push('/users')
  }
}

// 通知列表 - 增强版
interface Notification {
  id: string | number
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
const notificationLoading = ref(false)
const notificationPage = ref(1)
const notificationPageSize = 20
const notificationTotal = ref(0)
const notificationUnreadCount = ref(0)
const onlyUnread = ref(false)
const hasMoreNotifications = computed(() => notifications.value.length < notificationTotal.value)
const loadMoreText = computed(() => locale.value === 'en' ? 'Load More' : '加载更多')
const openReminderCenterText = computed(() => locale.value === 'en' ? 'Open Reminder Center' : '前往提醒中心')
const markAllReadText = computed(() => locale.value === 'en' ? 'Mark All as Read' : '全部标记已读')
const notificationProgressText = computed(() =>
  locale.value === 'en'
    ? `Loaded ${notifications.value.length} / ${notificationTotal.value}`
    : `已加载 ${notifications.value.length} / 总 ${notificationTotal.value}`
)

const loadNotificationHistory = async (loadMore = false) => {
  notificationLoading.value = true
  try {
    if (!loadMore) {
      notificationPage.value = 1
    }
    const res = await getMyNotifications({
      page: notificationPage.value,
      pageSize: notificationPageSize,
      category: notifTab.value === 'approval' ? 'approval' : (notifTab.value === 'system' ? 'system' : undefined),
      unreadOnly: onlyUnread.value || undefined,
      importantOnly: notifTab.value === 'important' ? true : undefined
    })
    const list = Array.isArray(res?.data?.list) ? res.data.list : []
    const mapped = list.map((item: any) => ({
      id: item.id,
      type: getUiNotificationType(item.type),
      priority: item.type === 'approval_request' ? 'important' : 'normal',
      title: item.title || t('common.notification'),
      desc: item.content || '',
      time: item.createdAt ? new Date(item.createdAt.replace(' ', 'T')) : new Date(),
      unread: !item.isRead,
      important: Boolean(item.isImportant),
      link: item?.data?.contractId ? `/contracts/${item.data.contractId}` : undefined
    }))
    if (loadMore) {
      notifications.value = [...notifications.value, ...mapped]
    } else {
      notifications.value = mapped
    }
    notificationTotal.value = Number(res?.data?.total || 0)
    notificationUnreadCount.value = Number(res?.data?.unreadCount || 0)
  } catch {
    // ignore load failures to avoid blocking layout render
  } finally {
    notificationLoading.value = false
  }
}

const loadMoreNotifications = () => {
  if (!hasMoreNotifications.value || notificationLoading.value) return
  notificationPage.value += 1
  loadNotificationHistory(true)
}

const getUiNotificationType = (rawType?: string): Notification['type'] => {
  if (!rawType) return 'system'
  if (['approval_request', 'approval_result'].includes(rawType)) return 'approval'
  if (['renewal_request', 'renewal_result'].includes(rawType)) return 'renewal'
  if (rawType === 'reminder') return 'reminder'
  return 'system'
}

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
  loadNotificationHistory()
})


const unreadCount = computed(() => notificationUnreadCount.value)

// WebSocket 连接状态
const wsConnected = computed(() => webSocketService.connected.value)

const filteredNotifications = computed(() => {
  if (notifTab.value === 'all') return notifications.value
  if (notifTab.value === 'important') return notifications.value.filter(n => n.important)
  if (notifTab.value === 'approval') return notifications.value.filter(n => n.type === 'approval')
  if (notifTab.value === 'system') return notifications.value.filter(n => n.type === 'system')
  return notifications.value
})

watch(notifTab, () => {
  loadNotificationHistory()
})

const toggleUnreadOnly = () => {
  onlyUnread.value = !onlyUnread.value
  loadNotificationHistory()
}

const handleNotificationScroll = (event: Event) => {
  const target = event.target as HTMLElement
  if (!target || notificationLoading.value || !hasMoreNotifications.value) return
  const distanceToBottom = target.scrollHeight - target.scrollTop - target.clientHeight
  if (distanceToBottom <= 24) {
    loadMoreNotifications()
  }
}

const markAllRead = () => {
  notifications.value.forEach(n => n.unread = false)
  notificationUnreadCount.value = 0
  markAllNotificationRead().catch(() => undefined)
}

const deleteNotification = async (id?: string | number) => {
  if (id) {
    const target = notifications.value.find(n => n.id === id)
    notifications.value = notifications.value.filter(n => n.id !== id)
    if (typeof id === 'number') {
      await deleteNotificationById(id).catch(() => undefined)
    }
    notificationTotal.value = Math.max(0, notificationTotal.value - 1)
    if (target?.unread) {
      notificationUnreadCount.value = Math.max(0, notificationUnreadCount.value - 1)
    }
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

  const formatEnglishRelative = (value: number, unit: 'minute' | 'hour' | 'day') => {
    const suffix = value === 1 ? unit : `${unit}s`
    return `${value} ${suffix} ago`
  }
  
  if (minutes < 1) return t('common.justNow')
  if (minutes < 60) {
    return locale.value === 'en'
      ? formatEnglishRelative(minutes, 'minute')
      : t('common.minutesAgo', { count: minutes })
  }
  if (hours < 24) {
    return locale.value === 'en'
      ? formatEnglishRelative(hours, 'hour')
      : t('common.hoursAgo', { count: hours })
  }
  if (days < 7) {
    return locale.value === 'en'
      ? formatEnglishRelative(days, 'day')
      : t('common.daysAgo', { count: days })
  }
  return date.toLocaleDateString()
}

// 声音开关
const notificationSoundEnabled = ref(localStorage.getItem('notificationSound') !== 'false')

const toggleSound = () => {
  notificationSoundEnabled.value = !notificationSoundEnabled.value
  localStorage.setItem('notificationSound', String(notificationSoundEnabled.value))
  webSocketService.setSoundEnabled(notificationSoundEnabled.value)
}

// 点击通知
const handleNotificationClick = (notif: Notification) => {
  if (notif.unread) {
    notif.unread = false
    notificationUnreadCount.value = Math.max(0, notificationUnreadCount.value - 1)
  }
  if (typeof notif.id === 'number') {
    markNotificationRead(notif.id).catch(() => undefined)
  }
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
  if (notif && notif.unread) {
    notif.unread = false
    notificationUnreadCount.value = Math.max(0, notificationUnreadCount.value - 1)
  }
  if (notif && typeof notif.id === 'number') {
    markNotificationRead(notif.id).catch(() => undefined)
  }
  closeContextMenu()
}

const toggleImportant = (notif: Notification | null) => {
  if (notif) {
    notif.important = !notif.important
    if (typeof notif.id === 'number') {
      setNotificationImportant(notif.id, notif.important).catch(() => undefined)
    }
  }
  closeContextMenu()
}

// 滑动删除
const touchStartX = ref(0)
const touchStartY = ref(0)
const touchEndX = ref(0)
const touchEndY = ref(0)
const swipingNotifId = ref<string | number | null>(null)

const handleTouchStart = (event: TouchEvent, notif: Notification) => {
  touchStartX.value = event.touches[0].clientX
  touchStartY.value = event.touches[0].clientY
  touchEndX.value = touchStartX.value
  touchEndY.value = touchStartY.value
  swipingNotifId.value = notif.id
}

const handleTouchMove = (event: TouchEvent) => {
  const diffX = touchStartX.value - event.touches[0].clientX
  const diffY = Math.abs(touchStartY.value - event.touches[0].clientY)
  touchEndX.value = event.touches[0].clientX
  touchEndY.value = event.touches[0].clientY
  
  // 只处理水平滑动
  if (diffY < 10 && diffX > 50 && swipingNotifId.value) {
    event.preventDefault()
  }
}

const handleTouchEnd = (notif: Notification) => {
  const diffX = touchStartX.value - touchEndX.value
  const diffY = Math.abs(touchStartY.value - touchEndY.value)
  if (swipingNotifId.value === notif.id && diffY < 16 && diffX > 100) {
    deleteNotification(notif.id)
  }
  swipingNotifId.value = null
}

// 通知设置
const handleNotificationSettings = (command: string) => {
  if (command === 'markAll') {
    markAllRead()
  } else if (command === 'clearAll') {
    notifications.value = []
    notificationTotal.value = 0
    notificationUnreadCount.value = 0
    clearMyNotifications().catch(() => undefined)
  } else if (command === 'settings') {
    router.push('/settings')
    showNotifications.value = false
  }
}

// 启动：搜索历史、通知、WebSocket（失败则静默）
onMounted(() => {
  loadSearchHistory()
  loadNotificationHistory()
  mergeWebSocketNotifications()
  webSocketService.setSoundEnabled(notificationSoundEnabled.value)
  if (userStore.isLoggedIn && userStore.userInfo?.id) {
    try {
      webSocketService.connect(userStore.userInfo.id)
    } catch {
      /* ignore */
    }
  }
})

onUnmounted(() => {
  webSocketService.disconnect()
  activeSearchAbortController?.abort()
  if (searchDebounceTimer) {
    window.clearTimeout(searchDebounceTimer)
    searchDebounceTimer = null
  }
  if (themeSwitchTimer) {
    window.clearTimeout(themeSwitchTimer)
    themeSwitchTimer = null
  }
})

// 访问过的页面标签
const visitedViews = ref([
  { path: '/dashboard', title: 'menu.dashboard' },
  { path: '/contracts', title: 'menu.contracts' }
])

const activeMenu = computed(() => route.path)

const toggleSidebar = () => appStore.toggleSidebar()
const toggleTheme = () => {
  disableToolbarTransition.value = true
  if (themeSwitchTimer) {
    window.clearTimeout(themeSwitchTimer)
  }
  appStore.toggleTheme()
  themeSwitchTimer = window.setTimeout(() => {
    disableToolbarTransition.value = false
    themeSwitchTimer = null
  }, 160)
}

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
      title: route.meta.title as string || 'common.untitled'
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
        min-width: 0;
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

    :deep(.el-sub-menu__title) {
      min-height: 48px;
      height: auto;
      line-height: 20px;
      padding-top: 10px !important;
      padding-bottom: 10px !important;

      span {
        min-width: 0;
        max-width: calc(100% - 36px);
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
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
      background: var(--bg-hover);
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s;
      
      &:hover {
        background: var(--primary);
        color: white;
      }
    }
  }
  
  .navbar-right {
    display: flex;
    align-items: center;
    gap: 8px;

    &.theme-switching {
      .tool-group,
      .tool-btn,
      .tool-btn svg,
      .ws-status,
      .ws-status .ws-dot,
      .ws-status .ws-text,
      .locale-btn .locale-indicator {
        transition: none !important;
        animation: none !important;
      }
    }
    
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
        color: var(--text-secondary);
        transition: all 0.3s ease;
      }
      
      &:hover {
        background: linear-gradient(135deg, rgba(102, 126, 234, 0.12) 0%, rgba(118, 75, 162, 0.12) 100%);
        
        svg {
          color: var(--primary);
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
          background: var(--primary);
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

    .locale-btn {
      gap: 4px;
      width: 64px;
      min-width: 64px;
      padding: 0 8px;
      justify-content: center;

      .locale-indicator {
        font-size: 10px;
        font-weight: 700;
        color: var(--text-secondary);
        letter-spacing: 0.3px;
      }

      &:hover .locale-indicator {
        color: var(--primary);
      }
    }

    .ws-status {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      height: 36px;
      min-width: 108px;
      padding: 0 12px;
      border-radius: 18px;
      box-sizing: border-box;
      background: rgba(148, 163, 184, 0.16);
      color: var(--text-secondary);
      font-size: 12px;
      line-height: 1;
      transition: background-color 0.2s ease, color 0.2s ease;

      .ws-dot {
        width: 10px;
        height: 10px;
        flex-shrink: 0;
        border-radius: 50%;
        background: #f87171;
        transition: background-color 0.2s ease, box-shadow 0.2s ease;
      }

      .ws-text {
        font-weight: 600;
        white-space: nowrap;
      }

      &.connected {
        background: rgba(34, 197, 94, 0.14);
        color: #16a34a;

        .ws-dot {
          background: #22c55e;
          box-shadow: 0 0 8px rgba(34, 197, 94, 0.35);
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
    background: var(--bg-card);
    border-bottom: 1px solid var(--border-color);
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
      background: var(--border-color);
      border-radius: 4px;
      
      &:hover {
        background: var(--text-placeholder);
      }
    }
  }
}

/* ── Tags Bar ──────────────────────────────────────── */
.tags-bar {
  height: 44px;
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-color);
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
    background: var(--border-color);
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
      background: rgba(255, 255, 255, 0.1);
      color: var(--text-secondary);
      
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

    .locale-btn .locale-indicator {
      color: var(--text-secondary);
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
      max-width: 240px;
      overflow: hidden;
      text-overflow: ellipsis;
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
    padding: 10px 16px;
    margin: 0;
    border-bottom: 1px solid rgba(255, 255, 255, 0.18);
    
    .el-dialog__title {
      color: white;
      font-weight: 600;
      font-size: 14px;
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
      
      :deep(.search-input) {
        flex: 1;
      }

      :deep(.search-input .el-input__wrapper) {
        box-shadow: none !important;
        background: transparent !important;
        padding: 0;
      }

      :deep(.search-input .el-input__inner) {
        font-size: 15px;
        color: var(--text-primary);
      }

      :deep(.search-input .el-input__inner::placeholder) {
        color: var(--text-placeholder);
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

        .history-type {
          padding: 2px 6px;
          border-radius: 10px;
          background: rgba(102, 126, 234, 0.15);
          font-size: 11px;
          line-height: 1;
        }

        .history-keyword {
          max-width: 140px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .history-remove {
          opacity: 0.75;
          border-radius: 50%;
          padding: 1px;

          &:hover {
            opacity: 1;
            background: rgba(239, 68, 68, 0.18);
          }
        }
      }
    }
  }
  
  .search-results {
    max-height: 400px;
    overflow-y: auto;

    .search-group {
      .group-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 8px 12px;
        margin-top: 4px;
        color: var(--text-secondary);
        font-size: 12px;
        font-weight: 600;
      }

      .group-count {
        opacity: 0.75;
      }
    }
    
    .results-header {
      padding: 0 12px 8px;
      border-bottom: 1px solid var(--border-color);
      margin-bottom: 8px;
      display: flex;
      flex-direction: column;
      gap: 4px;
      
      .results-count {
        font-size: 13px;
        color: var(--text-secondary);
      }

      .results-note {
        font-size: 12px;
        color: var(--text-placeholder);
        display: inline-flex;
        align-items: center;
        gap: 4px;
      }

      .results-note-info {
        cursor: help;
        opacity: 0.8;
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

      &.active {
        background: var(--bg-hover);
        border: 1px solid var(--primary);
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

    :deep(mark) {
      background: rgba(245, 158, 11, 0.25);
      color: inherit;
      border-radius: 4px;
      padding: 0 2px;
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

      .empty-note {
        margin-top: 10px;
        font-size: 12px;
        color: var(--text-placeholder);
        display: inline-flex;
        align-items: center;
        gap: 4px;
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

    .notification-load-more {
      padding: 10px 0 14px;
      display: flex;
      justify-content: center;
    }
  }
  
  .notification-footer {
    display: flex;
    justify-content: space-between;
    padding: 12px 16px;
    border-top: 1px solid var(--border-color);
    gap: 8px;
    
    span {
      font-size: 13px;
      color: var(--primary);
      cursor: pointer;
      
      &:hover {
        text-decoration: underline;
      }
    }

    .notification-progress {
      color: var(--text-secondary);
      cursor: default;
      flex: 1;
      text-align: center;

      &:hover {
        text-decoration: none;
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
