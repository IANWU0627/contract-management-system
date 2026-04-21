import { get, post } from './index'

export interface AiConfig {
  provider: string
  apiUrl: string
  apiKey: string
  model: string
}

export const testAiConnection = (config: AiConfig) => 
  post('/ai/test', config)

export const getOllamaModels = (baseUrl: string) => 
  get('/ai/ollama/models', { params: { baseUrl } })
