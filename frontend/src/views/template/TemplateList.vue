<template>
  <div class="template-list">
    <div class="page-header">
      <h1 class="page-title">{{ $t('template.title') }}</h1>
      <el-button type="primary" class="gradient-btn" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        {{ $t('template.create') }}
      </el-button>
    </div>
    
    <!-- 筛选 -->
    <div class="filter-section">
      <div class="filter-row">
        <div class="filter-item filter-item-wide" style="flex: 1;">
          <el-input
            v-model="query.keyword"
            :placeholder="$t('template.placeholder.search')"
            clearable
            @keyup.enter="fetchData"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <div class="filter-item" style="width: 160px;">
          <el-select v-model="query.category" clearable :placeholder="$t('template.category')" @change="fetchData">
            <el-option v-for="cat in categories" :key="cat.code" :label="getCategoryName(cat.code)" :value="cat.code">
              <span :style="{ color: cat.color }">●</span> {{ getCategoryName(cat.code) }}
            </el-option>
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
    
    <!-- 模板列表 -->
    <div class="template-grid">
      <div v-for="template in templates" :key="template.id" class="template-card">
        <div class="template-header">
          <div class="template-icon" :class="getCategoryClass(template.category)">
            <el-icon><Document /></el-icon>
          </div>
          <div class="template-info">
            <h3 class="template-name">{{ template.name }}</h3>
            <div class="template-meta">
              <el-tag size="small" :style="{ background: getCategoryColor(template.category) + '20', borderColor: getCategoryColor(template.category), color: getCategoryColor(template.category) }">
                {{ getCategoryName(template.category) }}
              </el-tag>
              <span class="usage-count">{{ $t('template.usage', { count: template.usageCount }) }}</span>
            </div>
          </div>
        </div>
        
        <p class="template-desc">{{ template.description || $t('template.placeholder.noDescription') }}</p>
        
        <div class="template-footer">
          <span class="update-time">{{ $t('template.updatedAt', { time: template.updatedAt }) }}</span>
          <div class="template-actions">
            <el-tooltip :content="$t('common.view')" placement="top">
              <el-button text @click="handlePreview(template)">
                <el-icon><View /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip :content="$t('common.download')" placement="top">
              <el-button text @click="handleExport(template)">
                <el-icon><Download /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip :content="$t('template.watermark')" placement="top">
              <el-button text @click="handleWatermark(template)">
                <el-icon><Stamp /></el-icon>
              </el-button>
            </el-tooltip>
            <el-dropdown trigger="click">
              <el-button text><el-icon><MoreFilled /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleEdit(template.id)">
                    <el-icon><Edit /></el-icon> {{ $t('common.edit') }}
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleClone(template.id)">
                    <el-icon><CopyDocument /></el-icon> {{ $t('template.clone') }}
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleUse(template.id)">
                    <el-icon><Files /></el-icon> {{ $t('template.use') }}
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleDelete(template.id)">
                    <el-icon><Delete /></el-icon> {{ $t('common.delete') }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
      
      <div v-if="templates.length === 0 && !loading" class="empty-state">
        <el-empty :description="$t('template.placeholder.noTemplates')">
          <el-button type="primary" @click="handleCreate">{{ $t('template.create') }}</el-button>
        </el-empty>
      </div>
    </div>
    
    <!-- 预览弹窗 -->
    <el-dialog v-model="previewVisible" :title="$t('template.preview')" width="1000px" destroy-on-close>
      <div class="preview-header">
        <h2>{{ previewData.name }}</h2>
        <el-tag>{{ getCategoryName(previewData.category) }}</el-tag>
      </div>
      
      <el-tabs v-model="previewTab" class="preview-tabs">
        <el-tab-pane :label="$t('template.variableFill')" name="variables">
          <div class="variable-fill-form" v-if="previewVariables.length > 0">
            <div class="variable-grid">
              <div v-for="v in previewVariables" :key="v" class="variable-item">
                <label>{{ v }}</label>
                <el-input v-model="variableValues[v]" :placeholder="t('template.placeholder.inputVariable', { variable: v })" @input="updatePreviewContent" />
              </div>
            </div>
            <div class="variable-actions">
              <el-button size="small" @click="fillSampleData">{{ $t('template.fillSampleData') }}</el-button>
              <el-button size="small" @click="clearVariableValues">{{ $t('template.clearValues') }}</el-button>
            </div>
          </div>
          <div v-else class="no-variables">
            <p>{{ t('template.placeholder.noVariablesDetected') }}</p>
          </div>
        </el-tab-pane>
        <el-tab-pane :label="$t('template.previewEffect')" name="preview">
          <div class="preview-content preview-content--rich" v-if="previewData.content">
            <div class="ql-snow template-preview-quill">
              <div class="ql-editor" v-html="filledPreviewContent || previewData.content"></div>
            </div>
          </div>
          <div class="preview-content preview-empty" v-else>
            <p>{{ t('template.previewEmpty') }}</p>
          </div>
        </el-tab-pane>
      </el-tabs>
      
      <template #footer>
        <el-button @click="previewVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button @click="handleExportPdf">{{ $t('template.downloadPdf') }}</el-button>
        <el-button type="primary" @click="handleUsePreview">{{ $t('template.use') }}</el-button>
      </template>
    </el-dialog>
    
    <!-- 水印弹窗 -->
    <el-dialog v-model="watermarkVisible" :title="$t('template.watermark')" width="600px" destroy-on-close>
      <el-form :model="watermarkForm" label-width="100px">
        <el-form-item :label="$t('template.watermarkType')">
          <el-radio-group v-model="watermarkForm.type">
            <el-radio value="text">{{ $t('template.watermarkTypeText') }}</el-radio>
            <el-radio value="image">{{ $t('template.watermarkTypeImage') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <template v-if="watermarkForm.type === 'text'">
          <el-form-item :label="$t('template.watermarkText')">
            <el-input v-model="watermarkForm.text" :placeholder="$t('template.placeholder.watermarkText')" />
          </el-form-item>
        </template>
        
        <template v-else>
          <el-form-item :label="$t('template.watermarkImage')">
            <div class="image-upload-row">
              <el-input v-model="watermarkForm.imageUrl" :placeholder="$t('template.placeholder.watermarkImage')" style="flex: 1" />
              <el-upload
                action="#"
                :auto-upload="false"
                :show-file-list="false"
                accept="image/*"
                :on-change="handleImageUpload"
                style="margin-left: 8px"
              >
                <el-button type="primary">{{ $t('common.upload') }}</el-button>
              </el-upload>
            </div>
            <div class="form-tip">{{ $t('template.watermarkImageTip') }}</div>
          </el-form-item>
        </template>
        
        <el-form-item :label="$t('template.watermarkPosition')">
          <el-select v-model="watermarkForm.position" style="width: 100%">
            <el-option :label="$t('template.watermarkPositions.diagonal')" value="diagonal" />
            <el-option :label="$t('template.watermarkPositions.header')" value="header" />
            <el-option :label="$t('template.watermarkPositions.footer')" value="footer" />
            <el-option :label="$t('template.watermarkPositions.corner')" value="corner" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('template.watermarkOpacity')">
          <el-slider v-model="watermarkForm.opacity" :min="10" :max="80" show-input />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="watermarkVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button @click="handleApplyWatermark">{{ $t('template.applyWatermark') }}</el-button>
        <el-button type="primary" @click="handleExportWatermark">{{ $t('template.exportWatermarked') }}</el-button>
      </template>
    </el-dialog>
    
    <!-- 导出弹窗 -->
    <el-dialog v-model="exportVisible" :title="$t('template.export')" width="400px" destroy-on-close>
      <div class="export-info">
        <p><strong>{{ $t('template.fileName') }}：</strong>{{ exportData.fileName }}</p>
        <p><strong>{{ $t('template.fileSize') }}：</strong>{{ formatSize(exportData.size) }}</p>
        <p><strong>{{ $t('template.exportTime') }}：</strong>{{ exportData.exportedAt }}</p>
      </div>
      <div class="export-content">
        <pre>{{ exportData.content }}</pre>
      </div>
      <template #footer>
        <el-button @click="exportVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="downloadTemplate">{{ $t('common.download') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTemplateList, deleteTemplate, previewTemplate, exportTemplate, watermarkTemplate, cloneTemplate } from '@/api/template'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { getContractCategories } from '@/api/contractCategory'
import { Plus, Document, View, Download, Edit, Delete, CopyDocument, Files, MoreFilled, Stamp, Search, Refresh } from '@element-plus/icons-vue'
import html2canvas from 'html2canvas'
import { jsPDF } from 'jspdf'

const router = useRouter()
const { t, locale } = useI18n()

const loading = ref(false)
const templates = ref<any[]>([])

const query = reactive({
  page: 1,
  pageSize: 20,
  category: '',
  keyword: ''
})

// 预览相关
const previewVisible = ref(false)
const previewData = ref<any>({})
const previewTemplateId = ref<number | null>(null)
const previewTab = ref('variables')
const previewVariables = ref<string[]>([])
const variableValues = reactive<Record<string, string>>({})
const filledPreviewContent = ref('')

// 水印相关
const watermarkVisible = ref(false)
const watermarkForm = reactive({
  type: 'text',
  text: 'CONFIDENTIAL',
  imageUrl: '',
  position: 'diagonal',
  opacity: 30
})
const watermarkTemplateId = ref<number | null>(null)
const watermarkedContent = ref('')

// 导出相关
const exportVisible = ref(false)
const exportData = ref<any>({})

// 分类相关
const categories = ref<any[]>([])

const getCategoryName = (cat: string) => {
  const found = categories.value.find(c => c.code === cat)
  if (!found) return cat
  if (locale.value === 'en') {
    return found.nameEn || found.name || cat
  }
  return found.name || found.nameEn || cat
}

const getCategoryColor = (cat: string) => {
  const found = categories.value.find(c => c.code === cat)
  return found?.color || '#409eff'
}

const getCategoryClass = (cat: string) => {
  const map: Record<string, string> = {
    PURCHASE: 'category-purchase',
    SALES: 'category-sales',
    SERVICE: 'category-service',
    LEASE: 'category-lease',
    EMPLOYMENT: 'category-employment'
  }
  return map[cat] || ''
}

const fetchCategories = async () => {
  try {
    const res = await getContractCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error('Failed to fetch categories', error)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getTemplateList(query)
    templates.value = res.data.list
  } catch (error) {
    ElMessage.error(t('template.error.load'))
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  query.category = ''
  query.keyword = ''
  fetchData()
}

const handleCreate = () => {
  router.push('/templates/create')
}

const handleEdit = (id: number) => {
  router.push({ name: 'TemplateEdit', params: { id: String(id) } })
}

const handleUse = (id: number) => {
  router.push({ path: '/contracts/create', query: { templateId: String(id) } })
}

const handleUsePreview = () => {
  if (previewTemplateId.value) {
    router.push({ path: '/contracts/create', query: { templateId: String(previewTemplateId.value) } })
  }
}

const handleClone = async (id: number) => {
  try {
    await cloneTemplate(id)
    ElMessage.success(t('common.success'))
    fetchData()
  } catch (error) {
    ElMessage.error(t('template.error.clone'))
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm(t('template.confirmDelete'), t('common.warning'), { 
      type: 'warning',
      confirmButtonText: t('common.delete'),
      cancelButtonText: t('common.cancel')
    })
    await deleteTemplate(id)
    ElMessage.success(t('common.success'))
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(t('template.error.delete'))
  }
}

// 预览
const handlePreview = async (template: any) => {
  previewTemplateId.value = template.id
  try {
    const res = await previewTemplate(template.id)
    const data = res.data
    const rawContent = data.originalContent || data.template?.content || ''
    previewData.value = {
      id: template.id,
      name: data.template?.name || template.name,
      category: data.template?.category || template.category,
      content: rawContent,
      description: data.template?.description || ''
    }
    previewVariables.value = extractVariables(rawContent)
    Object.keys(variableValues).forEach(k => delete variableValues[k])
    filledPreviewContent.value = ''
    previewTab.value = 'variables'
    previewVisible.value = true
  } catch (error) {
    ElMessage.error(t('template.error.preview'))
  }
}

const extractVariables = (content: string): string[] => {
  const pattern = /\[\[(\w+)\]\]/g
  const matches: string[] = []
  let match
  while ((match = pattern.exec(content)) !== null) {
    if (!matches.includes(match[1])) {
      matches.push(match[1])
    }
  }
  return matches
}

const updatePreviewContent = () => {
  let content = previewData.value.content || ''
  for (const [key, value] of Object.entries(variableValues)) {
    if (value) {
      content = content.replace(new RegExp(`\\[\\[${key}\\]\\]`, 'g'), value)
    }
  }
  filledPreviewContent.value = content
}

const fillSampleData = () => {
  const today = new Date().toISOString().split('T')[0]
  const end = new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  const samples: Record<string, string> = {
    contractNo: t('template.sampleData.contractNo'),
    partyA: t('template.sampleData.partyA'),
    partyB: t('template.sampleData.partyB'),
    productName: t('template.sampleData.productName'),
    quantity: t('template.sampleData.quantity'),
    unitPrice: t('template.sampleData.unitPrice'),
    totalPrice: t('template.sampleData.totalPrice'),
    signDate: today,
    startDate: today,
    endDate: end,
    amount: t('template.sampleData.amount'),
    projectName: t('template.sampleData.projectName'),
    address: t('template.sampleData.address')
  }
  for (const v of previewVariables.value) {
    if (samples[v]) {
      variableValues[v] = samples[v]
    }
  }
  updatePreviewContent()
}

const clearVariableValues = () => {
  for (const key of Object.keys(variableValues)) {
    variableValues[key] = ''
  }
  filledPreviewContent.value = ''
}

// 导出
const handleExport = async (template: any) => {
  try {
    const res = await exportTemplate(template.id)
    exportData.value = res.data
    exportVisible.value = true
  } catch (error) {
    ElMessage.error(t('template.error.export'))
  }
}

const handleExportPreview = () => {
  if (previewData.value.content) {
    exportData.value = {
      fileName: previewData.value.name + '.txt',
      content: previewData.value.content,
      size: previewData.value.content.length,
      exportedAt: new Date().toISOString().split('T')[0]
    }
    exportVisible.value = true
  }
}

const downloadTemplate = () => {
  const content = exportData.value.content
  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = exportData.value.fileName
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success(t('common.success'))
}

const formatSize = (bytes: number) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

// 水印
const handleWatermark = async (template: any) => {
  watermarkTemplateId.value = template.id
  watermarkedContent.value = ''
  watermarkVisible.value = true
}

const handleImageUpload = (file: any) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    watermarkForm.imageUrl = e.target?.result as string
  }
  reader.readAsDataURL(file.raw)
}

const handleApplyWatermark = async () => {
  if (!watermarkTemplateId.value) return
  try {
    const res = await watermarkTemplate(watermarkTemplateId.value, {
      text: watermarkForm.type === 'text' ? watermarkForm.text : undefined,
      imageUrl: watermarkForm.type === 'image' ? watermarkForm.imageUrl : undefined,
      position: watermarkForm.position as 'diagonal' | 'header' | 'footer' | 'corner',
      opacity: watermarkForm.opacity
    })
    watermarkedContent.value = res.data.watermarkedContent
    previewData.value = {
      name: t('template.watermarkedTemplate'),
      content: res.data.watermarkedContent,
      category: 'WATERMARKED'
    }
    ElMessage.success(t('template.success.watermark'))
  } catch (error) {
    ElMessage.error(t('template.error.watermark'))
  }
}

const handleExportWatermark = async () => {
  if (!watermarkedContent.value) {
    ElMessage.warning(t('template.warning.watermark'))
    return
  }
  const blob = new Blob([watermarkedContent.value], { type: 'text/html;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${t('template.watermarkedTemplate')}_${Date.now()}.html`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success(t('common.success'))
}

const handleExportPdf = async () => {
  if (!previewData.value?.id) return
  
  ElMessage.info(t('template.generatingPdf'))
  
  try {
    const container = document.createElement('div')
    container.style.cssText = 'position: fixed; left: -9999px; top: 0; width: 794px; padding: 40px; background: white; font-family: "Noto Sans SC", "Microsoft YaHei", sans-serif;'
    
    const title = document.createElement('h1')
    title.style.cssText = 'text-align: center; font-size: 24px; margin-bottom: 20px; color: #333;'
    title.textContent = previewData.value.name
    container.appendChild(title)
    
    const content = document.createElement('div')
    content.style.cssText = 'font-size: 14px; line-height: 1.8; color: #333;'
    content.innerHTML = previewData.value.content
    container.appendChild(content)
    
    document.body.appendChild(container)
    
    await nextTick()
    
    const canvas = await html2canvas(container, {
      scale: 2,
      useCORS: true,
      allowTaint: true,
      logging: false,
      backgroundColor: '#ffffff',
      windowWidth: 794
    })
    
    document.body.removeChild(container)
    
    const imgData = canvas.toDataURL('image/png')
    const pdf = new jsPDF({
      orientation: 'portrait',
      unit: 'mm',
      format: 'a4'
    })
    
    const pdfWidth = pdf.internal.pageSize.getWidth()
    const pdfHeight = pdf.internal.pageSize.getHeight()
    const imgWidth = canvas.width
    const imgHeight = canvas.height
    const ratio = Math.min((pdfWidth - 20) / imgWidth, (pdfHeight - 20) / imgHeight)
    const imgX = (pdfWidth - imgWidth * ratio) / 2
    const imgY = 10
    
    let heightLeft = imgHeight * ratio
    let position = imgY
    
    pdf.addImage(imgData, 'PNG', imgX, position, imgWidth * ratio, imgHeight * ratio)
    heightLeft -= (pdfHeight - 20)
    
    while (heightLeft > 0) {
      position = heightLeft - imgHeight * ratio + 10
      pdf.addPage()
      pdf.addImage(imgData, 'PNG', imgX, position, imgWidth * ratio, imgHeight * ratio)
      heightLeft -= (pdfHeight - 20)
    }
    
    pdf.save(`${previewData.value.name}.pdf`)
    ElMessage.success(t('common.success'))
  } catch (error) {
    console.error('PDF export error:', error)
    ElMessage.error(t('template.error.download'))
  }
}

onMounted(() => {
  fetchCategories()
  fetchData()
})
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

.template-list {
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
      font-weight: 600;
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

.category-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
  vertical-align: middle;
}

/* 模板卡片网格 */
.template-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 20px;
}

.template-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: var(--primary-gradient);
    transform: scaleX(0);
    transition: transform 0.3s ease;
  }
  
  &:hover {
    border-color: var(--primary);
    box-shadow: 0 12px 32px rgba(102, 126, 234, 0.15);
    transform: translateY(-6px);
    
    &::before {
      transform: scaleX(1);
    }
    
    .template-icon {
      transform: scale(1.1) rotate(3deg);
    }
  }
  
  .template-header {
    display: flex;
    gap: 14px;
    margin-bottom: 14px;
    
    .template-icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 22px;
      color: white;
      flex-shrink: 0;
      transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
      
      &.category-purchase { background: linear-gradient(135deg, #667eea, #764ba2); }
      &.category-sales { background: linear-gradient(135deg, #11998e, #38ef7d); }
      &.category-service { background: linear-gradient(135deg, #f093fb, #f5576c); }
      &.category-lease { background: linear-gradient(135deg, #fa709a, #fee140); }
      &.category-employment { background: linear-gradient(135deg, #667eea, #764ba2); }
    }
    
    .template-info {
      flex: 1;
      min-width: 0;
      
      .template-name {
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0 0 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .template-meta {
        display: flex;
        align-items: center;
        gap: 10px;
        
        .usage-count {
          font-size: 12px;
          color: var(--text-secondary);
        }
      }
    }
  }
  
  .template-desc {
    font-size: 13px;
    color: var(--text-secondary);
    line-height: 1.6;
    margin: 0 0 16px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  
  .template-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 10px;
    min-width: 0;
    padding-top: 14px;
    border-top: 1px solid var(--border-color);
    
    .update-time {
      font-size: 12px;
      color: var(--text-secondary);
      max-width: 55%;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    
    .template-actions {
      display: flex;
      gap: 4px;
      flex-shrink: 0;
      
      .el-button {
        padding: 6px;
        
        .el-icon {
          font-size: 16px;
        }
      }
    }
  }
}

.empty-state {
  grid-column: 1 / -1;
  padding: 60px 0;
}

/* 预览弹窗 */
.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  
  h2 {
    margin: 0;
    font-size: 18px;
    color: var(--text-primary);
  }
}

.preview-content--rich {
  padding: 12px;
  max-height: min(70vh, 640px);
  overflow-y: auto;
  background: var(--el-bg-color-page, #fff);
  border: 1px solid var(--border-color);
  border-radius: 8px;
}

.template-preview-quill {
  background: var(--el-bg-color, #fff);
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
  border-radius: 6px;
  overflow: hidden;

  :deep(.ql-editor) {
    min-height: 120px;
    box-sizing: border-box;
    word-break: break-word;
  }

  :deep(.ql-table-embed) {
    margin: 0.75em 0;
  }

  :deep(.ql-table-embed table) {
    width: 100%;
    border-collapse: collapse;
  }

  :deep(.ql-table-embed td),
  :deep(.ql-table-embed th) {
    border: 1px solid var(--el-border-color, #dcdfe6);
    padding: 6px 8px;
  }

  :deep(.ql-editor:not(.ql-blank) table) {
    width: 100%;
    border-collapse: collapse;
  }

  :deep(.ql-editor:not(.ql-blank) td),
  :deep(.ql-editor:not(.ql-blank) th) {
    border: 1px solid var(--el-border-color, #dcdfe6);
    padding: 6px 8px;
  }

  :deep(.ql-editor img) {
    max-width: 100%;
    height: auto;
  }
}

.preview-content.preview-empty {
  padding: 40px 20px;
  text-align: center;
  color: var(--text-secondary);
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: 8px;
}

/* 预览弹窗变量填充 */
.preview-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 16px;
  }
}

.variable-fill-form {
  .variable-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 12px;
    margin-bottom: 16px;
    
    .variable-item {
      display: flex;
      flex-direction: column;
      gap: 4px;
      
      label {
        font-size: 12px;
        color: var(--text-secondary);
        font-weight: 500;
      }
    }
  }
  
  .variable-actions {
    display: flex;
    gap: 8px;
  }
}

.no-variables {
  padding: 40px;
  text-align: center;
  color: var(--text-secondary);
}

/* 水印弹窗 */
.watermark-preview {
  height: 120px;
  background: #f5f5f5;
  border: 1px dashed #ddd;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  
  &.diagonal .watermark-text,
  &.corner .watermark-text {
    position: absolute;
    transform: rotate(-30deg);
    font-size: 28px;
    font-weight: bold;
    color: #999;
  }
  
  &.header .watermark-text,
  &.footer .watermark-text {
    font-size: 16px;
    color: #999;
    text-align: center;
  }
  
  &.header .watermark-text {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
  }
  
  &.footer .watermark-text {
    position: absolute;
    bottom: 50%;
    transform: translateY(50%);
  }
}

.form-tip {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.image-upload-row {
  display: flex;
  align-items: center;
}

/* 导出弹窗 */
.export-info {
  background: #f5f5f5;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  
  p {
    margin: 0 0 8px;
    font-size: 14px;
    color: var(--text-regular);
    
    &:last-child { margin-bottom: 0; }
  }
}

.export-content {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 16px;
  max-height: 200px;
  overflow-y: auto;
  
  pre {
    margin: 0;
    font-family: 'Monaco', 'Menlo', monospace;
    font-size: 12px;
    white-space: pre-wrap;
    word-break: break-all;
    color: var(--text-primary);
  }
}
</style>
