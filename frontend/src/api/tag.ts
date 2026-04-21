import { get, post, put, del } from './index'

export interface Tag {
  id?: number
  name: string
  color: string
  usageCount?: number
  creatorId?: number
  isPublic?: boolean
}

export interface TagRelation {
  contractId: number
  tags: Tag[]
}

export const getTags = () =>
  get('/tags')

export const getMyTags = () =>
  get('/tags/my')

export const createTag = (data: Partial<Tag> & { isPublic?: boolean }) =>
  post('/tags', data)

export const updateTag = (id: number, data: Partial<Tag>) =>
  put(`/tags/${id}`, data)

export const deleteTag = (id: number) =>
  del(`/tags/${id}`)

export const getTagsByContract = (contractId: number) =>
  get(`/tags/contract/${contractId}`)

export const addTagToContract = (contractId: number, tagId: number) =>
  post(`/tags/contract/${contractId}`, { tagId })

export const removeTagFromContract = (contractId: number, tagId: number) =>
  del(`/tags/contract/${contractId}/${tagId}`)
