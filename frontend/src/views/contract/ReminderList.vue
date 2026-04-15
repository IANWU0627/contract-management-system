<template>
  <div class="reminders">
    <div class="page-header">
      <h1 class="page-title">{{ t('menu.reminders') }}</h1>
      <el-button type="primary" @click="handleCheck">
        <el-icon><Refresh /></el-icon>
        {{ t('reminder.check') }}
      </el-button>
    </div>
    
    <!-- 搜索筛选 -->
    <div class="filter-section">
      <div class="filter-row">
        <div class="filter-item filter-item-wide" style="flex: 1;">
          <el-input
            v-model="query.keyword"
            :placeholder="$t('contract.placeholder.keyword')"
            clearable
            @keyup.enter="fetchData"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <div class="filter-item" style="width: 160px;">
          <el-select v-model="query.status" clearable :placeholder="$t('common.status')" @change="fetchData">
            <el-option :label="$t('common.all')" value="" />
            <el-option :label="$t('reminder.unread')" :value="0" />
            <el-option :label="$t('reminder.read')" :value="1" />
          </el-select>
        </div>
        <div class="filter-actions">
          <el-button type="primary" @click="fetchData" class="gradient-btn">
            <el-icon><Search /></el-icon>
            {{ $t('common.search') }}
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            {{ $t('common.reset') }}
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 提醒列表 -->
    <el-card shadow="hover">
      <el-table :data="reminders" v-loading="loading" style="width: 100%">
        <el-table-column prop="contractNo" :label="$t('contract.no')" width="180" show-overflow-tooltip />
        <el-table-column prop="contractTitle" :label="$t('contract.name')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="expireDate" :label="$t('reminder.expireDate')" width="120">
          <template #default="{ row }">
            <span :class="{ 'expire-today': row.remindDays === 0, 'expire-soon': row.remindDays > 0 && row.remindDays <= 7 }">
              {{ row.expireDate }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="remindDays" :label="$t('reminder.daysLeft')" width="100">
          <template #default="{ row }">
            <el-tag :type="getDaysTagType(row.remindDays)">
              {{ row.remindDays === 0 ? t('reminder.expireToday') : `${row.remindDays}${t('reminder.daysLater')}` }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('common.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : 'success'">
              {{ row.status === 0 ? t('reminder.unread') : t('reminder.read') }}
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
                  <el-dropdown-item command="view">{{ $t('common.view') }}</el-dropdown-item>
                  <el-dropdown-item command="read" v-if="row.status === 0">{{ $t('reminder.markRead') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="!loading && reminders.length === 0" :description="$t('reminder.noReminders')" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getMyReminders, markReminderRead, triggerReminderCheck } from '@/api/reminder'
import { Refresh, View, Check, ArrowDown, Search } from '@element-plus/icons-vue'

const { t } = useI18n()

const router = useRouter()

const loading = ref(false)
const reminders = ref<any[]>([])

const query = reactive({
  keyword: '',
  status: ''
})

const handleReset = () => {
  query.keyword = ''
  query.status = ''
  fetchData()
}

const getDaysTagType = (days: number) => {
  if (days === 0) return 'danger'
  if (days <= 7) return 'warning'
  return 'info'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyReminders()
    // 处理后端返回的数据结构 { list: [...], total: 0, unreadCount: 0 }
    if (res.data && Array.isArray(res.data.list)) {
      reminders.value = res.data.list
    } else {
      reminders.value = []
    }
  } catch (error) {
    reminders.value = []
  }
  finally { loading.value = false }
}

const handleView = (id: number) => {
  router.push(`/contracts/${id}`)
}

const handleAction = (command: string, row: any) => {
  switch (command) {
    case 'view':
      handleView(row.contractId)
      break
    case 'read':
      handleRead(row.id)
      break
  }
}

const handleRead = async (id: number) => {
  try {
    await markReminderRead(id)
    ElMessage.success(t('common.success'))
    fetchData()
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const handleCheck = async () => {
  try {
    await triggerReminderCheck()
    ElMessage.success(t('reminder.checkCompleted'))
    fetchData()
  } catch (error) {
    ElMessage.error(t('reminder.checkFailed'))
  }
}

onMounted(() => { fetchData() })
</script>

<style scoped lang="scss">
.gradient-btn {
  background: var(--primary-gradient) !important;
  border: none !important;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
  }
  
  .el-icon {
    margin-right: 4px;
  }
}

/* 筛选区 */
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

.reminders {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    gap: 12px;
    flex-wrap: wrap;
    min-width: 0;
    
    .page-title {
      font-size: 24px;
      font-weight: 700;
      margin: 0;
      background: var(--primary-gradient);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      max-width: 100%;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
  
  .expire-today {
    color: #F56C6C;
    font-weight: bold;
  }
  
  .expire-soon {
    color: #E6A23C;
  }

  :deep(.el-table th .cell) {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}
</style>
