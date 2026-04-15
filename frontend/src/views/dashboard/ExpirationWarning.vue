<template>
  <div class="expiration-dashboard">
    <div class="page-header">
      <h1 class="page-title">{{ $t('dashboard.expirationWarning') }}</h1>
      <div class="header-actions">
        <el-select v-model="daysFilter" style="width: 150px">
          <el-option :label="$t('dashboard.expiring7Days')" :value="7" />
          <el-option :label="$t('dashboard.expiring30Days')" :value="30" />
          <el-option :label="$t('dashboard.expiring90Days')" :value="90" />
        </el-select>
        <el-select v-model="typeFilter" style="width: 120px" clearable :placeholder="$t('contract.type')">
          <el-option :label="$t('contract.types.purchase')" value="PURCHASE" />
          <el-option :label="$t('contract.types.sales')" value="SALES" />
          <el-option :label="$t('contract.types.service')" value="SERVICE" />
          <el-option :label="$t('contract.types.lease')" value="LEASE" />
          <el-option :label="$t('contract.types.employment')" value="EMPLOYMENT" />
        </el-select>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card expired" shadow="hover">
          <div class="stat-icon">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.expired }}</div>
            <div class="stat-label">{{ $t('dashboard.expired') }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card today" shadow="hover">
          <div class="stat-icon">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.today }}</div>
            <div class="stat-label">{{ $t('dashboard.expiringToday') }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card urgent" shadow="hover">
          <div class="stat-icon">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.urgent }}</div>
            <div class="stat-label">{{ $t('dashboard.expiringSoon') }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card warning" shadow="hover">
          <div class="stat-icon">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.warning }}</div>
            <div class="stat-label">{{ $t('dashboard.expiringMonth') }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 合同列表 -->
    <el-card shadow="hover" class="list-card">
      <template #header>
        <div class="card-header">
          <span>{{ $t('dashboard.expiringContracts') }}</span>
          <span class="contract-count">{{ $t('common.totalOnly', { count: contracts.length }) }}</span>
        </div>
      </template>
      
      <el-table :data="contracts" v-loading="loading" stripe style="width: 100%">
        <el-table-column :label="$t('contract.no')" prop="contractNo" width="180" />
        <el-table-column :label="$t('contract.name')" prop="title" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="$router.push(`/contracts/${row.id}`)">
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column :label="$t('contract.type')" prop="type" width="100">
          <template #default="{ row }">
            <el-tag>{{ formatType(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('contract.amount')" prop="amount" width="120">
          <template #default="{ row }">
            {{ getCurrencySymbol(row.currency) }}{{ formatAmount(row.amount) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('contract.endDate')" prop="endDate" width="120" />
        <el-table-column :label="$t('dashboard.daysRemaining')" prop="daysRemaining" width="120">
          <template #default="{ row }">
            <el-tag :type="getExpireTagType(row.expireStatus)">
              {{ row.expireStatusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.action')" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="$router.push(`/contracts/${row.id}`)">
              <el-icon><View /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="!loading && contracts.length === 0" :description="$t('dashboard.noExpiringContracts')" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { getExpiringContracts } from '@/api/contract'
import { Warning, Clock, Timer, Calendar, View } from '@element-plus/icons-vue'
import { formatAmountByLocale, getCurrencySymbol } from '@/utils/currency'

const { t, locale } = useI18n()
const router = useRouter()

const loading = ref(false)
const contracts = ref<any[]>([])
const stats = ref({
  expired: 0,
  today: 0,
  urgent: 0,
  warning: 0
})
const daysFilter = ref(30)
const typeFilter = ref('')

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getExpiringContracts({
      days: daysFilter.value,
      type: typeFilter.value || undefined
    })
    contracts.value = res.data?.list || []
    stats.value = res.data?.stats || { expired: 0, today: 0, urgent: 0, warning: 0 }
  } catch (error) {
    console.error('Failed to fetch expiring contracts:', error)
    contracts.value = []
  } finally {
    loading.value = false
  }
}

const formatType = (type: string) => {
  const map: Record<string, string> = {
    PURCHASE: t('contract.types.purchase'),
    SALES: t('contract.types.sales'),
    SERVICE: t('contract.types.service'),
    LEASE: t('contract.types.lease'),
    EMPLOYMENT: t('contract.types.employment'),
    OTHER: t('contract.types.other')
  }
  return map[type] || type
}

const formatAmount = (amount: number) => {
  return formatAmountByLocale(amount, locale.value)
}

const getExpireTagType = (status: string) => {
  const map: Record<string, string> = {
    expired: 'danger',
    today: 'danger',
    urgent: 'warning',
    warning: 'info',
    normal: 'success'
  }
  return map[status] || 'info'
}

watch([daysFilter, typeFilter], () => {
  fetchData()
})

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.expiration-dashboard {
  .page-title {
    font-size: 24px;
    font-weight: 700;
    margin: 0;
    background: var(--primary-gradient);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
  
  .stats-row {
    margin-bottom: 20px;
  }
  
  .stat-card {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28px;
      color: white;
    }
    
    &.expired .stat-icon { background: linear-gradient(135deg, #f56c6c, #e04040); }
    &.today .stat-icon { background: linear-gradient(135deg, #e6a23c, #f56c6c); }
    &.urgent .stat-icon { background: linear-gradient(135deg, #e6a23c, #fbb64d); }
    &.warning .stat-icon { background: linear-gradient(135deg, #409eff, #66b1ff); }
    
    .stat-content {
      .stat-value {
        font-size: 28px;
        font-weight: bold;
        color: var(--text-primary);
      }
      
      .stat-label {
        font-size: 14px;
        color: var(--text-secondary);
        margin-top: 4px;
      }
    }
  }
  
  .list-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .contract-count {
        font-size: 14px;
        color: var(--text-secondary);
      }
    }
  }
}
</style>
