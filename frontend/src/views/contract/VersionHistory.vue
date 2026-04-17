<template>
  <div class="version-history">
    <div class="version-header-actions">
      <el-button 
        type="primary" 
        size="small" 
        @click="showCompareDialog"
        :disabled="selectedVersions.length !== 2"
      >
        {{ $t('contract.actions.compare') }}
      </el-button>
    </div>
    <div class="snapshot-compare-actions">
      <el-select v-model="selectedSnapshotA" :placeholder="$t('contract.versionsDetail.selectSnapshotA')" style="width: 260px" clearable filterable>
        <el-option v-for="item in snapshots" :key="item.id" :label="snapshotLabel(item)" :value="item.id" />
      </el-select>
      <el-select v-model="selectedSnapshotB" :placeholder="$t('contract.versionsDetail.selectSnapshotB')" style="width: 260px" clearable filterable>
        <el-option v-for="item in snapshots" :key="item.id" :label="snapshotLabel(item)" :value="item.id" />
      </el-select>
      <el-button size="small" @click="compareSelectedSnapshots" :disabled="!selectedSnapshotA || !selectedSnapshotB || selectedSnapshotA === selectedSnapshotB">
        {{ $t('contract.versionsDetail.compareSnapshots') }}
      </el-button>
    </div>
    
    <el-timeline>
      <el-timeline-item
        v-for="version in versions"
        :key="version.id"
        :timestamp="formatTime(version.createdAt)"
        placement="top"
        :type="version.version === latestVersion ? 'primary' : ''"
      >
        <el-card>
          <div class="version-header">
            <div class="version-title">
              <el-checkbox 
                v-model="version.selected"
                @change="handleSelectionChange(version)"
                :disabled="selectedVersions.length >= 2 && !version.selected"
              />
              <h4>{{ $t('contract.versions') }} {{ version.version }}</h4>
              <el-tag v-if="version.version === latestVersion" size="small" type="success">{{ $t('contract.statuses.current') }}</el-tag>
            </div>
          </div>
          <p class="operator">
            <el-icon><User /></el-icon>
            {{ version.operatorName || $t('common.unknown') }}
          </p>
          <p class="change-desc">{{ formatChangeDesc(version.changeDesc) || $t('contract.versionsDetail.initial') }}</p>
          <div class="version-actions">
            <el-button type="primary" text size="small" @click="handleView(version)">
              <el-icon><View /></el-icon>
            </el-button>
            <el-button type="success" text size="small" @click="handleCompareWithCurrent(version)" v-if="version.version !== latestVersion">
              <el-icon><Connection /></el-icon>
            </el-button>
            <el-button type="warning" text size="small" @click="handleRestore(version)" v-if="version.version !== latestVersion">
              <el-icon><RefreshRight /></el-icon>
            </el-button>
          </div>
        </el-card>
      </el-timeline-item>
    </el-timeline>
    
    <el-empty v-if="versions.length === 0" :description="$t('common.none')" />
    
    <!-- 版本详情对话框 -->
    <el-dialog v-model="detailVisible" :title="$t('contract.versionsDetail.detail')" width="700px">
      <div class="version-detail">
        <div class="detail-header">
          <p><strong>{{ $t('contract.versionsDetail.no') }}:</strong> {{ currentVersion?.version }}</p>
          <p><strong>{{ $t('contract.versionsDetail.operator') }}:</strong> {{ currentVersion?.operatorName }}</p>
          <p><strong>{{ $t('contract.versionsDetail.time') }}:</strong> {{ formatTime(currentVersion?.createdAt) }}</p>
          <p><strong>{{ $t('contract.versionsDetail.desc') }}:</strong> {{ currentVersion?.changeDesc || $t('common.none') }}</p>
        </div>
        <el-divider />
        <div class="detail-content">
          <h4>{{ $t('contract.content') }}</h4>
          <pre class="content-pre">{{ currentContent }}</pre>
        </div>
      </div>
    </el-dialog>
    
    <!-- 版本对比对话框 -->
    <el-dialog v-model="compareVisible" :title="$t('contract.versionsDetail.compare')" width="900px" class="compare-dialog">
      <div class="compare-header">
        <div class="version-info">
          <span class="version-label">{{ $t('contract.versionsDetail.versionA') }}:</span>
          <el-tag>{{ compareResult.version1 }}</el-tag>
        </div>
        <el-icon class="compare-arrow"><Right /></el-icon>
        <div class="version-info">
          <span class="version-label">{{ $t('contract.versionsDetail.versionB') }}:</span>
          <el-tag type="success">{{ compareResult.version2 }}</el-tag>
        </div>
      </div>
      
      <div class="compare-stats">
        <el-statistic :title="$t('contract.versionsDetail.added')" :value="compareResult.addedLines || 0" value-style="color: #67c23a" />
        <el-statistic :title="$t('contract.versionsDetail.removed')" :value="compareResult.removedLines || 0" value-style="color: #f56c6c" />
      </div>

      <div class="risk-panel" v-if="compareResult.overallRisk || (compareResult.riskItems && compareResult.riskItems.length)">
        <div class="risk-header">
          <span class="risk-title">{{ $t('contract.versionsDetail.riskTitle') }}</span>
          <el-tag :type="riskTagType(compareResult.overallRisk)" size="small">
            {{ $t(`contract.versionsDetail.riskLevels.${(compareResult.overallRisk || 'low').toLowerCase()}`) }}
          </el-tag>
        </div>
        <p class="risk-commentary" v-if="compareResult.aiCommentary || compareResult.aiCommentaryKey">{{ resolveI18nText(compareResult.aiCommentaryKey, compareResult.aiCommentary) }}</p>
        <div class="risk-items" v-if="compareResult.riskItems && compareResult.riskItems.length">
          <el-card v-for="(risk, idx) in compareResult.riskItems" :key="idx" shadow="never" class="risk-item">
            <div class="risk-item-header">
              <el-tag :type="riskTagType(risk.level)" size="small">
                {{ $t(`contract.versionsDetail.riskLevels.${(risk.level || 'low').toLowerCase()}`) }}
              </el-tag>
              <span class="risk-item-title">{{ resolveI18nText(risk.titleKey, risk.title) || '-' }}</span>
            </div>
            <p class="risk-item-line">{{ resolveI18nText(risk.reasonKey, risk.reason) || '-' }}</p>
            <p class="risk-item-line" v-if="risk.evidence"><strong>{{ $t('contract.versionsDetail.evidence') }}:</strong> {{ risk.evidence }}</p>
            <p class="risk-item-line" v-if="risk.suggestion || risk.suggestionKey"><strong>{{ $t('contract.versionsDetail.suggestion') }}:</strong> {{ resolveI18nText(risk.suggestionKey, risk.suggestion) }}</p>
          </el-card>
        </div>
      </div>

      <div class="clause-changes" v-if="compareResult.clauseChanges && compareResult.clauseChanges.length">
        <div class="clause-changes-header">
          <div class="clause-changes-title">{{ $t('contract.versionsDetail.clauseChanges') }}</div>
          <div class="clause-changes-tools">
            <el-select v-model="clauseRiskFilter" size="small" style="width: 170px">
              <el-option :label="$t('contract.versionsDetail.riskFilterMediumHigh')" value="medium-high" />
              <el-option :label="$t('contract.versionsDetail.riskFilterHigh')" value="high" />
              <el-option :label="$t('contract.versionsDetail.riskFilterAll')" value="all" />
            </el-select>
            <el-switch
              v-model="showMinorClauseChanges"
              :active-text="$t('contract.versionsDetail.showMinorChanges')"
              size="small"
            />
          </div>
        </div>
        <div class="clause-changes-stats">
          <span class="stats-label">{{ $t('contract.versionsDetail.filteredStats') }}:</span>
          <el-tag size="small" type="danger">{{ $t('contract.versionsDetail.riskLevels.high') }} {{ visibleClauseStats.high }}</el-tag>
          <el-tag size="small" type="warning">{{ $t('contract.versionsDetail.riskLevels.medium') }} {{ visibleClauseStats.medium }}</el-tag>
          <el-tag size="small" type="info">{{ $t('contract.versionsDetail.riskLevels.low') }} {{ visibleClauseStats.low }}</el-tag>
          <el-tag size="small">{{ $t('contract.versionsDetail.changeTypeMinor') }} {{ visibleClauseStats.minor }}</el-tag>
        </div>
        <el-empty v-if="!visibleClauseChanges.length" :description="$t('common.none')" />
        <el-card v-for="(item, idx) in visibleClauseChanges" :key="idx" shadow="never" class="clause-change-item">
          <div class="clause-change-header">
            <el-tag size="small" :type="clauseChangeTagType(item.changeType)">{{ clauseChangeTypeLabel(item.changeType) }}</el-tag>
            <strong>{{ item.title || '-' }}</strong>
            <el-tag size="small" :type="riskTagType(item.riskLevel)">{{ $t(`contract.versionsDetail.riskLevels.${(item.riskLevel || 'low').toLowerCase()}`) }}</el-tag>
          </div>
          <div class="clause-change-body">
            <p v-if="item.before"><strong>{{ $t('contract.versionsDetail.before') }}:</strong> {{ item.before }}</p>
            <p v-if="item.after"><strong>{{ $t('contract.versionsDetail.after') }}:</strong> {{ item.after }}</p>
            <p v-if="item.riskReason || item.riskReasonKey"><strong>{{ $t('contract.versionsDetail.riskReason') }}:</strong> {{ resolveI18nText(item.riskReasonKey, item.riskReason) }}</p>
          </div>
        </el-card>
      </div>
      
      <div class="diff-container">
        <div v-for="(diff, index) in compareResult.differences" :key="index" :class="['diff-line', diff.type]">
          <span class="line-num">{{ diff.lineNum1 || diff.lineNum2 }}</span>
          <span class="line-marker">{{ getDiffMarker(diff.type) }}</span>
          <span class="line-content">{{ diff.content }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getVersionHistory, getVersionDetail, restoreVersion, compareVersions, compareSnapshots, getContractSnapshots } from '@/api/extra'
import { User, Right, View, RefreshRight, Connection } from '@element-plus/icons-vue'

const { t, locale } = useI18n()
const route = useRoute()
const props = defineProps<{ contractId?: number }>()

const contractId = computed(() => props.contractId || Number(route.params.id))

const versions = ref<any[]>([])
const snapshots = ref<any[]>([])
const latestVersion = ref('')
const detailVisible = ref(false)
const compareVisible = ref(false)
const currentVersion = ref<any>(null)
const currentContent = ref('')
const selectedVersions = ref<any[]>([])
const selectedSnapshotA = ref<number | null>(null)
const selectedSnapshotB = ref<number | null>(null)
const compareResult = ref<any>({
  version1: '',
  version2: '',
  differences: [],
  clauseChanges: [],
  addedLines: 0,
  removedLines: 0,
  overallRisk: 'low',
  riskItems: [],
  aiCommentary: ''
})
const showMinorClauseChanges = ref(false)
const clauseRiskFilter = ref<'all' | 'high' | 'medium-high'>('medium-high')
const visibleClauseChanges = computed(() => {
  let list = Array.isArray(compareResult.value?.clauseChanges) ? compareResult.value.clauseChanges : []
  if (!showMinorClauseChanges.value) {
    list = list.filter((item: any) => (item?.changeType || '').toLowerCase() !== 'minor')
  }
  if (clauseRiskFilter.value === 'high') {
    return list.filter((item: any) => (item?.riskLevel || '').toLowerCase() === 'high')
  }
  if (clauseRiskFilter.value === 'medium-high') {
    return list.filter((item: any) => {
      const level = (item?.riskLevel || '').toLowerCase()
      return level === 'high' || level === 'medium'
    })
  }
  return list
})
const visibleClauseStats = computed(() => {
  const stats = { high: 0, medium: 0, low: 0, minor: 0 }
  for (const item of visibleClauseChanges.value) {
    const level = (item?.riskLevel || '').toLowerCase()
    const type = (item?.changeType || '').toLowerCase()
    if (level === 'high') stats.high++
    else if (level === 'medium') stats.medium++
    else stats.low++
    if (type === 'minor') stats.minor++
  }
  return stats
})

const formatChangeDesc = (desc: string): string => {
  const map: Record<string, string> = {
    '创建合同': t('contract.versionsDetail.created'),
    '更新合同': t('contract.versionsDetail.updated')
  }
  return map[desc] || desc
}

const fetchVersions = async () => {
  try {
    const res = await getVersionHistory(contractId.value)
    const data = res.data || {}
    versions.value = (data.list || []).map((v: any) => ({ ...v, selected: false }))
    latestVersion.value = data.latestVersion || ''
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const fetchSnapshots = async () => {
  try {
    const res = await getContractSnapshots(contractId.value)
    snapshots.value = Array.isArray(res.data) ? res.data : []
  } catch (_error) {
    snapshots.value = []
  }
}

const handleSelectionChange = (version: any) => {
  if (version.selected) {
    if (selectedVersions.value.length >= 2) {
      version.selected = false
      ElMessage.warning(t('contract.versionsDetail.maxTwoWarning'))
      return
    }
    selectedVersions.value.push(version)
  } else {
    selectedVersions.value = selectedVersions.value.filter(v => v.id !== version.id)
  }
}

const handleView = async (version: any) => {
  try {
    const res = await getVersionDetail(contractId.value, version.id)
    currentVersion.value = res.data
    currentContent.value = res.data?.content || t('contract.versionsDetail.noContent')
    detailVisible.value = true
  } catch (error) {
    ElMessage.error(t('contract.versionsDetail.detailLoadFailed'))
  }
}

const handleCompareWithCurrent = async (version: any) => {
  try {
    const currentVersionId = versions.value.find(v => v.version === latestVersion.value)?.id
    if (!currentVersionId) {
      ElMessage.error(t('contract.versionsDetail.currentVersionMissing'))
      return
    }
    const res = await compareVersions(contractId.value, version.id, currentVersionId)
    compareResult.value = res.data
    compareVisible.value = true
  } catch (error) {
    ElMessage.error(t('contract.versionsDetail.compareFailed'))
  }
}

const showCompareDialog = async () => {
  if (selectedVersions.value.length !== 2) {
    ElMessage.warning(t('contract.versionsDetail.selectTwoWarning'))
    return
  }
  try {
    const [v1, v2] = selectedVersions.value
    const res = await compareVersions(contractId.value, v1.id, v2.id)
    compareResult.value = res.data
    compareVisible.value = true
  } catch (error) {
    ElMessage.error(t('contract.versionsDetail.compareFailed'))
  }
}

const compareSelectedSnapshots = async () => {
  if (!selectedSnapshotA.value || !selectedSnapshotB.value || selectedSnapshotA.value === selectedSnapshotB.value) {
    return
  }
  try {
    const res = await compareSnapshots(contractId.value, selectedSnapshotA.value, selectedSnapshotB.value)
    compareResult.value = res.data
    compareVisible.value = true
  } catch (error) {
    ElMessage.error(t('contract.versionsDetail.compareFailed'))
  }
}

const handleRestore = async (version: any) => {
  try {
    await ElMessageBox.confirm(
      t('contract.versionsDetail.restoreConfirm', { version: version.version }),
      t('contract.versionsDetail.restoreTitle'),
      { type: 'warning' }
    )
    await restoreVersion(contractId.value, version.id)
    ElMessage.success(t('contract.versionsDetail.restoreSuccess'))
    fetchVersions()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || t('contract.versionsDetail.restoreFailed'))
    }
  }
}

const formatTime = (time: string) => {
  if (!time) return ''
  const localeCode = locale.value === 'en' ? 'en-US' : 'zh-CN'
  return new Date(time).toLocaleString(localeCode)
}

const getDiffMarker = (type: string) => {
  switch (type) {
    case 'added': return '+'
    case 'removed': return '-'
    default: return ' '
  }
}

const riskTagType = (level: string) => {
  const normalized = (level || '').toLowerCase()
  if (normalized === 'high') return 'danger'
  if (normalized === 'medium') return 'warning'
  return 'info'
}

const clauseChangeTagType = (changeType: string) => {
  const normalized = (changeType || '').toLowerCase()
  if (normalized === 'added') return 'success'
  if (normalized === 'removed') return 'danger'
  if (normalized === 'minor') return 'info'
  return 'warning'
}

const clauseChangeTypeLabel = (changeType: string) => {
  const normalized = (changeType || '').toLowerCase()
  if (normalized === 'added') return t('contract.versionsDetail.changeTypeAdded')
  if (normalized === 'removed') return t('contract.versionsDetail.changeTypeRemoved')
  if (normalized === 'minor') return t('contract.versionsDetail.changeTypeMinor')
  return t('contract.versionsDetail.changeTypeModified')
}

const resolveI18nText = (key?: string, fallback?: string) => {
  if (key && key.trim()) {
    const translated = t(key)
    if (translated !== key) {
      return translated
    }
  }
  return fallback || ''
}

const snapshotLabel = (item: any) => {
  const type = item?.snapshotType || 'SNAPSHOT'
  const time = item?.createdAt ? formatTime(item.createdAt) : '-'
  return `${type} #${item?.id} (${time})`
}

onMounted(() => {
  fetchVersions()
  fetchSnapshots()
})
</script>

<style scoped lang="scss">
.version-history {
  .version-header-actions {
    margin-bottom: 16px;
    display: flex;
    justify-content: flex-end;
  }

  .snapshot-compare-actions {
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
  }
  
  .version-header {
    .version-title {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;
      
      h4 { margin: 0; }
    }
  }
  
  .operator {
    color: var(--text-secondary);
    font-size: 14px;
    margin: 4px 0;
    display: flex;
    align-items: center;
    gap: 4px;
  }
  
  .change-desc {
    color: var(--text-regular);
    margin: 8px 0;
    padding: 8px;
    background: #f5f7fa;
    border-radius: 4px;
  }
  
  .version-actions {
    margin-top: 8px;
    display: flex;
    gap: 8px;
  }
  
  .content-pre {
    white-space: pre-wrap;
    word-break: break-all;
    max-height: 400px;
    overflow-y: auto;
    background: #f5f7fa;
    padding: 16px;
    border-radius: 4px;
    font-family: monospace;
  }
  
  .version-detail {
    .detail-header {
      p {
        margin: 8px 0;
      }
    }
    
    .detail-content {
      h4 {
        margin-bottom: 12px;
      }
    }
  }
}

.compare-dialog {
  .compare-header {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 24px;
    margin-bottom: 20px;
    padding: 16px;
    background: #f5f7fa;
    border-radius: 8px;
    
    .version-info {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    
    .version-label {
      font-weight: 500;
    }
    
    .compare-arrow {
      font-size: 24px;
      color: #909399;
    }
  }
  
  .compare-stats {
    display: flex;
    justify-content: center;
    gap: 48px;
    margin-bottom: 20px;
  }

  .risk-panel {
    margin-bottom: 16px;
    padding: 12px;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    background: #fafafa;

    .risk-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 8px;
    }

    .risk-title {
      font-weight: 600;
      color: #303133;
    }

    .risk-commentary {
      margin: 0 0 10px;
      color: #606266;
      line-height: 1.6;
    }

    .risk-items {
      display: grid;
      grid-template-columns: repeat(1, minmax(0, 1fr));
      gap: 8px;
    }

    .risk-item {
      :deep(.el-card__body) {
        padding: 10px 12px;
      }
    }

    .risk-item-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 6px;
    }

    .risk-item-title {
      font-weight: 500;
      color: #303133;
    }

    .risk-item-line {
      margin: 0 0 4px;
      line-height: 1.5;
      color: #606266;
      font-size: 13px;
    }
  }

  .clause-changes {
    margin-bottom: 16px;

    .clause-changes-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;
      flex-wrap: wrap;
    }

    .clause-changes-title {
      font-weight: 600;
      color: #303133;
    }

    .clause-changes-tools {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
    }

    .clause-change-item {
      margin-bottom: 8px;

      :deep(.el-card__body) {
        padding: 10px 12px;
      }
    }

    .clause-changes-stats {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;
      flex-wrap: wrap;
    }

    .stats-label {
      color: #606266;
      font-size: 13px;
    }

    .clause-change-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 6px;
      flex-wrap: wrap;
    }

    .clause-change-body {
      p {
        margin: 0 0 4px;
        line-height: 1.5;
        color: #606266;
        font-size: 13px;
      }
    }
  }
  
  .diff-container {
    max-height: 500px;
    overflow-y: auto;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    font-family: monospace;
    font-size: 13px;
    
    .diff-line {
      display: flex;
      padding: 4px 8px;
      border-bottom: 1px solid #f0f0f0;
      
      &.added {
        background: #f0f9ff;
        .line-marker { color: #67c23a; }
      }
      
      &.removed {
        background: #fff5f5;
        .line-marker { color: #f56c6c; }
      }
      
      &.unchanged {
        background: #fff;
      }
      
      .line-num {
        width: 40px;
        color: #909399;
        text-align: right;
        margin-right: 12px;
        flex-shrink: 0;
      }
      
      .line-marker {
        width: 20px;
        text-align: center;
        margin-right: 8px;
        font-weight: bold;
        flex-shrink: 0;
      }
      
      .line-content {
        flex: 1;
        white-space: pre-wrap;
        word-break: break-all;
      }
    }
  }
}
</style>
