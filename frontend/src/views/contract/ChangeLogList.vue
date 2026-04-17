<template>
  <div class="change-log-list">
    <el-table :data="logs" style="width: 100%" v-loading="loading" size="small">
      <el-table-column prop="changeType" :label="$t('contract.changeType')" width="120">
        <template #default="{ row }">
          <el-tag :type="getTypeTagType(row.changeType)" size="small">
            {{ formatChangeType(row.changeType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="fieldName" :label="$t('contract.field')" width="140">
        <template #default="{ row }">
          <span>{{ formatFieldLabel(row.fieldName) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('contract.auditLevel')" width="100">
        <template #default="{ row }">
          <el-tag v-if="isCriticalChange(row)" type="danger" size="small">{{ $t('contract.auditCritical') }}</el-tag>
          <el-tag v-else size="small">{{ $t('contract.auditNormal') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('contract.changeContent')" min-width="260">
        <template #default="{ row }">
          <div v-if="resolvedDiff(row).oldValue || resolvedDiff(row).newValue" class="diff-cell">
            <div v-if="resolvedDiff(row).longText" class="diff-meta">
              <el-tag size="small" type="info">{{ $t('contract.longTextDiff') }}</el-tag>
              <span class="len-hint">
                {{ resolvedDiff(row).oldLength ?? 0 }} → {{ resolvedDiff(row).newLength ?? 0 }}
                {{ $t('contract.charsUnit') }}
              </span>
              <el-popover
                v-if="showContentPopover(row)"
                placement="bottom"
                :width="560"
                trigger="click"
              >
                <template #reference>
                  <el-button link type="primary" size="small">{{ $t('contract.viewDiffDetail') }}</el-button>
                </template>
                <div class="popover-diff">
                  <div class="popover-block">
                    <div class="popover-label">{{ $t('contract.beforeChange') }}</div>
                    <pre class="popover-pre">{{ resolvedDiff(row).oldValue || '—' }}</pre>
                  </div>
                  <div class="popover-block">
                    <div class="popover-label">{{ $t('contract.afterChange') }}</div>
                    <pre class="popover-pre">{{ resolvedDiff(row).newValue || '—' }}</pre>
                  </div>
                </div>
              </el-popover>
            </div>
            <div class="diff-line">
              <span class="old-value">{{ resolvedDiff(row).oldValue || '(empty)' }}</span>
              <el-icon class="arrow"><Right /></el-icon>
              <span class="new-value">{{ resolvedDiff(row).newValue || '(empty)' }}</span>
            </div>
          </div>
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

const formatFieldLabel = (fieldName: string | null | undefined) => {
  if (!fieldName) return '—'
  const key = `contract.fieldLabels.${fieldName}`
  const translated = t(key)
  return translated !== key ? translated : fieldName
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
    currency: 'contract.changeRemark.currencyChanged',
    startDate: 'contract.changeRemark.startDateChanged',
    endDate: 'contract.changeRemark.endDateChanged',
    status: 'contract.changeRemark.statusChanged',
    content: 'contract.changeRemark.contentChanged',
    parentContractId: 'contract.changeRemark.parentContractChanged',
    relationType: 'contract.changeRemark.relationTypeChanged'
  }
  
  const key = keyMap[fieldName]
  if (key) {
    const translated = t(key)
    if (translated !== key) return translated
  }
  
  return fieldName || '-'
}

const resolvedDiff = (row: any) => {
  const raw = row?.diffJson ?? row?.diff_json
  if (!raw) {
    return {
      oldValue: row?.oldValue,
      newValue: row?.newValue,
      critical: false,
      longText: false,
      oldLength: undefined as number | undefined,
      newLength: undefined as number | undefined
    }
  }
  try {
    const parsed = typeof raw === 'string' ? JSON.parse(raw) : raw
    return {
      oldValue: parsed.oldValue ?? row.oldValue,
      newValue: parsed.newValue ?? row.newValue,
      critical: parsed.critical === true,
      longText: parsed.longText === true,
      oldLength: typeof parsed.oldLength === 'number' ? parsed.oldLength : undefined,
      newLength: typeof parsed.newLength === 'number' ? parsed.newLength : undefined
    }
  } catch {
    return {
      oldValue: row?.oldValue,
      newValue: row?.newValue,
      critical: false,
      longText: false,
      oldLength: undefined,
      newLength: undefined
    }
  }
}

const showContentPopover = (row: any) => {
  const d = resolvedDiff(row)
  return d.longText && row.fieldName === 'content'
}

const isCriticalChange = (row: any) => {
  return resolvedDiff(row).critical
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

  .diff-cell {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .diff-meta {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px;
  }

  .len-hint {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .diff-line {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 4px;
  }
  
  .old-value {
    color: var(--el-text-color-secondary);
    text-decoration: line-through;
    word-break: break-word;
  }
  
  .new-value {
    color: var(--el-color-success);
    word-break: break-word;
  }
  
  .arrow {
    margin: 0 4px;
    color: var(--el-color-primary);
    flex-shrink: 0;
  }
}

.popover-diff {
  max-height: 420px;
  overflow: auto;
}

.popover-block {
  margin-bottom: 12px;
}

.popover-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
}

.popover-pre {
  margin: 0;
  padding: 10px;
  font-size: 12px;
  line-height: 1.45;
  white-space: pre-wrap;
  word-break: break-word;
  background: var(--el-fill-color-light);
  border-radius: 8px;
  max-height: 180px;
  overflow: auto;
}
</style>
