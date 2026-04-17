<template>
  <div class="clause-library">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">{{ t('clause.title') }}</h1>
        <span class="page-subtitle">{{ t('clause.subtitle') }}</span>
      </div>
      <div class="header-actions">
        <el-button @click="createTemplateFromSelected" :disabled="selectedClauses.length === 0">
          <el-icon><DocumentAdd /></el-icon>
          {{ t('clause.batchCreateTemplate') }}
          <span v-if="selectedClauses.length">({{ selectedClauses.length }})</span>
        </el-button>
        <el-button type="primary" @click="openCreateDialog" class="gradient-btn">
          <el-icon><Plus /></el-icon>
          {{ t('common.add') }}
        </el-button>
      </div>
    </div>

    <div class="filter-section">
      <div class="filter-row">
        <div class="filter-item filter-item-wide" style="flex: 1;">
          <el-input
            v-model="searchKeyword"
            :placeholder="t('clause.searchPlaceholder')"
            clearable
            @keyup.enter="loadClauses"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <div class="filter-item" style="width: 180px;">
          <el-select v-model="selectedCategory" clearable :placeholder="t('clause.category')" class="filter-select">
            <el-option v-for="item in categoryOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </div>
        <div class="filter-item" style="width: 140px;">
          <el-select v-model="selectedStatus" clearable :placeholder="t('common.status')" class="filter-select">
            <el-option :label="t('common.enabled')" :value="1" />
            <el-option :label="t('common.disabled')" :value="0" />
          </el-select>
        </div>
        <div class="filter-actions">
          <el-button type="primary" @click="loadClauses" class="gradient-btn">{{ t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ t('common.reset') }}</el-button>
        </div>
      </div>
    </div>

    <el-card shadow="hover">
      <el-table :data="clauses" v-loading="loading" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="code" :label="t('clause.code')" width="150" show-overflow-tooltip />
        <el-table-column :label="t('clause.name')" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ locale === 'en' && row.nameEn ? row.nameEn : row.name }}
          </template>
        </el-table-column>
        <el-table-column prop="nameEn" :label="t('clause.nameEn')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="category" :label="t('clause.category')" width="140" show-overflow-tooltip />
        <el-table-column :label="t('template.content')" min-width="280" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="content-preview" v-html="row.content"></div>
          </template>
        </el-table-column>
        <el-table-column prop="description" :label="t('clause.description')" min-width="200" show-overflow-tooltip />
        <el-table-column :label="t('clause.referenceUsage')" width="120" align="center">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              :disabled="getClauseReferenceCount(row) === 0"
              @click="openReferenceDialog(row)"
            >
              {{ getClauseReferenceCount(row) }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.status')" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? t('common.enabled') : t('common.disabled') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.action')" width="110" fixed="right" align="center">
          <template #default="{ row }">
            <el-dropdown @command="(command) => handleAction(command, row)">
              <el-button size="small">
                {{ t('common.action') }}
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="createTemplate">{{ t('clause.createTemplate') }}</el-dropdown-item>
                  <el-dropdown-item command="references">{{ t('clause.viewReferences') }}</el-dropdown-item>
                  <el-dropdown-item command="edit">{{ t('common.edit') }}</el-dropdown-item>
                  <el-dropdown-item command="copy">{{ t('common.copy') }}</el-dropdown-item>
                  <el-dropdown-item command="delete" divided type="danger">{{ t('common.delete') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? t('common.edit') : t('common.add')" width="720px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item :label="t('clause.code')" prop="code">
          <el-input v-model="form.code" :disabled="isEdit" />
        </el-form-item>
        <el-form-item :label="t('clause.name')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="t('clause.nameEn')">
          <el-input v-model="form.nameEn" />
        </el-form-item>
        <el-form-item :label="t('clause.category')">
          <el-input v-model="form.category" />
        </el-form-item>
        <el-form-item :label="t('clause.sortOrder')">
          <el-input-number v-model="form.sortOrder" :min="0" :step="10" />
        </el-form-item>
        <el-form-item :label="t('common.status')">
          <el-switch v-model="form.statusEnabled" />
        </el-form-item>
        <el-form-item :label="t('template.content')" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="10" />
        </el-form-item>
        <el-form-item :label="t('clause.description')">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="referenceDialogVisible" :title="referenceDialogTitle" width="640px">
      <div v-if="currentReferenceTemplates.length > 0" class="reference-list">
        <div v-for="item in currentReferenceTemplates" :key="item.id" class="reference-item">
          <div class="reference-item-main">
            <div class="reference-item-name">{{ item.name }}</div>
            <div class="reference-item-meta">
              <el-tag size="small" effect="plain">{{ item.category || '-' }}</el-tag>
              <span>{{ item.updatedAt || '-' }}</span>
            </div>
          </div>
          <el-button size="small" @click="goToTemplate(item.id)">{{ t('common.view') }}</el-button>
        </div>
      </div>
      <el-empty v-else :description="t('clause.noReferences')" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus, Search, ArrowDown, DocumentAdd } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import {
  createClause,
  deleteClause,
  getClauseList,
  getClauseReferences,
  updateClause,
  type Clause,
  type ClauseReferenceTemplate,
  type ClauseReferenceSummary
} from '@/api/clause'

const { t, locale } = useI18n()
const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const referenceDialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number>()
const formRef = ref<FormInstance>()

const clauses = ref<Clause[]>([])
const selectedClauses = ref<Clause[]>([])
const clauseReferences = ref<Record<string, { count: number; templates: ClauseReferenceTemplate[] }>>({})
const currentReferenceTemplates = ref<ClauseReferenceTemplate[]>([])
const referenceDialogTitle = ref('')
const searchKeyword = ref('')
const selectedCategory = ref('')
const selectedStatus = ref<number | undefined>(1)

const form = reactive({
  code: '',
  name: '',
  nameEn: '',
  category: '',
  content: '',
  description: '',
  sortOrder: 0,
  statusEnabled: true
})

const rules = {
  code: [{ required: true, message: t('clause.codeRequired'), trigger: 'blur' }],
  name: [{ required: true, message: t('clause.nameRequired'), trigger: 'blur' }],
  content: [{ required: true, message: t('clause.contentRequired'), trigger: 'blur' }]
}

const categoryOptions = computed(() => {
  return [...new Set(clauses.value.map(item => item.category).filter(Boolean) as string[])].sort()
})

const applyRouteQuery = () => {
  const keyword = typeof route.query.keyword === 'string' ? route.query.keyword : ''
  if (keyword !== searchKeyword.value) {
    searchKeyword.value = keyword
  }
}

const resetForm = () => {
  editingId.value = undefined
  form.code = ''
  form.name = ''
  form.nameEn = ''
  form.category = ''
  form.content = ''
  form.description = ''
  form.sortOrder = 0
  form.statusEnabled = true
}

const loadClauses = async () => {
  loading.value = true
  try {
    const res = await getClauseList({
      keyword: searchKeyword.value || undefined,
      category: selectedCategory.value || undefined,
      status: selectedStatus.value
    })
    clauses.value = res.data || []
    selectedClauses.value = []
    await loadClauseReferences()

    const highlightId = Number(route.query.highlightId || 0)
    if (highlightId && route.query.open === 'edit') {
      const target = clauses.value.find(item => Number(item.id) === highlightId)
      if (target) {
        openEditDialog(target)
        router.replace({
          path: route.path,
          query: {
            ...route.query,
            open: undefined,
            highlightId: undefined
          }
        })
      }
    }
  } catch (error) {
    console.error('Failed to load clauses', error)
    ElMessage.error(t('clause.loadError'))
  } finally {
    loading.value = false
  }
}

const loadClauseReferences = async () => {
  try {
    const res = await getClauseReferences()
    const data = res.data
    if (data && typeof data === 'object' && !Array.isArray(data)) {
      const normalized: Record<string, ClauseReferenceSummary> = {}
      for (const [key, value] of Object.entries(data as unknown as Record<string, unknown>)) {
        if (!value || typeof value !== 'object') continue
        const summary = value as { count?: unknown; templates?: unknown }
        normalized[key] = {
          count: Number(summary.count || 0),
          templates: Array.isArray(summary.templates) ? (summary.templates as ClauseReferenceTemplate[]) : []
        }
      }
      clauseReferences.value = normalized
    } else {
      clauseReferences.value = {}
    }
  } catch (error) {
    console.error('Failed to load clause references', error)
    clauseReferences.value = {}
  }
}

const handleSelectionChange = (rows: Clause[]) => {
  selectedClauses.value = rows
}

const getClauseReferenceCount = (row: Clause) => {
  return clauseReferences.value[String(row.id || '')]?.count || 0
}

const openReferenceDialog = (row: Clause) => {
  currentReferenceTemplates.value = clauseReferences.value[String(row.id || '')]?.templates || []
  referenceDialogTitle.value = `${locale.value === 'en' && row.nameEn ? row.nameEn : row.name} · ${t('clause.viewReferences')}`
  referenceDialogVisible.value = true
}

const goToTemplate = (id: number) => {
  referenceDialogVisible.value = false
  router.push(`/templates/${id}`)
}

const openCreateDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (row: Clause) => {
  isEdit.value = true
  editingId.value = row.id
  form.code = row.code || ''
  form.name = row.name || ''
  form.nameEn = row.nameEn || ''
  form.category = row.category || ''
  form.content = row.content || ''
  form.description = row.description || ''
  form.sortOrder = row.sortOrder || 0
  form.statusEnabled = row.status !== 0
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  submitLoading.value = true
  try {
    const payload = {
      code: form.code.trim(),
      name: form.name.trim(),
      nameEn: form.nameEn.trim(),
      category: form.category.trim(),
      content: form.content,
      description: form.description.trim(),
      sortOrder: form.sortOrder,
      status: form.statusEnabled ? 1 : 0
    }
    if (isEdit.value && editingId.value) {
      await updateClause(editingId.value, payload)
    } else {
      await createClause(payload)
    }
    ElMessage.success(t('common.saveSuccess'))
    dialogVisible.value = false
    loadClauses()
  } catch (error) {
    console.error('Failed to save clause', error)
    ElMessage.error(t('common.saveFailed'))
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row: Clause) => {
  await ElMessageBox.confirm(t('clause.deleteConfirm'), t('common.warning'), { type: 'warning' })
  await deleteClause(row.id as number)
  ElMessage.success(t('common.deleteSuccess'))
  loadClauses()
}

const copyClause = async (row: Clause) => {
  await navigator.clipboard.writeText(row.content || '')
  ElMessage.success(t('clause.copySuccess'))
}

const handleAction = (command: string, row: Clause) => {
  switch (command) {
    case 'createTemplate':
      createTemplateFromClause(row)
      break
    case 'edit':
      openEditDialog(row)
      break
    case 'references':
      openReferenceDialog(row)
      break
    case 'copy':
      copyClause(row)
      break
    case 'delete':
      handleDelete(row)
      break
  }
}

const createTemplateFromClause = (row: Clause) => {
  const clauseName = locale.value === 'en' && row.nameEn ? row.nameEn : row.name
  router.push({
    path: '/templates/create',
    query: {
      clauseId: row.id ? String(row.id) : undefined,
      clauseCode: row.code || '',
      clauseName: clauseName || '',
      clauseCategory: row.category || '',
      clauseDescription: row.description || '',
      clauseContent: row.content || ''
    }
  })
}

const createTemplateFromSelected = () => {
  if (selectedClauses.value.length === 0) return

  const normalizedClauses = selectedClauses.value.map((item) => ({
    id: item.id,
    code: item.code || '',
    name: item.name || '',
    nameEn: item.nameEn || '',
    category: item.category || '',
    content: item.content || '',
    description: item.description || ''
  }))
  const localizedNames = normalizedClauses
    .map((item) => (locale.value === 'en' && item.nameEn ? item.nameEn : item.name))
    .filter(Boolean)
  const categorySet = [...new Set(normalizedClauses.map((item) => item.category).filter(Boolean))]
  const suggestedName = localizedNames.slice(0, 3).join(locale.value === 'en' ? ' + ' : '、')

  router.push({
    path: '/templates/create',
    query: {
      clauses: JSON.stringify(normalizedClauses),
      templateName: suggestedName
        ? `${suggestedName}${locale.value === 'en' ? ' Template' : '模板'}`
        : undefined,
      clauseCategory: categorySet.length === 1 ? categorySet[0] : undefined
    }
  })
}

const handleReset = () => {
  searchKeyword.value = ''
  selectedCategory.value = ''
  selectedStatus.value = 1
  loadClauses()
}

onMounted(() => {
  applyRouteQuery()
  loadClauses()
})

watch(
  () => route.query.keyword,
  () => {
    applyRouteQuery()
    loadClauses()
  }
)
</script>

<style scoped lang="scss">
.clause-library {
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

      .page-subtitle {
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

  .filter-select {
    width: 100%;
  }

  .filter-actions {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
  }

  .content-preview {
    color: var(--el-text-color-regular);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .reference-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .reference-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding: 12px 14px;
    border: 1px solid var(--border-color);
    border-radius: 10px;
    background: var(--bg-card);
  }

  .reference-item-main {
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .reference-item-name {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-primary);
  }

  .reference-item-meta {
    display: flex;
    align-items: center;
    gap: 8px;
    color: var(--text-secondary);
    font-size: 12px;
  }

  @media (max-width: 1100px) {
    .page-header,
    .filter-row {
      flex-wrap: wrap;
      align-items: stretch;
    }

    .header-left {
      flex-direction: column;
      align-items: flex-start;
      gap: 4px;
    }

    .filter-actions {
      width: 100%;
    }
  }
}
</style>
