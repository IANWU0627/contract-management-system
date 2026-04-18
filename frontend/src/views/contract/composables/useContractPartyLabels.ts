import { useI18n } from 'vue-i18n'

export function useContractPartyLabels() {
  const { t } = useI18n()

  const getPartyLabel = (type: string) => {
    const map: Record<string, string> = {
      partyA: t('contract.partyTypes.partyA'),
      partyB: t('contract.partyTypes.partyB'),
      partyC: t('contract.partyTypes.partyC'),
      partyD: t('contract.partyTypes.partyD'),
      partyE: t('contract.partyTypes.partyE'),
      partyF: t('contract.partyTypes.partyF'),
      partyG: t('contract.partyTypes.partyG'),
      partyH: t('contract.partyTypes.partyH'),
      partyI: t('contract.partyTypes.partyI'),
      partyJ: t('contract.partyTypes.partyJ')
    }
    return map[type] || type
  }

  const getPartyTagType = (type: string) => {
    const map: Record<string, string> = {
      partyA: 'primary',
      partyB: 'success',
      partyC: 'warning',
      partyD: 'info',
      partyE: 'danger',
      partyF: '',
      partyG: '',
      partyH: '',
      partyI: '',
      partyJ: ''
    }
    return map[type] || ''
  }

  return { getPartyLabel, getPartyTagType }
}
