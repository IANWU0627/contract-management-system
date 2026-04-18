import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { t } from '@/locales/index'

function resolveApiMessage(payload: any, fallback: string) {
  const key = payload?.messageKey as string | undefined
  if (key) {
    const raw = payload?.data
    const params =
      raw && typeof raw === 'object' && !Array.isArray(raw)
        ? (raw as Record<string, unknown>)
        : {}
    const translated = t(key, params as Record<string, unknown>)
    if (translated !== key) return translated as string
  }
  return payload?.message || fallback
}

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000
})

// 请求拦截
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截
service.interceptors.response.use(
  (response: AxiosResponse) => {
    // 如果是 blob 类型，直接返回原始 response
    if (response.config.responseType === 'blob') {
      return response
    }
    const res = response.data
    if (res.code === 200 || res.success) {
      return res
    }
    ElMessage.error(resolveApiMessage(res, t('error.requestFailed')))
    return Promise.reject(new Error(resolveApiMessage(res, t('error.requestFailed'))))
  },
  (error) => {
    const data = error.response?.data
    const errorMessage = resolveApiMessage(data, error.message || t('error.networkError'))
    if (error.response?.status === 401) {
      ElMessage.error(t('error.sessionExpired'))
      localStorage.removeItem('token')
      window.location.href = '/login'
    } else {
      ElMessage.error(resolveApiMessage(data, errorMessage))
    }
    return Promise.reject(error)
  }
)

export default service

// 封装常用方法 - 响应拦截器已返回 res.data，这里直接返回 res
export const get = <T = any>(url: string, config?: AxiosRequestConfig): Promise<T> => 
  service.get(url, config) as Promise<T>

export const post = <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> => 
  service.post(url, data, config) as Promise<T>

export const put = <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> => 
  service.put(url, data, config) as Promise<T>

export const del = <T = any>(url: string, config?: AxiosRequestConfig): Promise<T> => 
  service.delete(url, config) as Promise<T>

export const download = (url: string, filename: string) => {
  return service.get(url, { 
    responseType: 'blob'
  }).then((response: AxiosResponse) => {
    // 根据文件名扩展名确定Content-Type
    const getContentType = (name: string) => {
      const ext = name.split('.').pop()?.toLowerCase()
      const mimeTypes: Record<string, string> = {
        pdf: 'application/pdf',
        doc: 'application/msword',
        docx: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        xls: 'application/vnd.ms-excel',
        xlsx: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        jpg: 'image/jpeg',
        jpeg: 'image/jpeg',
        png: 'image/png',
        gif: 'image/gif',
        txt: 'text/plain'
      }
      return mimeTypes[ext || ''] || 'application/octet-stream'
    }
    
    const blob = new Blob([response.data], { 
      type: getContentType(filename)
    })
    const downloadUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(downloadUrl)
  }).catch((error: any) => {
    console.error('Download failed:', error)
    throw error
  })
}
