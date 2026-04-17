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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { get } from '@/api'
import { getContractCategories } from '@/api/contractCategory'
import { addFavorite, removeFavorite, getFavorites } from '@/api/favorite'
import { ElMessage } from 'element-plus'
import rough from 'roughjs'
import { Lightning, DataAnalysis, Search, Star, StarFilled, Document, Money, Timer, Bell, Plus, FolderOpened, RefreshRight, MoreFilled, ArrowDown } from '@element-plus/icons-vue'
import SkeletonLoader from '@/components/SkeletonLoader.vue'
import { DEFAULT_CURRENCY, formatAmountByLocale, getCurrencySymbol } from '@/utils/currency'

const { t, locale } = useI18n()
const router = useRouter()

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
  await fetchData()
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
