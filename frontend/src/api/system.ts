import { get, post, del } from './index'

export interface SystemConfig {
  [key: string]: any
}

export interface SystemStatus {
  cpu: number
  memory: number
  disk: number
  onlineUsers: number
}

export interface OperationLog {
  id: number
  time: string
  level: string
  module: string
  action: string
  user: string
  content: string
  ip: string
}

export interface UserSession {
  id: number
  user: string
  ip: string
  location: string
  device: string
  loginTime: string
  lastActive: string
}

export const getSystemConfigs = () => get<SystemConfig>('/system/configs')

export const saveSystemConfigs = (configs: SystemConfig) => post('/system/configs', configs)

export const getSystemStatus = () => get<SystemStatus>('/system/monitor')

export const getOperationLogs = (params: {
  keyword?: string
  module?: string
  level?: string
  page?: number
  pageSize?: number
}) => get('/system/operation-logs', { params })

export const getActiveSessions = () => get<UserSession[]>('/system/sessions')

export const getLoginHistory = (params: {
  page?: number
  pageSize?: number
}) => get('/system/login-history', { params })

export const terminateSession = (id: number) => del(`/system/sessions/${id}`)

export const terminateAllSessions = () => del('/system/sessions')

export const sendTestEmail = (data: { toEmail: string }) => post('/system/email/test', data)

export const sendTestSms = (data: { phone: string }) => post('/system/sms/test', data)
