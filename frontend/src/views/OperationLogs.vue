<template>
  <div class="operation-logs">
    <div class="page-header">
      <h1 class="page-title">{{ t('menu.operationLogs') }}</h1>
      <div class="header-actions">
        <el-button type="primary" @click="handleExport" class="gradient-btn">
          <el-icon><Download /></el-icon>
          导出Excel
        </el-button>
      </div>
    </div>
    
    <!-- 搜索筛选 -->
    <div class="filter-section">
      <div class="filter-row">
        <div class="filter-item" style="flex: 1;">
          <el-input
            v-model="query.keyword"
            placeholder="搜索操作描述、IP、操作者..."
            clearable
            @keyup.enter="fetchData"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <div class="filter-item" style="width: 140px;">
          <el-select v-model="query.module" clearable placeholder="模块" @change="fetchData">
            <el-option label="全部模块" value="" />
            <el-option label="合同管理" value="合同管理" />
            <el-option label="用户管理" value="用户管理" />
            <el-option label="模板管理" value="模板管理" />
            <el-option label="提醒管理" value="提醒管理" />
            <el-option label="续约管理" value="续约管理" />
            <el-option label="认证授权" value="认证授权" />
            <el-option label="其他" value="其他" />
          </el-select>
        </div>
        <div class="filter-item" style="width: 140px;">
          <el-select v-model="query.operation" clearable placeholder="操作类型" @change="fetchData">
            <el-option label="全部操作" value="" />
            <el-option label="创建" value="CREATE" />
            <el-option label="更新" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
            <el-option label="读取" value="READ" />
            <el-option label="登录" value="LOGIN" />
            <el-option label="上传" value="UPLOAD" />
            <el-option label="下载" value="DOWNLOAD" />
            <el-option label="审批通过" value="APPROVE" />
            <el-option label="拒绝" value="REJECT" />
            <el-option label="签署" value="SIGN" />
            <el-option label="归档" value="ARCHIVE" />
            <el-option label="提交审批" value="SUBMIT" />
            <el-option label="收藏" value="FAVORITE" />
            <el-option label="复制" value="COPY" />
            <el-option label="批量操作" value="BATCH" />
            <el-option label="终止" value="TERMINATE" />
            <el-option label="分析" value="ANALYZE" />
          </el-select>
        </div>
        <div class="filter-item" style="width: 160px;">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            @change="handleDateChange"
          />
        </div>
        <div class="filter-actions">
          <el-button type="primary" @click="fetchData" class="gradient-btn">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 日志列表 -->
    <el-card shadow="hover" class="table-card">
      <div class="table-info">
        共 {{ total }} 条记录
      </div>
      
      <el-table :data="logs" v-loading="loading" style="width: 100%" @row-click="showDetail">
        <el-table-column prop="module" label="模块" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.module }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operation" label="操作类型" width="90">
          <template #default="{ row }">
            <el-tag :type="getOperationType(row.operation)" size="small">
              {{ formatOperation(row.operation) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="操作描述" min-width="280" show-overflow-tooltip />
        <el-table-column prop="operatorName" label="操作者" width="100">
          <template #default="{ row }">
            <span class="operator-name">{{ row.operatorName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP地址" width="130">
          <template #default="{ row }">
            <el-tag type="info" size="small" class="ip-tag">{{ row.ip || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="操作时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="详情" width="70">
          <template #default="{ row }">
            <el-link type="primary" @click.stop="showDetail(row)">详情</el-link>
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
    <el-dialog v-model="showDetailDialog" title="操作详情" width="700px">
      <el-descriptions v-if="currentLog" :column="1" border>
        <el-descriptions-item label="模块">{{ currentLog.module }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="getOperationType(currentLog.operation)">{{ formatOperation(currentLog.operation) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作描述">{{ currentLog.description }}</el-descriptions-item>
        <el-descriptions-item label="操作者">{{ currentLog.operatorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ip || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ formatTime(currentLog.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="详细信息">
          <pre class="detail-json">{{ formatDetail(currentLog.detail) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getOperationLogs } from '@/api/extra'
import { Search, Download, Refresh } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

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
    ARCHIVE: '',
    SUBMIT: 'warning',
    FAVORITE: 'warning',
    COPY: 'primary',
    BATCH: 'warning',
    TERMINATE: 'danger',
    ANALYZE: 'info'
  }
  return map[operation] || ''
}

const formatOperation = (operation: string) => {
  const map: Record<string, string> = {
    CREATE: '创建',
    UPDATE: '更新',
    DELETE: '删除',
    READ: '读取',
    LOGIN: '登录',
    UPLOAD: '上传',
    DOWNLOAD: '下载',
    APPROVE: '审批通过',
    REJECT: '拒绝',
    SIGN: '签署',
    ARCHIVE: '归档',
    SUBMIT: '提交审批',
    FAVORITE: '收藏',
    COPY: '复制',
    BATCH: '批量操作',
    TERMINATE: '终止',
    ANALYZE: '分析',
    OTHER: '其他'
  }
  return map[operation] || operation
}

const formatTime = (time: string) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN', {
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
    ElMessage.warning('没有数据可导出')
    return
  }
  
  try {
    const exportData = logs.value.map(log => ({
      '模块': log.module,
      '操作类型': formatOperation(log.operation),
      '操作描述': log.description,
      '操作者': log.operatorName || '-',
      'IP地址': log.ip || '-',
      '操作时间': formatTime(log.createdAt)
    }))
    
    const ws = XLSX.utils.json_to_sheet(exportData)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, '操作日志')
    
    const fileName = `操作日志_${new Date().toLocaleDateString('zh-CN').replace(/\//g, '-')}.xlsx`
    XLSX.writeFile(wb, fileName)
    
    ElMessage.success('导出成功！')
  } catch (error) {
    ElMessage.error('导出失败')
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
    
    .page-title { 
      font-size: 24px; 
      font-weight: 700; 
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text; 
      margin: 0;
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
  }
  
  .filter-actions {
    display: flex;
    gap: 12px;
    margin-left: auto;
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
    }
  }
  
  .pagination-wrap { 
    margin-top: 20px; 
    display: flex; 
    justify-content: flex-end; 
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
}
</style>
