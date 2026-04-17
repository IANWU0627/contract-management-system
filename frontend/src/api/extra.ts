import { get, post, del } from './index'

// 版本历史
export const getVersionHistory = (contractId: number) => 
  get(`/contracts/${contractId}/versions`)

export const getVersionDetail = (contractId: number, versionId: number) => 
  get(`/contracts/${contractId}/versions/${versionId}`)

export const restoreVersion = (contractId: number, versionId: number) => 
  post(`/contracts/${contractId}/versions/${versionId}/restore`)

export const compareVersions = (contractId: number, versionId1: number, versionId2: number) => 
  post(`/contracts/${contractId}/versions/compare?versionId1=${versionId1}&versionId2=${versionId2}`)

export const compareSnapshots = (contractId: number, baseSnapshotId: number, targetSnapshotId: number) =>
  post(`/contracts/${contractId}/snapshots/compare?baseSnapshotId=${baseSnapshotId}&targetSnapshotId=${targetSnapshotId}`)

export const getContractSnapshots = (contractId: number) =>
  get(`/contracts/${contractId}/snapshots`)

// 评论
export const getComments = (contractId: number) => 
  get(`/contracts/${contractId}/comments`)

export const addComment = (contractId: number, data: { content: string; parentId?: number }) => 
  post(`/contracts/${contractId}/comments`, data)

export const deleteComment = (contractId: number, commentId: number) => 
  del(`/contracts/${contractId}/comments/${commentId}`)

// 操作日志
export const getOperationLogs = (params: { module?: string; targetId?: number; page?: number; pageSize?: number }) => 
  get('/logs', { params })

// 标签管理
export const getTags = () => get('/tags')

export const createTag = (data: { name: string; color: string }) => post('/tags', data)

export const deleteTag = (id: number) => del(`/tags/${id}`)

export const addTagToContract = (contractId: number, tagId: number) => 
  post(`/tags/contract/${contractId}`, { tagId })

export const removeTagFromContract = (contractId: number, tagId: number) => 
  del(`/tags/contract/${contractId}/${tagId}`)
