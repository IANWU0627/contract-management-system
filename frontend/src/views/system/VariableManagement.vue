<template>
  <div class="variable-management">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">{{ t('variable.title') }}</h1>
        <span class="variable-count">{{ t('variable.total') }}: {{ total }}</span>
      </div>
      <div class="header-actions">
        <el-button @click="handleInitDefaults" :loading="initLoading">
          <el-icon><Refresh /></el-icon>
          {{ t('variable.initDefaults') }}
        </el-button>
        <el-button @click="openBatchImportDialog">
          <el-icon><Plus /></el-icon>
          {{ t('variable.batchImport') }}
        </el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          {{ t('common.add') }}
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <div class="filter-section">
      <div class="filter-row">
        <div class="filter-item filter-item-wide" style="flex: 1;">
          <el-input
            v-model="searchKeyword"
            :placeholder="t('variable.searchPlaceholder')"
            clearable
            @input="handleSearchInput"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <div class="filter-item" style="width: 160px;">
          <el-select v-model="filterCategory" :placeholder="t('variable.category')" clearable @change="handleFilterChange">
            <el-option v-for="cat in categories" :key="cat.value" :label="cat.label" :value="cat.value" />
          </el-select>
        </div>
        <div class="filter-item" style="width: 120px;">
          <el-select v-model="filterType" :placeholder="t('variable.type')" clearable @change="handleFilterChange">
            <el-option label="text" value="text" />
            <el-option label="number" value="number" />
            <el-option label="date" value="date" />
            <el-option label="textarea" value="textarea" />
          </el-select>
        </div>
        <div class="filter-actions">
          <el-button type="primary" @click="loadVariables" class="gradient-btn">
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

    <el-card shadow="hover">
        <el-table :data="variables" v-loading="loading" stripe>
        <el-table-column prop="code" :label="t('variable.code')" width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <code class="var-code">{{ row.code }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="name" :label="t('variable.name')" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">
            {{ locale === 'en' && row.nameEn ? row.nameEn : row.name }}
          </template>
        </el-table-column>
        <el-table-column prop="nameEn" :label="t('variable.nameEn')" min-width="120" show-overflow-tooltip />
        <el-table-column prop="label" :label="t('variable.label')" min-width="120" show-overflow-tooltip />
        <el-table-column prop="category" :label="t('variable.category')" width="120" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag size="small" :type="getCategoryTagType(row.category)">
              {{ getCategoryLabel(row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="type" :label="t('variable.type')" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag size="small">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="required" :label="t('variable.required')" width="80" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            <el-icon v-if="row.required" color="#67c23a"><Check /></el-icon>
            <el-icon v-else color="#909399"><Close /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="description" :label="t('variable.description')" min-width="140" show-overflow-tooltip />
        <el-table-column :label="t('common.action')" width="100" fixed="right">
          <template #default="{ row }">
            <el-dropdown @command="(command) => handleAction(command, row)">
              <el-button size="small">
                {{ t('common.action') }}
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">{{ t('common.edit') }}</el-dropdown-item>
                  <el-dropdown-item command="delete" divided type="danger">{{ t('common.delete') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
        </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handlePageSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="t('variable.code')" prop="code">
          <el-input v-model="form.code" :placeholder="t('variable.codePlaceholder')" :disabled="isEdit" />
        </el-form-item>
        <el-form-item :label="t('variable.name')" prop="name">
          <el-input v-model="form.name" :placeholder="t('variable.namePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('variable.nameEn')">
          <el-input v-model="form.nameEn" :placeholder="t('variable.nameEnPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('variable.label')" prop="label">
          <el-input v-model="form.label" :placeholder="t('variable.labelPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('variable.category')" prop="category">
          <el-select v-model="form.category" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.value" :label="cat.label" :value="cat.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('variable.type')" prop="type">
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="Text (文本)" value="text" />
            <el-option label="Number (数字)" value="number" />
            <el-option label="Date (日期)" value="date" />
            <el-option label="Textarea (多行文本)" value="textarea" />
            <el-option label="Select (单选下拉)" value="select" />
            <el-option label="Multiselect (多选下拉)" value="multiselect" />
          </el-select>
        </el-form-item>
        <el-form-item 
          v-if="form.type === 'select' || form.type === 'multiselect'" 
          :label="'关联快速代码'" 
          prop="quickCodeCode"
        >
          <el-select v-model="form.quickCodeCode" style="width: 100%" clearable placeholder="请选择快速代码">
            <el-option 
              v-for="qc in quickCodes" 
              :key="qc.code" 
              :label="qc.name + ' (' + qc.code + ')'" 
              :value="qc.code" 
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('variable.defaultValue')">
          <el-input v-model="form.defaultValue" :placeholder="t('variable.defaultValuePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('variable.required')">
          <el-switch v-model="form.required" />
        </el-form-item>
        <el-form-item :label="t('variable.description')">
          <el-input v-model="form.description" type="textarea" :rows="2" :placeholder="t('variable.descriptionPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchImportVisible" :title="t('variable.batchImportTitle')" width="760px">
      <el-form label-width="120px">
        <el-form-item :label="t('variable.batchImportMode')">
          <el-segmented v-model="batchImportConflictPolicy" :options="batchImportModeOptions" />
        </el-form-item>
        <el-form-item>
          <el-button text type="primary" @click="fillBatchImportTemplate">
            {{ t('variable.batchImportFillTemplate') }}
          </el-button>
        </el-form-item>
        <el-form-item :label="t('variable.batchImportPayload')">
          <el-input
            v-model="batchImportText"
            type="textarea"
            :rows="14"
            :placeholder="t('variable.batchImportPlaceholder')"
          />
        </el-form-item>
      </el-form>
      <div v-if="batchImportResult" class="batch-import-result">
        <el-tag type="success">{{ t('variable.batchImportCreated') }}: {{ batchImportResult.created?.length || 0 }}</el-tag>
        <el-tag type="warning">{{ t('variable.batchImportUpdated') }}: {{ batchImportResult.updated?.length || 0 }}</el-tag>
        <el-tag type="info">{{ t('variable.batchImportSkipped') }}: {{ batchImportResult.skipped?.length || 0 }}</el-tag>
        <el-tag type="danger">{{ t('variable.batchImportFailed') }}: {{ batchImportResult.failed?.length || 0 }}</el-tag>
      </div>
      <template #footer>
        <el-button @click="batchImportVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="batchImportLoading" @click="handleBatchImportSubmit">
          {{ t('common.confirm') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { Plus, Edit, Delete, Search, Check, Close, Refresh, ArrowDown } from '@element-plus/icons-vue'
import {
  getTemplateVariables,
  createTemplateVariable,
  updateTemplateVariable,
  deleteTemplateVariable,
  getTemplateVariableImpact,
  batchCreateTemplateVariables,
  getVariableCategories,
  initDefaultVariables,
  type TemplateVariableItem
} from '@/api/templateVariable'
import { getQuickCodes } from '@/api/quickCode'

interface QuickCodeItem {
  code: string
  name: string
  status?: number
}

interface VariableCategoryItem {
  value: string
  label: string
}

interface BatchImportResult {
  created?: Array<{ id?: number; code?: string }>
  updated?: Array<{ id?: number; code?: string }>
  skipped?: Array<{ code?: string; reason?: string }>
  failed?: Array<{ code?: string; reason?: string }>
}

const { t, locale } = useI18n()

const loading = ref(false)
const quickCodes = ref<QuickCodeItem[]>([])
const submitLoading = ref(false)
const initLoading = ref(false)
const dialogVisible = ref(false)
const batchImportVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const variables = ref<TemplateVariableItem[]>([])
const categories = ref<VariableCategoryItem[]>([])

const filterCategory = ref('')
const filterType = ref('')
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const batchImportLoading = ref(false)
const batchImportConflictPolicy = ref<'skip' | 'overwrite'>('skip')
const batchImportText = ref('')
const batchImportResult = ref<BatchImportResult | null>(null)
let searchDebounceTimer: ReturnType<typeof setTimeout> | null = null

const batchImportModeOptions = computed(() => [
  { label: t('variable.batchImportModeSkip'), value: 'skip' },
  { label: t('variable.batchImportModeOverwrite'), value: 'overwrite' }
])

const form = ref({
  code: '',
  name: '',
  nameEn: '',
  label: '',
  type: 'text',
  quickCodeCode: '',
  category: '',
  defaultValue: '',
  required: false,
  description: ''
})

const rules = {
  code: [{ required: true, message: t('variable.error.codeRequired'), trigger: 'blur' }],
  name: [{ required: true, message: t('variable.error.nameRequired'), trigger: 'blur' }]
}

const dialogTitle = computed(() => isEdit.value ? t('common.edit') : t('common.add'))

const getCategoryLabel = (category: string) => {
  const cat = categories.value.find(c => c.value === category)
  if (cat) return cat.label
  
  // 如果找不到，尝试从国际化文件中查找
  const i18nKey = `variable.categories.${category}`
  const translated = t(i18nKey)
  if (translated !== i18nKey) return translated
  
  return category
}

const getCategoryTagType = (category: string) => {
  const types: Record<string, string> = {
    system: 'primary',
    party: 'success',
    purchase: 'warning',
    service: 'info',
    lease: 'danger',
    employment: 'warning',
    agency: 'info',
    custom: 'info'
  }
  return types[category] || ''
}

const loadQuickCodes = async () => {
  try {
    const res = await getQuickCodes()
    quickCodes.value = (res.data as QuickCodeItem[] | undefined)?.filter((qc) => qc.status === 1) || []
  } catch (error) {
    console.error('Failed to load quick codes:', error)
    quickCodes.value = []
  }
}

const loadCategories = async () => {
  const res = await getVariableCategories()
  const backendCategories = res.data
  categories.value = backendCategories.map((item) => {
    const value = item.value || item.label
    const i18nKey = `variable.categories.${value}`
    const translated = t(i18nKey)
    return {
      value,
      label: translated !== i18nKey ? translated : value
    }
  })
}

const loadVariables = async () => {
  loading.value = true
  try {
    const res = await getTemplateVariables({
      status: 1,
      page: currentPage.value,
      pageSize: pageSize.value,
      category: filterCategory.value || undefined,
      type: filterType.value || undefined,
      keyword: searchKeyword.value?.trim() || undefined
    })
    const payload = res.data
    variables.value = payload.list
    total.value = payload.total
    currentPage.value = payload.page
    pageSize.value = payload.pageSize
  } catch (error) {
    ElMessage.error(t('common.error'))
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  // 使用第一个分类作为默认值
  const defaultCategory = categories.value.length > 0 ? categories.value[0].value : ''
  form.value = {
    code: '',
    name: '',
    nameEn: '',
    label: '',
    type: 'text',
    quickCodeCode: '',
    category: defaultCategory,
    defaultValue: '',
    required: false,
    description: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row: TemplateVariableItem) => {
  isEdit.value = true
  form.value = {
    code: row.code,
    name: row.name,
    nameEn: row.nameEn || '',
    label: row.label || row.name,
    type: row.type || 'text',
    quickCodeCode: row.quickCodeCode || '',
    category: row.category || 'custom',
    defaultValue: row.defaultValue || '',
    required: !!row.required,
    description: row.description || ''
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      const data = {
        code: form.value.code,
        name: form.value.name,
        nameEn: form.value.nameEn,
        label: form.value.label || form.value.name,
        type: form.value.type,
        quickCodeCode: form.value.quickCodeCode || null,
        category: form.value.category,
        defaultValue: form.value.defaultValue,
        required: form.value.required,
        description: form.value.description
      }
      
      if (isEdit.value) {
        const variable = variables.value.find(v => v.code === form.value.code)
        if (variable) {
          await updateTemplateVariable(variable.id, data)
        }
      } else {
        await createTemplateVariable(data)
      }
      
      ElMessage.success(t('common.success'))
      dialogVisible.value = false
      loadVariables()
    } catch (error: unknown) {
      const err = error as { message?: string }
      ElMessage.error(err.message || t('common.error'))
    } finally {
      submitLoading.value = false
    }
  })
}

const handleDelete = async (row: TemplateVariableItem) => {
  try {
    const impactRes = await getTemplateVariableImpact(row.id)
    const impact = impactRes.data || {}
    const templateCount = Number(impact.templateCount || 0)
    if (templateCount > 0) {
      ElMessage.warning(t('error.templateVariable.inUseCannotDelete', {
        code: row.code,
        templateCount
      }))
      return
    }
    await ElMessageBox.confirm(
      t('variable.deleteConfirm', { name: row.name }),
      t('common.warning'),
      { type: 'warning' }
    )
    await deleteTemplateVariable(row.id)
    ElMessage.success(t('common.success'))
    loadVariables()
  } catch (error: unknown) {
    const err = error as { message?: string }
    if (error !== 'cancel') {
      ElMessage.error(err.message || t('common.error'))
    }
  }
}

const handleAction = (command: string, row: TemplateVariableItem) => {
  switch (command) {
    case 'edit':
      handleEdit(row)
      break
    case 'delete':
      handleDelete(row)
      break
  }
}

const handleReset = () => {
  if (searchDebounceTimer) {
    clearTimeout(searchDebounceTimer)
    searchDebounceTimer = null
  }
  searchKeyword.value = ''
  filterCategory.value = ''
  filterType.value = ''
  currentPage.value = 1
  loadVariables()
}

const handleSearchInput = () => {
  if (searchDebounceTimer) {
    clearTimeout(searchDebounceTimer)
  }
  searchDebounceTimer = setTimeout(() => {
    if (currentPage.value !== 1) {
      currentPage.value = 1
      return
    }
    loadVariables()
  }, 300)
}

const handleFilterChange = () => {
  if (currentPage.value !== 1) {
    currentPage.value = 1
    return
  }
  loadVariables()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadVariables()
}

const handlePageSizeChange = (size: number) => {
  pageSize.value = size
  if (currentPage.value !== 1) {
    currentPage.value = 1
  }
  loadVariables()
}

const handleInitDefaults = async () => {
  initLoading.value = true
  try {
    await initDefaultVariables()
    ElMessage.success(t('variable.initSuccess'))
    loadVariables()
  } catch (error: unknown) {
    const err = error as { message?: string }
    ElMessage.error(err.message || t('common.error'))
  } finally {
    initLoading.value = false
  }
}

const openBatchImportDialog = () => {
  batchImportVisible.value = true
  batchImportConflictPolicy.value = 'skip'
  batchImportResult.value = null
  if (!batchImportText.value.trim()) {
    batchImportText.value = JSON.stringify(
      [
        {
          code: 'demoField',
          name: '示例字段',
          nameEn: 'Demo Field',
          label: '示例字段',
          type: 'text',
          category: 'custom',
          required: false,
          description: 'batch import example'
        }
      ],
      null,
      2
    )
  }
}

const handleBatchImportSubmit = async () => {
  const raw = batchImportText.value.trim()
  if (!raw) {
    ElMessage.warning(t('variable.batchImportEmpty'))
    return
  }
  let parsedItems: Record<string, unknown>[] = []
  try {
    if (raw.startsWith('[')) {
      const parsed = JSON.parse(raw)
      if (!Array.isArray(parsed)) {
        ElMessage.warning(t('variable.batchImportInvalid'))
        return
      }
      parsedItems = parsed
    } else {
      // Auto-detect JSON Lines input: one JSON object per line.
      const lines = raw
        .split(/\r?\n/)
        .map(line => line.trim())
        .filter(Boolean)
      parsedItems = lines.map(line => JSON.parse(line))
    }
  } catch (_err) {
    ElMessage.error(t('variable.batchImportInvalid'))
    return
  }

  batchImportLoading.value = true
  try {
    const res = await batchCreateTemplateVariables({
      items: parsedItems,
      conflictPolicy: batchImportConflictPolicy.value
    })
    batchImportResult.value = res.data || null
    ElMessage.success(t('common.success'))
    currentPage.value = 1
    await loadVariables()
  } catch (error: unknown) {
    const err = error as { message?: string }
    ElMessage.error(err.message || t('common.error'))
  } finally {
    batchImportLoading.value = false
  }
}

const fillBatchImportTemplate = () => {
  batchImportText.value = JSON.stringify(
    [
      {
        code: 'paymentTerm',
        name: '付款条件',
        nameEn: 'Payment Term',
        label: '付款条件',
        type: 'text',
        category: 'custom',
        defaultValue: '',
        required: false,
        description: '用于标记合同中的付款条件'
      },
      {
        code: 'warrantyMonths',
        name: '质保期(月)',
        nameEn: 'Warranty Months',
        label: '质保期(月)',
        type: 'number',
        category: 'custom',
        defaultValue: '12',
        required: false,
        description: '默认质保月份'
      }
    ],
    null,
    2
  )
}

onMounted(() => {
  loadVariables()
  loadQuickCodes()
  loadCategories()
})

onBeforeUnmount(() => {
  if (searchDebounceTimer) {
    clearTimeout(searchDebounceTimer)
    searchDebounceTimer = null
  }
})

// 监听语言切换，重新加载分类
watch(locale, () => {
  loadCategories()
})
</script>

<style scoped lang="scss">
.variable-management {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .header-left {
      display: flex;
      align-items: baseline;
      gap: 12px;
      
      .page-title {
        margin: 0;
        font-size: 24px;
        font-weight: 700;
        background: var(--primary-gradient);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }
      
      .variable-count {
        color: var(--text-secondary);
        font-size: 14px;
      }
    }
    
    .header-actions {
      display: flex;
      gap: 8px;
    }
  }
  
  .gradient-btn {
    background: var(--primary-gradient) !important;
    border: none !important;
  }
  
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
  
  .pagination-wrap {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }

  .batch-import-result {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    margin-top: 8px;
  }
  
  .var-code {
    background: var(--bg-hover);
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 12px;
    color: var(--primary);
  }
}
</style>
