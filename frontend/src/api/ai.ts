import { get, post } from './index'

export interface AiConfig {
  provider: string
  apiUrl: string
  apiKey: string
  model: string
}

export const testAiConnection = (config: AiConfig) => 
  post('/ai/test', config)

/** 合同 AI 分析，与 `contract.ts` 的 `analyzeContract` 相同（优先使用 `POST /contracts/{id}/analyze`） */
export const analyzeWithAi = (contractId: number, config: Partial<AiConfig> & { temperature?: number; maxTokens?: number }) =>
  post(`/contracts/${contractId}/analyze`, config || {})

export const getOllamaModels = (baseUrl: string) => 
  get('/ai/ollama/models', { params: { baseUrl } })
