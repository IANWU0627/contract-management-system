import { get, post, put } from './index'

export interface Reminder {
  id?: number
  contractId: number
  contractNo: string
  contractTitle: string
  expireDate: string
  remindDays: number
  reminderType: number
  status: number
  remindedAt?: string
  createdAt?: string
}

// 获取我的提醒
export const getMyReminders = (params?: {
  page?: number
  pageSize?: number
  keyword?: string
  status?: number | ''
}) => get('/reminders/my', { params })

// 标记为已读
export const markReminderRead = (id: number) => put(`/reminders/${id}/read`)

// 批量标记已读
export const markReminderBatchRead = (ids: number[]) => put('/reminders/read-batch', { ids })

// 手动触发检查
export const triggerReminderCheck = () => post('/reminders/check')

// 创建提醒
export const createReminder = (data: Partial<Reminder> & { recipientUserId?: number }) =>
  post('/reminders', data)
