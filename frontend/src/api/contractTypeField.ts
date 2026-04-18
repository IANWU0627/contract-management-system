import { get, post, put, del } from './index'

export interface ContractTypeField {
  id?: number
  contractType: string
  fieldKey: string
  fieldLabel: string
  fieldLabelEn?: string
  fieldType: string
  quickCodeId?: string
  required?: boolean
  showInList?: boolean
  showInForm?: boolean
  fieldOrder?: number
  placeholder?: string
  placeholderEn?: string
  defaultValue?: string
  options?: string
  minValue?: number
  maxValue?: number
}

const CACHE_KEY = 'contract_field_config_cache'
const CACHE_TTL = 30 * 60 * 1000

interface CacheItem {
  data: any
  timestamp: number
}

const loadFromLocalStorage = (): Map<string, CacheItem> => {
  try {
    const cached = localStorage.getItem(CACHE_KEY)
    if (cached) {
      const parsed = JSON.parse(cached)
      const map = new Map<string, CacheItem>()
      Object.keys(parsed).forEach(key => {
        map.set(key, parsed[key])
      })
      return map
    }
  } catch (e) {
    console.error('Failed to load cache from localStorage:', e)
  }
  return new Map()
}

const saveToLocalStorage = (cache: Map<string, CacheItem>) => {
  try {
    const obj: Record<string, CacheItem> = {}
    cache.forEach((value, key) => {
      obj[key] = value
    })
    localStorage.setItem(CACHE_KEY, JSON.stringify(obj))
  } catch (e) {
    console.error('Failed to save cache to localStorage:', e)
  }
}

let fieldConfigCache = loadFromLocalStorage()

export const clearFieldConfigCache = () => {
  fieldConfigCache.clear()
  localStorage.removeItem(CACHE_KEY)
}

const isCacheValid = (item: CacheItem): boolean => {
  return Date.now() - item.timestamp < CACHE_TTL
}

export const getContractTypeFields = (contractType?: string) => {
  const cacheKey = contractType ? `fields:${contractType}` : 'fields:all'
  const cached = fieldConfigCache.get(cacheKey)
  if (cached && isCacheValid(cached)) {
    return Promise.resolve(cached.data)
  }
  const promise = contractType 
    ? get(`/contract-type-fields?contractType=${contractType}`)
    : get('/contract-type-fields')
  promise.then(res => {
    fieldConfigCache.set(cacheKey, { data: res, timestamp: Date.now() })
    saveToLocalStorage(fieldConfigCache)
  })
  return promise
}

export const getFieldCounts = () =>
  get('/contract-type-fields/counts')

export const getContractTypeFieldConfig = (contractType: string, page = 1, pageSize = 50) => {
  const cacheKey = `config:${contractType}`
  const cached = fieldConfigCache.get(cacheKey)
  if (cached && isCacheValid(cached)) {
    return Promise.resolve(cached.data)
  }
  const promise = get(`/contract-type-fields/config/${contractType}?page=${page}&pageSize=${pageSize}`)
  promise.then(res => {
    fieldConfigCache.set(cacheKey, { data: res, timestamp: Date.now() })
    saveToLocalStorage(fieldConfigCache)
  })
  return promise
}

export const getContractTypeFieldDraftConfig = (contractType: string) => {
  clearFieldConfigCache()
  return get(`/contract-type-fields/draft/${contractType}`)
}

export const saveContractTypeFieldDraft = (contractType: string, fields: Partial<ContractTypeField>[]) => {
  clearFieldConfigCache()
  return put(`/contract-type-fields/draft/${contractType}`, { fields })
}

export const discardContractTypeFieldDraft = (contractType: string) => {
  clearFieldConfigCache()
  return del(`/contract-type-fields/draft/${contractType}`)
}

export const publishContractTypeFieldDraft = (contractType: string) => {
  clearFieldConfigCache()
  return post(`/contract-type-fields/publish/${contractType}`)
}

export const getContractTypeFormFields = (contractType: string) => {
  const cacheKey = `form:${contractType}`
  const cached = fieldConfigCache.get(cacheKey)
  if (cached && isCacheValid(cached)) {
    return Promise.resolve(cached.data)
  }
  const promise = get(`/contract-type-fields/form/${contractType}`)
  promise.then(res => {
    fieldConfigCache.set(cacheKey, { data: res, timestamp: Date.now() })
    saveToLocalStorage(fieldConfigCache)
  })
  return promise
}

export const getContractTypes = () => {
  const cacheKey = 'types'
  const cached = fieldConfigCache.get(cacheKey)
  if (cached && isCacheValid(cached)) {
    return Promise.resolve(cached.data)
  }
  const promise = get('/contract-type-fields/types')
  promise.then(res => {
    fieldConfigCache.set(cacheKey, { data: res, timestamp: Date.now() })
    saveToLocalStorage(fieldConfigCache)
  })
  return promise
}

export const createContractTypeField = (data: Partial<ContractTypeField>) => {
  clearFieldConfigCache()
  return post('/contract-type-fields', data)
}

export const updateContractTypeField = (id: number, data: Partial<ContractTypeField>) => {
  clearFieldConfigCache()
  return put(`/contract-type-fields/${id}`, data)
}

export const deleteContractTypeField = (id: number) => {
  clearFieldConfigCache()
  return del(`/contract-type-fields/${id}`)
}

export const batchCreateContractTypeFields = (contractType: string, fields: Partial<ContractTypeField>[]) => {
  clearFieldConfigCache()
  return post('/contract-type-fields/batch', { contractType, fields })
}

export const deleteContractTypeFieldsByType = (contractType: string) => {
  clearFieldConfigCache()
  return del(`/contract-type-fields/type/${contractType}`)
}

export const exportFieldConfig = (contractType?: string) => {
  const url = contractType 
    ? `/contract-type-fields/export?contractType=${contractType}`
    : '/contract-type-fields/export'
  return url
}

export const importFieldConfig = (data: any) => {
  clearFieldConfigCache()
  return post('/contract-type-fields/import', data)
}
