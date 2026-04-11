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
            @input="loadVariables"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <div class="filter-item" style="width: 160px;">
          <el-select v-model="filterCategory" :placeholder="t('variable.category')" clearable @change="loadVariables">
            <el-option v-for="cat in categories" :key="cat.value" :label="cat.label" :value="cat.value" />
          </el-select>
        </div>
        <div class="filter-item" style="width: 120px;">
          <el-select v-model="filterType" :placeholder="t('variable.type')" clearable @change="loadVariables">
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
        <el-table :data="paginatedVariables" v-loading="loading" stripe>
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
          :label="关联快速代码" 
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { Plus, Edit, Delete, Search, Check, Close, Refresh, ArrowDown } from '@element-plus/icons-vue'
import {
  getTemplateVariables,
  createTemplateVariable,
  updateTemplateVariable,
  deleteTemplateVariable,
  getVariableCategories,
  initDefaultVariables
} from '@/api/templateVariable'
import { getQuickCodes, getQuickCodeByCode } from '@/api/quickCode'

const { t, locale } = useI18n()

const loading = ref(false)
const quickCodes = ref<any[]>([])
const submitLoading = ref(false)
const initLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const variables = ref<any[]>([])
const categories = ref<any[]>([])
const variableCategoryQuickCodeCode = 'TEMPLATE_VARIABLE_CATEGORY'

const filterCategory = ref('')
const filterType = ref('')
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)

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

const filteredVariables = computed(() => {
  return variables.value.filter(v => {
    if (filterCategory.value && v.category !== filterCategory.value) return false
    if (filterType.value && v.type !== filterType.value) return false
    if (searchKeyword.value) {
      const kw = searchKeyword.value.toLowerCase()
      return v.code.toLowerCase().includes(kw) || 
             v.name.toLowerCase().includes(kw) || 
             v.label.toLowerCase().includes(kw)
    }
    return true
  })
})

const paginatedVariables = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredVariables.value.slice(start, end)
})

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
    custom: 'info'
  }
  return types[category] || ''
}

const loadQuickCodes = async () => {
  try {
    const res = await getQuickCodes()
    quickCodes.value = res.data?.filter((qc: any) => qc.status === 1) || []
    
    // 加载模板变量分类的快码配置
    try {
      const catRes = await getQuickCodeByCode(variableCategoryQuickCodeCode)
      if (catRes.data) {
        categories.value = catRes.data
          .map((item: any) => ({
            value: item.code,
            label: locale.value === 'en' && item.meaningEn ? item.meaningEn : (item.meaning || item.code)
          }))
      }
    } catch (catErr) {
      console.warn('Failed to load variable categories, using defaults', catErr)
      // 如果加载失败，使用默认分类
      categories.value = [
        { value: 'system', label: locale.value === 'en' ? 'System' : '系统变量' },
        { value: 'party', label: locale.value === 'en' ? 'Counterparty' : '相对方信息' },
        { value: 'purchase', label: locale.value === 'en' ? 'Purchase' : '采购合同' },
        { value: 'service', label: locale.value === 'en' ? 'Service' : '服务合同' },
        { value: 'lease', label: locale.value === 'en' ? 'Lease' : '租赁合同' },
        { value: 'employment', label: locale.value === 'en' ? 'Employment' : '劳动合同' },
        { value: 'custom', label: locale.value === 'en' ? 'Custom' : '自定义变量' }
      ]
    }
  } catch (error) {
    console.error('Failed to load quick codes:', error)
  }
}

const loadVariables = async () => {
  loading.value = true
  try {
    const res = await getTemplateVariables({ status: 1 })
    variables.value = res.data || []
    total.value = filteredVariables.value.length
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

const handleEdit = (row: any) => {
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
        category: form.value.category,
        defaultValue: form.value.defaultValue,
        required: form.value.required ? 1 : 0,
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
    } catch (error: any) {
      ElMessage.error(error.message || t('common.error'))
    } finally {
      submitLoading.value = false
    }
  })
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      t('variable.deleteConfirm', { name: row.name }),
      t('common.warning'),
      { type: 'warning' }
    )
    await deleteTemplateVariable(row.id)
    ElMessage.success(t('common.success'))
    loadVariables()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || t('common.error'))
    }
  }
}

const handleAction = (command: string, row: any) => {
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
  searchKeyword.value = ''
  filterCategory.value = ''
  filterType.value = ''
  loadVariables()
}

const handleInitDefaults = async () => {
  initLoading.value = true
  try {
    await initDefaultVariables()
    ElMessage.success(t('variable.initSuccess'))
    loadVariables()
  } catch (error: any) {
    ElMessage.error(error.message || t('common.error'))
  } finally {
    initLoading.value = false
  }
}

onMounted(() => {
  loadVariables()
  loadQuickCodes()
})

watch(filteredVariables, () => {
  total.value = filteredVariables.value.length
  currentPage.value = 1
})

// 监听语言切换，重新加载分类
watch(locale, () => {
  loadQuickCodes()
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
  
  .var-code {
    background: var(--bg-hover);
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 12px;
    color: var(--primary);
  }
}
</style>
