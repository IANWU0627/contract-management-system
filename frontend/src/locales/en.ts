import { createI18n } from 'vue-i18n'

const messages = {
  en: {
    app: { title: 'Contract Management System' },
    common: {
      login: 'Login', logout: 'Logout', register: 'Register', search: 'Search',
      add: 'Add', edit: 'Edit', delete: 'Delete', save: 'Save', cancel: 'Cancel',
      confirm: 'Confirm', back: 'Back', submit: 'Submit', preview: 'Preview', reset: 'Reset',
      status: 'Status', action: 'Action', actions: 'Actions', more: 'More',
      yes: 'Yes', no: 'No', all: 'All', none: 'No Data',
      loading: 'Loading...', success: 'Success', saveDraft: 'Save Draft',
      error: 'Error', warning: 'Warning', confirmDelete: 'Are you sure to delete?',
      selectAll: 'Select All', batchDelete: 'Batch Delete',
      basicInfo: 'Basic Info', to: 'To', viewAll: 'View All', view: 'View', download: 'Download',
      notification: 'Notification Center', searchHistory: 'Search History', searchResults: 'Search Results',
      next: 'Next', prev: 'Previous',
      placeholder: { select: 'Please select', input: 'Please input' },
      error: { title: 'Something went wrong', retry: 'Retry', goHome: 'Go to Home' },
      menu: {
        dashboard: 'Dashboard', contracts: 'Contracts', templates: 'Templates',
        approvals: 'Approvals', reminders: 'Expiry Reminders', favorites: 'My Favorites',
        statistics: 'Statistics', users: 'User Management', settings: 'Settings',
        profile: 'Profile', operationLogs: 'Operation Logs', tagManagement: 'Tag Management',
        reminderRules: 'Reminder Rules', roleManagement: 'Role Management',
        typeFieldConfig: 'Type Field Config', renewals: 'Renewals',
        contractTools: 'Contract Tools', systemManagement: 'System',
        folderManagement: 'Folder Management', workflow: 'Workflow', analytics: 'Analytics'
      },
      dashboard: { title: 'Workbench', welcome: 'Welcome back' }
    },
    auth: {
      loginTitle: 'Contract Management System', loginSubtitle: 'Toy Contract',
      welcomeBack: 'Welcome back!', loginInfo: 'Please enter your login information',
      username: 'Username', password: 'Password', remember: 'Remember me',
      rememberDays: 'Remember me for 30 days', forgotPassword: 'Forgot password?',
      noAccount: "Don't have an account?", registerNow: 'Register now', login: 'Login',
      loggingIn: 'Logging in...', or: 'or',
      privacyPolicy: 'Privacy Policy', termsOfService: 'Terms of Service', contactUs: 'Contact Us',
      error: {
        username: 'Please enter username', password: 'Please enter password',
        passwordMin: 'Password must be at least 6 characters',
        loginFailed: 'Invalid username or password, please try again'
      }
    },
    contract: {
      title: 'Contract Management', contractNo: 'Contract No.', type: 'Type',
      amount: 'Amount', startDate: 'Start Date', endDate: 'End Date',
      counterparty: 'Counterparty', content: 'Content',
      selectTemplate: 'Select Template', generateFile: 'Generate File',
      noContractFile: 'No contract file', contractFile: 'Contract File',
      contractContent: 'Contract Content', supportFiles: 'Support Files',
      generated: 'Generated', preview: 'Preview', download: 'Download', upload: 'Upload',
      statuses: {
        DRAFT: 'Draft', PENDING: 'Pending', APPROVING: 'Approving',
        APPROVED: 'Approved', REJECTED: 'Rejected', SIGNED: 'Signed',
        EXPIRED: 'Expired', TERMINATED: 'Terminated', ARCHIVED: 'Archived'
      }
    },
    settings: {
      title: 'Settings', appearance: 'Appearance', theme: 'Theme',
      themeLight: 'Light', themeDark: 'Dark', themeSystem: 'System',
      language: 'Language', langChinese: '中文', langEnglish: 'English'
    }
  }
}

export const en = messages.en
export { messages }