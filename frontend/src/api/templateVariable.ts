import { get, post, put, del } from './index'

export const getTemplateVariables = (params?: { category?: string; type?: string; status?: number; page?: number; pageSize?: number }) =>
  get('/template-variables', { params })

export const getTemplateVariable = (id: number) => get(`/template-variables/${id}`)

export const getVariableCategories = () => get('/template-variables/categories')

export const createTemplateVariable = (data: any) => post('/template-variables', data)

export const updateTemplateVariable = (id: number, data: any) => put(`/template-variables/${id}`, data)

export const deleteTemplateVariable = (id: number) => del(`/template-variables/${id}`)

export const initDefaultVariables = () => post('/template-variables/init-defaults', {})
