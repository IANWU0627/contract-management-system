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
import { getVersionHistory, getVersionDetail, restoreVersion, compareVersions } from '@/api/extra'
import { User, Right, View, RefreshRight, Connection } from '@element-plus/icons-vue'

const { t, locale } = useI18n()
const route = useRoute()
const props = defineProps<{ contractId?: number }>()

const contractId = computed(() => props.contractId || Number(route.params.id))

const versions = ref<any[]>([])
const latestVersion = ref('')
const detailVisible = ref(false)
const compareVisible = ref(false)
const currentVersion = ref<any>(null)
const currentContent = ref('')
const selectedVersions = ref<any[]>([])
const compareResult = ref<any>({
  version1: '',
  version2: '',
  differences: [],
  addedLines: 0,
  removedLines: 0
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

onMounted(() => { fetchVersions() })
</script>

<style scoped lang="scss">
.version-history {
  .version-header-actions {
    margin-bottom: 16px;
    display: flex;
    justify-content: flex-end;
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
