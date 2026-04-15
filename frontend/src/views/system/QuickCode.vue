<template>
  <div class="quick-code-page">
    <div class="page-header">
      <h1 class="page-title">{{ $t('quickCode.management') }}</h1>
      <el-button type="primary" class="gradient-btn" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        {{ $t('quickCode.add') }}
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
        <div class="filter-item">
          <el-select v-model="searchStatus" :placeholder="$t('common.status')" clearable style="width: 100px" @change="handleSearch">
            <el-option :label="$t('common.enable')" :value="1" />
            <el-option :label="$t('common.disable')" :value="0" />
          </el-select>
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
      <div class="filter-row filter-row-secondary">
        <div class="filter-actions-secondary">
          <el-button @click="handleExport" size="small">
            <el-icon><Download /></el-icon>
            {{ t('common.export') }}
          </el-button>
          <el-upload
            :show-file-list="false"
            :before-upload="handleImport"
            accept=".json"
          >
            <el-button size="small">
              <el-icon><Upload /></el-icon>
              {{ t('common.upload') }}
            </el-button>
          </el-upload>
        </div>
      </div>
    </div>
    
    <div class="table-section">
      <el-table :data="filteredHeaders" v-loading="loading" style="width: 100%">
        <el-table-column :label="$t('quickCode.name')" prop="name" min-width="150" show-overflow-tooltip />
        <el-table-column :label="$t('quickCode.code')" prop="code" width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.code }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('quickCode.description')" prop="description" min-width="200" show-overflow-tooltip />
        <el-table-column :label="$t('quickCode.itemCount')" prop="itemCount" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag size="small" type="warning">{{ row.itemCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('quickCode.status')" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? $t('common.enable') : $t('common.disable') }}
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
                  <el-dropdown-item command="edit">
                    <el-icon><Edit /></el-icon>
                    <span style="margin-left: 5px">{{ $t('common.edit') }}</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="copy">
                    <el-icon><DocumentCopy /></el-icon>
                    <span style="margin-left: 5px">{{ $t('quickCode.copy') }}</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="toggle">
                    <el-icon><Check /></el-icon>
                    <span style="margin-left: 5px">{{ row.status === 1 ? $t('common.disable') : $t('common.enable') }}</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided type="danger">
                    <el-icon><Delete /></el-icon>
                    <span style="margin-left: 5px">{{ $t('common.delete') }}</span>
                  </el-dropdown-item>
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
      :title="isEdit ? $t('quickCode.edit') : $t('quickCode.add')" 
      width="1120px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('quickCode.name')" prop="name">
              <el-input v-model="form.name" :placeholder="$t('quickCode.placeholder.name')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('quickCode.nameEn')" prop="nameEn">
              <el-input v-model="form.nameEn" :placeholder="$t('quickCode.placeholder.nameEn')" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="$t('quickCode.code')" prop="code" v-if="!isEdit">
          <el-input v-model="form.code" :placeholder="$t('quickCode.placeholder.code')" />
        </el-form-item>
        <el-form-item :label="$t('quickCode.code')" v-else>
          <el-input v-model="form.code" disabled />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('quickCode.description')">
              <el-input v-model="form.description" type="textarea" :rows="2" :placeholder="$t('quickCode.placeholder.description')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('quickCode.descriptionEn')">
              <el-input v-model="form.descriptionEn" type="textarea" :rows="2" :placeholder="$t('quickCode.placeholder.descriptionEn')" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 选项管理 -->
        <el-divider content-position="left">{{ $t('quickCode.items') }}</el-divider>
        
        <div class="items-section">
          <div class="items-header">
            <span class="items-title">{{ $t('quickCode.itemList') }} ({{ allItems.length }})</span>
            <div class="items-actions">
              <el-input
                v-model="itemSearchKeyword"
                :placeholder="$t('quickCode.searchItems') + '...'"
                clearable
                size="small"
                style="width: 180px"
                @input="handleItemSearch"
              >
                <template #prefix><el-icon><Search /></el-icon></template>
              </el-input>
              <el-button type="primary" size="small" @click="handleAddItem" class="add-item-btn">
                <el-icon><Plus /></el-icon>
                {{ $t('quickCode.addItem') }}
              </el-button>
            </div>
          </div>
          
          <el-table ref="itemsTableEl" :data="paginatedItems" border style="width: 100%; margin-top: 12px" max-height="400" class="items-table" size="small">
            <el-table-column :label="$t('quickCode.sortOrder')" prop="sortOrder" width="64" align="center">
              <template #default="{ row }">
                <span class="sort-index">{{ (row.sortOrder ?? 0) + 1 }}</span>
              </template>
            </el-table-column>
            <el-table-column width="40" align="center">
              <template #default>
                <span class="drag-handle" :title="$t('quickCode.sortOrder')">⋮⋮</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('quickCode.itemCode')" prop="code" min-width="120">
              <template #default="{ row }">
                <el-input v-model="row.code" size="small" @change="handleItemChange(row)" :placeholder="$t('quickCode.placeholder.code')" />
              </template>
            </el-table-column>
            <el-table-column :label="$t('quickCode.itemMeaning')" prop="meaning" min-width="160">
              <template #default="{ row }">
                <el-input v-model="row.meaning" size="small" @change="handleItemChange(row)" :placeholder="$t('quickCode.placeholder.meaningZh')" />
              </template>
            </el-table-column>
            <el-table-column :label="$t('quickCode.itemMeaningEn')" prop="meaningEn" min-width="180">
              <template #default="{ row }">
                <el-input v-model="row.meaningEn" size="small" @change="handleItemChange(row)" :placeholder="$t('quickCode.placeholder.meaningEn')" />
              </template>
            </el-table-column>
            <el-table-column :label="$t('quickCode.itemTag')" prop="tag" min-width="90">
              <template #default="{ row }">
                <el-input v-model="row.tag" size="small" @change="handleItemChange(row)" :placeholder="$t('quickCode.itemTag')" />
              </template>
            </el-table-column>
            <el-table-column :label="t('quickCode.itemValidFrom')" min-width="132">
              <template #default="{ row }">
                <el-date-picker
                  v-model="row.validFrom"
                  type="date"
                  size="small"
                  :placeholder="$t('quickCode.from')"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  @change="handleItemChange(row)"
                />
              </template>
            </el-table-column>
            <el-table-column :label="t('quickCode.itemValidTo')" min-width="132">
              <template #default="{ row }">
                <el-date-picker
                  v-model="row.validTo"
                  type="date"
                  size="small"
                  :placeholder="$t('quickCode.to')"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  @change="handleItemChange(row)"
                />
              </template>
            </el-table-column>
            <el-table-column :label="$t('common.status')" width="72" align="center">
              <template #default="{ row }">
                <el-switch v-model="row.enabled" size="small" @change="handleItemChange(row)" />
              </template>
            </el-table-column>
            <el-table-column :label="$t('common.action')" width="96" fixed="right" align="center">
              <template #default="{ row }">
                <div class="row-actions">
                  <el-tooltip :content="$t('quickCode.moveUp')" placement="top">
                    <el-button
                      type="primary"
                      size="small"
                      text
                      :disabled="allItems.findIndex(item => item === row) === 0"
                      @click="handleMoveItemDirect(row, -1)"
                    >
                      <el-icon><Top /></el-icon>
                    </el-button>
                  </el-tooltip>
                  <el-tooltip :content="$t('quickCode.moveDown')" placement="top">
                    <el-button
                      type="primary"
                      size="small"
                      text
                      :disabled="allItems.findIndex(item => item === row) === allItems.length - 1"
                      @click="handleMoveItemDirect(row, 1)"
                    >
                      <el-icon><Bottom /></el-icon>
                    </el-button>
                  </el-tooltip>
                  <el-tooltip :content="$t('common.delete')" placement="top">
                    <el-button type="danger" size="small" text @click="handleRemoveItemDirect(allItems.findIndex(item => item === row))">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </el-tooltip>
                </div>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="items-pagination" v-if="allItems.length > itemPageSize">
            <el-pagination
              v-model:current-page="itemPage"
              v-model:page-size="itemPageSize"
              :total="allItems.length"
              :page-sizes="[5, 10, 20]"
              layout="total, prev, pager, next"
              @size-change="handleItemSizeChange"
              @current-change="handleItemPageChange"
            />
          </div>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick, onBeforeUnmount, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import Sortable from 'sortablejs'
import { 
  getAllQuickCodes, 
  getQuickCode,
  createQuickCode, 
  updateQuickCode, 
  deleteQuickCode,
  toggleQuickCode 
} from '@/api/quickCode'
import { Plus, Edit, Delete, Check, Close, Search, Refresh, DocumentCopy, Download, Upload, Top, Bottom } from '@element-plus/icons-vue'

const { t } = useI18n()

const loading = ref(false)
const searchKeyword = ref('')
const searchStatus = ref<number | null>(null)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const headers = ref<any[]>([])
const total = ref(0)
const formRef = ref()
const itemsTableEl = ref<any>(null)
let sortableInstance: Sortable | null = null
let dragRelatedRow: HTMLElement | null = null

// Items pagination
const allItems = ref<any[]>([])
const itemSearchKeyword = ref('')
const itemPage = ref(1)
const itemPageSize = ref(5)

const filteredItems = computed(() => {
  if (!itemSearchKeyword.value) return allItems.value
  const keyword = itemSearchKeyword.value.toLowerCase()
  return allItems.value.filter(item =>
    item.code?.toLowerCase().includes(keyword) ||
    item.meaning?.toLowerCase().includes(keyword)
  )
})

const paginatedItems = computed(() => {
  const start = (itemPage.value - 1) * itemPageSize.value
  return filteredItems.value.slice(start, start + itemPageSize.value)
})

const handleItemSearch = () => {
  itemPage.value = 1
}

const handleItemSizeChange = () => {
  itemPage.value = 1
  nextTick(initRowDrag)
}

const handleItemPageChange = () => {
  nextTick(initRowDrag)
}

const handleItemChange = (row: any) => {
  const index = allItems.value.findIndex(i => i === row)
  if (index !== -1) {
    allItems.value[index] = { ...row }
  }
}

watch(dialogVisible, (visible) => {
  if (visible) {
    nextTick(initRowDrag)
  } else {
    sortableInstance?.destroy()
    sortableInstance = null
  }
})

watch(paginatedItems, () => {
  if (dialogVisible.value) {
    nextTick(initRowDrag)
  }
})

const filteredHeaders = computed(() => {
  let result = headers.value
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(h =>
      h.name?.toLowerCase().includes(keyword) ||
      h.code?.toLowerCase().includes(keyword) ||
      h.description?.toLowerCase().includes(keyword)
    )
  }
  if (searchStatus.value !== null) {
    result = result.filter(h => h.status === searchStatus.value)
  }
  return result
})

const handleSearch = () => {
  query.page = 1
}

const handleReset = () => {
  searchKeyword.value = ''
  searchStatus.value = null
  query.page = 1
}

const handleCopy = async (row: any) => {
  try {
    const res = await getQuickCode(row.id)
    isEdit.value = false
    form.id = null
    form.name = row.name + '_copy'
    form.nameEn = row.nameEn ? row.nameEn + '_copy' : ''
    form.code = row.code + '_COPY'
    form.description = row.description || ''
    form.descriptionEn = row.descriptionEn || ''
    allItems.value = (res.data?.items || []).map((item: any) => ({
      id: null,
      code: item.code,
      meaning: item.meaning,
      meaningEn: item.meaningEn || '',
      description: item.description || '',
      descriptionEn: item.descriptionEn || '',
      tag: item.tag || '',
      validFrom: item.validFrom || null,
      validTo: item.validTo || null,
      enabled: item.enabled !== undefined ? item.enabled : true,
      sortOrder: item.sortOrder || 0,
      _isNew: true
    }))
    itemSearchKeyword.value = ''
    itemPage.value = 1
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const handleExport = () => {
  const data = filteredHeaders.value.map(header => ({
    name: header.name,
    nameEn: header.nameEn,
    code: header.code,
    description: header.description,
    descriptionEn: header.descriptionEn,
    items: allItems.value.length > 0 ? allItems.value : []
  }))
  
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'quick-codes.json'
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success(t('common.exportSuccess'))
}

const handleImport = (file: File) => {
  const reader = new FileReader()
  reader.onload = async (e) => {
    try {
      const data = JSON.parse(e.target?.result as string)
      if (!Array.isArray(data)) {
        ElMessage.error(t('common.importFailed'))
        return
      }
      
      for (const item of data) {
        if (!item.name || !item.code) continue
        try {
          await createQuickCode(item)
        } catch (err) {
          console.error('Failed to import item:', err)
        }
      }
      
      ElMessage.success(t('common.importSuccess'))
      fetchData()
    } catch (err) {
      ElMessage.error(t('common.importFailed'))
    }
  }
  reader.readAsText(file)
  return false
}

const query = reactive({
  page: 1,
  pageSize: 10
})

const form = reactive({
  id: null as number | null,
  name: '',
  nameEn: '',
  code: '',
  description: '',
  descriptionEn: '',
})

const rules = {
  name: [{ required: true, message: t('quickCode.error.name'), trigger: 'blur' }],
  code: [{ required: true, message: t('quickCode.error.code'), trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAllQuickCodes(query.page, query.pageSize)
    headers.value = res.data?.list || []
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
  fetchData()
}

const handlePageChange = (val: number) => {
  query.page = val
  fetchData()
}

const handleCreate = () => {
  isEdit.value = false
  form.id = null
  form.name = ''
  form.nameEn = ''
  form.code = ''
  form.description = ''
  form.descriptionEn = ''
  allItems.value = []
  itemSearchKeyword.value = ''
  itemPage.value = 1
  dialogVisible.value = true
}

const handleEdit = async (row: any) => {
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.nameEn = row.nameEn || ''
  form.code = row.code
  form.description = row.description || ''
  form.descriptionEn = row.descriptionEn || ''
  allItems.value = []
  itemSearchKeyword.value = ''
  itemPage.value = 1
  
  try {
    const res = await getQuickCode(row.id)
    allItems.value = res.data?.items || []
  } catch (error) {
    console.error('Failed to load quick code items:', error)
  }
  
  dialogVisible.value = true
}

const handleAddItem = () => {
  allItems.value.unshift({
    id: null,
    code: '',
    meaning: '',
    meaningEn: '',
    description: '',
    descriptionEn: '',
    tag: '',
    validFrom: null,
    validTo: null,
    enabled: true,
    sortOrder: allItems.value.length,
    _isNew: true
  })
  itemPage.value = 1
  nextTick(() => {
    const tableRoot = itemsTableEl.value?.$el || itemsTableEl.value
    const firstInput = tableRoot?.querySelector('.el-table__body-wrapper tbody tr:first-child td:nth-child(3) input') as HTMLInputElement | null
    firstInput?.focus()
  })
}

const handleMoveItemDirect = (row: any, direction: number) => {
  const realIndex = allItems.value.findIndex(item => item === row)
  const newIndex = realIndex + direction
  if (newIndex < 0 || newIndex >= allItems.value.length) return
  
  const temp = allItems.value[realIndex]
  allItems.value[realIndex] = allItems.value[newIndex]
  allItems.value[newIndex] = temp
  
  allItems.value.forEach((item, i) => {
    item.sortOrder = i
  })
}

const handleRemoveItemDirect = (realIndex: number) => {
  if (realIndex < 0 || realIndex >= allItems.value.length) return
  allItems.value.splice(realIndex, 1)
  allItems.value.forEach((item, i) => {
    item.sortOrder = i
  })
}

const initRowDrag = () => {
  if (!itemsTableEl.value) return
  const tableRoot = itemsTableEl.value.$el || itemsTableEl.value
  const tbody = tableRoot.querySelector('.el-table__body-wrapper tbody') as HTMLElement | null
  if (!tbody) return
  sortableInstance?.destroy()
  sortableInstance = Sortable.create(tbody, {
    handle: '.drag-handle',
    animation: 150,
    ghostClass: 'sortable-ghost-row',
    chosenClass: 'sortable-chosen-row',
    dragClass: 'sortable-drag-row',
    onMove(evt) {
      if (dragRelatedRow && dragRelatedRow !== evt.related) {
        dragRelatedRow.classList.remove('drop-target-row')
      }
      if (evt.related) {
        dragRelatedRow = evt.related as HTMLElement
        dragRelatedRow.classList.add('drop-target-row')
      }
      return true
    },
    onEnd(evt) {
      if (dragRelatedRow) {
        dragRelatedRow.classList.remove('drop-target-row')
        dragRelatedRow = null
      }
      const from = evt.oldIndex ?? -1
      const to = evt.newIndex ?? -1
      if (from === -1 || to === -1 || from === to) return
      const pageOffset = (itemPage.value - 1) * itemPageSize.value
      const fromIndex = pageOffset + from
      const toIndex = pageOffset + to
      const moved = allItems.value.splice(fromIndex, 1)[0]
      allItems.value.splice(toIndex, 0, moved)
      allItems.value.forEach((item, i) => {
        item.sortOrder = i
      })
    }
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    // 验证选项
    const codes = new Set<string>()
    for (const item of allItems.value) {
      if (!item.code || !item.meaning) {
        ElMessage.warning(t('quickCode.error.itemRequired'))
        return
      }
      if (codes.has(item.code)) {
        ElMessage.warning(t('quickCode.error.duplicateItemCode', { code: item.code }))
        return
      }
      codes.add(item.code)
    }
    
    submitting.value = true
    try {
      const data: any = {
        name: form.name,
        nameEn: form.nameEn,
        description: form.description,
        descriptionEn: form.descriptionEn,
        items: allItems.value.map((item, index) => ({
          id: item.id,
          code: item.code,
          meaning: item.meaning,
          meaningEn: item.meaningEn || '',
          description: item.description || '',
          descriptionEn: item.descriptionEn || '',
          tag: item.tag || '',
          validFrom: item.validFrom || null,
          validTo: item.validTo || null,
          enabled: item.enabled,
          sortOrder: item.sortOrder !== undefined ? item.sortOrder : index
        }))
      }
      
      if (isEdit.value) {
        await updateQuickCode(form.id!, data)
        ElMessage.success(t('common.success'))
      } else {
        data.code = form.code
        await createQuickCode(data)
        ElMessage.success(t('common.success'))
      }
      dialogVisible.value = false
      fetchData()
    } catch (error: any) {
      ElMessage.error(error.message || t('common.error'))
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(t('quickCode.confirmDelete'), t('common.warning'), { type: 'warning' })
    await deleteQuickCode(row.id)
    ElMessage.success(t('common.success'))
    fetchData()
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
    case 'copy':
      handleCopy(row)
      break
    case 'toggle':
      handleToggle(row)
      break
    case 'delete':
      handleDelete(row)
      break
  }
}

const handleToggle = async (row: any) => {
  try {
    await toggleQuickCode(row.id)
    ElMessage.success(t('common.success'))
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || t('common.error'))
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
  allItems.value = []
}

onMounted(() => {
  fetchData()
})

onBeforeUnmount(() => {
  if (dragRelatedRow) {
    dragRelatedRow.classList.remove('drop-target-row')
    dragRelatedRow = null
  }
  sortableInstance?.destroy()
  sortableInstance = null
})
</script>

<style scoped lang="scss">
.quick-code-page {
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
  
  .filter-row-secondary {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid var(--border-color);
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
  
  .filter-actions-secondary {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
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
      --el-table-cell-padding: 16px 12px;
      
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
}

.items-section {
  background: var(--bg-color);
  padding: 16px;
  border-radius: 8px;
  
  .items-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 12px;
    font-weight: 500;
    color: var(--text-primary);
    
    .items-title {
      font-weight: 600;
      font-size: 14px;
      color: var(--text-primary);
    }
    
    .items-actions {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }
  
  .items-pagination {
    display: flex;
    justify-content: flex-end;
    padding-top: 8px;
    margin-top: 12px;
    border-top: 1px solid var(--border-color);
  }
  
  .date-picker-row {
    display: flex;
    align-items: center;
    gap: 4px;
    
    :deep(.el-date-editor) {
      width: 100%;
    }
  }
  
  .date-separator {
    color: var(--text-secondary);
    font-size: 14px;
  }
  
  .sort-buttons {
    display: flex;
    align-items: center;
    gap: 2px;
    justify-content: center;
    
    .el-button {
      padding: 2px;
      height: 24px;
      width: 24px;
      min-width: 24px;
    }
    
    .move-btn {
      &:not(:disabled):hover {
        background-color: var(--el-color-primary-light-9);
        color: var(--el-color-primary);
      }
    }
  }
  
  .items-table {
    :deep(tr.sortable-ghost-row > td) {
      background: rgba(99, 102, 241, 0.12) !important;
    }

    :deep(tr.sortable-chosen-row > td) {
      background: rgba(99, 102, 241, 0.16) !important;
    }

    :deep(tr.drop-target-row > td) {
      background: rgba(16, 185, 129, 0.12) !important;
      box-shadow: inset 0 1px 0 rgba(16, 185, 129, 0.45), inset 0 -1px 0 rgba(16, 185, 129, 0.45);
    }

    .sort-index {
      font-weight: 600;
      color: var(--text-secondary);
      font-size: 12px;
    }

    .drag-handle {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      cursor: grab;
      color: var(--text-secondary);
      font-size: 14px;
      letter-spacing: -1px;
      user-select: none;

      &:active {
        cursor: grabbing;
      }
    }

    :deep(.el-table__row) {
      transition: background-color 0.2s ease;
      
      &:hover {
        background-color: var(--bg-hover);
      }
    }
    
    :deep(.el-input__wrapper) {
      box-shadow: none !important;
      padding: 4px 10px;
      min-height: 30px;
      
      &:hover {
        box-shadow: 0 0 0 1px var(--el-color-primary) inset !important;
      }
      
      &.is-focus {
        box-shadow: 0 0 0 1px var(--el-color-primary) inset !important;
      }
    }
    
    :deep(.el-table__cell) {
      padding: 7px 0;
    }

    :deep(.el-date-editor.el-input) {
      width: 100%;
    }
    
    :deep(.el-switch) {
      --el-switch-on-color: var(--el-color-primary);
    }

    .row-actions {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 2px;

      .el-button {
        padding: 4px;
        min-width: 24px;
        height: 24px;
      }
    }
  }
}
</style>
