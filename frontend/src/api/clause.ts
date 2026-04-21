import { del, get, post, put } from './index'

export interface Clause {
  id?: number
  code: string
  name: string
  nameEn?: string
  category?: string
  content: string
  description?: string
  status?: number
  sortOrder?: number
  createdAt?: string
  updatedAt?: string
}

export interface ClauseReferenceTemplate {
  id: number
  name: string
  category?: string
  updatedAt?: string
}

export interface ClauseReferenceSummary {
  count: number
  templates: ClauseReferenceTemplate[]
}

export const getClauseList = (params?: {
  keyword?: string
  category?: string
  status?: number
}) => get('/clauses', { params })

export const getClauseReferences = () =>
  get<Record<string, ClauseReferenceSummary>>('/clauses/references')

export const createClause = (data: Partial<Clause>) => post('/clauses', data)

export const updateClause = (id: number, data: Partial<Clause>) => put(`/clauses/${id}`, data)

export const deleteClause = (id: number) => del(`/clauses/${id}`)
