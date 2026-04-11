<template>
  <div class="contract-category-page">
    <div class="page-header">
      <h1 class="page-title">{{ $t('contract.categoryManagement') }}</h1>
      <el-button type="primary" class="gradient-btn" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        {{ $t('contract.addCategory') }}
      </el-button>
    </div>
    
    <div class="filter-section">
      <div class="filter-row">
        <div class="filter-item filter-item-wide" style="flex: 1;">
          <el-input
            v-model="searchKeyword"
            :placeholder="$t('common.search') + '...'"
            clearable
            @input="handleSearch"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <div class="filter-actions">
          <el-button type="primary" @click="handleSearch" class="gradient-btn">
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
    
    <div class="table-section">
      <el-table :data="filteredCategories" v-loading="loading" style="width: 100%">
        <el-table-column :label="$t('contract.categoryName')" min-width="150">
          <template #default="{ row }">
            <div class="category-cell">
              <span class="category-dot" :style="{ background: row.color || '#409eff' }"></span>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="$t('contract.categoryNameEn')" prop="nameEn" min-width="150" />
        <el-table-column :label="$t('contract.categoryCode')" prop="code" width="150" />
        <el-table-column :label="$t('contract.categoryColor')" width="100">
          <template #default="{ row }">
            <div 
              class="color-dot"
              :style="{ background: row.color || '#409eff' }"
              :title="$t('common.clickToEdit')"
              @click="!isSystemCategory(row.code) && openColorPicker(row)"
            ></div>
          </template>
        </el-table-column>
        <el-table-column :label="$t('contract.templateCount')" prop="templateCount" width="120">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.templateCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('contract.contractCount')" prop="contractCount" width="120">
          <template #default="{ row }">
            <el-tag size="small" type="warning">{{ row.contractCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('contract.sortOrder')" prop="sortOrder" width="100" />
        <el-table-column :label="$t('contract.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.active ? 'success' : 'info'" size="small">
              {{ row.active ? $t('common.enable') : $t('common.disable') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.action')" width="100" fixed="right">
          <template #default="{ row }">
            <el-dropdown @command="(command) => handleAction(command, row)">
              <el-button size="small">
                {{ $t('common.action') }}
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit" :disabled="isSystemCategory(row.code)">{{ $t('common.edit') }}</el-dropdown-item>
                  <el-dropdown-item command="toggleStatus">
                    {{ row.active ? $t('common.disable') : $t('common.enable') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" v-if="!isSystemCategory(row.code)" divided type="danger">{{ $t('common.delete') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-wrap" v-if="total > 0">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>
    
    <!-- 创建/编辑弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? $t('contract.editCategory') : $t('contract.addCategory')" 
      width="500px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="$t('contract.categoryName')" prop="name">
          <el-input v-model="form.name" :placeholder="t('contract.placeholder.categoryName')" />
        </el-form-item>
        <el-form-item :label="$t('contract.categoryNameEn')" prop="nameEn">
          <el-input v-model="form.nameEn" :placeholder="t('contract.placeholder.categoryNameEn')" />
        </el-form-item>
        <el-form-item :label="$t('contract.categoryCode')" prop="code" v-if="!isEdit">
          <el-input v-model="form.code" :placeholder="t('contract.placeholder.categoryCode')" />
        </el-form-item>
        <el-form-item :label="$t('contract.categoryCode')" v-else>
          <el-input v-model="form.code" disabled />
        </el-form-item>
        <el-form-item :label="$t('contract.categoryColor')">
          <div class="color-picker">
            <div
              v-for="color in colorOptions"
              :key="color"
              class="color-option"
              :class="{ active: form.color === color }"
              :style="{ background: color }"
              @click="form.color = color"
            >
              <el-icon v-if="form.color === color"><Check /></el-icon>
            </div>
          </div>
        </el-form-item>
        <el-form-item :label="$t('contract.sortOrder')">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <!-- 颜色选择弹窗 -->
    <el-dialog v-model="colorPickerVisible" :title="$t('contract.categoryColor')" width="320px">
      <div class="color-picker-modal">
        <div
          v-for="color in colorOptions"
          :key="color"
          class="color-option"
          :class="{ active: selectedColor === color }"
          :style="{ background: color }"
          @click="selectedColor = color"
        >
          <el-icon v-if="selectedColor === color"><Check /></el-icon>
        </div>
      </div>
      <template #footer>
        <el-button @click="colorPickerVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="confirmColorChange">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllContractCategories, createContractCategory, updateContractCategory, deleteContractCategory } from '@/api/contractCategory'
import { Plus, Edit, Delete, Check, Close, Search, Refresh, ArrowDown } from '@element-plus/icons-vue'

const { t } = useI18n()

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const categories = ref<any[]>([])
const total = ref(0)
const formRef = ref()
const colorPickerVisible = ref(false)
const selectedColor = ref('#409eff')
const editingColorCategoryId = ref<number | null>(null)

const colorOptions = [
  '#667eea', '#764ba2', '#f093fb', '#f5576c',
  '#11998e', '#38ef7d', '#ff6b6b', '#ee5a24',
  '#fbbf24', '#f59e0b', '#3b82f6', '#10b981'
]

const query = reactive({
  page: 1,
  pageSize: 10
})

const searchKeyword = ref('')

const filteredCategories = computed(() => {
  if (!searchKeyword.value) {
    return categories.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return categories.value.filter(cat => 
    cat.name?.toLowerCase().includes(keyword) ||
    cat.code?.toLowerCase().includes(keyword) ||
    String(cat.templateCount || 0).includes(keyword) ||
    String(cat.contractCount || 0).includes(keyword)
  )
})

const handleSearch = () => {
  query.page = 1
}

const handleReset = () => {
  searchKeyword.value = ''
  query.page = 1
}

const SYSTEM_CODES = ['PURCHASE', 'SALES', 'SERVICE', 'LEASE', 'EMPLOYMENT', 'OTHER']

const form = reactive({
  id: null as number | null,
  name: '',
  nameEn: '',
  code: '',
  color: '#409eff',
  sortOrder: 0
})

const rules = {
  name: [{ required: true, message: t('contract.error.categoryName'), trigger: 'blur' }],
  nameEn: [{ required: true, message: t('contract.error.categoryNameEn'), trigger: 'blur' }],
  code: [{ required: true, message: t('contract.error.categoryCode'), trigger: 'blur' }]
}

const isSystemCategory = (code: string) => {
  return SYSTEM_CODES.includes(code)
}

const fetchCategories = async () => {
  loading.value = true
  try {
    const res = await getAllContractCategories(query.page, query.pageSize)
    categories.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error(t('common.error'))
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val: number) => {
  query.pageSize = val
  query.page = 1
  fetchCategories()
}

const handlePageChange = (val: number) => {
  query.page = val
  fetchCategories()
}

const handleCreate = () => {
  isEdit.value = false
  form.id = null
  form.name = ''
  form.nameEn = ''
  form.code = ''
  form.color = '#409eff'
  form.sortOrder = categories.value.length
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.nameEn = row.nameEn || ''
  form.code = row.code
  form.color = row.color || '#409eff'
  form.sortOrder = row.sortOrder || 0
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEdit.value) {
        await updateContractCategory(form.id!, {
          name: form.name,
          nameEn: form.nameEn,
          color: form.color,
          sortOrder: form.sortOrder
        })
        ElMessage.success(t('common.success'))
      } else {
        await createContractCategory({
          name: form.name,
          nameEn: form.nameEn,
          code: form.code,
          color: form.color,
          sortOrder: form.sortOrder
        })
        ElMessage.success(t('common.success'))
      }
      dialogVisible.value = false
      fetchCategories()
    } catch (error: any) {
      ElMessage.error(error.message || t('common.error'))
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      t('contract.confirmDeleteCategory'),
      t('common.warning'),
      { type: 'warning' }
    )
    await deleteContractCategory(row.id)
    ElMessage.success(t('common.success'))
    fetchCategories()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(t('common.error'))
    }
  }
}

const handleToggleStatus = async (row: any) => {
  try {
    await updateContractCategory(row.id, {
      active: !row.active
    })
    ElMessage.success(t('common.success'))
    fetchCategories()
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const openColorPicker = (row: any) => {
  editingColorCategoryId.value = row.id
  selectedColor.value = row.color || '#409eff'
  colorPickerVisible.value = true
}

const handleAction = (command: string, row: any) => {
  switch (command) {
    case 'edit':
      handleEdit(row)
      break
    case 'toggleStatus':
      handleToggleStatus(row)
      break
    case 'delete':
      handleDelete(row)
      break
  }
}

const confirmColorChange = async () => {
  if (!editingColorCategoryId.value) return
  try {
    await updateContractCategory(editingColorCategoryId.value, {
      color: selectedColor.value
    })
    ElMessage.success(t('common.success'))
    colorPickerVisible.value = false
    fetchCategories()
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped lang="scss">
.contract-category-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .page-title {
      font-size: 24px;
      font-weight: 600;
      margin: 0;
      background: var(--primary-gradient);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
  }
}

.table-section {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  overflow: hidden;
  
  .el-table {
    --el-table-border-color: #f0f0f0;
    --el-table-header-bg-color: #fafafa;
    --el-table-header-text-color: #1a1a1a;
    --el-table-row-hover-bg-color: #f9f9f9;
    --el-table-cell-padding: 14px 12px;
    
    th {
      font-weight: 600;
      font-size: 13px;
    }
    
    td {
      font-size: 14px;
      color: #595959;
    }
  }
  
  .pagination-wrap {
    display: flex;
    justify-content: flex-end;
    padding: 16px 24px;
    border-top: 1px solid #f0f0f0;
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

.category-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .category-dot {
    width: 12px;
    height: 12px;
    border-radius: 50%;
    flex-shrink: 0;
  }
}

.color-dot {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  cursor: pointer;
  transition: transform 0.2s;
  
  &:hover {
    transform: scale(1.1);
  }
}

.color-picker {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  
  .color-option {
    width: 32px;
    height: 32px;
    border-radius: 6px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;
    border: 2px solid transparent;
    
    &:hover {
      transform: scale(1.1);
    }
    
    &.active {
      border-color: var(--text-primary);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
    }
    
    .el-icon {
      color: white;
      font-weight: bold;
    }
  }
}

.color-picker-modal {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 16px;
  
  .color-option {
    width: 40px;
    height: 40px;
    border-radius: 8px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;
    border: 2px solid transparent;
    
    &:hover {
      transform: scale(1.1);
    }
    
    &.active {
      border-color: var(--text-primary);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
    }
    
    .el-icon {
      color: white;
      font-weight: bold;
      font-size: 18px;
    }
  }
}
</style>
