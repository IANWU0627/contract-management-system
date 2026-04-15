export const DEFAULT_CURRENCY = 'CNY'

export const getCurrencySymbol = (currency?: string) => {
  switch (currency) {
    case 'USD':
      return '$'
    case 'EUR':
      return 'EUR '
    case 'HKD':
      return 'HK$'
    default:
      return '¥'
  }
}

export const getLocaleCode = (locale?: string) => {
  return locale === 'en' ? 'en-US' : 'zh-CN'
}

export const formatAmountByLocale = (amount: number | null | undefined, locale?: string) => {
  if (!amount) return '0'
  return new Intl.NumberFormat(getLocaleCode(locale)).format(amount)
}
