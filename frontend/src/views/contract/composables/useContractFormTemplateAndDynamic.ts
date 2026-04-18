import { ref, watch, computed, type Ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { getContractTypeFormFields, type ContractTypeField } from '@/api/contractTypeField'
import { getQuickCodeByCode } from '@/api/quickCode'
import { getTemplateList, extractTemplateVariables, replaceTemplateVariables, type Template } from '@/api/template'
import { fetchAllTemplateVariables, type TemplateVariableItem } from '@/api/templateVariable'
import type { ExtractedVariableField } from '@/views/contract/components/ContractFormContentSection.vue'
import type {
  DynamicFieldValue,
  QuickCodeOption,
  ContractFormState,
  TemplateVariableValue
} from '@/views/contract/contractFormTypes'

export function useContractFormTemplateAndDynamic(
  form: ContractFormState,
  options: {
    isEdit: Ref<boolean>
    contentMode: Ref<'template' | 'upload'>
    dynamicFieldValues: Record<string, DynamicFieldValue>
  }
) {
  const { locale } = useI18n()

  const dynamicFields = ref<ContractTypeField[]>([])
  const quickCodeItemsCache = ref<Record<string, QuickCodeOption[]>>({})

  const templates = ref<Template[]>([])
  const filteredTemplates = computed(() => {
    const list = templates.value
    const t = form.type
    if (!t) return []
    return list.filter((item) => (item.category || '') === t)
  })
  const templateVariableList = ref<TemplateVariableItem[]>([])
  const selectedTemplate = ref<Template | null>(null)
  const selectedTemplateId = ref<number | null>(null)
  const templateVariables = ref<Record<string, TemplateVariableValue>>({})
  const extractedVariables = ref<ExtractedVariableField[]>([])
  const previewContent = ref('')

  const loadTemplates = async () => {
    try {
      const res = await getTemplateList({ pageSize: 100 })
      templates.value = res.data.list
    } catch (error) {
      console.error('Failed to load templates:', error)
    }
  }

  const loadTemplateVariables = async () => {
    try {
      templateVariableList.value = await fetchAllTemplateVariables({ status: 1 })
    } catch (error) {
      console.error('Failed to load template variables:', error)
    }
  }

  const getVariableLabel = (key: string): string => {
    const tv = templateVariableList.value.find((v) => v.code === key)
    if (tv) {
      const zh = tv.name?.trim() || tv.label?.trim()
      if (locale.value === 'en') {
        const en = tv.nameEn?.trim()
        return en || zh || key
      }
      return zh || key
    }
    const dynamicField = dynamicFields.value.find((f) => f.fieldKey === key)
    if (dynamicField) {
      return locale.value === 'zh' ? dynamicField.fieldLabel : dynamicField.fieldLabelEn || dynamicField.fieldLabel
    }
    return key
  }

  const loadQuickCodeItems = async (quickCodeCode: string) => {
    if (!quickCodeCode || quickCodeItemsCache.value[quickCodeCode]) return
    try {
      const res = await getQuickCodeByCode(quickCodeCode)
      const d = res.data as QuickCodeOption[] | { items?: QuickCodeOption[] } | undefined
      const items = Array.isArray(d) ? d : (d?.items ?? [])
      quickCodeItemsCache.value[quickCodeCode] = items.filter((item) => item.enabled !== false)
    } catch (error) {
      console.error('Failed to load quick code items:', error)
    }
  }

  const extractVariables = async () => {
    if (!selectedTemplate.value?.content) return
    try {
      const res = await extractTemplateVariables(selectedTemplate.value.content)
      const variableCodes = res.data
      templateVariables.value = {}
      extractedVariables.value = []
      for (const v of variableCodes) {
        const tv = templateVariableList.value.find((item) => item.code === v)
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
          type: variableType,
          quickCodeCode,
          required: !!tv?.required
        })
        if (quickCodeCode) {
          await loadQuickCodeItems(quickCodeCode)
        }
      }
    } catch (error) {
      console.error('Failed to extract variables:', error)
    }
  }

  const handleTemplateChange = async (templateId: number | null) => {
    if (!templateId) {
      selectedTemplate.value = null
      selectedTemplateId.value = null
      return
    }
    selectedTemplateId.value = templateId
    selectedTemplate.value = templates.value.find((t) => Number(t.id) === Number(templateId)) ?? null
    if (selectedTemplate.value) {
      await extractVariables()
      updatePreview()
    }
  }

  const getQuickCodeOptions = (quickCodeCode: string): QuickCodeOption[] => {
    if (!quickCodeCode) return []
    if (quickCodeItemsCache.value[quickCodeCode]) {
      return quickCodeItemsCache.value[quickCodeCode]
    }
    return []
  }

  const updatePreview = async () => {
    if (!selectedTemplate.value?.content) return

    const allValues: Record<string, unknown> = {
      ...templateVariables.value,
      contractNo: form.contractNo,
      startDate: form.startDate,
      endDate: form.endDate,
      amount: form.amount,
      counterparties: form.counterparties.map((cp) => ({ type: cp.type, name: cp.name }))
    }

    for (const key in options.dynamicFieldValues) {
      allValues[key] = options.dynamicFieldValues[key]
    }

    try {
      const res = await replaceTemplateVariables(selectedTemplate.value.content, allValues)
      previewContent.value = res.data
      form.content = previewContent.value
    } catch {
      previewContent.value = selectedTemplate.value.content
      form.content = previewContent.value
    }
  }

  let previewDebounceTimer: ReturnType<typeof setTimeout> | null = null
  const updatePreviewDebounced = () => {
    if (previewDebounceTimer) clearTimeout(previewDebounceTimer)
    previewDebounceTimer = setTimeout(() => {
      updatePreview()
    }, 500)
  }

  watch(templateVariables, () => {
    if (selectedTemplate.value && options.contentMode.value === 'template') {
      updatePreviewDebounced()
    }
  }, { deep: true })

  watch(
    () => form.counterparties,
    () => {
      if (selectedTemplate.value && options.contentMode.value === 'template') {
        updatePreviewDebounced()
      }
    },
    { deep: true }
  )

  watch(locale, () => {
    if (extractedVariables.value.length > 0) {
      extractedVariables.value = extractedVariables.value.map((v) => ({
        ...v,
        label: getVariableLabel(v.key)
      }))
    }
  })

  watch(
    () => form.amount,
    () => {
      if (selectedTemplate.value && options.contentMode.value === 'template') {
        updatePreviewDebounced()
      }
    }
  )

  const getSelectOptions = (field: ContractTypeField) => {
    if (field.quickCodeId) {
      const items = quickCodeItemsCache.value[field.quickCodeId] || []
      return items.map((item) => ({
        ...item,
        displayLabel: locale.value === 'en' && item.meaningEn ? item.meaningEn : item.meaning
      }))
    }
    return []
  }

  const normalizeDynamicFieldValue = (field: ContractTypeField, rawValue: unknown): DynamicFieldValue => {
    if (rawValue === null || rawValue === undefined) {
      return field.fieldType === 'number' ? null : ''
    }
    if (field.fieldType === 'number') {
      if (typeof rawValue === 'number') return rawValue
      const parsed = Number(rawValue)
      return Number.isFinite(parsed) ? parsed : null
    }
    return rawValue as DynamicFieldValue
  }

  const loadDynamicFields = async () => {
    if (!form.type) {
      dynamicFields.value = []
      return
    }
    try {
      const res = await getContractTypeFormFields(form.type)
      dynamicFields.value = (res.data || []) as ContractTypeField[]
      for (const field of dynamicFields.value) {
        if (options.dynamicFieldValues[field.fieldKey] === undefined) {
          if (field.fieldType === 'number') {
            options.dynamicFieldValues[field.fieldKey] = field.defaultValue ? Number(field.defaultValue) : 0
          } else {
            options.dynamicFieldValues[field.fieldKey] = field.defaultValue || ''
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

  watch(
    () => form.type,
    async (newType) => {
      if (newType) {
        await loadDynamicFields()
      }
      const mismatch =
        selectedTemplate.value &&
        ((newType && selectedTemplate.value.category !== newType) || !newType)
      if (mismatch) {
        await handleTemplateChange(null)
        templateVariables.value = {}
        extractedVariables.value = []
        previewContent.value = ''
        if (options.contentMode.value === 'template') {
          form.content = ''
        }
      }
    }
  )

  const handleTypeChange = () => {
    if (!options.isEdit.value) {
      loadDynamicFields()
    }
  }

  return {
    dynamicFields,
    quickCodeItemsCache,
    templates,
    filteredTemplates,
    templateVariableList,
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
  }
}
