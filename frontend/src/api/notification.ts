import { del, get, put } from './index'

export const getMyNotifications = (params?: {
  page?: number
  pageSize?: number
  type?: string
  category?: string
  keyword?: string
  unreadOnly?: boolean
  importantOnly?: boolean
}) => get('/notifications/my', { params })

export const markNotificationRead = (id: number) => put(`/notifications/${id}/read`)

export const markAllNotificationRead = () => put('/notifications/read-all')

export const setNotificationImportant = (id: number, important: boolean) =>
  put(`/notifications/${id}/important`, { important })

export const deleteNotificationById = (id: number) => del(`/notifications/${id}`)

export const clearMyNotifications = () => del('/notifications/my')
