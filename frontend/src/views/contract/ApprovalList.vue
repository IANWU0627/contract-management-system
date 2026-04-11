<template>
  <div class="approval-list">
    <div class="page-header">
      <h1 class="page-title">{{ $t('menu.approvals') }}</h1>
    </div>
    
    <!-- 搜索筛选 -->
    <div class="filter-section">
      <div class="filter-row">
        <div class="filter-item filter-item-wide" style="flex: 1;">
          <el-input
            v-model="query.keyword"
            :placeholder="$t('contract.placeholder.keyword')"
            clearable
            @keyup.enter="fetchData"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <div class="filter-item" style="width: 160px;">
          <el-select v-model="query.status" clearable :placeholder="$t('contract.status')" @change="fetchData">
            <el-option :label="$t('contract.statuses.pending')" value="PENDING" />
            <el-option :label="$t('contract.statuses.approved')" value="APPROVED" />
            <el-option :label="$t('contract.statuses.rejected')" value="REJECTED" />
          </el-select>
        </div>
        <div class="filter-actions">
          <el-button type="primary" @click="fetchData" class="gradient-btn">
            <el-icon><Search /></el-icon>
            {{ $t('common.search') }}
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            {{ $t('common.reset') }}
          </el-button>
        </div>
      </div>
    </div>
    
    <el-card shadow="hover">
      <el-table :data="approvals" v-loading="loading" style="width: 100%">
        <el-table-column prop="contractNo" :label="$t('contract.no')" width="150" show-overflow-tooltip />
        <el-table-column prop="title" :label="$t('contract.name')" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link type="primary" @click="$router.push(`/contracts/${row.id}`)" class="table-title-link">
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="type" :label="$t('contract.type')" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag size="small">{{ formatType(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="counterparty" :label="$t('contract.party')" width="130" show-overflow-tooltip />
        <el-table-column prop="amount" :label="$t('contract.amount')" width="120" show-overflow-tooltip>
          <template #default="{ row }">
            ¥{{ formatAmount(row.amount) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('contract.status')" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag type="warning" size="small">{{ formatStatus(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="$t('contract.createdAt')" width="160" show-overflow-tooltip />
        <el-table-column :label="$t('common.action')" width="100" fixed="right">
          <template #default="{ row }">
            <el-dropdown @command="(command) => handleAction(command, row)">
              <el-button size="small">
                {{ $t('common.action') }}
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="view">{{ $t('common.view') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="fetchData"
          @size-change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { get } from '@/api'
import { getContractCategories } from '@/api/contractCategory'
import { Checked, ArrowDown, Search, Refresh } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const { t } = useI18n()
const router = useRouter()

const handleAction = (command: string, row: any) => {
  switch (command) {
    case 'view':
      router.push(`/contracts/${row.id}`)
      break
  }
}

const loading = ref(false)
const approvals = ref<any[]>([])
const total = ref(0)
const categories = ref<any[]>([])

const query = reactive({
  page: 1,
  pageSize: 10,
  status: 'PENDING',
  keyword: ''
})

const handleReset = () => {
  query.keyword = ''
  query.status = 'PENDING'
  query.page = 1
  fetchData()
}

const formatAmount = (amount: number) => amount ? new Intl.NumberFormat('zh-CN').format(amount) : '0'

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
    PENDING: t('contract.statuses.pending'),
    APPROVING: t('contract.statuses.approving')
  }
  return map[status] || status
}

const loadCategories = async () => {
  try {
    const res = await getContractCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await get('/contracts', { params: { ...query, status: 'PENDING,APPROVING' } })
    approvals.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (error) { } 
  finally { loading.value = false }
}

onMounted(async () => {
  await loadCategories()
  fetchData()
})
</script>

<style scoped lang="scss">
.gradient-btn {
  background: var(--primary-gradient) !important;
  border: none !important;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
  }
  
  .el-icon {
    margin-right: 4px;
  }
}

/* 筛选区 */
.filter-section {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 24px;
}

.filter-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.filter-item-wide {
  flex: 1;
  
  :deep(.el-input__wrapper) {
    border-radius: 8px;
  }
}

.filter-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.approval-list {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .page-title {
      font-size: 24px;
      font-weight: 700;
      margin: 0;
      background: var(--primary-gradient);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
  }
  
  .pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
}
</style>
