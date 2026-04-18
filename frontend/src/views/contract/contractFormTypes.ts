/** Shared types for contract form page and composables */

export interface ContractFolderRow {
  id: number
  name: string
  color?: string
}

export interface ContractAttachmentDraft {
  uid?: number
  name: string
  fileName?: string
  url?: string
  fileUrl?: string
  size?: number
  fileSize?: number
  type?: string
  fileCategory?: string
  uploadTime?: string
}

export type DynamicFieldValue = string | number | string[] | null

export interface AiContractAnalysisResult {
  summary: string
  score: number
  risks: Array<{ level?: string; content?: string }>
  suggestions: string[]
  keyInfo: {
    partyA?: string
    partyB?: string
    amount?: string | number
    duration?: string
    keyClauses?: unknown[]
  }
  negotiationPoints: unknown[]
  missingClauseChecks: unknown[]
  actionSuggestions: unknown[]
  clauseSuggestions: unknown[]
}

export interface Counterparty {
  type: string
  name: string
  contact: string
  phone: string
  email: string
}

export interface QuickCodeOption {
  code: string
  meaning: string
  meaningEn?: string
  enabled?: boolean
}

export type TemplateVariableValue = string | number | string[] | null

/** 创建/更新合同请求体（含模板变量、动态字段等扩展字段） */
export interface ContractFormUpsertPayload {
  title: string
  type: string
  amount: number
  currency: string
  startDate: string
  endDate: string
  content: string
  remark: string
  folderId: number | null
  relationType: 'MAIN' | 'SUPPLEMENT'
  parentContractId: number | null
  timezone: string
  templateVariables: string
  dynamicFieldValues: string
  dynamicFields: Record<string, DynamicFieldValue>
  templateId: number | null
  contentMode: 'template' | 'upload'
  status?: string
}

export interface ContractFormState {
  title: string
  type: string
  amount: number
  currency: string
  startDate: string
  endDate: string
  content: string
  remark: string
  contractNo: string
  createdBy: string
  timezone: string
  createdAt: string
  folderId: number | null
  relationType: 'MAIN' | 'SUPPLEMENT'
  parentContractId: number | null
  counterparties: Counterparty[]
}
