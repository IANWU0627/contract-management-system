<template>
  <div class="attachment-section">
    <div class="section-title-row">
      <div class="section-title">
        <el-icon><Files /></el-icon>
        <span>{{ $t('contract.fileManagement') }}</span>
      </div>
    </div>

    <div class="file-management-wrapper">
      <div class="contract-file-section">
        <div class="sub-section-header">
          <el-icon><Document /></el-icon>
          <span>{{ $t('contract.contractFile') }}</span>
        </div>
        <div v-if="generatedPdf" class="file-item">
          <el-icon class="pdf-icon"><Document /></el-icon>
          <span class="file-name" @click="$emit('download-generated-pdf')" style="cursor: pointer; color: var(--primary);">{{ generatedPdf.name }}</span>
          <el-tag size="small" type="success">{{ $t('contract.generated') }}</el-tag>
          <el-button type="danger" text size="small" @click="$emit('remove-generated-pdf')">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
        <div v-else class="empty-tip">
          <span>{{ $t('contract.noContractFile') }}</span>
        </div>
      </div>

      <div class="support-files-section">
        <div class="sub-section-header">
          <el-icon><Paperclip /></el-icon>
          <span>{{ $t('contract.supportFiles') }}</span>
        </div>
        <el-upload ref="attachmentUploadRef" :auto-upload="false" :on-change="onAttachmentChange" :on-remove="onAttachmentRemove" multiple>
          <el-button type="primary" plain size="small">
            <el-icon><Upload /></el-icon>
            {{ $t('contract.uploadSupportFile') }}
          </el-button>
        </el-upload>
        <div class="attachment-list" v-if="attachments.length > 0">
          <div v-for="(att, idx) in attachments" :key="idx" class="file-item">
            <el-icon><Document /></el-icon>
            <span class="file-name">{{ att.name }}</span>
            <el-button type="danger" text size="small" @click="$emit('remove-attachment', idx)">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { UploadFile } from 'element-plus'
import { Files, Document, Paperclip, Upload, Close } from '@element-plus/icons-vue'

export interface GeneratedPdfInfo {
  name: string
  url: string
  blob?: Blob
  serverUrl?: string
}

export interface AttachmentDraft {
  uid?: number
  name: string
  fileName?: string
  url?: string
  fileUrl?: string
  size?: number
  fileSize?: number
  type?: string
  fileCategory?: string
  uploadTime?: string
}

defineProps<{
  generatedPdf: GeneratedPdfInfo | null
  attachments: AttachmentDraft[]
}>()

const emit = defineEmits<{
  'download-generated-pdf': []
  'remove-generated-pdf': []
  'attachment-change': [file: UploadFile]
  'attachment-remove': [file: UploadFile]
  'remove-attachment': [index: number]
}>()

function onAttachmentChange(file: UploadFile) {
  emit('attachment-change', file)
}

function onAttachmentRemove(file: UploadFile) {
  emit('attachment-remove', file)
}
</script>

<style scoped lang="scss">
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

.attachment-section {
  margin: 20px 0;
}

.file-management-wrapper {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-top: 16px;
}

.contract-file-section,
.support-files-section {
  background: var(--bg-hover);
  border-radius: 12px;
  padding: 16px;
}

.sub-section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);

  .el-icon {
    color: var(--primary);
  }
}

.contract-file-section {
  .pdf-icon {
    color: #e6a23c;
    font-size: 20px;
  }

  .file-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    background: white;
    border-radius: 8px;
    border: 1px solid var(--border-color);
  }

  .empty-tip {
    padding: 24px;
    text-align: center;
    color: var(--text-secondary);
    font-size: 13px;
    background: white;
    border-radius: 8px;
    border: 1px dashed var(--border-color);
  }
}

.support-files-section {
  .attachment-list {
    margin-top: 12px;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .file-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    background: white;
    border-radius: 6px;
    border: 1px solid var(--border-color);
  }
}

.file-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
