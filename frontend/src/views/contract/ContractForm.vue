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
        <ContractFormBasicSection
          :form="form"
          :categories="categories"
          :folders="folders"
          :parent-contract-options="parentContractOptions"
          :locale="locale"
          :currency-options="currencyOptions"
          @type-change="handleTypeChange"
        />

        <ContractFormDynamicFieldsSection
          :dynamic-fields="dynamicFields"
          :dynamic-field-values="dynamicFieldValues"
          :locale="locale"
          :get-select-options="getSelectOptions"
        />

        <ContractFormCounterpartySection
          :counterparties="form.counterparties"
          @add="addCounterparty"
          @remove="removeCounterparty"
        />

        <ContractFormContentSection
          v-model:content-mode="contentMode"
          v-model:show-full-preview-dialog="showFullPreviewDialog"
          :form="form"
          :contract-type="form.type"
          :templates="filteredTemplates"
          :selected-template-id="selectedTemplateId"
          :selected-template="selectedTemplate"
          :template-variables="templateVariables"
          :extracted-variables="extractedVariables"
          :preview-content="previewContent"
          :uploaded-file="uploadedFile"
          :uploaded-file-url="uploadedFileUrl"
          :pdf-generating="pdfGenerating"
          :get-quick-code-options="getQuickCodeOptions"
          @template-change="handleTemplateChange"
          @preview-update="updatePreview"
          @file-upload="handleFileUpload"
          @clear-upload="clearUpload"
          @generate-pdf="generatePdf"
        />

        <ContractFormAttachmentsSection
          :generated-pdf="generatedPdf"
          :attachments="attachments"
          @download-generated-pdf="handleDownloadGeneratedPdf"
          @remove-generated-pdf="removeGeneratedPdf"
          @attachment-change="handleAttachmentChange"
          @attachment-remove="handleAttachmentRemove"
          @remove-attachment="removeAttachment"
        />

        <div class="form-actions">
          <el-button v-if="isEdit" type="warning" :loading="aiAnalyzing" @click="handleEditAiAnalyze">
            {{ $t('ai.analyze') }}
          </el-button>
          <el-button v-if="isEdit" type="info" :loading="savingDraft" @click="handleSaveDraft">{{ $t('common.saveDraft') }}</el-button>
          <el-button type="primary" :loading="loading" @click="handleSubmit">{{ $t('common.submit') }}</el-button>
        </div>
      </el-form>
    </el-card>

    <ContractFormAiDialog
      v-model="aiDialogVisible"
      :ai-analyzing="aiAnalyzing"
      :ai-config-snapshot="aiConfigSnapshot"
      :ai-result="aiResult"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { createContract, updateContract, getContract, getContractPayload, getNextContractNo } from '@/api/contract'
import { getCounterpartiesByContractId, saveCounterpartiesBatch, type Counterparty as ApiCounterparty } from '@/api/counterparty'
import { getAttachmentsByContractId, saveAttachmentsBatch, type Attachment as ContractAttachment } from '@/api/attachment'
import { ArrowLeft } from '@element-plus/icons-vue'
import ContractFormBasicSection from './components/ContractFormBasicSection.vue'
import ContractFormDynamicFieldsSection from './components/ContractFormDynamicFieldsSection.vue'
import ContractFormCounterpartySection from './components/ContractFormCounterpartySection.vue'
import ContractFormContentSection from './components/ContractFormContentSection.vue'
import ContractFormAttachmentsSection from './components/ContractFormAttachmentsSection.vue'
import ContractFormAiDialog from './components/ContractFormAiDialog.vue'
import type { ExtractedVariableField } from './components/ContractFormContentSection.vue'
import type {
  ContractFormState,
  ContractFormUpsertPayload,
  ContractAttachmentDraft,
  DynamicFieldValue
} from '@/views/contract/contractFormTypes'
import { useContractFormCatalog } from '@/views/contract/composables/useContractFormCatalog'
import { useContractFormTemplateAndDynamic } from '@/views/contract/composables/useContractFormTemplateAndDynamic'
import { useContractFormFileActions } from '@/views/contract/composables/useContractFormFileActions'
import { useContractFormAi } from '@/views/contract/composables/useContractFormAi'

const route = useRoute()
const router = useRouter()
const { t, locale } = useI18n()
const formRef = ref<FormInstance>()
const loading = ref(false)

const isEdit = computed(() => !!route.params.id)
const contractId = computed(() => Number(route.params.id))

const {
  categories,
  folders,
  parentContractOptions,
  loadCategories,
  loadFolders,
  loadParentContracts
} = useContractFormCatalog(isEdit, contractId)

const form = reactive<ContractFormState>({
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
  folderId: null,
  relationType: 'MAIN',
  parentContractId: null,
  counterparties: [{ type: 'partyA', name: '', contact: '', phone: '', email: '' }]
})

const contentMode = ref<'template' | 'upload'>('template')
const showFullPreviewDialog = ref(false)
const dynamicFieldValues = reactive<Record<string, DynamicFieldValue>>({})

const {
  dynamicFields,
  templates,
  filteredTemplates,
  selectedTemplate,
  selectedTemplateId,
  templateVariables,
  extractedVariables,
  previewContent,
  loadTemplates,
  loadTemplateVariables,
  handleTemplateChange,
  getQuickCodeOptions,
  updatePreview,
  getSelectOptions,
  normalizeDynamicFieldValue,
  loadDynamicFields,
  handleTypeChange,
  getVariableLabel
} = useContractFormTemplateAndDynamic(form, {
  isEdit,
  contentMode,
  dynamicFieldValues
})

const attachments = ref<ContractAttachmentDraft[]>([])
const {
  uploadedFile,
  uploadedFileUrl,
  pdfGenerating,
  generatedPdf,
  handleFileUpload,
  clearUpload,
  handleAttachmentChange,
  handleAttachmentRemove,
  removeAttachment,
  generatePdf,
  removeGeneratedPdf,
  handleDownloadGeneratedPdf
} = useContractFormFileActions(form, attachments)

const {
  aiDialogVisible,
  aiAnalyzing,
  aiConfigSnapshot,
  aiResult,
  handleEditAiAnalyze
} = useContractFormAi(form, isEdit, contractId)

const savingDraft = ref(false)

const currencyOptions = [
  { value: 'CNY', label: 'CNY - RMB' },
  { value: 'USD', label: 'USD - US Dollar' },
  { value: 'EUR', label: 'EUR - Euro' },
  { value: 'HKD', label: 'HKD - Hong Kong Dollar' }
]

const currentStepRules = computed<FormRules>(() => {
  const rules: FormRules = {}
  rules.title = [{ required: true, message: t('contract.error.title'), trigger: 'blur' }]
  rules.type = [{ required: true, message: t('contract.error.type'), trigger: 'change' }]
  rules.amount = [{ required: true, message: t('contract.error.amount'), trigger: 'blur' }]
  rules.currency = [{ required: true, message: t('contract.error.currency'), trigger: 'change' }]
  rules.startDate = [{ required: true, message: t('contract.error.startDate'), trigger: 'change' }]
  rules.endDate = [{ required: true, message: t('contract.error.endDate'), trigger: 'change' }]
  return rules
})

watch(
  () => form.relationType,
  (newType) => {
    if (newType !== 'SUPPLEMENT') {
      form.parentContractId = null
    }
  }
)

const addCounterparty = () => {
  const types = ['partyA', 'partyB', 'partyC', 'partyD', 'partyE', 'partyF', 'partyG', 'partyH', 'partyI', 'partyJ']
  const usedTypes = form.counterparties.map((cp) => cp.type)
  const availableType = types.find((x) => !usedTypes.includes(x))
  if (!availableType) {
    ElMessage.warning(t('contract.maxCounterparties'))
    return
  }
  form.counterparties.push({ type: availableType, name: '', contact: '', phone: '', email: '' })
}

const removeCounterparty = (index: number) => {
  form.counterparties.splice(index, 1)
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
      const tpl = templates.value.find((x) => Number(x.id) === Number(data.templateId)) ?? null
      if (tpl && form.type && tpl.category !== form.type) {
        selectedTemplateId.value = null
        selectedTemplate.value = null
      } else {
        selectedTemplate.value = tpl
      }
    }

    if (data.contentMode) {
      contentMode.value = data.contentMode
    }

    if (data.templateVariables) {
      try {
        const vars = JSON.parse(data.templateVariables)
        templateVariables.value = vars
        extractedVariables.value = Object.keys(vars).map(
          (key): ExtractedVariableField => ({
            key,
            label: getVariableLabel(key),
            type: 'text',
            quickCodeCode: '',
            required: false
          })
        )
      } catch (e) {
        console.error('Failed to parse templateVariables:', e)
      }
    }

    if (selectedTemplate.value && form.content) {
      previewContent.value = form.content
    }

    try {
      const cpRes = await getCounterpartiesByContractId(contractId.value)
      const cpList = cpRes.data ?? []
      if (cpList.length > 0) {
        form.counterparties = cpList.map((cp: ApiCounterparty) => ({
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
      const attList = attRes.data ?? []
      if (attList.length > 0) {
        attachments.value = attList
          .filter((att: ContractAttachment) => att.fileCategory === 'support')
          .map((att: ContractAttachment) => ({
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
        const contractFile = attList.find((att: ContractAttachment) => att.fileCategory === 'contract')
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
  } catch {
    ElMessage.error(t('contract.error.fetch'))
  }
}

const handleSaveDraft = async () => {
  if (form.relationType === 'SUPPLEMENT' && !form.parentContractId) {
    ElMessage.error(t('contract.error.parentContract'))
    return
  }
  savingDraft.value = true
  try {
    const draftData: ContractFormUpsertPayload = {
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

      const draftAttachments: Array<{
        fileName: string
        fileUrl: string
        fileSize?: number
        fileType: string
        fileCategory: 'contract' | 'support'
      }> = attachments.value.map((att) => ({
        fileName: att.fileName || att.name,
        fileUrl: att.fileUrl || att.url || '',
        fileSize: att.fileSize || att.size,
        fileType: att.type || 'application/octet-stream',
        fileCategory: 'support'
      }))

      if (generatedPdf.value?.serverUrl) {
        draftAttachments.push({
          fileName: generatedPdf.value.name,
          fileUrl: generatedPdf.value.serverUrl,
          fileSize: generatedPdf.value.blob?.size || 0,
          fileType: 'application/pdf',
          fileCategory: 'contract'
        })
      }

      if (draftAttachments.length > 0) {
        await saveAttachmentsBatch(savedContractId, draftAttachments)
      }
    }

    ElMessage.success(t('common.success'))
  } catch {
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
  const emptyNames = form.counterparties.filter((cp) => !cp.name.trim())
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
    const submitData: ContractFormUpsertPayload = {
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

    const submitAttachments: Array<{
      fileName: string
      fileUrl: string
      fileSize?: number
      fileType: string
      fileCategory: 'contract' | 'support'
    }> = attachments.value.map((att) => ({
      fileName: att.fileName || att.name,
      fileUrl: att.fileUrl || att.url || '',
      fileSize: att.fileSize || att.size,
      fileType: att.type || 'application/octet-stream',
      fileCategory: 'support'
    }))

    if (generatedPdf.value?.serverUrl) {
      submitAttachments.push({
        fileName: generatedPdf.value.name,
        fileUrl: generatedPdf.value.serverUrl,
        fileSize: generatedPdf.value.blob?.size || 0,
        fileType: 'application/pdf',
        fileCategory: 'contract'
      })
    }

    if (submitAttachments.length > 0) {
      await saveAttachmentsBatch(savedContractId, submitAttachments)
    }

    ElMessage.success(t('common.success'))
    router.push('/contracts')
  } catch (error: unknown) {
    ElMessage.error((error as { message?: string })?.message || t('common.error'))
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadCategories(), loadFolders(), loadParentContracts(), loadTemplates(), loadTemplateVariables()])
  if (isEdit.value) {
    await fetchContract()
  } else {
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

.form-card {
  margin-top: 8px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}
</style>
