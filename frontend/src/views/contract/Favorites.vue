<template>
  <div class="favorites-page">
    <div class="page-header">
      <h1 class="page-title">{{ $t('contract.favorites') }}</h1>
    </div>
    
    <div class="favorites-grid" v-loading="loading">
      <div v-for="item in favorites" :key="item.id" class="favorite-card">
        <div class="card-header">
          <div class="contract-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="contract-info">
            <h3 class="contract-title">{{ item.title }}</h3>
            <span class="contract-no">{{ item.contractNo }}</span>
          </div>
          <el-icon class="favorite-icon active" @click="handleUnfavorite(item.id)">
            <StarFilled />
          </el-icon>
        </div>
        
        <div class="card-body">
          <div class="info-row">
            <span class="label">{{ $t('contract.type') }}:</span>
            <el-tag size="small">{{ formatType(item.type) }}</el-tag>
          </div>
          <div class="info-row">
            <span class="label">{{ $t('contract.status') }}:</span>
            <el-tag :type="getStatusType(item.status)" size="small">{{ formatStatus(item.status) }}</el-tag>
          </div>
          <div class="info-row">
            <span class="label">{{ $t('contract.amount') }}:</span>
            <span class="value">{{ getCurrencySymbol(item.currency) }}{{ formatAmount(item.amount) }}</span>
          </div>
          <div class="info-row">
            <span class="label">{{ $t('contract.endDate') }}:</span>
            <span class="value">{{ item.endDate || '-' }}</span>
          </div>
        </div>
        
        <div class="card-footer">
          <span class="favorited-time">{{ $t('contract.favoritedAt') }}: {{ formatDate(item.favoritedAt) }}</span>
          <el-button type="primary" size="small" text @click="$router.push(`/contracts/${item.id}`)">
            {{ $t('common.view') }}
          </el-button>
        </div>
      </div>
      
      <div v-if="favorites.length === 0 && !loading" class="empty-state">
        <el-empty :description="$t('contract.noFavorites')">
          <el-button type="primary" @click="$router.push('/contracts')">
            {{ $t('contract.gotoContracts') }}
          </el-button>
        </el-empty>
      </div>
    </div>
    
    <div class="pagination-wrap" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[12, 24, 48]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getFavorites, removeFavorite, addFavorite } from '@/api/favorite'
import { getContractCategories } from '@/api/contractCategory'
import { Document, Star, StarFilled } from '@element-plus/icons-vue'
import { formatAmountByLocale, getCurrencySymbol } from '@/utils/currency'

const { t, locale } = useI18n()

const loading = ref(false)
const favorites = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const categories = ref<any[]>([])

const formatAmount = (amount: number) => {
  return formatAmountByLocale(amount, locale.value)
}

const formatType = (type: string) => {
  const cat = categories.value.find(c => c.code === type)
  if (cat) return cat.name
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

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    DRAFT: 'info',
    PENDING: 'warning',
    APPROVING: 'warning',
    APPROVED: 'success',
    SIGNED: 'success',
    ARCHIVED: 'info',
    TERMINATED: 'danger'
  }
  return map[status] || 'info'
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return date.split('T')[0]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getFavorites({ page: currentPage.value, pageSize: pageSize.value })
    favorites.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error(t('common.error'))
  } finally {
    loading.value = false
  }
}

const handleUnfavorite = async (contractId: number) => {
  try {
    await removeFavorite(contractId)
    ElMessage.success(t('common.success'))
    favorites.value = favorites.value.filter(item => item.id !== contractId)
    total.value--
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const loadCategories = async () => {
  try {
    const res = await getContractCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
}

onMounted(async () => {
  await loadCategories()
  fetchData()
})
</script>

<style scoped lang="scss">
.favorites-page {
  .page-header {
    margin-bottom: 24px;
    min-width: 0;
    
    .page-title {
      font-size: 24px;
      font-weight: 600;
      background: var(--primary-gradient);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      margin: 0;
      max-width: 100%;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.favorite-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 16px;
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  overflow: hidden;
  min-height: 180px;
  display: flex;
  flex-direction: column;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: var(--primary-gradient);
    border-radius: 16px 16px 0 0;
  }

  &:hover {
    border-color: transparent;
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(102, 110, 234, 0.18);
    
    .contract-icon {
      transform: scale(1.08) rotate(-3deg);
    }
  }
  
  .card-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;
    flex-shrink: 0;
    
    .contract-icon {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      background: var(--primary-gradient);
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
      
      .el-icon {
        font-size: 20px;
        color: white;
      }
    }
    
    .contract-info {
      flex: 1;
      min-width: 0;
      
      .contract-title {
        font-size: 14px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0 0 2px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .contract-no {
        font-size: 11px;
        color: var(--text-secondary);
      }
    }
    
    .favorite-icon {
      font-size: 18px;
      color: #ccc;
      cursor: pointer;
      transition: all 0.2s;
      flex-shrink: 0;
      
      &:hover {
        transform: scale(1.2);
      }
      
      &.active {
        color: #fbbf24;
        
        &:hover {
          color: #f59e0b;
        }
      }
      
      &:active {
        transform: scale(0.9);
      }
    }
  }
  
  .card-body {
    flex: 1;
    
    .info-row {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 4px 0;
      font-size: 12px;
      
      .label {
        color: var(--text-secondary);
        min-width: 56px;
      }
      
      .value {
        color: var(--text-primary);
      }
    }
  }
  
  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 8px;
    padding-top: 10px;
    border-top: 1px solid var(--border-color);
    margin-top: 10px;
    flex-shrink: 0;
    
    .favorited-time {
      font-size: 11px;
      color: var(--text-secondary);
      max-width: 70%;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}

.empty-state {
  grid-column: 1 / -1;
  padding: 60px 0;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  min-width: 0;
}
</style>
