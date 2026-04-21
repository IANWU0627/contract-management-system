import { get, post, put, del } from './index'
import type { ApiResponse, PageData } from './types'

export interface TemplateVariableQuery {
  category?: string
  type?: string
  status?: number
  keyword?: string
  page?: number
  pageSize?: number
}

export interface TemplateVariableItem {
  id: number
  code: string
  name: string
  nameEn?: string
  label?: string
  type: string
  quickCodeCode?: string
  category: string
  defaultValue?: string
  required?: boolean | number
  description?: string
  sortOrder?: number
  status?: number
}

export type TemplateVariablePageData = PageData<TemplateVariableItem>

export interface TemplateVariableBatchPayload {
  items: Record<string, unknown>[]
  conflictPolicy?: 'skip' | 'overwrite'
}

export const getTemplateVariables = (params?: TemplateVariableQuery) =>
  get<ApiResponse<TemplateVariablePageData>>('/template-variables', { params })

/** 分页拉取全部模板变量（管理列表默认每页 10 条，表单侧若只请求一页会匹配不到变量名） */
export async function fetchAllTemplateVariables(
  params?: Omit<TemplateVariableQuery, 'page' | 'pageSize'>
): Promise<TemplateVariableItem[]> {
  const pageSize = 200
  const all: TemplateVariableItem[] = []
  let page = 1
  for (;;) {
    const res = await getTemplateVariables({ ...params, page, pageSize })
    const list = res.data?.list ?? []
    const total = res.data?.total ?? 0
    all.push(...list)
    if (list.length < pageSize || all.length >= total) break
    page += 1
    if (page > 500) break
  }
  return all
}

export const getVariableCategories = () =>
  get<ApiResponse<Array<{ value: string; label: string }>>>('/template-variables/categories')

export const createTemplateVariable = (data: Record<string, unknown>) =>
  post<ApiResponse<TemplateVariableItem>>('/template-variables', data)

export const updateTemplateVariable = (id: number, data: Record<string, unknown>) =>
  put<ApiResponse<TemplateVariableItem>>(`/template-variables/${id}`, data)

export const deleteTemplateVariable = (id: number) => del(`/template-variables/${id}`)

export const getTemplateVariableImpact = (id: number) => get(`/template-variables/${id}/impact`)

export const batchCreateTemplateVariables = (payload: TemplateVariableBatchPayload) =>
  post('/template-variables/batch', payload)

export const initDefaultVariables = () => post('/template-variables/init-defaults', {})
