import { ref, reactive, type Reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { analyzeTemplate } from '@/api/template'
import { getSystemConfigs } from '@/api/system'
import type { ApiResponse } from '@/api/types'
import type { AiTemplateAnalysisResult } from '@/views/template/templateFormTypes'

export interface TemplateFormAiFields {
  name: string
  category: string
  content: string
  variablesText: string
  description: string
}

export function useTemplateFormAi(form: Reactive<TemplateFormAiFields>) {
  const { t } = useI18n()

  const aiDialogVisible = ref(false)
  const aiAnalyzing = ref(false)
  const aiConfigSnapshot = reactive({
    apiUrl: '',
    model: 'gpt-3.5-turbo',
    temperature: 0.7,
    maxTokens: 1000
  })

  const aiResult = ref<AiTemplateAnalysisResult>({
    summary: '',
    score: 75,
    risks: [],
    suggestions: [],
    keyInfo: {}
  })

  const createAiFallbackResult = (): AiTemplateAnalysisResult => ({
    summary: t('template.ai.fallbackSummary'),
    score: 72,
    risks: [{ level: 'low', content: t('template.ai.fallbackRisk'), anchor: '' }],
    suggestions: [t('template.ai.fallbackSuggestion')],
    keyInfo: {
      templateName: form.name || '-',
      category: form.category || '-'
    }
  })

  const normalizeAiResult = (raw: unknown): AiTemplateAnalysisResult => {
    if (!raw || typeof raw !== 'object') return createAiFallbackResult()
    const fallback = createAiFallbackResult()
    const rawObj = raw as Record<string, unknown>
    const keyInfo =
      rawObj.keyInfo && typeof rawObj.keyInfo === 'object' ? (rawObj.keyInfo as Record<string, unknown>) : {}
    const cw = keyInfo.consistencyWarnings
    const consistencyWarnings = Array.isArray(cw) ? cw.map((x) => String(x)) : []

    return {
      summary:
        typeof rawObj.summary === 'string' && rawObj.summary.trim() ? rawObj.summary : fallback.summary,
      score: Number.isFinite(Number(rawObj.score)) ? Number(rawObj.score) : fallback.score,
      risks:
        Array.isArray(rawObj.risks) && rawObj.risks.length > 0
          ? (rawObj.risks as AiTemplateAnalysisResult['risks'])
          : fallback.risks,
      suggestions:
        Array.isArray(rawObj.suggestions) && rawObj.suggestions.length > 0
          ? (rawObj.suggestions as string[])
          : fallback.suggestions,
      keyInfo: {
        templateName: (keyInfo.templateName as string | undefined) || form.name || '-',
        category: (keyInfo.category as string | undefined) || form.category || '-',
        structureNotes: (keyInfo.structureNotes as string | undefined) || '',
        variableHints: (keyInfo.variableHints as string | undefined) || '',
        consistencyWarnings
      }
    }
  }

  const loadAiRuntimeConfig = async () => {
    const response = await getSystemConfigs()
    const configs = (response as { data?: Record<string, unknown> }).data || {}
    aiConfigSnapshot.apiUrl = String(configs.ai_api_url || '')
    aiConfigSnapshot.model = String(configs.ai_model || 'gpt-3.5-turbo')
    aiConfigSnapshot.temperature = Number(configs.bd_temperature ?? 0.7)
    aiConfigSnapshot.maxTokens = Number(configs.bd_max_tokens ?? 1000)
    return {
      apiUrl: aiConfigSnapshot.apiUrl,
      apiKey: String(configs.ai_api_key || ''),
      model: aiConfigSnapshot.model,
      temperature: aiConfigSnapshot.temperature,
      maxTokens: aiConfigSnapshot.maxTokens
    }
  }

  const handleTemplateAiAnalyze = async () => {
    const content = form.content?.trim()
    if (!content) {
      ElMessage.warning(t('template.ai.contentRequired'))
      return
    }
    aiDialogVisible.value = true
    aiAnalyzing.value = true
    try {
      const aiConfig = await loadAiRuntimeConfig()
      const res = (await analyzeTemplate({
        name: form.name,
        category: form.category,
        description: form.description,
        content: form.content,
        variablesText: form.variablesText,
        ...aiConfig
      })) as ApiResponse<Record<string, unknown>>
      aiResult.value = normalizeAiResult(res.data)
      if (!aiConfig.apiUrl) {
        ElMessage.info(t('ai.offlineDemoHint'))
      }
    } catch (error: unknown) {
      aiResult.value = createAiFallbackResult()
      ElMessage.error((error as { message?: string })?.message || t('ai.analysisFailed'))
    } finally {
      aiAnalyzing.value = false
    }
  }

  return {
    aiDialogVisible,
    aiAnalyzing,
    aiConfigSnapshot,
    aiResult,
    handleTemplateAiAnalyze
  }
}
