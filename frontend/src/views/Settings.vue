<template>
  <div class="settings">
    <h1 class="page-title">{{ $t('menu.settings') }}</h1>
    
    <el-tabs v-model="activeTab" class="settings-tabs">
      <!-- 基础设置 -->
      <el-tab-pane :label="t('settings.basicSettings')" name="basic">
        <el-card shadow="hover">
          <el-form label-width="140px">
            <el-divider content-position="left">{{ t('settings.appearance') }}</el-divider>
            
            <el-form-item :label="t('settings.theme')">
              <el-radio-group v-model="theme" @change="handleThemeChange">
                <el-radio value="light">{{ t('settings.themeLight') }}</el-radio>
                <el-radio value="dark">{{ t('settings.themeDark') }}</el-radio>
                <el-radio value="system">{{ t('settings.themeSystem') }}</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item :label="t('settings.language')">
              <el-select v-model="locale" @change="handleLocaleChange" style="width: 200px">
                <el-option :label="t('settings.langChinese')" value="zh" />
                <el-option :label="t('settings.langEnglish')" value="en" />
              </el-select>
            </el-form-item>
            
            <el-divider content-position="left">{{ t('settings.system') }}</el-divider>
            
            <el-form-item :label="t('settings.systemName')">
              <el-input v-model="systemName" style="width: 300px" />
            </el-form-item>

            <el-divider content-position="left">{{ t('settings.dataBackup') }}</el-divider>

            <el-alert
              :title="t('settings.dataBackupStatusTitle')"
              :description="t('settings.dataBackupStatusDesc')"
              type="warning"
              :closable="false"
              show-icon
              style="margin-bottom: 16px"
            />

            <el-form-item :label="t('settings.exportData')">
              <el-tag type="info">{{ t('settings.comingSoon') }}</el-tag>
            </el-form-item>
            <el-form-item :label="t('settings.importData')">
              <el-tag type="info">{{ t('settings.comingSoon') }}</el-tag>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleSaveBasic">{{ t('settings.saveSettings') }}</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      
      <!-- 通知设置 -->
      <el-tab-pane :label="t('settings.notificationSettings')" name="notification">
        <el-card shadow="hover">
          <template #header><span>{{ t('settings.notificationSettings') }}</span></template>

          <div class="notification-section">
            <h3 class="section-title">{{ t('settings.notificationPolicy') }}</h3>

            <el-form label-width="180px">
              <el-form-item :label="t('settings.expirationReminder')">
                <el-switch v-model="notificationSettings.expirationReminder" />
              </el-form-item>
              <el-form-item :label="t('settings.approvalNotification')">
                <el-switch v-model="notificationSettings.approvalNotification" />
              </el-form-item>
              <el-form-item :label="t('settings.commentNotification')">
                <el-switch v-model="notificationSettings.commentNotification" />
              </el-form-item>
              <el-form-item :label="t('settings.emailChannel')">
                <el-switch v-model="notificationSettings.emailNotification" />
              </el-form-item>
              <el-form-item :label="t('settings.systemChannel')">
                <el-switch v-model="notificationSettings.systemNotification" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSaveNotificationSettings">{{ t('settings.saveNotificationPolicy') }}</el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <!-- 邮件通知 -->
          <div class="notification-section">
            <h3 class="section-title">{{ t('settings.emailNotification') }}</h3>
            
            <el-form label-width="160px">
              <el-form-item :label="t('settings.smtpHost')">
                <el-input v-model="emailSettings.smtpHost" :placeholder="t('settings.smtpHostPlaceholder')" style="width: 400px" />
              </el-form-item>
              
              <el-form-item :label="t('settings.smtpPort')">
                <el-input-number v-model="emailSettings.smtpPort" :min="1" :max="65535" style="width: 200px" />
              </el-form-item>
              
              <el-form-item :label="t('settings.smtpFrom')">
                <el-input v-model="emailSettings.smtpFrom" :placeholder="t('settings.smtpFromPlaceholder')" style="width: 400px" />
              </el-form-item>
              
              <el-form-item :label="t('settings.smtpUsername')">
                <el-input v-model="emailSettings.smtpUsername" :placeholder="t('settings.smtpUsernamePlaceholder')" style="width: 400px" />
              </el-form-item>
              
              <el-form-item :label="t('settings.smtpPassword')">
                <el-input v-model="emailSettings.smtpPassword" type="password" :placeholder="t('settings.smtpPasswordPlaceholder')" style="width: 400px" show-password />
              </el-form-item>
              
              <el-form-item :label="t('settings.smtpUseSSL')">
                <el-switch v-model="emailSettings.smtpUseSSL" />
              </el-form-item>
              
              <el-form-item>
                <el-button @click="handleTestEmail">{{ t('settings.testEmail') }}</el-button>
                <el-button type="primary" @click="handleSaveEmailSettings">{{ t('settings.saveSettings') }}</el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <!-- 短信通知 -->
          <div class="notification-section">
            <h3 class="section-title">{{ t('settings.smsNotification') }}</h3>
            
            <el-form label-width="160px">
              <el-form-item :label="t('settings.smsProvider')">
                <el-select v-model="smsSettings.provider" style="width: 400px">
                  <el-option :label="t('settings.smsProviderAliyun')" value="aliyun" />
                  <el-option :label="t('settings.smsProviderTencent')" value="tencent" />
                  <el-option :label="t('settings.smsProviderHuawei')" value="huawei" />
                  <el-option :label="t('settings.smsProviderCustom')" value="custom" />
                </el-select>
              </el-form-item>
              
              <template v-if="smsSettings.provider">
                <el-form-item :label="t('settings.smsAccessKey')">
                  <el-input v-model="smsSettings.accessKey" :placeholder="t('settings.smsAccessKeyPlaceholder')" style="width: 400px" />
                </el-form-item>
                
                <el-form-item :label="t('settings.smsAccessSecret')">
                  <el-input v-model="smsSettings.accessSecret" type="password" :placeholder="t('settings.smsAccessSecretPlaceholder')" style="width: 400px" show-password />
                </el-form-item>
                
                <el-form-item :label="t('settings.smsSignName')">
                  <el-input v-model="smsSettings.signName" :placeholder="t('settings.smsSignNamePlaceholder')" style="width: 400px" />
                </el-form-item>
                
                <el-form-item :label="t('settings.smsTemplateCode')">
                  <el-input v-model="smsSettings.templateCode" :placeholder="t('settings.smsTemplateCodePlaceholder')" style="width: 400px" />
                </el-form-item>
                
                <template v-if="smsSettings.provider === 'custom'">
                  <el-form-item :label="t('settings.smsApiUrl')">
                    <el-input v-model="smsSettings.apiUrl" :placeholder="t('settings.smsApiUrlPlaceholder')" style="width: 400px" />
                  </el-form-item>
                </template>
              </template>
              
              <el-form-item>
                <el-button @click="handleTestSms">{{ t('settings.testSms') }}</el-button>
                <el-button type="primary" @click="handleSaveSmsSettings">{{ t('settings.saveSettings') }}</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </el-tab-pane>
      
      <!-- 系统监控 -->
      <el-tab-pane :label="t('settings.systemMonitor')" name="monitor">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>{{ t('settings.systemStatus') }}</span>
              <el-button type="primary" size="small" @click="loadSystemStatus" :loading="monitorLoading">
                <el-icon><Refresh /></el-icon>
                {{ t('settings.refresh') }}
              </el-button>
            </div>
          </template>
          
          <div class="monitor-grid">
            <el-alert
              v-if="monitorLoadFailed"
              type="error"
              :title="t('settings.systemStatus') + t('common.error')"
              :closable="false"
              show-icon
              style="grid-column: 1 / -1"
            >
              <template #default>
                <el-button link type="primary" @click="loadSystemStatus">{{ t('common.retry') }}</el-button>
              </template>
            </el-alert>
            <div class="monitor-card">
              <div class="monitor-title">{{ t('settings.cpuUsage') }}</div>
              <div class="monitor-value">{{ systemStatus.cpu }}%</div>
              <el-progress :percentage="systemStatus.cpu" :color="getProgressColor(systemStatus.cpu)" :stroke-width="8" :show-text="false" />
              <div class="progress-label">{{ systemStatus.cpu }}%</div>
            </div>
            
            <div class="monitor-card">
              <div class="monitor-title">{{ t('settings.memoryUsage') }}</div>
              <div class="monitor-value">{{ systemStatus.memory }}%</div>
              <el-progress :percentage="systemStatus.memory" :color="getProgressColor(systemStatus.memory)" :stroke-width="8" :show-text="false" />
              <div class="progress-label">{{ systemStatus.memory }}%</div>
            </div>
            
            <div class="monitor-card">
              <div class="monitor-title">{{ t('settings.diskUsage') }}</div>
              <div class="monitor-value">{{ systemStatus.disk }}%</div>
              <el-progress :percentage="systemStatus.disk" :color="getProgressColor(systemStatus.disk)" :stroke-width="8" :show-text="false" />
              <div class="progress-label">{{ systemStatus.disk }}%</div>
            </div>
            
            <div class="monitor-card">
              <div class="monitor-title">{{ t('settings.onlineUsers') }}</div>
              <div class="monitor-value">{{ systemStatus.onlineUsers }}</div>
            </div>
          </div>
          
          <!-- 操作日志 -->
          <div class="operation-logs-section">
            <h3 class="section-title">{{ t('settings.operationLogs') }}</h3>
            <el-alert
              v-if="logsLoadFailed"
              type="error"
              :title="t('settings.operationLogs') + t('common.error')"
              :closable="false"
              show-icon
              style="margin-bottom: 12px"
            >
              <template #default>
                <el-button link type="primary" @click="loadOperationLogs">{{ t('common.retry') }}</el-button>
              </template>
            </el-alert>
            
            <div class="logs-filter">
              <el-input v-model="logsFilter.keyword" :placeholder="t('settings.searchKeyword')" style="width: 200px" clearable />
              <el-select v-model="logsFilter.module" :placeholder="t('settings.selectModule')" style="width: 150px" clearable>
                <el-option :label="t('settings.filterAll')" value="" />
                <el-option v-for="item in logModuleOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
              <el-select v-model="logsFilter.level" :placeholder="t('settings.selectLevel')" style="width: 120px" clearable>
                <el-option :label="t('settings.filterAll')" value="" />
                <el-option :label="t('settings.filterInfo')" value="info" />
                <el-option :label="t('settings.filterWarning')" value="warning" />
                <el-option :label="t('settings.filterError')" value="error" />
              </el-select>
              <el-button type="primary" @click="loadOperationLogs">{{ t('settings.search') }}</el-button>
            </div>
            
            <el-table :data="operationLogs" stripe style="width: 100%" size="small" v-loading="logsLoading">
              <el-table-column :prop="'time'" :label="t('settings.time')" width="180" />
              <el-table-column :prop="'level'" :label="t('settings.level')" width="80">
                <template #default="{ row }">
                  <el-tag :type="getLevelTagType(row.level)" size="small">{{ getLevelLabel(row.level) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column :prop="'module'" :label="t('settings.module')" width="110" show-overflow-tooltip>
                <template #default="{ row }">
                  {{ getModuleLabel(row.module) }}
                </template>
              </el-table-column>
              <el-table-column :prop="'action'" :label="t('settings.operation')" width="110" show-overflow-tooltip>
                <template #default="{ row }">
                  <el-tag size="small" type="info">{{ getActionLabel(row.action) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column :prop="'user'" :label="t('settings.operator')" width="100" />
              <el-table-column :prop="'content'" :label="t('settings.content')" min-width="220" show-overflow-tooltip>
                <template #default="{ row }">
                  {{ getLocalizedLogContent(row.content) }}
                </template>
              </el-table-column>
              <el-table-column :prop="'ip'" :label="t('settings.ipAddress')" width="130" />
            </el-table>
            
            <el-pagination
              v-model:current-page="logsPage"
              v-model:page-size="logsPageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="logsTotal"
              layout="total, sizes, prev, pager, next"
              @size-change="loadOperationLogs"
              @current-change="loadOperationLogs"
              style="margin-top: 16px; justify-content: flex-end"
            />
          </div>
        </el-card>
      </el-tab-pane>
      
      <!-- 会话管理 -->
      <el-tab-pane :label="t('settings.sessionManagement')" name="sessions">
        <el-card shadow="hover">
          <template #header>
            <span>{{ t('settings.sessionManagement') }}</span>
          </template>
          
          <!-- 会话配置 -->
          <div class="session-config-section">
            <h3 class="section-title">{{ t('settings.sessionConfig') }}</h3>
            
            <el-form label-width="140px">
              <el-form-item :label="t('settings.tokenExpiry')">
                <div class="form-item-row">
                  <el-input-number v-model="sessionConfig.tokenExpiry" :min="1" :max="720" style="width: 200px" />
                  <span class="form-item-hint">{{ t('settings.hours') }}</span>
                </div>
              </el-form-item>
              
              <el-form-item :label="t('settings.refreshTokenExpiry')">
                <div class="form-item-row">
                  <el-input-number v-model="sessionConfig.refreshTokenExpiry" :min="1" :max="720" style="width: 200px" />
                  <span class="form-item-hint">{{ t('settings.hours') }}</span>
                </div>
              </el-form-item>
              
              <el-form-item :label="t('settings.singleSession')">
                <div class="form-item-row">
                  <el-switch v-model="sessionConfig.singleSession" />
                  <span class="form-item-hint">{{ t('settings.singleSessionHint') }}</span>
                </div>
              </el-form-item>
              
              <el-form-item :label="t('settings.sessionTimeout')">
                <div class="form-item-row">
                  <el-input-number v-model="sessionConfig.sessionTimeout" :min="5" :max="480" style="width: 200px" />
                  <span class="form-item-hint">{{ t('settings.minutes') }}</span>
                  <span class="form-item-hint">{{ t('settings.sessionTimeoutHint') }}</span>
                </div>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="handleSaveSessionConfig">{{ t('settings.saveSettings') }}</el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <!-- 在线会话 -->
          <div class="online-sessions-section">
            <h3 class="section-title">{{ t('settings.activeSessions') }}</h3>
            <el-alert
              v-if="sessionsLoadFailed"
              type="error"
              :title="t('settings.activeSessions') + t('common.error')"
              :closable="false"
              show-icon
              style="margin-bottom: 12px"
            >
              <template #default>
                <el-button link type="primary" @click="loadActiveSessions">{{ t('common.retry') }}</el-button>
              </template>
            </el-alert>
            
            <el-table :data="activeSessions" stripe style="width: 100%" size="default" v-loading="sessionsLoading">
              <el-table-column :prop="'user'" :label="t('settings.sessionUser')" width="120" />
              <el-table-column :prop="'ip'" :label="t('settings.sessionIp')" width="140" />
              <el-table-column :prop="'location'" :label="t('settings.sessionLocation')" width="100" />
              <el-table-column :label="t('settings.loginTime')" width="180">
                <template #default="{ row }">
                  {{ formatTime(row.loginTime) }}
                </template>
              </el-table-column>
              <el-table-column :label="t('settings.lastActive')" width="180">
                <template #default="{ row }">
                  {{ formatTime(row.lastActive || row.loginTime) }}
                </template>
              </el-table-column>
              <el-table-column :label="t('common.action')" width="120" align="center">
                <template #default="{ row }">
                  <el-button type="danger" size="small" link @click="handleTerminateSession(row.id)">{{ t('settings.terminate') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <div class="terminate-all-wrapper">
              <el-button type="danger" @click="handleTerminateAllSessions">{{ t('settings.terminateAll') }}</el-button>
            </div>
            
            <el-empty v-if="!sessionsLoading && activeSessions.length === 0" :description="t('settings.noActiveSessions')" />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAppStore } from '@/stores/app'
import { getSystemConfigs, saveSystemConfigs, getSystemStatus, getActiveSessions, terminateSession, terminateAllSessions, sendTestEmail, sendTestSms, getOperationLogs } from '@/api/system'
import { Refresh, Cpu, Odometer, Box, User, SwitchButton, Monitor } from '@element-plus/icons-vue'

const { locale, t } = useI18n()
const appStore = useAppStore()

const activeTab = ref('basic')

const theme = computed({
  get: () => appStore.theme,
  set: (val: string) => appStore.setTheme(val)
})
const systemName = ref(appStore.systemName)

const notificationSettings = ref({
  expirationReminder: true,
  approvalNotification: true,
  commentNotification: true,
  emailNotification: true,
  systemNotification: true
})

const emailSettings = ref({
  smtpHost: '',
  smtpPort: 465,
  smtpFrom: '',
  smtpUsername: '',
  smtpPassword: '',
  smtpUseSSL: true
})

const smsSettings = ref({
  provider: '',
  accessKey: '',
  accessSecret: '',
  signName: '',
  templateCode: '',
  apiUrl: ''
})

const monitorLoading = ref(false)
const monitorLoadFailed = ref(false)
const monitorErrorNotified = ref(false)
const systemStatus = ref({
  cpu: 0,
  memory: 0,
  disk: 0,
  onlineUsers: 0
})

const sessionsLoading = ref(false)
const sessionsLoadFailed = ref(false)
const sessionsErrorNotified = ref(false)
const activeSessions = ref<any[]>([])

const sessionConfig = ref({
  tokenExpiry: 24,
  refreshTokenExpiry: 168,
  singleSession: false,
  sessionTimeout: 30
})

const logsLoading = ref(false)
const logsLoadFailed = ref(false)
const logsErrorNotified = ref(false)
const operationLogs = ref<any[]>([])
const logsFilter = ref({
  keyword: '',
  module: '',
  level: ''
})
const logsPage = ref(1)
const logsPageSize = ref(10)
const logsTotal = ref(0)
const logModuleOptions = computed(() => [
  { value: '用户管理', label: t('settings.filterUser') },
  { value: '合同管理', label: t('settings.filterContract') },
  { value: '模板管理', label: t('settings.filterTemplate') },
  { value: '提醒管理', label: t('settings.filterReminder') },
  { value: '统计报表', label: t('settings.filterStatistics') },
  { value: '认证授权', label: t('settings.filterAuth') },
  { value: '收藏管理', label: t('settings.filterFavorite') },
  { value: '标签管理', label: t('settings.filterTag') },
  { value: '续约管理', label: t('settings.filterRenewal') },
  { value: '其他', label: t('settings.filterSystem') }
])

let monitorTimer: number | null = null

const handleThemeChange = (val: string) => { appStore.setTheme(val) }
const handleLocaleChange = (val: string) => { appStore.setLocale(val); locale.value = val }

const handleSaveBasic = () => {
  appStore.setSystemName(systemName.value)
  ElMessage.success(t('common.success'))
}

const notifyErrorOnce = (notifiedRef: { value: boolean }) => {
  if (notifiedRef.value) return
  notifiedRef.value = true
  ElMessage.error(t('common.error'))
}

const loadNotificationSettings = async () => {
  try {
    const response = await getSystemConfigs()
    if (response && response.data) {
      const configs = response.data
      if (configs.ntf_expiration_reminder !== undefined) notificationSettings.value.expirationReminder = configs.ntf_expiration_reminder === true || configs.ntf_expiration_reminder === 'true'
      if (configs.ntf_approval_notification !== undefined) notificationSettings.value.approvalNotification = configs.ntf_approval_notification === true || configs.ntf_approval_notification === 'true'
      if (configs.ntf_comment_notification !== undefined) notificationSettings.value.commentNotification = configs.ntf_comment_notification === true || configs.ntf_comment_notification === 'true'
      if (configs.ntf_email_notification !== undefined) notificationSettings.value.emailNotification = configs.ntf_email_notification === true || configs.ntf_email_notification === 'true'
      if (configs.ntf_system_notification !== undefined) notificationSettings.value.systemNotification = configs.ntf_system_notification === true || configs.ntf_system_notification === 'true'
      
      if (configs.email_smtp_host) emailSettings.value.smtpHost = configs.email_smtp_host
      if (configs.email_smtp_port) emailSettings.value.smtpPort = Number(configs.email_smtp_port)
      if (configs.email_smtp_from) emailSettings.value.smtpFrom = configs.email_smtp_from
      if (configs.email_smtp_username) emailSettings.value.smtpUsername = configs.email_smtp_username
      if (configs.email_smtp_password) emailSettings.value.smtpPassword = configs.email_smtp_password
      if (configs.email_smtp_use_ssl !== undefined) emailSettings.value.smtpUseSSL = configs.email_smtp_use_ssl === true || configs.email_smtp_use_ssl === 'true'
      
      if (configs.sms_provider) smsSettings.value.provider = configs.sms_provider
      if (configs.sms_access_key) smsSettings.value.accessKey = configs.sms_access_key
      if (configs.sms_access_secret) smsSettings.value.accessSecret = configs.sms_access_secret
      if (configs.sms_sign_name) smsSettings.value.signName = configs.sms_sign_name
      if (configs.sms_template_code) smsSettings.value.templateCode = configs.sms_template_code
      if (configs.sms_api_url) smsSettings.value.apiUrl = configs.sms_api_url
      
      if (configs.session_token_expiry) sessionConfig.value.tokenExpiry = Number(configs.session_token_expiry)
      if (configs.session_refresh_token_expiry) sessionConfig.value.refreshTokenExpiry = Number(configs.session_refresh_token_expiry)
      if (configs.session_single_session !== undefined) sessionConfig.value.singleSession = configs.session_single_session === true || configs.session_single_session === 'true'
      if (configs.session_timeout) sessionConfig.value.sessionTimeout = Number(configs.session_timeout)
    }
  } catch (error) {
    console.error('加载通知设置失败:', error)
    ElMessage.error(t('common.error'))
  }
}

const handleSaveNotificationSettings = async () => {
  try {
    await saveSystemConfigs({
      ntf_expiration_reminder: notificationSettings.value.expirationReminder,
      ntf_approval_notification: notificationSettings.value.approvalNotification,
      ntf_comment_notification: notificationSettings.value.commentNotification,
      ntf_email_notification: notificationSettings.value.emailNotification,
      ntf_system_notification: notificationSettings.value.systemNotification
    })
    ElMessage.success(t('common.success'))
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const handleSaveEmailSettings = async () => {
  try {
    await saveSystemConfigs({
      email_smtp_host: emailSettings.value.smtpHost,
      email_smtp_port: emailSettings.value.smtpPort,
      email_smtp_from: emailSettings.value.smtpFrom,
      email_smtp_username: emailSettings.value.smtpUsername,
      email_smtp_password: emailSettings.value.smtpPassword,
      email_smtp_use_ssl: emailSettings.value.smtpUseSSL
    })
    ElMessage.success(t('common.success'))
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const handleTestEmail = async () => {
  try {
    const { value: toEmail } = await ElMessageBox.prompt(t('settings.testEmailPlaceholder'), t('settings.testEmailTitle'), {
      confirmButtonText: t('settings.testEmailConfirm'),
      cancelButtonText: t('settings.testEmailCancel'),
      inputPattern: /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/,
      inputErrorMessage: t('settings.testEmailInvalid')
    })
    await sendTestEmail({ toEmail })
    ElMessage.success(t('settings.testEmailSent'))
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(t('settings.testEmailFailed'))
    }
  }
}

const handleSaveSmsSettings = async () => {
  try {
    await saveSystemConfigs({
      sms_provider: smsSettings.value.provider,
      sms_access_key: smsSettings.value.accessKey,
      sms_access_secret: smsSettings.value.accessSecret,
      sms_sign_name: smsSettings.value.signName,
      sms_template_code: smsSettings.value.templateCode,
      sms_api_url: smsSettings.value.apiUrl
    })
    ElMessage.success(t('common.success'))
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const handleTestSms = async () => {
  try {
    const { value: phone } = await ElMessageBox.prompt(t('settings.testSmsPlaceholder'), t('settings.testSmsTitle'), {
      confirmButtonText: t('settings.testSmsConfirm'),
      cancelButtonText: t('settings.testSmsCancel'),
      inputPattern: /^1[3-9]\d{9}$/,
      inputErrorMessage: t('settings.testSmsInvalid')
    })
    await sendTestSms({ phone })
    ElMessage.success(t('settings.testSmsSent'))
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(t('settings.testSmsFailed'))
    }
  }
}

const loadSystemStatus = async () => {
  try {
    monitorLoading.value = true
    monitorLoadFailed.value = false
    monitorErrorNotified.value = false
    const status = await getSystemStatus()
    if (status && (status as any).data) {
      systemStatus.value = (status as any).data
    }
  } catch (error) {
    monitorLoadFailed.value = true
    console.error('加载系统状态失败:', error)
    notifyErrorOnce(monitorErrorNotified)
  } finally {
    monitorLoading.value = false
  }
}

const getProgressColor = (percentage: number) => {
  if (percentage < 50) return '#67c23a'
  if (percentage < 80) return '#e6a23c'
  return '#f56c6c'
}

const loadActiveSessions = async () => {
  try {
    sessionsLoading.value = true
    sessionsLoadFailed.value = false
    sessionsErrorNotified.value = false
    const sessions = await getActiveSessions()
    if (sessions && (sessions as any).data) {
      activeSessions.value = (sessions as any).data
    }
  } catch (error) {
    sessionsLoadFailed.value = true
    console.error('加载活跃会话失败:', error)
    notifyErrorOnce(sessionsErrorNotified)
  } finally {
    sessionsLoading.value = false
  }
}

const handleTerminateSession = async (id: number) => {
  try {
    await ElMessageBox.confirm(t('settings.confirmTerminateSession'), t('common.warning'), { type: 'warning' })
    await terminateSession(id)
    ElMessage.success(t('common.success'))
    loadActiveSessions()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(t('common.error'))
    }
  }
}

const handleTerminateAllSessions = async () => {
  try {
    await ElMessageBox.confirm(t('settings.confirmTerminateAllSessions'), t('common.warning'), { type: 'warning' })
    await terminateAllSessions()
    ElMessage.success(t('common.success'))
    loadActiveSessions()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(t('common.error'))
    }
  }
}

const handleSaveSessionConfig = async () => {
  try {
    await saveSystemConfigs({
      session_token_expiry: sessionConfig.value.tokenExpiry,
      session_refresh_token_expiry: sessionConfig.value.refreshTokenExpiry,
      session_single_session: sessionConfig.value.singleSession,
      session_timeout: sessionConfig.value.sessionTimeout
    })
    ElMessage.success(t('common.success'))
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const formatDevice = (device: string) => {
  if (!device) return t('profile.deviceUnknown')
  if (device.includes('Chrome')) return t('profile.deviceChrome')
  if (device.includes('Firefox')) return t('profile.deviceFirefox')
  if (device.includes('Safari')) return t('profile.deviceSafari')
  if (device.includes('Edge')) return t('profile.deviceEdge')
  if (device.includes('Windows')) return t('profile.deviceWindows')
  if (device.includes('Mac')) return t('profile.deviceMacOS')
  if (device.includes('Linux')) return t('profile.deviceLinux')
  if (device.length > 50) {
    return device.substring(0, 50) + '...'
  }
  return device
}

const formatTime = (time: string) => {
  if (!time) return '-'
  const date = new Date(time)
  const localeCode = locale.value === 'en' ? 'en-US' : 'zh-CN'
  return date.toLocaleString(localeCode, {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const loadOperationLogs = async () => {
  try {
    logsLoading.value = true
    logsLoadFailed.value = false
    logsErrorNotified.value = false
    const response = await getOperationLogs({
      page: logsPage.value,
      pageSize: logsPageSize.value,
      keyword: logsFilter.value.keyword,
      module: logsFilter.value.module,
      level: logsFilter.value.level
    })
    if (response && response.data) {
      operationLogs.value = response.data.list
      logsTotal.value = response.data.total
    }
  } catch (error) {
    logsLoadFailed.value = true
    console.error('加载操作日志失败:', error)
    notifyErrorOnce(logsErrorNotified)
  } finally {
    logsLoading.value = false
  }
}

const getLevelTagType = (level: string) => {
  switch (level?.toLowerCase()) {
    case 'info': return 'info'
    case 'warning': return 'warning'
    case 'error': return 'danger'
    default: return 'info'
  }
}

const getLevelLabel = (level: string) => {
  switch (level?.toLowerCase()) {
    case 'info': return t('settings.levelInfo')
    case 'warning': return t('settings.levelWarning')
    case 'error': return t('settings.levelError')
    default: return level || '-'
  }
}

const getModuleLabel = (module: string) => {
  const map: Record<string, string> = {
    '用户管理': t('settings.filterUser'),
    '合同管理': t('settings.filterContract'),
    '模板管理': t('settings.filterTemplate'),
    '提醒管理': t('settings.filterReminder'),
    '统计报表': t('settings.filterStatistics'),
    '认证授权': t('settings.filterAuth'),
    '收藏管理': t('settings.filterFavorite'),
    '标签管理': t('settings.filterTag'),
    '续约管理': t('settings.filterRenewal'),
    '其他': t('settings.filterSystem')
  }
  return map[module] || module || '-'
}

const getActionLabel = (action: string) => {
  const normalized = String(action || '').toUpperCase()
  const map: Record<string, string> = {
    CREATE: t('settings.actionCreate'),
    UPDATE: t('settings.actionUpdate'),
    DELETE: t('settings.actionDelete'),
    READ: t('settings.actionRead'),
    LOGIN: t('settings.actionLogin'),
    UPLOAD: t('settings.actionUpload'),
    DOWNLOAD: t('settings.actionDownload'),
    APPROVE: t('settings.actionApprove'),
    REJECT: t('settings.actionReject'),
    SIGN: t('settings.actionSign'),
    ARCHIVE: t('settings.actionArchive'),
    SUBMIT: t('settings.actionSubmit'),
    FAVORITE: t('settings.actionFavorite'),
    RENEWAL: t('settings.actionRenewal'),
    COPY: t('settings.actionCopy'),
    BATCH: t('settings.actionBatch'),
    TERMINATE: t('settings.actionTerminate'),
    ANALYZE: t('settings.actionAnalyze'),
    OTHER: t('settings.filterSystem')
  }
  return map[normalized] || normalized || '-'
}

const getLocalizedLogContent = (content: string) => {
  if (!content) return '-'
  let text = String(content)

  const classMap: Array<[RegExp, string]> = [
    [/ContractController/g, t('settings.logClassContract')],
    [/TemplateController/g, t('settings.logClassTemplate')],
    [/UserController/g, t('settings.logClassUser')],
    [/AuthController/g, t('settings.logClassAuth')],
    [/SystemController/g, t('settings.logClassSystem')],
    [/RoleController/g, t('settings.logClassRole')],
    [/ReminderController/g, t('settings.logClassReminder')],
    [/FavoriteController/g, t('settings.logClassFavorite')],
    [/TagController/g, t('settings.logClassTag')],
    [/RenewalController/g, t('settings.logClassRenewal')]
  ]
  classMap.forEach(([pattern, replacement]) => {
    text = text.replace(pattern, replacement)
  })

  const methodVerbMap: Array<[RegExp, string]> = [
    [/\.create([A-Z]\w*)?\(/g, `.${t('settings.logVerbCreate')}(`],
    [/\.add([A-Z]\w*)?\(/g, `.${t('settings.logVerbCreate')}(`],
    [/\.update([A-Z]\w*)?\(/g, `.${t('settings.logVerbUpdate')}(`],
    [/\.edit([A-Z]\w*)?\(/g, `.${t('settings.logVerbUpdate')}(`],
    [/\.delete([A-Z]\w*)?\(/g, `.${t('settings.logVerbDelete')}(`],
    [/\.remove([A-Z]\w*)?\(/g, `.${t('settings.logVerbDelete')}(`],
    [/\.get([A-Z]\w*)?\(/g, `.${t('settings.logVerbRead')}(`],
    [/\.list([A-Z]\w*)?\(/g, `.${t('settings.logVerbRead')}(`],
    [/\.find([A-Z]\w*)?\(/g, `.${t('settings.logVerbRead')}(`],
    [/\.select([A-Z]\w*)?\(/g, `.${t('settings.logVerbRead')}(`],
    [/\.login([A-Z]\w*)?\(/g, `.${t('settings.logVerbLogin')}(`],
    [/\.logout([A-Z]\w*)?\(/g, `.${t('settings.logVerbLogout')}(`],
    [/\.upload([A-Z]\w*)?\(/g, `.${t('settings.logVerbUpload')}(`],
    [/\.download([A-Z]\w*)?\(/g, `.${t('settings.logVerbDownload')}(`],
    [/\.export([A-Z]\w*)?\(/g, `.${t('settings.logVerbDownload')}(`],
    [/\.approve([A-Z]\w*)?\(/g, `.${t('settings.logVerbApprove')}(`],
    [/\.reject([A-Z]\w*)?\(/g, `.${t('settings.logVerbReject')}(`],
    [/\.sign([A-Z]\w*)?\(/g, `.${t('settings.logVerbSign')}(`],
    [/\.archive([A-Z]\w*)?\(/g, `.${t('settings.logVerbArchive')}(`],
    [/\.submit([A-Z]\w*)?\(/g, `.${t('settings.logVerbSubmit')}(`],
    [/\.favorite([A-Z]\w*)?\(/g, `.${t('settings.logVerbFavorite')}(`],
    [/\.star([A-Z]\w*)?\(/g, `.${t('settings.logVerbFavorite')}(`],
    [/\.renewal([A-Z]\w*)?\(/g, `.${t('settings.logVerbRenewal')}(`],
    [/\.copy([A-Z]\w*)?\(/g, `.${t('settings.logVerbCopy')}(`],
    [/\.batch([A-Z]\w*)?\(/g, `.${t('settings.logVerbBatch')}(`],
    [/\.terminate([A-Z]\w*)?\(/g, `.${t('settings.logVerbTerminate')}(`],
    [/\.analyze([A-Z]\w*)?\(/g, `.${t('settings.logVerbAnalyze')}(`]
  ]
  methodVerbMap.forEach(([pattern, replacement]) => {
    text = text.replace(pattern, replacement)
  })

  text = text
    .replace(/ - 操作成功/g, ` - ${t('settings.logOperationSuccess')}`)
    .replace(/ - 操作失败/g, ` - ${t('settings.logOperationFailed')}`)

  return text
}

onMounted(() => {
  loadNotificationSettings()
  loadSystemStatus()
  loadActiveSessions()
  loadOperationLogs()
  
  monitorTimer = window.setInterval(() => {
    if (activeTab.value === 'monitor') {
      loadSystemStatus()
    }
  }, 10000)
})

onUnmounted(() => {
  if (monitorTimer) {
    clearInterval(monitorTimer)
  }
})
</script>

<style scoped lang="scss">
.settings {
  .page-title {
    font-size: 24px;
    font-weight: 700;
    margin-bottom: 24px;
    background: var(--primary-gradient);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    max-width: 100%;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .settings-tabs {
    :deep(.el-tabs__header) { margin-bottom: 24px; }
    :deep(.el-tabs__item) {
      font-size: 16px;
      max-width: 220px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 12px;
    min-width: 0;

    > span {
      min-width: 0;
      max-width: 100%;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
  
  .notification-section {
    margin-bottom: 32px;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
  
  .section-title {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 20px;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--border-color);
    min-width: 0;
    max-width: 100%;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .monitor-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 24px;
    margin-bottom: 32px;
    
    .monitor-card {
      background: var(--bg-hover);
      border-radius: 8px;
      padding: 24px;
      text-align: center;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }
      
      .monitor-title {
        font-size: 14px;
        color: var(--text-secondary);
        margin-bottom: 12px;
      }
      
      .monitor-value {
        font-size: 32px;
        font-weight: 700;
        color: var(--text-primary);
        margin-bottom: 12px;
      }
      
      .progress-label {
        margin-top: 8px;
        font-size: 14px;
        color: var(--text-secondary);
      }
    }
  }
  
  .operation-logs-section {
    .section-title {
      margin-top: 32px;
    }
    
    .logs-filter {
      display: flex;
      gap: 12px;
      margin-bottom: 20px;
      flex-wrap: wrap;
      align-items: center;

      :deep(.el-input),
      :deep(.el-select) {
        min-width: 0;
      }

      :deep(.el-input__wrapper),
      :deep(.el-select__wrapper),
      :deep(.el-button) {
        max-width: 100%;
      }
    }
  }
  
  .session-config-section {
    margin-bottom: 32px;
    padding-bottom: 24px;
    border-bottom: 1px solid var(--border-color);
  }
  
  .form-item-row {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
  }
  
  .form-item-hint {
    color: var(--text-secondary);
    white-space: nowrap;
    max-width: 260px;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .online-sessions-section {
    .terminate-all-wrapper {
      margin-top: 16px;
      text-align: right;
    }
  }
  
  .device-cell {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .el-icon {
      color: var(--primary);
      font-size: 16px;
    }
    
    .device-text {
      font-weight: 500;
    }
  }

  :deep(.el-table th .cell) {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}
</style>
