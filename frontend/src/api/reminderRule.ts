import { get, post, put, del } from './index'

export interface ReminderRule {
  id?: number
  name: string
  contractType?: string | string[]
  minAmount?: number
  maxAmount?: number
  remindDays: number
  isEnabled: boolean
  creatorId?: number
  isPublic?: boolean
}

export const getReminderRules = () =>
  get('/reminder-rules')

export const getMyReminderRules = () =>
  get('/reminder-rules/my')

export const createReminderRule = (data: Partial<ReminderRule> & { isPublic?: boolean }) =>
  post('/reminder-rules', data)

export const updateReminderRule = (id: number, data: Partial<ReminderRule>) =>
  put(`/reminder-rules/${id}`, data)

export const deleteReminderRule = (id: number) =>
  del(`/reminder-rules/${id}`)

export const toggleReminderRule = (id: number) =>
  put(`/reminder-rules/${id}/toggle`)
