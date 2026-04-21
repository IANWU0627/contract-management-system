import { get, post, put, del } from './index'

export interface Role {
  id?: number
  name: string
  code: string
  description: string
  status: number
}

export interface Permission {
  id: number
  name: string
  code: string
  path: string
  method: string
  description: string
  status: number
}

export const getRoles = () => get('/roles')

export const getActiveRoles = () => get('/roles/active')

export const getRoleDetail = (id: number) => get(`/roles/${id}`)

export const createRole = (data: any) => post('/roles', data)

export const updateRole = (id: number, data: any) => put(`/roles/${id}`, data)

export const deleteRole = (id: number) => del(`/roles/${id}`)

export const toggleRole = (id: number) => put(`/roles/${id}/toggle`)

export const getActivePermissions = () => get('/roles/permissions')
