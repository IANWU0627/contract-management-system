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
        <el-table-column prop="contractNo" :label="$t('contract.no')" min-width="220" class-name="contract-no-column">
          <template #default="{ row }">
            <span class="contract-no-full">{{ row.contractNo }}</span>
          </template>
        </el-table-column>
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
            {{ getCurrencySymbol(row.currency) }}{{ formatAmount(row.amount) }}
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
          <el-dropdown-item command="aiAnalyze" v-if="row.id">{{ $t('ai.analyze') }}</el-dropdown-item>
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
    <el-dialog v-model="aiDialogVisible" :title="$t('ai.assistantTitle')" width="820px" class="ai-analysis-dialog">
      <div v-loading="aiLoading" class="ai-analysis-content">
        <!-- 风险评分 -->
        <div class="score-section" v-if="aiResult.score !== undefined">
          <div class="score-header">
            <span class="score-label">{{ $t('ai.riskScore') }}</span>
            <span class="score-value" :class="getScoreClass(aiResult.score)">{{ aiResult.score }}</span>
          </div>
          <el-progress :percentage="aiResult.score" :color="getScoreColor(aiResult.score)" :stroke-width="12" />
        </div>
        <el-alert
          v-if="isAiRiskWarning"
          type="warning"
          :title="$t('ai.riskThresholdAlert', { threshold: aiPreferences.riskThreshold })"
          :closable="false"
          show-icon
          style="margin-bottom: 16px"
        />

        <!-- 合同摘要 -->
        <div class="section">
          <div class="section-title">
            <el-icon><Document /></el-icon>
            <span>{{ $t('ai.summary') }}</span>
          </div>
          <p class="summary-text">{{ aiResult.summary }}</p>
        </div>

        <!-- 谈判要点（助手） -->
        <div class="section" v-if="aiResult.negotiationPoints && aiResult.negotiationPoints.length > 0">
          <div class="section-title">
            <el-icon><ChatLineRound /></el-icon>
            <span>{{ $t('ai.negotiationPoints') }}</span>
            <el-tag size="small" type="primary">{{ aiResult.negotiationPoints.length }}</el-tag>
          </div>
          <ul class="negotiation-list">
            <li v-for="(pt, idx) in aiResult.negotiationPoints" :key="idx">
              <span class="suggestion-index">{{ Number(idx) + 1 }}.</span>
              {{ pt }}
            </li>
          </ul>
        </div>

        <!-- 条款覆盖检查（助手） -->
        <div class="section" v-if="aiResult.missingClauseChecks && aiResult.missingClauseChecks.length > 0">
          <div class="section-title">
            <el-icon><List /></el-icon>
            <span>{{ $t('ai.missingClauseChecks') }}</span>
            <el-tag size="small">{{ aiResult.missingClauseChecks.length }}</el-tag>
          </div>
          <el-table :data="aiResult.missingClauseChecks" size="small" border class="clause-check-table">
            <el-table-column prop="topic" :label="$t('ai.checkTopic')" min-width="120" />
            <el-table-column :label="$t('ai.checkCovered')" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.present ? 'success' : 'danger'" size="small">
                  {{ row.present ? $t('ai.yes') : $t('ai.no') }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="$t('ai.checkSeverity')" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="severityTagType(row.severity)" size="small">{{ row.severity }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="note" :label="$t('ai.checkNote')" min-width="160" show-overflow-tooltip />
          </el-table>
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
              <div class="risk-main">
                <div class="risk-content">{{ getRiskContent(risk) }}</div>
                <blockquote v-if="getRiskAnchor(risk)" class="risk-anchor">{{ getRiskAnchor(risk) }}</blockquote>
              </div>
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
              <span class="suggestion-index">{{ Number(idx) + 1 }}.</span>
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

        <div class="section" v-if="aiResult.actionSuggestions && aiResult.actionSuggestions.length > 0">
          <div class="section-title">
            <el-icon><MagicStick /></el-icon>
            <span>{{ $t('ai.actionSuggestions') }}</span>
            <el-tag size="small" type="success">{{ aiResult.actionSuggestions.length }}</el-tag>
          </div>
          <div class="action-suggestion-list">
            <div v-for="(item, idx) in aiResult.actionSuggestions" :key="`${item.action}-${idx}`" class="action-suggestion-item">
              <div class="action-suggestion-main">
                <div class="action-suggestion-title">{{ item.title }}</div>
                <div class="action-suggestion-description">{{ item.description }}</div>
              </div>
              <el-button
                type="primary"
                size="small"
                :loading="aiActionLoading === item.action"
                @click="handleAiSuggestedAction(item)"
              >
                {{ getAiActionButtonText(item.action) }}
              </el-button>
            </div>
          </div>
        </div>
        
        <div class="section" v-if="aiResult.clauseSuggestions && aiResult.clauseSuggestions.length > 0">
          <div class="section-title">
            <el-icon><Collection /></el-icon>
            <span>{{ $t('ai.clauseSuggestions') }}</span>
            <el-tag size="small" type="warning">{{ aiResult.clauseSuggestions.length }}</el-tag>
          </div>
          <div class="clause-suggestion-list">
            <div v-for="(item, idx) in aiResult.clauseSuggestions" :key="`${item.code || item.name}-${idx}`" class="clause-suggestion-item">
              <div class="clause-suggestion-header">
                <div class="clause-suggestion-title">{{ item.name || item.code }}</div>
                <el-tag size="small" effect="plain">{{ item.category || '-' }}</el-tag>
              </div>
              <div class="clause-suggestion-reason">{{ item.reason }}</div>
              <div class="clause-suggestion-snippet" v-if="item.snippet">
                <pre>{{ item.snippet }}</pre>
              </div>
              <div class="clause-suggestion-actions" v-if="item.snippet">
                <el-button size="small" @click="copyClauseSnippet(item.snippet)">{{ $t('ai.copyClause') }}</el-button>
                <el-button
                  size="small"
                  type="primary"
                  plain
                  :disabled="isClauseSuggestionSaved(item, Number(idx))"
                  :loading="aiClauseSavingKey === getClauseSuggestionKey(item, Number(idx))"
                  @click="saveAiClauseSuggestion(item, Number(idx))"
                >
                  {{ isClauseSuggestionSaved(item, Number(idx)) ? $t('ai.savedToClauseLibrary') : $t('ai.saveToClauseLibrary') }}
                </el-button>
                <el-button
                  v-if="getSavedClauseInfo(item, Number(idx))"
                  size="small"
                  link
                  type="primary"
                  @click="goToSavedClause(item, Number(idx))"
                >
                  {{ $t('ai.openClauseLibrary') }}
                </el-button>
              </div>
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
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type TableInstance } from 'element-plus'
import { getContractList, deleteContract, analyzeContract, batchDeleteContracts, batchUpdateStatus, batchApprove, batchSubmit, exportContractsExcel, batchEdit, submitContract, approveContract, rejectContract, signContract, terminateContract, startRenewalFlow, completeRenewalFlow, declineRenewalFlow } from '@/api/contract'
import { createClause } from '@/api/clause'
import { getContractTypeFieldConfig } from '@/api/contractTypeField'
import { getAllFolders } from '@/api/folder'
import { getContractCategories } from '@/api/contractCategory'
import { getQuickCodeByCode } from '@/api/quickCode'
import { getSystemConfigs } from '@/api/system'
import { Search, Refresh, Plus, ArrowDown, Delete, Edit, More, Download, CircleCheck, TopRight, Document, Warning, InfoFilled, MagicStick, Collection, ChatLineRound, List } from '@element-plus/icons-vue'
import EmptyState from '@/components/EmptyState.vue'
import { DEFAULT_CURRENCY, formatAmountByLocale, getCurrencySymbol } from '@/utils/currency'

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
  negotiationPoints: [],
  missingClauseChecks: [],
  risks: [],
  suggestions: [],
  keyInfo: {},
  actionSuggestions: [],
  clauseSuggestions: []
})
const aiActionLoading = ref('')
const aiClauseSavingKey = ref('')
const currentAiContract = ref<any>(null)
const savedClauseSuggestionKeys = ref<string[]>([])
const savedClauseSuggestionMap = ref<Record<string, { id?: number; code: string }>>({})
const aiPreferences = ref({
  riskThreshold: 50,
  enableCache: false,
  cacheTimeout: 60
})
const isAiRiskWarning = computed(() =>
  typeof aiResult.value?.score === 'number' && aiResult.value.score < aiPreferences.value.riskThreshold
)

/** Bump when cached payload shape changes; old keys are naturally orphaned. */
const AI_ANALYSIS_CACHE_VERSION = 'v3'

const getAiAnalysisCacheKey = (contractId: number) =>
  `ai-analysis:${AI_ANALYSIS_CACHE_VERSION}:${contractId}`

const createAiFallbackResult = (contractRow?: any) => ({
  summary: '未获取到完整 AI 结果，已为你提供基础分析视图。建议补充合同正文后重试，或配置可用的大模型接口以获得更完整输出。',
  score: 75,
  negotiationPoints: [
    '确认付款条件、发票类型与违约责任是否明确',
    '核对争议解决条款（管辖法院/仲裁地）是否符合公司政策',
    '检查交付与验收标准是否可量化、可执行'
  ],
  missingClauseChecks: [],
  risks: [{ level: 'low', content: '暂未识别到结构化风险结果，建议人工复核关键条款。', anchor: '' }],
  suggestions: ['建议完善合同正文后重新分析，以便生成更精准的风险与条款建议。'],
  keyInfo: {
    partyA: contractRow?.counterparty || '-',
    partyB: '-',
    amount: contractRow?.amount || '-',
    duration: contractRow?.startDate && contractRow?.endDate ? `${contractRow.startDate} ~ ${contractRow.endDate}` : '-',
    keyClauses: []
  },
  actionSuggestions: [],
  clauseSuggestions: []
})

const normalizeAiResult = (raw: any, contractRow?: any) => {
  if (!raw || typeof raw !== 'object') return createAiFallbackResult(contractRow)
  const fallback = createAiFallbackResult(contractRow)
  const keyInfo = raw.keyInfo && typeof raw.keyInfo === 'object' ? raw.keyInfo : {}
  const summary = typeof raw.summary === 'string' && raw.summary.trim() ? raw.summary : fallback.summary
  return {
    summary,
    score: Number.isFinite(Number(raw.score)) ? Number(raw.score) : fallback.score,
    negotiationPoints: Array.isArray(raw.negotiationPoints) ? raw.negotiationPoints : fallback.negotiationPoints,
    missingClauseChecks: Array.isArray(raw.missingClauseChecks) ? raw.missingClauseChecks : fallback.missingClauseChecks,
    risks: Array.isArray(raw.risks) && raw.risks.length > 0 ? raw.risks : fallback.risks,
    suggestions: Array.isArray(raw.suggestions) && raw.suggestions.length > 0 ? raw.suggestions : fallback.suggestions,
    keyInfo: {
      partyA: keyInfo.partyA || fallback.keyInfo.partyA,
      partyB: keyInfo.partyB || fallback.keyInfo.partyB,
      amount: keyInfo.amount || fallback.keyInfo.amount,
      duration: keyInfo.duration || fallback.keyInfo.duration,
      keyClauses: Array.isArray(keyInfo.keyClauses) ? keyInfo.keyClauses : []
    },
    actionSuggestions: Array.isArray(raw.actionSuggestions) ? raw.actionSuggestions : [],
    clauseSuggestions: Array.isArray(raw.clauseSuggestions) ? raw.clauseSuggestions : []
  }
}

type AiRuntimeConfig = {
  apiUrl: string
  apiKey: string
  model: string
  temperature: number
  maxTokens: number
}

/** 不含 riskThreshold：阈值仅影响展示，不应使缓存失效 */
const buildAiConfigSignature = (cfg: AiRuntimeConfig) =>
  [
    AI_ANALYSIS_CACHE_VERSION,
    cfg.apiUrl || '',
    cfg.model || '',
    String(cfg.temperature ?? ''),
    String(cfg.maxTokens ?? '')
  ].join('|')

const loadAiRuntimeConfig = async (): Promise<AiRuntimeConfig> => {
  const response = await getSystemConfigs()
  const configs = response?.data || {}

  aiPreferences.value = {
    riskThreshold: Number(configs.bd_risk_threshold ?? 50),
    enableCache: configs.bd_enable_cache === true || configs.bd_enable_cache === 'true',
    cacheTimeout: Number(configs.bd_cache_timeout ?? 60)
  }

  return {
    apiUrl: configs.ai_api_url || '',
    apiKey: configs.ai_api_key || '',
    model: configs.ai_model || 'gpt-3.5-turbo',
    temperature: Number(configs.bd_temperature ?? 0.7),
    maxTokens: Number(configs.bd_max_tokens ?? 1000)
  }
}

const getCachedAiAnalysis = (contractId: number, configSig: string) => {
  const raw = localStorage.getItem(getAiAnalysisCacheKey(contractId))
  if (!raw) return null

  try {
    const parsed = JSON.parse(raw)
    const expiresAt = Number(parsed.expiresAt || 0)
    if (Date.now() > expiresAt) {
      localStorage.removeItem(getAiAnalysisCacheKey(contractId))
      return null
    }
    if (parsed.sig !== configSig) {
      localStorage.removeItem(getAiAnalysisCacheKey(contractId))
      return null
    }
    return parsed.result || null
  } catch {
    localStorage.removeItem(getAiAnalysisCacheKey(contractId))
    return null
  }
}

const setCachedAiAnalysis = (contractId: number, result: any, configSig: string) => {
  const expiresAt = Date.now() + aiPreferences.value.cacheTimeout * 60 * 1000
  localStorage.setItem(
    getAiAnalysisCacheKey(contractId),
    JSON.stringify({ result, expiresAt, sig: configSig })
  )
}

const slugifyClauseCode = (value: string) => {
  return value
    .trim()
    .replace(/([a-z])([A-Z])/g, '$1_$2')
    .replace(/[^a-zA-Z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '')
    .toUpperCase()
}

const getClauseSuggestionKey = (item: any, idx: number) => {
  return `${item.code || item.name || 'AI'}-${idx}-${item.snippet || ''}`
}

const isClauseSuggestionSaved = (item: any, idx: number) => {
  return savedClauseSuggestionKeys.value.includes(getClauseSuggestionKey(item, idx))
}

const getSavedClauseInfo = (item: any, idx: number) => {
  return savedClauseSuggestionMap.value[getClauseSuggestionKey(item, idx)]
}

const buildClauseSuggestionCode = (item: any, idx: number) => {
  const contractId = currentAiContract.value?.id || 'GEN'
  const fromCode = slugifyClauseCode(item.code || '')
  if (fromCode) return `AI_${fromCode}_${contractId}_${idx + 1}_${Date.now()}`

  const fromName = slugifyClauseCode(item.name || '')
  if (fromName) return `AI_${fromName}_${contractId}_${idx + 1}_${Date.now()}`

  return `AI_CLAUSE_${contractId}_${idx + 1}_${Date.now()}`
}

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

const getRiskAnchor = (risk: any): string => {
  if (typeof risk === 'string') return ''
  const a = risk?.anchor ?? risk?.evidence
  return typeof a === 'string' && a.trim() ? a.trim() : ''
}

const severityTagType = (sev: string): 'success' | 'warning' | 'danger' | 'info' => {
  const s = (sev || '').toLowerCase()
  if (s === 'high') return 'danger'
  if (s === 'medium') return 'warning'
  if (s === 'low') return 'success'
  return 'info'
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
  return formatAmountByLocale(amount, locale.value)
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
    APPROVED: t('contract.statuses.approved'),
    SIGNED: t('contract.statuses.signed'),
    RENEWING: t('contract.statuses.renewing'),
    RENEWED: t('contract.statuses.renewed'),
    NOT_RENEWED: t('contract.statuses.notRenewed'),
    ARCHIVED: t('contract.statuses.archived'),
    TERMINATED: t('contract.statuses.terminated')
  }
  return map[status] || status
}

const getTypeTagType = (type: string) => {
  const cat = categories.value.find(c => c.code === type)
  if (cat) return cat.color ? 'primary' : 'info'
  const map: Record<string, string> = {
    PURCHASE: 'success',
    SALES: 'primary',
    LEASE: 'warning',
    EMPLOYMENT: 'info',
    SERVICE: 'danger',
    OTHER: 'info'
  }
  return map[type] || 'info'
}

const getStatusTagType = (status: string) => {
  const map: Record<string, string> = {
    DRAFT: 'info',
    PENDING: 'warning',
    APPROVING: 'warning',
    APPROVED: 'success',
    SIGNED: 'success',
    RENEWING: 'warning',
    RENEWED: 'success',
    NOT_RENEWED: 'info',
    ARCHIVED: 'info',
    TERMINATED: 'danger'
  }
  return map[status] || 'info'
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
    return `${getCurrencySymbol(DEFAULT_CURRENCY)}${formatAmountByLocale(Number(value), locale.value)}`
  }
  if (fieldType === 'number') {
    return formatAmountByLocale(Number(value), locale.value)
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
    await ElMessageBox.confirm(t('common.confirmDelete'), t('common.warning'), {
      type: 'warning'
    })
    await deleteContract(id)
    ElMessage.success(t('common.success'))
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || t('common.error'))
    }
  }
}

const getAiActionButtonText = (action: string) => {
  switch (action) {
    case 'submit': return t('contract.actions.submit')
    case 'approve': return t('contract.actions.approve')
    case 'reject': return t('contract.actions.reject')
    case 'sign': return t('contract.actions.sign')
    case 'terminate': return t('contract.actions.terminate')
    case 'startRenewal': return t('contract.actions.startRenewal')
    case 'completeRenewal': return t('contract.actions.completeRenewal')
    case 'declineRenewal': return t('contract.actions.declineRenewal')
    default: return t('common.confirm')
  }
}

const handleAiAnalyze = async (row: any, forceRefresh = false) => {
  const id = row.id
  currentAiContract.value = row
  savedClauseSuggestionKeys.value = []
  savedClauseSuggestionMap.value = {}
  aiDialogVisible.value = true
  aiLoading.value = true
  try {
    const aiConfig = await loadAiRuntimeConfig()
    const configSig = buildAiConfigSignature(aiConfig)

    if (aiPreferences.value.enableCache && !forceRefresh) {
      const cachedResult = getCachedAiAnalysis(id, configSig)
      if (cachedResult) {
        aiResult.value = normalizeAiResult(cachedResult, row)
        ElMessage.success(t('ai.cacheHit'))
        return
      }
    }

    const res = await analyzeContract(id, aiConfig)
    aiResult.value = normalizeAiResult(res.data, row)

    if (!aiConfig.apiUrl) {
      ElMessage.info(t('ai.offlineDemoHint'))
    }

    if (aiPreferences.value.enableCache) {
      setCachedAiAnalysis(id, aiResult.value, configSig)
    }
  } catch (error) {
    aiResult.value = createAiFallbackResult(row)
    ElMessage.error(t('ai.analysisFailed'))
  } finally {
    aiLoading.value = false
  }
}

const handleAiSuggestedAction = async (item: any) => {
  if (!currentAiContract.value?.id || !item?.action) return
  const contractId = currentAiContract.value.id
  aiActionLoading.value = item.action
  try {
    switch (item.action) {
      case 'submit':
        await ElMessageBox.confirm(item.description || t('ai.executeConfirm'), item.title || t('ai.actionSuggestions'), { type: 'info' })
        await submitContract(contractId)
        break
      case 'approve':
        await ElMessageBox.confirm(item.description || t('ai.executeConfirm'), item.title || t('ai.actionSuggestions'), { type: 'info' })
        await approveContract(contractId)
        break
      case 'sign':
        await ElMessageBox.confirm(item.description || t('ai.executeConfirm'), item.title || t('ai.actionSuggestions'), { type: 'info' })
        await signContract(contractId)
        break
      case 'reject': {
        const { value } = await ElMessageBox.prompt(item.description || t('contract.rejectReason'), item.title || t('contract.actions.reject'), {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel'),
          inputValue: item.reason || ''
        })
        await rejectContract(contractId, value)
        break
      }
      case 'terminate': {
        const { value } = await ElMessageBox.prompt(item.description || t('contract.terminateReason'), item.title || t('contract.actions.terminate'), {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel'),
          inputPlaceholder: t('contract.terminateReasonPlaceholder'),
          inputValue: item.reason || ''
        })
        await terminateContract(contractId, value)
        break
      }
      case 'startRenewal': {
        const { value } = await ElMessageBox.prompt(item.description || t('contract.renewalReason'), item.title || t('contract.actions.startRenewal'), {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel'),
          inputPlaceholder: t('contract.renewalReasonPlaceholder'),
          inputValue: item.reason || ''
        })
        await startRenewalFlow(contractId, value)
        break
      }
      case 'completeRenewal': {
        const { value } = await ElMessageBox.prompt(item.description || t('contract.renewalCompleteReason'), item.title || t('contract.actions.completeRenewal'), {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel'),
          inputPlaceholder: t('contract.renewalCompleteReasonPlaceholder'),
          inputValue: item.reason || ''
        })
        await completeRenewalFlow(contractId, value)
        break
      }
      case 'declineRenewal': {
        const { value } = await ElMessageBox.prompt(item.description || t('contract.notRenewReason'), item.title || t('contract.actions.declineRenewal'), {
          confirmButtonText: t('common.confirm'),
          cancelButtonText: t('common.cancel'),
          inputPlaceholder: t('contract.notRenewReasonPlaceholder'),
          inputValue: item.reason || ''
        })
        await declineRenewalFlow(contractId, value)
        break
      }
      default:
        return
    }
    ElMessage.success(t('ai.executeSuccess'))
    localStorage.removeItem(getAiAnalysisCacheKey(contractId))
    await fetchData()
    const updated = contracts.value.find((contract: any) => contract.id === contractId) || currentAiContract.value
    currentAiContract.value = updated
    await handleAiAnalyze(updated, true)
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || t('common.error'))
    }
  } finally {
    aiActionLoading.value = ''
  }
}

const copyClauseSnippet = async (snippet: string) => {
  if (!snippet) return
  try {
    await navigator.clipboard.writeText(snippet)
    ElMessage.success(t('ai.copyClauseSuccess'))
  } catch (_error) {
    ElMessage.error(t('common.error'))
  }
}

const saveAiClauseSuggestion = async (item: any, idx: number) => {
  if (!item?.snippet) return
  const suggestionKey = getClauseSuggestionKey(item, idx)
  if (savedClauseSuggestionKeys.value.includes(suggestionKey)) return

  aiClauseSavingKey.value = suggestionKey
  try {
    const clauseCode = buildClauseSuggestionCode(item, idx)
    const clauseName = item.name || item.code || `${t('ai.clauseDefaultName')} ${idx + 1}`
    const clauseNameEn = item.nameEn || item.name || item.code || clauseCode
    const res = await createClause({
      code: clauseCode,
      name: clauseName,
      nameEn: clauseNameEn,
      category: item.category || t('ai.defaultClauseCategory'),
      content: item.snippet,
      description: item.reason || `${t('ai.generatedFromContract')}: ${currentAiContract.value?.title || '-'}`
    })
    savedClauseSuggestionKeys.value = [...savedClauseSuggestionKeys.value, suggestionKey]
    savedClauseSuggestionMap.value = {
      ...savedClauseSuggestionMap.value,
      [suggestionKey]: {
        id: res?.data,
        code: clauseCode
      }
    }
    ElMessage.success(t('ai.saveClauseSuccess'))
  } catch (error: any) {
    ElMessage.error(error?.message || t('ai.saveClauseFailed'))
  } finally {
    aiClauseSavingKey.value = ''
  }
}

const goToSavedClause = (item: any, idx: number) => {
  const savedInfo = getSavedClauseInfo(item, idx)
  if (!savedInfo) return

  router.push({
    path: '/clauses',
    query: {
      keyword: savedInfo.code,
      highlightId: savedInfo.id ? String(savedInfo.id) : undefined,
      open: savedInfo.id ? 'edit' : undefined
    }
  })
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
      handleAiAnalyze(row)
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
  aiResult.value = { summary: '', score: 75, negotiationPoints: [], missingClauseChecks: [], risks: [], suggestions: [], keyInfo: {}, actionSuggestions: [], clauseSuggestions: [] }
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
    gap: 12px;
    flex-wrap: wrap;
    min-width: 0;
    
    .page-title {
      font-size: 24px;
      font-weight: 700;
      margin: 0;
      background: var(--primary-gradient);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      max-width: 100%;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
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
    flex-wrap: wrap;
  }
  
  .filter-item-wide {
    flex: 1;
    min-width: 240px;
    
    :deep(.el-input__wrapper) {
      border-radius: 8px;
    }
  }
  
  .filter-actions {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
    flex-wrap: wrap;
  }
  
  /* ── Table Section ─────────────────────────────── */
  .table-section {
    background: var(--bg-card);
    border: 1px solid var(--border-color);
    border-radius: 12px;
    overflow: hidden;
    
    :deep(.el-table) {
      .el-table__header-wrapper th .cell {
        word-break: normal;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        line-height: 1.2;
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
        max-width: 320px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        
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
        flex-wrap: wrap;
        min-width: 0;
        
        .el-checkbox {
          margin-right: 0;
        }
        
        .selected-count {
          font-size: 13px;
          color: var(--text-secondary);
          padding: 0 8px;
          border-right: 1px solid var(--border-color);
          margin-right: 4px;
          white-space: nowrap;
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
          white-space: nowrap;
          
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
      --el-table-border-color: var(--border-color);
      --el-table-header-bg-color: var(--bg-hover);
      --el-table-header-text-color: var(--text-primary);
      --el-table-row-hover-bg-color: var(--bg-hover);
      --el-table-cell-padding: 16px 12px;
      
      th {
        font-weight: 600;
        font-size: 13px;
      }
      
      td {
        font-size: 14px;
        color: var(--text-regular);
        transition: background 0.2s ease;
      }
      
      tr:hover > td {
        background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%) !important;
      }
      
      .cell {
        display: flex;
        align-items: center;
        gap: 8px;
        min-width: 0;
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
      border-top: 1px solid var(--border-color);
      min-width: 0;
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

  .contract-no-full {
    white-space: nowrap;
    font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
    color: var(--text-primary);
  }

  :deep(.contract-no-column .cell) {
    white-space: nowrap !important;
    overflow: visible !important;
    text-overflow: clip !important;
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
      color: var(--text-primary);
      
      .el-icon {
        color: var(--el-color-primary);
      }
    }
    
    .sub-section-title {
      font-size: 13px;
      font-weight: 600;
      color: var(--text-secondary);
      margin: 16px 0 8px;
    }
    
    .summary-text {
      padding: 12px 16px;
      background: var(--bg-hover);
      border-radius: 8px;
      line-height: 1.6;
      color: var(--text-primary);
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
      background: var(--bg-color);
      
      &.risk-high {
        border-left-color: #F56C6C;
        background: rgba(245, 108, 108, 0.08);
      }
      
      &.risk-medium {
        border-left-color: #E6A23C;
        background: rgba(230, 162, 60, 0.08);
      }
      
      &.risk-low {
        border-left-color: var(--text-secondary);
        background: rgba(144, 147, 153, 0.08);
      }
      
      .risk-level-badge {
        flex-shrink: 0;
        margin-top: 2px;
      }
      
      .risk-main {
        flex: 1;
        min-width: 0;
        display: flex;
        flex-direction: column;
        gap: 8px;
      }

      .risk-content {
        line-height: 1.5;
        color: var(--text-primary);
        word-break: break-word;
      }

      .risk-anchor {
        margin: 0;
        padding: 8px 10px;
        font-size: 12px;
        line-height: 1.45;
        color: var(--el-text-color-secondary);
        background: var(--el-fill-color-light);
        border-radius: 6px;
        border-left: 3px solid var(--el-color-primary);
      }
    }
  }

  .negotiation-list {
    list-style: none;
    padding: 0;
    margin: 0;

    li {
      padding: 10px 12px 10px 32px;
      background: var(--bg-hover);
      border-radius: 6px;
      margin-bottom: 8px;
      position: relative;
      line-height: 1.5;
      color: var(--text-primary);

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

  .clause-check-table {
    margin-top: 4px;
  }
  
  .suggestions-list {
    list-style: none;
    padding: 0;
    margin: 0;
    
    li {
      padding: 10px 12px 10px 32px;
      background: var(--bg-hover);
      border-radius: 6px;
      margin-bottom: 8px;
      position: relative;
      line-height: 1.5;
      color: var(--text-primary);
      
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

  .action-suggestion-list {
    display: flex;
    flex-direction: column;
    gap: 10px;

    .action-suggestion-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 16px;
      padding: 14px 16px;
      border: 1px solid var(--border-color);
      border-radius: 10px;
      background: var(--bg-hover);
    }

    .action-suggestion-main {
      flex: 1;
      min-width: 0;
    }

    .action-suggestion-title {
      font-size: 14px;
      font-weight: 600;
      color: var(--text-primary);
      margin-bottom: 4px;
    }

    .action-suggestion-description {
      font-size: 13px;
      color: var(--text-secondary);
      line-height: 1.5;
    }
  }

  .clause-suggestion-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .clause-suggestion-item {
    padding: 14px 16px;
    border-radius: 10px;
    border: 1px solid var(--border-color);
    background: var(--bg-hover);
  }

  .clause-suggestion-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 8px;
  }

  .clause-suggestion-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-primary);
  }

  .clause-suggestion-reason {
    font-size: 13px;
    color: var(--text-secondary);
    line-height: 1.6;
    margin-bottom: 10px;
  }

  .clause-suggestion-snippet {
    background: var(--bg-card);
    border-radius: 8px;
    border: 1px dashed var(--border-color);
    padding: 10px 12px;
    margin-bottom: 10px;

    pre {
      margin: 0;
      white-space: pre-wrap;
      word-break: break-word;
      font-size: 12px;
      line-height: 1.7;
      color: var(--text-primary);
      font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
    }
  }

  .clause-suggestion-actions {
    display: flex;
    justify-content: flex-end;
  }
  
  .key-info-descriptions {
    :deep(.el-descriptions__label) {
      background: var(--bg-color-page);
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
