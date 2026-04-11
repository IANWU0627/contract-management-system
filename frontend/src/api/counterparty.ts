import { get, post, put, del } from './index'

export interface Counterparty {
  id?: number
  contractId?: number
  name: string
  type?: string
  contactPerson?: string
  contactPhone?: string
  contactEmail?: string
  address?: string
  sortOrder?: number
}

export const getCounterpartiesByContractId = (contractId: number) =>
  get<Counterparty[]>(`/contract-counterparties/contract/${contractId}`)

export const createCounterparty = (data: Counterparty) =>
  post<Counterparty>('/contract-counterparties', data)

export const updateCounterparty = (id: number, data: Counterparty) =>
  put<Counterparty>(`/contract-counterparties/${id}`, data)

export const deleteCounterparty = (id: number) =>
  del(`/contract-counterparties/${id}`)

export const saveCounterpartiesBatch = (contractId: number, counterparties: Counterparty[]) =>
  post('/contract-counterparties/batch', { contractId, counterparties })
