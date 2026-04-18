import { ref, computed, type ComputedRef } from 'vue'
import { getContractList, type Contract } from '@/api/contract'
import { getContractCategories, type ContractCategory } from '@/api/contractCategory'
import { getAllFolders } from '@/api/folder'
import type { ContractFolderRow } from '@/views/contract/contractFormTypes'

export function useContractFormCatalog(isEdit: ComputedRef<boolean>, contractId: ComputedRef<number>) {
  const categories = ref<ContractCategory[]>([])
  const folders = ref<ContractFolderRow[]>([])
  const parentContracts = ref<Contract[]>([])

  const parentContractOptions = computed(() =>
    parentContracts.value.filter(
      (c) => (!isEdit.value || c.id !== contractId.value) && (c.relationType || 'MAIN') !== 'SUPPLEMENT'
    )
  )

  const loadCategories = async () => {
    try {
      const res = await getContractCategories()
      categories.value = (res.data || []) as ContractCategory[]
    } catch (error) {
      console.error('Failed to load categories:', error)
    }
  }

  const loadFolders = async () => {
    try {
      const res = await getAllFolders()
      folders.value = (res.data || []) as ContractFolderRow[]
    } catch (error) {
      console.error('Failed to load folders:', error)
    }
  }

  const loadParentContracts = async () => {
    try {
      const res = await getContractList({ page: 1, pageSize: 300, status: 'APPROVED,SIGNED,ARCHIVED,RENEWING,RENEWED' })
      parentContracts.value = res.data.list
    } catch (error) {
      console.error('Failed to load parent contracts:', error)
      parentContracts.value = []
    }
  }

  return {
    categories,
    folders,
    parentContracts,
    parentContractOptions,
    loadCategories,
    loadFolders,
    loadParentContracts
  }
}
