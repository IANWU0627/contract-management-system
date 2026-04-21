import { ref, reactive, type Ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { analyzeContract } from '@/api/contract'
import { getSystemConfigs } from '@/api/system'
import type { AiContractAnalysisResult, ContractFormState } from '@/views/contract/contractFormTypes'

export function useContractFormAi(
  form: ContractFormState,
  isEdit: Ref<boolean>,
  contractId: Ref<number>
) {
  const { t } = useI18n()

  const aiDialogVisible = ref(false)
  const aiAnalyzing = ref(false)
  const aiConfigSnapshot = reactive({
    apiUrl: '',
    model: 'gpt-3.5-turbo',
    temperature: 0.7,
    maxTokens: 1000
  })
  const aiResult = ref<AiContractAnalysisResult>({
    summary: '',
    score: 75,
    risks: [],
    suggestions: [],
    keyInfo: {},
    negotiationPoints: [],
    missingClauseChecks: [],
    actionSuggestions: [],
    clauseSuggestions: []
  })

  const createAiFallbackResult = (): AiContractAnalysisResult => ({
    summary: '未获取到完整 AI 结果，已提供基础分析视图。请保存合同内容后重试，或配置可用 AI 接口。',
    score: 75,
    risks: [{ level: 'low', content: '暂未识别到结构化风险，请人工复核关键条款。' }],
    suggestions: ['建议先保存草稿并补充正文，再重新分析。'],
    keyInfo: {
      partyA: form.counterparties.find((cp) => cp.type === 'partyA')?.name || '-',
      partyB: form.counterparties.find((cp) => cp.type === 'partyB')?.name || '-',
      amount: form.amount || '-',
      duration: form.startDate && form.endDate ? `${form.startDate} ~ ${form.endDate}` : '-'
    },
    negotiationPoints: [],
    missingClauseChecks: [],
    actionSuggestions: [],
    clauseSuggestions: []
  })

  const normalizeAiResult = (raw: unknown): AiContractAnalysisResult => {
    if (!raw || typeof raw !== 'object') return createAiFallbackResult()
    const fallback = createAiFallbackResult()
    const rawObj = raw as Record<string, unknown>
    const keyInfo =
      rawObj.keyInfo && typeof rawObj.keyInfo === 'object' ? (rawObj.keyInfo as Record<string, unknown>) : {}
    return {
      summary: typeof rawObj.summary === 'string' && rawObj.summary.trim() ? rawObj.summary : fallback.summary,
      score: Number.isFinite(Number(rawObj.score)) ? Number(rawObj.score) : fallback.score,
      risks:
        Array.isArray(rawObj.risks) && rawObj.risks.length > 0
          ? (rawObj.risks as AiContractAnalysisResult['risks'])
          : fallback.risks,
      suggestions:
        Array.isArray(rawObj.suggestions) && rawObj.suggestions.length > 0
          ? (rawObj.suggestions as string[])
          : fallback.suggestions,
      keyInfo: {
        partyA: (keyInfo.partyA as string | undefined) || fallback.keyInfo.partyA,
        partyB: (keyInfo.partyB as string | undefined) || fallback.keyInfo.partyB,
        amount: (keyInfo.amount as string | number | undefined) || fallback.keyInfo.amount,
        duration: (keyInfo.duration as string | undefined) || fallback.keyInfo.duration,
        keyClauses: Array.isArray(keyInfo.keyClauses) ? keyInfo.keyClauses : []
      },
      negotiationPoints: Array.isArray(rawObj.negotiationPoints) ? rawObj.negotiationPoints : [],
      missingClauseChecks: Array.isArray(rawObj.missingClauseChecks) ? rawObj.missingClauseChecks : [],
      actionSuggestions: Array.isArray(rawObj.actionSuggestions) ? rawObj.actionSuggestions : [],
      clauseSuggestions: Array.isArray(rawObj.clauseSuggestions) ? rawObj.clauseSuggestions : []
    }
  }

  const loadAiRuntimeConfig = async () => {
    const response = await getSystemConfigs()
    const configs = response?.data || {}
    aiConfigSnapshot.apiUrl = configs.ai_api_url || ''
    aiConfigSnapshot.model = configs.ai_model || 'gpt-3.5-turbo'
    aiConfigSnapshot.temperature = Number(configs.bd_temperature ?? 0.7)
    aiConfigSnapshot.maxTokens = Number(configs.bd_max_tokens ?? 1000)
    return {
      apiUrl: aiConfigSnapshot.apiUrl,
      apiKey: configs.ai_api_key || '',
      model: aiConfigSnapshot.model,
      temperature: aiConfigSnapshot.temperature,
      maxTokens: aiConfigSnapshot.maxTokens
    }
  }

  const runAiAnalyze = async (targetContractId: number) => {
    if (!targetContractId) {
      ElMessage.warning('请先保存合同后再进行 AI 分析')
      return
    }
    aiDialogVisible.value = true
    aiAnalyzing.value = true
    try {
      const aiConfig = await loadAiRuntimeConfig()
      const res = await analyzeContract(targetContractId, aiConfig)
      aiResult.value = normalizeAiResult(res.data)
    } catch (error: unknown) {
      aiResult.value = createAiFallbackResult()
      ElMessage.error((error as { message?: string })?.message || t('ai.analysisFailed'))
    } finally {
      aiAnalyzing.value = false
    }
  }

  const handleEditAiAnalyze = async () => {
    if (!isEdit.value || !contractId.value) {
      ElMessage.warning('请先保存合同后再进行 AI 分析')
      return
    }
    await runAiAnalyze(contractId.value)
  }

  const handleAiAnalyzeById = async (targetContractId: number) => {
    await runAiAnalyze(targetContractId)
  }

  return {
    aiDialogVisible,
    aiAnalyzing,
    aiConfigSnapshot,
    aiResult,
    handleEditAiAnalyze,
    handleAiAnalyzeById
  }
}
