import { describe, expect, it, vi, beforeEach } from 'vitest'
import { shallowMount } from '@vue/test-utils'
import ContractList from '../ContractList.vue'

const {
  getContractListMock,
  getContractCategoriesMock,
  getAllFoldersMock
} = vi.hoisted(() => ({
  getContractListMock: vi.fn(),
  getContractCategoriesMock: vi.fn(),
  getAllFoldersMock: vi.fn()
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn()
  })
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    locale: { value: 'zh' }
  })
}))

vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() },
  ElMessageBox: { confirm: vi.fn(), prompt: vi.fn() }
}))

vi.mock('@element-plus/icons-vue', () => {
  const icon = { template: '<span />' }
  return {
    Search: icon,
    Refresh: icon,
    Plus: icon,
    ArrowDown: icon,
    Delete: icon,
    Edit: icon,
    More: icon,
    Download: icon,
    CircleCheck: icon,
    TopRight: icon,
    Document: icon,
    Warning: icon,
    InfoFilled: icon,
    MagicStick: icon,
    Collection: icon,
    ChatLineRound: icon,
    List: icon
  }
})

vi.mock('@/api/contract', () => ({
  getContractList: getContractListMock,
  deleteContract: vi.fn(),
  analyzeContract: vi.fn(),
  batchDeleteContracts: vi.fn(),
  batchUpdateStatus: vi.fn(),
  batchApprove: vi.fn(),
  batchSubmit: vi.fn(),
  exportContractsExcel: vi.fn(),
  batchEdit: vi.fn(),
  submitContract: vi.fn(),
  approveContract: vi.fn(),
  rejectContract: vi.fn(),
  signContract: vi.fn(),
  terminateContract: vi.fn(),
  startRenewalFlow: vi.fn(),
  completeRenewalFlow: vi.fn(),
  declineRenewalFlow: vi.fn()
}))

vi.mock('@/api/contractCategory', () => ({
  getContractCategories: getContractCategoriesMock
}))

vi.mock('@/api/folder', () => ({
  getAllFolders: getAllFoldersMock
}))

vi.mock('@/api/clause', () => ({ createClause: vi.fn() }))
vi.mock('@/api/contractTypeField', () => ({ getContractTypeFieldConfig: vi.fn().mockResolvedValue({ data: { fields: [] } }) }))
vi.mock('@/api/quickCode', () => ({ getQuickCodeByCode: vi.fn().mockResolvedValue({ data: [] }) }))
vi.mock('@/api/system', () => ({ getSystemConfigs: vi.fn().mockResolvedValue({ data: {} }) }))
vi.mock('@/components/EmptyState.vue', () => ({ default: { template: '<div />' } }))

describe('ContractList', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    getContractListMock.mockResolvedValue({ data: { list: [], total: 0 } })
    getContractCategoriesMock.mockResolvedValue({ data: [] })
    getAllFoldersMock.mockResolvedValue({ data: [] })
  })

  it('mounts and requests contract list on load', async () => {
    shallowMount(ContractList, {
      global: {
        stubs: {
          'el-icon': true,
          'el-button': true,
          'el-input': true,
          'el-option': true,
          'el-select': true,
          'el-date-picker': true,
          'el-input-number': true,
          'el-checkbox': true,
          'el-dropdown-item': true,
          'el-tag': true,
          'el-dropdown-menu': true,
          'el-dropdown': true,
          'el-link': true,
          'el-pagination': true,
          'el-progress': true,
          'el-alert': true,
          'el-descriptions-item': true,
          'el-descriptions': true,
          'el-dialog': true,
          'el-form-item': true,
          'el-form': true,
          'el-table': { template: '<div><slot /></div>' },
          'el-table-column': { template: '<div><slot :row="{}" /></div>' }
        },
        mocks: {
          $t: (key: string) => key,
          $router: { push: vi.fn() }
        },
        directives: {
          loading: () => {}
        },
        config: {
          compilerOptions: {
            isCustomElement: (tag) => tag.startsWith('el-')
          }
        }
      }
    })

    await Promise.resolve()
    await Promise.resolve()

    expect(getContractListMock).toHaveBeenCalledTimes(1)
  })
})
