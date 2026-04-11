import { get, post, put, del } from './index'

export const getFolderTree = () => get('/contract-folders/tree')

export const getAllFolders = () => get('/contract-folders')

export const getFolder = (id: number) => get(`/contract-folders/${id}`)

export const createFolder = (data: {
  name: string
  parentId?: number | null
  description?: string
  color?: string
  sortOrder?: number
}) => post('/contract-folders', data)

export const updateFolder = (id: number, data: Partial<{
  name: string
  parentId: number | null
  description: string
  color: string
  sortOrder: number
}>) => put(`/contract-folders/${id}`, data)

export const deleteFolder = (id: number) => del(`/contract-folders/${id}`)
