<template>
  <div class="template-form">
    <div class="page-header">
      <el-button @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        {{ $t('common.back') }}
      </el-button>
      <h1 class="page-title">{{ isEdit ? $t('template.edit') : $t('template.create') }}</h1>
    </div>
    
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
                <el-option v-for="cat in categories" :key="cat.code" :label="getCategoryName(cat)" :value="cat.code">
                  <span class="category-dot" :style="{ background: cat.color }"></span>
                  {{ getCategoryName(cat) }}
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item :label="$t('template.description')" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" :placeholder="t('template.placeholder.description')" />
        </el-form-item>

        <el-form-item :label="$t('template.content')" prop="content">
          <div class="content-editor">
            <el-alert
              type="info"
              :closable="true"
              show-icon
              class="clause-quick-use-alert"
              :title="t('template.clauseQuickUseTitle')"
            >
              <p class="clause-quick-use-text">{{ t('template.clauseQuickUseP1') }}</p>
              <p class="clause-quick-use-text">{{ t('template.clauseQuickUseP2') }}</p>
              <el-button type="primary" link @click="goClauseLibrary">
                {{ t('template.openClauseLibrary') }}
              </el-button>
            </el-alert>
            <div class="variable-toolbar">
              <div class="toolbar-group toolbar-group--vars">
                <span class="toolbar-label">{{ $t('template.insertVariable') }}:</span>
                <el-select v-model="selectedCategory" :placeholder="$t('variable.category')" class="toolbar-select toolbar-select--sm" clearable>
                  <el-option v-for="cat in categoryOptions" :key="cat.value" :label="cat.label" :value="cat.value" />
                </el-select>
                <el-select v-model="selectedVariable" :placeholder="$t('template.selectVariable')" class="toolbar-select toolbar-select--lg" clearable filterable>
                  <el-option v-for="v in filteredVariables" :key="v.code" :label="v.code + ' - ' + v.name" :value="v.code" />
                </el-select>
                <el-button type="primary" @click="insertSelectedVariable" :disabled="!selectedVariable">
                  {{ $t('template.insert') }}
                </el-button>
                <el-button @click="loadVariables" :loading="loadingVariables">
                  <el-icon><Refresh /></el-icon>
                </el-button>
              </div>
              <div class="toolbar-divider toolbar-divider--responsive" aria-hidden="true" />
              <div class="toolbar-group toolbar-group--clauses">
                <span class="toolbar-label">{{ $t('template.insertClause') }}:</span>
                <el-select v-model="selectedClauseCategory" :placeholder="$t('clause.category')" class="toolbar-select toolbar-select--sm" clearable>
                  <el-option v-for="cat in clauseCategoryOptions" :key="cat" :label="cat" :value="cat" />
                </el-select>
                <el-select v-model="selectedClauseId" :placeholder="$t('template.selectClause')" class="toolbar-select toolbar-select--lg" clearable filterable>
                  <el-option
                    v-for="clause in filteredClauses"
                    :key="clause.id"
                    :label="(locale === 'en' && clause.nameEn ? clause.nameEn : clause.name) + (clause.category ? ' · ' + clause.category : '')"
                    :value="clause.id"
                  />
                </el-select>
                <el-button @click="insertSelectedClause" :disabled="!selectedClauseId">
                  {{ $t('template.insert') }}
                </el-button>
                <el-button @click="addClauseToOutline" :disabled="!selectedClauseId">
                  {{ $t('template.addToOutline') }}
                </el-button>
              </div>
              <div class="toolbar-divider toolbar-divider--responsive" aria-hidden="true" />
              <div class="toolbar-group toolbar-group--tools">
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
                <span class="toolbar-label">{{ $t('template.editorMode') }}:</span>
                <el-radio-group v-model="editorMode" size="small">
                  <el-radio-button value="rich">{{ $t('template.richEditor') }}</el-radio-button>
                  <el-radio-button value="code">{{ $t('template.codeEditor') }}</el-radio-button>
                </el-radio-group>
              </div>
            </div>

            <div v-if="editorMode === 'rich'" class="editor-meta-row">
              <span class="editor-hint-text">{{ $t('template.editorHelpHint') }}</span>
              <span class="content-stats">{{ $t('template.contentStats', { chars: contentPlainLength }) }}</span>
            </div>

            <div class="clause-outline" v-if="clauseOutline.length > 0">
              <div class="clause-outline-header">
                <div class="clause-outline-title">
                  <span>{{ $t('template.clauseOutline') }}</span>
                  <el-tag size="small" type="info">{{ clauseOutline.length }}</el-tag>
                </div>
                <div class="clause-outline-actions">
                  <el-button size="small" type="primary" plain @click="insertClauseOutline">
                    {{ $t('template.insertOutline') }}
                  </el-button>
                  <el-button size="small" @click="clearClauseOutline">
                    {{ $t('template.clearOutline') }}
                  </el-button>
                </div>
              </div>
              <div class="clause-outline-list">
                <div v-for="(item, idx) in clauseOutline" :key="`${item.id}-${idx}`" class="clause-outline-item">
                  <div class="clause-outline-main">
                    <div class="clause-outline-name">{{ locale === 'en' && item.nameEn ? item.nameEn : item.name }}</div>
                    <div class="clause-outline-meta">
                      <el-tag size="small" effect="plain">{{ item.category || '-' }}</el-tag>
                      <span class="clause-outline-index">{{ $t('template.clauseOrder', { order: idx + 1 }) }}</span>
                    </div>
                  </div>
                  <div class="clause-outline-item-actions">
                    <el-button size="small" text :disabled="idx === 0" @click="moveClauseOutlineItem(idx, -1)">{{ $t('common.prev') }}</el-button>
                    <el-button size="small" text :disabled="idx === clauseOutline.length - 1" @click="moveClauseOutlineItem(idx, 1)">{{ $t('common.next') }}</el-button>
                    <el-button size="small" text type="danger" @click="removeClauseOutlineItem(idx)">{{ $t('common.delete') }}</el-button>
                  </div>
                </div>
              </div>
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
        
        <el-form-item v-if="sortedExtractedEntries.length > 0" class="extracted-variables-form-item">
          <template #label>
            <span class="extracted-variables-label">
              <span class="extracted-variables-title">{{ $t('template.extractedVariables') }}</span>
              <el-tag size="small" type="info" effect="plain" round>{{ sortedExtractedEntries.length }}</el-tag>
            </span>
          </template>
          <div class="extracted-variables-layout">
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
        
        <el-form-item :label="$t('template.variables')" prop="variablesText">
          <el-input 
            v-model="form.variablesText" 
            type="textarea" 
            :rows="4" 
            :placeholder="t('template.placeholder.variables')"
          />
          <div class="variables-tip">{{ $t('template.variablesTip') }}</div>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="saveTemplate(false)">
            {{ $t('common.save') }}
          </el-button>
          <el-button :loading="loading" @click="saveTemplate(true)">
            {{ $t('template.saveAndReturn') }}
          </el-button>
          <el-button @click="$router.back()">{{ $t('common.cancel') }}</el-button>
          <el-button @click="handlePreview" :loading="previewLoading">
            {{ $t('common.preview') }}
          </el-button>
          <el-button type="warning" :loading="templateAiAnalyzing" @click="handleTemplateAiAnalyze">
            {{ $t('ai.analyze') }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <TemplateFormAiDialog
      v-model="templateAiDialogVisible"
      :ai-analyzing="templateAiAnalyzing"
      :ai-config-snapshot="templateAiConfigSnapshot"
      :ai-result="templateAiResult"
    />

    <el-dialog v-model="showPreview" :title="t('common.preview')" width="min(960px, 94vw)" class="template-preview-dialog">
      <p v-if="previewUsedFallback" class="preview-fallback-hint">{{ t('template.previewFallbackHint') }}</p>
      <div class="preview-content preview-content--html" v-if="previewContent">
        <!-- Quill Snow 样式依赖 .ql-snow > .ql-editor，与编辑器内 DOM 结构一致，才能保留标题/字体/对齐/表格等格式 -->
        <div class="ql-snow template-preview-quill">
          <div class="ql-editor" v-html="previewContent"></div>
        </div>
      </div>
      <div v-else class="preview-empty">
        {{ t('template.previewEmpty') }}
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
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
import { ArrowLeft, Refresh, Grid } from '@element-plus/icons-vue'
import { getTemplate, createTemplate, updateTemplate, replaceTemplateVariables } from '@/api/template'
import type { ApiResponse } from '@/api/types'
import { useUnsavedChangesGuard } from './composables/useUnsavedChangesGuard'
import { useTemplateFormAi } from './composables/useTemplateFormAi'
import TemplateFormAiDialog from './components/TemplateFormAiDialog.vue'
import { getClauseList, type Clause } from '@/api/clause'
import { fetchAllTemplateVariables, type TemplateVariableItem } from '@/api/templateVariable'
import { getContractCategories } from '@/api/contractCategory'
import { templateQuillEditorOptions } from './templateQuillConfig'

interface ContractCategoryItem {
  code: string
  name: string
  nameEn?: string
  color?: string
}

const { t, locale } = useI18n()

const route = useRoute()
const router = useRouter()

const formRef = ref<FormInstance>()
const loading = ref(false)
const previewLoading = ref(false)
const showPreview = ref(false)
const previewContent = ref('')
const previewUsedFallback = ref(false)
const loadingVariables = ref(false)
const editorMode = ref('rich')
const quillEditor = ref<InstanceType<typeof QuillEditor> | null>(null)

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

const goClauseLibrary = () => {
  router.push({ name: 'ClauseLibrary' })
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

const allVariables = ref<TemplateVariableItem[]>([])
const selectedCategory = ref('')
const selectedVariable = ref('')
const allClauses = ref<Clause[]>([])
const selectedClauseCategory = ref('')
const selectedClauseId = ref<number | ''>('')
const clauseOutline = ref<Clause[]>([])
const categories = ref<ContractCategoryItem[]>([])

const initialSnapshot = ref('')

const buildFormSnapshot = () =>
  JSON.stringify({
    name: form.name,
    category: form.category,
    content: form.content,
    variablesText: form.variablesText,
    description: form.description,
    editorMode: editorMode.value,
    clauseOutline: clauseOutline.value.map((c) => ({ id: c.id, content: c.content ?? '' }))
  })

const captureSnapshot = () => {
  initialSnapshot.value = buildFormSnapshot()
}

const isDirty = computed(() => initialSnapshot.value !== buildFormSnapshot())

useUnsavedChangesGuard(() => isDirty.value)

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
      const style =
        i === 0
          ? 'border: 1px solid #333; padding: 8px; background: #f5f5f5; font-weight: bold; text-align: center;'
          : 'border: 1px solid #333; padding: 8px;'
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

// 合同分类（用于模板分类选择）
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

const clauseCategoryOptions = computed(() => {
  return [...new Set(allClauses.value.map(item => item.category).filter(Boolean) as string[])].sort()
})

const filteredClauses = computed(() => {
  if (!selectedClauseCategory.value) {
    return allClauses.value
  }
  return allClauses.value.filter(item => item.category === selectedClauseCategory.value)
})

const loadCategories = async () => {
  try {
    const res = await getContractCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error('Failed to load categories', error)
  }
}

const getCategoryName = (category: ContractCategoryItem | null | undefined) => {
  if (!category) return ''
  if (locale.value === 'en') {
    return category.nameEn || category.name || category.code || ''
  }
  return category.name || category.nameEn || category.code || ''
}

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

const loadClauses = async () => {
  try {
    const res = await getClauseList({ status: 1 })
    allClauses.value = res.data || []
  } catch (error) {
    console.error('Failed to load clauses', error)
  }
}

const form = reactive({
  name: '',
  category: '',
  content: '',
  variablesText: '{}',
  description: ''
})

const {
  aiDialogVisible: templateAiDialogVisible,
  aiAnalyzing: templateAiAnalyzing,
  aiConfigSnapshot: templateAiConfigSnapshot,
  aiResult: templateAiResult,
  handleTemplateAiAnalyze
} = useTemplateFormAi(form)

const extractedVariables = ref<string[]>([])

const variableByCode = computed(() => {
  const map = new Map<string, TemplateVariableItem>()
  for (const v of allVariables.value) {
    map.set(v.code, v)
  }
  return map
})

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

const extractedPreviewSlice = computed(() => {
  const codes = sortedExtractedEntries.value.map((e) => e.code)
  const max = 12
  return {
    codes: codes.slice(0, max),
    overflow: Math.max(0, codes.length - max)
  }
})

const extractedCollapseActive = ref<string[]>([])

const extractVariablesFromContent = (content: string) => {
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

watch(
  () => form.content,
  (newContent) => {
    if (newContent) {
      extractVariablesFromContent(newContent)
    } else {
      extractedVariables.value = []
    }
  },
  { immediate: true }
)

const applyBatchClausePrefill = () => {
  if (isEdit.value) return false

  const clausesQuery = typeof route.query.clauses === 'string' ? route.query.clauses : ''
  if (!clausesQuery) return false

  try {
    const parsedClauses = JSON.parse(clausesQuery)
    if (!Array.isArray(parsedClauses) || parsedClauses.length === 0) return false

    const normalizedClauses = parsedClauses
      .filter(item => item && typeof item === 'object' && item.content)
      .map(item => ({
        id: typeof item.id === 'number' ? item.id : Number(item.id) || undefined,
        code: String(item.code || ''),
        name: String(item.name || ''),
        nameEn: String(item.nameEn || ''),
        category: String(item.category || ''),
        content: String(item.content || ''),
        description: String(item.description || '')
      }))

    if (normalizedClauses.length === 0) return false

    clauseOutline.value = normalizedClauses

    const templateName = typeof route.query.templateName === 'string' ? route.query.templateName : ''
    const categories = [...new Set(normalizedClauses.map(item => item.category).filter(Boolean))]
    const descriptions = normalizedClauses.map(item => item.description).filter(Boolean)
    const mergedContent = normalizedClauses.map(item => item.content).filter(Boolean).join('\n\n')

    if (templateName && !form.name) {
      form.name = templateName
    }
    if (categories.length === 1 && !form.category) {
      form.category = categories[0]
    }
    if (descriptions.length > 0 && !form.description) {
      form.description = descriptions.slice(0, 3).join(' / ')
    }
    if (mergedContent && !form.content) {
      form.content = mergedContent
    }

    const sourceClauseCodes = normalizedClauses.map(item => item.code).filter(Boolean)
    if (sourceClauseCodes.length > 0) {
      try {
        const currentVariables = JSON.parse(form.variablesText || '{}')
        if (!currentVariables.sourceClauseCodes) {
          currentVariables.sourceClauseCodes = sourceClauseCodes
        }
        if (!currentVariables.sourceClauseCode && sourceClauseCodes.length === 1) {
          currentVariables.sourceClauseCode = sourceClauseCodes[0]
        }
        form.variablesText = JSON.stringify(currentVariables, null, 2)
      } catch {
        form.variablesText = JSON.stringify({
          sourceClauseCodes,
          sourceClauseCode: sourceClauseCodes.length === 1 ? sourceClauseCodes[0] : undefined
        }, null, 2)
      }
    }

    return true
  } catch (error) {
    console.error('Failed to parse batch clause prefill', error)
    return false
  }
}

const applyClausePrefill = () => {
  if (isEdit.value) return
  if (applyBatchClausePrefill()) return

  const clauseName = typeof route.query.clauseName === 'string' ? route.query.clauseName : ''
  const clauseCode = typeof route.query.clauseCode === 'string' ? route.query.clauseCode : ''
  const clauseCategory = typeof route.query.clauseCategory === 'string' ? route.query.clauseCategory : ''
  const clauseDescription = typeof route.query.clauseDescription === 'string' ? route.query.clauseDescription : ''
  const clauseContent = typeof route.query.clauseContent === 'string' ? route.query.clauseContent : ''

  if (clauseName && !form.name) {
    form.name = `${clauseName}${locale.value === 'en' ? ' Template' : '模板'}`
  }
  if (clauseCategory && !form.category) {
    form.category = clauseCategory
  }
  if (clauseDescription && !form.description) {
    form.description = clauseDescription
  }
  if (clauseContent && !form.content) {
    form.content = clauseContent
  }

  if (clauseContent && clauseName && clauseOutline.value.length === 0) {
    clauseOutline.value = [{
      id: typeof route.query.clauseId === 'string' ? Number(route.query.clauseId) : undefined,
      code: clauseCode,
      name: clauseName,
      category: clauseCategory,
      content: clauseContent,
      description: clauseDescription
    }]
  }

  if (clauseCode) {
    try {
      const currentVariables = JSON.parse(form.variablesText || '{}')
      if (!currentVariables.sourceClauseCode) {
        currentVariables.sourceClauseCode = clauseCode
        form.variablesText = JSON.stringify(currentVariables, null, 2)
      }
    } catch {
      form.variablesText = JSON.stringify({ sourceClauseCode: clauseCode }, null, 2)
    }
  }
}

const rules = {
  name: [{ required: true, message: t('template.error.name'), trigger: 'blur' }],
  category: [{ required: true, message: t('template.error.category'), trigger: 'change' }],
  content: [{ required: true, message: t('template.error.content'), trigger: 'blur' }],
  variablesText: [
    {
      validator: (_rule: unknown, value: string, callback: (e?: Error) => void) => {
        const s = value == null ? '' : String(value).trim()
        if (!s) {
          callback()
          return
        }
        try {
          JSON.parse(String(value))
          callback()
        } catch {
          callback(new Error(t('template.variablesError')))
        }
      },
      trigger: 'blur'
    }
  ]
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

const getSafeQuillInsertIndex = (quill: QuillInstance) => {
  const len = quill.getLength()
  if (len <= 1) return 0
  const range = quill.getSelection(true)
  const raw = range ? range.index : len - 1
  return Math.max(0, Math.min(raw, len - 1))
}

const insertTextAtCursor = (text: string, useHtml = false) => {
  if (editorMode.value === 'rich' && quillEditor.value) {
    const quill = quillEditor.value.getQuill()
    const index = getSafeQuillInsertIndex(quill)
    if (useHtml) {
      quill.clipboard.dangerouslyPasteHTML(index, text)
    } else {
      quill.insertText(index, text)
    }
    quill.focus()
    return
  }

  form.content += `${form.content ? '\n\n' : ''}${text}`
}

const insertSelectedClause = () => {
  const clause = allClauses.value.find(item => item.id === selectedClauseId.value)
  if (!clause?.content) return

  insertTextAtCursor(clause.content, true)
  ElMessage.success(`${t('template.clauseInserted')}: ${locale.value === 'en' && clause.nameEn ? clause.nameEn : clause.name}`)
  selectedClauseId.value = ''
}

const addClauseToOutline = () => {
  const clause = allClauses.value.find(item => item.id === selectedClauseId.value)
  if (!clause?.content) return

  clauseOutline.value = [...clauseOutline.value, { ...clause }]
  ElMessage.success(`${t('template.addedToOutline')}: ${locale.value === 'en' && clause.nameEn ? clause.nameEn : clause.name}`)
  selectedClauseId.value = ''
}

const moveClauseOutlineItem = (index: number, direction: -1 | 1) => {
  const targetIndex = index + direction
  if (targetIndex < 0 || targetIndex >= clauseOutline.value.length) return
  const next = [...clauseOutline.value]
  const [item] = next.splice(index, 1)
  next.splice(targetIndex, 0, item)
  clauseOutline.value = next
}

const removeClauseOutlineItem = (index: number) => {
  clauseOutline.value = clauseOutline.value.filter((_, idx) => idx !== index)
}

const clearClauseOutline = () => {
  clauseOutline.value = []
}

const insertClauseOutline = () => {
  if (clauseOutline.value.length === 0) return
  const mergedContent = clauseOutline.value
    .map(item => item.content || '')
    .filter(Boolean)
    .join('\n\n')
  if (!mergedContent) return

  insertTextAtCursor(mergedContent, true)
  ElMessage.success(t('template.outlineInserted'))
}

const saveTemplate = async (goBack: boolean) => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    const data = {
      name: form.name,
      category: form.category,
      content: form.content,
      variables: form.variablesText,
      description: form.description
    }

    if (isEdit.value) {
      await updateTemplate(templateId.value, data)
      ElMessage.success(t('common.success'))
      captureSnapshot()
      if (goBack) router.push('/templates')
    } else {
      const res = (await createTemplate(data)) as ApiResponse<Record<string, unknown>>
      ElMessage.success(t('common.success'))
      const newId = res.data?.id
      captureSnapshot()
      if (goBack) {
        router.push('/templates')
      } else if (newId != null && (typeof newId === 'number' || typeof newId === 'string')) {
        const idStr = String(newId)
        await router.replace({ name: 'TemplateEdit', params: { id: idStr } })
      } else {
        router.push('/templates')
      }
    }
  } catch (error: unknown) {
    const err = error as { message?: string }
    ElMessage.error(err.message || t('common.error'))
  } finally {
    loading.value = false
  }
}

const fetchTemplate = async () => {
  try {
    const res = await getTemplate(templateId.value)
    if (res.data) {
      form.name = res.data.name || ''
      form.category = res.data.category || ''
      form.content = res.data.content || ''
      form.description = res.data.description || ''
      
      if (res.data.variables && typeof res.data.variables === 'object') {
        form.variablesText = JSON.stringify(res.data.variables, null, 2)
      } else {
        form.variablesText = '{}'
      }
      extractVariablesFromContent(form.content || '')
    }
  } catch (error) {
    ElMessage.error(t('template.error.load'))
  }
}

const escapeRegExp = (s: string) => s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')

const handlePreview = async () => {
  if (!form.content?.trim()) {
    ElMessage.warning(t('template.previewEmpty'))
    return
  }
  let parsed: Record<string, unknown> = {}
  try {
    if (form.variablesText?.trim()) {
      parsed = JSON.parse(form.variablesText) as Record<string, unknown>
    }
  } catch {
    ElMessage.error(t('template.variablesError'))
    return
  }

  previewLoading.value = true
  previewUsedFallback.value = false
  try {
    const res = (await replaceTemplateVariables(form.content, parsed)) as ApiResponse<string>
    previewContent.value = res.data ?? ''
  } catch {
    previewUsedFallback.value = true
    let content = form.content
    Object.keys(parsed).forEach((key) => {
      const v = parsed[key]
      const str = v != null ? String(v) : ''
      const regex = new RegExp(`\\[\\[${escapeRegExp(key)}\\]\\]`, 'g')
      content = content.replace(regex, `【${str}】`)
    })
    previewContent.value = content
  } finally {
    previewLoading.value = false
    showPreview.value = true
  }
}

const bootstrap = async () => {
  loadCategories()
  loadVariables()
  loadClauses()
  if (isEdit.value) {
    await fetchTemplate()
  } else {
    applyClausePrefill()
  }
  await nextTick()
  captureSnapshot()
}

onMounted(() => {
  void bootstrap()
})
</script>

<style scoped lang="scss">
.template-form {
  .page-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 24px;
    
    .page-title {
      margin: 0;
      font-size: 24px;
      font-weight: 700;
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
    align-items: flex-start;
    gap: 10px 12px;
    margin-bottom: 12px;
    flex-wrap: wrap;
    padding: 10px 12px;
    background: var(--bg-hover);
    border-radius: 4px;
  }

  .toolbar-group {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px 10px;
    min-width: 0;
  }

  .toolbar-select {
    min-width: 0;
  }

  .toolbar-select--sm {
    width: 160px;
    max-width: 100%;
  }

  .toolbar-select--lg {
    width: 280px;
    max-width: 100%;
  }

  .toolbar-label {
    font-size: 13px;
    color: var(--text-secondary);
    font-weight: 500;
    white-space: nowrap;
  }

  .toolbar-divider {
    width: 1px;
    min-height: 28px;
    align-self: stretch;
    background: var(--border-color);
    margin: 2px 4px;
    flex-shrink: 0;
  }

  @media (max-width: 960px) {
    .toolbar-divider--responsive {
      display: none;
    }

    .toolbar-group {
      width: 100%;
      padding-bottom: 10px;
      border-bottom: 1px solid var(--border-color);
    }

    .toolbar-group:last-child {
      border-bottom: none;
      padding-bottom: 0;
    }
  }

  @media (max-width: 600px) {
    .toolbar-select--sm,
    .toolbar-select--lg {
      width: 100%;
    }
  }

  .table-insert-form {
    .el-form-item {
      margin-bottom: 12px;
    }
  }

  .clause-outline {
    margin-bottom: 12px;
    border: 1px solid var(--border-color);
    border-radius: 8px;
    background: var(--bg-card);

    .clause-outline-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      padding: 12px 14px;
      border-bottom: 1px solid var(--border-color);
      background: var(--bg-hover);
    }

    .clause-outline-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 14px;
      font-weight: 600;
      color: var(--text-primary);
    }

    .clause-outline-actions {
      display: flex;
      gap: 8px;
    }

    .clause-outline-list {
      display: flex;
      flex-direction: column;
    }

    .clause-outline-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      padding: 12px 14px;
      border-bottom: 1px solid var(--border-color);

      &:last-child {
        border-bottom: none;
      }
    }

    .clause-outline-main {
      display: flex;
      flex-direction: column;
      gap: 6px;
      min-width: 0;
    }

    .clause-outline-name {
      font-size: 14px;
      font-weight: 500;
      color: var(--text-primary);
    }

    .clause-outline-meta {
      display: flex;
      align-items: center;
      gap: 8px;
      color: var(--text-secondary);
      font-size: 12px;
    }

    .clause-outline-item-actions {
      display: flex;
      align-items: center;
      gap: 4px;
      flex-shrink: 0;
    }
  }
}

.clause-quick-use-alert {
  margin-bottom: 14px;

  .clause-quick-use-text {
    margin: 0 0 8px;
    font-size: 13px;
    line-height: 1.55;
    color: var(--el-text-color-regular);
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

.content-tip {
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.extracted-variables-form-item {
  align-items: flex-start;

  :deep(.el-form-item__label) {
    display: inline-flex;
    align-items: flex-start;
    justify-content: flex-end;
    line-height: 1.45;
  }
}

.extracted-variables-label {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  width: 100%;
  box-sizing: border-box;
}

.extracted-variables-layout {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
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

.variables-tip {
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-secondary);
}

.preview-fallback-hint {
  margin: 0 0 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.preview-content {
  background: var(--bg-hover);
  border-radius: 8px;
  padding: 12px;
  max-height: min(70vh, 640px);
  overflow-y: auto;

  pre {
    margin: 0;
    font-size: 13px;
    line-height: 1.8;
    white-space: pre-wrap;
    word-break: break-all;
  }
}

/* 与编辑器共用 Snow 主题：由 quill.snow.css 提供标题、字体、颜色、对齐、列表等；此处仅做容器与表格嵌入补强 */
.template-preview-quill {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  overflow: hidden;

  :deep(.ql-editor) {
    min-height: 120px;
    box-sizing: border-box;
    word-break: break-word;
  }

  :deep(.ql-table-embed) {
    margin: 0.75em 0;
  }

  :deep(.ql-table-embed table) {
    width: 100%;
    border-collapse: collapse;
  }

  :deep(.ql-table-embed td),
  :deep(.ql-table-embed th) {
    border: 1px solid var(--el-border-color);
    padding: 6px 8px;
  }

  :deep(.ql-editor:not(.ql-blank) table) {
    width: 100%;
    border-collapse: collapse;
  }

  :deep(.ql-editor:not(.ql-blank) td),
  :deep(.ql-editor:not(.ql-blank) th) {
    border: 1px solid var(--el-border-color);
    padding: 6px 8px;
  }

  :deep(.ql-editor img) {
    max-width: 100%;
    height: auto;
  }
}

.preview-empty {
  text-align: center;
  padding: 40px;
  color: var(--text-secondary);
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
