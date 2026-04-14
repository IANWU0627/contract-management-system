<template>
  <div class="profile">
    <h1 class="page-title">{{ $t('profile.personalCenter') }}</h1>
    
    <el-tabs v-model="activeTab" class="profile-tabs">
      <!-- 基本信息 -->
      <el-tab-pane :label="t('profile.basicInfo')" name="basic">
        <el-row :gutter="20">
          <el-col :span="16">
            <el-card shadow="hover">
              <template #header><span>{{ t('profile.basicInfo') }}</span></template>
              
              <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
                <el-form-item :label="$t('user.username')">
                  <el-input v-model="form.username" disabled />
                </el-form-item>
                <el-form-item :label="$t('user.nickname')" prop="nickname">
                  <el-input v-model="form.nickname" :placeholder="t('profile.placeholder.nickname')" />
                </el-form-item>
                <el-form-item :label="$t('user.email')" prop="email">
                  <el-input v-model="form.email" :placeholder="t('profile.placeholder.email')" />
                </el-form-item>
                <el-form-item :label="$t('user.phone')" prop="phone">
                  <el-input v-model="form.phone" :placeholder="t('profile.placeholder.phone')" />
                </el-form-item>
                <el-form-item :label="$t('user.role')">
                  <el-tag>{{ formatRole(form.role) }}</el-tag>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="loading" @click="handleSubmit">{{ t('common.save') }}</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>
          
          <el-col :span="8">
            <el-card shadow="hover">
              <template #header><span>{{ t('profile.avatar') }}</span></template>
              <div class="avatar-section">
                <el-avatar :size="120" :src="avatarUrl || form.username?.[0]?.toUpperCase()">
                  {{ !avatarUrl ? form.username?.[0]?.toUpperCase() : '' }}
                </el-avatar>
                <el-upload
                  :show-file-list="false"
                  :auto-upload="false"
                  :on-change="handleAvatarChange"
                  :before-upload="beforeAvatarUpload"
                  accept="image/*"
                  class="avatar-upload"
                >
                  <el-button size="small" style="margin-top: 16px" :loading="avatarLoading">
                    {{ t('profile.changeAvatar') }}
                  </el-button>
                  <template #tip>
                    <div class="avatar-upload-tip">
                      {{ t('profile.avatarTip') }}
                    </div>
                  </template>
                </el-upload>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- 修改密码 -->
      <el-tab-pane :label="t('profile.changePassword')" name="password">
        <el-card shadow="hover">
          <template #header><span>{{ t('profile.changePassword') }}</span></template>
          
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="120px">
            <el-form-item :label="t('profile.oldPassword')" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item :label="t('profile.newPassword')" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item :label="t('profile.confirmPassword')" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="pwdLoading" @click="handleChangePassword">{{ t('profile.changePassword') }}</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 登录历史 -->
      <el-tab-pane :label="t('profile.loginHistory')" name="history">
        <el-card shadow="hover">
          <template #header><span>{{ t('profile.loginHistory') }}</span></template>
          
          <el-table :data="loginHistory" stripe style="width: 100%">
            <el-table-column :prop="'loginTime'" :label="t('profile.loginTime')" width="180" />
            <el-table-column :prop="'ipAddress'" :label="t('profile.ipAddress')" width="150" />
            <el-table-column :prop="'device'" :label="t('profile.device')" />
            <el-table-column :prop="'location'" :label="t('profile.location')" />
            <el-table-column :prop="'status'" :label="t('profile.status')" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'success' ? 'success' : 'danger'">
                  {{ row.status === 'success' ? t('profile.loginSuccess') : t('profile.loginFailed') }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          
          <el-pagination
            v-model:current-page="historyPage"
            v-model:page-size="historyPageSize"
            :total="historyTotal"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            style="margin-top: 20px; justify-content: flex-end"
            @size-change="loadLoginHistory"
            @current-change="loadLoginHistory"
          />
        </el-card>
      </el-tab-pane>

      <!-- 安全设置 -->
      <el-tab-pane :label="t('profile.securitySettings')" name="security">
        <el-card shadow="hover">
          <template #header><span>{{ t('profile.securitySettings') }}</span></template>
          
          <el-form label-width="180px">
            <el-divider content-position="left">{{ t('profile.twoFactorAuth') }}</el-divider>
            <el-form-item :label="t('profile.enable2FA')">
              <el-switch v-model="securitySettings.twoFAEnabled" @change="handle2FAChange" />
            </el-form-item>
            
            <el-divider content-position="left">{{ t('profile.sessionManagement') }}</el-divider>
            <el-form-item :label="t('profile.currentSessions')">
              <el-button type="primary" @click="showSessionsDialog = true">{{ t('profile.viewSessions') }}</el-button>
            </el-form-item>
            <el-form-item :label="t('profile.logoutOtherSessions')">
              <el-button type="danger" @click="handleLogoutOtherSessions">{{ t('profile.logoutAll') }}</el-button>
            </el-form-item>
            
            <el-divider content-position="left">{{ t('profile.loginAlerts') }}</el-divider>
            <el-form-item :label="t('profile.emailAlerts')">
              <el-switch v-model="securitySettings.emailAlerts" />
            </el-form-item>
            <el-form-item :label="t('profile.smsAlerts')">
              <el-switch v-model="securitySettings.smsAlerts" />
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 通知设置 -->
      <el-tab-pane :label="t('profile.notificationSettings')" name="notification">
        <el-card shadow="hover">
          <template #header><span>{{ t('profile.notificationSettings') }}</span></template>
          
          <el-form label-width="200px">
            <el-divider content-position="left">{{ t('profile.contractNotifications') }}</el-divider>
            <el-form-item :label="t('profile.expirationReminder')">
              <el-switch v-model="notificationSettings.expirationReminder" />
            </el-form-item>
            <el-form-item :label="t('profile.approvalNotification')">
              <el-switch v-model="notificationSettings.approvalNotification" />
            </el-form-item>
            <el-form-item :label="t('profile.commentNotification')">
              <el-switch v-model="notificationSettings.commentNotification" />
            </el-form-item>
            
            <el-divider content-position="left">{{ t('profile.notificationMethods') }}</el-divider>
            <el-form-item :label="t('profile.emailNotification')">
              <el-switch v-model="notificationSettings.emailNotification" />
            </el-form-item>
            <el-form-item :label="t('profile.systemNotification')">
              <el-switch v-model="notificationSettings.systemNotification" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleSaveNotificationSettings">{{ t('common.save') }}</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 大数据对接设置 -->
      <el-tab-pane :label="t('profile.bigDataSettings')" name="bigdata">
        <el-card shadow="hover">
          <template #header><span>{{ t('profile.bigDataSettings') }}</span></template>
          
          <el-form label-width="120px">
            <el-divider content-position="left">{{ t('profile.aiConfig') }}</el-divider>
            
            <el-form-item :label="t('profile.aiProvider')">
              <el-select v-model="aiConfig.provider" style="width: 280px" @change="handleAiProviderChange">
                <el-option :label="t('profile.aiNone')" value="none" />
                <el-option-group :label="t('profile.aiGroupOpenAI')">
                  <el-option :label="t('profile.aiOpenAI')" value="openai" />
                  <el-option :label="t('profile.aiAzure')" value="azure" />
                </el-option-group>
                <el-option-group :label="t('profile.aiGroupChinese')">
                  <el-option :label="t('profile.aiZhipu')" value="zhipu" />
                  <el-option :label="t('profile.aiBaichuan')" value="baichuan" />
                  <el-option :label="t('profile.aiTongyi')" value="tongyi" />
                  <el-option :label="t('profile.aiYi')" value="yi" />
                  <el-option :label="t('profile.aiDeepseek')" value="deepseek" />
                </el-option-group>
                <el-option-group :label="t('profile.aiGroupOther')">
                  <el-option :label="t('profile.aiSiliconFlow')" value="siliconflow" />
                  <el-option :label="t('profile.aiAnthropic')" value="anthropic" />
                  <el-option :label="t('profile.aiGemini')" value="gemini" />
                  <el-option :label="t('profile.aiOllama')" value="ollama" />
                </el-option-group>
              </el-select>
            </el-form-item>
            
            <template v-if="aiConfig.provider !== 'none'">
              <el-form-item :label="t('profile.aiApiUrl')">
                <el-input 
                  v-model="aiConfig.apiUrl" 
                  :placeholder="t('profile.aiApiUrlPlaceholder')"
                  style="width: 400px" 
                />
              </el-form-item>
              
              <el-form-item :label="t('profile.aiApiKey')">
                <el-input 
                  v-model="aiConfig.apiKey" 
                  type="password"
                  :placeholder="t('profile.aiApiKeyPlaceholder')"
                  style="width: 400px" 
                  show-password
                />
              </el-form-item>
              
              <el-form-item :label="t('profile.aiModel')">
                <el-select 
                  v-model="aiConfig.model" 
                  style="width: 280px"
                  filterable
                  allow-create
                  :allow-create-fn="(input: string) => ({ value: input, label: input })"
                  :placeholder="t('profile.aiModelPlaceholder')"
                >
                  <el-option
                    v-for="model in availableModels"
                    :key="model.value"
                    :label="model.label"
                    :value="model.value"
                  />
                </el-select>
              </el-form-item>
              
              <el-form-item :label="t('profile.aiTestConnection')">
                <el-button @click="handleTestAiConnection" :loading="aiTesting">
                  {{ t('profile.aiTest') }}
                </el-button>
                <span v-if="aiTestResult" :class="aiTestResult === 'success' ? 'test-success' : 'test-error'">
                  {{ aiTestResult === 'success' ? t('profile.aiTestSuccess') : t('profile.aiTestError') }}
                </span>
              </el-form-item>
            </template>
            
            <el-form-item>
              <el-button type="primary" @click="handleSaveAiConfig">{{ t('common.save') }}</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 会话管理对话框 -->
    <el-dialog v-model="showSessionsDialog" :title="t('profile.currentSessions')" width="600px">
      <el-table :data="activeSessions" stripe style="width: 100%">
        <el-table-column :prop="'device'" :label="t('profile.device')" />
        <el-table-column :prop="'ipAddress'" :label="t('profile.ipAddress')" width="130" />
        <el-table-column :prop="'loginTime'" :label="t('profile.loginTime')" width="180" />
        <el-table-column :label="t('common.operation')" width="100">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="handleTerminateSession(row.id)">{{ t('profile.terminate') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import type { FormInstance } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { getUserInfo, updateUserInfo, changePassword } from '@/api/auth'
import { testAiConnection } from '@/api/ai'

const { t } = useI18n()

const userStore = useUserStore()
const appStore = useAppStore()

const activeTab = ref('basic')

const formRef = ref<FormInstance>()
const pwdFormRef = ref<FormInstance>()
const loading = ref(false)
const pwdLoading = ref(false)
const avatarLoading = ref(false)
const avatarUrl = ref('')

const form = reactive({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  role: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  nickname: [{ required: true, message: t('profile.error.nickname'), trigger: 'blur' }],
  email: [{ type: 'email', message: t('profile.error.email'), trigger: 'blur' }]
}

const pwdRules = {
  oldPassword: [{ required: true, message: t('profile.error.oldPassword'), trigger: 'blur' }],
  newPassword: [{ required: true, message: t('profile.error.newPassword'), trigger: 'blur' }, { min: 6, message: t('profile.error.passwordMin'), trigger: 'blur' }],
  confirmPassword: [{ required: true, message: t('profile.error.confirmPassword'), trigger: 'blur' }]
}

const formatRole = (role: string) => {
  const map: Record<string, string> = {
    ADMIN: t('user.roles.admin'),
    LEGAL: t('user.roles.legal'),
    USER: t('user.roles.user')
  }
  return map[role] || role
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const updatedInfo = {
        nickname: form.nickname,
        email: form.email,
        phone: form.phone
      }
      await updateUserInfo(updatedInfo)
      if (userStore.userInfo) {
        Object.assign(userStore.userInfo, updatedInfo)
      }
      ElMessage.success(t('common.success'))
    } catch (error: any) {
      ElMessage.error(error.message || t('common.error'))
    } finally {
      loading.value = false
    }
  })
}

const handleChangePassword = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    if (pwdForm.newPassword !== pwdForm.confirmPassword) {
      ElMessage.error(t('profile.error.passwordMismatch'))
      return
    }
    pwdLoading.value = true
    try {
      await changePassword({
        oldPassword: pwdForm.oldPassword,
        newPassword: pwdForm.newPassword
      })
      ElMessage.success(t('common.success'))
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
      pwdForm.confirmPassword = ''
    } catch (error: any) {
      ElMessage.error(error.message || t('common.error'))
    } finally {
      pwdLoading.value = false
    }
  })
}

const beforeAvatarUpload = (file: any) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif'
  const isLt10M = file.size / 1024 / 1024 < 10
  
  if (!isJPG) {
    ElMessage.error(t('profile.error.avatarFormat'))
  }
  if (!isLt10M) {
    ElMessage.error(t('profile.error.avatarSize'))
  }
  
  return isJPG && isLt10M
}

const handleAvatarChange = async (file: any) => {
  try {
    if (!beforeAvatarUpload(file.raw)) {
      return
    }
    
    avatarLoading.value = true
    
    const url = URL.createObjectURL(file.raw)
    avatarUrl.value = url
    
    const reader = new FileReader()
    reader.onload = async (e) => {
      const base64 = e.target?.result as string
      try {
        await updateUserInfo({ avatar: base64 })
        if (userStore.userInfo) {
          userStore.userInfo.avatar = base64
        }
        ElMessage.success(t('common.success'))
      } catch (error: any) {
        ElMessage.error(error.message || t('common.error'))
      } finally {
        avatarLoading.value = false
      }
    }
    reader.readAsDataURL(file.raw)
  } catch (error: any) {
    ElMessage.error(error.message || t('common.error'))
    avatarLoading.value = false
  }
}

const loadUserData = async () => {
  try {
    if (userStore.userInfo) {
      Object.assign(form, userStore.userInfo)
      avatarUrl.value = userStore.userInfo.avatar || ''
    } else {
      ElMessage.error('未找到用户信息')
    }
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const loginHistory = ref<any[]>([
  { loginTime: '2026-04-14 10:22:00', ipAddress: '192.168.1.100', device: 'Chrome / MacOS', location: '中国 北京', status: 'success' },
  { loginTime: '2026-04-13 15:30:00', ipAddress: '192.168.1.101', device: 'Safari / MacOS', location: '中国 北京', status: 'success' },
  { loginTime: '2026-04-12 09:15:00', ipAddress: '192.168.1.102', device: 'Firefox / Windows', location: '中国 上海', status: 'success' }
])
const historyPage = ref(1)
const historyPageSize = ref(10)
const historyTotal = ref(3)

const loadLoginHistory = () => {}

const securitySettings = reactive({
  twoFAEnabled: false,
  emailAlerts: true,
  smsAlerts: false
})

const handle2FAChange = async (val: boolean) => {
  if (val) {
    ElMessage.info(t('profile.2FAEnableInfo'))
  } else {
    ElMessage.success(t('common.success'))
  }
}

const showSessionsDialog = ref(false)
const activeSessions = ref<any[]>([
  { id: 1, device: 'Chrome / MacOS', ipAddress: '192.168.1.100', loginTime: '2026-04-14 10:22:00' },
  { id: 2, device: 'Safari / iPhone', ipAddress: '192.168.1.103', loginTime: '2026-04-13 18:45:00' }
])

const handleLogoutOtherSessions = async () => {
  try {
    await ElMessageBox.confirm(t('profile.logoutConfirm'), t('common.confirm'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    ElMessage.success(t('common.success'))
  } catch {
  }
}

const handleTerminateSession = (id: number) => {
  activeSessions.value = activeSessions.value.filter(s => s.id !== id)
  ElMessage.success(t('common.success'))
}

const notificationSettings = reactive({
  expirationReminder: true,
  approvalNotification: true,
  commentNotification: true,
  emailNotification: true,
  systemNotification: true
})

const handleSaveNotificationSettings = () => {
  ElMessage.success(t('common.success'))
}

interface AiConfig {
  provider: string
  apiUrl: string
  apiKey: string
  model: string
}

const aiConfig = reactive<AiConfig>({
  provider: localStorage.getItem('ai_provider') || 'none',
  apiUrl: localStorage.getItem('ai_api_url') || '',
  apiKey: localStorage.getItem('ai_api_key') || '',
  model: localStorage.getItem('ai_model') || ''
})

interface AiProvider {
  apiUrl: string
  models: { value: string; label: string }[]
}

const providerPresets: Record<string, AiProvider> = {
  openai: {
    apiUrl: 'https://api.openai.com/v1/chat/completions',
    models: [
      { value: 'gpt-4o', label: 'GPT-4o (最新)' },
      { value: 'gpt-4o-mini', label: 'GPT-4o Mini' },
      { value: 'gpt-4-turbo', label: 'GPT-4 Turbo' },
      { value: 'gpt-4', label: 'GPT-4' },
      { value: 'gpt-3.5-turbo', label: 'GPT-3.5 Turbo' }
    ]
  },
  azure: {
    apiUrl: 'https://your-resource.openai.azure.com',
    models: [
      { value: 'gpt-4o', label: 'GPT-4o' },
      { value: 'gpt-4', label: 'GPT-4' },
      { value: 'gpt-35-turbo', label: 'GPT-3.5 Turbo' }
    ]
  },
  anthropic: {
    apiUrl: 'https://api.anthropic.com/v1/messages',
    models: [
      { value: 'claude-sonnet-4-20250514', label: 'Claude Sonnet 4' },
      { value: 'claude-3-5-sonnet-latest', label: 'Claude 3.5 Sonnet' },
      { value: 'claude-3-opus-latest', label: 'Claude 3 Opus' },
      { value: 'claude-3-haiku-latest', label: 'Claude 3 Haiku' }
    ]
  },
  gemini: {
    apiUrl: 'https://generativelanguage.googleapis.com/v1beta/models',
    models: [
      { value: 'gemini-2.0-flash-exp', label: 'Gemini 2.0 Flash' },
      { value: 'gemini-1.5-pro', label: 'Gemini 1.5 Pro' },
      { value: 'gemini-1.5-flash', label: 'Gemini 1.5 Flash' },
      { value: 'gemini-pro', label: 'Gemini Pro' }
    ]
  },
  zhipu: {
    apiUrl: 'https://open.bigmodel.cn/api/paas/v4/chat/completions',
    models: [
      { value: 'glm-4-plus', label: 'GLM-4 Plus (最新)' },
      { value: 'glm-4', label: 'GLM-4' },
      { value: 'glm-4-flash', label: 'GLM-4 Flash' },
      { value: 'glm-3-turbo', label: 'GLM-3 Turbo' }
    ]
  },
  baichuan: {
    apiUrl: 'https://api.baichuan-ai.com/v1/chat/completions',
    models: [
      { value: 'Baichuan4', label: '百川4' },
      { value: 'Baichuan3-Turbo', label: '百川3-Turbo' },
      { value: 'Baichuan2-Turbo', label: '百川2-Turbo' }
    ]
  },
  tongyi: {
    apiUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions',
    models: [
      { value: 'qwen-plus', label: '通义千问 Plus' },
      { value: 'qwen-turbo', label: '通义千问 Turbo' },
      { value: 'qwen-max', label: '通义千问 Max' },
      { value: 'qwen-long', label: '通义千问 Long' }
    ]
  },
  yi: {
    apiUrl: 'https://api.lingyiwanwu.com/v1/chat/completions',
    models: [
      { value: 'yi-large', label: 'Yi Large (最新)' },
      { value: 'yi-medium', label: 'Yi Medium' },
      { value: 'yi-small', label: 'Yi Small' }
    ]
  },
  deepseek: {
    apiUrl: 'https://api.deepseek.com/v1/chat/completions',
    models: [
      { value: 'deepseek-chat', label: 'DeepSeek Chat' },
      { value: 'deepseek-coder', label: 'DeepSeek Coder' }
    ]
  },
  siliconflow: {
    apiUrl: 'https://api.siliconflow.cn/v1/chat/completions',
    models: [
      { value: 'deepseek-ai/DeepSeek-V2.5', label: 'DeepSeek V2.5' },
      { value: 'Qwen/Qwen2.5-72B-Instruct', label: 'Qwen2.5-72B' },
      { value: 'Qwen/Qwen2.5-7B-Instruct', label: 'Qwen2.5-7B' },
      { value: 'THUDM/glm-4-9b-chat', label: 'GLM-4-9B' },
      { value: '01-ai/Yi-1.5-34B-Chat', label: 'Yi-1.5-34B' }
    ]
  },
  ollama: {
    apiUrl: 'http://localhost:11434/v1/chat/completions',
    models: [
      { value: 'gpt-oss:20b', label: 'GPT-OSS 20B (您当前使用)' },
      { value: 'llama3.2', label: 'Llama 3.2 (最新)' },
      { value: 'llama3.1', label: 'Llama 3.1' },
      { value: 'llama3', label: 'Llama 3' },
      { value: 'llama3.2-vision', label: 'Llama 3.2 Vision (多模态)' },
      { value: 'mistral', label: 'Mistral' },
      { value: 'mistral-nemo', label: 'Mistral Nemo' },
      { value: 'mixtral', label: 'Mixtral 8x7B' },
      { value: 'qwen2.5', label: 'Qwen 2.5' },
      { value: 'qwen2.5-coder', label: 'Qwen 2.5 Coder' },
      { value: 'deepseek-r1', label: 'DeepSeek R1' },
      { value: 'deepseek-r1-distill-qwen', label: 'DeepSeek R1 (Qwen蒸馏)' },
      { value: 'deepseek-r1-distill-llama', label: 'DeepSeek R1 (Llama蒸馏)' },
      { value: 'codellama', label: 'Code Llama' },
      { value: 'codellama:70b', label: 'Code Llama 70B' },
      { value: 'gemma2', label: 'Gemma 2' },
      { value: 'gemma2:27b', label: 'Gemma 2 27B' },
      { value: 'phi3', label: 'Phi-3' },
      { value: 'phi3.5', label: 'Phi-3.5' },
      { value: 'nomic-embed-text', label: 'Nomic Embed Text (嵌入)' },
      { value: 'mxbai-embed-large', label: 'MxBai Embed Large (嵌入)' },
      { value: 'granite3.3', label: 'Granite 3.3' },
      { value: 'command-r7b', label: 'Command R7B' },
      { value: 'stablelm2', label: 'Stable LM 2' },
      { value: 'aya', label: 'Aya (多语言)' },
      { value: 'wizardlm2', label: 'WizardLM 2' },
      { value: 'neural-chat', label: 'Neural Chat' }
    ]
  }
}

const availableModels = computed(() => {
  if (!aiConfig.provider || aiConfig.provider === 'none') return []
  const preset = providerPresets[aiConfig.provider]
  return preset?.models || []
})

const aiTesting = ref(false)
const aiTestResult = ref<'success' | 'error' | ''>('')

const handleAiProviderChange = (provider: string) => {
  if (provider === 'none') {
    aiConfig.apiUrl = ''
    aiConfig.model = ''
    return
  }
  
  const preset = providerPresets[provider]
  if (preset) {
    aiConfig.apiUrl = preset.apiUrl
    aiConfig.model = preset.models[0]?.value || ''
  }
}

const handleTestAiConnection = async () => {
  if (!aiConfig.apiKey) {
    ElMessage.warning(t('profile.aiApiKeyRequired'))
    return
  }
  
  aiTesting.value = true
  aiTestResult.value = ''
  
  try {
    await testAiConnection({
      provider: aiConfig.provider,
      apiUrl: aiConfig.apiUrl,
      apiKey: aiConfig.apiKey,
      model: aiConfig.model
    })
    aiTestResult.value = 'success'
    ElMessage.success(t('profile.aiTestSuccess'))
  } catch (error) {
    aiTestResult.value = 'error'
    ElMessage.error(t('profile.aiTestError'))
  } finally {
    aiTesting.value = false
  }
}

const handleSaveAiConfig = () => {
  localStorage.setItem('ai_provider', aiConfig.provider)
  localStorage.setItem('ai_api_url', aiConfig.apiUrl)
  localStorage.setItem('ai_api_key', aiConfig.apiKey)
  localStorage.setItem('ai_model', aiConfig.model)
  
  ElMessage.success(t('common.success'))
}

onMounted(() => {
  loadUserData()
})
</script>

<style scoped lang="scss">
.profile {
  .page-title { font-size: 24px; font-weight: 700; margin-bottom: 24px; background: var(--primary-gradient); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
  
  .profile-tabs {
    :deep(.el-tabs__header) { margin-bottom: 24px; }
    :deep(.el-tabs__item) { font-size: 16px; }
  }
  
  .password-card { margin-top: 20px; }
  
  .avatar-section { text-align: center;
    .avatar-upload-tip {
      font-size: 12px;
      color: var(--text-secondary);
      margin-top: 8px;
      line-height: 1.4;
    }
  }
  
  .test-success {
    color: #67c23a;
    margin-left: 12px;
  }
  
  .test-error {
    color: #f56c6c;
    margin-left: 12px;
  }
}
</style>
