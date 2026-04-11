import { post } from './index'

export interface AiConfig {
  provider: string
  apiUrl: string
  apiKey: string
  model: string
}

export const testAiConnection = (config: AiConfig) => 
  post('/ai/test', config)

export const analyzeWithAi = (contractId: number, config: AiConfig) => 
  post(`/ai/analyze/${contractId}`, config)
