<template>
  <div class="reminder-rules-page">
    <div class="page-header">
      <h1 class="page-title">{{ $t('contract.reminderRules') }}</h1>
      <el-button type="primary" class="gradient-btn" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        {{ $t('contract.createRule') }}
      </el-button>
    </div>
    
    <div class="table-section" v-loading="loading">
      <div class="section-title">
        <el-icon><User /></el-icon>
        <span>{{ $t('contract.myRules') }}</span>
        <el-tag size="small" type="info">{{ myRules.length }}</el-tag>
      </div>
      <el-table :data="paginatedMyRules" style="width: 100%" v-if="myRules.length > 0">
        <el-table-column prop="name" :label="$t('contract.ruleName')" min-width="140" show-overflow-tooltip />
        <el-table-column prop="contractType" :label="$t('contract.applicableType')" width="110" show-overflow-tooltip>
          <template #default="{ row }">
            {{ formatType(row.contractType) || $t('contract.allTypes') }}
          </template>
        </el-table-column>
        <el-table-column prop="minAmount" :label="$t('contract.amountRange')" width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.minAmount || row.maxAmount">
              {{ getCurrencySymbol(row.currency || DEFAULT_CURRENCY) }}{{ formatAmount(row.minAmount) }} - {{ getCurrencySymbol(row.currency || DEFAULT_CURRENCY) }}{{ formatAmount(row.maxAmount) }}
            </span>
            <span v-else>{{ $t('contract.noLimit') }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remindDays" :label="$t('contract.remindDays')" width="110" show-overflow-tooltip>
          <template #default="{ row }">
            {{ $t('contract.daysBefore', { days: row.remindDays }) }}
          </template>
        </el-table-column>
        <el-table-column prop="isEnabled" :label="$t('contract.status')" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            <el-switch 
              :model-value="row.isEnabled" 
              @change="handleToggle(row)"
              active-text=""
              inactive-text=""
            />
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
      <el-empty v-else :description="$t('contract.noRules')" />
      <div class="pagination-wrap" v-if="myRules.length > 4">
        <el-pagination
          v-model:current-page="myPage"
          v-model:page-size="myPageSize"
          :total="myRules.length"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleMySizeChange"
          @current-change="handleMyPageChange"
        />
      </div>
    </div>
    
    <div class="table-section" v-loading="loading">
      <div class="section-title">
        <el-icon><Connection /></el-icon>
        <span>{{ $t('contract.teamRules') }}</span>
        <el-tag size="small" type="info">{{ teamRules.length }}</el-tag>
      </div>
      <el-table :data="paginatedTeamRules" style="width: 100%" v-if="teamRules.length > 0">
        <el-table-column prop="name" :label="$t('contract.ruleName')" min-width="140" show-overflow-tooltip />
        <el-table-column prop="contractType" :label="$t('contract.applicableType')" width="110" show-overflow-tooltip>
          <template #default="{ row }">
            {{ formatType(row.contractType) || $t('contract.allTypes') }}
          </template>
        </el-table-column>
        <el-table-column prop="minAmount" :label="$t('contract.amountRange')" width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.minAmount || row.maxAmount">
              {{ getCurrencySymbol(row.currency || DEFAULT_CURRENCY) }}{{ formatAmount(row.minAmount) }} - {{ getCurrencySymbol(row.currency || DEFAULT_CURRENCY) }}{{ formatAmount(row.maxAmount) }}
            </span>
            <span v-else>{{ $t('contract.noLimit') }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remindDays" :label="$t('contract.remindDays')" width="110" show-overflow-tooltip>
          <template #default="{ row }">
            {{ $t('contract.daysBefore', { days: row.remindDays }) }}
          </template>
        </el-table-column>
        <el-table-column prop="isEnabled" :label="$t('contract.status')" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag size="small" :type="row.isEnabled ? 'success' : 'info'">
              {{ row.isEnabled ? $t('common.enable') : $t('common.disable') }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else :description="$t('contract.noRules')" />
      <div class="pagination-wrap" v-if="teamRules.length > 4">
        <el-pagination
          v-model:current-page="teamPage"
          v-model:page-size="teamPageSize"
          :total="teamRules.length"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleTeamSizeChange"
          @current-change="handleTeamPageChange"
        />
      </div>
    </div>
    
    <el-dialog v-model="showCreateDialog" :title="$t('contract.createRule')" width="500px">
      <el-form :model="ruleForm" label-width="100px">
        <el-form-item :label="$t('contract.ruleName')" required>
          <el-input v-model="ruleForm.name" :placeholder="$t('contract.ruleNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('contract.applicableType')">
          <el-select v-model="ruleForm.contractType" :placeholder="$t('contract.selectType')" clearable multiple @change="handleTypeChange">
            <el-option :label="$t('contract.allTypes')" value="ALL" />
            <el-option v-for="cat in categories" :key="cat.code" :label="locale === 'en' && cat.nameEn ? cat.nameEn : cat.name" :value="cat.code" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('contract.amountRange')">
          <el-input-number v-model="ruleForm.minAmount" :min="0" :precision="2" style="width: 45%" />
          <span style="margin: 0 8px">-</span>
          <el-input-number v-model="ruleForm.maxAmount" :min="0" :precision="2" style="width: 45%" />
        </el-form-item>
        <el-form-item :label="$t('contract.remindDays')" required>
          <el-input-number v-model="ruleForm.remindDays" :min="1" :max="365" />
          <span style="margin-left: 8px">{{ $t('contract.daysUnit') }}</span>
        </el-form-item>
        <el-form-item :label="$t('contract.visibility')">
          <el-radio-group v-model="ruleForm.isPublic">
            <el-radio :value="true">{{ $t('contract.teamVisible') }}</el-radio>
            <el-radio :value="false">{{ $t('contract.onlyMe') }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleCreate">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="showEditDialog" :title="$t('common.edit')" width="500px">
      <el-form :model="ruleForm" label-width="100px">
        <el-form-item :label="$t('contract.ruleName')" required>
          <el-input v-model="ruleForm.name" />
        </el-form-item>
        <el-form-item :label="$t('contract.applicableType')">
          <el-select v-model="ruleForm.contractType" clearable multiple @change="handleTypeChange">
            <el-option :label="$t('contract.allTypes')" value="ALL" />
            <el-option v-for="cat in categories" :key="cat.code" :label="locale === 'en' && cat.nameEn ? cat.nameEn : cat.name" :value="cat.code" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('contract.amountRange')">
          <el-input-number v-model="ruleForm.minAmount" :min="0" :precision="2" style="width: 45%" />
          <span style="margin: 0 8px">-</span>
          <el-input-number v-model="ruleForm.maxAmount" :min="0" :precision="2" style="width: 45%" />
        </el-form-item>
        <el-form-item :label="$t('contract.remindDays')" required>
          <el-input-number v-model="ruleForm.remindDays" :min="1" :max="365" />
          <span style="margin-left: 8px">{{ $t('contract.daysUnit') }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleUpdate">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyReminderRules, createReminderRule, updateReminderRule, deleteReminderRule, toggleReminderRule } from '@/api/reminderRule'
import { getContractCategories } from '@/api/contractCategory'
import { Plus, Edit, Delete, User, Connection, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { DEFAULT_CURRENCY, formatAmountByLocale, getCurrencySymbol } from '@/utils/currency'

const { t, locale } = useI18n()
const userStore = useUserStore()

const loading = ref(false)
const allRules = ref<any[]>([])
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const editingRuleId = ref<number | null>(null)
const categories = ref<any[]>([])

const myRules = computed(() => {
  return allRules.value.filter(rule => rule.creatorId === userStore.userInfo?.id)
})

const teamRules = computed(() => {
  return allRules.value.filter(rule => rule.isPublic)
})

const myPage = ref(1)
const myPageSize = ref(10)
const teamPage = ref(1)
const teamPageSize = ref(10)

const paginatedMyRules = computed(() => {
  const start = (myPage.value - 1) * myPageSize.value
  return myRules.value.slice(start, start + myPageSize.value)
})

const paginatedTeamRules = computed(() => {
  const start = (teamPage.value - 1) * teamPageSize.value
  return teamRules.value.slice(start, start + teamPageSize.value)
})

const handleMySizeChange = () => {
  myPage.value = 1
}

const handleMyPageChange = () => {}

const handleTeamSizeChange = () => {
  teamPage.value = 1
}

const handleTeamPageChange = () => {}

const ruleForm = reactive({
  name: '',
  contractType: [] as string[],
  minAmount: undefined as number | undefined,
  maxAmount: undefined as number | undefined,
  remindDays: 30,
  isPublic: true
})

const formatAmount = (amount: number) => {
  return formatAmountByLocale(amount, locale.value)
}

const formatType = (type: string | string[]) => {
  if (Array.isArray(type)) {
    return type.map(t => {
      const cat = categories.value.find(c => c.code === t)
      if (cat) {
        return locale.value === 'en' && cat.nameEn ? cat.nameEn : (cat.name || t)
      }
      return t
    }).join(', ')
  }
  if (!type) return t('contract.allTypes')
  const cat = categories.value.find(c => c.code === type)
  if (cat) {
    return locale.value === 'en' && cat.nameEn ? cat.nameEn : (cat.name || type)
  }
  return type
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyReminderRules()
    allRules.value = res.data?.list || []
  } catch (error) {
    ElMessage.error(t('common.error'))
  } finally {
    loading.value = false
  }
}

const handleCreate = async () => {
  if (!ruleForm.name.trim()) {
    ElMessage.warning(t('contract.ruleNameRequired'))
    return
  }
  try {
    const data = {
      name: ruleForm.name,
      contractType: ruleForm.contractType,
      minAmount: ruleForm.minAmount,
      maxAmount: ruleForm.maxAmount,
      remindDays: ruleForm.remindDays,
      isPublic: ruleForm.isPublic
    }
    await createReminderRule(data)
    ElMessage.success(t('common.success'))
    showCreateDialog.value = false
    resetForm()
    fetchData()
  } catch (error) {
    ElMessage.error((error as any)?.message || t('common.error'))
  }
}

const handleEdit = (rule: any) => {
  editingRuleId.value = rule.id
  ruleForm.name = rule.name
  ruleForm.contractType = rule.contractType ? (Array.isArray(rule.contractType) ? rule.contractType : [rule.contractType]) : []
  ruleForm.minAmount = rule.minAmount
  ruleForm.maxAmount = rule.maxAmount
  ruleForm.remindDays = rule.remindDays
  showEditDialog.value = true
}

const handleUpdate = async () => {
  if (!editingRuleId.value) return
  try {
    const data = {
      name: ruleForm.name,
      contractType: ruleForm.contractType,
      minAmount: ruleForm.minAmount,
      maxAmount: ruleForm.maxAmount,
      remindDays: ruleForm.remindDays
    }
    await updateReminderRule(editingRuleId.value, data)
    ElMessage.success(t('common.success'))
    showEditDialog.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || t('common.error'))
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm(t('contract.confirmDeleteRule'), t('common.warning'), { type: 'warning' })
    await deleteReminderRule(id)
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
    case 'delete':
      handleDelete(row.id)
      break
  }
}

const handleToggle = async (rule: any) => {
  try {
    await toggleReminderRule(rule.id)
    ElMessage.success(t('common.success'))
    fetchData()
  } catch (error) {
    ElMessage.error((error as any)?.message || t('common.error'))
  }
}

const handleTypeChange = (values: string[]) => {
  if (values.includes('ALL')) {
    ruleForm.contractType = categories.value.map(c => c.code)
  }
}

const handleAdd = () => {
  resetForm()
  showCreateDialog.value = true
}

const resetForm = () => {
  ruleForm.name = ''
  ruleForm.contractType = []
  ruleForm.minAmount = undefined
  ruleForm.maxAmount = undefined
  ruleForm.remindDays = 30
  ruleForm.isPublic = true
}

const loadCategories = async () => {
  try {
    const res = await getContractCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
}

onMounted(async () => {
  await loadCategories()
  fetchData()
})
</script>

<style scoped lang="scss">
.reminder-rules-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .page-title {
      font-size: 24px;
      font-weight: 600;
      background: var(--primary-gradient);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      margin: 0;
    }
  }
  
  .gradient-btn {
    background: var(--primary-gradient) !important;
    border: none !important;
  }
  
  .table-section {
    background: var(--bg-card);
    border: 1px solid var(--border-color);
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 24px;
    overflow: hidden;
    
    .section-title {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 16px;
      font-size: 16px;
      font-weight: 600;
      color: var(--text-primary);
      
      .el-icon {
        color: var(--primary);
      }
    }
    
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
      padding: 16px 0 0;
    }
    
    .el-empty {
      padding: 40px 0;
    }
  }
}

:deep(.el-input-number) {
  .el-input__inner {
    text-align: left;
  }
}
</style>
