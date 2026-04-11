<template>
  <div class="contract-list">
    <div class="page-header">
      <h1 class="page-title">{{ $t('contract.title') }}</h1>
      <div class="header-actions">
        <el-button type="primary" class="gradient-btn" @click="$router.push('/contracts/create')">
          <el-icon><Plus /></el-icon>
          {{ $t('contract.create') }}
        </el-button>
      </div>
    </div>
    
    <!-- 搜索筛选 -->
    <div class="filter-section">
      <div class="filter-row">
        <div class="filter-item filter-item-wide" style="flex: 1;">
          <el-input
            v-model="query.keyword"
            :placeholder="$t('contract.placeholder.keyword')"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <div class="filter-item" style="width: 160px;">
          <el-select v-model="query.folderId" clearable :placeholder="$t('folder.title')" @change="handleSearch">
            <el-option
              v-for="folder in folders"
              :key="folder.id"
              :label="folder.name"
              :value="folder.id"
            >
              <span :style="{ color: folder.color }">●</span> {{ folder.name }}
            </el-option>
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
    
    <!-- 合同列表 -->
    <div class="table-section">
      <div class="table-header">
        <div class="batch-actions">
          <el-checkbox v-model="selectAll" :indeterminate="indeterminate" @change="handleSelectAll">
            {{ $t('common.selectAll') }}
          </el-checkbox>
          <span class="selected-count" v-if="selectedIds.length > 0">
            {{ $t('common.selected', { count: selectedIds.length }) }}
          </span>
          
          <!-- 主要批量操作按钮（始终显示） -->
          <el-button 
            type="danger" 
            size="small" 
            :disabled="selectedIds.length === 0" 
            @click="handleBatchDelete"
            class="batch-btn-main"
          >
            <el-icon><Delete /></el-icon>
            {{ $t('common.batchDelete') }}
          </el-button>
          
          <el-button 
            type="primary" 
            size="small" 
            :disabled="selectedIds.length === 0" 
            @click="showBatchEditDialog = true"
            class="batch-btn-main"
          >
            <el-icon><Edit /></el-icon>
            {{ $t('contract.batchEdit') }}
          </el-button>
          
          <!-- 更多批量操作下拉菜单 -->
          <el-dropdown trigger="click" :disabled="selectedIds.length === 0" @command="handleBatchCommand">
            <el-button type="info" size="small" :disabled="selectedIds.length === 0">
              <el-icon><More /></el-icon>
              {{ $t('common.more') }}
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="approve">
                  <el-icon><CircleCheck /></el-icon>
                  {{ $t('contract.batchApprove') }}
                </el-dropdown-item>
                <el-dropdown-item command="submit">
                  <el-icon><TopRight /></el-icon>
                  {{ $t('contract.batchSubmit') }}
                </el-dropdown-item>
                <el-dropdown-item divided>
                  <span style="color: var(--text-secondary); font-size: 12px;">{{ $t('contract.batchStatus') }}</span>
                </el-dropdown-item>
                <el-dropdown-item command="status_DRAFT">
                  <el-tag size="small" type="info">{{ $t('contract.statuses.draft') }}</el-tag>
                </el-dropdown-item>
                <el-dropdown-item command="status_PENDING">
                  <el-tag size="small" type="warning">{{ $t('contract.statuses.pending') }}</el-tag>
                </el-dropdown-item>
                <el-dropdown-item command="status_APPROVED">
                  <el-tag size="small" type="success">{{ $t('contract.statuses.approved') }}</el-tag>
                </el-dropdown-item>
                <el-dropdown-item command="status_SIGNED">
                  <el-tag size="small" type="success">{{ $t('contract.statuses.signed') }}</el-tag>
                </el-dropdown-item>
                <el-dropdown-item command="status_ARCHIVED">
                  <el-tag size="small">{{ $t('contract.statuses.archived') }}</el-tag>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          
          <!-- 导出按钮（独立） -->
          <el-button type="info" size="small" plain @click="handleBatchExport" class="batch-btn-export">
            <el-icon><Download /></el-icon>
            {{ $t('contract.exportExcel') }}
          </el-button>
        </div>
        <div class="table-info">
          {{ $t('common.totalCount', { count: total, item: $t('contract.title') }) }}
        </div>
      </div>
      
        <el-table ref="tableRef" :data="contracts" v-loading="loading" style="width: 100%" @selection-change="handleSelectionChange">
        <template #empty>
          <!-- 空状态已移除 -->
        </template>
        <el-table-column type="selection" width="50" />
        <el-table-column prop="contractNo" :label="$t('contract.no')" width="130" show-overflow-tooltip />
        <el-table-column prop="title" :label="$t('contract.name')" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link type="primary" @click="$router.push(`/contracts/${row.id}`)" class="title-link">
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="type" :label="$t('contract.type')" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)" size="small">{{ formatType(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" :label="$t('contract.amount')" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            ¥{{ formatAmount(row.amount) }}
          </template>
        </el-table-column>
        <el-table-column prop="startDate" :label="$t('contract.startDate')" width="90" show-overflow-tooltip />
        <el-table-column prop="endDate" :label="$t('contract.endDate')" width="90" show-overflow-tooltip />
        <el-table-column prop="status" :label="$t('contract.status')" width="75" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">{{ formatStatus(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('contract.tag')" min-width="90" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="contract-tags" v-if="row.tags && row.tags.length > 0">
              <el-tag 
                v-for="tag in row.tags.slice(0, 1)" 
                :key="tag.id"
                size="small"
                :style="{ background: tag.color + '20', borderColor: tag.color, color: tag.color }"
                class="tag-item"
              >
                {{ tag.name }}
              </el-tag>
              <el-tag v-if="row.tags.length > 1" size="small" type="info">+{{ row.tags.length - 1 }}</el-tag>
            </div>
            <span v-else class="no-tags">-</span>
          </template>
        </el-table-column>
        <template v-for="type in Array.from(loadedTypes)" :key="type">
          <template v-for="field in dynamicFieldsMap[type] || []" :key="`${type}-${field.id}`">
            <el-table-column :label="locale === 'zh' ? field.fieldLabel : (field.fieldLabelEn || field.fieldLabel)" min-width="85" show-overflow-tooltip>
              <template #default="{ row }">
                <span v-if="row.type === type" class="dynamic-field-value">
                  {{ row.dynamicFields ? formatDynamicValue(row.dynamicFields[field.fieldKey], field.fieldType, field) : '-' }}
                </span>
              </template>
            </el-table-column>
          </template>
        </template>
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
                  <el-dropdown-item command="edit" v-if="row.status === 'DRAFT'">{{ $t('common.edit') }}</el-dropdown-item>
                  <el-dropdown-item command="aiAnalyze" v-if="row.content">{{ $t('ai.analyze') }}</el-dropdown-item>
                  <el-dropdown-item command="delete" v-if="row.status === 'DRAFT'" divided type="danger">{{ $t('common.delete') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </div>
    
    <!-- AI分析对话框 -->
    <el-dialog v-model="aiDialogVisible" :title="$t('ai.analyze')" width="700px" class="ai-analysis-dialog">
      <div v-loading="aiLoading" class="ai-analysis-content">
        <!-- 风险评分 -->
        <div class="score-section" v-if="aiResult.score !== undefined">
          <div class="score-header">
            <span class="score-label">{{ $t('ai.riskScore') }}</span>
            <span class="score-value" :class="getScoreClass(aiResult.score)">{{ aiResult.score }}</span>
          </div>
          <el-progress :percentage="aiResult.score" :color="getScoreColor(aiResult.score)" :stroke-width="12" />
        </div>

        <!-- 合同摘要 -->
        <div class="section">
          <div class="section-title">
            <el-icon><Document /></el-icon>
            <span>{{ $t('ai.summary') }}</span>
          </div>
          <p class="summary-text">{{ aiResult.summary }}</p>
        </div>
        
        <!-- 风险点 -->
        <div class="section" v-if="aiResult.risks && aiResult.risks.length > 0">
          <div class="section-title">
            <el-icon><Warning /></el-icon>
            <span>{{ $t('ai.risks') }}</span>
            <el-tag size="small" type="info">{{ aiResult.risks.length }}</el-tag>
          </div>
          <div class="risks-list">
            <div 
              v-for="(risk, idx) in aiResult.risks" 
              :key="idx" 
              class="risk-item"
              :class="`risk-${getRiskLevel(risk)}`"
            >
              <div class="risk-level-badge">
                <el-tag :type="getRiskTagType(getRiskLevel(risk))" size="small">
                  {{ getRiskLevelText(getRiskLevel(risk)) }}
                </el-tag>
              </div>
              <div class="risk-content">{{ getRiskContent(risk) }}</div>
            </div>
          </div>
        </div>

        <!-- 建议 -->
        <div class="section" v-if="aiResult.suggestions && aiResult.suggestions.length > 0">
          <div class="section-title">
            <el-icon><CircleCheck /></el-icon>
            <span>{{ $t('ai.suggestions') }}</span>
          </div>
          <ul class="suggestions-list">
            <li v-for="(suggestion, idx) in aiResult.suggestions" :key="idx">
              <span class="suggestion-index">{{ idx + 1 }}.</span>
              {{ suggestion }}
            </li>
          </ul>
        </div>
        
        <!-- 关键信息 -->
        <div class="section" v-if="aiResult.keyInfo">
          <div class="section-title">
            <el-icon><InfoFilled /></el-icon>
            <span>{{ $t('ai.keyInfo') }}</span>
          </div>
          <el-descriptions :column="2" border class="key-info-descriptions">
            <el-descriptions-item :label="$t('contract.partyTypes.partyA')" class="description-item">
              {{ aiResult.keyInfo?.partyA || '-' }}
            </el-descriptions-item>
            <el-descriptions-item :label="$t('contract.partyTypes.partyB')" class="description-item">
              {{ aiResult.keyInfo?.partyB || '-' }}
            </el-descriptions-item>
            <el-descriptions-item :label="$t('contract.amount')" class="description-item">
              {{ aiResult.keyInfo?.amount || '-' }}
            </el-descriptions-item>
            <el-descriptions-item :label="$t('contract.duration')" class="description-item">
              {{ aiResult.keyInfo?.duration || '-' }}
            </el-descriptions-item>
          </el-descriptions>
          
          <!-- 关键条款 -->
          <div v-if="aiResult.keyInfo?.keyClauses && aiResult.keyInfo.keyClauses.length > 0" class="key-clauses">
            <div class="sub-section-title">{{ $t('ai.keyClauses') }}</div>
            <div class="clauses-list">
              <el-tag v-for="(clause, idx) in aiResult.keyInfo.keyClauses" :key="idx" size="small" class="clause-tag">
                {{ clause }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
    
    <!-- 批量编辑对话框 -->
    <el-dialog v-model="showBatchEditDialog" :title="$t('contract.batchEdit')" width="500px">
      <el-form label-width="100px">
        <el-form-item :label="$t('contract.type')">
          <el-select v-model="batchEditForm.type" :placeholder="$t('common.placeholder.select')" clearable>
            <el-option v-for="cat in categories" :key="cat.code" :label="cat.name" :value="cat.code" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('contract.counterparty')">
          <el-input v-model="batchEditForm.counterparty" :placeholder="$t('contract.counterpartyPlaceholder')" clearable />
        </el-form-item>
        <el-form-item :label="$t('contract.remark')">
          <el-input v-model="batchEditForm.remark" type="textarea" :rows="3" :placeholder="$t('contract.remarkPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBatchEditDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleBatchEdit">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type TableInstance } from 'element-plus'
import { getContractList, deleteContract, analyzeContract, batchDeleteContracts, batchUpdateStatus, batchApprove, batchSubmit, exportContractsExcel, batchEdit } from '@/api/contract'
import { getContractTypeFieldConfig } from '@/api/contractTypeField'
import { getAllFolders } from '@/api/folder'
import { getContractCategories } from '@/api/contractCategory'
import { getQuickCodeByCode } from '@/api/quickCode'
import { Search, Refresh, Plus, ArrowDown, Delete, Edit, More, Download, CircleCheck, TopRight, Document, Warning, InfoFilled } from '@element-plus/icons-vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const { t, locale } = useI18n()

const loading = ref(false)
const contracts = ref<any[]>([])
const total = ref(0)
const tableRef = ref<TableInstance>()
const dynamicFieldsMap = ref<Record<string, any[]>>({})
const loadedTypes = ref<Set<string>>(new Set())
const quickCodeItemsCache = ref<Record<string, any[]>>({})
const folders = ref<any[]>([])
const categories = ref<any[]>([])

const query = reactive({
  page: 1,
  pageSize: 10,
  title: '',
  contractNo: '',
  type: '',
  status: '',
  folderId: null as number | null,
  startDateFrom: '',
  startDateTo: '',
  endDateFrom: '',
  endDateTo: '',
  amountMin: undefined as number | undefined,
  amountMax: undefined as number | undefined,
  keyword: ''
})

const dateRange = ref<string[]>([])

const loadFolders = async () => {
  try {
    const res = await getAllFolders()
    folders.value = res.data || []
  } catch (error) {
    console.error('Failed to load folders:', error)
  }
}

const handleDateRangeChange = (val: string[] | null) => {
  if (val && val.length === 2) {
    query.startDateFrom = val[0]
    query.startDateTo = val[1]
  } else {
    query.startDateFrom = ''
    query.startDateTo = ''
  }
}

const aiDialogVisible = ref(false)
const aiLoading = ref(false)
const aiResult = ref<any>({
  summary: '',
  score: 75,
  risks: [],
  suggestions: [],
  keyInfo: {}
})

// AI分析辅助函数
const getScoreClass = (score: number) => {
  if (score >= 80) return 'score-high'
  if (score >= 60) return 'score-medium'
  return 'score-low'
}

const getScoreColor = (score: number) => {
  if (score >= 80) return '#67C23A'
  if (score >= 60) return '#E6A23C'
  return '#F56C6C'
}

const getRiskLevel = (risk: any): string => {
  if (typeof risk === 'string') return 'medium'
  return risk?.level || 'medium'
}

const getRiskContent = (risk: any): string => {
  if (typeof risk === 'string') return risk
  return risk?.content || ''
}

const getRiskTagType = (level: string): '' | 'success' | 'warning' | 'danger' | 'info' => {
  switch (level) {
    case 'high': return 'danger'
    case 'medium': return 'warning'
    case 'low': return 'info'
    default: return 'info'
  }
}

const getRiskLevelText = (level: string): string => {
  switch (level) {
    case 'high': return locale.value === 'en' ? 'High' : '高风险'
    case 'medium': return locale.value === 'en' ? 'Medium' : '中风险'
    case 'low': return locale.value === 'en' ? 'Low' : '低风险'
    default: return locale.value === 'en' ? 'Unknown' : '未知'
  }
}

// 批量编辑
const showBatchEditDialog = ref(false)
const batchEditForm = reactive({
  type: '',
  counterparty: '',
  remark: ''
})

// 批量选择
const selectedIds = ref<number[]>([])
const selectAll = ref(false)
const indeterminate = ref(false)

const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map((item: any) => item.id)
  indeterminate.value = selectedIds.value.length > 0 && selectedIds.value.length < contracts.value.length
}

const handleSelectAll = (val: boolean) => {
  if (tableRef.value) {
    if (val) {
      contracts.value.forEach(contract => {
        tableRef.value?.toggleRowSelection(contract, true)
      })
    } else {
      tableRef.value.clearSelection()
    }
  }
}

const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) return
  try {
    await ElMessageBox.confirm(t('common.confirmDeleteBatch', { count: selectedIds.value.length, item: t('contract.title') }), t('common.warning'), { type: 'warning' })
    await batchDeleteContracts(selectedIds.value)
    ElMessage.success(t('common.success'))
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || t('common.error'))
  }
}

const handleBatchExport = () => {
  exportContractsExcel(query)
}

const handleBatchEdit = async () => {
  if (selectedIds.value.length === 0) return
  if (!batchEditForm.type && !batchEditForm.counterparty && !batchEditForm.remark) {
    ElMessage.warning(t('contract.batchEditNoChanges'))
    return
  }
  try {
    await ElMessageBox.confirm(
      t('contract.confirmBatchEdit', { count: selectedIds.value.length }),
      t('common.warning'),
      { type: 'warning' }
    )
    const res = await batchEdit(selectedIds.value, {
      type: batchEditForm.type || undefined,
      counterparty: batchEditForm.counterparty || undefined,
      remark: batchEditForm.remark || undefined
    })
    const successCount = res.data?.successCount || 0
    ElMessage.success(t('contract.batchSuccess', { count: successCount }))
    showBatchEditDialog.value = false
    batchEditForm.type = ''
    batchEditForm.counterparty = ''
    batchEditForm.remark = ''
    selectedIds.value = []
    selectAll.value = false
    indeterminate.value = false
    tableRef.value?.clearSelection()
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || t('common.error'))
  }
}

const handleBatchStatus = async (status: string) => {
  if (selectedIds.value.length === 0) return
  try {
    await ElMessageBox.confirm(
      t('contract.confirmBatchStatus', { count: selectedIds.value.length, status: formatStatus(status) }),
      t('common.warning'),
      { type: 'warning' }
    )
    const res = await batchUpdateStatus(selectedIds.value, status)
    const successCount = res.data?.successCount || 0
    ElMessage.success(t('contract.batchSuccess', { count: successCount }))
    selectedIds.value = []
    selectAll.value = false
    indeterminate.value = false
    tableRef.value?.clearSelection()
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || t('common.error'))
  }
}

// 批量操作命令处理
const handleBatchCommand = (command: string) => {
  if (command.startsWith('status_')) {
    const status = command.replace('status_', '')
    handleBatchStatus(status)
  } else if (command === 'approve') {
    handleBatchApprove()
  } else if (command === 'submit') {
    handleBatchSubmit()
  }
}

const handleBatchApprove = async () => {
  if (selectedIds.value.length === 0) return
  try {
    await ElMessageBox.confirm(
      t('contract.confirmBatchApprove', { count: selectedIds.value.length }),
      t('common.warning'),
      { type: 'warning' }
    )
    const res = await batchApprove(selectedIds.value)
    const successCount = res.data?.successCount || 0
    ElMessage.success(t('contract.batchSuccess', { count: successCount }))
    selectedIds.value = []
    selectAll.value = false
    indeterminate.value = false
    tableRef.value?.clearSelection()
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || t('common.error'))
  }
}

const handleBatchSubmit = async () => {
  if (selectedIds.value.length === 0) return
  try {
    await ElMessageBox.confirm(
      t('contract.confirmBatchSubmit', { count: selectedIds.value.length }),
      t('common.warning'),
      { type: 'warning' }
    )
    const res = await batchSubmit(selectedIds.value)
    const successCount = res.data?.successCount || 0
    ElMessage.success(t('contract.batchSuccess', { count: successCount }))
    selectedIds.value = []
    selectAll.value = false
    indeterminate.value = false
    tableRef.value?.clearSelection()
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || t('common.error'))
  }
}

const formatAmount = (amount: number) => {
  if (!amount) return '0'
  return new Intl.NumberFormat('zh-CN').format(amount)
}

const formatType = (type: string) => {
  const cat = categories.value.find(c => c.code === type)
  if (cat) {
    return locale.value === 'en' && cat.nameEn ? cat.nameEn : (cat.name || t('contract.types.other'))
  }
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
    SIGNED: t('contract.statuses.signed'),
    ARCHIVED: t('contract.statuses.archived'),
    TERMINATED: t('contract.statuses.terminated')
  }
  return map[status] || status
}

const getTypeTagType = (type: string) => {
  const cat = categories.value.find(c => c.code === type)
  if (cat) return cat.color ? 'primary' : ''
  const map: Record<string, string> = {
    PURCHASE: 'success',
    SALES: 'primary',
    LEASE: 'warning',
    EMPLOYMENT: 'info',
    SERVICE: '',
    OTHER: 'info'
  }
  return map[type] || ''
}

const getStatusTagType = (status: string) => {
  const map: Record<string, string> = {
    DRAFT: 'info',
    PENDING: 'warning',
    APPROVING: 'warning',
    SIGNED: 'success',
    ARCHIVED: '',
    TERMINATED: 'danger'
  }
  return map[status] || ''
}

const loadDynamicFieldsForType = async (type: string) => {
  if (loadedTypes.value.has(type)) return
  try {
    const res = await getContractTypeFieldConfig(type)
    const fields = res.data?.fields || []
    
    for (const field of fields) {
      if (field.fieldType === 'select' && field.quickCodeId && !quickCodeItemsCache.value[field.quickCodeId]) {
        try {
          const qcRes = await getQuickCodeByCode(field.quickCodeId)
          quickCodeItemsCache.value[field.quickCodeId] = qcRes.data || []
        } catch (e) {
          quickCodeItemsCache.value[field.quickCodeId] = []
        }
      }
    }
    
    dynamicFieldsMap.value[type] = fields.filter((f: any) => f.showInList)
    loadedTypes.value.add(type)
  } catch (error) {
    dynamicFieldsMap.value[type] = []
  }
}

const getDynamicColumns = (type: string) => {
  return dynamicFieldsMap.value[type] || []
}

const formatDynamicValue = (value: any, fieldType: string, field?: any) => {
  if (value === null || value === undefined || value === '') return '-'
  if (fieldType === 'currency') {
    return '¥' + new Intl.NumberFormat('zh-CN').format(Number(value))
  }
  if (fieldType === 'number') {
    return new Intl.NumberFormat('zh-CN').format(Number(value))
  }
  if (fieldType === 'select' && field?.quickCodeId) {
    const items = quickCodeItemsCache.value[field.quickCodeId] || []
    const item = items.find(i => i.code === value)
    if (item) {
      return locale.value === 'en' && item.meaningEn ? item.meaningEn : item.meaning
    }
  }
  return String(value)
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getContractList(query)
    contracts.value = res.data?.list || []
    total.value = res.data?.total || 0
    
    // Load dynamic fields for each contract type
    const types = [...new Set(contracts.value.map(c => c.type).filter(Boolean))]
    for (const type of types) {
      await loadDynamicFieldsForType(type)
    }
  } catch (error) {
    // Use mock data
    contracts.value = [
      {
        id: 1,
        contractNo: 'CT-20260326-0001',
        title: '测试合同1',
        type: 'PURCHASE',
        status: 'DRAFT',
        amount: 10000,
        startDate: '2026-03-26',
        endDate: '2026-03-31',
        dynamicFields: { supplier: '测试供应商', quantity: 100 }
      },
      {
        id: 2,
        contractNo: 'CT-20260326-0002',
        title: '测试合同2',
        type: 'SALES',
        status: 'APPROVED',
        amount: 20000,
        startDate: '2026-03-26',
        endDate: '2026-04-30',
        dynamicFields: { customer_name: '测试客户' }
      }
    ]
    total.value = 2
    await loadDynamicFieldsForType('PURCHASE')
    await loadDynamicFieldsForType('SALES')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.page = 1
  fetchData()
}

const handleReset = () => {
  query.title = ''
  query.contractNo = ''
  query.type = ''
  query.status = ''
  query.folderId = null
  query.startDateFrom = ''
  query.startDateTo = ''
  query.endDateFrom = ''
  query.endDateTo = ''
  query.amountMin = undefined
  query.amountMax = undefined
  query.keyword = ''
  dateRange.value = []
  query.page = 1
  fetchData()
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该合同吗？', '提示', {
      type: 'warning'
    })
    await deleteContract(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const handleAiAnalyze = async (id: number) => {
  aiDialogVisible.value = true
  aiLoading.value = true
  try {
    const aiConfig = {
      provider: localStorage.getItem('ai_provider') || 'none',
      apiUrl: localStorage.getItem('ai_api_url') || '',
      apiKey: localStorage.getItem('ai_api_key') || '',
      model: localStorage.getItem('ai_model') || 'gpt-3.5-turbo'
    }
    
    const res = await analyzeContract(id, aiConfig)
    aiResult.value = res.data || {}
  } catch (error) {
    ElMessage.error('AI分析失败')
  } finally {
    aiLoading.value = false
  }
}

const handleAction = (command: string, row: any) => {
  switch (command) {
    case 'view':
      router.push(`/contracts/${row.id}`)
      break
    case 'edit':
      router.push(`/contracts/${row.id}/edit`)
      break
    case 'aiAnalyze':
      handleAiAnalyze(row.id)
      break
    case 'delete':
      handleDelete(row.id)
      break
  }
}

onMounted(async () => {
  await loadCategories()
  fetchData()
  loadFolders()
})

const loadCategories = async () => {
  try {
    const res = await getContractCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
}

onUnmounted(() => {
  // 清理资源
  loading.value = false
  contracts.value = []
  total.value = 0
  selectedIds.value = []
  selectAll.value = false
  indeterminate.value = false
  aiDialogVisible.value = false
  aiLoading.value = false
  aiResult.value = { summary: '', risks: [], keyInfo: {} }
})
</script>

<style scoped lang="scss">
/* ── Contract List Apifox Style ──────────────────────── */
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

.contract-list {
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
    
    .header-actions {
      display: flex;
      gap: 12px;
      
      .el-button {
        padding: 10px 20px;
        border-radius: 8px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        max-width: 150px;
        font-weight: 500;
        
        &:hover {
          overflow: visible;
          white-space: normal;
          text-overflow: clip;
        }
      }
    }
  }
  
  /* ── Filter Section ────────────────────────────── */
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
  
  /* ── Table Section ─────────────────────────────── */
  .table-section {
    background: var(--bg-card);
    border: 1px solid var(--border-color);
    border-radius: 12px;
    overflow: hidden;
    
    :deep(.el-table) {
      .el-table__header-wrapper th .cell {
        word-break: break-word;
        white-space: normal;
        line-height: 1.4;
      }
      
      .el-table__body-wrapper td .cell {
        word-break: break-word;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      
      .title-link {
        display: inline-block;
        max-width: 100%;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
    
    .table-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 24px;
      border-bottom: 1px solid var(--border-color);
      
      .table-info {
        font-size: 14px;
        color: var(--text-secondary);
        
        strong {
          color: var(--text-primary);
        }
      }
      
      .table-actions {
        display: flex;
        gap: 8px;
      }
      
      .batch-actions {
        display: flex;
        align-items: center;
        gap: 10px;
        
        .el-checkbox {
          margin-right: 0;
        }
        
        .selected-count {
          font-size: 13px;
          color: var(--text-secondary);
          padding: 0 8px;
          border-right: 1px solid var(--border-color);
          margin-right: 4px;
        }
        
        .batch-btn-main {
          font-size: 13px;
          padding: 6px 12px;
          
          .el-icon {
            margin-right: 4px;
          }
        }
        
        .batch-btn-export {
          font-size: 13px;
          padding: 6px 12px;
          margin-left: 8px;
          border-left: 1px solid var(--border-color);
          padding-left: 16px;
          border-radius: 0;
          
          .el-icon {
            margin-right: 4px;
          }
        }
        
        .el-dropdown {
          .el-button {
            font-size: 13px;
            padding: 6px 12px;
            
            .el-icon {
              margin-right: 4px;
            }
          }
        }
      }
      
      /* 下拉菜单样式 */
      :deep(.el-dropdown-menu) {
        padding: 8px;
        
        .el-dropdown-item {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 8px 12px;
          border-radius: 6px;
          
          .el-icon {
            font-size: 16px;
          }
          
          &:hover {
            background: var(--bg-hover);
          }
        }
      }
    }
    
    .el-table {
      --el-table-border-color: #f0f0f0;
      --el-table-header-bg-color: #fafafa;
      --el-table-header-text-color: #1a1a1a;
      --el-table-row-hover-bg-color: #f9f9f9;
      --el-table-cell-padding: 16px 12px;
      
      th {
        font-weight: 600;
        font-size: 13px;
      }
      
      td {
        font-size: 14px;
        color: #595959;
        transition: background 0.2s ease;
      }
      
      tr:hover > td {
        background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%) !important;
      }
      
      .cell {
        display: flex;
        align-items: center;
        gap: 8px;
      }
      
      .el-link {
        font-weight: 500;
        
        &:hover {
          color: var(--primary);
        }
      }
    }
    
    .pagination-wrap {
      display: flex;
      justify-content: flex-end;
      padding: 16px 24px;
      border-top: 1px solid #f0f0f0;
    }
  }
  
  /* ── Status Tags ────────────────────────────────── */
  .status-tag {
    display: inline-flex;
    align-items: center;
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 500;
    transition: all 0.2s;
    
    &.draft {
      background: rgba(102, 126, 234, 0.12);
      color: #667eea;
    }
    
    &.pending, &.approving {
      background: rgba(251, 146, 60, 0.12);
      color: #f97316;
    }
    
    &.approved, &.signed {
      background: rgba(34, 197, 94, 0.12);
      color: #22c55e;
    }
    
    &.archived {
      background: rgba(102, 126, 234, 0.12);
      color: #667eea;
    }
    
    &.terminated {
      background: rgba(239, 68, 68, 0.12);
      color: #ef4444;
    }
  }
  
  .type-tag {
    display: inline-flex;
    align-items: center;
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 500;
    background: var(--primary-gradient);
    color: #fff;
  }
  
  .contract-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    
    .tag-item {
      margin: 0;
    }
  }
  
  .no-tags {
    color: var(--text-placeholder, #999);
  }
}

/* AI Analysis Dialog Styles */
.ai-analysis-dialog {
  :deep(.el-dialog__body) {
    padding: 20px 24px;
  }
}

.ai-analysis-content {
  .score-section {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 20px;
    border-radius: 12px;
    margin-bottom: 20px;
    
    .score-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      
      .score-label {
        color: rgba(255, 255, 255, 0.9);
        font-size: 14px;
        font-weight: 500;
      }
      
      .score-value {
        font-size: 32px;
        font-weight: 700;
        color: #fff;
        text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
        
        &.score-high {
          color: #90EE90;
        }
        
        &.score-medium {
          color: #FFD700;
        }
        
        &.score-low {
          color: #FF6B6B;
        }
      }
    }
    
    :deep(.el-progress-bar__outer) {
      background-color: rgba(255, 255, 255, 0.3);
    }
  }
  
  .section {
    margin-bottom: 24px;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .section-title {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 12px;
      font-size: 15px;
      font-weight: 600;
      color: var(--text-primary, #333);
      
      .el-icon {
        color: var(--el-color-primary);
      }
    }
    
    .sub-section-title {
      font-size: 13px;
      font-weight: 600;
      color: var(--text-secondary, #666);
      margin: 16px 0 8px;
    }
    
    .summary-text {
      padding: 12px 16px;
      background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
      border-radius: 8px;
      line-height: 1.6;
      color: var(--text-primary, #333);
      margin: 0;
    }
  }
  
  .risks-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .risk-item {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      padding: 12px;
      border-radius: 8px;
      border-left: 4px solid var(--el-border-color);
      background: var(--bg-color, #fafafa);
      
      &.risk-high {
        border-left-color: #F56C6C;
        background: rgba(245, 108, 108, 0.08);
      }
      
      &.risk-medium {
        border-left-color: #E6A23C;
        background: rgba(230, 162, 60, 0.08);
      }
      
      &.risk-low {
        border-left-color: #909399;
        background: rgba(144, 147, 153, 0.08);
      }
      
      .risk-level-badge {
        flex-shrink: 0;
        margin-top: 2px;
      }
      
      .risk-content {
        flex: 1;
        line-height: 1.5;
        color: var(--text-primary, #333);
        word-break: break-word;
      }
    }
  }
  
  .suggestions-list {
    list-style: none;
    padding: 0;
    margin: 0;
    
    li {
      padding: 10px 12px 10px 32px;
      background: linear-gradient(135deg, #e0f7fa 0%, #b2ebf2 100%);
      border-radius: 6px;
      margin-bottom: 8px;
      position: relative;
      line-height: 1.5;
      color: var(--text-primary, #333);
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .suggestion-index {
        position: absolute;
        left: 12px;
        top: 10px;
        width: 18px;
        height: 18px;
        background: var(--el-color-primary);
        color: #fff;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 11px;
        font-weight: 600;
      }
    }
  }
  
  .key-info-descriptions {
    :deep(.el-descriptions__label) {
      background: var(--bg-color-page, #f5f7fa);
      font-weight: 600;
    }
    
    .description-item {
      :deep(.el-descriptions__content) {
        word-break: break-word;
      }
    }
  }
  
  .key-clauses {
    .clauses-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      
      .clause-tag {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
        border: none;
        padding: 6px 14px;
      }
    }
  }
}
</style>
