import { get, post, put, del } from './index'

export interface QuickCodeHeader {
  id?: number
  name: string
  nameEn?: string
  code: string
  description?: string
  descriptionEn?: string
  status?: number
  itemCount?: number
}

export interface QuickCodeItem {
  id?: number
  headerId?: number
  code: string
  meaning: string
  meaningEn?: string
  description?: string
  descriptionEn?: string
  tag?: string
  validFrom?: string
  validTo?: string
  enabled?: boolean
  sortOrder?: number
}

const QC_CACHE_KEY = 'quick_code_cache'
const QC_CACHE_TTL = 30 * 60 * 1000

interface QcCacheItem {
  data: any
  timestamp: number
}

const loadQcFromLocalStorage = (): Map<string, QcCacheItem> => {
  try {
    const cached = localStorage.getItem(QC_CACHE_KEY)
    if (cached) {
      const parsed = JSON.parse(cached)
      const map = new Map<string, QcCacheItem>()
      Object.keys(parsed).forEach(key => {
        map.set(key, parsed[key])
      })
      return map
    }
  } catch (e) {
    console.error('Failed to load quick code cache from localStorage:', e)
  }
  return new Map()
}

const saveQcToLocalStorage = (cache: Map<string, QcCacheItem>) => {
  try {
    const obj: Record<string, QcCacheItem> = {}
    cache.forEach((value, key) => {
      obj[key] = value
    })
    localStorage.setItem(QC_CACHE_KEY, JSON.stringify(obj))
  } catch (e) {
    console.error('Failed to save quick code cache to localStorage:', e)
  }
}

let quickCodeCache = loadQcFromLocalStorage()

export const clearQuickCodeCache = () => {
  quickCodeCache.clear()
  localStorage.removeItem(QC_CACHE_KEY)
}

const isQcCacheValid = (item: QcCacheItem): boolean => {
  return Date.now() - item.timestamp < QC_CACHE_TTL
}

export const getQuickCodes = () => {
  const cacheKey = 'quickCodes:list'
  const cached = quickCodeCache.get(cacheKey)
  if (cached && isQcCacheValid(cached)) {
    return Promise.resolve(cached.data)
  }
  const promise = get('/quick-codes')
  promise.then(res => {
    quickCodeCache.set(cacheKey, { data: res, timestamp: Date.now() })
    saveQcToLocalStorage(quickCodeCache)
  })
  return promise
}

export const getAllQuickCodes = (page?: number, pageSize?: number) => 
  get('/quick-codes/all', { params: { page, pageSize } })

export const getQuickCode = (id: number) => get(`/quick-codes/${id}`)

export const getQuickCodeByCode = (code: string) => {
  const cacheKey = `quickCodes:code:${code}`
  const cached = quickCodeCache.get(cacheKey)
  if (cached && isQcCacheValid(cached)) {
    return Promise.resolve(cached.data)
  }
  const promise = get(`/quick-codes/code/${code}`)
  promise.then(res => {
    quickCodeCache.set(cacheKey, { data: res, timestamp: Date.now() })
    saveQcToLocalStorage(quickCodeCache)
  })
  return promise
}

export const createQuickCode = (data: Partial<QuickCodeHeader> & { items?: Partial<QuickCodeItem>[] }) => {
  clearQuickCodeCache()
  return post('/quick-codes', data)
}

export const updateQuickCode = (id: number, data: Partial<QuickCodeHeader>) => {
  clearQuickCodeCache()
  return put(`/quick-codes/${id}`, data)
}

export const deleteQuickCode = (id: number) => {
  clearQuickCodeCache()
  return del(`/quick-codes/${id}`)
}

export const toggleQuickCode = (id: number) => {
  clearQuickCodeCache()
  return put(`/quick-codes/${id}/toggle`)
}

export const addQuickCodeItem = (headerId: number, data: Partial<QuickCodeItem>) => {
  clearQuickCodeCache()
  return post(`/quick-codes/${headerId}/items`, data)
}

export const updateQuickCodeItem = (itemId: number, data: Partial<QuickCodeItem>) => {
  clearQuickCodeCache()
  return put(`/quick-codes/items/${itemId}`, data)
}

export const deleteQuickCodeItem = (itemId: number) => {
  clearQuickCodeCache()
  return del(`/quick-codes/items/${itemId}`)
}
