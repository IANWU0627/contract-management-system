import { get } from './index'

export const getChangeLogs = (contractId: number) => get(`/contracts/${contractId}/change-logs`)

export const getRecentChangeLogs = (limit = 20) => get(`/change-logs/recent?limit=${limit}`)

export const getMyChangeLogs = (limit = 20) => get(`/change-logs/my?limit=${limit}`)
