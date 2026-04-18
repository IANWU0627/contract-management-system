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
        <el-form-item :label="$t('template.name')" prop="name">
          <el-input v-model="form.name" :placeholder="t('template.placeholder.name')" />
        </el-form-item>
        
        <el-form-item :label="$t('template.category')" prop="category">
          <el-select v-model="form.category" :placeholder="t('common.placeholder.select')">
            <el-option v-for="cat in categories" :key="cat.code" :label="getCategoryName(cat)" :value="cat.code">
              <span class="category-dot" :style="{ background: cat.color }"></span>
              {{ getCategoryName(cat) }}
            </el-option>
          </el-select>
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
              <span class="toolbar-label">{{ $t('template.insertClause') }}:</span>
              <el-select v-model="selectedClauseCategory" :placeholder="$t('clause.category')" style="width: 160px" clearable>
                <el-option v-for="cat in clauseCategoryOptions" :key="cat" :label="cat" :value="cat" />
              </el-select>
              <el-select v-model="selectedClauseId" :placeholder="$t('template.selectClause')" style="width: 280px" clearable filterable>
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
              <div class="toolbar-divider"></div>
              <span class="toolbar-label">{{ $t('template.editorMode') }}:</span>
              <el-radio-group v-model="editorMode" size="small">
                <el-radio-button value="rich">{{ $t('template.richEditor') }}</el-radio-button>
                <el-radio-button value="code">{{ $t('template.codeEditor') }}</el-radio-button>
              </el-radio-group>
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
            <div v-show="editorMode === 'rich'" class="quill-wrapper">
              <QuillEditor 
                ref="quillEditor"
                v-model:content="form.content" 
                contentType="html"
                :toolbar="quillToolbarOptions"
                :placeholder="t('template.placeholder.templateContent')"
                theme="snow"
                class="quill-editor"
              />
            </div>
            
            <!-- Code Editor (Textarea) -->
            <div v-show="editorMode === 'code'" class="code-wrapper">
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
        
        <el-form-item :label="$t('template.variables')" prop="variables">
          <el-input 
            v-model="form.variablesText" 
            type="textarea" 
            :rows="4" 
            :placeholder="t('template.placeholder.variables')"
          />
          <div class="variables-tip">{{ $t('template.variablesTip') }}</div>
        </el-form-item>
        
        <el-form-item :label="$t('template.description')" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" :placeholder="t('template.placeholder.description')" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            {{ $t('common.save') }}
          </el-button>
          <el-button @click="$router.back()">{{ $t('common.cancel') }}</el-button>
          <el-button @click="handlePreview" :loading="previewLoading">
            {{ $t('common.preview') }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-dialog v-model="showPreview" :title="t('common.preview')" width="800px">
      <div class="preview-content" v-if="previewContent">
        <pre>{{ previewContent }}</pre>
      </div>
      <div v-else class="preview-empty">
        {{ t('template.previewEmpty') }}
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import type { FormInstance } from 'element-plus'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { ArrowLeft, Refresh } from '@element-plus/icons-vue'
import { getTemplate, createTemplate, updateTemplate } from '@/api/template'
import { getClauseList, type Clause } from '@/api/clause'
import { getTemplateVariables, type TemplateVariableItem } from '@/api/templateVariable'
import { getContractCategories } from '@/api/contractCategory'

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
const loadingVariables = ref(false)
const editorMode = ref('rich')
const quillEditor = ref<InstanceType<typeof QuillEditor> | null>(null)

const quillToolbarOptions = [
  [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
  [{ 'font': [] }],
  ['bold', 'italic', 'underline', 'strike', { 'color': [] }, { 'background': [] }],
  [{ 'script': 'sub' }, { 'script': 'super' }],
  [{ 'list': 'ordered' }, { 'list': 'bullet' }, { 'list': 'check' }],
  [{ 'indent': '-1' }, { 'indent': '+1' }],
  [{ 'direction': 'rtl' }],
  [{ 'align': [] }],
  ['link', 'image', 'video'],
  ['blockquote', 'code-block', { 'header': 1 }, { 'header': 2 }],
  ['superscript', 'subscript'],
  ['clean']
]

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

// 合同分类（用于模板分类选择）
const categoryOptions = computed(() => {
  const cats = [
    { value: 'system', label: t('variable.categories.system') },
    { value: 'party', label: t('variable.categories.party') },
    { value: 'purchase', label: t('variable.categories.purchase') },
    { value: 'service', label: t('variable.categories.service') },
    { value: 'lease', label: t('variable.categories.lease') },
    { value: 'employment', label: t('variable.categories.employment') },
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
    const res = await getTemplateVariables({ status: 1 })
    allVariables.value = res.data.list
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
  content: [{ required: true, message: t('template.error.content'), trigger: 'blur' }]
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

const insertTextAtCursor = (text: string, useHtml = false) => {
  if (editorMode.value === 'rich' && quillEditor.value) {
    const quill = quillEditor.value.getQuill()
    const range = quill.getSelection(true)
    if (useHtml) {
      quill.clipboard.dangerouslyPasteHTML(range ? range.index : quill.getLength(), text)
    } else if (range) {
      quill.insertText(range.index, text)
    } else {
      quill.insertText(quill.getLength(), text)
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

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
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
      } else {
        await createTemplate(data)
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
      form.content = res.data.content || ''
      form.description = res.data.description || ''
      
      if (res.data.variables && typeof res.data.variables === 'object') {
        form.variablesText = JSON.stringify(res.data.variables, null, 2)
      } else {
        form.variablesText = '{}'
      }
    }
  } catch (error) {
    ElMessage.error(t('template.error.load'))
  }
}

const handlePreview = () => {
  try {
    let content = form.content
    
    if (form.variablesText) {
      const vars = JSON.parse(form.variablesText)
      Object.keys(vars).forEach(key => {
        const regex = new RegExp(`\\[\\[${key}\\]\\]`, 'g')
        content = content.replace(regex, `【${vars[key]}】`)
      })
    }
    
    previewContent.value = content
    showPreview.value = true
  } catch (error) {
    ElMessage.error(t('template.variablesError'))
  }
}

onMounted(() => {
  loadCategories()
  loadVariables()
  loadClauses()
  if (isEdit.value) {
    fetchTemplate()
  } else {
    applyClausePrefill()
  }
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

.quill-wrapper {
  border: 1px solid var(--border-color);
  border-radius: 4px;
  overflow: hidden;
  width: 100%;
  
  :deep(.ql-container) {
    min-height: 500px;
    max-height: 700px;
    font-size: 14px;
    width: 100%;
  }
  
  :deep(.ql-editor) {
    min-height: 500px;
    max-height: 700px;
    width: 100%;
  }
  
  :deep(.ql-toolbar) {
    width: 100%;
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

.variables-tip {
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-secondary);
}

.preview-content {
  background: var(--bg-hover);
  border-radius: 8px;
  padding: 16px;
  max-height: 500px;
  overflow-y: auto;
  
  pre {
    margin: 0;
    font-size: 13px;
    line-height: 1.8;
    white-space: pre-wrap;
    word-break: break-all;
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
