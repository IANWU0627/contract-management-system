<template>
  <div class="renewal-history">
    <div class="history-header">
      <el-button type="primary" size="small" @click="showCreateDialog = true" v-if="canCreateRenewal">
        <el-icon><Plus /></el-icon>
        {{ $t('contract.renewContract') }}
      </el-button>
    </div>

    <el-table :data="renewals" style="width: 100%" v-loading="loading" size="small">
      <el-table-column prop="OLD_END_DATE" :label="$t('contract.oldEndDate')" width="120">
        <template #default="{ row }">{{ formatDate(row.OLD_END_DATE) }}</template>
      </el-table-column>
      <el-table-column prop="NEW_END_DATE" :label="$t('contract.newEndDate')" width="120">
        <template #default="{ row }">{{ formatDate(row.NEW_END_DATE) }}</template>
      </el-table-column>
      <el-table-column prop="RENEWAL_TYPE" :label="$t('contract.renewalType')" width="100">
        <template #default="{ row }">
          <el-tag :type="getRenewalTypeTag(row.RENEWAL_TYPE)" size="small">
            {{ formatRenewalType(row.RENEWAL_TYPE) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="STATUS" :label="$t('contract.status')" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.STATUS)" size="small">
            {{ formatStatus(row.STATUS) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="OPERATOR_NAME" :label="$t('log.operator')" width="100" />
      <el-table-column prop="REMARK" :label="$t('contract.remark')" min-width="150" show-overflow-tooltip />
      <el-table-column prop="CREATED_AT" :label="$t('contract.createdAt')" width="160">
        <template #default="{ row }">{{ formatDateTime(row.CREATED_AT) }}</template>
      </el-table-column>
    </el-table>

    <div v-if="renewals.length === 0 && !loading" class="empty-state">
      <el-empty :description="$t('contract.noRenewals')" />
    </div>

    <el-dialog v-model="showCreateDialog" :title="$t('contract.renewContract')" width="500px">
      <el-form :model="renewalForm" label-width="100px">
        <el-form-item :label="$t('contract.oldEndDate')">
          <el-input :value="contractEndDate" disabled />
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
        <el-button type="primary" :loading="creating" @click="handleCreate">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getRenewals, createRenewal } from '@/api/renewal'
import { Plus } from '@element-plus/icons-vue'

const props = defineProps<{
  contractId: number
}>()

const { t } = useI18n()

const loading = ref(false)
const creating = ref(false)
const renewals = ref<any[]>([])
const showCreateDialog = ref(false)
const contractEndDate = ref('')

const renewalForm = ref<{
  newEndDate: string | Date | null
  renewalType: string
  remark: string
}>({
  newEndDate: null,
  renewalType: 'EXTEND',
  remark: ''
})

const canCreateRenewal = computed(() => {
  return true
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
    const res = await getRenewals(props.contractId)
    renewals.value = res.data?.list || []
  } catch (error) {
    ElMessage.error(t('common.error'))
  } finally {
    loading.value = false
  }
}

const handleCreate = async () => {
  if (!renewalForm.value.newEndDate) {
    ElMessage.warning(t('contract.selectNewDate'))
    return
  }
  
  creating.value = true
  try {
    let dateStr = ''
    const newDate = renewalForm.value.newEndDate as any
    if (typeof newDate === 'string') {
      dateStr = newDate
    } else if (newDate instanceof Date) {
      const year = newDate.getFullYear()
      const month = String(newDate.getMonth() + 1).padStart(2, '0')
      const day = String(newDate.getDate()).padStart(2, '0')
      dateStr = `${year}-${month}-${day}`
    }
    
    await createRenewal(props.contractId, {
      newEndDate: dateStr,
      renewalType: renewalForm.value.renewalType,
      remark: renewalForm.value.remark
    })
    ElMessage.success(t('common.success'))
    showCreateDialog.value = false
    renewalForm.value = { newEndDate: null, renewalType: 'EXTEND', remark: '' }
    fetchRenewals()
  } catch (error: any) {
    ElMessage.error(error.message || t('common.error'))
  } finally {
    creating.value = false
  }
}

onMounted(() => {
  fetchRenewals()
})
</script>

<style scoped lang="scss">
.renewal-history {
  .history-header {
    margin-bottom: 16px;
    display: flex;
    justify-content: flex-end;
  }
  
  .empty-state {
    padding: 40px 0;
  }
}
</style>
