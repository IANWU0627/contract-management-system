import { createI18n } from 'vue-i18n'
import { messages } from './zh'

const i18n = createI18n({
  legacy: false,
  locale: localStorage.getItem('locale') || 'zh',
  fallbackLocale: 'zh',
  messages
})

export const t = (key: string, params?: Record<string, unknown>) => i18n.global.t(key, params)
export default i18n
