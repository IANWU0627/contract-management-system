<template>
  <div class="dashboard">
    <h1 class="page-title">{{ t('menu.dashboard') }}</h1>
    
    <!-- 第一行：统计卡片 -->
    <div class="stats-grid">
      <template v-if="loading">
        <div v-for="i in 4" :key="i" class="stat-card-skeleton">
          <SkeletonLoader type="stat" />
        </div>
      </template>
      <template v-else>
        <div class="stat-card gradient-purple" @click="$router.push('/contracts')">
          <div class="stat-bg-icon">
            <el-icon :size="50"><Document /></el-icon>
          </div>
          <div class="stat-icon">
            <el-icon :size="16"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalContracts }}</div>
            <div class="stat-label">{{ t('statistics.totalContracts') }}</div>
          </div>
          <div class="stat-meta">
            <span>{{ t('contract.statuses.signed') }} {{ statsMeta.signedCount }}</span>
            <span>{{ t('contract.statuses.archived') }} {{ statsMeta.archivedCount }}</span>
          </div>
        </div>
        
        <div class="stat-card gradient-green" @click="$router.push('/contracts')">
          <div class="stat-bg-icon">
            <el-icon :size="50"><Money /></el-icon>
          </div>
          <div class="stat-icon">
            <el-icon :size="16"><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ getCurrencySymbol(DEFAULT_CURRENCY) }}{{ formatAmount(stats.totalAmount) }}</div>
            <div class="stat-label">{{ t('statistics.totalAmount') }}</div>
          </div>
          <div class="stat-meta">
            <span>{{ t('contract.statuses.signed') }} {{ getCurrencySymbol(DEFAULT_CURRENCY) }}{{ formatAmount(statsMeta.signedAmount) }}</span>
            <span>{{ t('statistics.totalContracts') }} {{ stats.totalContracts }}</span>
          </div>
        </div>
        
        <div class="stat-card gradient-orange" @click="$router.push('/approvals')">
          <div class="stat-bg-icon">
            <el-icon :size="50"><Timer /></el-icon>
          </div>
          <div class="stat-icon">
            <el-icon :size="16"><Timer /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingApproval }}</div>
            <div class="stat-label">{{ t('statistics.pendingApproval') }}</div>
          </div>
          <div class="stat-meta">
            <span>{{ t('contract.statuses.draft') }} {{ statsMeta.draftCount }}</span>
            <span>{{ t('contract.statuses.approving') }} {{ statsMeta.approvingCount }}</span>
          </div>
          <div class="stat-badge warning" v-if="stats.pendingApproval > 0">{{ stats.pendingApproval }}</div>
        </div>
        
        <div class="stat-card gradient-red" @click="$router.push('/reminders')">
          <div class="stat-bg-icon">
            <el-icon :size="50"><Bell /></el-icon>
          </div>
          <div class="stat-icon">
            <el-icon :size="16"><Bell /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.expiringSoon }}</div>
            <div class="stat-label">{{ t('statistics.expiringSoon') }}</div>
          </div>
          <div class="stat-meta">
            <span>{{ t('dashboard.expiringMonth') }} {{ statsMeta.expiringMonthCount }}</span>
            <span>{{ t('dashboard.expired') }} {{ statsMeta.expiredCount }}</span>
          </div>
          <div class="stat-badge danger" v-if="stats.expiringSoon > 0">{{ stats.expiringSoon }}</div>
        </div>
      </template>
    </div>
    
    <!-- 第二行：左侧最近合同 + 右侧快捷方式和分析 -->
    <el-row :gutter="14" class="main-row">
      <!-- 左侧：最近合同 + 搜索 -->
      <el-col :span="14">
        <el-card shadow="hover" class="recent-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <span class="title">{{ t('contract.list') }}</span>
                <el-input
                  v-model="searchKeyword"
                  :placeholder="t('contract.placeholder.keyword')"
                  clearable
                  @clear="handleSearch"
                  @change="handleSearch"
                  @keyup.enter="handleSearch"
                  size="small"
                  class="header-search-input"
                >
                  <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
                <el-select v-model="selectedStatus" :placeholder="t('contract.status')" clearable size="small" class="header-filter-select" @change="handleSearch">
                  <el-option :label="t('common.all')" value="" />
                  <el-option v-for="item in statusFilterOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
                <el-select v-model="selectedType" :placeholder="t('contract.type')" clearable size="small" class="header-filter-select" @change="handleSearch">
                  <el-option :label="t('common.all')" value="" />
                  <el-option v-for="item in typeFilterOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
                <el-button
                  size="small"
                  @click="clearSearch"
                  v-if="searchKeyword || selectedStatus || selectedType"
                  link
                >{{ t('common.reset') }}</el-button>
              </div>
              <el-button type="primary" size="small" @click="$router.push('/contracts')">{{ t('common.viewAll') }}</el-button>
            </div>
          </template>
          
          <el-table
            :data="filteredContracts"
            style="width: 100%"
            size="small"
            :max-height="dashboardTableMaxHeight"
            v-loading="searchLoading"
            class="dashboard-table"
          >
            <el-table-column prop="contractNo" :label="t('contract.no')" min-width="178" class-name="contract-no-column" />
            <el-table-column prop="title" :label="t('contract.name')" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">
                <el-link type="primary" @click="$router.push(`/contracts/${row.id}`)" class="title-link">
                  {{ row.title }}
                </el-link>
              </template>
            </el-table-column>
            <el-table-column prop="type" :label="t('contract.type')" width="100" show-overflow-tooltip>
              <template #default="{ row }">
                <el-tag :type="getTypeTagType(row.type)" size="small">{{ formatType(row.type) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="amount" :label="t('contract.amount')" width="85" show-overflow-tooltip>
              <template #default="{ row }">{{ getCurrencySymbol(row.currency) }}{{ formatAmount(row.amount) }}</template>
            </el-table-column>
            <el-table-column prop="status" :label="t('contract.status')" width="75" show-overflow-tooltip>
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.status)" size="small">{{ formatStatus(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="t('contract.favorite')" width="86" align="center" show-overflow-tooltip>
              <template #default="{ row }">
                <el-icon 
                  :class="['favorite-icon', { active: row.starred }]"
                  @click="handleToggleFavorite(row)"
                >
                  <StarFilled v-if="row.starred" />
                  <Star v-else />
                </el-icon>
              </template>
            </el-table-column>
            <el-table-column :label="t('common.more')" width="104" align="center">
              <template #default="{ row }">
                <el-dropdown @command="(command) => handleMoreAction(command, row)">
                  <el-button size="small" class="more-action-btn">
                    <el-icon><MoreFilled /></el-icon>
                    <span>{{ t('common.more') }}</span>
                    <el-icon class="arrow"><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="detail">{{ t('contract.actions.view') }}</el-dropdown-item>
                      <el-dropdown-item v-if="row.status === 'DRAFT'" command="edit">{{ t('contract.actions.edit') }}</el-dropdown-item>
                      <el-dropdown-item command="copyNo">{{ t('contract.actions.copy') }}{{ t('contract.no') }}</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 分页组件 -->
          <div class="pagination-container" v-if="total > pageSize">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[8, 16, 32, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              style="margin-top: 12px; padding-top: 10px; border-top: 1px solid var(--border-color); text-align: right"
            />
          </div>
        </el-card>
      </el-col>
      
      <!-- 右侧 -->
      <el-col :span="10">
        <!-- 快捷方式 -->
        <el-card shadow="hover" class="quick-card">
          <template #header>
            <div class="card-header">
              <el-icon><Lightning /></el-icon>
              <span>{{ t('dashboard.quickActions') }}</span>
            </div>
          </template>
          <div class="quick-actions">
            <div class="quick-item" @click="$router.push('/contracts/create')">
              <div class="quick-icon blue">
                <el-icon :size="18"><Plus /></el-icon>
              </div>
              <span>{{ t('dashboard.createContract') }}</span>
            </div>
            <div class="quick-item" @click="$router.push('/templates')">
              <div class="quick-icon green">
                <el-icon :size="18"><FolderOpened /></el-icon>
              </div>
              <span>{{ t('dashboard.contractTemplate') }}</span>
            </div>
            <div class="quick-item" @click="$router.push('/favorites')">
              <div class="quick-icon orange">
                <el-icon :size="18"><Star /></el-icon>
              </div>
              <span>{{ t('menu.favorites') }}</span>
            </div>
            <div class="quick-item" @click="$router.push('/renewals')">
              <div class="quick-icon red">
                <el-icon :size="18"><RefreshRight /></el-icon>
              </div>
              <span>{{ t('menu.renewals') }}</span>
            </div>
          </div>
        </el-card>

        <el-card shadow="hover" class="expiring-workbench-card">
          <template #header>
            <div class="card-header">
              <el-icon><Bell /></el-icon>
              <span>{{ t('dashboard.expirationReminder') }}</span>
            </div>
          </template>
          <div class="expiring-overview">
            <div class="overview-pill danger">
              <span class="label">{{ t('dashboard.workbench.within1Day') }}</span>
              <span class="value">{{ expiringOverview.today }}</span>
            </div>
            <div class="overview-pill warning">
              <span class="label">{{ t('dashboard.workbench.within7Days') }}</span>
              <span class="value">{{ expiringOverview.within7Days }}</span>
            </div>
            <div class="overview-pill default">
              <span class="label">{{ t('dashboard.workbench.within30Days') }}</span>
              <span class="value">{{ expiringOverview.within30Days }}</span>
            </div>
          </div>
          <el-segmented
            v-model="expiringBucket"
            :options="[
              { label: t('dashboard.workbench.bucket1Day'), value: 'oneDay' },
              { label: t('dashboard.workbench.bucket7Days'), value: 'sevenDays' },
              { label: t('dashboard.workbench.bucket30Days'), value: 'thirtyDays' }
            ]"
            size="small"
            class="expiring-bucket-switch"
          />
          <div class="expiring-actions">
            <el-radio-group v-model="workbenchViewMode" size="small">
              <el-radio-button value="list">{{ t('dashboard.workbench.listView') }}</el-radio-button>
              <el-radio-button value="owner">{{ t('dashboard.workbench.ownerView') }}</el-radio-button>
            </el-radio-group>
            <div class="expiring-filters">
              <el-switch v-model="workbenchOnlyMine" inline-prompt :active-text="t('dashboard.workbench.onlyMine')" />
              <el-switch
                v-model="workbenchOnlyExecutable"
                inline-prompt
                :active-text="t('dashboard.workbench.onlyExecutable')"
              />
              <el-tag size="small" type="success">
                {{ t('dashboard.workbench.executableRenewalCount', { count: executableRenewalCount }) }}
              </el-tag>
              <el-tag size="small" type="warning">
                {{ t('dashboard.workbench.executableReminderCount', { count: executableReminderCount }) }}
              </el-tag>
            </div>
            <div class="batch-actions" v-if="workbenchViewMode === 'list'">
              <el-checkbox
                :model-value="allVisibleSelected"
                :indeterminate="isVisiblePartiallySelected"
                @change="toggleSelectAllVisible"
              >
                {{ t('dashboard.workbench.selectAll') }}
              </el-checkbox>
              <el-button size="small" :disabled="selectedExpiringIds.length === 0" @click="handleBatchRenewal">
                {{ t('dashboard.workbench.batchRenewal') }}
              </el-button>
              <el-button size="small" :disabled="selectedExpiringIds.length === 0" @click="handleBatchReminder">
                {{ t('dashboard.workbench.batchReminder') }}
              </el-button>
            </div>
          </div>
          <div class="expiring-list" v-loading="expiringLoading">
            <div v-if="activeExpiringList.length === 0" class="expiring-empty">
              {{ t('dashboard.workbench.empty') }}
            </div>
            <template v-else-if="workbenchViewMode === 'owner'">
              <div v-for="group in groupedExpiringByOwner" :key="group.ownerKey" class="owner-group">
                <div class="owner-header">
                  <span class="owner-name">{{ group.ownerName }}</span>
                  <span class="owner-count">{{ group.items.length }} 条</span>
                </div>
                <div v-for="item in group.items.slice(0, 3)" :key="item.id" class="expiring-item grouped">
                  <div class="expiring-main" @click="$router.push(`/contracts/${item.id}`)">
                    <div class="expiring-title">{{ item.title || item.contractNo }}</div>
                    <div class="expiring-meta">
                      <span class="contract-no">{{ item.contractNo }}</span>
                      <span :class="['remaining-days', item.daysRemaining <= 1 ? 'danger' : item.daysRemaining <= 7 ? 'warning' : '']">
                        {{ t('dashboard.workbench.remainingDays', { days: item.daysRemaining }) }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </template>
            <div
              v-else
              v-for="item in activeExpiringList.slice(0, 8)"
              :key="item.id"
              :class="['expiring-item', { 'just-processed': recentlyProcessedIds.has(item.id) }]"
            >
              <el-checkbox :model-value="selectedExpiringIds.includes(item.id)" @change="toggleSelectItem(item.id)" />
              <div class="expiring-main" @click="$router.push(`/contracts/${item.id}`)">
                <div class="expiring-title">{{ item.title || item.contractNo }}</div>
                <div class="expiring-meta">
                  <span class="contract-no">{{ item.contractNo }}</span>
                  <span :class="['remaining-days', item.daysRemaining <= 1 ? 'danger' : item.daysRemaining <= 7 ? 'warning' : '']">
                    {{ t('dashboard.workbench.remainingDays', { days: item.daysRemaining }) }}
                  </span>
                </div>
              </div>
              <el-button
                v-if="item.recommendedAction === 'startRenewal'"
                type="primary"
                link
                size="small"
                @click="handleStartRenewalFromDashboard(item)"
              >
                {{ t('dashboard.workbench.startRenewal') }}
              </el-button>
              <el-button v-else type="primary" link size="small" @click="$router.push(`/contracts/${item.id}`)">
                {{ t('common.view') }}
              </el-button>
            </div>
          </div>
          <div class="expiring-footer">
            <el-button type="primary" link @click="$router.push('/dashboard/expiration')">{{ t('common.viewAll') }}</el-button>
          </div>
        </el-card>
        
        <!-- 合同分析 -->
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header chart-header-with-presets">
              <div class="chart-header-title">
                <el-icon><DataAnalysis /></el-icon>
                <span>{{ t('statistics.title') }}</span>
              </div>
              <el-segmented v-model="currentRoughPreset" :options="roughPresetOptions" size="small" />
            </div>
          </template>
          <div class="charts-wrap">
            <div class="chart-item">
              <div class="chart-title">{{ t('statistics.byStatus') }}</div>
              <canvas ref="statusChartCanvasRef" class="chart-canvas"></canvas>
            </div>
            <div class="chart-item">
              <div class="chart-title">{{ t('statistics.byType') }}</div>
              <canvas ref="typeChartCanvasRef" class="chart-canvas"></canvas>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog
      v-model="batchResultDialogVisible"
      :title="batchResultDialogTitle"
      width="560px"
      destroy-on-close
    >
      <div class="batch-result-summary">
        <el-tag type="success">{{ t('dashboard.workbench.resultSuccess', { count: batchResult.success.length }) }}</el-tag>
        <el-tag type="warning">{{ t('dashboard.workbench.resultReused', { count: batchResult.reused.length }) }}</el-tag>
        <el-tag type="info">{{ t('dashboard.workbench.resultSkipped', { count: batchResult.skipped.length }) }}</el-tag>
        <el-tag type="danger">{{ t('dashboard.workbench.resultFailed', { count: batchResult.failed.length }) }}</el-tag>
      </div>
      <div v-if="batchResult.success.length > 0" class="batch-result-section">
        <div class="section-title">{{ t('dashboard.workbench.resultSuccessList') }}</div>
        <div class="section-list">{{ batchResult.success.join('、') }}</div>
      </div>
      <div v-if="batchResult.reused.length > 0" class="batch-result-section">
        <div class="section-title">{{ t('dashboard.workbench.resultReusedList') }}</div>
        <div class="section-list">{{ batchResult.reused.join('、') }}</div>
      </div>
      <div v-if="batchResult.skipped.length > 0" class="batch-result-section">
        <div class="section-title">{{ t('dashboard.workbench.resultSkippedList') }}</div>
        <div class="section-list">{{ batchResult.skipped.join('、') }}</div>
      </div>
      <div v-if="batchResult.failed.length > 0" class="batch-result-section">
        <div class="section-title with-action">
          <span>{{ t('dashboard.workbench.resultFailedList') }}</span>
          <el-button link type="primary" @click="copyBatchFailedList">
            {{ t('dashboard.workbench.copyFailedList') }}
          </el-button>
        </div>
        <div v-if="failedReasonGroups.length > 0" class="failed-reason-groups">
          <button
            class="reason-chip"
            :class="{ active: !selectedFailedReason }"
            @click="selectedFailedReason = ''"
          >
            {{ t('dashboard.workbench.failedReasonAll') }}：{{ batchResult.failed.length }}
          </button>
          <button
            v-for="group in failedReasonGroups"
            :key="group.reason"
            class="reason-chip"
            :class="{ active: selectedFailedReason === group.reason }"
            @click="selectedFailedReason = group.reason"
          >
            {{ group.reason }}：{{ group.count }}
          </button>
        </div>
        <div class="section-list">
          <div v-for="entry in filteredFailedEntries" :key="entry.id" class="failed-item-row">
            <span>{{ entry.name }}</span>
            <el-button
              link
              type="primary"
              :loading="retryingFailedIds.has(entry.id)"
              @click="retrySingleFailedReminder(entry)"
            >
              {{ t('dashboard.workbench.retrySingle') }}
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { get } from '@/api'
import { getContractCategories } from '@/api/contractCategory'
import { addFavorite, removeFavorite, getFavorites } from '@/api/favorite'
import { getExpiringWorkbenchSummary, startRenewalFlow } from '@/api/contract'
import { createReminder, getMyReminders } from '@/api/reminder'
import { ElMessage, ElMessageBox } from 'element-plus'
import rough from 'roughjs'
import { Lightning, DataAnalysis, Search, Star, StarFilled, Document, Money, Timer, Bell, Plus, FolderOpened, RefreshRight, MoreFilled, ArrowDown } from '@element-plus/icons-vue'
import SkeletonLoader from '@/components/SkeletonLoader.vue'
import { DEFAULT_CURRENCY, formatAmountByLocale, getCurrencySymbol } from '@/utils/currency'
import { useUserStore } from '@/stores/user'

const { t, locale } = useI18n()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)

const stats = ref({
  totalContracts: 0,
  totalAmount: 0,
  pendingApproval: 0,
  expiringSoon: 0
})
const statsMeta = ref({
  signedCount: 0,
  archivedCount: 0,
  signedAmount: 0,
  draftCount: 0,
  approvingCount: 0,
  expiringMonthCount: 0,
  expiredCount: 0
})

const recentContracts = ref<any[]>([])
const filteredContracts = ref<any[]>([])
const categories = ref<any[]>([])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)

// 搜索相关
const searchKeyword = ref('')
const selectedStatus = ref('')
const selectedType = ref('')

// 缓存数据
let cachedContracts: any[] = []
type RoughPresetKey = 'chalk' | 'marker' | 'sketch'
const currentRoughPreset = ref<RoughPresetKey>('chalk')
const roughPresetOptions = computed(() => [
  { label: t('dashboard.chalk'), value: 'chalk' },
  { label: t('dashboard.marker'), value: 'marker' },
  { label: t('dashboard.sketch'), value: 'sketch' }
])
const statusFilterOptions = computed(() => ([
  'DRAFT',
  'PENDING',
  'APPROVING',
  'APPROVED',
  'SIGNED',
  'ARCHIVED',
  'TERMINATED'
]).map(code => ({ value: code, label: formatStatus(code) })))
const typeFilterOptions = computed(() => {
  if (categories.value.length > 0) {
    return categories.value.map((item: any) => ({
      value: item.code,
      label: locale.value === 'en' && item.nameEn ? item.nameEn : item.name
    }))
  }
  return ['PURCHASE', 'SALES', 'SERVICE', 'LEASE', 'EMPLOYMENT', 'OTHER'].map(code => ({
    value: code,
    label: formatType(code)
  }))
})

watch(locale, () => {
  nextTick(() => {
    if (cachedContracts.length > 0) {
      initCharts(cachedContracts)
    }
  })
})
watch(currentRoughPreset, () => {
  nextTick(() => {
    if (cachedContracts.length > 0) {
      initCharts(cachedContracts)
    }
  })
})
const searchLoading = ref(false)
const expiringLoading = ref(false)
const WORKBENCH_FILTER_PREF_KEY = 'dashboard_workbench_filter_pref'
const expiringBucket = ref<'oneDay' | 'sevenDays' | 'thirtyDays'>('sevenDays')
const workbenchViewMode = ref<'list' | 'owner'>('list')
const workbenchOnlyMine = ref(false)
const workbenchOnlyExecutable = ref(false)
const selectedExpiringIds = ref<number[]>([])
const recentlyProcessedIds = ref<Set<number>>(new Set())
const batchResultDialogVisible = ref(false)
const batchResultDialogTitle = ref('')
const selectedFailedReason = ref('')
type FailedItemDetail = { name: string; reason: string }
type FailedReminderEntry = {
  id: number
  name: string
  contractNo?: string
  contractTitle?: string
  expireDate?: string
  remindDays?: number
  reason: string
}
const retryingFailedIds = ref<Set<number>>(new Set())
const batchResult = ref<{
  success: string[]
  reused: string[]
  skipped: string[]
  failed: string[]
  failedDetails: FailedItemDetail[]
  failedEntries: FailedReminderEntry[]
}>({
  success: [],
  reused: [],
  skipped: [],
  failed: [],
  failedDetails: [],
  failedEntries: []
})
const failedReasonGroups = computed(() => {
  const grouped = new Map<string, number>()
  ;(batchResult.value.failedDetails || []).forEach((detail) => {
    grouped.set(detail.reason, (grouped.get(detail.reason) || 0) + 1)
  })
  return Array.from(grouped.entries()).map(([reason, count]) => ({ reason, count }))
})
const filteredFailedEntries = computed(() => {
  const entries = (batchResult.value.failedEntries && batchResult.value.failedEntries.length > 0)
    ? batchResult.value.failedEntries
    : (batchResult.value.failedDetails || []).map((detail, idx) => ({
        id: -(idx + 1),
        name: detail.name,
        reason: detail.reason
      }))
  if (!selectedFailedReason.value) return entries
  return entries.filter((entry) => entry.reason === selectedFailedReason.value)
})
const expiringOverview = ref({
  today: 0,
  within7Days: 0,
  within30Days: 0
})
const expiringWorkbench = ref<Record<'oneDay' | 'sevenDays' | 'thirtyDays', any[]>>({
  oneDay: [],
  sevenDays: [],
  thirtyDays: []
})
const activeReminderContractIds = ref<Set<number>>(new Set())
const currentUserId = computed(() => Number(userStore.userInfo?.id || 0))
const activeExpiringList = computed(() => {
  let items = expiringWorkbench.value[expiringBucket.value] || []
  if (workbenchOnlyMine.value && currentUserId.value > 0) {
    items = items.filter((item: any) => Number(item.creatorId) === currentUserId.value)
  }
  if (workbenchOnlyExecutable.value) {
    items = items.filter((item: any) => (
      item.recommendedAction === 'startRenewal'
      || !activeReminderContractIds.value.has(Number(item.id))
    ))
  }
  return items
})
const executableRenewalCount = computed(() => {
  return activeExpiringList.value.filter((item: any) => item.recommendedAction === 'startRenewal').length
})
const executableReminderCount = computed(() => {
  return activeExpiringList.value.filter(
    (item: any) => !activeReminderContractIds.value.has(Number(item.id))
  ).length
})
const groupedExpiringByOwner = computed(() => {
  const grouped = new Map<string, { ownerKey: string; ownerName: string; items: any[] }>()
  activeExpiringList.value.forEach((item: any) => {
    const ownerName = item.currentApproverName
      || (item.creatorId
        ? t('dashboard.workbench.creatorOwner', { id: item.creatorId })
        : t('dashboard.workbench.unassignedOwner'))
    const ownerKey = item.currentApproverName ? `approver:${item.currentApproverName}` : `creator:${item.creatorId || 'unknown'}`
    if (!grouped.has(ownerKey)) {
      grouped.set(ownerKey, { ownerKey, ownerName, items: [] })
    }
    grouped.get(ownerKey)!.items.push(item)
  })
  return Array.from(grouped.values()).sort((a, b) => b.items.length - a.items.length)
})
const allVisibleSelected = computed(() => {
  const visibleIds = activeExpiringList.value.slice(0, 8).map((item: any) => item.id)
  return visibleIds.length > 0 && visibleIds.every(id => selectedExpiringIds.value.includes(id))
})
const isVisiblePartiallySelected = computed(() => {
  const visibleIds = activeExpiringList.value.slice(0, 8).map((item: any) => item.id)
  if (visibleIds.length === 0) return false
  const selectedCount = visibleIds.filter(id => selectedExpiringIds.value.includes(id)).length
  return selectedCount > 0 && selectedCount < visibleIds.length
})

const statusChartCanvasRef = ref<HTMLCanvasElement>()
const typeChartCanvasRef = ref<HTMLCanvasElement>()
const dashboardTableMaxHeight = ref(360)

const formatAmount = (amount: number) => {
  return formatAmountByLocale(amount, locale.value)
}

const formatType = (type: string) => {
  const category = categories.value.find((c: any) => c.code === type)
  if (category) {
    return locale.value === 'en' && category.nameEn ? category.nameEn : category.name
  }
  return type
}

const formatStatus = (status: string) => {
  const map: Record<string, string> = { 
    DRAFT: t('contract.statuses.draft'), 
    PENDING: t('contract.statuses.pending'), 
    APPROVING: t('contract.statuses.approving'), 
    APPROVED: t('contract.statuses.approved'), 
    SIGNED: t('contract.statuses.signed'), 
    ARCHIVED: t('contract.statuses.archived'), 
    TERMINATED: t('contract.statuses.terminated') 
  }
  return map[status] || status
}

const getTypeTagType = (type: string) => {
  const map: Record<string, string> = { 
    PURCHASE: 'success', SALES: 'primary', LEASE: 'warning', 
    EMPLOYMENT: 'info', SERVICE: 'danger', OTHER: 'info' 
  }
  return map[type] || 'info'
}

const getStatusTagType = (status: string) => {
  const map: Record<string, string> = { 
    DRAFT: 'info', PENDING: 'warning', APPROVING: 'warning', 
    APPROVED: 'success', SIGNED: 'success', ARCHIVED: 'info', TERMINATED: 'danger' 
  }
  return map[status] || 'info'
}

const initCharts = async (allContracts: any[]) => {
  // 确保 allContracts 是数组
  const contracts = Array.isArray(allContracts) ? allContracts : []
  
  // Calculate status distribution
  const statusCounts: Record<string, number> = {
    DRAFT: 0,
    PENDING: 0,
    APPROVING: 0,
    APPROVED: 0,
    SIGNED: 0,
    ARCHIVED: 0,
    TERMINATED: 0
  }
  
  const typeCounts: Record<string, number> = {
    PURCHASE: 0,
    SALES: 0,
    SERVICE: 0,
    LEASE: 0,
    EMPLOYMENT: 0,
    OTHER: 0
  }
  
  contracts.forEach(contract => {
    if (contract.status) {
      statusCounts[contract.status] = (statusCounts[contract.status] || 0) + 1
    }
    if (contract.type) {
      typeCounts[contract.type] = (typeCounts[contract.type] || 0) + 1
    }
  })
  
  const statusData = [
    { value: statusCounts.DRAFT, name: t('contract.statuses.draft'), itemStyle: { color: '#94a3b8' } },
    { value: statusCounts.PENDING + statusCounts.APPROVING, name: t('contract.statuses.pending'), itemStyle: { color: '#fbbf24' } },
    { value: statusCounts.APPROVED, name: t('contract.statuses.approved'), itemStyle: { color: '#60a5fa' } },
    { value: statusCounts.SIGNED, name: t('contract.statuses.signed'), itemStyle: { color: '#34d399' } },
    { value: statusCounts.ARCHIVED, name: t('contract.statuses.archived'), itemStyle: { color: '#818cf8' } },
    { value: statusCounts.TERMINATED, name: t('contract.statuses.terminated'), itemStyle: { color: '#f87171' } }
  ].filter(item => item.value > 0)
  
  const typeData = [
    { value: typeCounts.PURCHASE, itemStyle: { color: '#818cf8' } },
    { value: typeCounts.SALES, itemStyle: { color: '#34d399' } },
    { value: typeCounts.SERVICE, itemStyle: { color: '#f87171' } },
    { value: typeCounts.LEASE, itemStyle: { color: '#fbbf24' } },
    { value: typeCounts.EMPLOYMENT, itemStyle: { color: '#38bdf8' } },
    { value: typeCounts.OTHER, itemStyle: { color: '#a78bfa' } }
  ]
  
  const css = getComputedStyle(document.documentElement)
  const textPrimary = (css.getPropertyValue('--text-primary') || '#1a1a1a').trim()
  const textSecondary = (css.getPropertyValue('--text-secondary') || '#666666').trim()
  const statusDataFormatted = statusData.map(item => ({ type: item.name, value: item.value }))
  
  const typeLabels = [t('contract.types.purchase'), t('contract.types.sales'), t('contract.types.service'), t('contract.types.lease'), t('contract.types.employment'), t('contract.types.other')]
  
  const typeDataFormatted = typeData
    .map((item: any, index: number) => ({ type: typeLabels[index], value: item.value }))
    .filter(item => item.value > 0)

  drawRoughChart(statusChartCanvasRef.value, statusDataFormatted, {
    orientation: 'horizontal',
    title: `${t('statistics.byStatus')} (${statusDataFormatted.reduce((s, i) => s + i.value, 0)})`,
    textPrimary,
    textSecondary
  })

  drawRoughChart(typeChartCanvasRef.value, typeDataFormatted, {
    orientation: 'vertical',
    title: `${t('statistics.byType')} (${typeDataFormatted.reduce((s, i) => s + i.value, 0)})`,
    textPrimary,
    textSecondary
  })
}

type RoughChartOptions = {
  orientation: 'horizontal' | 'vertical'
  title: string
  textPrimary: string
  textSecondary: string
}

const roughPresets: Record<RoughPresetKey, {
  palette: string[]
  background: string
  stroke: string
  roughness: number
  bowing: number
  strokeWidth: number
  hachureGap: number
  fillStyle: 'hachure' | 'cross-hatch' | 'zigzag'
}> = {
  chalk: {
    palette: ['#89A8FF', '#8FD6B5', '#F8D27A', '#FF9B8E', '#9EDCF5', '#B9A5E8'],
    background: '#f7f4ea',
    stroke: '#2f2f2f',
    roughness: 2.8,
    bowing: 2.2,
    strokeWidth: 2.2,
    hachureGap: 6,
    fillStyle: 'hachure'
  },
  marker: {
    palette: ['#5A77FF', '#47C69D', '#FFC145', '#FF6B5A', '#57C7FF', '#8D6BFF'],
    background: '#fffdf5',
    stroke: '#272727',
    roughness: 2.2,
    bowing: 1.8,
    strokeWidth: 2.4,
    hachureGap: 8,
    fillStyle: 'zigzag'
  },
  sketch: {
    palette: ['#9aa0ac', '#9ab8a9', '#b8b08f', '#b49898', '#93a9b8', '#a99db5'],
    background: '#f5f5f3',
    stroke: '#404040',
    roughness: 3.3,
    bowing: 2.9,
    strokeWidth: 1.8,
    hachureGap: 5,
    fillStyle: 'cross-hatch'
  }
}

const drawRoughChart = (canvas: HTMLCanvasElement | undefined, data: Array<{ type: string; value: number }>, options: RoughChartOptions) => {
  if (!canvas) return
  const width = canvas.clientWidth || 360
  const height = canvas.clientHeight || 180
  canvas.width = width
  canvas.height = height

  const ctx = canvas.getContext('2d')
  if (!ctx) return
  ctx.clearRect(0, 0, width, height)

  const preset = roughPresets[currentRoughPreset.value]

  // Paper-like background
  ctx.fillStyle = preset.background
  ctx.fillRect(0, 0, width, height)

  const rc = rough.canvas(canvas)
  const palette = preset.palette

  const left = 56
  const right = width - 16
  const top = 24
  const bottom = height - 24
  const drawWidth = right - left
  const drawHeight = bottom - top
  const maxValue = Math.max(...data.map(d => d.value), 1)

  // Axes
  rc.line(left, top, left, bottom, { stroke: preset.stroke, strokeWidth: preset.strokeWidth, roughness: preset.roughness, bowing: preset.bowing })
  rc.line(left, bottom, right, bottom, { stroke: preset.stroke, strokeWidth: preset.strokeWidth, roughness: preset.roughness, bowing: preset.bowing })

  ctx.font = '600 11px "Comic Sans MS", "Marker Felt", "PingFang SC", sans-serif'
  ctx.fillStyle = options.textSecondary
  ctx.fillText(options.title, left, 14)

  if (options.orientation === 'horizontal') {
    const rowH = drawHeight / Math.max(data.length, 1)
    data.forEach((item, i) => {
      const y = top + i * rowH + rowH * 0.2
      const barH = rowH * 0.58
      const barW = (item.value / maxValue) * (drawWidth - 12)
      const color = palette[i % palette.length]
      rc.rectangle(left + 1, y, barW, barH, {
        fill: color,
        fillStyle: preset.fillStyle,
        hachureGap: preset.hachureGap,
        stroke: preset.stroke,
        strokeWidth: preset.strokeWidth,
        roughness: preset.roughness,
        bowing: preset.bowing
      })
      ctx.fillStyle = options.textPrimary
      ctx.font = 'italic 10px "Comic Sans MS", "Marker Felt", "PingFang SC", sans-serif'
      ctx.fillText(item.type, 6, y + barH * 0.68)
      ctx.fillText(String(item.value), left + barW + 6, y + barH * 0.68)
    })
  } else {
    const colW = drawWidth / Math.max(data.length, 1)
    data.forEach((item, i) => {
      const x = left + i * colW + colW * 0.2
      const barW = colW * 0.56
      const barH = (item.value / maxValue) * (drawHeight - 22)
      const y = bottom - barH
      const color = palette[i % palette.length]
      rc.rectangle(x, y, barW, barH, {
        fill: color,
        fillStyle: preset.fillStyle,
        hachureGap: preset.hachureGap,
        stroke: preset.stroke,
        strokeWidth: preset.strokeWidth,
        roughness: preset.roughness,
        bowing: preset.bowing
      })
      ctx.fillStyle = options.textPrimary
      ctx.font = 'italic 10px "Comic Sans MS", "Marker Felt", "PingFang SC", sans-serif'
      const shortName = item.type.length > 6 ? `${item.type.slice(0, 6)}…` : item.type
      ctx.fillText(shortName, x - 4, bottom + 14)
      ctx.fillText(String(item.value), x + 2, y - 6)
    })
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    // 先加载合同分类
    try {
      const catRes = await getContractCategories()
      categories.value = catRes.data || []
    } catch (catErr) {
      console.error('Failed to load categories:', catErr)
    }
    
    const overviewRes = await get('/statistics/overview')
    stats.value = overviewRes.data
    
    // 获取所有合同数据以计算准确的统计数据
    const allContractsParams = new URLSearchParams()
    allContractsParams.append('pageSize', '1000') // 假设合同数量不超过1000
    const allContractsRes = await get(`/contracts?${allContractsParams.toString()}`)
    const allContracts = allContractsRes?.data?.list || []
    cachedContracts = allContracts
    
    // 获取分页数据
    const params = new URLSearchParams()
    params.append('page', currentPage.value.toString())
    params.append('pageSize', pageSize.value.toString())
    if (searchKeyword.value) params.append('keyword', searchKeyword.value)
    if (selectedStatus.value) params.append('status', selectedStatus.value)
    if (selectedType.value) params.append('type', selectedType.value)
    
    const contractsRes = await get(`/contracts?${params.toString()}`)
    recentContracts.value = contractsRes?.data?.list || []
    filteredContracts.value = recentContracts.value
    total.value = contractsRes?.data?.total || 0
    
    // Mark favorite status for each contract
    try {
      const favRes = await getFavorites({ pageSize: 100 })
      const favoriteIds = new Set((favRes?.data?.list || []).map((f: any) => f.contractId || f.id))
      recentContracts.value.forEach((c: any) => {
        c.starred = favoriteIds.has(c.id)
      })
      filteredContracts.value.forEach((c: any) => {
        c.starred = favoriteIds.has(c.id)
      })
    } catch (e) {
      // ignore
    }
    
    // 使用实际的合同数量
    stats.value.totalContracts = allContracts.length
    
    // 计算实际的合同总金额
    const actualTotalAmount = allContracts.reduce((sum, contract) => {
      return sum + (contract.amount || 0)
    }, 0)
    stats.value.totalAmount = actualTotalAmount
    
    // 计算待审批数量（草稿 + 审批中 + 待审批）
    const pendingApprovalCount = allContracts.filter(contract => {
      return contract.status === 'DRAFT' || contract.status === 'APPROVING' || contract.status === 'PENDING'
    }).length
    stats.value.pendingApproval = pendingApprovalCount
    statsMeta.value.draftCount = allContracts.filter(contract => contract.status === 'DRAFT').length
    statsMeta.value.approvingCount = allContracts.filter(contract => contract.status === 'APPROVING').length
    
    // 计算即将到期数量（距离今天前七天内到期）
    const today = new Date()
    const sevenDaysLater = new Date()
    sevenDaysLater.setDate(today.getDate() + 7)
    
    const expiringSoonCount = allContracts.filter(contract => {
      if (!contract.endDate) return false
      const endDate = new Date(contract.endDate)
      return endDate >= today && endDate <= sevenDaysLater
    }).length
    stats.value.expiringSoon = expiringSoonCount
    const thirtyDaysLater = new Date()
    thirtyDaysLater.setDate(today.getDate() + 30)
    statsMeta.value.expiringMonthCount = allContracts.filter(contract => {
      if (!contract.endDate) return false
      const endDate = new Date(contract.endDate)
      return endDate >= today && endDate <= thirtyDaysLater
    }).length
    statsMeta.value.expiredCount = allContracts.filter(contract => {
      if (!contract.endDate) return false
      const endDate = new Date(contract.endDate)
      return endDate < today
    }).length
    statsMeta.value.signedCount = allContracts.filter(contract => contract.status === 'SIGNED').length
    statsMeta.value.archivedCount = allContracts.filter(contract => contract.status === 'ARCHIVED').length
    statsMeta.value.signedAmount = allContracts
      .filter(contract => contract.status === 'SIGNED')
      .reduce((sum, contract) => sum + (contract.amount || 0), 0)
    
    // Initialize charts with real data
    initCharts(allContracts)
  } catch (error) {
    // ignore
  } finally {
    loading.value = false
  }
}

const fetchExpiringWorkbench = async () => {
  expiringLoading.value = true
  try {
    const res = await getExpiringWorkbenchSummary()
    expiringOverview.value = res?.data?.overview || {
      today: 0,
      within7Days: 0,
      within30Days: 0
    }
    expiringWorkbench.value = {
      oneDay: res?.data?.oneDay || [],
      sevenDays: res?.data?.sevenDays || [],
      thirtyDays: res?.data?.thirtyDays || []
    }
    await refreshActiveReminderContractIds()
    selectedExpiringIds.value = []
  } catch (_err) {
    expiringOverview.value = { today: 0, within7Days: 0, within30Days: 0 }
    expiringWorkbench.value = { oneDay: [], sevenDays: [], thirtyDays: [] }
    activeReminderContractIds.value = new Set()
  } finally {
    expiringLoading.value = false
  }
}

const refreshActiveReminderContractIds = async () => {
  try {
    const res = await getMyReminders({ page: 1, pageSize: 1000 })
    const list = res?.data?.list || []
    activeReminderContractIds.value = new Set(
      list
        .filter((item: any) => (item.status ?? 0) === 0)
        .map((item: any) => Number(item.contractId))
        .filter((id: number) => Number.isFinite(id))
    )
  } catch (_err) {
    activeReminderContractIds.value = new Set()
  }
}

const loadWorkbenchFilterPref = () => {
  try {
    const raw = localStorage.getItem(WORKBENCH_FILTER_PREF_KEY)
    if (!raw) return
    const pref = JSON.parse(raw)
    workbenchOnlyMine.value = Boolean(pref?.onlyMine)
    workbenchOnlyExecutable.value = Boolean(pref?.onlyExecutable)
  } catch (_err) {
    // ignore invalid local preference
  }
}

watch([workbenchOnlyMine, workbenchOnlyExecutable], () => {
  localStorage.setItem(
    WORKBENCH_FILTER_PREF_KEY,
    JSON.stringify({
      onlyMine: workbenchOnlyMine.value,
      onlyExecutable: workbenchOnlyExecutable.value
    })
  )
})

const toggleSelectItem = (id: number) => {
  const set = new Set(selectedExpiringIds.value)
  if (set.has(id)) set.delete(id)
  else set.add(id)
  selectedExpiringIds.value = Array.from(set)
}

const toggleSelectAllVisible = (checked: boolean | string | number) => {
  const shouldSelect = Boolean(checked)
  const visibleIds = activeExpiringList.value.slice(0, 8).map((item: any) => item.id)
  const set = new Set(selectedExpiringIds.value)
  if (shouldSelect) {
    visibleIds.forEach(id => set.add(id))
  } else {
    visibleIds.forEach(id => set.delete(id))
  }
  selectedExpiringIds.value = Array.from(set)
}

const handleBatchRenewal = async () => {
  const selectedItems = activeExpiringList.value.filter((item: any) => selectedExpiringIds.value.includes(item.id))
  if (selectedItems.length === 0) return
  try {
    const renewableItems = selectedItems.filter((item: any) => item.recommendedAction === 'startRenewal')
    const blockedItems = selectedItems.filter((item: any) => item.recommendedAction !== 'startRenewal')
    if (renewableItems.length === 0) {
      ElMessage.warning(t('dashboard.workbench.batchRenewalUnavailable'))
      return
    }
    if (blockedItems.length > 0) {
      const sample = blockedItems.slice(0, 3).map((item: any) => item.contractNo || item.title).join('、')
      await ElMessageBox.confirm(
        t('dashboard.workbench.batchRenewalPartialConfirm', {
          total: selectedItems.length,
          blocked: blockedItems.length,
          sample,
          renewable: renewableItems.length
        }),
        t('dashboard.workbench.batchRenewal'),
        { type: 'warning' }
      )
    } else {
      await ElMessageBox.confirm(
        t('dashboard.workbench.batchRenewalConfirm', { count: renewableItems.length }),
        t('dashboard.workbench.batchRenewal'),
        { type: 'warning' }
      )
    }
    let success = 0
    const successIds: number[] = []
    const successNos: string[] = []
    const failedNos: string[] = []
    for (const item of renewableItems) {
      try {
        await startRenewalFlow(item.id, t('dashboard.workbench.batchRenewalReason'))
        success++
        successIds.push(item.id)
        successNos.push(contractDisplayName(item))
      } catch (_err) {
        failedNos.push(contractDisplayName(item))
      }
    }
    ElMessage.success(t('dashboard.workbench.batchRenewalSuccess', { success, total: renewableItems.length }))
    selectedExpiringIds.value = []
    if (successIds.length > 0) {
      markRecentlyProcessed(successIds)
    }
    showBatchResultDialog(t('dashboard.workbench.batchRenewalResultTitle'), {
      success: successNos,
      reused: [],
      skipped: blockedItems.map((item: any) => contractDisplayName(item)),
      failed: failedNos
    })
    await fetchExpiringWorkbench()
    await fetchData()
  } catch (_err) {
    // ignore cancel
  }
}

const handleBatchReminder = async () => {
  const selectedItems = activeExpiringList.value.filter((item: any) => selectedExpiringIds.value.includes(item.id))
  if (selectedItems.length === 0) return
  try {
    const existingRes = await getMyReminders({ page: 1, pageSize: 1000 })
    const existingList = existingRes?.data?.list || []
    const existingContractIds = new Set(
      existingList
        .filter((r: any) => (r.status ?? 0) === 0)
        .map((r: any) => Number(r.contractId))
        .filter((id: number) => Number.isFinite(id))
    )
    const executableItems = selectedItems.filter((item: any) => !existingContractIds.has(Number(item.id)))
    const skippedItems = selectedItems.filter((item: any) => existingContractIds.has(Number(item.id)))
    if (executableItems.length === 0) {
      ElMessage.warning(t('dashboard.workbench.batchReminderUnavailable'))
      return
    }
    if (skippedItems.length > 0) {
      const sample = skippedItems.slice(0, 3).map((item: any) => item.contractNo || item.title).join('、')
      await ElMessageBox.confirm(
        t('dashboard.workbench.batchReminderPartialConfirm', {
          total: selectedItems.length,
          skipped: skippedItems.length,
          sample,
          executable: executableItems.length
        }),
        t('dashboard.workbench.batchReminder'),
        { type: 'warning' }
      )
    }
    let success = 0
    const failedNos: string[] = []
    const failedDetails: FailedItemDetail[] = []
    const failedEntries: FailedReminderEntry[] = []
    const successIds: number[] = []
    const successNos: string[] = []
    const reusedNos: string[] = []
    for (const item of executableItems) {
      try {
        const resp = await createReminder({
          contractId: item.id,
          contractNo: item.contractNo,
          contractTitle: item.title,
          expireDate: item.endDate,
          remindDays: Math.max(0, Number(item.daysRemaining) || 0),
          reminderType: 0,
          status: 0
        })
        success++
        successIds.push(item.id)
        if (resp?.data?.existed) {
          reusedNos.push(contractDisplayName(item))
        } else {
          successNos.push(contractDisplayName(item))
        }
      } catch (err: any) {
        const displayName = contractDisplayName(item)
        const reason = normalizeReminderErrorReason(err)
        failedNos.push(displayName)
        failedDetails.push({
          name: displayName,
          reason
        })
        failedEntries.push({
          id: Number(item.id),
          name: displayName,
          contractNo: item.contractNo,
          contractTitle: item.title,
          expireDate: item.endDate,
          remindDays: Math.max(0, Number(item.daysRemaining) || 0),
          reason
        })
      }
    }
    selectedExpiringIds.value = []
    if (successIds.length > 0) {
      markRecentlyProcessed(successIds)
    }
    showBatchResultDialog(t('dashboard.workbench.batchReminderResultTitle'), {
      success: successNos,
      reused: reusedNos,
      skipped: skippedItems.map((item: any) => contractDisplayName(item)),
      failed: failedNos,
      failedDetails,
      failedEntries
    })
    if (failedNos.length > 0) {
      ElMessage.warning(
        t('dashboard.workbench.batchReminderPartialFailed', {
          success,
          total: executableItems.length,
          sample: failedNos.slice(0, 3).join('、')
        })
      )
    } else {
      ElMessage.success(
        t('dashboard.workbench.batchReminderSuccessDetailed', {
          success,
          total: executableItems.length,
          created: successNos.length,
          reused: reusedNos.length
        })
      )
    }
  } catch (_err) {
    ElMessage.error(t('dashboard.workbench.batchReminderFailed'))
  }
}

const markRecentlyProcessed = (ids: number[]) => {
  const next = new Set(recentlyProcessedIds.value)
  ids.forEach(id => next.add(id))
  recentlyProcessedIds.value = next
  setTimeout(() => {
    const after = new Set(recentlyProcessedIds.value)
    ids.forEach(id => after.delete(id))
    recentlyProcessedIds.value = after
  }, 3500)
}

const contractDisplayName = (item: any) => item.contractNo || item.title || String(item.id)

const normalizeReminderErrorReason = (err: any): string => {
  const messageKey = err?.response?.data?.messageKey
  if (messageKey === 'error.forbidden') return t('dashboard.workbench.failureReasonPermission')
  if (messageKey === 'error.auth.unauthorized') return t('dashboard.workbench.failureReasonUnauthorized')
  if (messageKey === 'error.reminder.createFailed') return t('dashboard.workbench.failureReasonCreateRejected')

  const status = Number(err?.response?.status)
  if (status === 401) return t('dashboard.workbench.failureReasonUnauthorized')
  if (status === 403) return t('dashboard.workbench.failureReasonPermission')
  if (status >= 500) return t('dashboard.workbench.failureReasonServer')

  if (err?.message && String(err.message).toLowerCase().includes('network')) {
    return t('dashboard.workbench.failureReasonNetwork')
  }
  return t('dashboard.workbench.failureReasonUnknown')
}

const copyBatchFailedList = async () => {
  const list = filteredFailedEntries.value.map((entry) => entry.name)
  if (list.length === 0) {
    ElMessage.warning(t('dashboard.workbench.copyFailedListEmpty'))
    return
  }
  try {
    await navigator.clipboard.writeText(list.join('\n'))
    ElMessage.success(t('dashboard.workbench.copyFailedListSuccess'))
  } catch (_err) {
    ElMessage.error(t('dashboard.workbench.copyFailedListError'))
  }
}

const retrySingleFailedReminder = async (entry: FailedReminderEntry) => {
  const next = new Set(retryingFailedIds.value)
  next.add(entry.id)
  retryingFailedIds.value = next
  try {
    const resp = await createReminder({
      contractId: entry.id,
      contractNo: entry.contractNo,
      contractTitle: entry.contractTitle,
      expireDate: entry.expireDate,
      remindDays: entry.remindDays ?? 0,
      reminderType: 0,
      status: 0
    })
    if (resp?.data?.existed) {
      batchResult.value.reused = [...batchResult.value.reused, entry.name]
    } else {
      batchResult.value.success = [...batchResult.value.success, entry.name]
    }
    batchResult.value.failedEntries = (batchResult.value.failedEntries || []).filter((item) => item.id !== entry.id)
    batchResult.value.failedDetails = (batchResult.value.failedDetails || []).filter((item) => item.name !== entry.name)
    batchResult.value.failed = (batchResult.value.failed || []).filter((name) => name !== entry.name)
    markRecentlyProcessed([entry.id])
    ElMessage.success(t('dashboard.workbench.retrySingleSuccess'))
  } catch (_err) {
    ElMessage.error(t('dashboard.workbench.retrySingleFailed'))
  } finally {
    const after = new Set(retryingFailedIds.value)
    after.delete(entry.id)
    retryingFailedIds.value = after
  }
}

const showBatchResultDialog = (
  title: string,
  payload: {
    success: string[]
    reused: string[]
    skipped: string[]
    failed: string[]
    failedDetails?: FailedItemDetail[]
    failedEntries?: FailedReminderEntry[]
  }
) => {
  batchResultDialogTitle.value = title
  batchResult.value = {
    ...payload,
    failedDetails: payload.failedDetails || [],
    failedEntries: payload.failedEntries || []
  }
  selectedFailedReason.value = ''
  batchResultDialogVisible.value = true
}

const handleStartRenewalFromDashboard = async (item: any) => {
  try {
    await ElMessageBox.confirm(
      t('dashboard.workbench.startRenewalConfirm', { title: item.title || item.contractNo }),
      t('dashboard.workbench.startRenewal'),
      { type: 'warning' }
    )
    await startRenewalFlow(item.id, t('dashboard.workbench.startRenewalReason'))
    ElMessage.success(t('dashboard.workbench.startRenewalSuccess'))
    await fetchExpiringWorkbench()
    await fetchData()
  } catch (err: any) {
    if (err !== 'cancel') {
      ElMessage.error(err?.message || t('dashboard.workbench.startRenewalFailed'))
    }
  }
}

const refreshDashboardLayout = () => {
  const viewportHeight = window.innerHeight || 900
  dashboardTableMaxHeight.value = Math.max(240, Math.min(420, Math.floor(viewportHeight * 0.36)))
  if (cachedContracts.length > 0) {
    initCharts(cachedContracts)
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  searchLoading.value = true
  setTimeout(async () => {
    await fetchData()
    searchLoading.value = false
  }, 300)
}

// 清空搜索
const clearSearch = () => {
  searchKeyword.value = ''
  selectedStatus.value = ''
  selectedType.value = ''
  currentPage.value = 1
  fetchData()
}

// 分页处理
const handleCurrentChange = (current: number) => {
  currentPage.value = current
  fetchData()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchData()
}

const handleToggleFavorite = async (row: any) => {
  try {
    if (row.starred) {
      await removeFavorite(row.id)
      row.starred = false
    } else {
      await addFavorite(row.id)
      row.starred = true
    }
  } catch (error) {
    console.error('Toggle favorite failed:', error)
  }
}

const handleMoreAction = async (command: string, row: any) => {
  if (command === 'detail') {
    router.push(`/contracts/${row.id}`)
    return
  }
  if (command === 'edit') {
    if (row.status !== 'DRAFT') {
      ElMessage.warning(t('contract.onlyDraftCanEdit'))
      return
    }
    router.push(`/contracts/${row.id}/edit`)
    return
  }
  if (command === 'copyNo') {
    if (!row.contractNo) return
    try {
      await navigator.clipboard.writeText(row.contractNo)
      ElMessage.success(t('contract.actions.copySuccess'))
    } catch (_err) {
      ElMessage.error(t('contract.actions.copyError'))
    }
    return
  }
}

onMounted(async () => {
  loadWorkbenchFilterPref()
  await fetchData()
  await fetchExpiringWorkbench()
  refreshDashboardLayout()
  window.addEventListener('resize', refreshDashboardLayout)
})

onUnmounted(() => {
  window.removeEventListener('resize', refreshDashboardLayout)
})
</script>

<style scoped lang="scss">
.dashboard {
  padding: 0;
  display: flex;
  flex-direction: column;
  
  .page-title {
    font-size: 22px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 12px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 100%;
    cursor: pointer;
    background: var(--primary-gradient);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    
    &:hover {
      overflow: visible;
      white-space: normal;
      text-overflow: clip;
    }
  }
}

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 12px;
  
  .stat-card-skeleton {
    background: var(--bg-card);
    border: 1px solid var(--border-color);
    border-radius: var(--radius);
    padding: 16px;
  }
  
  .stat-card {
    border-radius: var(--radius);
    padding: 12px;
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
    transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
    cursor: pointer;
    position: relative;
    overflow: hidden;
    
    &::before {
      content: '';
      position: absolute;
      inset: 0;
      opacity: 0.95;
      z-index: 0;
    }
    
    &::after {
      content: '';
      position: absolute;
      top: -50%;
      right: -50%;
      width: 100%;
      height: 100%;
      background: radial-gradient(circle, rgba(255,255,255,0.3) 0%, transparent 70%);
      z-index: 1;
    }
    
    &:hover {
      transform: translateY(-4px);
      
      .stat-icon {
        transform: scale(1.1);
      }
    }
    
    &.gradient-purple:hover {
      box-shadow: 0 16px 36px rgba(102, 126, 234, 0.4);
    }
    
    &.gradient-green:hover {
      box-shadow: 0 16px 36px rgba(34, 197, 94, 0.35);
    }
    
    &.gradient-orange:hover {
      box-shadow: 0 16px 36px rgba(249, 115, 22, 0.35);
    }
    
    &.gradient-red:hover {
      box-shadow: 0 16px 36px rgba(59, 130, 246, 0.35);
    }
    
    .stat-bg-icon {
      position: absolute;
      bottom: -5px;
      right: -5px;
      width: 42px;
      height: 42px;
      opacity: 0.08;
      z-index: 0;
      
      svg {
        width: 100%;
        height: 100%;
        color: #fff;
      }
    }
    
    .stat-icon {
      width: 28px;
      height: 28px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      position: relative;
      z-index: 2;
      transition: transform 0.3s ease;
      background: rgba(255, 255, 255, 0.2);
      backdrop-filter: blur(10px);
      
      svg {
        width: 14px;
        height: 14px;
        color: #fff;
      }
    }
    
    .stat-info {
      position: relative;
      z-index: 2;
      flex: 1;
      
      .stat-value {
        font-size: 18px;
        font-weight: 700;
        color: #fff;
        line-height: 1.2;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }
      
      .stat-label {
        font-size: 11px;
        color: rgba(255, 255, 255, 0.85);
        margin-top: 2px;
        font-weight: 500;
      }
    }

    .stat-meta {
      position: relative;
      z-index: 2;
      display: flex;
      gap: 6px;
      flex-wrap: wrap;

      span {
        font-size: 10px;
        line-height: 1.2;
        color: rgba(255, 255, 255, 0.9);
        background: rgba(255, 255, 255, 0.16);
        border: 1px solid rgba(255, 255, 255, 0.22);
        border-radius: 10px;
        padding: 2px 7px;
      }
    }
    
    .stat-badge {
      position: absolute;
      top: 12px;
      right: 12px;
      min-width: 22px;
      height: 22px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
      font-weight: 700;
      color: #fff;
      background: rgba(255, 255, 255, 0.3);
      backdrop-filter: blur(10px);
      z-index: 2;
      
      &.warning {
        background: rgba(255, 255, 255, 0.3);
      }
      
      &.danger {
        background: rgba(255, 255, 255, 0.3);
      }
    }
    
    /* 经典高饱和配色 - 每张卡片有独特性格 */
    &.gradient-purple {
      background: linear-gradient(135deg, #259C71 0%, #1e7d5a 100%);
      color: #fff;
      
      .stat-icon {
        background: rgba(255, 255, 255, 0.2);
      }
    }
    
    &.gradient-green {
      background: linear-gradient(135deg, #EF0056 0%, #c40047 100%);
      color: #fff;
      
      .stat-icon {
        background: rgba(255, 255, 255, 0.2);
      }
    }
    
    &.gradient-orange {
      background: linear-gradient(135deg, #003153 0%, #00243d 100%);
      color: #fff;
      
      .stat-icon {
        background: rgba(255, 255, 255, 0.2);
      }
    }
    
    &.gradient-red {
      background: linear-gradient(135deg, #EA642D 0%, #c45124 100%);
      color: #fff;
      
      .stat-icon {
        background: rgba(255, 255, 255, 0.2);
      }
    }
  }
}

/* 主内容行 */
.main-row {
  flex: 1;
  min-height: 0;
  .recent-card {
    height: 100%;
  }
  
  .quick-card, .chart-card {
    .card-header {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 15px;
      
      .el-icon {
        font-size: 15px;
        padding: 4px;
        border-radius: 7px;
        
        svg {
          color: #fff;
        }
      }
    }
    
    &.quick-card .card-header .el-icon {
      background: linear-gradient(135deg, #f5af19, #f12711);
    }
    
    &.chart-card .card-header .el-icon {
      background: linear-gradient(135deg, #667eea, #764ba2);
    }
  }
  
  .recent-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;
      gap: 10px;
      flex-wrap: nowrap;
      min-width: 0;
      
      .header-left {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-wrap: nowrap;
        flex: 1;
        min-width: 0;
        overflow: hidden;
        
        .title {
          font-size: 15px;
          font-weight: 600;
          color: var(--text-primary);
          margin-right: 4px;
          max-width: 140px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }

      :deep(.el-select),
      :deep(.el-input) {
        flex-shrink: 0;
      }
    }
  }
  
  .quick-card {
    margin-bottom: 10px;

    :deep(.el-card__header) {
      padding: 6px 12px;
      min-height: 32px;
      display: flex;
      align-items: center;
    }

    :deep(.el-card__body) {
      padding: 16px 12px 16px;
    }
  }

  .expiring-workbench-card {
    margin-bottom: 10px;
    border: 1px solid var(--border-color);

    :deep(.el-card__header) {
      padding: 6px 12px;
      min-height: 32px;
      display: flex;
      align-items: center;
    }

    :deep(.el-card__body) {
      padding: 12px;
    }

    .card-header .el-icon {
      font-size: 15px;
      padding: 4px;
      border-radius: 7px;
      background: linear-gradient(135deg, #fb7185, #f97316);

      svg {
        color: #fff;
      }
    }

    .expiring-overview {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 8px;
      margin-bottom: 10px;

      .overview-pill {
        border: 1px solid var(--border-color);
        border-radius: 10px;
        padding: 8px;
        background: var(--bg-hover);
        display: flex;
        flex-direction: column;
        gap: 2px;
        min-width: 0;

        .label {
          font-size: 11px;
          color: var(--text-secondary);
        }

        .value {
          font-size: 16px;
          font-weight: 700;
          color: var(--text-primary);
          line-height: 1.1;
        }

        &.danger {
          border-color: rgba(245, 108, 108, 0.25);
          background: rgba(245, 108, 108, 0.08);
        }

        &.warning {
          border-color: rgba(230, 162, 60, 0.28);
          background: rgba(230, 162, 60, 0.08);
        }
      }
    }

    .expiring-bucket-switch {
      margin-bottom: 10px;
      width: 100%;
    }

    .expiring-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 8px;
      margin-bottom: 10px;
      flex-wrap: wrap;
    }

    .batch-actions {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
    }

    .expiring-filters {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-left: auto;
    }

    .expiring-list {
      min-height: 110px;
    }

    .expiring-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 8px;
      padding: 8px;
      border: 1px solid var(--border-color);
      border-radius: 10px;
      margin-bottom: 8px;
      background: var(--bg-hover);

      &:last-child {
        margin-bottom: 0;
      }

      &.grouped {
        padding: 6px 8px;
      }

      &.just-processed {
        border-color: color-mix(in srgb, var(--primary) 40%, var(--border-color));
        box-shadow: 0 0 0 1px color-mix(in srgb, var(--primary) 25%, transparent);
        animation: workbenchPulse 0.9s ease;
      }
    }

    .owner-group {
      margin-bottom: 10px;
    }

    .owner-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 6px;
      font-size: 12px;

      .owner-name {
        color: var(--text-primary);
        font-weight: 600;
      }

      .owner-count {
        color: var(--text-secondary);
      }
    }

    .expiring-main {
      min-width: 0;
      cursor: pointer;
      flex: 1;
    }

    .expiring-title {
      font-size: 13px;
      color: var(--text-primary);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .expiring-meta {
      display: flex;
      gap: 8px;
      font-size: 12px;
      color: var(--text-secondary);
      align-items: center;
      flex-wrap: wrap;

      .contract-no {
        max-width: 130px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .remaining-days {
        border-radius: 999px;
        padding: 1px 8px;
        background: var(--info-bg);
        color: var(--text-secondary);

        &.warning {
          color: #e6a23c;
          background: rgba(230, 162, 60, 0.12);
        }

        &.danger {
          color: #f56c6c;
          background: rgba(245, 108, 108, 0.12);
        }
      }
    }

    .expiring-empty {
      font-size: 12px;
      color: var(--text-secondary);
      padding: 10px 0;
    }

    .expiring-footer {
      margin-top: 6px;
      text-align: right;
    }
  }

  .chart-card {
    :deep(.el-card__header) {
      padding: 3px 12px;
      min-height: 29px;
      display: flex;
      align-items: center;
    }

    :deep(.el-card__body) {
      padding: 19px 12px 19px;
    }

    .card-header {
      font-size: 16px;

      .el-icon {
        font-size: 16px;
        padding: 5px;
        border-radius: 8px;
      }
    }
  }
}

.batch-result-summary {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.batch-result-section {
  margin-bottom: 10px;

  .section-title {
    font-size: 12px;
    color: var(--text-secondary);
    margin-bottom: 4px;
  }

  .section-title.with-action {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .failed-reason-groups {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: 6px;
  }

  .reason-chip {
    font-size: 12px;
    color: var(--text-secondary);
    background: var(--bg-hover);
    border: 1px solid var(--border-color);
    border-radius: 999px;
    padding: 2px 8px;
    cursor: pointer;
  }

  .reason-chip.active {
    color: #fff;
    background: var(--el-color-primary);
    border-color: var(--el-color-primary);
  }

  .section-list {
    font-size: 13px;
    color: var(--text-primary);
    line-height: 1.5;
    word-break: break-word;
    background: var(--bg-hover);
    border: 1px solid var(--border-color);
    border-radius: 8px;
    padding: 8px 10px;
  }

  .failed-item-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
  }

  .failed-item-row + .failed-item-row {
    margin-top: 4px;
  }
}

@keyframes workbenchPulse {
  0% {
    transform: translateY(0);
  }
  35% {
    transform: translateY(-1px);
  }
  100% {
    transform: translateY(0);
  }
}

/* 快捷方式 */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  
  .quick-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 12px 8px;
    background: var(--bg-card);
    border: 1px solid var(--border-color);
    border-radius: var(--radius);
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
    position: relative;
    overflow: hidden;
    
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 3px;
      border-radius: 3px 3px 0 0;
      opacity: 0;
      transition: opacity 0.3s ease;
    }
    
    &:hover {
      border-color: transparent;
      transform: translateY(-4px);
      box-shadow: 0 12px 28px rgba(0, 0, 0, 0.08);
      
      &::before {
        opacity: 1;
      }
      
      .quick-icon {
        transform: scale(1.08);
      }
    }
    
    .quick-icon {
      width: 38px;
      height: 38px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      transition: transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
      
      svg {
        width: 18px;
        height: 18px;
        color: #fff;
      }
      
      /* 丰富多彩配色 - 每个图标独立个性 */
      &.blue {
        background: linear-gradient(135deg, #1d4fff 0%, #00a3ff 100%);
        box-shadow: 0 6px 16px rgba(29, 79, 255, 0.45);
      }
      &.green {
        background: linear-gradient(135deg, #00d26a 0%, #00a86b 100%);
        box-shadow: 0 6px 16px rgba(0, 210, 106, 0.42);
      }
      &.orange {
        background: linear-gradient(135deg, #ff9f0a 0%, #ff6a00 100%);
        box-shadow: 0 6px 16px rgba(255, 122, 0, 0.45);
      }
      &.red {
        background: linear-gradient(135deg, #ff3b30 0%, #d70015 100%);
        box-shadow: 0 6px 16px rgba(255, 59, 48, 0.42);
      }

      .badge {
        position: absolute;
        top: -6px;
        right: -6px;
        min-width: 20px;
        height: 20px;
        background: var(--danger-gradient);
        color: #fff;
        border-radius: 10px;
        font-size: 12px;
        font-weight: 700;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 0 6px;
        border: 2px solid var(--bg-card);
      }
    }
    
    &:nth-child(1) {
      &::before { background: linear-gradient(90deg, #1d4fff, #00a3ff); }
      &:hover { box-shadow: 0 12px 28px rgba(29, 79, 255, 0.2); }
    }
    &:nth-child(2) {
      &::before { background: linear-gradient(90deg, #00d26a, #00a86b); }
      &:hover { box-shadow: 0 12px 28px rgba(0, 210, 106, 0.2); }
    }
    &:nth-child(3) {
      &::before { background: linear-gradient(90deg, #ff9f0a, #ff6a00); }
      &:hover { box-shadow: 0 12px 28px rgba(255, 122, 0, 0.22); }
    }
    &:nth-child(4) {
      &::before { background: linear-gradient(90deg, #ff3b30, #d70015); }
      &:hover { box-shadow: 0 12px 28px rgba(255, 59, 48, 0.22); }
    }

    span:not(.badge) {
      font-size: 13px;
      font-weight: 500;
      color: var(--text-secondary);
      text-align: center;
      line-height: 1.35;
      max-width: 100%;
      white-space: normal;
      word-wrap: break-word;
      overflow-wrap: break-word;
      display: block;
      padding: 0 4px;
      cursor: pointer;
      transition: color 0.2s;
    }
    
    &:hover span:not(.badge) {
      color: var(--text-primary);
    }
  }
}

@media (max-width: 1280px) {
  .quick-actions {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* 图表 */
.charts-wrap {
  display: flex;
  gap: 10px;
  
  .chart-item {
    flex: 1;
    min-width: 0;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .chart-title {
      font-size: 12px;
      font-weight: 600;
      color: var(--text-secondary);
      margin-bottom: 6px;
      word-break: break-word;
      line-height: 1.4;
    }
    
    .chart-canvas {
      width: 100%;
      height: 148px;
      border: 2px dashed rgba(32, 32, 32, 0.35);
      border-radius: var(--radius);
      box-shadow: inset 0 0 0 1px rgba(0, 0, 0, 0.04);
      background: #f8f5ec;
      display: block;
    }
  }
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  
  span {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 200px;
    cursor: pointer;
    
    &:hover {
      overflow: visible;
      white-space: normal;
      text-overflow: clip;
      background: var(--bg-hover);
      border-radius: 4px;
      padding: 4px;
      margin: -4px -4px;
    }
  }
}

.chart-header-with-presets {
  width: 100%;
  justify-content: space-between;
  gap: 10px;
  flex-wrap: wrap;
}

.chart-header-title {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

/* 搜索栏 */
.search-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-color);
  flex-wrap: wrap;
  align-items: center;
}

/* 表格深色模式样式 */
:deep(.el-table) {
  --el-table-bg-color: var(--bg-card);
  --el-table-tr-bg-color: var(--bg-card);
  --el-table-header-bg-color: var(--bg-hover);
  --el-table-row-hover-bg-color: var(--info-bg);
  --el-table-border-color: var(--border-color);
  --el-table-text-color: var(--text-primary);
  --el-table-header-text-color: var(--text-primary);
  
  th.el-table__cell {
    background-color: var(--bg-hover);
    color: var(--text-primary);
    font-weight: 600;
    border-color: var(--border-color);
  }
  
  td.el-table__cell {
    border-color: var(--border-color);
    color: var(--text-primary);
  }
  
  .el-table__header th {
    background-color: var(--bg-hover) !important;
    color: var(--text-primary) !important;
  }
  
  .el-table__body tr {
    background-color: var(--bg-card);
    
    &:hover > td {
      background-color: var(--info-bg) !important;
    }
  }
  
  .el-table__body td {
    border-bottom-color: var(--border-color);
  }
  
  .el-link--primary {
    color: var(--primary);
    
    &:hover {
      color: var(--primary-light);
    }
  }
}

:deep(.el-tag) {
  border-color: transparent;
}

:deep(.el-pagination) {
  --el-pagination-bg-color: var(--bg-card);
  --el-pagination-text-color: var(--text-secondary);
  --el-pagination-button-disabled-bg-color: var(--bg-hover);
  --el-pagination-hover-color: var(--primary);
  
  .el-pager li {
    background: var(--bg-card);
    color: var(--text-secondary);
    
    &:hover {
      color: var(--primary);
    }
    
    &.is-active {
      background: var(--primary);
      color: #fff;
    }
  }
  
  .btn-prev, .btn-next {
    background: var(--bg-card);
    color: var(--text-secondary);
    
    &:hover {
      color: var(--primary);
    }
  }
}

/* 快捷操作区域按钮深色模式 */
:deep(.el-button--primary) {
  background: var(--primary);
  border-color: var(--primary);
  color: #fff;
  
  &:hover, &:focus {
    background: var(--primary-light);
    border-color: var(--primary-light);
    color: #fff;
  }
}

/* 输入框深色模式 */
:deep(.el-input__wrapper) {
  background: var(--bg-card);
  border-color: var(--border-color);
  box-shadow: none;
  
  &:hover {
    border-color: var(--primary);
  }
  
  &.is-focus {
    border-color: var(--primary);
    box-shadow: 0 0 0 1px var(--primary) inset;
  }
  
  .el-input__inner {
    color: var(--text-primary);
    
    &::placeholder {
      color: var(--text-placeholder);
    }
  }
}

/* 选择器深色模式 */
:deep(.el-select__wrapper) {
  background: var(--bg-card);
  border-color: var(--border-color);
  box-shadow: none;
  
  &:hover {
    border-color: var(--primary);
  }
  
  &.is-focused {
    border-color: var(--primary);
    box-shadow: 0 0 0 1px var(--primary) inset;
  }
  
  .el-select__placeholder {
    color: var(--text-placeholder);
  }
}

:deep(.el-select-dropdown__item) {
  color: var(--text-primary);
  
  &:hover {
    background: var(--info-bg);
  }
  
  &.is-selected {
    color: var(--primary);
    font-weight: 600;
  }
}

/* 所有按钮深色模式 - 提高对比度 */
:deep(.el-button) {
  background: var(--bg-card);
  border-color: var(--border-color);
  color: var(--text-primary);
  
  &:hover, &:focus {
    background: var(--bg-hover);
    border-color: var(--primary);
    color: var(--primary);
  }
  
  &.el-button--primary {
    background: var(--primary);
    border-color: var(--primary);
    color: #fff;
    
    &:hover, &:focus {
      background: var(--primary-light);
      border-color: var(--primary-light);
      color: #fff;
    }
  }
  
  &.el-button--success {
    background: var(--success);
    border-color: var(--success);
    color: #fff;
    
    &:hover, &:focus {
      background: color-mix(in srgb, var(--success) 85%, #000);
      border-color: color-mix(in srgb, var(--success) 85%, #000);
      color: #fff;
    }
  }
  
  &.el-button--warning {
    background: var(--warning);
    border-color: var(--warning);
    color: #fff;
    
    &:hover, &:focus {
      background: color-mix(in srgb, var(--warning) 85%, #000);
      border-color: color-mix(in srgb, var(--warning) 85%, #000);
      color: #fff;
    }
  }
  
  &.el-button--danger {
    background: var(--danger);
    border-color: var(--danger);
    color: #fff;
    
    &:hover, &:focus {
      background: color-mix(in srgb, var(--danger) 85%, #000);
      border-color: color-mix(in srgb, var(--danger) 85%, #000);
      color: #fff;
    }
  }
  
  &.el-button--info {
    background: var(--info);
    border-color: var(--info);
    color: #fff;
    
    &:hover, &:focus {
      background: color-mix(in srgb, var(--info) 85%, #000);
      border-color: color-mix(in srgb, var(--info) 85%, #000);
      color: #fff;
    }
  }
}

/* 链接按钮样式 */
:deep(.el-link) {
  color: var(--primary);
  
  &:hover {
    color: var(--primary-light);
  }
  
  &.el-link--primary {
    color: var(--primary);
  }
  
  &.el-link--success {
    color: var(--success);
  }
  
  &.el-link--warning {
    color: var(--warning);
  }
  
  &.el-link--danger {
    color: var(--danger);
  }
}

/* 收藏图标 */
.favorite-icon {
  cursor: pointer;
  font-size: 16px;
  color: var(--text-placeholder);
  transition: color 0.2s;
  
  &:hover {
    color: #f5a623;
  }
  
  &.active {
    color: #f5a623;
  }
}

/* 标签单元格 */
.tags-cell {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
  
  .tag-item {
    max-width: 80px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

/* 表格优化样式 */
.dashboard-table {
  .more-action-btn {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 8px;
    border-radius: 12px;
    border: 1px solid var(--border-color);
    background: var(--bg-card);
    color: var(--text-secondary);

    .arrow {
      font-size: 12px;
      opacity: 0.85;
    }
  }

  :deep(.el-table__inner-wrapper::before) {
    height: 0;
  }

  :deep(.el-table__header-wrapper) {
    th .cell {
      word-break: normal;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      line-height: 1.2;
      font-weight: 600;
    }
  }
  
  :deep(.el-table__body-wrapper) {
    td .cell {
      word-break: break-word;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    td.contract-no-column .cell {
      overflow: visible;
      text-overflow: clip;
    }
  }

  :deep(.contract-no-column .cell) {
    white-space: nowrap !important;
    overflow: visible !important;
    text-overflow: clip !important;
  }
  
  .title-link {
    display: inline-block;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.header-search-input {
  width: 220px;
}

.header-filter-select {
  width: 110px;
}

@media (max-width: 1280px) {
  .main-row .recent-card .card-header {
    flex-wrap: wrap;

    .header-left {
      flex-wrap: wrap;
      overflow: visible;
    }
  }
}

@media (max-height: 920px) {
  .dashboard .page-title {
    margin-bottom: 8px;
  }

  .stats-grid {
    margin-bottom: 8px;

    .stat-card {
      padding: 10px;
    }
  }

  .charts-wrap .chart-item .chart-canvas {
    height: 132px;
  }
}
</style>
