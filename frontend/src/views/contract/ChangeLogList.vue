<template>
  <div class="change-log-list">
    <el-table :data="logs" style="width: 100%" v-loading="loading" size="small">
      <el-table-column prop="changeType" :label="$t('contract.changeType')" width="120">
        <template #default="{ row }">
          <el-tag :type="getChangeTypeTag(row.changeType)" size="small">
            {{ formatChangeType(row.changeType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="fieldName" :label="$t('contract.field')" width="120" />
      <el-table-column :label="$t('contract.changeContent')" min-width="200">
        <template #default="{ row }">
          <span v-if="row.oldValue || row.newValue">
            <span class="old-value">{{ row.oldValue || '(empty)' }}</span>
            <el-icon class="arrow"><Right /></el-icon>
            <span class="new-value">{{ row.newValue || '(empty)' }}</span>
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="operatorName" :label="$t('log.operator')" width="100" />
      <el-table-column prop="remark" :label="$t('contract.remark')" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">
          <span>{{ formatRemark(row.fieldName, row.changeType) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" :label="$t('contract.createdAt')" width="160">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
    </el-table>

    <div v-if="logs.length === 0 && !loading" class="empty-state">
      <el-empty :description="$t('contract.noChangeLogs')" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getChangeLogs } from '@/api/changeLog'
import { Right } from '@element-plus/icons-vue'

const props = defineProps<{
  contractId: number
}>()

const { t } = useI18n()

const loading = ref(false)
const logs = ref<any[]>([])

const formatDateTime = (date: string) => {
  if (!date) return '-'
  return date.replace('T', ' ').substring(0, 16)
}

const formatChangeType = (type: string) => {
  const key = `contract.changeTypes.${type}`
  const translated = t(key)
  return translated !== key ? translated : type
}

const getTypeTagType = (type: string) => {
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
  return map[type] || 'info'
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

const fetchLogs = async () => {
  loading.value = true
  try {
    const res = await getChangeLogs(props.contractId)
    logs.value = res.data?.list || []
  } catch (error) {
    ElMessage.error(t('common.error'))
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchLogs()
})
</script>

<style scoped lang="scss">
.change-log-list {
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
