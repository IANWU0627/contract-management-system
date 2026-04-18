import { get, post, put, del } from './index'
import type { ApiResponse, PageData } from './types'

export interface UserListQuery {
  page?: number
  pageSize?: number
  role?: string
  keyword?: string
}

export interface User {
  id?: number
  username: string
  nickname?: string
  email?: string
  phone?: string
  /** 部门标识，与数据权限（同部门可见合同）相关 */
  department?: string | null
  role: string
  status: string
  createdAt?: string
  avatar?: string
  lastLoginAt?: string
  contractCount?: number
  roles?: string[]
  permissions?: string[]
}

export type UserListData = PageData<User>

// 用户列表
export const getUserList = (params?: UserListQuery, options?: { signal?: AbortSignal }) =>
  get<ApiResponse<UserListData>>('/users', { params, ...(options?.signal ? { signal: options.signal } : {}) })

// 用户详情
export const getUser = (id: number) => 
  get(`/users/${id}`)

// 创建用户
export const createUser = (data: Partial<User>) => 
  post('/users', data)

// 更新用户
export const updateUser = (id: number, data: Partial<User>) => 
  put(`/users/${id}`, data)

// 删除用户
export const deleteUser = (id: number) => 
  del(`/users/${id}`)

// 获取所有角色
export const getRoles = () => get('/users/roles')
