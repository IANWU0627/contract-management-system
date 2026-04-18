<template>
  <div class="content-section">
    <div class="section-title-row">
      <div class="section-title">
        <el-icon><Edit /></el-icon>
        <span>{{ $t('contract.content') }}</span>
      </div>
    </div>

    <div class="mode-row">
      <el-form-item :label="$t('contract.contentMode')" required style="flex: 1;">
        <el-select v-model="contentMode" style="width: 100%;">
          <el-option :label="$t('contract.templateMode')" value="template" />
          <el-option :label="$t('contract.uploadMode')" value="upload" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="contentMode === 'template'" :label="$t('contract.selectTemplate')" required style="flex: 2;">
        <el-select
          :model-value="selectedTemplateId"
          :placeholder="contractType ? $t('contract.selectTemplatePlaceholder') : $t('contract.selectTemplateNeedType')"
          :disabled="!contractType"
          style="width: 100%;"
          filterable
          @change="(id: number | null) => $emit('template-change', id)"
        >
          <el-option v-for="t in templates" :key="t.id" :label="t.name" :value="t.id" />
        </el-select>
      </el-form-item>
    </div>

    <div v-if="contentMode === 'template'" class="template-mode">
      <div v-if="selectedTemplate" class="template-content-wrapper">
        <div class="variables-panel">
          <div class="panel-header">
            <el-icon><List /></el-icon>
            <span>{{ $t('contract.templateVariables') }}</span>
          </div>
          <div class="variables-form">
            <el-form label-position="top">
              <el-form-item v-for="variable in extractedVariables" :key="variable.key" :label="variable.label">
                <el-input
                  v-if="variable.type === 'text'"
                  v-model="templateVariables[variable.key]"
                  :placeholder="variable.label"
                  clearable
                  @input="$emit('preview-update')"
                />
                <el-input-number
                  v-else-if="variable.type === 'number'"
                  v-model="templateVariables[variable.key]"
                  :placeholder="variable.label"
                  style="width: 100%"
                  @change="$emit('preview-update')"
                />
                <el-date-picker
                  v-else-if="variable.type === 'date'"
                  v-model="templateVariables[variable.key]"
                  type="date"
                  :placeholder="variable.label"
                  style="width: 100%"
                  value-format="YYYY-MM-DD"
                  @change="$emit('preview-update')"
                />
                <el-input
                  v-else-if="variable.type === 'textarea'"
                  v-model="templateVariables[variable.key]"
                  type="textarea"
                  :rows="3"
                  :placeholder="variable.label"
                  @input="$emit('preview-update')"
                />
                <el-select
                  v-else-if="variable.type === 'select'"
                  v-model="templateVariables[variable.key]"
                  :placeholder="variable.label"
                  clearable
                  style="width: 100%"
                  @change="$emit('preview-update')"
                >
                  <el-option
                    v-for="item in getQuickCodeOptions(variable.quickCodeCode)"
                    :key="item.code"
                    :label="item.meaning"
                    :value="item.code"
                  />
                </el-select>
                <el-select
                  v-else-if="variable.type === 'multiselect'"
                  v-model="templateVariables[variable.key]"
                  :placeholder="variable.label"
                  clearable
                  multiple
                  style="width: 100%"
                  @change="$emit('preview-update')"
                >
                  <el-option
                    v-for="item in getQuickCodeOptions(variable.quickCodeCode)"
                    :key="item.code"
                    :label="item.meaning"
                    :value="item.code"
                  />
                </el-select>
                <el-input
                  v-else
                  v-model="templateVariables[variable.key]"
                  :placeholder="variable.label"
                  clearable
                  @input="$emit('preview-update')"
                />
              </el-form-item>
            </el-form>
            <div v-if="extractedVariables.length === 0" class="no-variables">
              {{ $t('contract.noVariables') }}
            </div>
          </div>
        </div>

        <div class="preview-panel">
          <div class="panel-header">
            <el-icon><View /></el-icon>
            <span>{{ $t('contract.preview') }}</span>
          </div>
          <div class="preview-body-scroll">
            <div class="ql-snow contract-template-preview-quill">
              <div class="ql-editor" v-html="previewContent"></div>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="no-template-selected">
        <el-empty
          :description="contractType ? $t('contract.selectTemplateToStart') : $t('contract.selectTemplateNeedType')"
        />
      </div>
    </div>

    <div v-if="contentMode === 'upload'" class="upload-mode">
      <el-upload
        class="upload-area"
        drag
        :auto-upload="false"
        :on-change="onUploadChange"
        accept=".pdf,.doc,.docx"
      >
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <div class="upload-text">
          <span>{{ $t('contract.uploadFileTip') }}</span>
          <span class="upload-hint">{{ $t('contract.uploadFormatsShort') }}</span>
        </div>
      </el-upload>

      <div v-if="uploadedFile" class="upload-preview">
        <div class="preview-header">
          <el-icon><Document /></el-icon>
          <span>{{ uploadedFile.name }}</span>
          <el-button type="danger" text size="small" @click="emit('clear-upload')">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
        <div class="preview-content">
          <img v-if="uploadedFileUrl && isImageFile(uploadedFile.name)" :src="uploadedFileUrl" alt="preview" />
          <div v-else class="file-preview-placeholder">
            <el-icon size="48"><Document /></el-icon>
            <span>{{ uploadedFile.name }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="form.content && form.content !== '<p><br></p>'" class="full-preview-section">
      <div class="preview-actions">
        <el-button type="primary" @click="showFullPreviewDialog = true">
          <el-icon><View /></el-icon>
          {{ $t('contract.fullPreview') }}
        </el-button>
        <el-button type="success" @click="$emit('generate-pdf')" :loading="pdfGenerating">
          <el-icon><Download /></el-icon>
          {{ $t('contract.generateFile') }}
        </el-button>
      </div>
    </div>

    <el-dialog v-model="showFullPreviewDialog" :title="$t('contract.fullPreview')" width="85%" destroy-on-close class="contract-full-preview-dialog">
      <div class="preview-dialog-scroll">
        <div class="ql-snow contract-template-preview-quill">
          <div class="ql-editor" v-html="form.content"></div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import type { UploadFile } from 'element-plus'
import { Edit, List, View, UploadFilled, Document, Close, Download } from '@element-plus/icons-vue'
import type { Template } from '@/api/template'
import '@vueup/vue-quill/dist/vue-quill.snow.css'

export interface ExtractedVariableField {
  key: string
  label: string
  type: string
  quickCodeCode: string
  required: boolean
}

export interface QuickCodeOption {
  code: string
  meaning: string
  meaningEn?: string
  enabled?: boolean
}

export interface ContentFormSlice {
  content: string
}

type TemplateVariableValue = string | number | string[] | null

const contentMode = defineModel<'template' | 'upload'>('contentMode', { required: true })
const showFullPreviewDialog = defineModel<boolean>('showFullPreviewDialog', { required: true })

defineProps<{
  form: ContentFormSlice
  /** 已选合同类型编码；未选时模板下拉禁用且无选项 */
  contractType?: string
  templates: Template[]
  selectedTemplateId: number | null
  selectedTemplate: Template | null
  templateVariables: Record<string, TemplateVariableValue>
  extractedVariables: ExtractedVariableField[]
  previewContent: string
  uploadedFile: UploadFile | null
  uploadedFileUrl: string
  pdfGenerating: boolean
  getQuickCodeOptions: (quickCodeCode: string) => QuickCodeOption[]
}>()

const emit = defineEmits<{
  'template-change': [id: number | null]
  'preview-update': []
  'file-upload': [file: UploadFile]
  'clear-upload': []
  'generate-pdf': []
}>()

function onUploadChange(file: UploadFile) {
  emit('file-upload', file)
}

function isImageFile(filename: string) {
  return /\.(jpg|jpeg|png|gif|bmp|webp)$/i.test(filename)
}
</script>

<style scoped lang="scss">
.content-section {
  margin: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  flex-shrink: 0;
}

.section-title-row {
  display: flex;
  align-items: center;
  margin: 20px 0 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-color);
}

.mode-row {
  display: flex;
  gap: 16px;
  margin-top: 16px;

  :deep(.el-form-item) {
    margin-bottom: 0;
  }
}

.template-mode {
  margin-top: 20px;
}

.template-content-wrapper {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 16px;
  min-height: 400px;
}

.variables-panel,
.preview-panel {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    padding: 12px 16px;
    background: var(--bg-hover);
    border-bottom: 1px solid var(--border-color);
    font-weight: 600;
    font-size: 14px;
    color: var(--text-primary);

    .el-icon {
      color: var(--primary);
    }
  }

  .variables-form {
    padding: 16px;
    max-height: 420px;
    overflow-y: auto;

    :deep(.el-form-item) {
      margin-bottom: 12px;

      .el-form-item__label {
        font-weight: 500;
        color: var(--text-primary);
      }
    }

    .no-variables {
      text-align: center;
      color: var(--text-secondary);
      padding: 40px 0;
    }
  }

  .preview-body-scroll {
    flex: 1;
    min-height: 0;
    padding: 12px;
    max-height: 420px;
    overflow-y: auto;
  }
}

.contract-template-preview-quill {
  background: var(--el-bg-color, #fff);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;

  :deep(.ql-editor) {
    min-height: 160px;
    box-sizing: border-box;
    word-break: break-word;
  }

  :deep(.ql-table-embed) {
    margin: 0.75em 0;
  }

  :deep(.ql-table-embed table),
  :deep(.ql-editor:not(.ql-blank) table) {
    width: 100%;
    border-collapse: collapse;
  }

  :deep(.ql-table-embed td),
  :deep(.ql-table-embed th),
  :deep(.ql-editor:not(.ql-blank) td),
  :deep(.ql-editor:not(.ql-blank) th) {
    border: 1px solid var(--border-color);
    padding: 6px 8px;
  }

  :deep(.ql-editor img) {
    max-width: 100%;
    height: auto;
  }
}

.preview-dialog-scroll {
  max-height: 70vh;
  overflow-y: auto;
  padding: 4px 0;
}

.no-template-selected {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  background: var(--bg-hover);
  border-radius: 12px;
}

.upload-mode {
  margin-top: 20px;
}

.upload-area {
  width: 100%;

  :deep(.el-upload-dragger) {
    padding: 32px;
    border: 2px dashed var(--border-color);
    border-radius: 16px;
    background: linear-gradient(135deg, var(--bg-hover) 0%, var(--bg-card) 100%);
    transition: all 0.3s;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

    &:hover {
      border-color: var(--primary);
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, var(--bg-hover) 100%);
      transform: translateY(-2px);
      box-shadow: 0 4px 16px rgba(102, 126, 234, 0.15);
    }
  }

  .upload-icon {
    font-size: 56px;
    color: var(--primary);
    margin-bottom: 12px;
    background: linear-gradient(135deg, var(--primary) 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  .upload-text {
    display: flex;
    flex-direction: column;
    gap: 6px;
    color: var(--text-primary);

    .upload-hint {
      font-size: 13px;
      color: var(--text-secondary);
      margin-top: 4px;
      padding: 4px 12px;
      background: var(--bg-color);
      border-radius: 12px;
      display: inline-block;
    }
  }
}

.upload-preview {
  margin-top: 24px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

  .preview-header {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 14px 18px;
    background: linear-gradient(135deg, var(--bg-hover) 0%, var(--bg-card) 100%);
    border-bottom: 1px solid var(--border-color);

    .el-icon {
      color: var(--primary);
      font-size: 18px;
    }

    span {
      flex: 1;
      font-weight: 600;
      font-size: 14px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .preview-content {
    padding: 20px;
    max-height: 450px;
    overflow: auto;
    text-align: center;
    background: linear-gradient(180deg, var(--bg-color) 0%, var(--bg-card) 100%);

    img {
      max-width: 100%;
      border-radius: 12px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    }

    .file-preview-placeholder {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16px;
      padding: 48px 24px;
      color: var(--text-secondary);
      background: var(--bg-hover);
      border-radius: 12px;
      border: 1px dashed var(--border-color);

      .el-icon {
        font-size: 64px;
        color: var(--primary);
        opacity: 0.6;
      }

      span {
        font-size: 15px;
        font-weight: 500;
        color: var(--text-secondary);
      }
    }
  }
}

.full-preview-section {
  margin-top: 16px;
  padding: 16px;
  background: var(--bg-hover);
  border-radius: 8px;
}

.preview-actions {
  display: flex;
  gap: 12px;
}

</style>
