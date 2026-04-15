import { createI18n } from 'vue-i18n'
import { messages } from './zh'

const i18n = createI18n({
  legacy: false,
  locale: localStorage.getItem('locale') || 'zh',
  fallbackLocale: 'zh',
  messages
})

export const t = (key: string) => i18n.global.t(key)
export default i18n
