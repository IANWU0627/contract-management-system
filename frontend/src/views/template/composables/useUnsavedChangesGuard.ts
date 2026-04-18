import { onBeforeUnmount, onMounted } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'

/**
 * 路由离开与关闭页签前提示未保存更改。
 */
export function useUnsavedChangesGuard(isDirty: () => boolean) {
  const { t } = useI18n()

  onBeforeRouteLeave((_to, _from, next) => {
    if (!isDirty()) {
      next()
      return
    }
    ElMessageBox.confirm(t('template.leaveUnsavedMessage'), t('template.leaveUnsavedTitle'), {
      type: 'warning',
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel')
    })
      .then(() => next())
      .catch(() => next(false))
  })

  const onBeforeUnload = (e: BeforeUnloadEvent) => {
    if (!isDirty()) return
    e.preventDefault()
    e.returnValue = ''
  }

  onMounted(() => {
    window.addEventListener('beforeunload', onBeforeUnload)
  })

  onBeforeUnmount(() => {
    window.removeEventListener('beforeunload', onBeforeUnload)
  })
}
