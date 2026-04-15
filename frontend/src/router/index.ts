import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { t } from '@/locales'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: 'auth.loginTitle', public: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: 'menu.dashboard', roles: ['ADMIN', 'LEGAL', 'USER'] }
      },
      {
        path: 'contracts',
        name: 'Contracts',
        component: () => import('@/views/contract/ContractList.vue'),
        meta: { title: 'menu.contracts', roles: ['ADMIN', 'LEGAL', 'USER'] }
      },
      {
        path: 'contracts/create',
        name: 'ContractCreate',
        component: () => import('@/views/contract/ContractForm.vue'),
        meta: { title: 'contract.create', roles: ['ADMIN', 'LEGAL', 'USER'] }
      },
      {
        path: 'contracts/:id',
        name: 'ContractDetail',
        component: () => import('@/views/contract/ContractDetail.vue'),
        meta: { title: 'contract.detail', roles: ['ADMIN', 'LEGAL', 'USER'] }
      },
      {
        path: 'contracts/:id/edit',
        name: 'ContractEdit',
        component: () => import('@/views/contract/ContractForm.vue'),
        meta: { title: 'contract.edit', roles: ['ADMIN', 'LEGAL', 'USER'] }
      },
      {
        path: 'templates',
        name: 'Templates',
        component: () => import('@/views/template/TemplateList.vue'),
        meta: { title: 'menu.templates', roles: ['ADMIN', 'LEGAL', 'USER'] }
      },
      {
        path: 'categories',
        name: 'ContractCategories',
        component: () => import('@/views/contract/ContractCategory.vue'),
        meta: { title: 'contract.categoryManagement', roles: ['ADMIN'] }
      },
      {
        path: 'quick-codes',
        name: 'QuickCodes',
        component: () => import('@/views/system/QuickCode.vue'),
        meta: { title: 'quickCode.management', roles: ['ADMIN'] }
      },
      {
        path: 'templates/create',
        name: 'TemplateCreate',
        component: () => import('@/views/template/TemplateForm.vue'),
        meta: { title: 'template.create', roles: ['ADMIN', 'LEGAL'] }
      },
      {
        path: 'templates/:id',
        name: 'TemplateDetail',
        component: () => import('@/views/template/TemplateDetail.vue'),
        meta: { title: 'template.preview', roles: ['ADMIN', 'LEGAL', 'USER'] }
      },
      {
        path: 'templates/:id/edit',
        name: 'TemplateEdit',
        component: () => import('@/views/template/TemplateForm.vue'),
        meta: { title: 'template.edit', roles: ['ADMIN', 'LEGAL'] }
      },
      {
        path: 'approvals',
        name: 'Approvals',
        component: () => import('@/views/contract/ApprovalList.vue'),
        meta: { title: 'menu.approvals', roles: ['ADMIN', 'LEGAL'] }
      },
      {
        path: 'reminders',
        name: 'Reminders',
        component: () => import('@/views/contract/ReminderList.vue'),
        meta: { title: 'menu.reminders', roles: ['ADMIN', 'LEGAL'] }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/Statistics.vue'),
        meta: { title: 'menu.statistics', roles: ['ADMIN', 'LEGAL'] }
      },
      {
        path: 'logs',
        name: 'OperationLogs',
        component: () => import('@/views/OperationLogs.vue'),
        meta: { title: 'menu.operationLogs', roles: ['ADMIN'] }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/user/UserList.vue'),
        meta: { title: 'menu.users', roles: ['ADMIN'] }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue'),
        meta: { title: 'menu.settings', roles: ['ADMIN'] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: 'menu.profile', roles: ['ADMIN', 'LEGAL', 'USER'] }
      },
      {
        path: 'favorites',
        name: 'Favorites',
        component: () => import('@/views/contract/Favorites.vue'),
        meta: { title: 'menu.favorites', roles: ['ADMIN', 'LEGAL', 'USER'] }
      },
      {
        path: 'tags',
        name: 'TagManagement',
        component: () => import('@/views/contract/TagManagement.vue'),
        meta: { title: 'menu.tagManagement', roles: ['ADMIN'] }
      },
      {
        path: 'reminder-rules',
        name: 'ReminderRules',
        component: () => import('@/views/contract/ReminderRules.vue'),
        meta: { title: 'menu.reminderRules', roles: ['ADMIN'] }
      },
      {
        path: 'renewals',
        name: 'Renewals',
        component: () => import('@/views/contract/Renewal.vue'),
        meta: { title: 'menu.renewals', roles: ['ADMIN', 'LEGAL'] }
      },
      {
        path: 'change-logs',
        name: 'ChangeLogs',
        component: () => import('@/views/contract/ChangeLog.vue'),
        meta: { title: 'contract.changeLog', roles: ['ADMIN'] }
      },
      {
        path: 'folders',
        name: 'FolderManagement',
        component: () => import('@/views/folder/FolderManagement.vue'),
        meta: { title: 'menu.folderManagement', roles: ['ADMIN', 'LEGAL'] }
      },
      {
        path: 'roles',
        name: 'RoleManagement',
        component: () => import('@/views/system/RoleManagement.vue'),
        meta: { title: 'menu.roleManagement', roles: ['ADMIN'] }
      },
      {
        path: 'type-field-config',
        name: 'ContractTypeFieldConfig',
        component: () => import('@/views/system/ContractTypeFieldConfig.vue'),
        meta: { title: 'typeFieldConfig.title', roles: ['ADMIN'] }
      },
      {
        path: 'variable-management',
        name: 'VariableManagement',
        component: () => import('@/views/system/VariableManagement.vue'),
        meta: { title: 'variable.title', roles: ['ADMIN'] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 页面标题翻译映射
const titleMap: Record<string, { zh: string; en: string }> = {
  'auth.loginTitle': { zh: '登录', en: 'Login' },
  'menu.dashboard': { zh: '工作台', en: 'Dashboard' },
  'menu.contracts': { zh: '合同管理', en: 'Contracts' },
  'contract.create': { zh: '新建合同', en: 'Create Contract' },
  'contract.detail': { zh: '合同详情', en: 'Contract Detail' },
  'contract.edit': { zh: '编辑合同', en: 'Edit Contract' },
  'contract.categoryManagement': { zh: '合同分类管理', en: 'Category Management' },
  'menu.templates': { zh: '模板管理', en: 'Templates' },
  'template.create': { zh: '新建模板', en: 'Create Template' },
  'template.preview': { zh: '模板预览', en: 'Template Preview' },
  'menu.approvals': { zh: '审批管理', en: 'Approvals' },
  'menu.reminders': { zh: '到期提醒', en: 'Reminders' },
  'menu.statistics': { zh: '统计报表', en: 'Statistics' },
  'menu.operationLogs': { zh: '操作日志', en: 'Operation Logs' },
  'menu.users': { zh: '用户管理', en: 'Users' },
  'menu.settings': { zh: '系统设置', en: 'Settings' },
  'menu.profile': { zh: '个人中心', en: 'Profile' }
}

// 路由守卫
router.beforeEach(async (to, from) => {
  const userStore = useUserStore()

  // 设置页面标题（支持多语言）
  const titleKey = to.meta.title as string
  const locale = localStorage.getItem('locale') || 'zh'

  if (titleKey && titleMap[titleKey]) {
    const pageTitle = titleMap[titleKey][locale as 'zh' | 'en'] || titleKey
    const appTitle = localStorage.getItem('systemName') || (locale === 'en' ? 'Contract Management System' : '合同管理系统')
    document.title = `${pageTitle} - ${appTitle}`
  } else {
    document.title = localStorage.getItem('systemName') || (locale === 'en' ? 'Contract Management System' : '合同管理系统')
  }

  // 公开页面直接访问
  if (to.meta.public) {
    return true
  }

  // 检查登录状态
  if (!userStore.isLoggedIn) {
    return '/login'
  }

  // 如果用户角色为空，尝试获取用户信息
  if (userStore.roles.length === 0) {
    await userStore.fetchUserInfo()
    // fetchUserInfo 失败会触发 logout，这里必须二次校验登录态
    if (!userStore.isLoggedIn) {
      return '/login'
    }
  }

  // 获取用户角色
  const userRoles = userStore.roles
  const allowedRoles = to.meta.roles as string[] | undefined

  // 检查角色是否有权限访问
  if (allowedRoles) {
    if (userRoles.length === 0) {
      ElMessage.warning(t('common.noPermission'))
      return '/dashboard'
    }
    const hasAccess = userRoles.some(role => allowedRoles.includes(role))
    if (!hasAccess) {
      ElMessage.warning(t('common.noPermission'))
      return '/dashboard'
    }
  }

  return true
})

// 导出权限检查函数
export const hasPermission = (roles: string[], allowedRoles: string[]): boolean => {
  return roles.some(role => allowedRoles.includes(role))
}

export default router
