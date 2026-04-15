<template>
  <div class="operation-logs">
    <div class="page-header">
      <h1 class="page-title">{{ t('menu.operationLogs') }}</h1>
      <div class="header-actions">
        <el-button type="primary" @click="handleExport" class="gradient-btn">
          <el-icon><Download /></el-icon>
          {{ t('log.exportExcel') }}
        </el-button>
      </div>
    </div>
    
    <!-- 搜索筛选 -->
    <div class="filter-section">
      <div class="filter-row">
        <div class="filter-item" style="flex: 1;">
          <el-input
            v-model="query.keyword"
            :placeholder="t('log.searchPlaceholder')"
            clearable
            @keyup.enter="fetchData"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <div class="filter-item" style="width: 140px;">
          <el-select v-model="query.module" clearable :placeholder="t('log.modulePlaceholder')" @change="fetchData">
            <el-option :label="t('log.allModules')" value="" />
            <el-option v-for="item in moduleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </div>
        <div class="filter-item" style="width: 140px;">
          <el-select v-model="query.operation" clearable :placeholder="t('log.operationPlaceholder')" @change="fetchData">
            <el-option :label="t('log.allOperations')" value="" />
            <el-option v-for="item in operationOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </div>
        <div class="filter-item" style="width: 160px;">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            :range-separator="t('common.to')"
            :start-placeholder="t('log.startTime')"
            :end-placeholder="t('log.endTime')"
            value-format="YYYY-MM-DD HH:mm:ss"
            @change="handleDateChange"
          />
        </div>
        <div class="filter-actions">
          <el-button type="primary" @click="fetchData" class="gradient-btn">
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
    
    <!-- 日志列表 -->
    <el-card shadow="hover" class="table-card">
      <div class="table-info">
        {{ t('log.totalRecords', { count: total }) }}
      </div>
      
      <el-table :data="logs" v-loading="loading" style="width: 100%" @row-click="showDetail">
        <el-table-column prop="module" :label="t('log.module')" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ formatModule(row.module) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operation" :label="t('log.operation')" width="90">
          <template #default="{ row }">
            <el-tag :type="getOperationType(row.operation)" size="small">
              {{ formatOperation(row.operation) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" :label="t('log.description')" min-width="280" show-overflow-tooltip />
        <el-table-column prop="operatorName" :label="t('log.operator')" width="100">
          <template #default="{ row }">
            <span class="operator-name">{{ row.operatorName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="ip" :label="t('log.ip')" width="130">
          <template #default="{ row }">
            <el-tag type="info" size="small" class="ip-tag">{{ row.ip || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="t('log.time')" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('log.detail')" width="70">
          <template #default="{ row }">
            <el-link type="primary" @click.stop="showDetail(row)">{{ t('log.detail') }}</el-link>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="fetchData"
          @size-change="fetchData"
        />
      </div>
    </el-card>
    
    <!-- 详情对话框 -->
    <el-dialog v-model="showDetailDialog" :title="t('log.detailTitle')" width="700px">
      <el-descriptions v-if="currentLog" :column="1" border>
        <el-descriptions-item :label="t('log.module')">{{ formatModule(currentLog.module) }}</el-descriptions-item>
        <el-descriptions-item :label="t('log.operation')">
          <el-tag :type="getOperationType(currentLog.operation)">{{ formatOperation(currentLog.operation) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="t('log.description')">{{ currentLog.description }}</el-descriptions-item>
        <el-descriptions-item :label="t('log.operator')">{{ currentLog.operatorName || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('log.ip')">{{ currentLog.ip || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('log.time')">{{ formatTime(currentLog.createdAt) }}</el-descriptions-item>
        <el-descriptions-item :label="t('log.detail')">
          <pre class="detail-json">{{ formatDetail(currentLog.detail) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="showDetailDialog = false">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { getOperationLogs } from '@/api/extra'
import { Search, Download, Refresh } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import { ElMessage } from 'element-plus'

const { t, locale } = useI18n()

const loading = ref(false)
const logs = ref<any[]>([])
const total = ref(0)
const showDetailDialog = ref(false)
const currentLog = ref<any>(null)
const dateRange = ref<string[] | null>(null)

const query = reactive({
  module: '',
  operation: '',
  keyword: '',
  startDate: '',
  endDate: '',
  page: 1,
  pageSize: 20
})

const moduleOptions = computed(() => [
  { value: '合同管理', label: t('settings.filterContract') },
  { value: '用户管理', label: t('settings.filterUser') },
  { value: '模板管理', label: t('settings.filterTemplate') },
  { value: '提醒管理', label: t('settings.filterReminder') },
  { value: '续约管理', label: t('settings.filterRenewal') },
  { value: '认证授权', label: t('settings.filterAuth') },
  { value: '其他', label: t('settings.filterSystem') }
])

const operationOptions = computed(() => [
  { value: 'CREATE', label: t('settings.actionCreate') },
  { value: 'UPDATE', label: t('settings.actionUpdate') },
  { value: 'DELETE', label: t('settings.actionDelete') },
  { value: 'READ', label: t('settings.actionRead') },
  { value: 'LOGIN', label: t('settings.actionLogin') },
  { value: 'UPLOAD', label: t('settings.actionUpload') },
  { value: 'DOWNLOAD', label: t('settings.actionDownload') },
  { value: 'APPROVE', label: t('settings.actionApprove') },
  { value: 'REJECT', label: t('settings.actionReject') },
  { value: 'SIGN', label: t('settings.actionSign') },
  { value: 'ARCHIVE', label: t('settings.actionArchive') },
  { value: 'SUBMIT', label: t('settings.actionSubmit') },
  { value: 'FAVORITE', label: t('settings.actionFavorite') },
  { value: 'COPY', label: t('settings.actionCopy') },
  { value: 'BATCH', label: t('settings.actionBatch') },
  { value: 'TERMINATE', label: t('settings.actionTerminate') },
  { value: 'ANALYZE', label: t('settings.actionAnalyze') }
])

const getOperationType = (operation: string) => {
  const map: Record<string, any> = {
    CREATE: 'success',
    UPDATE: 'primary',
    DELETE: 'danger',
    READ: 'info',
    LOGIN: 'warning',
    UPLOAD: 'success',
    DOWNLOAD: 'info',
    APPROVE: 'success',
    REJECT: 'danger',
    SIGN: 'success',
    ARCHIVE: 'info',
    SUBMIT: 'warning',
    FAVORITE: 'warning',
    COPY: 'primary',
    BATCH: 'warning',
    TERMINATE: 'danger',
    ANALYZE: 'info'
  }
  return map[operation] || 'info'
}

const formatOperation = (operation: string) => {
  const map: Record<string, string> = {
    CREATE: t('settings.actionCreate'),
    UPDATE: t('settings.actionUpdate'),
    DELETE: t('settings.actionDelete'),
    READ: t('settings.actionRead'),
    LOGIN: t('settings.actionLogin'),
    UPLOAD: t('settings.actionUpload'),
    DOWNLOAD: t('settings.actionDownload'),
    APPROVE: t('settings.actionApprove'),
    REJECT: t('settings.actionReject'),
    SIGN: t('settings.actionSign'),
    ARCHIVE: t('settings.actionArchive'),
    SUBMIT: t('settings.actionSubmit'),
    FAVORITE: t('settings.actionFavorite'),
    COPY: t('settings.actionCopy'),
    BATCH: t('settings.actionBatch'),
    TERMINATE: t('settings.actionTerminate'),
    ANALYZE: t('settings.actionAnalyze'),
    OTHER: t('settings.filterSystem')
  }
  return map[operation] || operation
}

const formatModule = (module: string) => {
  const map: Record<string, string> = {
    '合同管理': t('settings.filterContract'),
    '用户管理': t('settings.filterUser'),
    '模板管理': t('settings.filterTemplate'),
    '提醒管理': t('settings.filterReminder'),
    '续约管理': t('settings.filterRenewal'),
    '认证授权': t('settings.filterAuth'),
    '其他': t('settings.filterSystem')
  }
  return map[module] || module || '-'
}

const formatTime = (time: string) => {
  if (!time) return ''
  return new Date(time).toLocaleString(locale.value === 'en' ? 'en-US' : 'zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const formatDetail = (detail: string) => {
  if (!detail) return '-'
  try {
    const parsed = JSON.parse(detail)
    return JSON.stringify(parsed, null, 2)
  } catch {
    return detail
  }
}

const handleDateChange = (val: string[] | null) => {
  if (val && val.length === 2) {
    query.startDate = val[0]
    query.endDate = val[1]
  } else {
    query.startDate = ''
    query.endDate = ''
  }
  fetchData()
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getOperationLogs(query)
    logs.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  query.module = ''
  query.operation = ''
  query.keyword = ''
  query.startDate = ''
  query.endDate = ''
  query.page = 1
  dateRange.value = null
  fetchData()
}

const showDetail = (row: any) => {
  currentLog.value = row
  showDetailDialog.value = true
}

const handleExport = async () => {
  if (logs.value.length === 0) {
    ElMessage.warning(t('log.noDataExport'))
    return
  }
  
  try {
    const exportData = logs.value.map(log => ({
      [t('log.module')]: formatModule(log.module),
      [t('log.operation')]: formatOperation(log.operation),
      [t('log.description')]: log.description,
      [t('log.operator')]: log.operatorName || '-',
      [t('log.ip')]: log.ip || '-',
      [t('log.time')]: formatTime(log.createdAt)
    }))
    
    const ws = XLSX.utils.json_to_sheet(exportData)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, t('log.sheetName'))
    
    const fileName = `${t('log.filePrefix')}_${new Date().toLocaleDateString(locale.value === 'en' ? 'en-US' : 'zh-CN').replace(/\//g, '-')}.xlsx`
    XLSX.writeFile(wb, fileName)
    
    ElMessage.success(t('log.exportSuccess'))
  } catch (error) {
    ElMessage.error(t('log.exportFailed'))
    console.error(error)
  }
}

onMounted(() => { fetchData() })
</script>

<style scoped lang="scss">
.operation-logs {
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
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text; 
      margin: 0;
      min-width: 0;
      max-width: 100%;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
  
  .filter-section {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 20px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
    border: 1px solid var(--border-color, #e4e7ed);
  }
  
  .filter-row {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 16px;
  }
  
  .filter-item {
    display: flex;
    align-items: center;
    min-width: 0;

    &:nth-child(1) {
      flex: 1 1 280px !important;
    }

    &:nth-child(2),
    &:nth-child(3),
    &:nth-child(4) {
      flex: 0 1 180px !important;
      width: auto !important;
    }

    :deep(.el-input),
    :deep(.el-select),
    :deep(.el-date-editor) {
      width: 100% !important;
      min-width: 0;
    }
  }
  
  .filter-actions {
    display: flex;
    gap: 12px;
    margin-left: auto;
    flex-wrap: wrap;
  }
  
  .gradient-btn {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }
  }
  
  .table-card {
    .table-info {
      color: var(--text-secondary);
      font-size: 14px;
      margin-bottom: 16px;
      max-width: 100%;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
  
  .pagination-wrap { 
    margin-top: 20px; 
    display: flex; 
    justify-content: flex-end; 
    min-width: 0;
  }
  
  .operator-name {
    font-weight: 500;
    color: #409EFF;
  }
  
  .ip-tag {
    font-family: 'Monaco', 'Menlo', monospace;
  }
  
  .detail-json {
    margin: 0;
    padding: 12px;
    background: #f5f7fa;
    border-radius: 4px;
    font-size: 12px;
    max-height: 300px;
    overflow: auto;
  }

  :deep(.el-table th .cell) {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}
</style>
