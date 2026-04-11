import { get, post, put, del } from './index'

/**
 * 合同类型接口
 */
export interface ContractCategory {
  id?: number
  name: string
  nameEn?: string
  code: string
  icon?: string
  color?: string
  sortOrder?: number
  active?: boolean
  templateCount?: number
  createdAt?: string
  updatedAt?: string
}

// 获取启用的分类列表（供下拉选择用）
export const getContractCategories = () => get('/contract-categories')

// 获取所有分类（分页，用于管理页面）
export const getAllContractCategories = (page: number = 1, pageSize: number = 10) => 
  get('/contract-categories/all', { params: { page, pageSize } })

// 获取单个分类
export const getContractCategory = (id: number) => get(`/contract-categories/${id}`)

// 创建分类
export const createContractCategory = (data: Partial<ContractCategory>) => 
  post('/contract-categories', data)

// 更新分类
export const updateContractCategory = (id: number, data: Partial<ContractCategory>) => 
  put(`/contract-categories/${id}`, data)

// 删除分类
export const deleteContractCategory = (id: number) => del(`/contract-categories/${id}`)
