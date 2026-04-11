import { ElMessage, ElMessageBox } from 'element-plus'
import type { ElMessageBoxOptions } from 'element-plus'

/**
 * 消息提示工具类
 * 统一管理所有的消息提示，提供更好的用户体验
 */

// 默认配置
const defaultOptions = {
  duration: 3000,
  showClose: true,
  grouping: true
}

/**
 * 成功提示
 */
export const showSuccess = (message: string, options?: any) => {
  return ElMessage.success({
    message,
    ...defaultOptions,
    ...options
  })
}

/**
 * 错误提示
 */
export const showError = (message: string, options?: any) => {
  return ElMessage.error({
    message,
    ...defaultOptions,
    ...options
  })
}

/**
 * 警告提示
 */
export const showWarning = (message: string, options?: any) => {
  return ElMessage.warning({
    message,
    ...defaultOptions,
    ...options
  })
}

/**
 * 信息提示
 */
export const showInfo = (message: string, options?: any) => {
  return ElMessage.info({
    message,
    ...defaultOptions,
    ...options
  })
}

/**
 * 确认对话框
 */
export const showConfirm = (
  message: string,
  title: string = '确认',
  options?: ElMessageBoxOptions
) => {
  return ElMessageBox.confirm(message, title, {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
    ...options
  })
}

/**
 * 删除确认对话框
 */
export const showDeleteConfirm = (
  itemName: string = '该数据',
  options?: ElMessageBoxOptions
) => {
  return showConfirm(
    `确定要删除${itemName}吗？此操作不可恢复！`,
    '删除确认',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
      ...options
    }
  )
}

/**
 * 批量删除确认对话框
 */
export const showBatchDeleteConfirm = (
  count: number,
  itemName: string = '数据',
  options?: ElMessageBoxOptions
) => {
  return showConfirm(
    `确定要删除选中的 ${count} 条${itemName}吗？此操作不可恢复！`,
    '批量删除确认',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
      ...options
    }
  )
}

/**
 * 操作成功提示（带更多信息）
 */
export const showOperationSuccess = (
  operation: string,
  details?: string
) => {
  const message = details ? `${operation}成功！${details}` : `${operation}成功！`
  return showSuccess(message)
}

/**
 * 操作失败提示（带错误信息）
 */
export const showOperationError = (
  operation: string,
  error?: any
) => {
  let errorMessage = `${operation}失败`
  if (error) {
    if (error.message) {
      errorMessage += `：${error.message}`
    } else if (typeof error === 'string') {
      errorMessage += `：${error}`
    }
  }
  return showError(errorMessage)
}

/**
 * 加载中提示
 */
export const showLoading = (message: string = '加载中...') => {
  return ElMessage({
    message,
    duration: 0,
    iconClass: 'el-icon-loading',
    customClass: 'loading-message'
  })
}

/**
 * 关闭所有消息
 */
export const closeAllMessages = () => {
  ElMessage.closeAll()
}
