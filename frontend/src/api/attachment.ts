import { get, post } from './index'
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

export const saveAttachmentsBatch = (contractId: number, attachments: Attachment[]) =>
  post('/contract-attachments/batch', { contractId, attachments })
