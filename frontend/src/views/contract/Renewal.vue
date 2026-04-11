<template>
  <div class="renewal-page">
    <div class="page-header">
      <h1 class="page-title">{{ $t('contract.renewal') }}</h1>
      <el-button type="primary" class="gradient-btn" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        {{ $t('contract.renewContract') }}
      </el-button>
    </div>
    
    <!-- 搜索筛选 -->
    <div class="filter-section">
      <div class="filter-row">
        <div class="filter-item filter-item-wide" style="flex: 1;">
          <el-input
            v-model="query.keyword"
            :placeholder="$t('contract.placeholder.keyword')"
            clearable
            @keyup.enter="fetchRenewals"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <div class="filter-item" style="width: 160px;">
          <el-select v-model="query.status" clearable :placeholder="$t('contract.status')" @change="fetchRenewals">
            <el-option label="全部" value="" />
            <el-option :label="$t('contract.statuses.pending')" value="PENDING" />
            <el-option :label="$t('contract.statuses.approved')" value="APPROVED" />
            <el-option :label="$t('contract.statuses.rejected')" value="REJECTED" />
          </el-select>
        </div>
        <div class="filter-actions">
          <el-button type="primary" @click="fetchRenewals" class="gradient-btn">
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
    
    <el-card class="renewal-card" v-loading="loading">
      <el-table :data="renewals" style="width: 100%">
        <el-table-column prop="contractNo" :label="$t('contract.no')" width="160" show-overflow-tooltip />
        <el-table-column prop="title" :label="$t('contract.name')" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link type="primary" @click="$router.push(`/contracts/${row.contractId}`)" class="table-title-link">
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="oldEndDate" :label="$t('contract.oldEndDate')" width="110" show-overflow-tooltip>
          <template #default="{ row }">{{ formatDate(row.oldEndDate) }}</template>
        </el-table-column>
        <el-table-column prop="newEndDate" :label="$t('contract.newEndDate')" width="110" show-overflow-tooltip>
          <template #default="{ row }">{{ formatDate(row.newEndDate) }}</template>
        </el-table-column>
        <el-table-column prop="renewalType" :label="$t('contract.renewalType')" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag :type="getRenewalTypeTag(row.renewalType)" size="small">
              {{ formatRenewalType(row.renewalType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('contract.status')" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" :label="$t('log.operator')" width="100" show-overflow-tooltip />
        <el-table-column prop="remark" :label="$t('contract.remark')" min-width="130" show-overflow-tooltip />
        <el-table-column prop="createdAt" :label="$t('contract.createdAt')" width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column :label="$t('common.action')" width="100" fixed="right">
          <template #default="{ row }">
            <el-dropdown @command="(command) => handleAction(command, row)">
              <el-button size="small">
                {{ $t('common.action') }}
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="approve" v-if="row.status === 'PENDING'">
                    <el-icon><Check /></el-icon>
                    {{ $t('common.approve') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="reject" v-if="row.status === 'PENDING'" type="danger">
                    <el-icon><Close /></el-icon>
                    {{ $t('common.reject') }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      
      <div v-if="renewals.length === 0 && !loading" class="empty-state">
        <el-empty :description="$t('contract.noRenewals')" />
      </div>
      
      <div class="pagination-wrap" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchRenewals"
          @current-change="fetchRenewals"
        />
      </div>
    </el-card>
    
    <el-dialog v-model="showCreateDialog" :title="$t('contract.renewContract')" width="500px">
      <el-form :model="renewalForm" label-width="100px">
        <el-form-item :label="$t('contract.name')" required>
          <el-select v-model="renewalForm.contractId" :placeholder="$t('contract.selectContract')" filterable>
            <el-option 
              v-for="contract in availableContracts" 
              :key="contract.id" 
              :label="`${contract.contractNo} - ${contract.title}`" 
              :value="contract.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('contract.oldEndDate')">
          <el-input :value="selectedContractOldDate" disabled />
        </el-form-item>
        <el-form-item :label="$t('contract.newEndDate')" required>
          <el-date-picker
            v-model="renewalForm.newEndDate"
            type="date"
            :placeholder="$t('contract.selectNewDate')"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item :label="$t('contract.renewalType')">
          <el-select v-model="renewalForm.renewalType" style="width: 100%">
            <el-option :label="$t('contract.renewalTypes.extend')" value="EXTEND" />
            <el-option :label="$t('contract.renewalTypes.update')" value="UPDATE" />
            <el-option :label="$t('contract.renewalTypes.replace')" value="REPLACE" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('contract.remark')">
          <el-input v-model="renewalForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleCreate">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="showRejectDialog" :title="$t('contract.reject')" width="400px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item :label="$t('contract.rejectReason')">
          <el-input v-model="rejectForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRejectDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="danger" @click="confirmReject">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllRenewals, createRenewal, approveRenewal, rejectRenewal } from '@/api/renewal'
import { getContractList } from '@/api/contract'
import { Plus, Check, Close, ArrowDown, Search, Refresh } from '@element-plus/icons-vue'

const { t } = useI18n()

const loading = ref(false)
const renewals = ref<any[]>([])
const availableContracts = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const query = reactive({
  keyword: '',
  status: ''
})
const showCreateDialog = ref(false)
const showRejectDialog = ref(false)
const currentRenewal = ref<any>(null)

const renewalForm = reactive({
  contractId: undefined as number | undefined,
  newEndDate: '',
  renewalType: 'EXTEND',
  remark: ''
})

const rejectForm = reactive({
  remark: ''
})

const selectedContractOldDate = computed(() => {
  if (!renewalForm.contractId) return ''
  const contract = availableContracts.value.find(c => c.id === renewalForm.contractId)
  return contract?.endDate || ''
})

const formatDate = (date: string) => {
  if (!date) return '-'
  return date.split('T')[0]
}

const formatDateTime = (date: string) => {
  if (!date) return '-'
  return date.replace('T', ' ').substring(0, 16)
}

const formatRenewalType = (type: string) => {
  const map: Record<string, string> = {
    EXTEND: t('contract.renewalTypes.extend'),
    UPDATE: t('contract.renewalTypes.update'),
    REPLACE: t('contract.renewalTypes.replace')
  }
  return map[type] || type
}

const formatStatus = (status: string) => {
  const map: Record<string, string> = {
    PENDING: t('contract.statuses.pending'),
    APPROVED: t('contract.statuses.approved'),
    REJECTED: t('contract.statuses.rejected')
  }
  return map[status] || status
}

const getRenewalTypeTag = (type: string) => {
  const map: Record<string, string> = {
    EXTEND: 'primary',
    UPDATE: 'warning',
    REPLACE: 'info'
  }
  return map[type] || ''
}

const getStatusTagType = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return map[status] || ''
}

const fetchRenewals = async () => {
  loading.value = true
  try {
    const res = await getAllRenewals(query.keyword || query.status || undefined, currentPage.value, pageSize.value)
    renewals.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error(t('common.error'))
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  query.keyword = ''
  query.status = ''
  currentPage.value = 1
  fetchRenewals()
}

const fetchContracts = async () => {
  try {
    const res = await getContractList({ pageSize: 1000 })
    availableContracts.value = (res.data?.list || []).filter((c: any) => 
      c.status === 'SIGNED' || c.status === 'APPROVED'
    )
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const handleCreate = async () => {
  if (!renewalForm.contractId) {
    ElMessage.warning(t('contract.selectContract'))
    return
  }
  if (!renewalForm.newEndDate) {
    ElMessage.warning(t('contract.selectNewDate'))
    return
  }
  
  try {
    let dateStr = ''
    
    // 处理日期，无论是Date对象还是字符串
    if (typeof renewalForm.newEndDate === 'string') {
      dateStr = renewalForm.newEndDate
    } else if (renewalForm.newEndDate instanceof Date) {
      dateStr = renewalForm.newEndDate.toISOString().split('T')[0]
    }
    
    await createRenewal(renewalForm.contractId, {
      newEndDate: dateStr,
      renewalType: renewalForm.renewalType,
      remark: renewalForm.remark
    })
    ElMessage.success(t('common.success'))
    showCreateDialog.value = false
    resetForm()
    fetchRenewals()
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const handleApprove = async (row: any) => {
  try {
    await ElMessageBox.confirm(t('contract.confirmApproveRenewal'), t('common.warning'), { type: 'info' })
    await approveRenewal(row.contractId, row.id)
    ElMessage.success(t('common.success'))
    fetchRenewals()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(t('common.error'))
    }
  }
}

const handleAction = (command: string, row: any) => {
  switch (command) {
    case 'approve':
      handleApprove(row)
      break
    case 'reject':
      handleReject(row)
      break
  }
}

const handleReject = (row: any) => {
  currentRenewal.value = row
  rejectForm.remark = ''
  showRejectDialog.value = true
}

const confirmReject = async () => {
  if (!currentRenewal.value) return
  try {
    await rejectRenewal(currentRenewal.value.contractId, currentRenewal.value.id, rejectForm.remark)
    ElMessage.success(t('common.success'))
    showRejectDialog.value = false
    fetchRenewals()
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const resetForm = () => {
  renewalForm.contractId = undefined
  renewalForm.newEndDate = ''
  renewalForm.renewalType = 'EXTEND'
  renewalForm.remark = ''
}

onMounted(() => {
  fetchRenewals()
  fetchContracts()
})
</script>

<style scoped lang="scss">
.renewal-page {
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
}

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

.renewal-card {
  .empty-state {
    padding: 40px 0;
  }
  
  .pagination-wrap {
    display: flex;
    justify-content: flex-end;
    padding: 16px 0;
  }
}
</style>
