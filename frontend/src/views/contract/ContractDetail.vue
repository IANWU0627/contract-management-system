<template>
  <div class="contract-detail">
    <div class="page-header">
      <el-button @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        {{ $t('common.back') }}
      </el-button>
      <h1 class="page-title">{{ contract.title }}</h1>
    </div>
    
    <el-row :gutter="20">
      <!-- 基本信息 -->
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <span class="header-title">{{ $t('common.basicInfo') }}</span>
                <el-tag :type="getStatusTagType(contract.status)" size="default" effect="dark">
                  {{ formatStatus(contract.status) }}
                </el-tag>
              </div>
              <div class="header-actions">
                <el-button text @click="handleToggleFavorite" class="action-btn favorite-btn" :class="{ active: isFavorited }">
                  <el-icon><StarFilled v-if="isFavorited" /><Star v-else /></el-icon>
                  {{ isFavorited ? $t('contract.unfavorite') : $t('contract.favorite') }}
                </el-button>
                <el-dropdown trigger="click" @command="handleAddTag">
                  <el-button type="default" class="action-btn">
                    <el-icon><Collection /></el-icon>
                    {{ $t('contract.tag') }}
                    <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item v-for="tag in availableTags" :key="tag.id" :command="tag.id">
                        <span class="tag-dot" :style="{ background: tag.color }"></span>
                        {{ tag.name }}
                      </el-dropdown-item>
                      <el-dropdown-item divided command="manage" v-if="isAdmin">
                        {{ $t('menu.tagManagement') }}
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                <el-button type="primary" @click="handleEdit" v-if="contract.status === 'DRAFT'" class="action-btn primary">
                  <el-icon><Edit /></el-icon>
                  {{ $t('common.edit') }}
                </el-button>
                <el-button type="success" @click="handleDownloadPdf" class="action-btn success">
                  <el-icon><Download /></el-icon>
                  {{ $t('contract.actions.download') }}
                </el-button>
                <el-button type="info" @click="handleCopy" class="action-btn">
                  <el-icon><DocumentCopy /></el-icon>
                  {{ $t('contract.actions.copy') }}
                </el-button>
              </div>
            </div>
          </template>
          
          <el-descriptions :column="2" border class="basic-info-descriptions">
            <el-descriptions-item :label="$t('contract.name')" :span="2">{{ contract.title || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="$t('contract.no')">{{ contract.contractNo || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="$t('contract.type')">
              <el-tag>{{ formatType(contract.type) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('contract.status')">
              <el-tag :type="getStatusTagType(contract.status)">{{ formatStatus(contract.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('contract.amount')">
              {{ getCurrencySymbol(contract.currency || DEFAULT_CURRENCY) }}{{ formatAmount(contract.amount) }}
              <el-tag size="small" class="currency-tag">{{ contract.currency || DEFAULT_CURRENCY }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('contract.startDate')">{{ contract.startDate || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="$t('contract.endDate')">{{ contract.endDate || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="$t('folder.title')">
              <template v-if="contractFolder">
                <span :style="{ color: contractFolder.color }">●</span> {{ contractFolder.name }}
              </template>
              <template v-else>-</template>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('contract.createdBy')">{{ contract.createdBy || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="$t('contract.createdAt')">{{ contract.createdAt || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="$t('contract.timezone')">{{ contract.timezone || Intl.DateTimeFormat().resolvedOptions().timeZone }}</el-descriptions-item>
            <el-descriptions-item :label="$t('contract.version')">{{ contract.version || '1.0' }}</el-descriptions-item>
            <el-descriptions-item :label="$t('contract.relationType')">
              <el-tag size="small">{{ formatRelationType(contract.relationType) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('contract.parentContract')">
              <template v-if="contract.relatedContracts?.parentContract">
                <el-link type="primary" @click="goToContract(contract.relatedContracts.parentContract.id)">
                  {{ contract.relatedContracts.parentContract.contractNo }} - {{ contract.relatedContracts.parentContract.title }}
                </el-link>
              </template>
              <template v-else>-</template>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('contract.supplementContracts')" :span="2">
              <div v-if="(contract.relatedContracts?.supplements || []).length > 0" class="supplement-links">
                <el-link
                  v-for="item in contract.relatedContracts.supplements"
                  :key="item.id"
                  type="primary"
                  @click="goToContract(item.id)"
                >
                  {{ item.contractNo }} - {{ item.title }}
                </el-link>
              </div>
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('contract.remark')" :span="2">{{ contract.remark || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="$t('contract.tag')" :span="2">
              <div class="tag-list" v-if="contractTags.length > 0">
                <el-tag 
                  v-for="tag in contractTags" 
                  :key="tag.id" 
                  size="small" 
                  closable 
                  @close="handleRemoveTag(tag.id)"
                  :style="{ background: tag.color + '20', borderColor: tag.color, color: tag.color }"
                >
                  {{ tag.name }}
                </el-tag>
              </div>
              <span v-else>-</span>
            </el-descriptions-item>
          </el-descriptions>
          
          <!-- 相对方信息 -->
          <div class="counterparties-section" v-if="counterparties.length > 0">
            <h3 class="section-title">
              <el-icon><User /></el-icon>
              {{ $t('contract.counterparts') }}
            </h3>
            <div class="counterparties-grid">
              <div v-for="(cp, index) in counterparties" :key="index" class="counterparty-card">
                <div class="cp-header">
                  <el-tag :type="getPartyTagType(cp.type)">{{ getPartyLabel(cp.type) }}</el-tag>
                  <span class="cp-name">{{ cp.name }}</span>
                </div>
                <div class="cp-details">
                  <div v-if="cp.contactPerson" class="cp-item">
                    <span class="cp-label">{{ $t('contract.counterparty.contact') }}</span>
                    <span class="cp-value">{{ cp.contactPerson }}</span>
                  </div>
                  <div v-if="cp.contactPhone" class="cp-item">
                    <span class="cp-label">{{ $t('contract.counterparty.phone') }}</span>
                    <span class="cp-value">{{ cp.contactPhone }}</span>
                  </div>
                  <div v-if="cp.contactEmail" class="cp-item">
                    <span class="cp-label">{{ $t('contract.counterparty.email') }}</span>
                    <span class="cp-value">{{ cp.contactEmail }}</span>
                  </div>
                  <div v-if="cp.address" class="cp-item">
                    <span class="cp-label">{{ $t('contract.counterparty.address') }}</span>
                    <span class="cp-value">{{ cp.address }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 付款信息 -->
          <div class="payment-section" v-if="contract.paymentMethod">
            <h3 class="section-title">
              <el-icon><Money /></el-icon>
              {{ $t('contract.payment') }}
            </h3>
            <div class="payment-info">
              <el-tag>{{ getPaymentMethodLabel(contract.paymentMethod) }}</el-tag>
              <span v-if="contract.paymentDays">{{ $t('contract.paymentDays') }}：{{ contract.paymentDays }}天</span>
              <span v-if="contract.paymentPercent">{{ $t('contract.paymentPercent') }}：{{ contract.paymentPercent }}%</span>
            </div>
            <p v-if="contract.paymentNote" class="payment-note">{{ contract.paymentNote }}</p>
          </div>
          
          <!-- 动态字段展示 -->
          <div class="dynamic-fields-section" v-if="dynamicFields.length > 0">
            <h3 class="section-title">
              <el-icon><Tickets /></el-icon>
              {{ $t('typeFieldConfig.dynamicFields') }}
            </h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item
                v-for="field in dynamicFields.filter(f => f.showInForm)"
                :key="field.id"
                :label="locale === 'zh' ? field.fieldLabel : (field.fieldLabelEn || field.fieldLabel)"
              >
                <span v-if="field.fieldType === 'select'" class="field-value">
                  {{ getSelectDisplayValue(field, contract.dynamicFields?.[field.fieldKey]) }}
                </span>
                <span v-else class="field-value">
                  {{ formatDynamicValue(contract.dynamicFields?.[field.fieldKey], field.fieldType) }}
                </span>
              </el-descriptions-item>
            </el-descriptions>
          </div>
          
          <!-- 文件管理展示区域 -->
          <div class="file-management-section">
            <div class="section-title-row">
              <div class="section-title">
                <el-icon><Files /></el-icon>
                <span>{{ $t('contract.fileManagement') }}</span>
              </div>
            </div>
            
            <div class="file-management-wrapper">
              <!-- 合同文件 -->
              <div class="contract-file-section">
                <div class="sub-section-header">
                  <el-icon><Document /></el-icon>
                  <span>{{ $t('contract.contractFile') }}</span>
                </div>
                <!-- 合同PDF文件 -->
                <div v-for="(file, idx) in contractFiles" :key="'pdf-' + idx" class="file-item">
                  <el-icon class="file-icon"><Document /></el-icon>
                  <span class="file-name">{{ file.fileName }}</span>
                  <el-tag size="small" type="info">PDF</el-tag>
                  <el-button type="primary" link size="small" @click="handlePreviewAttachment(file)" v-if="canPreviewFile(file.fileName)">
                    <el-icon><View /></el-icon>
                  </el-button>
                  <el-button type="success" link size="small" @click="handleDownloadAttachment(file)">
                    <el-icon><Download /></el-icon>
                  </el-button>
                </div>
                <div v-if="contractFiles.length === 0" class="empty-tip">
                  <span>{{ $t('contract.noContractFile') }}</span>
                </div>
              </div>
              
              <!-- 支持文件 -->
              <div class="support-files-section">
                <div class="sub-section-header">
                  <el-icon><Paperclip /></el-icon>
                  <span>{{ $t('contract.supportFiles') }}</span>
                </div>
                <div class="attachments-list" v-if="supportingFiles.length > 0">
                  <div v-for="(attachment, index) in supportingFiles" :key="index" class="attachment-item">
                    <div class="attachment-info">
                      <el-icon class="attachment-icon" :size="20">
                        <Document v-if="isDocumentFile(attachment.fileName)" />
                        <Picture v-else-if="isImageFile(attachment.fileName)" />
                        <DocumentCopy v-else />
                      </el-icon>
                      <div class="attachment-details">
                        <span class="attachment-name">{{ attachment.fileName }}</span>
                        <span class="attachment-size" v-if="attachment.fileSize">{{ formatFileSize(attachment.fileSize) }}</span>
                      </div>
                    </div>
                    <div class="attachment-actions">
                      <el-button 
                        type="primary" 
                        link 
                        size="small"
                        @click="handlePreviewAttachment(attachment)"
                        v-if="canPreviewFile(attachment.fileName)"
                      >
                        <el-icon><View /></el-icon>
                      </el-button>
                      <el-button 
                        type="success" 
                        link 
                        size="small"
                        @click="handleDownloadAttachment(attachment)"
                      >
                        <el-icon><Download /></el-icon>
                      </el-button>
                    </div>
                  </div>
                </div>
                <el-empty v-else :description="$t('contract.placeholder.noAttachments')" />
              </div>
            </div>
          </div>

        </el-card>
        
        <!-- 版本记录和评论 -->
        <el-card shadow="hover" style="margin-top: 20px">
          <el-tabs v-model="activeTab">
            <el-tab-pane :label="$t('contract.versions')" name="versions">
              <VersionHistory :contract-id="contractId" />
            </el-tab-pane>
            <el-tab-pane :label="$t('contract.comments')" name="comments">
              <ContractComments :contract-id="contractId" />
            </el-tab-pane>
            <el-tab-pane :label="$t('contract.renewalHistory')" name="renewals">
              <RenewalHistory :contract-id="contractId" />
            </el-tab-pane>
            <el-tab-pane :label="$t('contract.changeLog')" name="changelog">
              <ChangeLogList :contract-id="contractId" />
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
      
      <!-- 操作面板 -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>{{ $t('common.action') }}</span>
          </template>
          
          <div class="action-list">
            <el-button 
              type="primary" 
              :loading="actionLoading" 
              @click="handleSubmit"
              v-if="contract.status === 'DRAFT'"
              class="action-btn"
            >
              {{ $t('contract.actions.submit') }}
            </el-button>

            <el-button
              type="warning"
              :loading="actionLoading"
              @click="handleWithdraw"
              v-if="['PENDING', 'APPROVING'].includes(contract.status)"
              class="action-btn"
            >
              {{ $t('contract.actions.withdraw') }}
            </el-button>
            
            <el-button 
              type="success" 
              :loading="actionLoading" 
              @click="handleApprove"
              v-if="canApprove"
              class="action-btn"
            >
              {{ $t('contract.actions.approve') }}
            </el-button>
            
            <el-button 
              type="danger" 
              :loading="actionLoading" 
              @click="handleReject"
              v-if="canApprove"
              class="action-btn"
            >
              {{ $t('contract.actions.reject') }}
            </el-button>
            
            <el-button 
              :loading="actionLoading" 
              @click="handleSign"
              v-if="contract.status === 'APPROVED'"
              class="action-btn"
            >
              {{ $t('contract.actions.sign') }}
            </el-button>
            
            <el-button 
              :loading="actionLoading" 
              @click="handleArchive"
              v-if="contract.status === 'SIGNED'"
              class="action-btn"
            >
              {{ $t('contract.actions.archive') }}
            </el-button>

            <el-button
              type="warning"
              :loading="actionLoading"
              @click="handleStartRenewalFlow"
              v-if="canStartRenewal"
              class="action-btn"
            >
              {{ $t('contract.actions.startRenewal') }}
            </el-button>

            <el-button
              type="success"
              :loading="actionLoading"
              @click="handleCompleteRenewalFlow"
              v-if="canCompleteRenewal"
              class="action-btn"
            >
              {{ $t('contract.actions.completeRenewal') }}
            </el-button>

            <el-button
              :loading="actionLoading"
              @click="handleDeclineRenewalFlow"
              v-if="canCompleteRenewal"
              class="action-btn"
            >
              {{ $t('contract.actions.declineRenewal') }}
            </el-button>
            
            <el-button 
              type="danger" 
              :loading="actionLoading" 
              @click="handleTerminate"
              v-if="canTerminate"
              class="action-btn"
            >
              {{ $t('contract.actions.terminate') }}
            </el-button>
          </div>
        </el-card>

        <el-card shadow="hover" class="history-card">
          <template #header>
            <span>{{ $t('contract.approvalFlow') }}</span>
          </template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item :label="$t('contract.currentStage')">{{ formatStatus(approvalFlow.currentStatus || contract.status) }}</el-descriptions-item>
            <el-descriptions-item :label="$t('contract.blockedBy')">{{ approvalFlow.blockedBy || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="$t('contract.submittedAt')">{{ formatDateTime(approvalFlow.submittedAt) }}</el-descriptions-item>
            <el-descriptions-item :label="$t('contract.frozen')">{{ approvalFlow.frozen ? $t('common.yes') : $t('common.no') }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card shadow="hover" class="history-card">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
              <span>审批摘要卡片</span>
              <el-button size="small" :loading="approvalSummaryLoading" @click="loadApprovalSummary(true)">重新生成</el-button>
            </div>
          </template>
          <el-empty v-if="!approvalSummary.summary" description="暂无摘要，点击重新生成" />
          <template v-else>
            <p style="margin: 0 0 8px; line-height: 1.6;">{{ approvalSummary.summary }}</p>
            <el-tag size="small" type="success" v-if="approvalSummary.score !== null">风险评分：{{ approvalSummary.score }}</el-tag>
            <el-descriptions :column="1" border size="small" style="margin-top: 12px;" v-if="approvalSummary.keyTerms && Object.keys(approvalSummary.keyTerms).length">
              <el-descriptions-item label="甲方">{{ approvalSummary.keyTerms.partyA || '-' }}</el-descriptions-item>
              <el-descriptions-item label="乙方">{{ approvalSummary.keyTerms.partyB || '-' }}</el-descriptions-item>
              <el-descriptions-item label="金额">{{ approvalSummary.keyTerms.amount || '-' }}</el-descriptions-item>
              <el-descriptions-item label="期限">{{ approvalSummary.keyTerms.duration || '-' }}</el-descriptions-item>
            </el-descriptions>
            <div style="margin-top: 12px;" v-if="Array.isArray(approvalSummary.risks) && approvalSummary.risks.length">
              <el-tag
                v-for="(risk, idx) in approvalSummary.risks"
                :key="idx"
                :type="risk.level === 'high' ? 'danger' : (risk.level === 'medium' ? 'warning' : 'info')"
                style="margin: 0 6px 6px 0;"
              >
                {{ risk.level || 'low' }}: {{ risk.content || '-' }}
              </el-tag>
            </div>
          </template>
        </el-card>
        
        <!-- 审批历史 -->
        <el-card shadow="hover" class="history-card">
          <template #header>
            <span>{{ $t('contract.approvalHistory') }}</span>
          </template>
          
          <el-timeline v-if="approvalHistory.length > 0">
            <el-timeline-item
              v-for="(item, index) in approvalHistory"
              :key="index"
              :timestamp="formatDateTime(item.timestamp)"
              placement="top"
              :type="getApprovalType(item.action)"
            >
              <el-card>
                <div class="approval-item">
                  <div class="approval-header">
                    <el-tag :type="getApprovalTagType(item.action)" size="small">
                      {{ getApprovalActionLabel(item.action) }}
                    </el-tag>
                    <span class="approver">{{ item.approverName || 'Unknown' }}</span>
                  </div>
                  <p v-if="item.comment" class="approval-comment">{{ item.comment }}</p>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else :description="$t('common.none')" />
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 文件预览对话框 -->
    <el-dialog v-model="previewDialogVisible" :title="previewFile?.fileName || previewFile?.name || $t('contract.preview')" width="80%" top="5vh" destroy-on-close @close="closePreviewDialog">
      <div class="preview-container">
        <img v-if="isImageFile(previewFile?.fileName || previewFile?.name || '')" :src="previewBlobUrl" class="preview-image" />
        <iframe v-else-if="isPdfFile(previewFile?.fileName || previewFile?.name || '')" :src="previewBlobUrl" class="preview-pdf" />
        <div v-else class="preview-unsupported">
          <el-icon :size="64"><Document /></el-icon>
          <p>{{ $t('contract.previewNotSupported') }}</p>
          <el-button type="primary" @click="handleDownloadAttachment(previewFile)">
            {{ $t('common.download') }}
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { 
  getContract, 
  getContractPayload,
  submitContract, 
  withdrawContract,
  approveContract, 
  rejectContract,
  signContract,
  archiveContract,
  terminateContract,
  startRenewalFlow,
  completeRenewalFlow,
  declineRenewalFlow,
  downloadContractPdf,
  getApprovalHistory,
  copyContract,
  getApprovalSummary,
  generateApprovalSummary
} from '@/api/contract'
import { addFavorite, removeFavorite, checkFavorite } from '@/api/favorite'
import { getTags, addTagToContract, removeTagFromContract, getTagsByContract } from '@/api/tag'
import { getFolder } from '@/api/folder'
import { getContractTypeFieldConfig } from '@/api/contractTypeField'
import { getContractCategories } from '@/api/contractCategory'
import { getQuickCodeByCode } from '@/api/quickCode'
import { getCounterpartiesByContractId } from '@/api/counterparty'
import { getAttachmentsByContractId } from '@/api/attachment'
import { DEFAULT_CURRENCY, formatAmountByLocale, getCurrencySymbol } from '@/utils/currency'
import { ArrowLeft, Download, User, Money, Document, Paperclip, View, Picture, DocumentCopy, Star, StarFilled, Collection, ArrowDown, Edit, Tickets, Files } from '@element-plus/icons-vue'
import VersionHistory from './VersionHistory.vue'
import ContractComments from './ContractComments.vue'
import RenewalHistory from './RenewalHistory.vue'
import ChangeLogList from './ChangeLogList.vue'

const { t, locale } = useI18n()

const route = useRoute()
const router = useRouter()

const contractId = computed(() => Number(route.params.id))
const activeTab = ref('versions')

const contract = ref<any>({
  id: null,
  contractNo: '',
  title: '',
  type: '',
  counterparty: '',
  amount: 0,
  startDate: '',
  endDate: '',
  status: 'DRAFT',
  content: '',
  createdBy: ''
})

const approvalFlow = ref<any>({
  currentStatus: '',
  submittedAt: '',
  blockedBy: '-',
  frozen: false,
  steps: [],
  lastAction: '-',
  lastActionAt: ''
})
const approvalSummary = ref<any>({
  summary: '',
  score: null,
  keyTerms: {},
  risks: [],
  updatedAt: ''
})
const approvalSummaryLoading = ref(false)

const actionLoading = ref(false)
const approvalHistory = ref<any[]>([])
const isFavorited = ref(false)
const availableTags = ref<any[]>([])
const contractTags = ref<any[]>([])
const contractFolder = ref<any | null>(null)
const isAdmin = ref(true)
const dynamicFields = ref<any[]>([])
const quickCodeItemsCache = ref<Record<string, any[]>>({})
const categories = ref<any[]>([])
const counterparties = ref<any[]>([])
const attachments = ref<any[]>([])

const canApprove = computed(() => {
  return ['PENDING', 'APPROVING'].includes(contract.value.status)
})

const canTerminate = computed(() => {
  return ['SIGNED', 'APPROVED', 'ARCHIVED', 'RENEWING'].includes(contract.value.status)
})

const canStartRenewal = computed(() => {
  return ['SIGNED', 'APPROVED', 'ARCHIVED'].includes(contract.value.status)
})

const canCompleteRenewal = computed(() => {
  return contract.value.status === 'RENEWING'
})

const formatAmount = (amount: number) => {
  return formatAmountByLocale(amount, locale.value)
}

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

const formatRelationType = (relationType?: string) => {
  if (relationType === 'SUPPLEMENT') return t('contract.relationTypes.supplement')
  return t('contract.relationTypes.main')
}

const getStatusTagType = (status: string) => {
  const map: Record<string, string> = {
    DRAFT: 'info', PENDING: 'warning', APPROVING: 'warning',
    APPROVED: 'success', SIGNED: 'success', RENEWING: 'warning',
    RENEWED: 'success', NOT_RENEWED: 'info', ARCHIVED: 'info', TERMINATED: 'danger'
  }
  return map[status] || 'info'
}

const getPartyLabel = (type: string) => {
  const map: Record<string, string> = {
    partyA: t('contract.partyTypes.partyA'),
    partyB: t('contract.partyTypes.partyB'),
    partyC: t('contract.partyTypes.partyC'),
    partyD: t('contract.partyTypes.partyD'),
    partyE: t('contract.partyTypes.partyE'),
    partyF: t('contract.partyTypes.partyF'),
    partyG: t('contract.partyTypes.partyG'),
    partyH: t('contract.partyTypes.partyH'),
    partyI: t('contract.partyTypes.partyI'),
    partyJ: t('contract.partyTypes.partyJ')
  }
  return map[type] || type
}

const getPartyTagType = (type: string) => {
  const map: Record<string, string> = {
    partyA: 'primary', partyB: 'success', partyC: 'warning', partyD: 'info'
  }
  return map[type] || 'info'
}

const getPaymentMethodLabel = (method: string) => {
  const map: Record<string, string> = {
    oneTime: t('contract.paymentMethods.oneTime'),
    installment: t('contract.paymentMethods.installment'),
    prepay: t('contract.paymentMethods.prepay'),
    cod: t('contract.paymentMethods.cod'),
    monthly: t('contract.paymentMethods.monthly')
  }
  return map[method] || method
}

const formatDateTime = (dateStr: string | undefined) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString(locale.value === 'en' ? 'en-US' : 'zh-CN')
}

const getApprovalActionLabel = (action: string) => {
  const map: Record<string, string> = {
    APPROVE: t('contract.approvalActions.approve'),
    REJECT: t('contract.approvalActions.reject'),
    SUBMIT: t('contract.approvalActions.submit'),
    WITHDRAW: t('contract.approvalActions.withdraw')
  }
  return map[action] || action
}

const getApprovalTagType = (action: string) => {
  const map: Record<string, string> = {
    APPROVE: 'success',
    REJECT: 'danger',
    SUBMIT: 'warning',
    WITHDRAW: 'info'
  }
  return map[action] || 'info'
}

const getApprovalType = (action: string) => {
  const map: Record<string, string> = {
    APPROVE: 'success',
    REJECT: 'danger',
    SUBMIT: 'primary',
    WITHDRAW: 'info'
  }
  return map[action] || 'info'
}

// 文件附件相关方法
const contractFiles = computed(() => {
  return attachments.value.filter(att => att.fileCategory === 'contract')
})

const supportingFiles = computed(() => {
  return attachments.value.filter(att => att.fileCategory === 'support')
})

const isDocumentFile = (filename: string) => {
  const ext = filename.split('.').pop()?.toLowerCase()
  return ['pdf', 'doc', 'docx', 'txt'].includes(ext || '')
}

const isImageFile = (filename: string) => {
  const ext = filename.split('.').pop()?.toLowerCase()
  return ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp'].includes(ext || '')
}

const canPreviewFile = (filename: string) => {
  const ext = filename.split('.').pop()?.toLowerCase()
  return ['pdf', 'jpg', 'jpeg', 'png', 'gif', 'txt', 'webp', 'bmp'].includes(ext || '')
}

const isPdfFile = (filename: string) => {
  const ext = filename.split('.').pop()?.toLowerCase()
  return ['pdf'].includes(ext || '')
}

const formatFileSize = (size: number) => {
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
  if (size < 1024 * 1024 * 1024) return (size / (1024 * 1024)).toFixed(2) + ' MB'
  return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
}

const formatUploadTime = (time: string) => {
  return new Date(time).toLocaleString()
}

const previewDialogVisible = ref(false)
const previewFile = ref<any>(null)
const previewBlobUrl = ref('')

const handlePreviewAttachment = (attachment: any) => {
  previewFile.value = attachment
  const url = attachment.fileUrl || attachment.url
  const name = attachment.fileName || attachment.name
  
  if (!url) return
  
  // 使用fetch获取带认证的文件blob
  fetch(url, {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
    .then(response => {
      if (!response.ok) {
        throw new Error(t('common.error'))
      }
      return response.blob()
    })
    .then(blob => {
      // 如果之前有blob url，先释放
      if (previewBlobUrl.value) {
        URL.revokeObjectURL(previewBlobUrl.value)
      }
      previewBlobUrl.value = URL.createObjectURL(blob)
      previewDialogVisible.value = true
    })
    .catch(error => {
      console.error('Attachment preview failed:', error)
      ElMessage.error(t('common.error'))
    })
}

// 关闭对话框时释放blob url
const closePreviewDialog = () => {
  previewDialogVisible.value = false
  if (previewBlobUrl.value) {
    URL.revokeObjectURL(previewBlobUrl.value)
    previewBlobUrl.value = ''
  }
}

const handleDownloadAttachment = (attachment: any) => {
  if (!attachment) return
  const url = attachment.fileUrl || attachment.url
  const name = attachment.fileName || attachment.name
  if (!url) return
  
  // 使用fetch下载，确保正确处理文件类型
  fetch(url, {
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
    .then(response => {
      if (!response.ok) {
        throw new Error(t('common.error'))
      }
      return response.blob()
    })
    .then(blob => {
      // 根据文件扩展名确定MIME类型
      const getContentType = (filename: string) => {
        const ext = filename.split('.').pop()?.toLowerCase()
        const mimeTypes: Record<string, string> = {
          pdf: 'application/pdf',
          doc: 'application/msword',
          docx: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
          xls: 'application/vnd.ms-excel',
          xlsx: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          jpg: 'image/jpeg',
          jpeg: 'image/jpeg',
          png: 'image/png',
          gif: 'image/gif',
          txt: 'text/plain'
        }
        return mimeTypes[ext || ''] || 'application/octet-stream'
      }
      
      const fileBlob = new Blob([blob], { type: getContentType(name) })
      const downloadUrl = window.URL.createObjectURL(fileBlob)
      const link = document.createElement('a')
      link.href = downloadUrl
      link.download = name
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(downloadUrl)
    })
    .catch(error => {
      console.error('Attachment download failed:', error)
      // 如果fetch失败，回退到直接链接下载
      const link = document.createElement('a')
      link.href = url
      link.download = name
      link.click()
    })
}



const fetchContract = async () => {
  try {
    const [detailRes, payloadRes] = await Promise.all([
      getContract(contractId.value),
      getContractPayload(contractId.value).catch(() => ({ data: null }))
    ])
    const detailData = detailRes.data || {}
    const payloadData = payloadRes?.data || {}
    contract.value = {
      ...detailData,
      content: payloadData.content ?? detailData.content ?? '',
      templateVariables: payloadData.templateVariables ?? detailData.templateVariables ?? '',
      dynamicFields: payloadData.dynamicFields ?? detailData.dynamicFields ?? {},
      dynamicFieldValues: payloadData.dynamicFieldValues ?? detailData.dynamicFieldValues ?? ''
    }
    approvalFlow.value = detailData?.approvalFlow || approvalFlow.value
    await Promise.all([
      loadFavoriteStatus(), 
      loadContractTags(), 
      loadDynamicFields(), 
      loadApprovalHistory(), 
      loadApprovalSummary(false),
      loadContractFolder(),
      loadCounterparties(),
      loadAttachments()
    ])
  } catch (error) {
    ElMessage.error(t('contract.error.fetch'))
  }
}

const loadApprovalSummary = async (forceGenerate = false) => {
  approvalSummaryLoading.value = true
  try {
    const res = forceGenerate
      ? await generateApprovalSummary(contractId.value, true)
      : await getApprovalSummary(contractId.value)
    const data = res?.data || {}
    if (!data.summary && !forceGenerate) {
      const generated = await generateApprovalSummary(contractId.value, false)
      approvalSummary.value = generated?.data || approvalSummary.value
      return
    }
    approvalSummary.value = {
      summary: data.summary || '',
      score: data.score ?? null,
      keyTerms: data.keyTerms || {},
      risks: Array.isArray(data.risks) ? data.risks : [],
      updatedAt: data.updatedAt || ''
    }
  } catch (_error) {
    if (forceGenerate) {
      ElMessage.error('审批摘要生成失败')
    }
  } finally {
    approvalSummaryLoading.value = false
  }
}

const loadCounterparties = async () => {
  try {
    const res = await getCounterpartiesByContractId(contractId.value)
    const rows = Array.isArray(res) ? res : (res as any)?.data || []
    counterparties.value = rows.map((item: any) => ({
      ...item,
      contactPerson: item.contactPerson || item.contact || '',
      contactPhone: item.contactPhone || item.phone || '',
      contactEmail: item.contactEmail || item.email || ''
    }))
  } catch (error) {
    counterparties.value = []
  }
}

const loadAttachments = async () => {
  try {
    const res = await getAttachmentsByContractId(contractId.value)
    attachments.value = Array.isArray(res) ? res : (res as any)?.data || []
  } catch (error) {
    attachments.value = []
  }
}

const loadContractFolder = async () => {
  if (!contract.value.folderId) {
    contractFolder.value = null
    return
  }
  try {
    const res = await getFolder(contract.value.folderId)
    contractFolder.value = res.data
  } catch (error) {
    contractFolder.value = null
  }
}

const loadApprovalHistory = async () => {
  try {
    const res = await getApprovalHistory(contractId.value)
    approvalHistory.value = res.data || []
  } catch (error) {
    approvalHistory.value = []
  }
}

const loadFavoriteStatus = async () => {
  try {
    const res = await checkFavorite(contractId.value)
    isFavorited.value = res.data?.isFavorited || false
  } catch (error) {
    isFavorited.value = false
  }
}

const loadContractTags = async () => {
  try {
    const res = await getTags()
    availableTags.value = res.data?.list || []
    const tagRes = await getTagsByContract(contractId.value)
    contractTags.value = tagRes.data?.list || []
  } catch (error) {
    availableTags.value = []
    contractTags.value = []
  }
}

const loadDynamicFields = async () => {
  if (!contract.value.type) return
  try {
    const res = await getContractTypeFieldConfig(contract.value.type)
    dynamicFields.value = res.data?.fields || []
    
    for (const field of dynamicFields.value) {
      if (field.fieldType === 'select' && field.quickCodeId && !quickCodeItemsCache.value[field.quickCodeId]) {
        try {
          const qcRes = await getQuickCodeByCode(field.quickCodeId)
          quickCodeItemsCache.value[field.quickCodeId] = qcRes.data || []
        } catch (e) {
          quickCodeItemsCache.value[field.quickCodeId] = []
        }
      }
    }
  } catch (error) {
    dynamicFields.value = []
  }
}

const getSelectDisplayValue = (field: any, value: string) => {
  if (!value) return '-'
  if (field.quickCodeId) {
    const items = quickCodeItemsCache.value[field.quickCodeId] || []
    const item = items.find(i => i.code === value)
    if (item) {
      return locale.value === 'en' && item.meaningEn ? item.meaningEn : item.meaning
    }
  }
  return value
}

const formatDynamicValue = (value: any, fieldType: string) => {
  if (value === null || value === undefined || value === '') return '-'
  if (fieldType === 'currency') {
    return `${getCurrencySymbol(DEFAULT_CURRENCY)}${formatAmountByLocale(Number(value), locale.value)}`
  }
  if (fieldType === 'number') {
    return formatAmountByLocale(Number(value), locale.value)
  }
  return String(value)
}

const handleToggleFavorite = async () => {
  try {
    if (isFavorited.value) {
      await removeFavorite(contractId.value)
      ElMessage.success(t('common.success'))
      isFavorited.value = false
    } else {
      await addFavorite(contractId.value)
      ElMessage.success(t('common.success'))
      isFavorited.value = true
    }
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const handleAddTag = async (command: string | number) => {
  if (command === 'manage') {
    router.push('/tags')
    return
  }
  try {
    await addTagToContract(contractId.value, Number(command))
    ElMessage.success(t('common.success'))
    loadContractTags()
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const handleRemoveTag = async (tagId: number) => {
  try {
    await removeTagFromContract(contractId.value, tagId)
    ElMessage.success(t('common.success'))
    loadContractTags()
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const handleEdit = () => {
  router.push(`/contracts/${contractId.value}/edit`)
}

const goToContract = (id: number) => {
  router.push(`/contracts/${id}`)
}

const handleSubmit = async () => {
  actionLoading.value = true
  try {
    await submitContract(contractId.value)
    ElMessage.success(t('common.success'))
    fetchContract()
  } catch (error: any) {
    ElMessage.error(error.message || t('common.error'))
  } finally {
    actionLoading.value = false
  }
}

const handleWithdraw = async () => {
  try {
    const { value } = await ElMessageBox.prompt(t('contract.withdrawReason'), t('contract.actions.withdraw'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      inputPlaceholder: t('contract.withdrawReasonPlaceholder')
    })
    actionLoading.value = true
    await withdrawContract(contractId.value, value)
    ElMessage.success(t('common.success'))
    fetchContract()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || t('common.error'))
    }
  } finally {
    actionLoading.value = false
  }
}

const handleApprove = async () => {
  actionLoading.value = true
  try {
    await approveContract(contractId.value)
    ElMessage.success(t('common.success'))
    fetchContract()
  } catch (error: any) {
    ElMessage.error(error.message || t('common.error'))
  } finally {
    actionLoading.value = false
  }
}

const handleReject = async () => {
  try {
    const { value } = await ElMessageBox.prompt(t('contract.rejectReason'), t('contract.actions.reject'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel')
    })
    actionLoading.value = true
    await rejectContract(contractId.value, value)
    ElMessage.success(t('common.success'))
    fetchContract()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || t('common.error'))
    }
  } finally {
    actionLoading.value = false
  }
}

const handleSign = async () => {
  actionLoading.value = true
  try {
    await signContract(contractId.value)
    ElMessage.success(t('common.success'))
    fetchContract()
  } catch (error: any) {
    ElMessage.error(error.message || t('common.error'))
  } finally {
    actionLoading.value = false
  }
}

const handleArchive = async () => {
  actionLoading.value = true
  try {
    await archiveContract(contractId.value)
    ElMessage.success(t('common.success'))
    fetchContract()
  } catch (error: any) {
    ElMessage.error(error.message || t('common.error'))
  } finally {
    actionLoading.value = false
  }
}

const handleTerminate = async () => {
  try {
    const { value } = await ElMessageBox.prompt(t('contract.terminateReason'), t('contract.actions.terminate'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      inputPlaceholder: t('contract.terminateReasonPlaceholder')
    })
    await ElMessageBox.confirm(t('contract.confirmTerminate'), t('common.warning'), { type: 'warning' })
    actionLoading.value = true
    await terminateContract(contractId.value, value)
    ElMessage.success(t('common.success'))
    fetchContract()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || t('common.error'))
    }
  } finally {
    actionLoading.value = false
  }
}

const handleStartRenewalFlow = async () => {
  try {
    const { value } = await ElMessageBox.prompt(t('contract.renewalReason'), t('contract.actions.startRenewal'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      inputPlaceholder: t('contract.renewalReasonPlaceholder')
    })
    actionLoading.value = true
    await startRenewalFlow(contractId.value, value)
    ElMessage.success(t('common.success'))
    fetchContract()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || t('common.error'))
    }
  } finally {
    actionLoading.value = false
  }
}

const handleCompleteRenewalFlow = async () => {
  try {
    const { value } = await ElMessageBox.prompt(t('contract.renewalCompleteReason'), t('contract.actions.completeRenewal'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      inputPlaceholder: t('contract.renewalCompleteReasonPlaceholder')
    })
    actionLoading.value = true
    await completeRenewalFlow(contractId.value, value)
    ElMessage.success(t('common.success'))
    fetchContract()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || t('common.error'))
    }
  } finally {
    actionLoading.value = false
  }
}

const handleDeclineRenewalFlow = async () => {
  try {
    const { value } = await ElMessageBox.prompt(t('contract.notRenewReason'), t('contract.actions.declineRenewal'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      inputPlaceholder: t('contract.notRenewReasonPlaceholder')
    })
    actionLoading.value = true
    await declineRenewalFlow(contractId.value, value)
    ElMessage.success(t('common.success'))
    fetchContract()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || t('common.error'))
    }
  } finally {
    actionLoading.value = false
  }
}

const handleDownloadPdf = () => {
  downloadContractPdf(contractId.value)
}

const handleCopy = async () => {
  try {
    const res = await copyContract(contractId.value)
    ElMessage.success(t('contract.actions.copySuccess'))
    router.push(`/contracts/${res.data.id}/edit`)
  } catch (error: any) {
    ElMessage.error(error.message || t('contract.actions.copyError') || t('common.error'))
  }
}

const loadCategories = async () => {
  try {
    const res = await getContractCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
}

onMounted(async () => {
  await loadCategories()
  fetchContract()
})
</script>

<style scoped lang="scss">
.contract-detail {
  .page-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 20px;
    
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
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 12px;
    
    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;
      flex-wrap: wrap;
      
      .header-title {
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary);
      }
    }
    
    .header-actions {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
      
      .action-btn {
        display: flex;
        align-items: center;
        gap: 4px;
        padding: 6px 10px;
        border-radius: 6px;
        font-size: 13px;
        transition: all 0.2s ease;
        
        .el-icon {
          font-size: 14px;
        }
        
        &.primary {
          background: var(--primary-gradient);
          border: none;
          color: #fff;
          
          &:hover {
            box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
          }
        }
        
        &.success {
          background: var(--success-gradient);
          border: none;
          color: #fff;
          
          &:hover {
            box-shadow: 0 2px 8px rgba(34, 197, 94, 0.3);
          }
        }
      }
      
      .favorite-btn {
        &.active {
          color: #f5a623;
          
          .el-icon {
            color: #f5a623;
          }
        }
        
        &:hover {
          color: #f5a623;
          
          .el-icon {
            color: #f5a623;
          }
        }
      }
    }
  }
  
  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 20px 0 16px;
    
    .el-icon {
      font-size: 18px;
      color: #667eea;
    }
  }

  .basic-info-descriptions {
    :deep(.el-descriptions__table) {
      table-layout: fixed;
    }

    :deep(.el-descriptions__label) {
      width: 136px;
      min-width: 136px;
      max-width: 136px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      vertical-align: top;
      font-weight: 600;
    }

    :deep(.el-descriptions__content) {
      white-space: normal;
      word-break: break-word;
      overflow-wrap: anywhere;
      line-height: 1.6;
    }

    :deep(.el-tag) {
      max-width: 100%;
      vertical-align: middle;
    }

    .currency-tag {
      margin-left: 8px;
    }
  }
  
  /* 相对方信息 */
  .counterparties-section {
    margin: 20px 0;
    
    .counterparties-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 16px;
      
      .counterparty-card {
        background: var(--bg-hover);
        border: 2px solid var(--primary);
        border-radius: 12px;
        padding: 16px;
        transition: all 0.3s ease;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 16px rgba(102, 126, 234, 0.2);
          border-color: var(--primary-light);
        }
        
        .cp-header {
          display: flex;
          align-items: center;
          gap: 10px;
          margin-bottom: 12px;
          padding-bottom: 10px;
          border-bottom: 1px dashed var(--border-color);
          
          .cp-name {
            font-weight: 600;
            font-size: 15px;
            color: var(--text-primary);
          }
        }
        
        .cp-details {
          .cp-item {
            display: flex;
            margin-bottom: 8px;
            font-size: 13px;
            padding: 4px 0;
            
            .cp-label {
              width: 80px;
              color: var(--text-secondary);
              flex-shrink: 0;
              font-weight: 500;
            }
            
            .cp-value {
              color: var(--text-primary);
              word-break: break-all;
              flex: 1;
            }
          }
        }
      }
    }
  }
  
  /* 付款信息 */
  .payment-section {
    background: var(--bg-hover);
    border: 1px solid var(--border-color);
    border-radius: 10px;
    padding: 16px;
    margin: 20px 0;
    
    .payment-info {
      display: flex;
      gap: 16px;
      align-items: center;
      flex-wrap: wrap;
      
      span {
        font-size: 14px;
        color: var(--text-primary);
      }
    }
    
    .payment-note {
      margin-top: 10px;
      font-size: 13px;
      color: var(--text-secondary);
    }
  }
  
  /* 动态字段展示 */
  .dynamic-fields-section {
    margin: 20px 0;
    padding: 16px;
    background: var(--bg-hover);
    border: 1px solid var(--border-color);
    border-radius: 10px;
    
    .section-title {
      margin-top: 0;
    }
    
    .field-value {
      color: var(--text-primary);
      font-weight: 500;
    }
  }
  
  .content-section {
    h3 {
      margin-bottom: 12px;
      color: var(--text-primary);
    }
    
    .contract-content {
      white-space: pre-wrap;
      word-break: break-word;
      line-height: 1.8;
      color: var(--text-regular);
      max-height: 400px;
      overflow-y: auto;
      padding: 16px;
      background: var(--bg-hover);
      border-radius: 8px;
      font-size: 14px;
    }
  }
  
  .remark-section {
    margin-top: 16px;
    
    h3 {
      margin-bottom: 8px;
      color: var(--text-primary);
    }
    
    .remark-content {
      color: var(--text-secondary);
      font-size: 14px;
    }
  }
  
  /* 文件附件展示区域 */
  .attachment-section {
    margin: 20px 0;
    padding: 16px;
    background: var(--bg-hover);
    border: 1px solid var(--border-color);
    border-radius: 10px;
    
    .section-title {
      margin-top: 0;
      margin-bottom: 16px;
      display: flex;
      align-items: center;
      gap: 8px;
      color: var(--text-primary);
    }
    
    .attachments-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
      
      .attachment-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 16px;
        background: var(--bg-card);
        border: 1px solid var(--border-color);
        border-radius: 8px;
        transition: all 0.3s ease;
        
        &:hover {
          box-shadow: var(--shadow);
          border-color: var(--primary);
        }
        
        .attachment-info {
          display: flex;
          align-items: center;
          gap: 12px;
          flex: 1;
          min-width: 0;
          
          .attachment-icon {
            color: var(--primary);
            flex-shrink: 0;
          }
          
          .attachment-details {
            display: flex;
            flex-direction: column;
            gap: 4px;
            min-width: 0;
            
            .attachment-name {
              font-size: 14px;
              color: var(--text-primary);
              font-weight: 500;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
              max-width: 300px;
            }
            
            .attachment-size,
            .attachment-time {
              font-size: 12px;
              color: var(--text-secondary);
            }
          }
        }
        
        .attachment-actions {
          display: flex;
          gap: 8px;
          flex-shrink: 0;
        }
      }
    }
  }
  
  .action-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    
    .action-btn {
      width: 100%;
      margin: 0;
    }
  }
  
  .history-card {
    margin-top: 20px;
    
    .author {
      color: var(--text-secondary);
      font-size: 12px;
      margin-top: 4px;
    }
    
    .approval-item {
      .approval-header {
        display: flex;
        align-items: center;
        gap: 8px;
        
        .approver {
          color: var(--text-secondary);
          font-size: 13px;
        }
      }
      
      .approval-comment {
        margin-top: 8px;
        color: var(--text-primary);
        font-size: 14px;
      }
    }
  }
  
  .tag-list {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }

  .supplement-links {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .tag-dot {
    display: inline-block;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    margin-right: 6px;
  }
  
  .preview-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 400px;
    max-height: 70vh;
    overflow: auto;
  }
  
  .preview-image {
    max-width: 100%;
    max-height: 70vh;
    object-fit: contain;
  }
  
  .preview-pdf {
    width: 100%;
    height: 70vh;
    border: none;
  }
  
  .preview-unsupported {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 16px;
    color: var(--text-secondary);
    
    p {
      font-size: 16px;
    }
  }
  
  .file-management-section {
    margin: 20px 0;
    
    .section-title-row {
      display: flex;
      align-items: center;
      margin: 0 0 16px;
      padding-bottom: 8px;
      border-bottom: 1px solid var(--border-color);
      
      .section-title {
        display: flex;
        align-items: center;
        gap: 8px;
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary);
        
        .el-icon {
          color: var(--primary);
        }
      }
    }
    
    .file-management-wrapper {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
    }
    
    .contract-file-section,
    .support-files-section {
      background: var(--bg-hover);
      border: 1px solid var(--border-color);
      border-radius: 8px;
      padding: 16px;
      
      .sub-section-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 12px;
        font-weight: 500;
        color: var(--text-primary);
        
        .el-icon {
          color: var(--primary);
        }
      }
      
      .file-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 10px 12px;
        background: var(--bg-card);
        border: 1px solid var(--border-color);
        border-radius: 6px;
        
        .file-icon {
          color: var(--primary);
        }
        
        .file-name {
          flex: 1;
          font-size: 14px;
        }
      }
      
      .empty-tip {
        text-align: center;
        padding: 20px;
        color: var(--text-secondary);
        font-size: 13px;
      }
    }
    
    .attachments-list {
      display: flex;
      flex-direction: column;
      gap: 8px;
      
      .attachment-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 8px 12px;
        background: var(--bg-card);
        border: 1px solid var(--border-color);
        border-radius: 6px;
        
        .attachment-info {
          display: flex;
          align-items: center;
          gap: 8px;
          flex: 1;
          min-width: 0;
          
          .attachment-icon {
            color: var(--primary);
            flex-shrink: 0;
          }
          
          .attachment-details {
            display: flex;
            flex-direction: column;
            gap: 2px;
            min-width: 0;
            
            .attachment-name {
              font-size: 13px;
              color: var(--text-primary);
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
            }
            
            .attachment-size {
              font-size: 11px;
              color: var(--text-secondary);
            }
          }
        }
        
        .attachment-actions {
          display: flex;
          gap: 4px;
          flex-shrink: 0;
        }
      }
    }
  }
}
</style>
