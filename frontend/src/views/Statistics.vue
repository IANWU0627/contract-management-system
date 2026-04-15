<template>
  <div class="statistics">
    <div class="page-header">
      <h1 class="page-title">{{ $t('statistics.title') }}</h1>
      <el-button type="primary" @click="handleExport">
        <el-icon><Download /></el-icon>
        {{ $t('statistics.export') }}
      </el-button>
    </div>
    
    <!-- 概览卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);"><el-icon><Document /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalContracts }}</div>
            <div class="stat-label">{{ $t('statistics.totalContracts') }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);"><el-icon><Money /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ getCurrencySymbol(DEFAULT_CURRENCY) }}{{ formatAmount(stats.totalAmount) }}</div>
            <div class="stat-label">{{ $t('statistics.totalAmount') }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);"><el-icon><Clock /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.pendingApproval }}</div>
            <div class="stat-label">{{ $t('statistics.pendingApproval') }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);"><el-icon><Warning /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.expiringSoon }}</div>
            <div class="stat-label">{{ $t('statistics.expiringSoon') }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 图表 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header><span>{{ $t('statistics.byType') }}</span></template>
          <div ref="typeChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header><span>{{ $t('statistics.byStatus') }}</span></template>
          <div ref="statusChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { get, download } from '@/api'
import { getContractCategories } from '@/api/contractCategory'
import { ElMessage } from 'element-plus'
import { Document, Money, Clock, Warning, Download } from '@element-plus/icons-vue'
import { Pie, Bar } from '@antv/g2plot'
import { DEFAULT_CURRENCY, formatAmountByLocale, getCurrencySymbol } from '@/utils/currency'

const { t, locale } = useI18n()

const stats = ref({ totalContracts: 0, totalAmount: 0, pendingApproval: 0, expiringSoon: 0 })
const typeChartRef = ref<HTMLElement>()
const statusChartRef = ref<HTMLElement>()
let typeChart: Pie | Bar | null = null
let statusChart: Pie | null = null
let cachedTypeData: any[] = []
let cachedStatusData: any[] = []
let contractCategories: any[] = []

const formatAmount = (amount: number) => {
  return formatAmountByLocale(amount, locale.value)
}

const getTypeNameMap = () => {
  const map: Record<string, string> = {}
  for (const cat of contractCategories) {
    map[cat.code] = locale.value === 'en' && cat.nameEn ? cat.nameEn : (cat.name || '')
  }
  return map
}

const getChartThemeTokens = () => {
  const css = getComputedStyle(document.documentElement)
  const textPrimary = (css.getPropertyValue('--text-primary') || '#1a1a1a').trim()
  const textSecondary = (css.getPropertyValue('--text-secondary') || '#666666').trim()
  const isDark = document.documentElement.classList.contains('dark')
  return {
    textPrimary,
    textSecondary,
    stroke: isDark ? 'rgba(148,163,184,0.4)' : 'rgba(255,255,255,0.6)',
  }
}

watch(locale, () => {
  nextTick(() => {
    if (cachedTypeData.length > 0 || cachedStatusData.length > 0) {
      renderChartsWithCache()
    }
  })
})

const renderChartsWithCache = () => {
  const theme = getChartThemeTokens()
  const typeNameMap = getTypeNameMap()
  const statusNames: Record<string, string> = {
    'DRAFT': t('contract.statuses.draft'),
    'PENDING': t('contract.statuses.pending'),
    'APPROVING': t('contract.statuses.approving'),
    'APPROVED': t('contract.statuses.approved'),
    'SIGNED': t('contract.statuses.signed'),
    'ARCHIVED': t('contract.statuses.archived'),
    'TERMINATED': t('contract.statuses.terminated'),
    'OTHER': t('contract.statuses.draft'),
  }

  if (typeChartRef.value && cachedTypeData.length > 0) {
    typeChart?.destroy()
    const typeData = cachedTypeData.map((item: any) => ({
      type: typeNameMap[item.code] || item.code,
      value: item.value
    }))
    const typeTotal = typeData.reduce((sum: number, item: any) => sum + item.value, 0)
    
    typeChart = new Bar(typeChartRef.value, {
      data: typeData,
      xField: 'value',
      yField: 'type',
      seriesField: 'type',
      legend: false,
      label: {
        position: 'right' as const,
        style: { fontSize: 11 },
      },
      color: ['#667eea', '#34d399', '#f87171', '#fbbf24', '#38bdf8', '#a78bfa', '#ff6b9d', '#c4b5fd'],
      barStyle: { radius: [4, 4, 0, 0] },
      interactions: [{ type: 'element-active' }],
    })
    typeChart.render()
  }

  if (statusChartRef.value && cachedStatusData.length > 0) {
    statusChart?.destroy()
    const statusData = cachedStatusData.map((item: any) => ({
      name: statusNames[item.code] || item.code,
      value: item.value
    }))
    const statusTotal = statusData.reduce((sum: number, item: any) => sum + item.value, 0)
    
    statusChart = new Pie(statusChartRef.value, {
      data: statusData,
      angleField: 'value',
      colorField: 'name',
      radius: 0.85,
      innerRadius: 0.65,
      label: {
        type: 'spider',
        content: '{name}\n{percentage}',
        style: { fontSize: 10, fontWeight: 500 },
      },
      legend: {
        position: 'right' as const,
        itemWidth: 80,
        itemName: { style: { fontSize: 11 } },
        itemValue: { style: { fontSize: 11, fill: theme.textSecondary } },
      },
      color: ['#f87171', '#fbbf24', '#60a5fa', '#34d399', '#818cf8', '#94a3b8'],
      pieStyle: { stroke: theme.stroke, lineWidth: 3 },
      statistic: {
        title: { content: t('statistics.total'), style: { fontSize: '12px', color: theme.textSecondary } },
        content: { content: String(statusTotal), style: { fontSize: '24px', fontWeight: 'bold', color: theme.textPrimary } },
      },
      interactions: [{ type: 'element-active' }],
    })
    statusChart.render()
  }
}

const initCharts = async () => {
  const theme = getChartThemeTokens()
  await new Promise(resolve => setTimeout(resolve, 100))

  try {
    const catRes = await getContractCategories()
    contractCategories = catRes.data || []
    const typeNameMap = getTypeNameMap()
    
    const typeRes = await get('/statistics/type-distribution')
    cachedTypeData = typeRes.data || []
    const typeData = cachedTypeData.map((item: any) => ({
      type: typeNameMap[item.code] || item.code,
      value: item.value
    }))

    if (typeChartRef.value) {
      typeChart?.destroy()
      if (typeData.length > 0) {
        typeChart = new Bar(typeChartRef.value, {
          data: typeData,
          xField: 'value',
          yField: 'type',
          seriesField: 'type',
          legend: false,
          label: {
            position: 'right' as const,
            style: { fontSize: 11 },
          },
          color: ['#667eea', '#34d399', '#f87171', '#fbbf24', '#38bdf8', '#a78bfa', '#ff6b9d', '#c4b5fd'],
          barStyle: { radius: [4, 4, 0, 0] },
          interactions: [{ type: 'element-active' }],
        })
        typeChart.render()
      }
    }
  } catch (error) {}

  try {
    const statusRes = await get('/statistics/status-distribution')
    cachedStatusData = statusRes.data || []
    const statusNames: Record<string, string> = {
      'DRAFT': t('contract.statuses.draft'),
      'PENDING': t('contract.statuses.pending'),
      'APPROVING': t('contract.statuses.approving'),
      'APPROVED': t('contract.statuses.approved'),
      'SIGNED': t('contract.statuses.signed'),
      'ARCHIVED': t('contract.statuses.archived'),
      'TERMINATED': t('contract.statuses.terminated'),
      'OTHER': t('contract.statuses.draft'),
    }
    const statusData = cachedStatusData.map((item: any) => ({
      name: statusNames[item.code] || item.code,
      value: item.value
    }))
    const statusTotal = statusData.reduce((sum: number, item: any) => sum + item.value, 0)

    if (statusChartRef.value) {
      statusChart?.destroy()
      if (statusData.length > 0) {
        statusChart = new Pie(statusChartRef.value, {
          data: statusData,
          angleField: 'value',
          colorField: 'name',
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
            itemName: {
              style: { fontSize: 11 }
            },
            itemValue: {
              style: { fontSize: 11, fill: theme.textSecondary }
            },
          },
          color: ['#f87171', '#fbbf24', '#60a5fa', '#34d399', '#818cf8', '#94a3b8'],
          pieStyle: {
            stroke: theme.stroke,
            lineWidth: 3,
          },
          statistic: {
            title: { 
              content: t('statistics.total'),
              style: { fontSize: '12px', color: theme.textSecondary }
            },
            content: { 
              content: String(statusTotal),
              style: { fontSize: '24px', fontWeight: 'bold', color: theme.textPrimary }
            },
          },
          interactions: [{ type: 'element-active' }],
        })
        statusChart.render()
      }
    }
  } catch (error) {}
}

const fetchData = async () => {
  try {
    const res = await get('/statistics/overview')
    stats.value = res.data || stats.value
  } catch (error) {}
}

const handleExport = async () => {
  try {
    download('/contracts/export', `contracts_${new Date().toISOString().split('T')[0]}.xlsx`)
    ElMessage.success(t('common.exportSuccess'))
  } catch (error: any) {
    ElMessage.error(error.message || t('common.error'))
  }
}

onMounted(async () => {
  await fetchData()
  await initCharts()
})

onUnmounted(() => {
  typeChart?.destroy()
  statusChart?.destroy()
})
</script>

<style scoped>
.statistics {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  padding: 20px;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.chart-card {
  margin-bottom: 16px;
  border-radius: 8px;
}

.chart-card :deep(.el-card__header) {
  font-weight: 600;
  font-size: 13px;
  padding: 10px 14px;
  background: linear-gradient(135deg, #667eea10, #764ba210);
  border-bottom: 1px solid var(--border-color);
}

.chart-container {
  width: 100%;
  height: 310px;
  padding: 8px;
}
</style>
