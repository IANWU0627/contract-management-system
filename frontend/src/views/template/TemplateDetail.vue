<template>
  <div class="template-detail">
    <div class="page-header">
      <el-button @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        {{ $t('common.back') }}
      </el-button>
      <h1 class="page-title">{{ isEdit ? $t('template.edit') : $t('template.create') }}</h1>
    </div>

    <el-alert
      v-if="isEdit"
      type="info"
      :closable="false"
      show-icon
      class="template-full-editor-banner"
    >
      <template #title>{{ $t('template.fullEditorTitle') }}</template>
      <span class="template-full-editor-banner__text">{{ $t('template.fullEditorDesc') }}</span>
      <el-button type="primary" link class="template-full-editor-banner__btn" @click="goFullEditor">
        {{ $t('template.openFullEditor') }}
      </el-button>
    </el-alert>
    
    <el-card shadow="hover">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('template.name')" prop="name">
              <el-input v-model="form.name" :placeholder="t('template.placeholder.name')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('template.category')" prop="category">
              <el-select v-model="form.category" :placeholder="t('common.placeholder.select')" style="width: 100%">
                <el-option v-for="cat in categories" :key="cat.code" :label="cat.name" :value="cat.code">
                  <span class="category-dot" :style="{ background: cat.color }"></span>
                  {{ cat.name }}
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item :label="$t('template.description')" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" :placeholder="t('template.placeholder.description')" />
        </el-form-item>
        
        <el-form-item :label="$t('template.content')" prop="content">
          <div class="content-editor">
            <div class="variable-toolbar">
              <span class="toolbar-label">{{ $t('template.insertVariable') }}:</span>
              <el-select v-model="selectedCategory" :placeholder="$t('variable.category')" style="width: 160px" clearable>
                <el-option v-for="cat in categoryOptions" :key="cat.value" :label="cat.label" :value="cat.value" />
              </el-select>
              <el-select v-model="selectedVariable" :placeholder="$t('template.selectVariable')" style="width: 280px" clearable filterable>
                <el-option v-for="v in filteredVariables" :key="v.code" :label="v.code + ' - ' + v.name" :value="v.code" />
              </el-select>
              <el-button type="primary" @click="insertSelectedVariable" :disabled="!selectedVariable">
                {{ $t('template.insert') }}
              </el-button>
              <el-button @click="loadVariables" :loading="loadingVariables">
                <el-icon><Refresh /></el-icon>
              </el-button>
              <div class="toolbar-divider"></div>
              <el-popover placement="bottom" :width="300" trigger="click">
                <template #reference>
                  <el-button size="small">
                    <el-icon><Grid /></el-icon>
                    {{ $t('template.insertTable') }}
                  </el-button>
                </template>
                <div class="table-insert-form">
                  <el-form :model="tableForm" label-width="80px" size="small">
                    <el-form-item :label="$t('template.tableRows')">
                      <el-input-number v-model="tableForm.rows" :min="1" :max="20" />
                    </el-form-item>
                    <el-form-item :label="$t('template.tableCols')">
                      <el-input-number v-model="tableForm.cols" :min="1" :max="10" />
                    </el-form-item>
                    <el-form-item>
                      <el-button type="primary" @click="insertTable">{{ $t('template.insert') }}</el-button>
                    </el-form-item>
                  </el-form>
                </div>
              </el-popover>
              <div class="toolbar-divider"></div>
              <span class="toolbar-label">{{ $t('template.editorMode') }}:</span>
              <el-radio-group v-model="editorMode" size="small">
                <el-radio-button value="rich">{{ $t('template.richEditor') }}</el-radio-button>
                <el-radio-button value="code">{{ $t('template.codeEditor') }}</el-radio-button>
              </el-radio-group>
            </div>

            <div v-if="editorMode === 'rich'" class="editor-meta-row">
              <span class="editor-hint-text">{{ $t('template.editorHelpHint') }}</span>
              <span class="content-stats">{{ $t('template.contentStats', { chars: contentPlainLength }) }}</span>
            </div>
            
            <!-- Rich Text Editor (Quill) -->
            <div v-show="editorMode === 'rich'" class="quill-editor-panel">
              <QuillEditor 
                ref="quillEditor"
                v-model:content="form.content" 
                contentType="html"
                :options="templateQuillEditorOptions"
                :placeholder="t('template.placeholder.templateContent')"
                theme="snow"
                class="quill-editor"
                @ready="onQuillReady"
              />
            </div>
            
            <!-- Code Editor (Textarea) -->
            <div v-show="editorMode === 'code'" class="code-wrapper">
              <div class="editor-meta-row editor-meta-row--code">
                <span class="content-stats">{{ $t('template.contentStats', { chars: contentPlainLength }) }}</span>
              </div>
              <el-input
                ref="contentTextarea"
                v-model="form.content"
                type="textarea"
                :rows="25"
                :placeholder="t('template.placeholder.content')"
                class="content-textarea"
              />
            </div>
          </div>
        </el-form-item>
        
        <el-form-item v-if="sortedExtractedEntries.length > 0" label-width="0" class="extracted-variables-form-item">
          <div class="extracted-variables-layout">
            <div class="extracted-variables-header">
              <span class="extracted-variables-title">{{ $t('template.extractedVariables') }}</span>
              <el-tag size="small" type="info" effect="plain" round>{{ sortedExtractedEntries.length }}</el-tag>
            </div>
            <el-collapse v-model="extractedCollapseActive" class="extracted-variables-collapse">
            <el-collapse-item name="detail">
              <template #title>
                <div class="extracted-collapse-title">
                  <div class="extracted-preview-tags">
                    <el-tag
                      v-for="(code, pi) in extractedPreviewSlice.codes"
                      :key="code"
                      :type="previewTagType(pi)"
                      size="small"
                      effect="light"
                      class="extracted-preview-tag"
                    >{{ code }}</el-tag>
                    <span v-if="extractedPreviewSlice.overflow > 0" class="extracted-preview-more">+{{ extractedPreviewSlice.overflow }}</span>
                  </div>
                  <span class="extracted-expand-hint">{{ $t('template.extractedVariablesExpandHint') }}</span>
                </div>
              </template>
              <div class="extracted-variables-panel">
                <p class="extracted-variables-hint">{{ $t('template.extractedVariablesTip') }}</p>
                <div class="extracted-variables-grid" role="list">
                  <div
                    v-for="(row, idx) in sortedExtractedEntries"
                    :key="row.code"
                    class="extracted-var-cell"
                    role="listitem"
                  >
                    <span class="extracted-var-index">{{ idx + 1 }}</span>
                    <div class="extracted-var-body">
                      <code class="extracted-var-code">[[{{ row.code }}]]</code>
                      <span v-if="row.displayName" class="extracted-var-name">{{ row.displayName }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">{{ $t('common.save') }}</el-button>
          <el-button @click="$router.back()">{{ $t('common.cancel') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import type { FormInstance } from 'element-plus'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import '@/utils/quillTableEmbed'
import Delta from 'quill-delta'
import Quill from 'quill'
import type { Quill as QuillInstance } from 'quill'
import { createTemplate, updateTemplate, getTemplate } from '@/api/template'
import { fetchAllTemplateVariables, type TemplateVariableItem } from '@/api/templateVariable'
import { getContractCategories } from '@/api/contractCategory'
import { ArrowLeft, Refresh, Grid } from '@element-plus/icons-vue'
import { templateQuillEditorOptions } from './templateQuillConfig'

interface ContractCategoryItem {
  code: string
  name: string
  color?: string
}

const { t, locale } = useI18n()

const route = useRoute()
const router = useRouter()

const formRef = ref<FormInstance>()
const loading = ref(false)
const loadingVariables = ref(false)
const contentTextarea = ref<HTMLTextAreaElement>()
const quillEditor = ref<InstanceType<typeof QuillEditor> | null>(null)
const categories = ref<ContractCategoryItem[]>([])

const editorMode = ref('rich')

const PREVIEW_TAG_TYPES = ['primary', 'success', 'warning', 'info', 'danger'] as const

const previewTagType = (index: number) => PREVIEW_TAG_TYPES[index % PREVIEW_TAG_TYPES.length]

const onQuillReady = (quill: QuillInstance) => {
  quill.clipboard.addMatcher('table', (node: HTMLElement) => {
    if (node.tagName !== 'TABLE') {
      return new Delta()
    }
    return new Delta().insert({ 'table-embed': node.outerHTML })
  })
}

const contentPlainLength = computed(() => {
  const html = form.content || ''
  if (!html) return 0
  if (editorMode.value === 'code') return html.length
  if (typeof document === 'undefined') return html.replace(/<[^>]+>/g, '').length
  const div = document.createElement('div')
  div.innerHTML = html
  return (div.textContent || '').length
})

const isEdit = computed(() => !!route.params.id)
const templateId = computed(() => Number(route.params.id))

const goFullEditor = () => {
  router.push({ name: 'TemplateEdit', params: { id: String(templateId.value) } })
}

const allVariables = ref<TemplateVariableItem[]>([])
const selectedCategory = ref('')
const selectedVariable = ref('')

const categoryOptions = computed(() => {
  const cats = [
    { value: 'system', label: t('variable.categories.system') },
    { value: 'party', label: t('variable.categories.party') },
    { value: 'purchase', label: t('variable.categories.purchase') },
    { value: 'service', label: t('variable.categories.service') },
    { value: 'lease', label: t('variable.categories.lease') },
    { value: 'employment', label: t('variable.categories.employment') },
    { value: 'agency', label: t('variable.categories.agency') },
    { value: 'custom', label: t('variable.categories.custom') }
  ]
  return cats
})

const filteredVariables = computed(() => {
  if (!selectedCategory.value) {
    return allVariables.value
  }
  return allVariables.value.filter(v => v.category === selectedCategory.value)
})

const loadVariables = async () => {
  loadingVariables.value = true
  try {
    allVariables.value = await fetchAllTemplateVariables({ status: 1 })
  } catch (error) {
    console.error('Failed to load variables', error)
    ElMessage.error(t('common.error'))
  } finally {
    loadingVariables.value = false
  }
}

const loadCategories = async () => {
  try {
    const res = await getContractCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error('Failed to load categories', error)
  }
}

const tableForm = reactive({
  rows: 3,
  cols: 3
})

const insertTable = () => {
  const rows = tableForm.rows
  const cols = tableForm.cols
  let tableInner = '<table style="border-collapse: collapse; width: 100%; margin: 16px 0;">'
  
  for (let i = 0; i < rows; i++) {
    tableInner += '<tr>'
    for (let j = 0; j < cols; j++) {
      const tag = i === 0 ? 'th' : 'td'
      const style = i === 0 ? 'border: 1px solid #333; padding: 8px; background: #f5f5f5; font-weight: bold; text-align: center;' : 'border: 1px solid #333; padding: 8px;'
      tableInner += `<${tag} style="${style}">${i === 0 ? t('template.tableHeaderCell', { col: j + 1 }) : ''}</${tag}>`
    }
    tableInner += '</tr>'
  }
  tableInner += '</table>'
  const tableHtmlForCode = `${tableInner}<p><br></p>`
  
  if (editorMode.value === 'rich' && quillEditor.value) {
    const quill = quillEditor.value.getQuill()
    const range = quill.getSelection(true)
    const index = range ? range.index : Math.max(0, quill.getLength() - 1)
    quill.insertEmbed(index, 'table-embed', tableInner, Quill.sources.USER)
    quill.insertText(index + 1, '\n', Quill.sources.USER)
    quill.setSelection(index + 2, 0, Quill.sources.SILENT)
    quill.focus()
  } else {
    form.content += tableHtmlForCode
  }
  
  ElMessage.success(t('template.tableInserted'))
}

const insertSelectedVariable = () => {
  if (selectedVariable.value) {
    const varText = '[[' + selectedVariable.value + ']]'
    
    if (editorMode.value === 'rich' && quillEditor.value) {
      const quill = quillEditor.value.getQuill()
      const range = quill.getSelection()
      if (range) {
        quill.insertText(range.index, varText)
      } else {
        quill.setText(quill.getText() + varText)
      }
      quill.focus()
    } else {
      form.content += varText
    }
    
    ElMessage.success(t('template.inserted') + ': ' + varText)
    selectedVariable.value = ''
  }
}

const form = reactive({
  name: '',
  category: '',
  description: '',
  content: ''
})

const extractedVariables = ref<string[]>([])

const variableByCode = computed(() => {
  const map = new Map<string, TemplateVariableItem>()
  for (const v of allVariables.value) {
    map.set(v.code, v)
  }
  return map
})

/** 排序后的展示行：编码序 + 解析到的显示名（与变量库对齐） */
const sortedExtractedEntries = computed(() => {
  const codes = extractedVariables.value.slice().sort((a, b) => a.localeCompare(b))
  return codes.map((code) => {
    const item = variableByCode.value.get(code)
    let displayName = ''
    if (item) {
      if (item.label) {
        displayName = item.label
      } else if (locale.value === 'en' && item.nameEn) {
        displayName = item.nameEn
      } else {
        displayName = item.name || ''
      }
    }
    return { code, displayName }
  })
})

/** 折叠态标题：前若干个变量编码（彩色标签） */
const extractedPreviewSlice = computed(() => {
  const codes = sortedExtractedEntries.value.map((e) => e.code)
  const max = 12
  return {
    codes: codes.slice(0, max),
    overflow: Math.max(0, codes.length - max)
  }
})

const extractedCollapseActive = ref<string[]>([])

const extractVariables = (content: string) => {
  const pattern = /\[\[(\w+)\]\]/g
  const matches: string[] = []
  let match
  while ((match = pattern.exec(content)) !== null) {
    if (!matches.includes(match[1])) {
      matches.push(match[1])
    }
  }
  extractedVariables.value = matches
}

watch(() => form.content, (newContent) => {
  if (newContent) {
    extractVariables(newContent)
  }
}, { immediate: true })

const rules = {
  name: [{ required: true, message: t('template.error.name'), trigger: 'blur' }],
  category: [{ required: true, message: t('template.error.category'), trigger: 'change' }],
  content: [{ required: true, message: t('template.error.content'), trigger: 'blur' }]
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      if (isEdit.value) {
        await updateTemplate(templateId.value, form)
        ElMessage.success(t('common.success'))
      } else {
        await createTemplate(form)
        ElMessage.success(t('common.success'))
      }
      router.push('/templates')
    } catch (error: unknown) {
      const err = error as { message?: string }
      ElMessage.error(err.message || t('common.error'))
    } finally {
      loading.value = false
    }
  })
}

const fetchTemplate = async () => {
  try {
    const res = await getTemplate(templateId.value)
    if (res.data) {
      form.name = res.data.name || ''
      form.category = res.data.category || ''
      form.description = res.data.description || ''
      form.content = res.data.content || ''
      extractVariables(form.content)
    }
  } catch (error) {
    ElMessage.error(t('template.error.load'))
  }
}

onMounted(() => {
  loadCategories()
  loadVariables()
  if (isEdit.value) {
    fetchTemplate()
  }
})
</script>

<style scoped lang="scss">
.template-full-editor-banner {
  margin-bottom: 16px;

  .template-full-editor-banner__text {
    margin-right: 8px;
    font-size: 13px;
    line-height: 1.5;
  }

  .template-full-editor-banner__btn {
    vertical-align: baseline;
  }
}

.template-detail {
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
}

.content-editor {
  width: 100%;
  
  .variable-toolbar {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 12px;
    flex-wrap: wrap;
    padding: 10px 12px;
    background: var(--bg-hover);
    border-radius: 4px;
    
    .toolbar-label {
      font-size: 13px;
      color: var(--text-secondary);
      font-weight: 500;
      white-space: nowrap;
    }
    
    .toolbar-divider {
      width: 1px;
      height: 24px;
      background: var(--border-color);
      margin: 0 4px;
    }
  }
}

.editor-meta-row {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px 16px;
  margin-bottom: 8px;
  font-size: 12px;
  line-height: 1.5;
}

.editor-meta-row--code {
  justify-content: flex-end;
  margin-bottom: 6px;
}

.editor-hint-text {
  flex: 1;
  min-width: 200px;
  color: var(--el-text-color-secondary);
}

.content-stats {
  flex-shrink: 0;
  color: var(--el-text-color-placeholder);
  font-variant-numeric: tabular-nums;
}

.quill-editor-panel {
  border: 1px solid var(--border-color);
  border-radius: 4px;
  overflow: hidden;
  width: 100%;
  background: var(--el-fill-color-blank);

  :deep(.ql-toolbar.ql-snow) {
    position: sticky;
    top: 0;
    z-index: 6;
    background: var(--el-bg-color-overlay, var(--el-fill-color-blank));
    border: none;
    border-bottom: 1px solid var(--border-color);
    flex-wrap: wrap;
    row-gap: 4px;
    padding-top: 8px;
    padding-bottom: 8px;
  }

  :deep(.ql-container.ql-snow) {
    border: none;
  }

  :deep(.ql-table-embed) {
    margin: 12px 0;

    table {
      width: 100%;
      border-collapse: collapse;
    }
  }
  
  :deep(.ql-container) {
    min-height: 500px;
    max-height: 720px;
    font-size: 14px;
    width: 100%;
    line-height: 1.65;
  }
  
  :deep(.ql-editor) {
    min-height: 500px;
    max-height: 720px;
    width: 100%;
    line-height: 1.65;
    tab-size: 4;
  }
  
  :deep(.ql-editor p),
  :deep(.ql-editor li) {
    line-height: 1.65;
  }
}

.code-wrapper {
  width: 100%;
  
  :deep(.el-textarea) {
    width: 100%;
  }
  
  :deep(textarea) {
    font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
    font-size: 13px;
    line-height: 1.6;
    min-height: 500px;
    width: 100%;
  }
}

.extracted-variables-form-item {
  align-items: flex-start;

  :deep(.el-form-item__content) {
    margin-left: 0 !important;
    width: 100%;
  }
}

.extracted-variables-layout {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.extracted-variables-header {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding-left: 0;
}

.extracted-variables-title {
  font-size: 14px;
  color: var(--el-text-color-regular);
  font-weight: 500;
}

.extracted-variables-collapse {
  width: 100%;
  border: none;

  :deep(.el-collapse-item__wrap) {
    border: none;
  }

  :deep(.el-collapse-item__header) {
    height: auto;
    min-height: 40px;
    line-height: 1.45;
    padding: 8px 12px;
    border-radius: 8px;
    border: 1px solid var(--el-border-color-lighter);
    background: var(--el-fill-color-blank);
  }

  :deep(.el-collapse-item__arrow) {
    margin: 0 0 0 8px;
  }

  :deep(.el-collapse-item__content) {
    padding-bottom: 0;
  }
}

.extracted-collapse-title {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 12px;
  width: 100%;
  padding-right: 8px;
}

.extracted-preview-tags {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}

.extracted-preview-tag {
  font-family: ui-monospace, 'Monaco', 'Menlo', monospace;
  max-width: 100%;
}

.extracted-preview-more {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  flex-shrink: 0;
}

.extracted-expand-hint {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.extracted-variables-panel {
  width: 100%;
}

.extracted-variables-hint {
  margin: 0 0 12px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
}

.extracted-variables-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 10px;
}

.extracted-var-cell {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  padding: 10px 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  background: var(--el-fill-color-blank);
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.extracted-var-cell:hover {
  border-color: var(--el-color-primary-light-5);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.extracted-var-index {
  flex-shrink: 0;
  width: 22px;
  height: 22px;
  line-height: 22px;
  text-align: center;
  font-size: 12px;
  font-weight: 600;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  border-radius: 6px;
}

.extracted-var-body {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.extracted-var-code {
  font-family: ui-monospace, 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  color: var(--el-color-primary);
  word-break: break-all;
}

.extracted-var-name {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.table-insert-form {
  .el-form-item {
    margin-bottom: 12px;
  }
}

.category-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
  vertical-align: middle;
}
</style>
