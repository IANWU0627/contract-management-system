<template>
  <div class="contract-form">
    <div class="page-header">
      <el-button @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        {{ $t('common.back') }}
      </el-button>
      <h1 class="page-title">{{ isEdit ? $t('contract.edit') : $t('contract.create') }}</h1>
    </div>

    <el-card shadow="hover" class="form-card">
      <el-form ref="formRef" :model="form" :rules="currentStepRules" label-width="100px">
          <div class="info-section">
            <div class="section-title-row">
              <div class="section-title">
                <el-icon><Collection /></el-icon>
                <span>{{ $t('common.basicInfo') }}</span>
              </div>
              <div class="meta-info-bar">
                <span class="meta-item">
                  <span class="meta-label">{{ $t('contract.no') }}:</span>
                  <span class="meta-value contract-no">{{ form.contractNo }}</span>
                </span>
                <span class="meta-separator">|</span>
                <span class="meta-item">
                  <span class="meta-label">{{ $t('contract.createdBy') }}:</span>
                  <span class="meta-value">{{ form.createdBy || $t('contract.placeholder.currentUser') }}</span>
                </span>
                <span class="meta-separator">|</span>
                <span class="meta-item">
                  <span class="meta-label">{{ $t('contract.timezone') }}:</span>
                  <span class="meta-value">{{ form.timezone }}</span>
                </span>
                <span class="meta-separator">|</span>
                <span class="meta-item">
                  <span class="meta-label">{{ $t('contract.createdAt') }}:</span>
                  <span class="meta-value">{{ form.createdAt }}</span>
                </span>
              </div>
            </div>
            <el-row :gutter="20">
              <el-col :span="24">
                <el-form-item :label="$t('contract.name')" prop="title">
                  <el-input v-model="form.title" :placeholder="$t('contract.placeholder.name')" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8" :xs="24" :sm="12" :md="8">
                <el-form-item :label="$t('contract.type')" prop="type">
                  <el-select v-model="form.type" :placeholder="$t('common.placeholder.select')" style="width: 100%" @change="handleTypeChange">
                    <el-option v-for="cat in categories" :key="cat.code" :label="locale === 'en' && cat.nameEn ? cat.nameEn : cat.name" :value="cat.code" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8" :xs="24" :sm="12" :md="8">
                <el-form-item :label="$t('contract.amount')" prop="amount">
                  <el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8" :xs="24" :sm="12" :md="8">
                <el-form-item :label="$t('contract.currency')" prop="currency">
                  <el-select v-model="form.currency" style="width: 100%">
                    <el-option v-for="item in currencyOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8" :xs="24" :sm="12" :md="8">
                <el-form-item :label="$t('contract.startDate')" prop="startDate">
                  <el-date-picker v-model="form.startDate" type="date" :placeholder="$t('contract.placeholder.startDate')" value-format="YYYY-MM-DD" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8" :xs="24" :sm="12" :md="8">
                <el-form-item :label="$t('contract.endDate')" prop="endDate">
                  <el-date-picker v-model="form.endDate" type="date" :placeholder="$t('contract.placeholder.endDate')" value-format="YYYY-MM-DD" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8" :xs="24" :sm="12" :md="8">
                <el-form-item :label="$t('contract.folder')">
                  <el-select v-model="form.folderId" clearable :placeholder="$t('contract.placeholder.folder')" style="width: 100%">
                    <el-option v-for="folder in folders" :key="folder.id" :label="folder.name" :value="folder.id">
                      <span :style="{ color: folder.color }">●</span> {{ folder.name }}
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8" :xs="24" :sm="12" :md="8">
                <el-form-item :label="$t('contract.relationType')">
                  <el-select v-model="form.relationType" style="width: 100%">
                    <el-option :label="$t('contract.relationTypes.main')" value="MAIN" />
                    <el-option :label="$t('contract.relationTypes.supplement')" value="SUPPLEMENT" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="16" :xs="24" :sm="12" :md="16" v-if="form.relationType === 'SUPPLEMENT'">
                <el-form-item :label="$t('contract.parentContract')">
                  <el-select v-model="form.parentContractId" filterable clearable :placeholder="$t('contract.placeholder.parentContract')" style="width: 100%">
                    <el-option
                      v-for="item in parentContracts.filter((c: any) => (!isEdit || c.id !== contractId) && (c.relationType || 'MAIN') !== 'SUPPLEMENT')"
                      :key="item.id"
                      :label="`${item.contractNo} - ${item.title}`"
                      :value="item.id"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <div class="remark-section">
              <el-form-item :label="$t('contract.remark')">
                <el-input v-model="form.remark" type="textarea" :rows="2" :placeholder="$t('contract.placeholder.remark')" />
              </el-form-item>
            </div>
          </div>

          <div class="dynamic-fields-section" v-if="dynamicFields.length > 0">
            <div class="section-title-row">
              <div class="section-title">
                <el-icon><Tickets /></el-icon>
                <span>{{ $t('typeFieldConfig.dynamicFields') }}</span>
                <el-tag size="small" type="info">{{ dynamicFields.length }}</el-tag>
              </div>
            </div>
            <el-row :gutter="20">
              <el-col v-for="field in dynamicFields" :key="field.id" :span="8" :xs="24" :sm="12" :md="8">
                <el-form-item :label="locale === 'zh' ? field.fieldLabel : (field.fieldLabelEn || field.fieldLabel)" :required="field.required">
                  <el-input v-if="field.fieldType === 'text'" v-model="dynamicFieldValues[field.fieldKey]" :placeholder="field.placeholder" clearable />
                  <el-input-number v-else-if="field.fieldType === 'number'" v-model="dynamicFieldValues[field.fieldKey]" :min="0" style="width: 100%" />
                  <el-date-picker v-else-if="field.fieldType === 'date'" v-model="dynamicFieldValues[field.fieldKey]" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
                  <el-select v-else-if="field.fieldType === 'select'" v-model="dynamicFieldValues[field.fieldKey]" style="width: 100%" clearable>
                    <el-option v-for="opt in getSelectOptions(field)" :key="opt.code" :label="opt.displayLabel" :value="opt.code" />
                  </el-select>
                  <el-select v-else-if="field.fieldType === 'multiselect'" v-model="dynamicFieldValues[field.fieldKey]" multiple style="width: 100%" clearable collapse-tags collapse-tags-tooltip>
                    <el-option v-for="opt in getSelectOptions(field)" :key="opt.code" :label="opt.displayLabel" :value="opt.code" />
                  </el-select>
                  <el-input v-else v-model="dynamicFieldValues[field.fieldKey]" />
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <div class="counterparty-section">
            <div class="section-title-row">
              <div class="section-title">
                <el-icon><User /></el-icon>
                <span>{{ $t('contract.counterparts') }}</span>
                <el-tag size="small" type="info">{{ form.counterparties.length }}</el-tag>
              </div>
              <el-button type="primary" size="small" @click="addCounterparty" class="add-btn">
                <el-icon><Plus /></el-icon>
                {{ $t('common.add') }}
              </el-button>
            </div>
            <el-table :data="form.counterparties" border class="counterparty-table">
              <el-table-column :label="$t('contract.counterpartyLabel')" min-width="140">
                <template #default="{ row, $index }">
                  <div class="cp-type-cell">
                    <el-tag :type="getPartyTagType(row.type)" effect="dark" size="small" round>
                      {{ getPartyLabel(row.type) }}
                    </el-tag>
                    <el-button type="danger" text size="small" @click="removeCounterparty($index)" v-if="form.counterparties.length > 1">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                </template>
              </el-table-column>
              <el-table-column :label="$t('contract.placeholder.companyName')" min-width="200">
                <template #default="{ row }">
                  <el-input v-model="row.name" :placeholder="$t('contract.placeholder.companyName')" />
                </template>
              </el-table-column>
              <el-table-column :label="$t('contract.counterparty.contact')" min-width="120">
                <template #default="{ row }">
                  <el-input v-model="row.contact" :placeholder="$t('contract.placeholder.contact')" />
                </template>
              </el-table-column>
              <el-table-column :label="$t('contract.counterparty.phone')" min-width="140">
                <template #default="{ row }">
                  <el-input v-model="row.phone" :placeholder="$t('contract.placeholder.phone')" />
                </template>
              </el-table-column>
              <el-table-column :label="$t('contract.counterparty.email')" min-width="180">
                <template #default="{ row }">
                  <el-input v-model="row.email" :placeholder="$t('contract.placeholder.email')" />
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="content-section">
            <div class="section-title-row">
              <div class="section-title">
                <el-icon><Edit /></el-icon>
                <span>{{ $t('contract.content') }}</span>
              </div>
            </div>
            
            <div class="mode-row">
              <el-form-item :label="$t('contract.contentMode')" required style="flex: 1;">
                <el-select v-model="contentMode" style="width: 100%;" @change="handleModeChange">
                  <el-option :label="$t('contract.templateMode')" value="template" />
                  <el-option :label="$t('contract.uploadMode')" value="upload" />
                </el-select>
              </el-form-item>
              <el-form-item v-if="contentMode === 'template'" :label="$t('contract.selectTemplate')" required style="flex: 2;">
                <el-select v-model="selectedTemplateId" :placeholder="$t('contract.selectTemplatePlaceholder')" style="width: 100%;" @change="handleTemplateChange" filterable>
                  <el-option v-for="t in templates" :key="t.id" :label="t.name" :value="t.id" />
                </el-select>
              </el-form-item>
            </div>

            <div v-if="contentMode === 'template'" class="template-mode">
              <div v-if="selectedTemplate" class="template-content-wrapper">
                <div class="variables-panel">
                  <div class="panel-header">
                    <el-icon><List /></el-icon>
                    <span>{{ $t('contract.templateVariables') }}</span>
                  </div>
                  <div class="variables-form">
                    <el-form label-position="top">
                      <el-form-item v-for="variable in extractedVariables" :key="variable.key" :label="variable.label">
                        <el-input 
                          v-if="variable.type === 'text'" 
                          v-model="templateVariables[variable.key]" 
                          :placeholder="variable.label" 
                          clearable 
                          @input="updatePreview" 
                        />
                        <el-input-number 
                          v-else-if="variable.type === 'number'" 
                          v-model="templateVariables[variable.key]" 
                          :placeholder="variable.label" 
                          style="width: 100%" 
                          @change="updatePreview" 
                        />
                        <el-date-picker 
                          v-else-if="variable.type === 'date'" 
                          v-model="templateVariables[variable.key]" 
                          type="date" 
                          :placeholder="variable.label" 
                          style="width: 100%" 
                          value-format="YYYY-MM-DD"
                          @change="updatePreview" 
                        />
                        <el-input 
                          v-else-if="variable.type === 'textarea'" 
                          v-model="templateVariables[variable.key]" 
                          type="textarea" 
                          :rows="3"
                          :placeholder="variable.label" 
                          @input="updatePreview" 
                        />
                        <el-select 
                          v-else-if="variable.type === 'select'" 
                          v-model="templateVariables[variable.key]" 
                          :placeholder="variable.label" 
                          clearable 
                          style="width: 100%" 
                          @change="updatePreview"
                        >
                          <el-option 
                            v-for="item in getQuickCodeOptions(variable.quickCodeCode)" 
                            :key="item.code" 
                            :label="item.meaning" 
                            :value="item.code" 
                          />
                        </el-select>
                        <el-select 
                          v-else-if="variable.type === 'multiselect'" 
                          v-model="templateVariables[variable.key]" 
                          :placeholder="variable.label" 
                          clearable 
                          multiple
                          style="width: 100%" 
                          @change="updatePreview"
                        >
                          <el-option 
                            v-for="item in getQuickCodeOptions(variable.quickCodeCode)" 
                            :key="item.code" 
                            :label="item.meaning" 
                            :value="item.code" 
                          />
                        </el-select>
                        <el-input 
                          v-else 
                          v-model="templateVariables[variable.key]" 
                          :placeholder="variable.label" 
                          clearable 
                          @input="updatePreview" 
                        />
                      </el-form-item>
                    </el-form>
                    <div v-if="extractedVariables.length === 0" class="no-variables">
                      {{ $t('contract.noVariables') }}
                    </div>
                  </div>
                </div>
                
                <div class="preview-panel">
                  <div class="panel-header">
                    <el-icon><View /></el-icon>
                    <span>{{ $t('contract.preview') }}</span>
                  </div>
                  <div class="preview-content" v-html="previewContent"></div>
                </div>
              </div>
              
              <div v-else class="no-template-selected">
                <el-empty :description="$t('contract.selectTemplateToStart')" />
              </div>
            </div>

            <div v-if="contentMode === 'upload'" class="upload-mode">
              <el-upload
                class="upload-area"
                drag
                :auto-upload="false"
                :on-change="handleFileUpload"
                accept=".pdf,.doc,.docx"
              >
                <el-icon class="upload-icon"><UploadFilled /></el-icon>
                <div class="upload-text">
                  <span>{{ $t('contract.uploadFileTip') }}</span>
                  <span class="upload-hint">PDF、Word</span>
                </div>
              </el-upload>
              
              <div v-if="uploadedFile" class="upload-preview">
                <div class="preview-header">
                  <el-icon><Document /></el-icon>
                  <span>{{ uploadedFile.name }}</span>
                  <el-button type="danger" text size="small" @click="uploadedFile = null; uploadedFileUrl = ''; form.content = ''">
                    <el-icon><Close /></el-icon>
                  </el-button>
                </div>
                <div class="preview-content">
                  <img v-if="uploadedFileUrl && isImageFile(uploadedFile.name)" :src="uploadedFileUrl" alt="preview" />
                  <div v-else class="file-preview-placeholder">
                    <el-icon size="48"><Document /></el-icon>
                    <span>{{ uploadedFile.name }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="form.content && form.content !== '<p><br></p>'" class="full-preview-section">
              <div class="preview-actions">
                <el-button type="primary" @click="showFullPreviewDialog = true">
                  <el-icon><View /></el-icon>
                  {{ $t('contract.fullPreview') }}
                </el-button>
                <el-button type="success" @click="generatePdf" :loading="pdfGenerating">
                  <el-icon><Download /></el-icon>
                  {{ $t('contract.generateFile') }}
                </el-button>
              </div>
            </div>

    <el-dialog v-model="showFullPreviewDialog" :title="$t('contract.fullPreview')" width="85%" destroy-on-close>
      <div class="preview-dialog-content" v-html="form.content"></div>
    </el-dialog>
          </div>

          <div class="attachment-section">
            <div class="section-title-row">
              <div class="section-title">
                <el-icon><Files /></el-icon>
                <span>{{ $t('contract.fileManagement') }}</span>
              </div>
            </div>
            
            <div class="file-management-wrapper">
              <div class="contract-file-section">
                <div class="sub-section-header">
                  <el-icon><Document /></el-icon>
                  <span>{{ $t('contract.contractFile') }}</span>
                </div>
                <div v-if="generatedPdf" class="file-item">
                  <el-icon class="pdf-icon"><Document /></el-icon>
                  <span class="file-name" @click="handleDownloadGeneratedPdf" style="cursor: pointer; color: var(--primary);">{{ generatedPdf.name }}</span>
                  <el-tag size="small" type="success">{{ $t('contract.generated') }}</el-tag>
                  <el-button type="danger" text size="small" @click="removeGeneratedPdf">
                    <el-icon><Close /></el-icon>
                  </el-button>
                </div>
                <div v-else class="empty-tip">
                  <span>{{ $t('contract.noContractFile') }}</span>
                </div>
              </div>
              
              <div class="support-files-section">
                <div class="sub-section-header">
                  <el-icon><Paperclip /></el-icon>
                  <span>{{ $t('contract.supportFiles') }}</span>
                </div>
                <el-upload ref="attachmentUploadRef" :auto-upload="false" :on-change="handleAttachmentChange" :on-remove="handleAttachmentRemove" multiple>
                  <el-button type="primary" plain size="small">
                    <el-icon><Upload /></el-icon>
                    {{ $t('contract.uploadSupportFile') }}
                  </el-button>
                </el-upload>
                <div class="attachment-list" v-if="attachments.length > 0">
                  <div v-for="(att, idx) in attachments" :key="idx" class="file-item">
                    <el-icon><Document /></el-icon>
                    <span class="file-name">{{ att.name }}</span>
                    <el-button type="danger" text size="small" @click="removeAttachment(idx)">
                      <el-icon><Close /></el-icon>
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>

        <div class="form-actions">
          <el-button v-if="isEdit" type="warning" :loading="aiAnalyzing" @click="handleEditAiAnalyze">
            {{ $t('ai.analyze') }}
          </el-button>
          <el-button v-if="isEdit" type="info" :loading="savingDraft" @click="handleSaveDraft">{{ $t('common.saveDraft') }}</el-button>
          <el-button type="primary" :loading="loading" @click="handleSubmit">{{ $t('common.submit') }}</el-button>
        </div>
      </el-form>
    </el-card>

    <el-dialog v-model="aiDialogVisible" :title="$t('ai.assistantTitle')" width="820px">
      <div v-loading="aiAnalyzing">
        <el-alert
          v-if="!aiConfigSnapshot.apiUrl"
          type="info"
          :closable="false"
          :title="$t('ai.offlineDemoHint')"
          style="margin-bottom: 12px;"
        />

        <el-card shadow="never" style="margin-bottom: 12px;">
          <div style="display:flex;justify-content:space-between;align-items:center;">
            <span>{{ $t('ai.riskScore') }}</span>
            <strong style="font-size:24px;">{{ aiResult.score ?? 75 }}</strong>
          </div>
          <el-progress :percentage="Number(aiResult.score || 75)" :show-text="false" />
        </el-card>

        <el-divider content-position="left">{{ $t('ai.summary') }}</el-divider>
        <p style="line-height:1.7;white-space:pre-wrap;">{{ aiResult.summary || '-' }}</p>

        <el-divider content-position="left">{{ $t('ai.keyInfo') }}</el-divider>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item :label="$t('contract.partyTypes.partyA')">{{ aiResult.keyInfo?.partyA || '-' }}</el-descriptions-item>
          <el-descriptions-item :label="$t('contract.partyTypes.partyB')">{{ aiResult.keyInfo?.partyB || '-' }}</el-descriptions-item>
          <el-descriptions-item :label="$t('contract.amount')">{{ aiResult.keyInfo?.amount || '-' }}</el-descriptions-item>
          <el-descriptions-item :label="$t('contract.period')">{{ aiResult.keyInfo?.duration || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">{{ $t('ai.risks') }}</el-divider>
        <el-empty v-if="!aiResult.risks?.length" :description="$t('common.noData')" :image-size="60" />
        <el-timeline v-else>
          <el-timeline-item
            v-for="(risk, idx) in aiResult.risks"
            :key="idx"
            :type="risk.level === 'high' ? 'danger' : risk.level === 'medium' ? 'warning' : 'primary'"
          >
            {{ risk.content || '-' }}
          </el-timeline-item>
        </el-timeline>

        <el-divider content-position="left">{{ $t('ai.suggestions') }}</el-divider>
        <el-empty v-if="!aiResult.suggestions?.length" :description="$t('common.noData')" :image-size="60" />
        <ul v-else style="padding-left: 18px; margin: 0;">
          <li v-for="(s, idx) in aiResult.suggestions" :key="idx" style="line-height:1.8;">{{ s }}</li>
        </ul>
      </div>
      <template #footer>
        <el-button @click="aiDialogVisible = false">{{ $t('common.close') }}</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { createContract, updateContract, getContract, getContractPayload, getContractList, getNextContractNo, uploadContractFile, analyzeContract } from '@/api/contract'
import { getContractCategories } from '@/api/contractCategory'
import { getContractTypeFormFields } from '@/api/contractTypeField'
import { getQuickCodeByCode } from '@/api/quickCode'
import { getAllFolders } from '@/api/folder'
import { getCounterpartiesByContractId, saveCounterpartiesBatch } from '@/api/counterparty'
import { getAttachmentsByContractId, saveAttachmentsBatch } from '@/api/attachment'
import { getTemplateList, extractTemplateVariables, replaceTemplateVariables } from '@/api/template'
import { getTemplateVariables } from '@/api/templateVariable'
import { getSystemConfigs } from '@/api/system'
import { generateContractPdf } from '@/api/contract'
import { ArrowLeft, Collection, User, Edit, Plus, Delete, Tickets, Paperclip, Document, Upload, Close, FolderOpened, Clock, Files, List, View, UploadFilled, Download } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const { t, locale } = useI18n()
const formRef = ref()
const loading = ref(false)
const categories = ref<any[]>([])
const folders = ref<any[]>([])
const parentContracts = ref<any[]>([])
const dynamicFields = ref<any[]>([])
const dynamicFieldValues = reactive<Record<string, any>>({})
const quickCodeItemsCache = ref<Record<string, any[]>>({})
const attachments = ref<any[]>([])
const attachmentUploadRef = ref()

const isEdit = computed(() => !!route.params.id)
const contractId = computed(() => Number(route.params.id))

const contentMode = ref<'template' | 'upload'>('template')
const templates = ref<any[]>([])
const selectedTemplate = ref<any>(null)
const selectedTemplateId = ref<number | null>(null)

const templateVariables = ref<Record<string, string | number>>({})
const extractedVariables = ref<any[]>([])
const previewContent = ref('')
const uploadedFile = ref<any>(null)
const uploadedFileUrl = ref('')
const pdfGenerating = ref(false)
const generatedPdf = ref<{ name: string; url: string; blob?: Blob; serverUrl?: string } | null>(null)
const savingDraft = ref(false)
const showFullPreviewDialog = ref(false)
const aiDialogVisible = ref(false)
const aiAnalyzing = ref(false)
const aiConfigSnapshot = reactive({
  apiUrl: '',
  model: 'gpt-3.5-turbo',
  temperature: 0.7,
  maxTokens: 1000
})
const aiResult = ref<any>({
  summary: '',
  score: 75,
  risks: [],
  suggestions: [],
  keyInfo: {},
  negotiationPoints: [],
  missingClauseChecks: [],
  actionSuggestions: [],
  clauseSuggestions: []
})

const quillToolbar = [
  ['bold', 'italic', 'underline', 'strike'],
  [{ header: 1 }, { header: 2 }],
  [{ list: 'ordered' }, { list: 'bullet' }],
  [{ indent: '-1' }, { indent: '+1' }],
  [{ size: ['small', false, 'large', 'huge'] }],
  [{ color: [] }, { background: [] }],
  [{ align: [] }],
  ['clean'],
  ['link']
]

interface Counterparty {
  type: string
  name: string
  contact: string
  phone: string
  email: string
}

const form = reactive({
  title: '',
  type: '',
  amount: 0,
  currency: 'CNY',
  startDate: '',
  endDate: '',
  content: '',
  remark: '',
  contractNo: '',
  createdBy: '',
  timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
  createdAt: new Date().toISOString().slice(0, 19).replace('T', ' '),
  folderId: null as number | null,
  relationType: 'MAIN' as 'MAIN' | 'SUPPLEMENT',
  parentContractId: null as number | null,
  counterparties: [
    { type: 'partyA', name: '', contact: '', phone: '', email: '' }
  ] as Counterparty[]
})

const currencyOptions = [
  { value: 'CNY', label: 'CNY - RMB' },
  { value: 'USD', label: 'USD - US Dollar' },
  { value: 'EUR', label: 'EUR - Euro' },
  { value: 'HKD', label: 'HKD - Hong Kong Dollar' }
]

const currentStepRules = computed(() => {
  const rules: any = {}
  rules.title = [{ required: true, message: t('contract.error.title'), trigger: 'blur' }]
  rules.type = [{ required: true, message: t('contract.error.type'), trigger: 'change' }]
  rules.amount = [{ required: true, message: t('contract.error.amount'), trigger: 'blur' }]
  rules.currency = [{ required: true, message: t('contract.error.currency'), trigger: 'change' }]
  rules.startDate = [{ required: true, message: t('contract.error.startDate'), trigger: 'change' }]
  rules.endDate = [{ required: true, message: t('contract.error.endDate'), trigger: 'change' }]
  return rules
})

const getSelectOptions = (field: any) => {
  if (field.quickCodeId) {
    const items = quickCodeItemsCache.value[field.quickCodeId] || []
    return items.map((item: any) => ({
      ...item,
      displayLabel: locale.value === 'en' && item.meaningEn ? item.meaningEn : item.meaning
    }))
  }
  return []
}

const loadCategories = async () => {
  try {
    const res = await getContractCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
}

const loadFolders = async () => {
  try {
    const res = await getAllFolders()
    folders.value = res.data || []
  } catch (error) {
    console.error('Failed to load folders:', error)
  }
}

const loadParentContracts = async () => {
  try {
    const res = await getContractList({ page: 1, pageSize: 300, status: 'APPROVED,SIGNED,ARCHIVED,RENEWING,RENEWED' } as any)
    parentContracts.value = res.data?.list || []
  } catch (error) {
    console.error('Failed to load parent contracts:', error)
    parentContracts.value = []
  }
}

const loadTemplates = async () => {
  try {
    const res = await getTemplateList({ pageSize: 100 })
    templates.value = res.data?.list || []
  } catch (error) {
    console.error('Failed to load templates:', error)
  }
}

const templateVariableList = ref<any[]>([])
const loadTemplateVariables = async () => {
  try {
    const res = await getTemplateVariables({ status: 1 })
    templateVariableList.value = res.data || []
  } catch (error) {
    console.error('Failed to load template variables:', error)
  }
}

const handleTemplateChange = async (templateId: number | null) => {
  if (!templateId) {
    selectedTemplate.value = null
    selectedTemplateId.value = null
    return
  }
  selectedTemplateId.value = templateId
  selectedTemplate.value = templates.value.find(t => t.id === templateId)
  if (selectedTemplate.value) {
    await extractVariables()
    updatePreview()
  }
}

const extractVariables = async () => {
  if (!selectedTemplate.value?.content) return
  try {
    const res = await extractTemplateVariables(selectedTemplate.value.content)
    if (res.data && Array.isArray(res.data)) {
      templateVariables.value = {}
      extractedVariables.value = []
      for (const v of res.data) {
        const tv = templateVariableList.value.find(tv => tv.code === v)
        const variableType = tv?.type || 'text'
        if (variableType === 'number') {
          templateVariables.value[v] = tv?.defaultValue ? Number(tv.defaultValue) : 0
        } else {
          templateVariables.value[v] = tv?.defaultValue || ''
        }
        const quickCodeCode = tv?.quickCodeCode || ''
        extractedVariables.value.push({
          key: v,
          label: getVariableLabel(v),
          type: tv?.type || 'text',
          quickCodeCode,
          required: tv?.required || false
        })
        if (quickCodeCode) {
          await loadQuickCodeItems(quickCodeCode)
        }
      }
    }
  } catch (error) {
    console.error('Failed to extract variables:', error)
  }
}

const getVariableLabel = (key: string): string => {
  const tv = templateVariableList.value.find(v => v.code === key)
  if (tv && tv.name) {
    return locale.value === 'en' && tv.nameEn ? tv.nameEn : tv.name
  }
  const dynamicField = dynamicFields.value.find(f => f.fieldKey === key)
  if (dynamicField) {
    return locale.value === 'zh' ? dynamicField.fieldLabel : (dynamicField.fieldLabelEn || dynamicField.fieldLabel)
  }
  return key
}

const getQuickCodeOptions = (quickCodeCode: string): any[] => {
  if (!quickCodeCode) return []
  if (quickCodeItemsCache.value[quickCodeCode]) {
    return quickCodeItemsCache.value[quickCodeCode]
  }
  return []
}

const loadQuickCodeItems = async (quickCodeCode: string) => {
  if (!quickCodeCode || quickCodeItemsCache.value[quickCodeCode]) return
  try {
    const res = await getQuickCodeByCode(quickCodeCode)
    // /quick-codes/code/{code} 的 data 为选项数组；详情接口为 { items: [] }
    const d = res.data as any[] | { items?: any[] } | undefined
    const items = Array.isArray(d) ? d : (d?.items ?? [])
    quickCodeItemsCache.value[quickCodeCode] = items.filter((item: any) => item.enabled !== false)
  } catch (error) {
    console.error('Failed to load quick code items:', error)
  }
}

const updatePreview = async () => {
  if (!selectedTemplate.value?.content) return
  
  const allValues: Record<string, any> = {
    ...templateVariables.value,
    contractNo: form.contractNo,
    startDate: form.startDate,
    endDate: form.endDate,
    amount: form.amount,
    counterparties: form.counterparties.map(cp => ({ type: cp.type, name: cp.name }))
  }
  
  for (const key in dynamicFieldValues) {
    allValues[key] = dynamicFieldValues[key]
  }
  
  try {
    const res = await replaceTemplateVariables(selectedTemplate.value.content, allValues)
    previewContent.value = res.data || selectedTemplate.value.content
    form.content = previewContent.value
  } catch (error) {
    previewContent.value = selectedTemplate.value.content
    form.content = previewContent.value
  }
}

let previewDebounceTimer: any = null
const updatePreviewDebounced = () => {
  if (previewDebounceTimer) clearTimeout(previewDebounceTimer)
  previewDebounceTimer = setTimeout(() => {
    updatePreview()
  }, 500)
}

watch(templateVariables, () => {
  if (selectedTemplate.value && contentMode.value === 'template') {
    updatePreviewDebounced()
  }
}, { deep: true })

watch(() => form.counterparties, () => {
  if (selectedTemplate.value && contentMode.value === 'template') {
    updatePreviewDebounced()
  }
}, { deep: true })

// 监听locale变化，仅更新变量显示标签，避免丢失 type 等元数据
watch(locale, () => {
  if (extractedVariables.value.length > 0) {
    extractedVariables.value = extractedVariables.value.map(v => ({
      ...v,
      label: getVariableLabel(v.key)
    }))
  }
})

watch(() => form.amount, () => {
  if (selectedTemplate.value && contentMode.value === 'template') {
    updatePreviewDebounced()
  }
})

watch(() => form.type, async (newType) => {
  if (newType) {
    await loadDynamicFields()
  }
})

watch(() => form.relationType, (newType) => {
  if (newType !== 'SUPPLEMENT') {
    form.parentContractId = null
  }
})

const handleFileUpload = async (file: any) => {
  uploadedFile.value = file
  const ext = file.name.split('.').pop()?.toLowerCase()
  
  if (ext && ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(ext)) {
    const reader = new FileReader()
    reader.onload = (e) => {
      uploadedFileUrl.value = e.target?.result as string
      form.content = `<div class="uploaded-file-preview"><p>${file.name}</p><img src="${uploadedFileUrl.value}" style="max-width: 100%;" /></div>`
    }
    reader.readAsDataURL(file.raw)
  } else if (ext && ['pdf'].includes(ext)) {
    const reader = new FileReader()
    reader.onload = (e) => {
      uploadedFileUrl.value = e.target?.result as string
      form.content = `<div class="uploaded-file-preview"><p>${file.name}</p><iframe src="${uploadedFileUrl.value}" style="width: 100%; height: 600px; border: none;"></iframe></div>`
    }
    reader.readAsDataURL(file.raw)
  } else {
    try {
      const res = await uploadContractFile(file.raw)
      const fileUrl = res.data?.fileUrl || `/api/contracts/download/${res.data?.fileName}`
      form.content = `<div class="uploaded-file-preview"><p>${file.name}</p><div class="file-placeholder"><p>${t('contract.uploadedFileHint')} <a href="${fileUrl}" target="_blank">${file.name}</a></p></div></div>`
      uploadedFileUrl.value = fileUrl
    } catch (error) {
      ElMessage.error(t('common.error'))
      form.content = `<div class="uploaded-file-preview"><p>${file.name}</p><div class="file-placeholder"><p>文件上传失败，请重试</p></div></div>`
    }
  }
  return false
}

const handleModeChange = (mode: 'template' | 'upload') => {
  contentMode.value = mode
}

const isImageFile = (filename: string) => {
  return /\.(jpg|jpeg|png|gif|bmp|webp)$/i.test(filename)
}

const loadDynamicFields = async () => {
  if (!form.type) {
    dynamicFields.value = []
    return
  }
  try {
    const res = await getContractTypeFormFields(form.type)
    dynamicFields.value = res.data || []
    for (const field of dynamicFields.value) {
      if (dynamicFieldValues[field.fieldKey] === undefined) {
        if (field.fieldType === 'number') {
          dynamicFieldValues[field.fieldKey] = field.defaultValue ? Number(field.defaultValue) : 0
        } else {
          dynamicFieldValues[field.fieldKey] = field.defaultValue || ''
        }
      }
      if (field.fieldType === 'select' && field.quickCodeId && !quickCodeItemsCache.value[field.quickCodeId]) {
        try {
          const qcRes = await getQuickCodeByCode(field.quickCodeId)
          quickCodeItemsCache.value[field.quickCodeId] = qcRes.data || []
        } catch {
          quickCodeItemsCache.value[field.quickCodeId] = []
        }
      }
    }
  } catch (error) {
    console.error('Failed to load dynamic fields:', error)
  }
}

const normalizeDynamicFieldValue = (field: any, rawValue: any) => {
  if (rawValue === null || rawValue === undefined) {
    return field.fieldType === 'number' ? null : ''
  }
  if (field.fieldType === 'number') {
    if (typeof rawValue === 'number') return rawValue
    const parsed = Number(rawValue)
    return Number.isFinite(parsed) ? parsed : null
  }
  return rawValue
}

const handleTypeChange = () => {
  if (!isEdit.value) {
    loadDynamicFields()
  }
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
    partyA: 'primary',
    partyB: 'success',
    partyC: 'warning',
    partyD: 'info',
    partyE: 'danger',
    partyF: '',
    partyG: '',
    partyH: '',
    partyI: '',
    partyJ: ''
  }
  return map[type] || ''
}

const addCounterparty = () => {
  const types = ['partyA', 'partyB', 'partyC', 'partyD', 'partyE', 'partyF', 'partyG', 'partyH', 'partyI', 'partyJ']
  const usedTypes = form.counterparties.map(cp => cp.type)
  const availableType = types.find(t => !usedTypes.includes(t))
  if (!availableType) {
    ElMessage.warning(t('contract.maxCounterparties'))
    return
  }
  form.counterparties.push({ type: availableType, name: '', contact: '', phone: '', email: '' })
}

const removeCounterparty = (index: number) => {
  form.counterparties.splice(index, 1)
}

const handleAttachmentChange = async (file: any) => {
  try {
    const res = await uploadContractFile(file.raw)
    const fileUrl = res.data?.fileUrl || `/api/contracts/download/${res.data?.fileName}`
    attachments.value.push({
      uid: file.uid || Date.now(),
      name: file.name,
      fileName: file.name,
      url: fileUrl,
      fileUrl: fileUrl,
      size: file.size,
      fileSize: file.size,
      type: file.raw?.type || file.type,
      fileCategory: 'support',
      uploadTime: new Date().toISOString()
    })
  } catch (error) {
    ElMessage.error(t('contract.uploadFailed'))
  }
}

const handleAttachmentRemove = (file: any) => {
  const idx = attachments.value.findIndex((a: any) => a.uid === file.uid)
  if (idx !== -1) attachments.value.splice(idx, 1)
}

const removeAttachment = (index: number) => {
  const removed = attachments.value.splice(index, 1)
  if (removed[0]?.url && removed[0].url.includes('/api/contracts/download/')) {
  }
}

const generatePdf = async () => {
  if (!form.content || form.content === '<p><br></p>') {
    ElMessage.error(t('contract.error.contentRequired'))
    return
  }
  pdfGenerating.value = true
  try {
    // 不管是什么模式，都强制从HTML内容生成PDF，确保最终文件是PDF
    const blob = await generateContractPdf({
      content: form.content,
      contractNo: form.contractNo,
      watermark: form.contractNo || 'CONFIDENTIAL'
    })
    const fileName = `${form.contractNo || 'contract'}.pdf`
    
    const url = URL.createObjectURL(blob)
    if (generatedPdf.value?.url) {
      URL.revokeObjectURL(generatedPdf.value.url)
    }
    generatedPdf.value = {
      name: fileName,
      url,
      blob
    }
    
    const file = new File([blob], fileName, { type: 'application/pdf' })
    try {
      const uploadRes = await uploadContractFile(file)
      const fileUrl = uploadRes.data?.fileUrl || `/api/contracts/download/${uploadRes.data?.fileName}`
      generatedPdf.value.serverUrl = fileUrl
    } catch (uploadError) {
      console.error('Failed to upload PDF to server:', uploadError)
    }
    
    ElMessage.success(t('common.success'))
  } catch (error) {
    ElMessage.error(t('common.error'))
  } finally {
    pdfGenerating.value = false
  }
}

const removeGeneratedPdf = () => {
  if (generatedPdf.value?.url) {
    URL.revokeObjectURL(generatedPdf.value.url)
  }
  generatedPdf.value = null
}

const handleDownloadGeneratedPdf = () => {
  if (!generatedPdf.value) return
  
  const name = generatedPdf.value.name
  const url = generatedPdf.value.serverUrl || generatedPdf.value.url
  const blob = generatedPdf.value.blob
  
  if (blob) {
    // 如果有blob，直接下载
    const downloadUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = name
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(downloadUrl)
  } else if (url) {
    // 否则使用fetch下载
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
        console.error('下载失败:', error)
        const link = document.createElement('a')
        link.href = url
        link.download = name
        link.click()
      })
  }
}

const fetchContract = async () => {
  try {
    const [detailRes, payloadRes] = await Promise.all([
      getContract(contractId.value),
      getContractPayload(contractId.value).catch(() => ({ data: null }))
    ])
    const detailData = detailRes.data || {}
    const payloadData = payloadRes?.data || {}
    const data = {
      ...detailData,
      content: payloadData.content ?? detailData.content ?? '',
      templateVariables: payloadData.templateVariables ?? detailData.templateVariables ?? '',
      dynamicFields: payloadData.dynamicFields ?? detailData.dynamicFields ?? {},
      dynamicFieldValues: payloadData.dynamicFieldValues ?? detailData.dynamicFieldValues ?? ''
    }
    form.title = data.title
    form.type = data.type
    form.amount = data.amount
    form.currency = data.currency || 'CNY'
    form.startDate = data.startDate
    form.endDate = data.endDate
    form.content = data.content || ''
    form.remark = data.remark || ''
    form.contractNo = data.contractNo
    form.createdBy = data.createdBy
    form.folderId = data.folderId || null
    form.relationType = data.relationType === 'SUPPLEMENT' ? 'SUPPLEMENT' : 'MAIN'
    form.parentContractId = data.parentContractId || null
    form.timezone = data.timezone || ''
    form.createdAt = data.createdAt || ''
    
    if (data.templateId) {
      selectedTemplateId.value = Number(data.templateId)
      selectedTemplate.value = templates.value.find(t => Number(t.id) === Number(data.templateId))
    }
    
    if (data.contentMode) {
      contentMode.value = data.contentMode
    }
    
    if (data.templateVariables) {
      try {
        const vars = JSON.parse(data.templateVariables)
        templateVariables.value = vars
        extractedVariables.value = Object.keys(vars).map(key => ({
          key,
          label: getVariableLabel(key)
        }))
      } catch (e) {
        console.error('Failed to parse templateVariables:', e)
      }
    }
    
    if (selectedTemplate.value && form.content) {
      previewContent.value = form.content
    }
    
    try {
      const cpRes = await getCounterpartiesByContractId(contractId.value)
      const cpList = Array.isArray(cpRes) ? cpRes : (cpRes as any)?.data || []
      if (cpList.length > 0) {
        form.counterparties = cpList.map((cp: any) => ({
          type: cp.type || 'partyB',
          name: cp.name || '',
          contact: cp.contactPerson || '',
          phone: cp.contactPhone || '',
          email: cp.contactEmail || ''
        }))
      } else {
        form.counterparties = [
          { type: 'partyA', name: '', contact: '', phone: '', email: '' },
          { type: 'partyB', name: '', contact: '', phone: '', email: '' }
        ]
      }
    } catch {
      form.counterparties = [
        { type: 'partyA', name: '', contact: '', phone: '', email: '' },
        { type: 'partyB', name: '', contact: '', phone: '', email: '' }
      ]
    }
    
    try {
      const attRes = await getAttachmentsByContractId(contractId.value)
      const attList = Array.isArray(attRes) ? attRes : (attRes as any)?.data || []
      if (attList.length > 0) {
        attachments.value = attList
          .filter((att: any) => att.fileCategory === 'support')
          .map((att: any) => ({
            uid: att.id || Date.now() + Math.random(),
            name: att.fileName,
            fileName: att.fileName,
            url: att.fileUrl,
            fileUrl: att.fileUrl,
            size: att.fileSize,
            fileSize: att.fileSize,
            type: att.fileType,
            fileCategory: 'support',
            uploadTime: att.uploadTime || new Date().toISOString()
          }))
        const contractFile = attList.find((att: any) => att.fileCategory === 'contract')
        if (contractFile) {
          generatedPdf.value = {
            name: contractFile.fileName,
            url: contractFile.fileUrl,
            serverUrl: contractFile.fileUrl,
            blob: null
          }
        }
      }
    } catch {
      attachments.value = []
    }
    
    await loadDynamicFields()

    const dynamicSource =
      data.dynamicFields && typeof data.dynamicFields === 'object'
        ? data.dynamicFields
        : (() => {
            if (!data.dynamicFieldValues) return {}
            try {
              return typeof data.dynamicFieldValues === 'string'
                ? JSON.parse(data.dynamicFieldValues)
                : data.dynamicFieldValues
            } catch {
              return {}
            }
          })()

    for (const field of dynamicFields.value) {
      if (!Object.prototype.hasOwnProperty.call(dynamicSource, field.fieldKey)) {
        continue
      }
      dynamicFieldValues[field.fieldKey] = normalizeDynamicFieldValue(field, dynamicSource[field.fieldKey])
    }
  } catch (error) {
    ElMessage.error(t('contract.error.fetch'))
  }
}

const createAiFallbackResult = () => ({
  summary: '未获取到完整 AI 结果，已提供基础分析视图。请保存合同内容后重试，或配置可用 AI 接口。',
  score: 75,
  risks: [{ level: 'low', content: '暂未识别到结构化风险，请人工复核关键条款。' }],
  suggestions: ['建议先保存草稿并补充正文，再重新分析。'],
  keyInfo: {
    partyA: form.counterparties.find(cp => cp.type === 'partyA')?.name || '-',
    partyB: form.counterparties.find(cp => cp.type === 'partyB')?.name || '-',
    amount: form.amount || '-',
    duration: form.startDate && form.endDate ? `${form.startDate} ~ ${form.endDate}` : '-'
  }
})

const normalizeAiResult = (raw: any) => {
  if (!raw || typeof raw !== 'object') return createAiFallbackResult()
  const fallback = createAiFallbackResult()
  const keyInfo = raw.keyInfo && typeof raw.keyInfo === 'object' ? raw.keyInfo : {}
  return {
    summary: typeof raw.summary === 'string' && raw.summary.trim() ? raw.summary : fallback.summary,
    score: Number.isFinite(Number(raw.score)) ? Number(raw.score) : fallback.score,
    risks: Array.isArray(raw.risks) && raw.risks.length > 0 ? raw.risks : fallback.risks,
    suggestions: Array.isArray(raw.suggestions) && raw.suggestions.length > 0 ? raw.suggestions : fallback.suggestions,
    keyInfo: {
      partyA: keyInfo.partyA || fallback.keyInfo.partyA,
      partyB: keyInfo.partyB || fallback.keyInfo.partyB,
      amount: keyInfo.amount || fallback.keyInfo.amount,
      duration: keyInfo.duration || fallback.keyInfo.duration,
      keyClauses: Array.isArray(keyInfo.keyClauses) ? keyInfo.keyClauses : []
    },
    negotiationPoints: Array.isArray(raw.negotiationPoints) ? raw.negotiationPoints : [],
    missingClauseChecks: Array.isArray(raw.missingClauseChecks) ? raw.missingClauseChecks : [],
    actionSuggestions: Array.isArray(raw.actionSuggestions) ? raw.actionSuggestions : [],
    clauseSuggestions: Array.isArray(raw.clauseSuggestions) ? raw.clauseSuggestions : []
  }
}

const loadAiRuntimeConfig = async () => {
  const response = await getSystemConfigs()
  const configs = response?.data || {}
  aiConfigSnapshot.apiUrl = configs.ai_api_url || ''
  aiConfigSnapshot.model = configs.ai_model || 'gpt-3.5-turbo'
  aiConfigSnapshot.temperature = Number(configs.bd_temperature ?? 0.7)
  aiConfigSnapshot.maxTokens = Number(configs.bd_max_tokens ?? 1000)
  return {
    apiUrl: aiConfigSnapshot.apiUrl,
    apiKey: configs.ai_api_key || '',
    model: aiConfigSnapshot.model,
    temperature: aiConfigSnapshot.temperature,
    maxTokens: aiConfigSnapshot.maxTokens
  }
}

const handleEditAiAnalyze = async () => {
  if (!isEdit.value || !contractId.value) {
    ElMessage.warning('请先保存合同后再进行 AI 分析')
    return
  }
  aiDialogVisible.value = true
  aiAnalyzing.value = true
  try {
    const aiConfig = await loadAiRuntimeConfig()
    const res = await analyzeContract(contractId.value, aiConfig)
    aiResult.value = normalizeAiResult(res.data)
  } catch (error: any) {
    aiResult.value = createAiFallbackResult()
    ElMessage.error(error?.message || t('ai.analysisFailed'))
  } finally {
    aiAnalyzing.value = false
  }
}

const handleSaveDraft = async () => {
  if (form.relationType === 'SUPPLEMENT' && !form.parentContractId) {
    ElMessage.error(t('contract.error.parentContract'))
    return
  }
  savingDraft.value = true
  try {
    const draftData: any = {
      title: form.title || t('contract.untitled'),
      type: form.type,
      amount: form.amount || 0,
      currency: form.currency,
      startDate: form.startDate,
      endDate: form.endDate,
      content: form.content,
      remark: form.remark,
      folderId: form.folderId,
      relationType: form.relationType,
      parentContractId: form.relationType === 'SUPPLEMENT' ? form.parentContractId : null,
      timezone: form.timezone,
      status: 'DRAFT',
      templateVariables: JSON.stringify(templateVariables.value),
      dynamicFieldValues: JSON.stringify(dynamicFieldValues),
      dynamicFields: { ...dynamicFieldValues },
      templateId: selectedTemplateId.value,
      contentMode: contentMode.value
    }
    
    let savedContractId: number
    if (isEdit.value) {
      await updateContract(contractId.value, draftData)
      savedContractId = contractId.value
    } else {
      const res = await createContract(draftData)
      savedContractId = res.data?.id
      if (savedContractId) {
        router.replace(`/contracts/edit/${savedContractId}`)
      }
    }
    
    if (savedContractId) {
      const counterpartiesData = form.counterparties.map((cp, index) => ({
        name: cp.name,
        type: cp.type,
        contactPerson: cp.contact,
        contactPhone: cp.phone,
        contactEmail: cp.email,
        sortOrder: index
      }))
      await saveCounterpartiesBatch(savedContractId, counterpartiesData)

      const attachmentsData = attachments.value
        .map((att: any) => ({
          fileName: att.fileName || att.name,
          fileUrl: att.fileUrl || att.url || '',
          fileSize: att.fileSize || att.size,
          fileType: att.type || 'application/octet-stream',
          fileCategory: 'support' as 'contract' | 'support'
        }))

      if (generatedPdf.value?.serverUrl) {
        attachmentsData.push({
          fileName: generatedPdf.value.name,
          fileUrl: generatedPdf.value.serverUrl,
          fileSize: generatedPdf.value.blob?.size || 0,
          fileType: 'application/pdf',
          fileCategory: 'contract' as 'contract' | 'support'
        })
      }

      if (attachmentsData.length > 0) {
        await saveAttachmentsBatch(savedContractId, attachmentsData)
      }
    }
    
    ElMessage.success(t('common.success'))
  } catch (error) {
    ElMessage.error(t('common.error'))
  } finally {
    savingDraft.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (form.counterparties.length < 2) {
    ElMessage.error(t('contract.error.counterparties'))
    return
  }
  const emptyNames = form.counterparties.filter(cp => !cp.name.trim())
  if (emptyNames.length > 0) {
    ElMessage.error(t('contract.error.counterpartyName'))
    return
  }
  if (form.relationType === 'SUPPLEMENT' && !form.parentContractId) {
    ElMessage.error(t('contract.error.parentContract'))
    return
  }

  loading.value = true
  try {
    const submitData: any = {
      title: form.title,
      type: form.type,
      amount: form.amount,
      currency: form.currency,
      startDate: form.startDate,
      endDate: form.endDate,
      content: form.content,
      remark: form.remark,
      folderId: form.folderId,
      relationType: form.relationType,
      parentContractId: form.relationType === 'SUPPLEMENT' ? form.parentContractId : null,
      timezone: form.timezone,
      templateVariables: JSON.stringify(templateVariables.value),
      dynamicFieldValues: JSON.stringify(dynamicFieldValues),
      dynamicFields: { ...dynamicFieldValues },
      templateId: selectedTemplateId.value,
      contentMode: contentMode.value
    }
    
    let savedContractId: number
    if (isEdit.value) {
      await updateContract(contractId.value, submitData)
      savedContractId = contractId.value
    } else {
      const res = await createContract(submitData)
      savedContractId = res.data?.id
    }
    
    const counterpartiesData = form.counterparties.map((cp, index) => ({
      name: cp.name,
      type: cp.type,
      contactPerson: cp.contact,
      contactPhone: cp.phone,
      contactEmail: cp.email,
      sortOrder: index
    }))
    await saveCounterpartiesBatch(savedContractId, counterpartiesData)
    
    const attachmentsData = attachments.value
      .map((att: any) => ({
        fileName: att.fileName || att.name,
        fileUrl: att.fileUrl || att.url || '',
        fileSize: att.fileSize || att.size,
        fileType: att.type || 'application/octet-stream',
        fileCategory: 'support' as 'contract' | 'support'
      }))
    
    if (generatedPdf.value?.serverUrl) {
      attachmentsData.push({
        fileName: generatedPdf.value.name,
        fileUrl: generatedPdf.value.serverUrl,
        fileSize: generatedPdf.value.blob?.size || 0,
        fileType: 'application/pdf',
        fileCategory: 'contract' as 'contract' | 'support'
      })
    }
    
    if (attachmentsData.length > 0) {
      await saveAttachmentsBatch(savedContractId, attachmentsData)
    }
    
    ElMessage.success(t('common.success'))
    router.push('/contracts')
  } catch (error: any) {
    ElMessage.error(error.message || t('common.error'))
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadCategories(), loadFolders(), loadParentContracts(), loadTemplates(), loadTemplateVariables()])
  if (isEdit.value) {
    await fetchContract()
  } else {
    // Generate contract number immediately for new contracts
    try {
      const res = await getNextContractNo()
      form.contractNo = res.data?.contractNo || ''
    } catch (error) {
      console.error('Failed to generate contract number:', error)
    }
  }
})
</script>

<style scoped lang="scss">
.contract-form {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.steps-container {
  margin-bottom: 16px;
}

.form-card {
  margin-top: 8px;
}

.step-content {
  min-height: 400px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  flex-shrink: 0;
}

.section-title-row {
  display: flex;
  align-items: center;
  margin: 20px 0 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-color);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.meta-info-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
  padding: 0;
  background: transparent;
  margin-left: 20px;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  white-space: nowrap;
}

.meta-separator {
  margin: 0 12px;
  color: var(--border-color);
}

.meta-label {
  color: var(--text-secondary);
}

.contract-no {
  font-family: monospace;
  font-weight: 600;
  color: var(--primary);
}

.dynamic-fields-section {
  margin: 0;
}

.counterparty-section {
  margin: 0;
}

.counterparty-table {
  margin-top: 16px;

  :deep(.el-input__wrapper) {
    box-shadow: none !important;
    background: transparent;
    padding: 0;
  }

  :deep(.el-input) {
    --el-input-border-color: transparent;
  }

  :deep(.el-input:hover .el-input__wrapper) {
    box-shadow: 0 0 0 1px var(--el-border-color) !important;
  }

  :deep(.el-input.is-focus .el-input__wrapper) {
    box-shadow: 0 0 0 1px var(--el-color-primary) !important;
  }
}

.cp-type-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.add-btn {
  margin-left: 8px;
}

.content-section {
  margin: 0;
}

.quill-wrapper {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;
}

.mode-row {
  display: flex;
  gap: 16px;
  margin-top: 16px;
  
  :deep(.el-form-item) {
    margin-bottom: 0;
  }
}

.template-mode {
  margin-top: 20px;
}

.template-content-wrapper {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 16px;
  min-height: 400px;
}

.variables-panel, .preview-panel {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  
  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    padding: 12px 16px;
    background: var(--bg-hover);
    border-bottom: 1px solid var(--border-color);
    font-weight: 600;
    font-size: 14px;
    color: var(--text-primary);
    
    .el-icon {
      color: var(--primary);
    }
  }
  
  .variables-form {
    padding: 16px;
    max-height: 420px;
    overflow-y: auto;
    
    :deep(.el-form-item) {
      margin-bottom: 12px;
      
      .el-form-item__label {
        font-weight: 500;
        color: var(--text-primary);
      }
    }
    
    .no-variables {
      text-align: center;
      color: var(--text-secondary);
      padding: 40px 0;
    }
  }
  
  .preview-content {
    padding: 16px;
    max-height: 420px;
    overflow-y: auto;
    font-size: 14px;
    line-height: 1.8;
    color: var(--text-primary);
    white-space: pre-wrap;
    word-break: break-word;
    
    :deep(h1), :deep(h2) {
      color: var(--primary);
      margin: 16px 0 8px;
    }
    
    :deep(p) {
      margin: 12px 0;
      text-indent: 2em;
    }
    
    :deep(table) {
      width: 100%;
      border-collapse: collapse;
      margin: 12px 0;
      
      td, th {
        border: 1px solid var(--border-color);
        padding: 8px;
      }
      
      th {
        background: var(--bg-hover);
        font-weight: 600;
      }
    }
  }
}

.no-template-selected {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  background: var(--bg-hover);
  border-radius: 12px;
}

.upload-mode {
  margin-top: 20px;
}

.upload-area {
  width: 100%;
  
  :deep(.el-upload-dragger) {
    padding: 32px;
    border: 2px dashed var(--border-color);
    border-radius: 16px;
    background: linear-gradient(135deg, var(--bg-hover) 0%, var(--bg-card) 100%);
    transition: all 0.3s;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    
    &:hover {
      border-color: var(--primary);
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, var(--bg-hover) 100%);
      transform: translateY(-2px);
      box-shadow: 0 4px 16px rgba(102, 126, 234, 0.15);
    }
  }
  
  .upload-icon {
    font-size: 56px;
    color: var(--primary);
    margin-bottom: 12px;
    background: linear-gradient(135deg, var(--primary) 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
  
  .upload-text {
    display: flex;
    flex-direction: column;
    gap: 6px;
    color: var(--text-primary);
    
    .upload-hint {
      font-size: 13px;
      color: var(--text-secondary);
      margin-top: 4px;
      padding: 4px 12px;
      background: var(--bg-color);
      border-radius: 12px;
      display: inline-block;
    }
  }
}

.upload-preview {
  margin-top: 24px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  
  .preview-header {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 14px 18px;
    background: linear-gradient(135deg, var(--bg-hover) 0%, var(--bg-card) 100%);
    border-bottom: 1px solid var(--border-color);
    
    .el-icon {
      color: var(--primary);
      font-size: 18px;
    }
    
    span {
      flex: 1;
      font-weight: 600;
      font-size: 14px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
  
  .preview-content {
    padding: 20px;
    max-height: 450px;
    overflow: auto;
    text-align: center;
    background: linear-gradient(180deg, var(--bg-color) 0%, var(--bg-card) 100%);
    
    img {
      max-width: 100%;
      border-radius: 12px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    }
    
    .file-preview-placeholder {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16px;
      padding: 48px 24px;
      color: var(--text-secondary);
      background: var(--bg-hover);
      border-radius: 12px;
      border: 1px dashed var(--border-color);
      
      .el-icon {
        font-size: 64px;
        color: var(--primary);
        opacity: 0.6;
      }
      
      span {
        font-size: 15px;
        font-weight: 500;
        color: var(--text-secondary);
      }
    }
  }
}

.attachment-section {
  margin: 20px 0;
}

.file-management-wrapper {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-top: 16px;
}

.contract-file-section, .support-files-section {
  background: var(--bg-hover);
  border-radius: 12px;
  padding: 16px;
}

.sub-section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
  
  .el-icon {
    color: var(--primary);
  }
}

.contract-file-section {
  .pdf-icon {
    color: #e6a23c;
    font-size: 20px;
  }
  
  .file-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    background: white;
    border-radius: 8px;
    border: 1px solid var(--border-color);
  }
  
  .empty-tip {
    padding: 24px;
    text-align: center;
    color: var(--text-secondary);
    font-size: 13px;
    background: white;
    border-radius: 8px;
    border: 1px dashed var(--border-color);
  }
}

.support-files-section {
  .attachment-list {
    margin-top: 12px;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .file-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    background: white;
    border-radius: 6px;
    border: 1px solid var(--border-color);
  }
}

.file-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-link {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--primary);
  text-decoration: none;
  
  &:hover {
    text-decoration: underline;
  }
}

.full-preview-section {
  margin-top: 16px;
  padding: 16px;
  background: var(--bg-hover);
  border-radius: 8px;
}

.preview-actions {
  display: flex;
  gap: 12px;
}

.full-preview-content {
  background: white;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 24px;
  max-height: 600px;
  overflow-y: auto;
  line-height: 1.8;
  font-size: 14px;
  color: #333;
  
  :deep(h1), :deep(h2) {
    color: #667eea;
    margin: 16px 0 8px;
    text-align: center;
  }
  
  :deep(table) {
    width: 100%;
    border-collapse: collapse;
    margin: 16px 0;
    
    td, th {
      border: 1px solid #ddd;
      padding: 10px 12px;
    }
    
    th {
      background: #f5f7fa;
      font-weight: 600;
      text-align: center;
    }
    
    td {
      text-align: left;
    }
  }
  
  :deep(p) {
    margin: 12px 0;
    text-indent: 2em;
  }
}

.remark-section {
  margin: 20px 0;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}

.preview-dialog-content {
  padding: 20px;
  max-height: 70vh;
  overflow-y: auto;
  line-height: 1.8;
  font-size: 14px;
  color: #333;
  
  :deep(h1), :deep(h2) {
    color: var(--primary);
    margin: 16px 0 8px;
    text-align: center;
  }
  
  :deep(table) {
    width: 100%;
    border-collapse: collapse;
    margin: 16px 0;
    
    td, th {
      border: 1px solid #ddd;
      padding: 10px 12px;
    }
    
    th {
      background: #f5f7fa;
      font-weight: 600;
      text-align: center;
    }
    
    td {
      text-align: left;
    }
  }
  
  :deep(p) {
    margin: 12px 0;
    text-indent: 2em;
  }
}
</style>
