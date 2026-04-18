import { get, post, put, del } from './index'
import type { ApiResponse, PageData } from './types'

export interface Template {
  id?: number
  name: string
  category: string
  content: string
  variables?: Record<string, string> | string
  description?: string
  usageCount?: number
  creator?: string
  createdAt?: string
  updatedAt?: string
}

export interface TemplateListQuery {
  page?: number
  pageSize?: number
  category?: string
  keyword?: string
}

export type TemplateListData = PageData<Template>

export const getTemplateList = (params?: TemplateListQuery, options?: { signal?: AbortSignal }) =>
  get<ApiResponse<TemplateListData>>('/templates', { params, ...(options?.signal ? { signal: options.signal } : {}) })

export const getTemplate = (id: number) => get(`/templates/${id}`)

export const createTemplate = (data: Partial<Template>) => post('/templates', data)

export const updateTemplate = (id: number, data: Partial<Template>) => put(`/templates/${id}`, data)

export const deleteTemplate = (id: number) => del(`/templates/${id}`)

export const previewTemplate = (id: number, params?: Record<string, string>) => 
  post(`/templates/${id}/preview`, params || {})

export const substituteTemplate = (id: number, data: {
  contractNo?: string
  counterparties?: Array<{ type: string; name: string; idNumber?: string }>
  startDate?: string
  endDate?: string
  amount?: number
  productName?: string
  [key: string]: any
}) => post(`/templates/${id}/substitute`, data)

export const getTemplateVariables = (id: number) => 
  get(`/templates/variables/${id}`)

export const exportTemplate = (id: number) => get(`/templates/${id}/export`)

export interface WatermarkConfig {
  text?: string
  imageUrl?: string
  position?: 'diagonal' | 'header' | 'footer' | 'corner'
  opacity?: number
}
export const watermarkTemplate = (id: number, config: WatermarkConfig) => 
  post(`/templates/${id}/watermark`, config)

export const cloneTemplate = (id: number) => post(`/templates/${id}/clone`)

export interface TemplateVariable {
  key: string
  label: string
  type: string
  required: boolean
  options?: string
  defaultValue?: string
}

export const getAvailableVariables = (contractType?: string) =>
  get('/templates/variables/list', contractType ? { params: { contractType } } : {})

export const extractTemplateVariables = (content: string) =>
  get<ApiResponse<string[]>>('/templates/variables/extract', { params: { content } })

export const replaceTemplateVariables = (content: string, values: Record<string, unknown>) =>
  post<ApiResponse<string>>('/templates/replace', { content, values })
