<template>
  <el-dialog :model-value="modelValue" :title="$t('template.ai.assistantTitle')" width="820px" @update:model-value="$emit('update:modelValue', $event)">
    <div v-loading="aiAnalyzing">
      <el-alert
        v-if="!aiConfigSnapshot.apiUrl"
        type="info"
        :closable="false"
        :title="$t('ai.offlineDemoHint')"
        style="margin-bottom: 12px;"
      />

      <el-card shadow="never" style="margin-bottom: 12px;">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>{{ $t('template.ai.qualityScore') }}</span>
          <strong style="font-size: 24px;">{{ aiResult.score ?? 75 }}</strong>
        </div>
        <el-progress :percentage="Number(aiResult.score || 75)" :show-text="false" />
      </el-card>

      <el-divider content-position="left">{{ $t('ai.summary') }}</el-divider>
      <p style="line-height: 1.7; white-space: pre-wrap;">{{ aiResult.summary || '-' }}</p>

      <el-divider content-position="left">{{ $t('template.ai.insights') }}</el-divider>
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item :label="$t('template.name')">{{ aiResult.keyInfo?.templateName || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="$t('template.category')">{{ aiResult.keyInfo?.category || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="aiResult.keyInfo?.structureNotes" :label="$t('template.ai.structureNotes')">
          <span style="line-height: 1.6;">{{ aiResult.keyInfo.structureNotes }}</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="aiResult.keyInfo?.variableHints" :label="$t('template.ai.variableHints')">
          <span style="line-height: 1.6;">{{ aiResult.keyInfo.variableHints }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <template v-if="aiResult.keyInfo?.consistencyWarnings?.length">
        <el-divider content-position="left">{{ $t('template.ai.consistency') }}</el-divider>
        <ul style="padding-left: 18px; margin: 0 0 8px;">
          <li v-for="(w, idx) in aiResult.keyInfo.consistencyWarnings" :key="idx" style="line-height: 1.8;">
            {{ w }}
          </li>
        </ul>
      </template>

      <el-divider content-position="left">{{ $t('ai.risks') }}</el-divider>
      <el-empty v-if="!aiResult.risks?.length" :description="$t('common.noData')" :image-size="60" />
      <el-timeline v-else>
        <el-timeline-item
          v-for="(risk, idx) in aiResult.risks"
          :key="idx"
          :type="risk.level === 'high' ? 'danger' : risk.level === 'medium' ? 'warning' : 'primary'"
        >
          {{ risk.content || '-' }}
        </el-timeline-item>
      </el-timeline>

      <el-divider content-position="left">{{ $t('ai.suggestions') }}</el-divider>
      <el-empty v-if="!aiResult.suggestions?.length" :description="$t('common.noData')" :image-size="60" />
      <ul v-else style="padding-left: 18px; margin: 0;">
        <li v-for="(s, idx) in aiResult.suggestions" :key="idx" style="line-height: 1.8;">{{ s }}</li>
      </ul>
    </div>
    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">{{ $t('common.close') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import type { AiTemplateAnalysisResult } from '@/views/template/templateFormTypes'

defineProps<{
  modelValue: boolean
  aiAnalyzing: boolean
  aiConfigSnapshot: { apiUrl: string; model: string; temperature: number; maxTokens: number }
  aiResult: AiTemplateAnalysisResult
}>()

defineEmits<{
  'update:modelValue': [open: boolean]
}>()
</script>
