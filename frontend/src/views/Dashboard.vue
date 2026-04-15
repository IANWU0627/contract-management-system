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
          <div class="stat-trend up">+12%</div>
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
          <div class="stat-trend up">+8.5%</div>
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
          <div class="stat-badge danger" v-if="stats.expiringSoon > 0">{{ stats.expiringSoon }}</div>
        </div>
      </template>
    </div>
    
    <!-- 第二行：左侧最近合同 + 右侧快捷方式和分析 -->
    <el-row :gutter="20" class="main-row">
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
                  style="width: 280px"
                >
                  <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
                <el-button size="small" @click="clearSearch" v-if="searchKeyword" link>{{ t('common.reset') }}</el-button>
              </div>
              <el-button type="primary" size="small" @click="$router.push('/contracts')">{{ t('common.viewAll') }}</el-button>
            </div>
          </template>
          
          <el-table :data="filteredContracts" style="width: 100%" size="small" v-loading="searchLoading" class="dashboard-table">
            <el-table-column prop="contractNo" :label="t('contract.no')" width="120" show-overflow-tooltip />
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
              style="margin-top: 24px; padding-top: 16px; border-top: 1px solid var(--border-color); text-align: right"
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
              <span>{{ t('contract.create') }}</span>
            </div>
            <div class="quick-item" @click="$router.push('/templates')">
              <div class="quick-icon green">
                <el-icon :size="18"><FolderOpened /></el-icon>
              </div>
              <span>{{ t('template.title') }}</span>
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
            <div class="card-header">
              <el-icon><DataAnalysis /></el-icon>
              <span>{{ t('statistics.title') }}</span>
            </div>
          </template>
          <div class="charts-wrap">
            <div class="chart-item">
              <div class="chart-title">{{ t('statistics.byStatus') }}</div>
              <div ref="statusChartRef" class="chart-container"></div>
            </div>
            <div class="chart-item">
              <div class="chart-title">{{ t('statistics.byType') }}</div>
              <div ref="typeChartRef" class="chart-container"></div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { get, put, post, del } from '@/api'
import { getContractList } from '@/api/contract'
import { getContractCategories } from '@/api/contractCategory'
import { addFavorite, removeFavorite, getFavorites } from '@/api/favorite'
import { Pie, Bar } from '@antv/g2plot'
import { Lightning, DataAnalysis, Search, Star, StarFilled, Document, Money, Timer, Bell, Plus, FolderOpened, RefreshRight } from '@element-plus/icons-vue'
import SkeletonLoader from '@/components/SkeletonLoader.vue'
import { DEFAULT_CURRENCY, formatAmountByLocale, getCurrencySymbol } from '@/utils/currency'

const { t, locale } = useI18n()

const loading = ref(true)

const stats = ref({
  totalContracts: 0,
  totalAmount: 0,
  pendingApproval: 0,
  expiringSoon: 0
})

const pendingCount = ref(0)
const expireCount = ref(0)
const recentContracts = ref<any[]>([])
const filteredContracts = ref<any[]>([])
const categories = ref<any[]>([])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)

// 搜索相关
const searchKeyword = ref('')

// 缓存数据
let cachedContracts: any[] = []

watch(locale, () => {
  nextTick(() => {
    if (cachedContracts.length > 0) {
      initCharts(cachedContracts)
    }
  })
})
const searchLoading = ref(false)

const statusChartRef = ref<HTMLElement>()
const typeChartRef = ref<HTMLElement>()
let statusChart: Pie | null = null
let typeChart: Bar | null = null

const formatAmount = (amount: number) => {
  return formatAmountByLocale(amount, locale.value)
}

const formatType = (type: string) => {
  const category = categories.value.find((c: any) => c.code === type)
  if (category) {
    return category.name
  }
  const map: Record<string, string> = { 
    PURCHASE: t('contract.types.purchase'), 
    SALES: t('contract.types.sales'), 
    LEASE: t('contract.types.lease'), 
    EMPLOYMENT: t('contract.types.employment'), 
    SERVICE: t('contract.types.service'), 
    OTHER: t('contract.types.other') 
  }
  return map[type] || type
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
  const isDark = document.documentElement.classList.contains('dark')
  const pieStroke = isDark ? 'rgba(148,163,184,0.4)' : 'rgba(255,255,255,0.6)'
  
  const statusDataFormatted = statusData.map(item => ({
    type: item.name,
    value: item.value
  }))
  
  const typeLabels = [t('contract.types.purchase'), t('contract.types.sales'), t('contract.types.service'), t('contract.types.lease'), t('contract.types.employment'), t('contract.types.other')]
  
  const statusTotal = statusDataFormatted.reduce((sum: number, item: any) => sum + item.value, 0)
  
  if (statusChartRef.value) {
    statusChart?.destroy()
    statusChart = new Pie(statusChartRef.value, {
      data: statusDataFormatted,
      angleField: 'value',
      colorField: 'type',
      radius: 0.85,
      innerRadius: 0.65,
      label: {
        type: 'spider',
        content: '{name}\n{percentage}',
        style: {
          fontSize: 9,
          fontWeight: 500,
        },
      },
      legend: {
        position: 'right' as const,
        itemWidth: 80,
        itemName: {
          style: { fontSize: 10 }
        },
        itemValue: {
          style: { fontSize: 10, fill: textSecondary }
        },
      },
      color: ['#f87171', '#fbbf24', '#60a5fa', '#34d399', '#818cf8', '#94a3b8'],
      pieStyle: {
        stroke: pieStroke,
        lineWidth: 3,
      },
      statistic: {
        title: { content: t('statistics.total'), style: { fontSize: '10px', color: textSecondary } },
        content: { content: String(statusTotal), style: { fontSize: '18px', fontWeight: 'bold', color: textPrimary } },
      },
      animation: {
        appear: { animation: 'wave-in', duration: 1000 },
      },
      interactions: [{ type: 'element-active' }],
    })
    statusChart.render()
  }
  
  if (typeChartRef.value) {
    typeChart?.destroy()
    const pieData = typeData.map((item: any, index: number) => ({
      type: typeLabels[index],
      value: item.value
    })).filter(item => item.value > 0)
    const typeTotal = pieData.reduce((sum: number, item: any) => sum + item.value, 0)
    
    typeChart = new (Pie as any)(typeChartRef.value, {
      data: pieData,
      angleField: 'value',
      colorField: 'type',
      radius: 0.85,
      innerRadius: 0.65,
      label: {
        type: 'spider',
        content: '{name}\n{percentage}',
        style: {
          fontSize: 10,
          fontWeight: 500,
        },
      },
      legend: {
        position: 'right' as const,
        itemWidth: 80,
        itemName: { style: { fontSize: 10 } },
        itemValue: { style: { fontSize: 10, fill: textSecondary } },
      },
      color: ['#667eea', '#34d399', '#f87171', '#fbbf24', '#38bdf8', '#a78bfa'],
      pieStyle: {
        stroke: pieStroke,
        lineWidth: 3,
      },
      statistic: {
        title: { content: t('statistics.total'), style: { fontSize: '12px', color: textSecondary } },
        content: { content: String(typeTotal), style: { fontSize: '24px', fontWeight: 'bold', color: textPrimary } },
      },
      animation: {
        appear: { animation: 'wave-in', duration: 1000 },
      },
      interactions: [{ type: 'element-active' }],
    })
    typeChart.render()
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
    pendingCount.value = pendingApprovalCount
    
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
    expireCount.value = expiringSoonCount
    
    // Initialize charts with real data
    initCharts(allContracts)
  } catch (error) {
    // ignore
  } finally {
    loading.value = false
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

onMounted(async () => {
  await fetchData()
})

onUnmounted(() => {
  statusChart?.destroy()
  typeChart?.destroy()
})
</script>

<style scoped lang="scss">
.dashboard {
  padding: 0;
  
  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 24px;
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
  gap: 20px;
  margin-bottom: 24px;
  
  .stat-card-skeleton {
    background: var(--bg-card);
    border: 1px solid var(--border-color);
    border-radius: var(--radius);
    padding: 16px;
  }
  
  .stat-card {
    border-radius: var(--radius);
    padding: 16px;
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
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
      width: 50px;
      height: 50px;
      opacity: 0.08;
      z-index: 0;
      
      svg {
        width: 100%;
        height: 100%;
        color: #fff;
      }
    }
    
    .stat-icon {
      width: 32px;
      height: 32px;
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
        width: 16px;
        height: 16px;
        color: #fff;
      }
    }
    
    .stat-info {
      position: relative;
      z-index: 2;
      flex: 1;
      
      .stat-value {
        font-size: 22px;
        font-weight: 700;
        color: #fff;
        line-height: 1.2;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }
      
      .stat-label {
        font-size: 12px;
        color: rgba(255, 255, 255, 0.85);
        margin-top: 2px;
        font-weight: 500;
      }
    }
    
    .stat-trend {
      position: relative;
      z-index: 2;
      font-size: 12px;
      font-weight: 600;
      padding: 4px 10px;
      border-radius: 20px;
      background: rgba(255, 255, 255, 0.25);
      backdrop-filter: blur(10px);
      color: #fff;
      
      &.up {
        background: rgba(255, 255, 255, 0.25);
      }
      
      &.down {
        background: rgba(255, 255, 255, 0.25);
      }
    }
    
    .stat-badge {
      position: absolute;
      top: 16px;
      right: 16px;
      min-width: 24px;
      height: 24px;
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
  .recent-card {
    height: 100%;
  }
  
  .quick-card, .chart-card {
    .card-header {
      display: flex;
      align-items: center;
      gap: 6px;
      
      .el-icon {
        font-size: 16px;
        padding: 4px;
        border-radius: 8px;
        
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
      flex-wrap: wrap;
      min-width: 0;
      
      .header-left {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-wrap: wrap;
        min-width: 0;
        
        .title {
          font-size: 15px;
          font-weight: 600;
          color: var(--text-primary);
          margin-right: 4px;
          max-width: 180px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }
    }
  }
  
  .quick-card {
    margin-bottom: 16px;
  }
}

/* 快捷方式 */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  
  .quick-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
    padding: 20px 12px 16px;
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
      width: 48px;
      height: 48px;
      border-radius: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      transition: transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
      
      svg {
        width: 22px;
        height: 22px;
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
      font-size: 12px;
      font-weight: 500;
      color: var(--text-secondary);
      text-align: center;
      line-height: 1.4;
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
  gap: 16px;
  
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
      margin-bottom: 8px;
      word-break: break-word;
      line-height: 1.4;
    }
    
    .chart-container {
      height: 180px;
      background: var(--bg-hover);
      border-radius: var(--radius);
      padding: 8px;
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

/* 搜索栏 */
.search-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  padding-bottom: 12px;
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
  }
  
  .title-link {
    display: inline-block;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>
