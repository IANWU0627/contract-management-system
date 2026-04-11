<template>
  <div class="change-log-page">
    <div class="page-header">
      <h1 class="page-title">{{ $t('contract.changeLog') }}</h1>
      <div class="header-actions">
        <el-button @click="fetchLogs">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>
    </div>
    
    <div class="filter-section">
      <div class="filter-row">
        <div class="filter-item filter-item-wide" style="flex: 1;">
          <el-input
            v-model="searchKeyword"
            :placeholder="$t('common.search') + '...'"
            clearable
            @input="handleSearch"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <div class="filter-item">
          <el-select v-model="searchType" :placeholder="$t('contract.changeType')" clearable style="width: 140px" @change="handleSearch">
            <el-option :label="$t('contract.changeTypes.CREATE')" value="CREATE" />
            <el-option :label="$t('contract.changeTypes.UPDATE')" value="UPDATE" />
            <el-option :label="$t('contract.changeTypes.DELETE')" value="DELETE" />
            <el-option :label="$t('contract.changeTypes.APPROVE')" value="APPROVE" />
            <el-option :label="$t('contract.changeTypes.REJECT')" value="REJECT" />
            <el-option :label="$t('contract.changeTypes.SIGN')" value="SIGN" />
            <el-option :label="$t('contract.changeTypes.RENEW')" value="RENEW" />
            <el-option :label="$t('contract.changeTypes.ARCHIVE')" value="ARCHIVE" />
            <el-option :label="$t('contract.changeTypes.TERMINATE')" value="TERMINATE" />
          </el-select>
        </div>
        <div class="filter-actions">
          <el-button type="primary" @click="handleSearch" class="gradient-btn">
            <el-icon><Search /></el-icon>
            {{ t('common.search') }}
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            {{ t('common.reset') }}
          </el-button>
        </div>
      </div>
    </div>
    
    <el-card class="log-card" v-loading="loading">
      <el-tabs v-model="activeTab">
        <el-tab-pane :label="$t('contract.recentChanges')" name="recent">
          <el-table :data="paginatedRecentLogs" style="width: 100%">
            <el-table-column prop="contract_no" :label="$t('contract.no')" width="150" />
            <el-table-column prop="contract_title" :label="$t('contract.name')" min-width="150" />
            <el-table-column prop="change_type" :label="$t('contract.changeType')" width="120">
              <template #default="{ row }">
                <el-tag :type="getChangeTypeTag(row.change_type)" size="small">
                  {{ formatChangeType(row.change_type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="field_name" :label="$t('contract.field')" width="120" />
            <el-table-column :label="$t('contract.changeContent')" min-width="200">
              <template #default="{ row }">
                <span v-if="row.old_value || row.new_value">
                  <span class="old-value">{{ row.old_value || '(empty)' }}</span>
                  <el-icon class="arrow"><Right /></el-icon>
                  <span class="new-value">{{ row.new_value || '(empty)' }}</span>
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="operator_name" :label="$t('log.operator')" width="100" />
            <el-table-column prop="created_at" :label="$t('contract.createdAt')" width="160">
              <template #default="{ row }">{{ formatDateTime(row.created_at) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane :label="$t('contract.myChanges')" name="my">
          <el-table :data="paginatedMyLogs" style="width: 100%">
            <el-table-column prop="contract_no" :label="$t('contract.no')" width="150" />
            <el-table-column prop="contract_title" :label="$t('contract.name')" min-width="150" />
            <el-table-column prop="change_type" :label="$t('contract.changeType')" width="120">
              <template #default="{ row }">
                <el-tag :type="getChangeTypeTag(row.change_type)" size="small">
                  {{ formatChangeType(row.change_type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="$t('contract.remark')" min-width="150" show-overflow-tooltip>
              <template #default="{ row }">
                {{ formatRemark(row.field_name, row.change_type) }}
              </template>
            </el-table-column>
            <el-table-column prop="created_at" :label="$t('contract.createdAt')" width="160">
              <template #default="{ row }">{{ formatDateTime(row.created_at) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
      
      <div v-if="filteredRecentLogs.length === 0 && filteredMyLogs.length === 0 && !loading" class="empty-state">
        <el-empty :description="$t('contract.noChangeLogs')" />
      </div>
      
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="activeTab === 'recent' ? filteredRecentLogs.length : filteredMyLogs.length"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getRecentChangeLogs, getMyChangeLogs } from '@/api/changeLog'
import { Refresh, Right, Search } from '@element-plus/icons-vue'

const { t } = useI18n()

const loading = ref(false)
const activeTab = ref('recent')
const recentLogs = ref<any[]>([])
const myLogs = ref<any[]>([])
const searchKeyword = ref('')
const searchType = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const filteredRecentLogs = computed(() => {
  return recentLogs.value.filter(log => {
    if (searchKeyword.value) {
      const kw = searchKeyword.value.toLowerCase()
      return (
        (log.contract_no?.toLowerCase().includes(kw) || '') ||
        (log.contract_title?.toLowerCase().includes(kw) || '') ||
        (log.field_name?.toLowerCase().includes(kw) || '') ||
        (log.operator_name?.toLowerCase().includes(kw) || '')
      )
    }
    if (searchType.value && log.change_type !== searchType.value) return false
    return true
  })
})

const filteredMyLogs = computed(() => {
  return myLogs.value.filter(log => {
    if (searchKeyword.value) {
      const kw = searchKeyword.value.toLowerCase()
      return (
        (log.contract_no?.toLowerCase().includes(kw) || '') ||
        (log.contract_title?.toLowerCase().includes(kw) || '') ||
        (log.remark?.toLowerCase().includes(kw) || '')
      )
    }
    return true
  })
})

const paginatedRecentLogs = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredRecentLogs.value.slice(start, end)
})

const paginatedMyLogs = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredMyLogs.value.slice(start, end)
})

const formatDateTime = (date: string) => {
  if (!date) return '-'
  return date.replace('T', ' ').substring(0, 16)
}

const formatChangeType = (type: string) => {
  const key = `contract.changeTypes.${type}`
  const translated = t(key)
  return translated !== key ? translated : type
}

const getChangeTypeTag = (type: string) => {
  const map: Record<string, string> = {
    CREATE: 'success',
    UPDATE: 'primary',
    DELETE: 'danger',
    APPROVE: 'success',
    REJECT: 'warning',
    SIGN: 'success',
    RENEW: 'info',
    ARCHIVE: 'info',
    TERMINATE: 'danger'
  }
  return map[type] || ''
}

const formatRemark = (fieldName: string, changeType: string) => {
  if (changeType === 'CREATE') {
    return t('contract.changeTypes.CREATE') + '合同'
  }
  
  const keyMap: Record<string, string> = {
    title: 'contract.changeRemark.titleChanged',
    type: 'contract.changeRemark.typeChanged',
    remark: 'contract.changeRemark.remarkChanged',
    counterparty: 'contract.changeRemark.counterpartyChanged',
    amount: 'contract.changeRemark.amountChanged',
    startDate: 'contract.changeRemark.startDateChanged',
    endDate: 'contract.changeRemark.endDateChanged',
    status: 'contract.changeRemark.statusChanged',
    content: 'contract.changeRemark.contentChanged'
  }
  
  const key = keyMap[fieldName]
  if (key) {
    const translated = t(key)
    if (translated !== key) return translated
  }
  
  return fieldName || '-'
}

const fetchRecentLogs = async () => {
  try {
    const res = await getRecentChangeLogs(50)
    recentLogs.value = res.data?.list || []
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const fetchMyLogs = async () => {
  try {
    const res = await getMyChangeLogs(50)
    myLogs.value = res.data?.list || []
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const fetchLogs = async () => {
  loading.value = true
  try {
    await Promise.all([fetchRecentLogs(), fetchMyLogs()])
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleReset = () => {
  searchKeyword.value = ''
  searchType.value = ''
  currentPage.value = 1
}

onMounted(() => {
  fetchLogs()
})
</script>

<style scoped lang="scss">
.change-log-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .page-title {
      font-size: 24px;
      font-weight: 600;
      background: var(--primary-gradient);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      margin: 0;
    }
  }
  
  .gradient-btn {
    background: var(--primary-gradient) !important;
    border: none !important;
  }
  
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
  
  .pagination-wrap {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}

.log-card {
  .empty-state {
    padding: 40px 0;
  }
  
  .old-value {
    color: var(--el-text-color-secondary);
    text-decoration: line-through;
  }
  
  .new-value {
    color: var(--el-color-success);
  }
  
  .arrow {
    margin: 0 8px;
    color: var(--el-color-primary);
  }
}
</style>
