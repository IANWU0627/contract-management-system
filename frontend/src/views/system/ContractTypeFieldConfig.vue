<template>
  <div class="type-field-config-page">
    <div class="page-header">
      <h1 class="page-title">{{ $t('typeFieldConfig.title') }}</h1>
      <div class="header-actions">
        <el-button @click="handleInitializeDefaults" class="init-btn">
          <el-icon><Setting /></el-icon>
          {{ $t('typeFieldConfig.initializeDefaults') }}
        </el-button>
        <el-button @click="handleExportAll" class="export-btn">
          <el-icon><Download /></el-icon>
          {{ $t('typeFieldConfig.exportConfig') }}
        </el-button>
        <el-button @click="handleImport" class="import-btn">
          <el-icon><Upload /></el-icon>
          {{ $t('typeFieldConfig.importConfig') }}
        </el-button>
        <el-button type="primary" class="gradient-btn" @click="handleSelectType">
          <el-icon><Plus /></el-icon>
          {{ $t('typeFieldConfig.addType') }}
        </el-button>
      </div>
      <input ref="fileInputRef" type="file" accept=".json" style="display: none" @change="handleFileChange" />
    </div>
    
    <div class="config-content" v-loading="loading">
      <div class="type-selector" v-if="!currentType">
        <el-card class="type-grid">
          <div class="section-title">{{ $t('typeFieldConfig.selectType') }}</div>
          <div class="type-list">
            <div 
              v-for="type in contractTypes" 
              :key="type.value"
              class="type-card"
              @click="selectType(type.value)"
            >
              <div class="type-icon" :style="{ background: type.color }">
                {{ type.label.charAt(0) }}
              </div>
              <div class="type-info">
                <div class="type-name">{{ type.label }}</div>
                <div class="type-count">{{ getFieldCount(type.value) }} {{ $t('typeFieldConfig.fields') }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </div>
      
      <div class="field-config" v-else>
        <div class="config-header">
          <el-button text @click="currentType = null">
            <el-icon><ArrowLeft /></el-icon>
            {{ $t('common.back') }}
          </el-button>
          <h2 class="config-title">
            <span class="type-badge" :style="{ background: getTypeColor(currentType) }">
              {{ getTypeLabel(currentType) }}
            </span>
            {{ $t('typeFieldConfig.fieldConfig') }}
          </h2>
          <div class="field-count">
            <el-tag type="warning" effect="plain" style="margin-right: 8px;">
              {{ $t('typeFieldConfig.draftMode') }} v{{ draftPublishVersion }}
            </el-tag>
            <el-tag v-if="hasLocalChanges" type="danger" effect="plain" style="margin-right: 8px;">
              {{ $t('typeFieldConfig.unsavedChanges') }}
            </el-tag>
            <el-tag type="info">{{ totalFields }} {{ $t('typeFieldConfig.fields') }}</el-tag>
            <el-button size="small" type="primary" style="margin-left: 8px;" :disabled="!hasLocalChanges" @click="handleSaveDraft">
              {{ $t('typeFieldConfig.saveDraft') }}
            </el-button>
            <el-button size="small" type="success" style="margin-left: 8px;" @click="handlePublishDraft">
              {{ $t('typeFieldConfig.publishDraft') }}
            </el-button>
            <el-button size="small" @click="handleDiscardDraft">
              {{ $t('typeFieldConfig.discardDraft') }}
            </el-button>
          </div>
        </div>
        
        <!-- 搜索区域 -->
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
          <!-- 添加字段按钮 -->
          <div class="add-row">
            <div class="add-buttons">
              <el-button type="primary" size="small" @click="openAddDialog">
                <el-icon><Plus /></el-icon>
                {{ $t('typeFieldConfig.addField') }}
              </el-button>
              <el-button size="small" @click="showPresetFields = !showPresetFields">
                <el-icon><Collection /></el-icon>
                {{ $t('typeFieldConfig.presetFields') }}
              </el-button>
              <el-button size="small" :disabled="selectedFieldIds.length === 0" @click="handleBatchSetRequired(true)">
                {{ $t('typeFieldConfig.batchSetRequired') }}
              </el-button>
              <el-button size="small" :disabled="selectedFieldIds.length === 0" @click="handleBatchSetShowInList(true)">
                {{ $t('typeFieldConfig.batchShowInList') }}
              </el-button>
              <el-button size="small" :disabled="selectedFieldIds.length === 0" @click="handleBatchSetShowInForm(true)">
                {{ $t('typeFieldConfig.batchShowInForm') }}
              </el-button>
              <el-button size="small" type="danger" :disabled="selectedFieldIds.length === 0" @click="handleBatchDelete">
                {{ $t('typeFieldConfig.batchDelete') }}
              </el-button>
            </div>
          </div>
          
          <!-- 字段配置表格 -->
          <el-table ref="fieldsTableRef" :data="paginatedFields" style="width: 100%" row-key="id" v-loading="loading" stripe @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="44" />
            <el-table-column :label="$t('typeFieldConfig.dragSort')" width="64" align="center">
              <template #default>
                <span class="drag-handle">⋮⋮</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('typeFieldConfig.fieldKey')" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">
                <code class="field-key-code">{{ row.fieldKey }}</code>
              </template>
            </el-table-column>
            <el-table-column :label="$t('typeFieldConfig.fieldLabel')" min-width="150" show-overflow-tooltip>
              <template #default="{ row }">
                {{ locale === 'en' && row.fieldLabelEn ? row.fieldLabelEn : row.fieldLabel }}
              </template>
            </el-table-column>
            <el-table-column :label="$t('typeFieldConfig.fieldLabelEn')" min-width="150" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.fieldLabelEn || '-' }}
              </template>
            </el-table-column>
            <el-table-column :label="$t('typeFieldConfig.fieldType')" min-width="120">
              <template #default="{ row }">
                <el-tag size="small">{{ getTypeLabel(row.fieldType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="$t('typeFieldConfig.quickCode')" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">
                <template v-if="row.fieldType === 'select' && row.quickCodeId">
                  <el-tag size="small" type="info">
                    {{ getQuickCodeDisplayName(row.quickCodeId) }}
                  </el-tag>
                </template>
                <span v-else-if="row.fieldType === 'select'" class="text-muted">-</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('typeFieldConfig.required')" min-width="80" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.required" type="success" size="small">✓</el-tag>
                <el-tag v-else type="info" size="small">-</el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="$t('typeFieldConfig.showInList')" min-width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.showInList" type="success" size="small">✓</el-tag>
                <el-tag v-else type="info" size="small">-</el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="$t('typeFieldConfig.showInForm')" min-width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.showInForm" type="success" size="small">✓</el-tag>
                <el-tag v-else type="info" size="small">-</el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="$t('typeFieldConfig.order')" width="120" fixed="right">
              <template #default="{ $index, row }">
                <div class="order-buttons">
                  <el-button 
                    size="small" 
                    :type="$index === 0 ? 'info' : 'primary'" 
                    circle 
                    @click="handleMoveField($index, -1)" 
                    :disabled="$index === 0 || !!searchKeyword"
                  >
                    <el-icon><SortUp /></el-icon>
                  </el-button>
                  <el-button 
                    size="small" 
                    :type="$index === filteredFieldsForSearch.length - 1 ? 'info' : 'primary'" 
                    circle 
                    @click="handleMoveField($index, 1)" 
                    :disabled="$index === filteredFieldsForSearch.length - 1 || !!searchKeyword"
                  >
                    <el-icon><SortDown /></el-icon>
                  </el-button>
                </div>
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
                      <el-dropdown-item command="edit">{{ $t('common.edit') }}</el-dropdown-item>
                      <el-dropdown-item command="delete" divided type="danger">{{ $t('common.delete') }}</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 添加/编辑对话框 -->
          <el-dialog 
            v-model="showFieldDialog" 
            :title="isEditMode ? $t('common.edit') : $t('typeFieldConfig.addField')" 
            width="500px"
          >
            <el-form :model="fieldForm" label-width="120px">
              <el-form-item :label="$t('typeFieldConfig.fieldKey')" required>
                <el-input v-model="fieldForm.fieldKey" :placeholder="$t('typeFieldConfig.fieldKey')" :disabled="isEditMode" />
              </el-form-item>
              <el-form-item :label="$t('typeFieldConfig.fieldLabel')" required>
                <el-input v-model="fieldForm.fieldLabel" :placeholder="$t('typeFieldConfig.fieldLabel')" />
              </el-form-item>
              <el-form-item :label="$t('typeFieldConfig.fieldLabelEn')">
                <el-input v-model="fieldForm.fieldLabelEn" :placeholder="$t('typeFieldConfig.fieldLabelEn')" />
              </el-form-item>
              <el-form-item :label="$t('typeFieldConfig.fieldType')" required>
                <el-select v-model="fieldForm.fieldType" style="width: 100%">
                  <el-option value="text" :label="t('typeFieldConfig.fieldTypes.text')" />
                  <el-option value="number" :label="t('typeFieldConfig.fieldTypes.number')" />
                  <el-option value="date" :label="t('typeFieldConfig.fieldTypes.date')" />
                  <el-option value="select" :label="t('typeFieldConfig.fieldTypes.select')" />
                  <el-option value="multiselect" :label="t('typeFieldConfig.fieldTypes.multiselect')" />
                  <el-option value="textarea" :label="t('typeFieldConfig.fieldTypes.textarea')" />
                  <el-option value="currency" :label="t('typeFieldConfig.fieldTypes.currency')" />
                </el-select>
              </el-form-item>
              <el-form-item v-if="fieldForm.fieldType === 'select' || fieldForm.fieldType === 'multiselect'" :label="$t('typeFieldConfig.quickCode')">
                <el-select
                  v-model="fieldForm.quickCodeId"
                  style="width: 100%"
                  :placeholder="$t('quickCode.select')"
                  clearable
                  filterable
                  :filter-method="handleQuickCodeFilter"
                  @visible-change="handleQuickCodeDropdownVisible"
                >
                  <el-option
                    v-for="qc in quickCodeOptionsToRender"
                    :key="qc.id"
                    :label="locale === 'en' && qc.nameEn ? qc.nameEn : qc.name"
                    :value="qc.code"
                  />
                </el-select>
              </el-form-item>
              <el-form-item :label="$t('typeFieldConfig.required')">
                <el-switch v-model="fieldForm.required" />
              </el-form-item>
              <el-form-item :label="$t('typeFieldConfig.showInList')">
                <el-switch v-model="fieldForm.showInList" />
              </el-form-item>
              <el-form-item :label="$t('typeFieldConfig.showInForm')">
                <el-switch v-model="fieldForm.showInForm" />
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="showFieldDialog = false">{{ $t('common.cancel') }}</el-button>
              <el-button type="primary" @click="handleSaveField" :loading="saving">{{ $t('common.save') }}</el-button>
            </template>
          </el-dialog>
          
          <!-- 预设字段区域 -->
          <div class="preset-section" v-if="showPresetFields">
            <div class="preset-header">
              <span class="preset-title">{{ $t('typeFieldConfig.presetFields') }}</span>
              <el-select v-model="presetFilterType" size="small" style="width: 100px" clearable>
                <el-option 
                  v-for="cat in presetCategories" 
                  :key="cat.value" 
                  :value="cat.value" 
                  :label="cat.label" 
                />
              </el-select>
              <el-input 
                v-model="presetSearch" 
                size="small" 
                :placeholder="$t('common.search') + '...'" 
                style="width: 120px"
                clearable
              >
                <template #prefix><el-icon><Search /></el-icon></template>
              </el-input>
            </div>
            <div class="preset-list" ref="presetListRef" @scroll.passive="handlePresetListScroll">
              <el-tag 
                v-for="preset in visiblePresetFields" 
                :key="preset.fieldKey"
                class="preset-tag"
                :type="addedFieldKeysSet.has(preset.fieldKey) ? 'info' : ''"
                :disable-transitions="true"
                @click="!addedFieldKeysSet.has(preset.fieldKey) && !addingPresetFieldMap[preset.fieldKey] && addPresetField(preset)"
              >
                {{ locale === 'en' ? preset.fieldLabelEn : preset.fieldLabel }} ({{ preset.fieldKey }})
                <el-icon v-if="addedFieldKeysSet.has(preset.fieldKey)"><Check /></el-icon>
                <el-icon v-else-if="addingPresetFieldMap[preset.fieldKey]"><Loading /></el-icon>
              </el-tag>
              <el-empty v-if="filteredPresetFields.length === 0" :description="$t('common.none')" :image-size="60" />
            </div>
            <div v-if="showPresetFields && filteredPresetFields.length > presetDisplayLimit" class="preset-more-row">
              <el-button link type="primary" @click="expandPresetList">
                {{ t('common.more') }} ({{ filteredPresetFields.length - presetDisplayLimit }})
              </el-button>
            </div>
          </div>
          
          <!-- 分页组件 -->
          <div class="pagination-section" v-if="totalFields > 0">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[5, 10, 20, 50]"
              :total="totalFields"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </div>
    </div>
    
    <el-dialog v-model="showTypeDialog" :title="$t('typeFieldConfig.addType')" width="400px">
      <el-form :model="typeForm" label-width="100px">
        <el-form-item :label="$t('typeFieldConfig.contractType')" required>
          <el-select v-model="typeForm.value" style="width: 100%">
            <el-option 
              v-for="type in availableTypes" 
              :key="type.value"
              :value="type.value"
              :label="type.label"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTypeDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSelectTypeConfirm">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import Sortable from 'sortablejs'
import { 
  getContractTypeFields, 
  getContractTypeFieldDraftConfig,
  saveContractTypeFieldDraft,
  discardContractTypeFieldDraft,
  publishContractTypeFieldDraft,
  getFieldCounts
} from '@/api/contractTypeField'
import { getContractCategories } from '@/api/contractCategory'
import { getQuickCodes } from '@/api/quickCode'
import { Plus, Delete, ArrowLeft, Check, Search, Download, Upload, Setting, Refresh, Collection, Close, Edit, SortUp, SortDown, ArrowDown, Loading } from '@element-plus/icons-vue'

const { t, locale } = useI18n()

const loading = ref(false)
const saving = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)
const currentType = ref<string | null>(null)
const fields = ref<any[]>([])
const allFields = ref<any[]>([])
const showTypeDialog = ref(false)
const typeForm = reactive({ value: '' })
const fieldCountMap = ref<Record<string, number>>({})
const fieldsCache = ref<Record<string, any[]>>({})
const totalFields = ref(0)
const hasLocalChanges = ref(false)
const currentPage = ref(1)
const pageSize = ref(5)
const searchKeyword = ref('')
const showPresetFields = ref(false)
const presetSearch = ref('')
const presetFilterType = ref('all')
const presetDisplayLimit = ref(40)
const addingPresetFieldMap = reactive<Record<string, boolean>>({})
const presetListRef = ref<HTMLElement | null>(null)
let presetRefreshTimer: number | null = null
let presetSuccessToastTimer: number | null = null
const presetSuccessCount = ref(0)
const draftPublishVersion = ref(0)
let localTempFieldId = -1
const selectedFieldIds = ref<number[]>([])
const fieldsTableRef = ref<any>(null)
let fieldSortable: Sortable | null = null

const showFieldDialog = ref(false)
const isEditMode = ref(false)
const LAST_SELECTED_TYPE_KEY = 'contract_type_field_last_selected_type'
const fieldForm = reactive({
  id: null as number | null,
  fieldKey: '',
  fieldLabel: '',
  fieldLabelEn: '',
  fieldType: 'text',
  quickCodeId: null as string | null,
  required: false,
  showInList: true,
  showInForm: true
})
const QUICK_CODE_RECENT_KEY = 'contract_type_field_recent_quick_codes'

const addedFieldKeysSet = computed(() => new Set(allFields.value.map(f => f.fieldKey)))

const paginatedFields = computed(() => {
  return fields.value
})

const filteredFieldsForSearch = computed(() => {
  if (!searchKeyword.value) return allFields.value
  const kw = searchKeyword.value.toLowerCase()
  return allFields.value.filter(f =>
    f.fieldKey?.toLowerCase().includes(kw) ||
    f.fieldLabel?.toLowerCase().includes(kw) ||
    f.fieldLabelEn?.toLowerCase().includes(kw)
  )
})

watch(filteredFieldsForSearch, (val) => {
  totalFields.value = val.length
  applyFieldPagination()
  nextTick(initFieldRowDrag)
})

watch([presetFilterType, presetSearch, showPresetFields], () => {
  if (showPresetFields.value) {
    presetDisplayLimit.value = 40
    if (presetListRef.value) {
      presetListRef.value.scrollTop = 0
    }
  }
})

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    text: t('typeFieldConfig.fieldTypes.text'),
    number: t('typeFieldConfig.fieldTypes.number'),
    date: t('typeFieldConfig.fieldTypes.date'),
    select: t('typeFieldConfig.fieldTypes.select'),
    multiselect: t('typeFieldConfig.fieldTypes.multiselect'),
    textarea: t('typeFieldConfig.fieldTypes.textarea'),
    currency: t('typeFieldConfig.fieldTypes.currency')
  }
  return map[type] || type
}

const openAddDialog = () => {
  isEditMode.value = false
  fieldForm.id = null
  fieldForm.fieldKey = ''
  fieldForm.fieldLabel = ''
  fieldForm.fieldLabelEn = ''
  fieldForm.fieldType = 'text'
  fieldForm.quickCodeId = null
  fieldForm.required = false
  fieldForm.showInList = true
  fieldForm.showInForm = true
  quickCodeSearchKeyword.value = ''
  quickCodeDropdownOpen.value = false
  showFieldDialog.value = true
}

const openEditDialog = (row: any) => {
  isEditMode.value = true
  fieldForm.id = row.id
  fieldForm.fieldKey = row.fieldKey
  fieldForm.fieldLabel = row.fieldLabel
  fieldForm.fieldLabelEn = row.fieldLabelEn || ''
  fieldForm.fieldType = row.fieldType
  fieldForm.quickCodeId = row.quickCodeId
  fieldForm.required = row.required
  fieldForm.showInList = row.showInList
  fieldForm.showInForm = row.showInForm
  quickCodeSearchKeyword.value = ''
  quickCodeDropdownOpen.value = false
  showFieldDialog.value = true
}

const handleSaveField = async () => {
  if (!fieldForm.fieldKey || !fieldForm.fieldLabel) {
    ElMessage.warning(t('typeFieldConfig.fillRequired'))
    return
  }
  
  if ((fieldForm.fieldType === 'select' || fieldForm.fieldType === 'multiselect') && !fieldForm.quickCodeId) {
    ElMessage.warning(t('typeFieldConfig.selectQuickCodeRequired'))
    return
  }
  
  if (!isEditMode.value && addedFieldKeysSet.value.has(fieldForm.fieldKey)) {
    ElMessage.warning(t('typeFieldConfig.fieldExists'))
    return
  }
  
  saving.value = true
  try {
    if (isEditMode.value && fieldForm.id != null) {
      const idx = allFields.value.findIndex(f => f.id === fieldForm.id)
      if (idx >= 0) {
        allFields.value[idx] = {
          ...allFields.value[idx],
          fieldLabel: fieldForm.fieldLabel,
          fieldLabelEn: fieldForm.fieldLabelEn,
          fieldType: fieldForm.fieldType,
          quickCodeId: (fieldForm.fieldType === 'select' || fieldForm.fieldType === 'multiselect')
            ? fieldForm.quickCodeId
            : null,
          required: fieldForm.required,
          showInList: fieldForm.showInList,
          showInForm: fieldForm.showInForm
        }
      }
    } else {
      allFields.value.push({
        id: localTempFieldId--,
        contractType: currentType.value,
        fieldKey: fieldForm.fieldKey,
        fieldLabel: fieldForm.fieldLabel,
        fieldLabelEn: fieldForm.fieldLabelEn,
        fieldType: fieldForm.fieldType,
        quickCodeId: (fieldForm.fieldType === 'select' || fieldForm.fieldType === 'multiselect')
          ? fieldForm.quickCodeId
          : null,
        required: fieldForm.required,
        showInList: fieldForm.showInList,
        showInForm: fieldForm.showInForm,
        fieldOrder: allFields.value.length
      })
    }
    hasLocalChanges.value = true
    if ((fieldForm.fieldType === 'select' || fieldForm.fieldType === 'multiselect') && fieldForm.quickCodeId) {
      recordRecentQuickCode(fieldForm.quickCodeId)
    }
    showFieldDialog.value = false
    totalFields.value = allFields.value.length
    applyFieldPagination()
  } catch (error: any) {
    ElMessage.error(error.message || t('common.error'))
  } finally {
    saving.value = false
  }
}

const categories = ref<any[]>([])

const contractTypes = computed(() => {
  if (categories.value.length > 0) {
    return categories.value.map(cat => ({
      value: cat.code,
      label: cat.name,
      color: cat.color || '#667eea'
    }))
  }
  return [
    { value: 'PURCHASE', label: t('contract.types.purchase'), color: '#667eea' },
    { value: 'SALES', label: t('contract.types.sales'), color: '#764ba2' },
    { value: 'LEASE', label: t('contract.types.lease'), color: '#f093fb' },
    { value: 'EMPLOYMENT', label: t('contract.types.employment'), color: '#f5576c' },
    { value: 'SERVICE', label: t('contract.types.service'), color: '#11998e' },
    { value: 'OTHER', label: t('contract.types.other'), color: '#38ef7d' }
  ]
})

const availableTypes = computed(() => contractTypes.value)

const presetFields = [
  { fieldKey: 'supplier', fieldLabel: '供应商', fieldLabelEn: 'Supplier', fieldType: 'text' },
  { fieldKey: 'supplier_contact', fieldLabel: '供应商联系人', fieldLabelEn: 'Supplier Contact', fieldType: 'text' },
  { fieldKey: 'supplier_phone', fieldLabel: '供应商电话', fieldLabelEn: 'Supplier Phone', fieldType: 'text' },
  { fieldKey: 'delivery_date', fieldLabel: '交货日期', fieldLabelEn: 'Delivery Date', fieldType: 'date' },
  { fieldKey: 'delivery_address', fieldLabel: '交货地址', fieldLabelEn: 'Delivery Address', fieldType: 'text' },
  { fieldKey: 'warranty_period', fieldLabel: '保修期', fieldLabelEn: 'Warranty Period', fieldType: 'text' },
  { fieldKey: 'quality_standard', fieldLabel: '质量标准', fieldLabelEn: 'Quality Standard', fieldType: 'text' },
  { fieldKey: 'quantity', fieldLabel: '数量', fieldLabelEn: 'Quantity', fieldType: 'number' },
  { fieldKey: 'unit_price', fieldLabel: '单价', fieldLabelEn: 'Unit Price', fieldType: 'currency' },
  { fieldKey: 'discount', fieldLabel: '折扣率(%)', fieldLabelEn: 'Discount (%)', fieldType: 'number' },
  { fieldKey: 'payment_terms', fieldLabel: '付款条款', fieldLabelEn: 'Payment Terms', fieldType: 'select' },
  { fieldKey: 'transport_fee', fieldLabel: '运输费用', fieldLabelEn: 'Transport Fee', fieldType: 'currency' },
  { fieldKey: 'customer_name', fieldLabel: '客户名称', fieldLabelEn: 'Customer Name', fieldType: 'text' },
  { fieldKey: 'customer_contact', fieldLabel: '客户联系人', fieldLabelEn: 'Customer Contact', fieldType: 'text' },
  { fieldKey: 'customer_phone', fieldLabel: '客户电话', fieldLabelEn: 'Customer Phone', fieldType: 'text' },
  { fieldKey: 'shipping_date', fieldLabel: '发货日期', fieldLabelEn: 'Shipping Date', fieldType: 'date' },
  { fieldKey: 'sales_region', fieldLabel: '销售区域', fieldLabelEn: 'Sales Region', fieldType: 'text' },
  { fieldKey: 'channel', fieldLabel: '销售渠道', fieldLabelEn: 'Sales Channel', fieldType: 'select' },
  { fieldKey: 'property_address', fieldLabel: '物业地址', fieldLabelEn: 'Property Address', fieldType: 'text' },
  { fieldKey: 'property_area', fieldLabel: '面积(㎡)', fieldLabelEn: 'Area (㎡)', fieldType: 'number' },
  { fieldKey: 'monthly_rent', fieldLabel: '月租金', fieldLabelEn: 'Monthly Rent', fieldType: 'currency' },
  { fieldKey: 'quarterly_rent', fieldLabel: '季度租金', fieldLabelEn: 'Quarterly Rent', fieldType: 'currency' },
  { fieldKey: 'annual_rent', fieldLabel: '年租金', fieldLabelEn: 'Annual Rent', fieldType: 'currency' },
  { fieldKey: 'deposit_amount', fieldLabel: '押金', fieldLabelEn: 'Deposit', fieldType: 'currency' },
  { fieldKey: 'payment_method', fieldLabel: '付款方式', fieldLabelEn: 'Payment Method', fieldType: 'select' },
  { fieldKey: 'property_type', fieldLabel: '物业类型', fieldLabelEn: 'Property Type', fieldType: 'select' },
  { fieldKey: 'renovation_allowance', fieldLabel: '装修免租期', fieldLabelEn: 'Decoration Allowance', fieldType: 'text' },
  { fieldKey: 'agent_fee', fieldLabel: '中介费', fieldLabelEn: 'Agent Fee', fieldType: 'currency' },
  { fieldKey: 'position', fieldLabel: '岗位', fieldLabelEn: 'Position', fieldType: 'text' },
  { fieldKey: 'department', fieldLabel: '部门', fieldLabelEn: 'Department', fieldType: 'text' },
  { fieldKey: 'base_salary', fieldLabel: '基本工资', fieldLabelEn: 'Base Salary', fieldType: 'currency' },
  { fieldKey: 'monthly_salary', fieldLabel: '月薪', fieldLabelEn: 'Monthly Salary', fieldType: 'currency' },
  { fieldKey: 'annual_salary', fieldLabel: '年薪', fieldLabelEn: 'Annual Salary', fieldType: 'currency' },
  { fieldKey: 'trial_salary', fieldLabel: '试用期工资', fieldLabelEn: 'Trial Salary', fieldType: 'currency' },
  { fieldKey: 'trial_period', fieldLabel: '试用期(月)', fieldLabelEn: 'Trial Period (Months)', fieldType: 'number' },
  { fieldKey: 'work_location', fieldLabel: '工作地点', fieldLabelEn: 'Work Location', fieldType: 'text' },
  { fieldKey: 'work_hours', fieldLabel: '工作时间', fieldLabelEn: 'Work Hours', fieldType: 'text' },
  { fieldKey: 'social_benefits', fieldLabel: '社保缴纳', fieldLabelEn: 'Social Benefits', fieldType: 'select' },
  { fieldKey: 'probation_period', fieldLabel: '转正考核期', fieldLabelEn: 'Probation Period', fieldType: 'text' },
  { fieldKey: 'contract_period', fieldLabel: '合同期限(年)', fieldLabelEn: 'Contract Period (Years)', fieldType: 'number' },
  { fieldKey: 'service_content', fieldLabel: '服务内容', fieldLabelEn: 'Service Content', fieldType: 'textarea' },
  { fieldKey: 'service_fee', fieldLabel: '服务费', fieldLabelEn: 'Service Fee', fieldType: 'currency' },
  { fieldKey: 'service_start', fieldLabel: '服务开始日期', fieldLabelEn: 'Service Start Date', fieldType: 'date' },
  { fieldKey: 'service_end', fieldLabel: '服务结束日期', fieldLabelEn: 'Service End Date', fieldType: 'date' },
  { fieldKey: 'service_frequency', fieldLabel: '服务频率', fieldLabelEn: 'Service Frequency', fieldType: 'select' },
  { fieldKey: 'service_scope', fieldLabel: '服务范围', fieldLabelEn: 'Service Scope', fieldType: 'textarea' },
  { fieldKey: 'service_level', fieldLabel: '服务水平要求', fieldLabelEn: 'Service Level', fieldType: 'select' },
  { fieldKey: 'response_time', fieldLabel: '响应时间', fieldLabelEn: 'Response Time', fieldType: 'text' },
  { fieldKey: 'contact_person', fieldLabel: '联系人', fieldLabelEn: 'Contact Person', fieldType: 'text' },
  { fieldKey: 'contact_phone', fieldLabel: '联系电话', fieldLabelEn: 'Contact Phone', fieldType: 'text' },
  { fieldKey: 'contact_email', fieldLabel: '联系邮箱', fieldLabelEn: 'Contact Email', fieldType: 'text' },
  { fieldKey: 'bank_name', fieldLabel: '开户银行', fieldLabelEn: 'Bank Name', fieldType: 'text' },
  { fieldKey: 'bank_account', fieldLabel: '银行账号', fieldLabelEn: 'Bank Account', fieldType: 'text' },
  { fieldKey: 'tax_rate', fieldLabel: '税率(%)', fieldLabelEn: 'Tax Rate (%)', fieldType: 'number' },
  { fieldKey: 'tax_included', fieldLabel: '是否含税', fieldLabelEn: 'Tax Included', fieldType: 'select' },
  { fieldKey: 'invoice_type', fieldLabel: '发票类型', fieldLabelEn: 'Invoice Type', fieldType: 'select' },
  { fieldKey: 'signing_location', fieldLabel: '签约地点', fieldLabelEn: 'Signing Location', fieldType: 'text' },
  { fieldKey: 'effective_date', fieldLabel: '生效日期', fieldLabelEn: 'Effective Date', fieldType: 'date' },
  { fieldKey: 'termination_clause', fieldLabel: '终止条款', fieldLabelEn: 'Termination Clause', fieldType: 'textarea' },
  { fieldKey: 'penalty_clause', fieldLabel: '违约条款', fieldLabelEn: 'Penalty Clause', fieldType: 'textarea' },
  { fieldKey: 'confidentiality', fieldLabel: '保密条款', fieldLabelEn: 'Confidentiality', fieldType: 'select' },
  { fieldKey: 'exclusive_clause', fieldLabel: '独家条款', fieldLabelEn: 'Exclusive Clause', fieldType: 'select' }
]

const quickCodes = ref<any[]>([])
const quickCodeSearchKeyword = ref('')
const recentQuickCodeCodes = ref<string[]>([])
const quickCodeDropdownOpen = ref(false)

const loadQuickCodes = async () => {
  try {
    const res = await getQuickCodes()
    quickCodes.value = res.data || []
  } catch (error) {
    console.error('Failed to load quick codes:', error)
  }
}

const loadRecentQuickCodes = () => {
  try {
    const raw = localStorage.getItem(QUICK_CODE_RECENT_KEY)
    if (!raw) return
    const parsed = JSON.parse(raw)
    if (Array.isArray(parsed)) {
      recentQuickCodeCodes.value = parsed.filter((item) => typeof item === 'string').slice(0, 8)
    }
  } catch (e) {
    recentQuickCodeCodes.value = []
  }
}

const recordRecentQuickCode = (code: string) => {
  const next = [code, ...recentQuickCodeCodes.value.filter(item => item !== code)].slice(0, 8)
  recentQuickCodeCodes.value = next
  try {
    localStorage.setItem(QUICK_CODE_RECENT_KEY, JSON.stringify(next))
  } catch (e) {
    // ignore localStorage failures
  }
}

const getQuickCodeName = (qc: any) => {
  return (locale.value === 'en' && qc.nameEn ? qc.nameEn : qc.name) || ''
}

const sortedQuickCodes = computed(() => {
  const keyword = quickCodeSearchKeyword.value.trim().toLowerCase()
  const recentMap = new Map<string, number>()
  recentQuickCodeCodes.value.forEach((code, idx) => recentMap.set(code, idx))
  const ranked = [...quickCodes.value].map((qc, idx) => {
    const code = String(qc.code || '')
    const name = getQuickCodeName(qc)
    const codeLower = code.toLowerCase()
    const nameLower = name.toLowerCase()
    let score = 0
    if (keyword) {
      if (codeLower.startsWith(keyword)) score += 400
      else if (codeLower.includes(keyword)) score += 250
      if (nameLower.startsWith(keyword)) score += 300
      else if (nameLower.includes(keyword)) score += 180
    }
    if (recentMap.has(code)) {
      score += 120 - (recentMap.get(code) || 0) * 10
    }
    return { qc, score, idx }
  })
  ranked.sort((a, b) => {
    if (b.score !== a.score) return b.score - a.score
    return a.idx - b.idx
  })
  return ranked.map(item => item.qc)
})

const quickCodeOptionsToRender = computed(() => {
  if (quickCodeDropdownOpen.value) return sortedQuickCodes.value
  const limited = sortedQuickCodes.value.slice(0, 40)
  if (!fieldForm.quickCodeId) return limited
  if (limited.some(item => item.code === fieldForm.quickCodeId)) return limited
  const selected = sortedQuickCodes.value.find(item => item.code === fieldForm.quickCodeId)
  return selected ? [selected, ...limited] : limited
})

const handleQuickCodeFilter = (value: string) => {
  quickCodeSearchKeyword.value = value || ''
}

const handleQuickCodeDropdownVisible = (visible: boolean) => {
  quickCodeDropdownOpen.value = visible
  if (!visible) {
    quickCodeSearchKeyword.value = ''
  }
}

const getQuickCodeLabel = (qc: any) => {
  const name = locale.value === 'en' && qc.nameEn ? qc.nameEn : qc.name
  return `${qc.code} - ${name}`
}

const getQuickCodeDisplayName = (code: string) => {
  const qc = quickCodes.value.find(q => q.code === code)
  if (!qc) return code
  return locale.value === 'en' && qc.nameEn ? qc.nameEn : qc.name
}

const getTypeColor = (type: string) => {
  const types = contractTypes.value
  const found = types.find(t => t.value === type)
  return found ? found.color : '#667eea'
}

const getFieldCount = (type: string) => {
  return fieldCountMap.value[type] || 0
}

const presetCategories = computed(() => {
  const base = [{ value: 'all', label: t('common.all') }]
  if (categories.value.length > 0) {
    return [
      ...base,
      ...categories.value.map(cat => ({
        value: cat.code,
        label: locale.value === 'en' && cat.nameEn ? cat.nameEn : cat.name
      }))
    ]
  }
  return [
    ...base,
    { value: 'PURCHASE', label: t('contract.types.purchase') },
    { value: 'SALES', label: t('contract.types.sales') },
    { value: 'LEASE', label: t('contract.types.lease') },
    { value: 'EMPLOYMENT', label: t('contract.types.employment') },
    { value: 'SERVICE', label: t('contract.types.service') },
    { value: 'OTHER', label: t('contract.types.other') }
  ]
})

const presetFieldCategories: Record<string, string[]> = {
  PURCHASE: ['supplier', 'supplier_contact', 'supplier_phone', 'delivery_date', 'delivery_address', 'warranty_period', 'quality_standard', 'quantity', 'unit_price', 'discount', 'payment_terms', 'transport_fee'],
  SALES: ['customer_name', 'customer_contact', 'customer_phone', 'shipping_date', 'sales_region', 'channel'],
  LEASE: ['property_address', 'property_area', 'monthly_rent', 'quarterly_rent', 'annual_rent', 'deposit_amount', 'payment_method', 'property_type', 'renovation_allowance', 'agent_fee'],
  EMPLOYMENT: ['position', 'department', 'base_salary', 'monthly_salary', 'annual_salary', 'trial_salary', 'trial_period', 'work_location', 'work_hours', 'social_benefits', 'probation_period', 'contract_period'],
  SERVICE: ['service_content', 'service_fee', 'service_start', 'service_end', 'service_frequency', 'service_scope', 'service_level', 'response_time'],
  OTHER: [],
  COMMON: ['contact_person', 'contact_phone', 'contact_email', 'bank_name', 'bank_account', 'tax_rate', 'tax_included', 'invoice_type', 'signing_location', 'effective_date', 'termination_clause', 'penalty_clause', 'confidentiality', 'exclusive_clause']
}

const filteredPresetFields = computed(() => {
  const filterType = presetFilterType.value
  const search = presetSearch.value.toLowerCase()
  
  const categoryKeys = new Set<string>()
  
  if (filterType !== 'all') {
    const typeFields = presetFieldCategories[filterType] || []
    typeFields.forEach(f => categoryKeys.add(f))
    const commonFields = presetFieldCategories['COMMON'] || []
    commonFields.forEach(f => categoryKeys.add(f))
  } else {
    Object.values(presetFieldCategories).forEach(keys => {
      keys.forEach(f => categoryKeys.add(f))
    })
  }
  
  return presetFields.filter(f => {
    if (!categoryKeys.has(f.fieldKey)) return false
    if (search) {
      return f.fieldLabel.toLowerCase().includes(search) || 
             f.fieldLabelEn.toLowerCase().includes(search) ||
             f.fieldKey.toLowerCase().includes(search)
    }
    return true
  })
})

const visiblePresetFields = computed(() => {
  return filteredPresetFields.value.slice(0, presetDisplayLimit.value)
})

const expandPresetList = () => {
  presetDisplayLimit.value = Math.min(filteredPresetFields.value.length, presetDisplayLimit.value + 40)
}

const handlePresetListScroll = () => {
  const node = presetListRef.value
  if (!node) return
  const distanceToBottom = node.scrollHeight - node.scrollTop - node.clientHeight
  if (distanceToBottom <= 24 && visiblePresetFields.value.length < filteredPresetFields.value.length) {
    expandPresetList()
  }
}

const normalizeDraftField = (field: any, idx: number) => ({
  id: typeof field.id === 'number' && field.id > 0 ? field.id : null,
  contractType: currentType.value,
  fieldKey: field.fieldKey,
  fieldLabel: field.fieldLabel,
  fieldLabelEn: field.fieldLabelEn || '',
  fieldType: field.fieldType,
  quickCodeId: (field.fieldType === 'select' || field.fieldType === 'multiselect')
    ? (field.quickCodeId || null)
    : null,
  required: !!field.required,
  showInList: field.showInList !== false,
  showInForm: field.showInForm !== false,
  fieldOrder: idx,
  placeholder: field.placeholder || null,
  placeholderEn: field.placeholderEn || null,
  defaultValue: field.defaultValue || null,
  options: field.options || null,
  minValue: field.minValue ?? null,
  maxValue: field.maxValue ?? null
})

const selectType = async (type: string) => {
  currentType.value = type
  localStorage.setItem(LAST_SELECTED_TYPE_KEY, type)
  presetFilterType.value = type
  presetSearch.value = ''
  showPresetFields.value = false
  searchKeyword.value = ''
  currentPage.value = 1
  
  if (fieldsCache.value[type] && (fieldsCache.value[type].length > 0 || getFieldCount(type) === 0)) {
    allFields.value = fieldsCache.value[type]
    totalFields.value = allFields.value.length
    applyFieldPagination()
    return
  }
  await loadFields()
}

const loadFields = async () => {
  if (!currentType.value) return
  loading.value = true
  try {
    const res = await getContractTypeFieldDraftConfig(currentType.value)
    allFields.value = res.data?.fields || []
    hasLocalChanges.value = false
    selectedFieldIds.value = []
    draftPublishVersion.value = res.data?.publishVersion || 0
    totalFields.value = allFields.value.length
    fieldsCache.value[currentType.value] = allFields.value
    applyFieldPagination()
    nextTick(initFieldRowDrag)
    fieldCountMap.value[currentType.value] = totalFields.value
  } catch (error) {
    ElMessage.error(t('common.error'))
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  applyFieldPagination()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  applyFieldPagination()
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleReset = () => {
  searchKeyword.value = ''
  currentPage.value = 1
}

const applyFieldPagination = () => {
  const start = (currentPage.value - 1) * pageSize.value
  fields.value = filteredFieldsForSearch.value.slice(start, start + pageSize.value)
  nextTick(initFieldRowDrag)
}

const scheduleApplyFieldPagination = () => {
  if (presetRefreshTimer) {
    window.clearTimeout(presetRefreshTimer)
  }
  presetRefreshTimer = window.setTimeout(() => {
    applyFieldPagination()
    presetRefreshTimer = null
  }, 120)
}

const schedulePresetSuccessToast = () => {
  presetSuccessCount.value += 1
  if (presetSuccessToastTimer) {
    window.clearTimeout(presetSuccessToastTimer)
  }
  presetSuccessToastTimer = window.setTimeout(() => {
    if (presetSuccessCount.value <= 1) {
      ElMessage.success(t('common.success'))
    } else {
      ElMessage.success(`${t('common.success')} (${presetSuccessCount.value})`)
    }
    presetSuccessCount.value = 0
    presetSuccessToastTimer = null
  }, 200)
}

const handleAction = (command: string, row: any) => {
  switch (command) {
    case 'edit':
      openEditDialog(row)
      break
    case 'delete':
      handleDelete(row.id)
      break
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm(t('common.confirmDelete'), t('common.warning'), { type: 'warning' })
    allFields.value = allFields.value.filter(f => f.id !== id)
    hasLocalChanges.value = true
    if (fieldsCache.value[currentType.value!]) {
      fieldsCache.value[currentType.value!] = fieldsCache.value[currentType.value!].filter(f => f.id !== id)
    }
    totalFields.value = allFields.value.length
    const start = (currentPage.value - 1) * pageSize.value
    fields.value = filteredFieldsForSearch.value.slice(start, start + pageSize.value)
    fieldCountMap.value[currentType.value!] = totalFields.value
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(t('common.error'))
    }
  }
}

const addPresetField = async (preset: any) => {
  if (addedFieldKeysSet.value.has(preset.fieldKey)) {
    ElMessage.warning(t('typeFieldConfig.fieldExists'))
    return
  }
  if (addingPresetFieldMap[preset.fieldKey]) return
  addingPresetFieldMap[preset.fieldKey] = true
  
  try {
    const createdField = {
      id: localTempFieldId--,
      contractType: currentType.value,
      ...preset,
      required: false,
      showInList: true,
      showInForm: true,
      fieldOrder: allFields.value.length
    }
    allFields.value = [...allFields.value, createdField]
    hasLocalChanges.value = true
    totalFields.value = allFields.value.length
    if (currentType.value) {
      fieldsCache.value[currentType.value] = [...allFields.value]
      fieldCountMap.value[currentType.value] = totalFields.value
    }
    scheduleApplyFieldPagination()
    schedulePresetSuccessToast()
  } catch (error: any) {
    ElMessage.error(error.message || t('common.error'))
  } finally {
    addingPresetFieldMap[preset.fieldKey] = false
  }
}

const handleSelectType = () => {
  typeForm.value = ''
  showTypeDialog.value = true
}

const handleSelectTypeConfirm = () => {
  if (!typeForm.value) {
    ElMessage.warning(t('typeFieldConfig.selectTypeWarning'))
    return
  }
  showTypeDialog.value = false
  selectType(typeForm.value)
}

const handleExportAll = async () => {
  try {
    const res = await getContractTypeFields()
    const allFieldsExport = res.data?.list || []
    
    const exportData: Record<string, any[]> = {}
    allFieldsExport.forEach((field: any) => {
      if (!exportData[field.contractType]) {
        exportData[field.contractType] = []
      }
      exportData[field.contractType].push({
        fieldKey: field.fieldKey,
        fieldLabel: field.fieldLabel,
        fieldLabelEn: field.fieldLabelEn,
        fieldType: field.fieldType,
        required: field.required,
        showInList: field.showInList,
        showInForm: field.showInForm
      })
    })
    
    const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `contract-type-fields-${new Date().toISOString().slice(0, 10)}.json`
    link.click()
    URL.revokeObjectURL(url)
    
    ElMessage.success(t('typeFieldConfig.exportSuccess'))
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const handleImport = () => {
  fileInputRef.value?.click()
}

const handleFileChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  
  try {
    const text = await file.text()
    const importData = JSON.parse(text)
    
    await ElMessageBox.confirm(t('typeFieldConfig.confirmImport'), t('common.warning'), { type: 'warning' })
    
    loading.value = true
    if (!currentType.value) {
      ElMessage.warning(t('typeFieldConfig.selectTypeWarning'))
      loading.value = false
      return
    }
    const importFields = (importData[currentType.value] || importData.fields || []) as any[]
    allFields.value = importFields.map((f: any, idx: number) => ({
      ...f,
      id: f.id ?? (localTempFieldId--),
      contractType: currentType.value,
      fieldOrder: typeof f.fieldOrder === 'number' ? f.fieldOrder : idx
    }))
    hasLocalChanges.value = true
    
    ElMessage.success(t('typeFieldConfig.importSuccess'))
    
    if (currentType.value) {
      delete fieldsCache.value[currentType.value]
      await loadFields()
    }
    await refreshFieldCounts()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(t('typeFieldConfig.importFailed'))
    }
  }
  
  target.value = ''
  loading.value = false
}

const refreshFieldCounts = async () => {
  try {
    const res = await getFieldCounts()
    fieldCountMap.value = res.data || {}
  } catch (error) {
    console.error('Failed to refresh field counts')
  }
}

const persistDraft = async (showMessage = true) => {
  if (!currentType.value) return
  const payload = allFields.value.map((field, idx) => normalizeDraftField(field, idx))
  await saveContractTypeFieldDraft(currentType.value, payload)
  hasLocalChanges.value = false
  fieldsCache.value[currentType.value] = [...allFields.value]
  if (showMessage) {
    ElMessage.success(t('common.success'))
  }
}

const handleSaveDraft = async () => {
  try {
    await persistDraft(true)
  } catch (error: any) {
    ElMessage.error(error.message || t('common.error'))
  }
}

const handlePublishDraft = async () => {
  if (!currentType.value) return
  try {
    if (hasLocalChanges.value) {
      await persistDraft(false)
    }
    await ElMessageBox.confirm(t('typeFieldConfig.confirmPublishDraft'), t('common.warning'), { type: 'warning' })
    const res = await publishContractTypeFieldDraft(currentType.value)
    draftPublishVersion.value = res.data?.publishVersion || draftPublishVersion.value + 1
    await refreshFieldCounts()
    ElMessage.success(t('typeFieldConfig.publishSuccess'))
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || t('common.error'))
    }
  }
}

const handleDiscardDraft = async () => {
  if (!currentType.value) return
  try {
    await ElMessageBox.confirm(t('typeFieldConfig.confirmDiscardDraft'), t('common.warning'), { type: 'warning' })
    await discardContractTypeFieldDraft(currentType.value)
    await loadFields()
    hasLocalChanges.value = false
    ElMessage.success(t('typeFieldConfig.discardSuccess'))
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || t('common.error'))
    }
  }
}

const handleInitializeDefaults = async () => {
  try {
    await ElMessageBox.confirm(t('typeFieldConfig.confirmInit'), t('common.warning'), { type: 'warning' })
    
    if (!currentType.value) {
      ElMessage.warning(t('typeFieldConfig.selectTypeWarning'))
      return
    }
    loading.value = true
    const typeFields = presetFields.filter((_, idx) => {
      const cats = Object.entries(presetFieldCategories)
      for (const [cat, keys] of cats) {
        if (cat !== 'COMMON' && keys.includes(presetFields[idx].fieldKey)) {
          return cat.toUpperCase() === currentType.value
        }
      }
      return false
    })
    const commonFields = presetFields.filter((_, idx) => {
      const cats = Object.entries(presetFieldCategories)
      for (const [cat, keys] of cats) {
        if (cat === 'COMMON' && keys.includes(presetFields[idx].fieldKey)) {
          return true
        }
      }
      return false
    })
    allFields.value = [...typeFields, ...commonFields].map((item, idx) => ({
      ...item,
      id: localTempFieldId--,
      contractType: currentType.value,
      required: false,
      showInList: true,
      showInForm: true,
      fieldOrder: idx
    }))
    hasLocalChanges.value = true
    ElMessage.success(t('common.success'))
    applyFieldPagination()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(t('common.error'))
    }
  } finally {
    loading.value = false
  }
}

const handleKeyDown = (event: KeyboardEvent) => {
  if (event.key === 'Escape') {
    if (showFieldDialog.value) {
      showFieldDialog.value = false
    }
    if (showTypeDialog.value) {
      showTypeDialog.value = false
    }
  }
  
  if ((event.ctrlKey || event.metaKey) && event.key === 's') {
    event.preventDefault()
    if (showFieldDialog.value) {
      handleSaveField()
    }
  }
}

const handleMoveField = async (index: number, direction: number) => {
  if (searchKeyword.value) {
    ElMessage.warning(t('typeFieldConfig.reorderSearchBlocked'))
    return
  }
  const realIndex = (currentPage.value - 1) * pageSize.value + index
  const newIndex = realIndex + direction
  
  if (newIndex < 0 || newIndex >= allFields.value.length) return
  
  const prevFields = allFields.value.map(f => ({ ...f }))
  const temp = allFields.value[realIndex]
  allFields.value[realIndex] = allFields.value[newIndex]
  allFields.value[newIndex] = temp
  
  allFields.value.forEach((field, i) => {
    field.fieldOrder = i
  })
  
  try {
    hasLocalChanges.value = true
    const start = (currentPage.value - 1) * pageSize.value
    fields.value = filteredFieldsForSearch.value.slice(start, start + pageSize.value)
  } catch (error: any) {
    allFields.value = prevFields
    const start = (currentPage.value - 1) * pageSize.value
    fields.value = filteredFieldsForSearch.value.slice(start, start + pageSize.value)
    ElMessage.error(error.message || t('common.error'))
  }
}

const handleSelectionChange = (rows: any[]) => {
  selectedFieldIds.value = rows.map(row => Number(row.id)).filter(id => !Number.isNaN(id))
}

const updateSelectedFields = (updater: (row: any) => any) => {
  const selected = new Set(selectedFieldIds.value)
  if (selected.size === 0) return
  allFields.value = allFields.value.map(row => {
    if (!selected.has(Number(row.id))) return row
    return updater({ ...row })
  })
  hasLocalChanges.value = true
  applyFieldPagination()
}

const handleBatchSetRequired = (required: boolean) => {
  updateSelectedFields(row => ({ ...row, required }))
}

const handleBatchSetShowInList = (showInList: boolean) => {
  updateSelectedFields(row => ({ ...row, showInList }))
}

const handleBatchSetShowInForm = (showInForm: boolean) => {
  updateSelectedFields(row => ({ ...row, showInForm }))
}

const handleBatchDelete = async () => {
  if (selectedFieldIds.value.length === 0) return
  try {
    await ElMessageBox.confirm(t('typeFieldConfig.confirmBatchDelete', { count: selectedFieldIds.value.length }), t('common.warning'), { type: 'warning' })
    const selected = new Set(selectedFieldIds.value)
    allFields.value = allFields.value.filter(row => !selected.has(Number(row.id)))
    selectedFieldIds.value = []
    hasLocalChanges.value = true
    totalFields.value = allFields.value.length
    applyFieldPagination()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || t('common.error'))
    }
  }
}

const initFieldRowDrag = () => {
  if (!fieldsTableRef.value || searchKeyword.value) {
    fieldSortable?.destroy()
    fieldSortable = null
    return
  }
  const tableRoot = fieldsTableRef.value.$el || fieldsTableRef.value
  const tbody = tableRoot?.querySelector('.el-table__body-wrapper tbody') as HTMLElement | null
  if (!tbody) return
  fieldSortable?.destroy()
  fieldSortable = Sortable.create(tbody, {
    handle: '.drag-handle',
    animation: 150,
    onEnd(evt) {
      const from = evt.oldIndex ?? -1
      const to = evt.newIndex ?? -1
      if (from < 0 || to < 0 || from === to) return
      const pageOffset = (currentPage.value - 1) * pageSize.value
      const fromIndex = pageOffset + from
      const toIndex = pageOffset + to
      const moved = allFields.value.splice(fromIndex, 1)[0]
      if (!moved) return
      allFields.value.splice(toIndex, 0, moved)
      allFields.value.forEach((field, idx) => {
        field.fieldOrder = idx
      })
      hasLocalChanges.value = true
      applyFieldPagination()
    }
  })
}

onMounted(async () => {
  try {
    loadRecentQuickCodes()
    const [countsRes, catsRes] = await Promise.all([
      getFieldCounts(),
      getContractCategories()
    ])
    fieldCountMap.value = countsRes.data || {}
    categories.value = catsRes.data || []
    await loadQuickCodes()

    const typeValues = contractTypes.value.map(item => item.value)
    const lastType = localStorage.getItem(LAST_SELECTED_TYPE_KEY)
    let initialType: string | null = null
    if (lastType && typeValues.includes(lastType)) {
      initialType = lastType
    } else {
      initialType = typeValues.find(type => getFieldCount(type) > 0) || typeValues[0] || null
    }
    if (initialType) {
      await selectType(initialType)
    }
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
  
  document.addEventListener('keydown', handleKeyDown)
  nextTick(initFieldRowDrag)
})

onUnmounted(() => {
  if (presetRefreshTimer) {
    window.clearTimeout(presetRefreshTimer)
    presetRefreshTimer = null
  }
  if (presetSuccessToastTimer) {
    window.clearTimeout(presetSuccessToastTimer)
    presetSuccessToastTimer = null
  }
  presetSuccessCount.value = 0
  fieldSortable?.destroy()
  fieldSortable = null
  document.removeEventListener('keydown', handleKeyDown)
})
</script>

<style scoped lang="scss">
.type-field-config-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    flex-wrap: wrap;
    gap: 16px;
    
    .page-title {
      font-size: 24px;
      font-weight: 600;
      background: var(--primary-gradient);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      margin: 0;
      max-width: 100%;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    
    .header-actions {
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
      min-width: 0;
    }
  }
  
  .export-btn, .import-btn, .init-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    border-radius: 8px;
    font-weight: 500;
    transition: all 0.3s ease;
    
    .el-icon {
      font-size: 16px;
    }
    
    &:hover {
      transform: translateY(-1px);
    }

    max-width: 220px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .export-btn {
    background: var(--bg-card);
    border: 1px solid var(--border-color);
    color: var(--text-primary);
    
    &:hover {
      border-color: var(--primary);
      color: var(--primary);
    }
  }
  
  .import-btn {
    background: var(--bg-card);
    border: 1px solid var(--border-color);
    color: var(--text-primary);
    
    &:hover {
      border-color: var(--success);
      color: var(--success);
    }
  }
  
  .init-btn {
    background: var(--bg-card);
    border: 1px solid var(--border-color);
    color: var(--text-primary);
    
    &:hover {
      border-color: var(--warning);
      color: var(--warning);
    }
  }
}

.gradient-btn {
  background: var(--primary-gradient) !important;
  border: none !important;
}

.type-grid {
  .section-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
    color: var(--text-primary);
  }
}

.type-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.type-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    border-color: var(--primary);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
    transform: translateY(-2px);
  }
  
  .type-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 20px;
    font-weight: bold;
  }
  
  .type-info {
    flex: 1;
    
    .type-name {
      font-size: 14px;
      font-weight: 600;
      color: var(--text-primary);
    }
    
    .type-count {
      font-size: 12px;
      color: var(--text-secondary);
      margin-top: 4px;
    }
  }
}

.field-config {
  .config-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 24px;
    flex-wrap: wrap;
    
    .config-title {
      font-size: 18px;
      margin: 0;
      min-width: 0;
      max-width: 100%;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      
      .type-badge {
        padding: 4px 12px;
        border-radius: 12px;
        color: white;
        font-size: 14px;
        margin-right: 8px;
      }
    }
    
    .field-count {
      margin-left: auto;
    }
  }
  
  .field-key-code {
    font-family: 'Monaco', 'Menlo', monospace;
    font-size: 12px;
    background: var(--bg-hover);
    padding: 2px 6px;
    border-radius: 4px;
    color: var(--primary);
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
    flex-wrap: wrap;
  }
  
  .filter-item-wide {
    flex: 1;
    min-width: 240px;
    
    :deep(.el-input__wrapper) {
      border-radius: 8px;
    }
  }
  
  .filter-actions {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
    flex-wrap: wrap;
  }
  
  .table-section {
    background: var(--bg-card);
    border: 1px solid var(--border-color);
    border-radius: 12px;
    overflow: hidden;
    
    .el-table {
      --el-table-border-color: #f0f0f0;
      --el-table-header-bg-color: #fafafa;
      --el-table-cell-padding: 12px 8px;
    }
    
    .add-row {
      padding: 12px 16px;
      border-bottom: 1px solid #f0f0f0;
      
      .add-buttons {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
      }

      :deep(.el-button.is-disabled) {
        opacity: 0.65;
      }
    }
    
    .pagination-section {
      padding: 16px;
      border-top: 1px solid #f0f0f0;
      display: flex;
      justify-content: flex-end;
    }
    
    .inline-add-row {
      display: flex;
      gap: 8px;
      align-items: center;
      padding: 12px 16px;
      background: #f8f9fa;
      border-bottom: 1px solid #f0f0f0;
      flex-wrap: wrap;
    }
    
    .preset-section {
      padding: 12px 16px;
      background: #fafafa;
      border-bottom: 1px solid #f0f0f0;
      
      .preset-header {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 12px;
        flex-wrap: wrap;
        
        .preset-title {
          font-weight: 600;
          font-size: 14px;
          color: var(--text-secondary);
        }
      }
      
      .preset-list {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        max-height: 150px;
        overflow-y: auto;
        
        .preset-tag {
          cursor: pointer;
          
          &:not(.is-info):hover {
            background: var(--primary);
            color: white;
          }
        }
      }
    }
  }
}

:deep(.el-table) {
  th .cell {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .el-input, .el-select {
    width: 100%;
  }
  
  .el-input .el-input__inner {
    padding: 0 8px;
  }
}

.quick-code-preview {
  font-size: 12px;
  color: var(--text-secondary);
  background: var(--bg-hover);
  padding: 2px 8px;
  border-radius: 4px;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.quick-code-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  
  .quick-code-items {
    font-size: 11px;
    color: var(--text-secondary);
    line-height: 1.3;
    max-height: 40px;
    overflow: hidden;
  }
}

.text-muted {
  color: var(--text-secondary);
  font-size: 12px;
}

.order-buttons {
  display: flex;
  gap: 4px;
  align-items: center;
  
  .el-button {
    &:hover:not(:disabled) {
      transform: scale(1.1);
      transition: transform 0.2s;
    }
  }
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
}
</style>
