import { describe, it, expect, beforeEach, vi } from 'vitest'
import { shallowMount } from '@vue/test-utils'
import { nextTick, ref } from 'vue'
import Dashboard from '../Dashboard.vue'

const {
  getMock,
  getContractCategoriesMock,
  getFavoritesMock,
  getExpiringWorkbenchSummaryMock,
  startRenewalFlowMock,
  createReminderMock,
  getMyRemindersMock,
  confirmMock,
  messageSuccessMock,
  messageWarningMock
} = vi.hoisted(() => ({
  getMock: vi.fn(),
  getContractCategoriesMock: vi.fn(),
  getFavoritesMock: vi.fn(),
  getExpiringWorkbenchSummaryMock: vi.fn(),
  startRenewalFlowMock: vi.fn(),
  createReminderMock: vi.fn(),
  getMyRemindersMock: vi.fn(),
  confirmMock: vi.fn(),
  messageSuccessMock: vi.fn(),
  messageWarningMock: vi.fn()
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() })
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    locale: ref('zh')
  })
}))

vi.mock('element-plus', () => ({
  ElMessage: {
    success: messageSuccessMock,
    warning: messageWarningMock,
    error: vi.fn()
  },
  ElMessageBox: {
    confirm: confirmMock
  }
}))

vi.mock('@element-plus/icons-vue', () => {
  const icon = { template: '<span />' }
  return {
    Lightning: icon,
    DataAnalysis: icon,
    Search: icon,
    Star: icon,
    StarFilled: icon,
    Document: icon,
    Money: icon,
    Timer: icon,
    Bell: icon,
    Plus: icon,
    FolderOpened: icon,
    RefreshRight: icon,
    MoreFilled: icon,
    ArrowDown: icon
  }
})

vi.mock('@/api', () => ({
  get: getMock
}))

vi.mock('@/api/contractCategory', () => ({
  getContractCategories: getContractCategoriesMock
}))

vi.mock('@/api/favorite', () => ({
  addFavorite: vi.fn(),
  removeFavorite: vi.fn(),
  getFavorites: getFavoritesMock
}))

vi.mock('@/api/contract', () => ({
  getExpiringWorkbenchSummary: getExpiringWorkbenchSummaryMock,
  startRenewalFlow: startRenewalFlowMock
}))

vi.mock('@/api/reminder', () => ({
  createReminder: createReminderMock,
  getMyReminders: getMyRemindersMock
}))

vi.mock('roughjs', () => ({
  default: {
    canvas: () => ({
      line: vi.fn(),
      rectangle: vi.fn()
    })
  }
}))

vi.mock('@/utils/currency', () => ({
  DEFAULT_CURRENCY: 'CNY',
  formatAmountByLocale: (amount: number) => String(amount ?? 0),
  getCurrencySymbol: () => '¥'
}))

vi.mock('@/components/SkeletonLoader.vue', () => ({
  default: { template: '<div />' }
}))

vi.mock('@/stores/user', () => ({
  useUserStore: () => ({
    userInfo: { id: 1 }
  })
}))

const flush = async () => {
  await Promise.resolve()
  await Promise.resolve()
  await Promise.resolve()
}

const mountDashboard = async () => {
  const wrapper = shallowMount(Dashboard, {
    global: {
      stubs: {
        'el-row': true,
        'el-col': true,
        'el-card': { template: '<div><slot name="header" /><slot /></div>' },
        'el-input': true,
        'el-select': true,
        'el-option': true,
        'el-button': true,
        'el-table': { template: '<div><slot /></div>' },
        'el-table-column': { template: '<div><slot :row="{}" /></div>' },
        'el-link': true,
        'el-dropdown': true,
        'el-dropdown-menu': true,
        'el-dropdown-item': true,
        'el-pagination': true,
        'el-tag': true,
        'el-segmented': true,
        'el-radio-group': true,
        'el-radio-button': true,
        'el-switch': true,
        'el-checkbox': true,
        'el-dialog': { template: '<div><slot /></div>' },
        'el-icon': true
      },
      directives: {
        loading: () => {}
      }
    }
  })
  await flush()
  return wrapper
}

describe('Dashboard workbench batch operations', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    confirmMock.mockResolvedValue(true)
    getContractCategoriesMock.mockResolvedValue({ data: [] })
    getFavoritesMock.mockResolvedValue({ data: { list: [] } })
    getMock.mockImplementation((url: string) => {
      if (url.startsWith('/statistics/overview')) {
        return Promise.resolve({ data: { totalContracts: 0, totalAmount: 0, pendingApproval: 0, expiringSoon: 0 } })
      }
      if (url.startsWith('/contracts?')) {
        return Promise.resolve({ data: { list: [], total: 0 } })
      }
      return Promise.resolve({ data: {} })
    })
    getExpiringWorkbenchSummaryMock.mockResolvedValue({
      data: {
        overview: { today: 0, within7Days: 0, within30Days: 3 },
        oneDay: [],
        sevenDays: [],
        thirtyDays: [
          { id: 1, contractNo: 'C-001', title: 'A', daysRemaining: 3, endDate: '2030-01-01', recommendedAction: 'startRenewal' },
          { id: 2, contractNo: 'C-002', title: 'B', daysRemaining: 4, endDate: '2030-01-02', recommendedAction: 'view' },
          { id: 3, contractNo: 'C-003', title: 'C', daysRemaining: 5, endDate: '2030-01-03', recommendedAction: 'startRenewal' }
        ]
      }
    })
    getMyRemindersMock.mockResolvedValue({ data: { list: [] } })
  })

  it('batch renewal should execute only renewable contracts and record skipped', async () => {
    const wrapper = await mountDashboard()
    ;(wrapper.vm as any).expiringWorkbench = {
      oneDay: [],
      sevenDays: [],
      thirtyDays: [
        { id: 1, contractNo: 'C-001', title: 'A', daysRemaining: 3, endDate: '2030-01-01', recommendedAction: 'startRenewal' },
        { id: 2, contractNo: 'C-002', title: 'B', daysRemaining: 4, endDate: '2030-01-02', recommendedAction: 'view' },
        { id: 3, contractNo: 'C-003', title: 'C', daysRemaining: 5, endDate: '2030-01-03', recommendedAction: 'startRenewal' }
      ]
    }
    ;(wrapper.vm as any).expiringBucket = 'thirtyDays'
    ;(wrapper.vm as any).selectedExpiringIds = [1, 2, 3]
    await nextTick()

    await (wrapper.vm as any).handleBatchRenewal()

    expect(startRenewalFlowMock).toHaveBeenCalledTimes(2)
    expect(startRenewalFlowMock).toHaveBeenCalledWith(1, 'dashboard.workbench.batchRenewalReason')
    expect(startRenewalFlowMock).toHaveBeenCalledWith(3, 'dashboard.workbench.batchRenewalReason')
    expect((wrapper.vm as any).batchResultDialogVisible).toBe(true)
    expect((wrapper.vm as any).batchResult.skipped).toContain('C-002')
  })

  it('batch reminder should show success/skipped/failed details', async () => {
    const wrapper = await mountDashboard()
    ;(wrapper.vm as any).expiringWorkbench = {
      oneDay: [],
      sevenDays: [],
      thirtyDays: [
        { id: 1, contractNo: 'C-001', title: 'A', daysRemaining: 3, endDate: '2030-01-01', recommendedAction: 'startRenewal' },
        { id: 2, contractNo: 'C-002', title: 'B', daysRemaining: 4, endDate: '2030-01-02', recommendedAction: 'view' },
        { id: 3, contractNo: 'C-003', title: 'C', daysRemaining: 5, endDate: '2030-01-03', recommendedAction: 'startRenewal' }
      ]
    }
    ;(wrapper.vm as any).expiringBucket = 'thirtyDays'
    ;(wrapper.vm as any).selectedExpiringIds = [1, 2, 3]
    await nextTick()
    getMyRemindersMock.mockResolvedValue({
      data: { list: [{ contractId: 2, status: 0 }] }
    })
    createReminderMock.mockImplementation((payload: any) => {
      if (payload.contractId === 3) {
        return Promise.reject(new Error('fail'))
      }
      return Promise.resolve({ data: {} })
    })

    await (wrapper.vm as any).handleBatchReminder()

    expect(createReminderMock).toHaveBeenCalledTimes(2)
    expect((wrapper.vm as any).batchResultDialogVisible).toBe(true)
    expect((wrapper.vm as any).batchResult.success).toContain('C-001')
    expect((wrapper.vm as any).batchResult.skipped).toContain('C-002')
    expect((wrapper.vm as any).batchResult.failed).toContain('C-003')
  })

  it('batch reminder should split created and reused and show detailed success message', async () => {
    const wrapper = await mountDashboard()
    ;(wrapper.vm as any).expiringWorkbench = {
      oneDay: [],
      sevenDays: [],
      thirtyDays: [
        { id: 1, contractNo: 'C-001', title: 'A', daysRemaining: 3, endDate: '2030-01-01', recommendedAction: 'startRenewal' },
        { id: 2, contractNo: 'C-002', title: 'B', daysRemaining: 4, endDate: '2030-01-02', recommendedAction: 'view' }
      ]
    }
    ;(wrapper.vm as any).expiringBucket = 'thirtyDays'
    ;(wrapper.vm as any).selectedExpiringIds = [1, 2]
    await nextTick()

    getMyRemindersMock.mockResolvedValue({ data: { list: [] } })
    createReminderMock.mockImplementation((payload: any) => {
      if (payload.contractId === 1) {
        return Promise.resolve({ data: { existed: false } })
      }
      return Promise.resolve({ data: { existed: true } })
    })

    await (wrapper.vm as any).handleBatchReminder()

    expect((wrapper.vm as any).batchResult.success).toEqual(['C-001'])
    expect((wrapper.vm as any).batchResult.reused).toEqual(['C-002'])
    expect(messageSuccessMock).toHaveBeenCalledWith('dashboard.workbench.batchReminderSuccessDetailed')
  })

  it('should copy failed batch reminder list to clipboard', async () => {
    const wrapper = await mountDashboard()
    const writeTextMock = vi.fn().mockResolvedValue(undefined)
    vi.stubGlobal('navigator', {
      clipboard: {
        writeText: writeTextMock
      }
    })

    ;(wrapper.vm as any).batchResult = {
      success: [],
      reused: [],
      skipped: [],
      failed: ['C-001', 'C-002'],
      failedDetails: [
        { name: 'C-001', reason: '未知原因' },
        { name: 'C-002', reason: '未知原因' }
      ]
    }

    await (wrapper.vm as any).copyBatchFailedList()

    expect(writeTextMock).toHaveBeenCalledWith('C-001\nC-002')
    expect(messageSuccessMock).toHaveBeenCalledWith('dashboard.workbench.copyFailedListSuccess')
  })

  it('should filter failed list by selected reason before copy', async () => {
    const wrapper = await mountDashboard()
    const writeTextMock = vi.fn().mockResolvedValue(undefined)
    vi.stubGlobal('navigator', {
      clipboard: {
        writeText: writeTextMock
      }
    })

    ;(wrapper.vm as any).batchResult = {
      success: [],
      reused: [],
      skipped: [],
      failed: ['C-001', 'C-002', 'C-003'],
      failedDetails: [
        { name: 'C-001', reason: '权限不足' },
        { name: 'C-002', reason: '网络异常' },
        { name: 'C-003', reason: '权限不足' }
      ]
    }
    ;(wrapper.vm as any).selectedFailedReason = '权限不足'

    await (wrapper.vm as any).copyBatchFailedList()

    expect(writeTextMock).toHaveBeenCalledWith('C-001\nC-003')
  })

  it('should retry single failed reminder and move it to success list', async () => {
    const wrapper = await mountDashboard()
    createReminderMock.mockResolvedValue({ data: { existed: false } })

    ;(wrapper.vm as any).batchResult = {
      success: [],
      reused: [],
      skipped: [],
      failed: ['C-001'],
      failedDetails: [{ name: 'C-001', reason: '权限不足' }],
      failedEntries: [{ id: 1, name: 'C-001', contractNo: 'C-001', contractTitle: 'A', expireDate: '2030-01-01', remindDays: 3, reason: '权限不足' }]
    }

    await (wrapper.vm as any).retrySingleFailedReminder((wrapper.vm as any).batchResult.failedEntries[0])

    expect(createReminderMock).toHaveBeenCalledTimes(1)
    expect((wrapper.vm as any).batchResult.success).toContain('C-001')
    expect((wrapper.vm as any).batchResult.failed).toEqual([])
    expect(messageSuccessMock).toHaveBeenCalledWith('dashboard.workbench.retrySingleSuccess')
  })

  it('should filter expiring list to only mine when enabled', async () => {
    const wrapper = await mountDashboard()
    ;(wrapper.vm as any).expiringWorkbench = {
      oneDay: [],
      sevenDays: [],
      thirtyDays: [
        { id: 1, contractNo: 'C-001', creatorId: 1, recommendedAction: 'view' },
        { id: 2, contractNo: 'C-002', creatorId: 2, recommendedAction: 'view' }
      ]
    }
    ;(wrapper.vm as any).expiringBucket = 'thirtyDays'
    ;(wrapper.vm as any).workbenchOnlyMine = true
    await nextTick()

    expect((wrapper.vm as any).activeExpiringList).toHaveLength(1)
    expect((wrapper.vm as any).activeExpiringList[0].contractNo).toBe('C-001')
  })

  it('should filter expiring list to only executable when enabled', async () => {
    const wrapper = await mountDashboard()
    ;(wrapper.vm as any).expiringWorkbench = {
      oneDay: [],
      sevenDays: [],
      thirtyDays: [
        { id: 1, contractNo: 'C-001', recommendedAction: 'startRenewal' },
        { id: 2, contractNo: 'C-002', recommendedAction: 'view' },
        { id: 3, contractNo: 'C-003', recommendedAction: 'view' }
      ]
    }
    ;(wrapper.vm as any).activeReminderContractIds = new Set([2])
    ;(wrapper.vm as any).expiringBucket = 'thirtyDays'
    ;(wrapper.vm as any).workbenchOnlyExecutable = true
    await nextTick()

    expect((wrapper.vm as any).activeExpiringList.map((item: any) => item.contractNo)).toEqual(['C-001', 'C-003'])
  })

  it('should persist workbench filter toggles to localStorage', async () => {
    const wrapper = await mountDashboard()
    ;(wrapper.vm as any).workbenchOnlyMine = true
    ;(wrapper.vm as any).workbenchOnlyExecutable = true
    await nextTick()

    const raw = localStorage.getItem('dashboard_workbench_filter_pref')
    expect(raw).toBeTruthy()
    expect(JSON.parse(raw as string)).toEqual({
      onlyMine: true,
      onlyExecutable: true
    })
  })

  it('should compute executable renewal and reminder counts from filtered list', async () => {
    const wrapper = await mountDashboard()
    ;(wrapper.vm as any).expiringWorkbench = {
      oneDay: [],
      sevenDays: [],
      thirtyDays: [
        { id: 1, contractNo: 'C-001', creatorId: 1, recommendedAction: 'startRenewal' },
        { id: 2, contractNo: 'C-002', creatorId: 1, recommendedAction: 'view' },
        { id: 3, contractNo: 'C-003', creatorId: 2, recommendedAction: 'startRenewal' }
      ]
    }
    ;(wrapper.vm as any).activeReminderContractIds = new Set([2])
    ;(wrapper.vm as any).expiringBucket = 'thirtyDays'
    ;(wrapper.vm as any).workbenchOnlyMine = true
    await nextTick()

    expect((wrapper.vm as any).executableRenewalCount).toBe(1)
    expect((wrapper.vm as any).executableReminderCount).toBe(1)
  })
})
