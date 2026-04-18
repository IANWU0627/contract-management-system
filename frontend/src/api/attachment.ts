import { get, post, put, del } from './index'
import type { ApiResponse } from './types'

export interface Attachment {
  id?: number
  contractId?: number
  fileName: string
  fileUrl: string
  fileSize?: number
  fileType?: string
  description?: string
  fileCategory?: 'contract' | 'support'
  uploadTime?: string
}

export const getAttachmentsByContractId = (contractId: number) =>
  get<ApiResponse<Attachment[]>>(`/contract-attachments/contract/${contractId}`)

export const createAttachment = (data: Attachment) =>
  post<Attachment>('/contract-attachments', data)

export const updateAttachment = (id: number, data: Attachment) =>
  put<Attachment>(`/contract-attachments/${id}`, data)

export const deleteAttachment = (id: number) =>
  del(`/contract-attachments/${id}`)

export const saveAttachmentsBatch = (contractId: number, attachments: Attachment[]) =>
  post('/contract-attachments/batch', { contractId, attachments })
