import { get, post, put } from './index'

export interface Renewal {
  id?: number
  contractId: number
  title?: string
  contractNo?: string
  oldEndDate: string
  newEndDate: string
  renewalType: string
  status: string
  remark?: string
  operatorName?: string
  createdAt?: string
}

export const getRenewals = (contractId: number) =>
  get(`/contracts/${contractId}/renewals`)

export const createRenewal = (contractId: number, data: Partial<Renewal>) =>
  post(`/contracts/${contractId}/renewals`, data)

export const approveRenewal = (contractId: number, renewalId: number) =>
  put(`/contracts/${contractId}/renewals/${renewalId}/approve`)

export const rejectRenewal = (contractId: number, renewalId: number, remark?: string) =>
  put(`/contracts/${contractId}/renewals/${renewalId}/reject`, { remark })

export const getAllRenewals = (status?: string, page: number = 1, pageSize: number = 10) =>
  get('/renewals', { params: { status, page, pageSize } })
