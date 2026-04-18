/** 与后端 `ApiResponse` 对齐的通用包装 */
export interface ApiResponse<T> {
  code: number
  message: string
  success?: boolean
  data: T
}

/** 标准分页体：list + total + page + pageSize */
export interface PageData<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

/** 从 `ApiResponse<PageData<T>>` 取 list，缺省为空数组 */
export function pageList<T>(res: { data?: PageData<T> | null } | null | undefined): T[] {
  return res?.data?.list ?? []
}

/** 分页 total，缺省为 0 */
export function pageTotal<T>(res: { data?: PageData<T> | null } | null | undefined): number {
  return res?.data?.total ?? 0
}
