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
      <div class="table-toolbar">
        <div class="table-summary">
          <span class="summary-item">{{ t('common.total') }} {{ filteredHeaders.length }}</span>
          <span class="summary-divider">|</span>
          <span class="summary-item">{{ t('quickCode.itemCount') }} {{ totalItemCount }}</span>
        </div>
      </div>
      <el-table :data="filteredHeaders" v-loading="loading" style="width: 100%">
        <el-table-column :label="$t('quickCode.code')" prop="code" width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.code }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="nameColumnLabel" min-width="210" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="primary-text">{{ getHeaderName(row) }}</div>
            <div class="secondary-text" v-if="getHeaderNameSub(row)">{{ getHeaderNameSub(row) }}</div>
          </template>
        </el-table-column>
        <el-table-column :label="$t('quickCode.description')" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="primary-text">{{ getHeaderDescription(row) || '-' }}</div>
            <div class="secondary-text" v-if="getHeaderDescriptionSub(row)">{{ getHeaderDescriptionSub(row) }}</div>
          </template>
        </el-table-column>
        <el-table-column :label="$t('quickCode.itemCount')" prop="itemCount" width="120" show-overflow-tooltip>
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
      width="1220px"
      class="quick-code-edit-dialog"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="98px">
        <div class="form-section">
          <div class="form-section-header" @click="basicInfoCollapsed = !basicInfoCollapsed">
            <div class="basic-info-title-wrap">
              <div class="form-section-title">{{ t('quickCode.basicInfo') }}</div>
              <span class="basic-info-hint">{{ t('quickCode.editHint') }}</span>
            </div>
            <el-icon class="collapse-icon" :class="{ collapsed: basicInfoCollapsed }"><ArrowDown /></el-icon>
          </div>
          <div v-show="!basicInfoCollapsed">
          <el-row :gutter="16">
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
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item :label="$t('quickCode.code')" prop="code" v-if="!isEdit">
                <el-input v-model="form.code" :placeholder="$t('quickCode.placeholder.code')" />
              </el-form-item>
              <el-form-item :label="$t('quickCode.code')" v-else>
                <el-input v-model="form.code" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12" />
          </el-row>
          <el-row :gutter="16">
            <el-col :span="24">
              <el-form-item :label="$t('quickCode.description')">
                <el-input v-model="form.description" type="textarea" :rows="2" :placeholder="$t('quickCode.placeholder.description')" />
              </el-form-item>
            </el-col>
          </el-row>
          </div>
        </div>

        <div class="form-section items-form-section">
          <div class="items-summary-row form-section-header" @click="itemsSectionCollapsed = !itemsSectionCollapsed">
            <div class="form-section-title">{{ $t('quickCode.items') }}</div>
            <div class="items-inline-toolbar" @click.stop>
              <span class="items-inline-stats">{{ t('quickCode.itemList') }} {{ allItems.length }}</span>
              <el-tag size="small" type="success" effect="plain">{{ $t('common.enable') }} {{ enabledItemCount }}</el-tag>
              <el-tag size="small" type="info" effect="plain">{{ $t('common.disable') }} {{ disabledItemCount }}</el-tag>
              <el-segmented
                v-model="itemStatusFilter"
                :options="itemStatusFilterOptions"
                size="small"
              />
              <el-input
                class="items-search-input"
                v-model="itemSearchKeyword"
                :placeholder="$t('quickCode.searchItems') + '...'"
                clearable
                size="small"
                @input="handleItemSearch"
              >
                <template #prefix><el-icon><Search /></el-icon></template>
              </el-input>
              <el-button type="primary" size="small" @click="handleAddItem" class="add-item-btn">
                <el-icon><Plus /></el-icon>
                {{ $t('quickCode.addItem') }}
              </el-button>
            </div>
            <el-icon class="collapse-icon" :class="{ collapsed: itemsSectionCollapsed }"><ArrowDown /></el-icon>
          </div>
          <div class="items-section" v-show="!itemsSectionCollapsed">
          <el-table
            ref="itemsTableEl"
            :data="paginatedItems"
            border
            style="width: 100%; margin-top: 12px"
            max-height="400"
            class="items-table"
            size="small"
            table-layout="fixed"
            scrollbar-always-on
          >
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
            <el-table-column :label="$t('quickCode.itemCode')" prop="code" width="130">
              <template #default="{ row }">
                <el-input v-model="row.code" size="small" @change="handleItemChange(row)" :placeholder="$t('quickCode.placeholder.code')" />
              </template>
            </el-table-column>
            <el-table-column :label="$t('quickCode.itemMeaning')" prop="meaning" width="170">
              <template #default="{ row }">
                <el-input v-model="row.meaning" size="small" @change="handleItemChange(row)" :placeholder="$t('quickCode.placeholder.meaningZh')" />
              </template>
            </el-table-column>
            <el-table-column :label="$t('quickCode.itemMeaningEn')" prop="meaningEn" width="180">
              <template #default="{ row }">
                <el-input v-model="row.meaningEn" size="small" @change="handleItemChange(row)" :placeholder="$t('quickCode.placeholder.meaningEn')" />
              </template>
            </el-table-column>
            <el-table-column :label="$t('quickCode.itemTag')" prop="tag" width="110" class-name="item-tag-col">
              <template #default="{ row }">
                <div class="cell-input-wrap">
                  <el-input v-model="row.tag" size="small" @change="handleItemChange(row)" :placeholder="$t('quickCode.itemTag')" />
                </div>
              </template>
            </el-table-column>
            <el-table-column :label="t('quickCode.itemValidFrom')" width="160" class-name="item-valid-from-col">
              <template #default="{ row }">
                <div class="date-cell">
                  <el-date-picker
                    v-model="row.validFrom"
                    type="date"
                    size="small"
                    :placeholder="$t('quickCode.from')"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    @change="handleItemChange(row)"
                  />
                </div>
              </template>
            </el-table-column>
            <el-table-column :label="t('quickCode.itemValidTo')" width="160" class-name="item-valid-to-col">
              <template #default="{ row }">
                <div class="date-cell">
                  <el-date-picker
                    v-model="row.validTo"
                    type="date"
                    size="small"
                    :placeholder="$t('quickCode.to')"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    @change="handleItemChange(row)"
                  />
                </div>
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
          
          <div class="items-pagination" v-if="filteredItems.length > 0">
            <el-pagination
              v-model:current-page="itemPage"
              v-model:page-size="itemPageSize"
              :total="filteredItems.length"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleItemSizeChange"
              @current-change="handleItemPageChange"
            />
          </div>
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
  getQuickCodeImpact,
  createQuickCode, 
  updateQuickCode, 
  deleteQuickCode,
  toggleQuickCode 
} from '@/api/quickCode'
import { Plus, Edit, Delete, Check, Close, Search, Refresh, DocumentCopy, Download, Upload, Top, Bottom } from '@element-plus/icons-vue'

const { t, locale } = useI18n()

const loading = ref(false)
const searchKeyword = ref('')
const searchStatus = ref<number | null>(null)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const basicInfoCollapsed = ref(false)
const itemsSectionCollapsed = ref(false)
const headers = ref<any[]>([])
const total = ref(0)
const formRef = ref()
const itemsTableEl = ref<any>(null)
let sortableInstance: Sortable | null = null
let dragRelatedRow: HTMLElement | null = null

// Items pagination
const allItems = ref<any[]>([])
const itemSearchKeyword = ref('')
const itemStatusFilter = ref<'all' | 'enabled'>('all')
const itemPage = ref(1)
const itemPageSize = ref(10)

const itemStatusFilterOptions = computed(() => [
  { label: t('common.all'), value: 'all' },
  { label: t('common.enable'), value: 'enabled' }
])

const filteredItems = computed(() => {
  let result = allItems.value
  if (itemStatusFilter.value === 'enabled') {
    result = result.filter(item => item.enabled !== false)
  }
  if (!itemSearchKeyword.value) return result
  const keyword = itemSearchKeyword.value.toLowerCase()
  return result.filter(item =>
    item.code?.toLowerCase().includes(keyword) ||
    item.meaning?.toLowerCase().includes(keyword)
  )
})

const enabledItemCount = computed(() => allItems.value.filter(item => item.enabled !== false).length)
const disabledItemCount = computed(() => allItems.value.length - enabledItemCount.value)

const paginatedItems = computed(() => {
  const start = (itemPage.value - 1) * itemPageSize.value
  return filteredItems.value.slice(start, start + itemPageSize.value)
})

const syncItemPageWithinBounds = () => {
  const maxPage = Math.max(1, Math.ceil(filteredItems.value.length / itemPageSize.value))
  if (itemPage.value > maxPage) {
    itemPage.value = maxPage
  } else if (itemPage.value < 1) {
    itemPage.value = 1
  }
}

const handleItemSearch = () => {
  syncItemPageWithinBounds()
}

const handleItemSizeChange = () => {
  syncItemPageWithinBounds()
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

watch([filteredItems, itemPageSize], () => {
  syncItemPageWithinBounds()
})

const filteredHeaders = computed(() => {
  let result = headers.value
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(h =>
      h.name?.toLowerCase().includes(keyword) ||
      h.nameEn?.toLowerCase().includes(keyword) ||
      h.code?.toLowerCase().includes(keyword) ||
      h.description?.toLowerCase().includes(keyword) ||
      h.descriptionEn?.toLowerCase().includes(keyword)
    )
  }
  if (searchStatus.value !== null) {
    result = result.filter(h => h.status === searchStatus.value)
  }
  return result
})

const totalItemCount = computed(() => {
  return filteredHeaders.value.reduce((sum, item) => sum + (item.itemCount || 0), 0)
})

const nameColumnLabel = computed(() => {
  return locale.value === 'en' ? t('quickCode.nameEn') : t('quickCode.name')
})

const getHeaderName = (row: any) => {
  return locale.value === 'en' ? (row.nameEn || row.name || '-') : (row.name || row.nameEn || '-')
}

const getHeaderNameSub = (row: any) => {
  return locale.value === 'en' ? row.name : row.nameEn
}

const getHeaderDescription = (row: any) => {
  return locale.value === 'en' ? (row.descriptionEn || row.description || '') : (row.description || row.descriptionEn || '')
}

const getHeaderDescriptionSub = (row: any) => {
  return locale.value === 'en' ? row.description : row.descriptionEn
}

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
    itemStatusFilter.value = 'all'
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
  itemStatusFilter.value = 'all'
  itemPage.value = 1
  basicInfoCollapsed.value = false
  itemsSectionCollapsed.value = false
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
  itemStatusFilter.value = 'all'
  itemPage.value = 1
  basicInfoCollapsed.value = false
  itemsSectionCollapsed.value = false
  
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
    const impactRes = await getQuickCodeImpact(row.id)
    const impact = impactRes.data
    if (impact?.inUse) {
      ElMessage.warning(formatImpactBlockMessage(impact))
      return
    }

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
    if (row.status === 1) {
      const impactRes = await getQuickCodeImpact(row.id)
      const impact = impactRes.data
      if (impact?.inUse) {
        ElMessage.warning(formatImpactBlockMessage(impact))
        return
      }
    }
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
  itemStatusFilter.value = 'all'
  basicInfoCollapsed.value = false
  itemsSectionCollapsed.value = false
}

const formatImpactBlockMessage = (impact: any) => {
  const typeText = Array.isArray(impact?.referencedContractTypes) && impact.referencedContractTypes.length > 0
    ? impact.referencedContractTypes.join(', ')
    : '-'
  return t('quickCode.error.inUseBlocked', {
    code: impact?.code || '',
    fieldCount: impact?.referencedFieldCount || 0,
    typeCount: impact?.referencedContractTypeCount || 0,
    types: typeText
  })
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

    .table-toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      border-bottom: 1px solid #f0f0f0;
      background: var(--bg-card);
    }

    .table-summary {
      display: flex;
      align-items: center;
      gap: 8px;
      color: var(--text-secondary);
      font-size: 13px;

      .summary-item {
        font-weight: 500;
      }

      .summary-divider {
        opacity: 0.5;
      }
    }
    
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

    .primary-text {
      color: var(--text-primary);
      font-weight: 500;
      line-height: 1.3;
    }

    .secondary-text {
      margin-top: 2px;
      color: var(--text-secondary);
      font-size: 12px;
      line-height: 1.3;
    }
    
    .pagination-wrap {
      display: flex;
      justify-content: flex-end;
      padding: 16px 24px;
      border-top: 1px solid #f0f0f0;
    }
  }
}

:deep(.quick-code-edit-dialog .el-dialog__body) {
  padding-top: 6px;
  padding-bottom: 6px;
  max-height: calc(100vh - 170px);
  overflow: auto;
}

:deep(.quick-code-edit-dialog .el-dialog__footer) {
  border-top: 1px solid var(--border-color);
  padding-top: 8px;
  padding-bottom: 8px;
  position: sticky;
  bottom: 0;
  background: var(--bg-card);
  z-index: 2;
}

:deep(.quick-code-edit-dialog .el-dialog) {
  margin-top: 6vh !important;
  margin-bottom: 6vh !important;
}

.items-section {
  background: var(--bg-color);
  padding: 6px 10px;
  border-radius: 8px;
  max-height: min(50vh, 520px);
  overflow: auto;
  
  .items-pagination {
    display: flex;
    justify-content: flex-end;
    padding-top: 4px;
    margin-top: 6px;
    border-top: 1px solid var(--border-color);
    position: static;
    background: transparent;
  }

  .date-cell {
    width: 100%;
    display: block;

    :deep(.el-date-editor.el-input) {
      width: 100%;
      display: block;
      margin: 0;
    }

    :deep(.el-input__wrapper) {
      box-sizing: border-box;
      min-height: 30px;
      padding: 4px 10px !important;
      align-items: center;
    }
  }

  .cell-input-wrap {
    width: 100%;
    display: block;

    :deep(.el-input) {
      width: 100%;
      display: block;
      margin: 0;
    }

    :deep(.el-input__wrapper) {
      box-sizing: border-box;
      min-height: 30px;
      padding: 4px 10px;
      align-items: center;
    }
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
    :deep(td.item-tag-col),
    :deep(td.item-valid-from-col),
    :deep(td.item-valid-to-col) {
      vertical-align: top;
    }

    :deep(td.item-tag-col .cell),
    :deep(td.item-valid-from-col .cell),
    :deep(td.item-valid-to-col .cell) {
      display: block;
      padding: 5px 10px;
    }

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
      padding: 5px 0;
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

@media (max-height: 860px) {
  .items-section {
    max-height: 42vh;
  }
}

.form-section {
  padding: 8px 10px 4px;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  background: var(--bg-color);
  margin-bottom: 8px;

  .form-section-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 0;
  }
}

.items-form-section {
  margin-bottom: 0;
}

.items-summary-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  flex-wrap: wrap;
}

.basic-info-title-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.basic-info-hint {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.items-inline-toolbar {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: auto;
  flex: 1;
  min-width: 460px;
}

.items-inline-stats {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.items-inline-toolbar .items-search-input {
  min-width: 240px;
  max-width: 420px;
  flex: 1;
}

.form-section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  cursor: pointer;
}

.collapse-icon {
  margin-left: auto;
  transition: transform 0.2s ease;
  color: var(--text-secondary);

  &.collapsed {
    transform: rotate(-90deg);
  }
}

</style>
