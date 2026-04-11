<template>
  <div class="template-detail">
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
        
        <el-form-item :label="$t('template.extractedVariables')" v-if="extractedVariables.length > 0">
          <div class="extracted-variables">
            <el-tag v-for="v in extractedVariables" :key="v" size="small" type="info" class="var-tag">
              [[{{ v }}]]
            </el-tag>
            <span class="var-tip">{{ $t('template.extractedVariablesTip') }}</span>
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
import { createTemplate, updateTemplate, getTemplate } from '@/api/template'
import { getTemplateVariables } from '@/api/templateVariable'
import { getContractCategories } from '@/api/contractCategory'
import { ArrowLeft, Refresh, Grid } from '@element-plus/icons-vue'

const { t } = useI18n()

const route = useRoute()
const router = useRouter()

const formRef = ref<FormInstance>()
const loading = ref(false)
const loadingVariables = ref(false)
const contentTextarea = ref<HTMLTextAreaElement>()
const quillEditor = ref<any>(null)
const categories = ref<any[]>([])

const editorMode = ref('rich')

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

const allVariables = ref<any[]>([])
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
    const res = await getTemplateVariables({ status: 1 })
    allVariables.value = res.data || []
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
  let tableHtml = '<table style="border-collapse: collapse; width: 100%; margin: 16px 0;">'
  
  for (let i = 0; i < rows; i++) {
    tableHtml += '<tr>'
    for (let j = 0; j < cols; j++) {
      const tag = i === 0 ? 'th' : 'td'
      const style = i === 0 ? 'border: 1px solid #333; padding: 8px; background: #f5f5f5; font-weight: bold; text-align: center;' : 'border: 1px solid #333; padding: 8px;'
      tableHtml += `<${tag} style="${style}">${i === 0 ? `列${j + 1}` : ''}</${tag}>`
    }
    tableHtml += '</tr>'
  }
  tableHtml += '</table><p><br></p>'
  
  if (editorMode.value === 'rich' && quillEditor.value) {
    const quill = quillEditor.value.getQuill()
    const range = quill.getSelection()
    const index = range ? range.index : quill.getLength() - 1
    
    quill.root.innerHTML = quill.root.innerHTML.slice(0, index) + tableHtml + quill.root.innerHTML.slice(index)
    quill.focus()
    quill.setSelection(index + tableHtml.length, 0)
    
    form.content = quill.root.innerHTML
  } else {
    form.content += tableHtml
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
    } catch (error: any) {
      ElMessage.error(error.message || t('common.error'))
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

.extracted-variables {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  
  .var-tag {
    font-family: 'Monaco', 'Menlo', monospace;
  }
  
  .var-tip {
    font-size: 12px;
    color: var(--text-secondary);
    margin-left: 8px;
  }
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
