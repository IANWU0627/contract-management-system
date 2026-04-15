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
          <el-alert
            v-if="historyLoadFailed"
            type="error"
            :title="t('profile.loginHistory') + t('common.error')"
            :closable="false"
            show-icon
            style="margin-bottom: 12px"
          >
            <template #default>
              <el-button link type="primary" @click="loadLoginHistory">{{ t('common.retry') }}</el-button>
            </template>
          </el-alert>
          
          <el-table :data="loginHistory" stripe style="width: 100%" size="default" v-loading="historyLoading">
            <el-table-column :prop="'loginTime'" :label="t('profile.loginTime')" width="180">
              <template #default="{ row }">
                {{ formatTime(row.loginTime) }}
              </template>
            </el-table-column>
            <el-table-column :prop="'ipAddress'" :label="t('profile.ipAddress')" width="140">
              <template #default="{ row }">
                <el-tag size="small" type="info">{{ row.ipAddress || '-' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column :prop="'device'" :label="t('profile.device')" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">
                <div class="device-cell">
                  <el-icon><Monitor /></el-icon>
                  <span class="device-text">{{ formatDevice(row.device) }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column :prop="'location'" :label="t('profile.location')" width="100">
              <template #default="{ row }">
                <el-tag size="small" type="success">{{ row.location || t('profile.deviceUnknown') }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column :prop="'status'" :label="t('profile.status')" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
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
            <el-divider content-position="left">{{ t('profile.sessionManagement') }}</el-divider>
            <el-form-item :label="t('profile.currentSessions')">
              <el-button type="primary" @click="openSessionsDialog">{{ t('profile.viewSessions') }}</el-button>
            </el-form-item>
            <el-form-item :label="t('profile.logoutOtherSessions')">
              <el-button type="danger" @click="handleLogoutOtherSessions">{{ t('profile.logoutAll') }}</el-button>
            </el-form-item>

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
          
          <el-form label-width="160px">
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
                  :loading="ollamaLoading"
                >
                  <el-option
                    v-for="model in availableModels"
                    :key="model.value"
                    :label="model.label"
                    :value="model.value"
                  />
                </el-select>
                <el-button 
                  v-if="aiConfig.provider === 'ollama'" 
                  size="small" 
                  @click="loadOllamaModels"
                  :loading="ollamaLoading"
                  style="margin-left: 8px"
                >
                  {{ t('profile.refreshModels') }}
                </el-button>
              </el-form-item>
              
              <el-form-item :label="t('profile.aiTestConnection')">
                <el-button @click="handleTestAiConnection" :loading="aiTesting">
                  {{ t('profile.aiTest') }}
                </el-button>
                <span v-if="aiTestResult" :class="aiTestResult === 'success' ? 'test-success' : 'test-error'">
                  {{ aiTestResult === 'success' ? t('profile.aiTestSuccess') : t('profile.aiTestError') }}
                </span>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="handleSaveAiConfig">{{ t('common.save') }}</el-button>
              </el-form-item>
            </template>

            <el-divider content-position="left">{{ t('profile.dataAnalysis') }}</el-divider>

            <div class="smart-analysis-panel">
              <p class="smart-analysis-intro">{{ t('profile.smartAnalysisIntro') }}</p>

              <div class="analysis-section">
                <div class="analysis-section-title">{{ t('profile.riskThresholdSection') }}</div>
                <el-form-item :label="t('profile.riskThreshold')" class="smart-analysis-form-item" label-width="112px">
                  <div class="form-item-stack">
                    <div class="risk-presets">
                      <el-button-group>
                        <el-button
                          size="small"
                          :type="isRiskPresetActive(40) ? 'primary' : 'default'"
                          @click="setRiskThresholdPreset(40)"
                        >
                          {{ t('profile.riskPresetStrict') }}
                        </el-button>
                        <el-button
                          size="small"
                          :type="isRiskPresetActive(55) ? 'primary' : 'default'"
                          @click="setRiskThresholdPreset(55)"
                        >
                          {{ t('profile.riskPresetBalanced') }}
                        </el-button>
                        <el-button
                          size="small"
                          :type="isRiskPresetActive(70) ? 'primary' : 'default'"
                          @click="setRiskThresholdPreset(70)"
                        >
                          {{ t('profile.riskPresetLenient') }}
                        </el-button>
                      </el-button-group>
                    </div>
                    <el-slider
                      v-model="bigDataConfig.riskThreshold"
                      :min="0"
                      :max="100"
                      :step="5"
                      show-input
                      class="risk-slider"
                    />
                    <span class="form-item-hint">{{ t('profile.riskThresholdHint') }}</span>
                  </div>
                </el-form-item>
              </div>

              <div class="analysis-section">
                <div class="analysis-section-title">{{ t('profile.modelParamsSection') }}</div>
                <el-form-item :label="t('profile.maxTokens')" class="smart-analysis-form-item" label-width="112px">
                  <div class="form-item-stack">
                    <el-slider
                      v-model="bigDataConfig.maxTokens"
                      :min="100"
                      :max="8192"
                      :step="100"
                      show-input
                      class="token-slider"
                    />
                    <span class="form-item-hint">{{ t('profile.maxTokensHint') }}</span>
                  </div>
                </el-form-item>
                <el-form-item :label="t('profile.temperature')" class="smart-analysis-form-item" label-width="112px">
                  <div class="form-item-stack">
                    <el-slider
                      v-model="bigDataConfig.temperature"
                      :min="0"
                      :max="2"
                      :step="0.1"
                      show-input
                      class="temp-slider"
                    />
                    <span class="form-item-hint">{{ t('profile.temperatureHint') }}</span>
                  </div>
                </el-form-item>
              </div>

              <div class="analysis-section advanced-section">
                <div class="analysis-section-title">{{ t('profile.advancedSettings') }}</div>
                <el-row :gutter="20">
                  <el-col :xs="24" :sm="12">
                    <el-form-item :label="t('profile.enableCache')" class="smart-analysis-form-item" label-width="112px">
                      <div class="cache-row">
                        <el-switch v-model="bigDataConfig.enableCache" />
                        <span class="form-item-hint">{{ t('profile.enableCacheHint') }}</span>
                      </div>
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12">
                    <el-form-item
                      v-if="bigDataConfig.enableCache"
                      :label="t('profile.cacheTimeout')"
                      class="smart-analysis-form-item"
                      label-width="112px"
                    >
                      <div class="form-item-stack">
                        <el-input-number
                          v-model="bigDataConfig.cacheTimeout"
                          :min="5"
                          :max="1440"
                          :step="5"
                          controls-position="right"
                          class="cache-timeout-input"
                        />
                        <span class="form-item-hint">{{ t('profile.cacheTimeoutHint') }}</span>
                      </div>
                    </el-form-item>
                  </el-col>
                </el-row>
              </div>

              <div class="smart-analysis-actions">
                <el-button type="primary" @click="handleSaveBigDataConfig">{{ t('common.save') }}</el-button>
                <el-button @click="handleResetBigDataConfig">{{ t('common.reset') }}</el-button>
              </div>
            </div>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 会话管理对话框 -->
    <el-dialog v-model="showSessionsDialog" :title="t('profile.currentSessions')" width="800px">
      <el-alert
        v-if="sessionsLoadFailed"
        type="error"
        :title="t('profile.currentSessions') + t('common.error')"
        :closable="false"
        show-icon
        style="margin-bottom: 12px"
      >
        <template #default>
          <el-button link type="primary" @click="loadActiveSessions">{{ t('common.retry') }}</el-button>
        </template>
      </el-alert>
      <el-table :data="activeSessions" stripe style="width: 100%" size="default">
        <el-table-column :prop="'device'" :label="t('profile.device')" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="device-cell">
              <el-icon><Monitor /></el-icon>
              <span class="device-text">{{ formatDevice(row.device) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column :prop="'ipAddress'" :label="t('profile.ipAddress')" width="140">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.ipAddress || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :prop="'location'" :label="t('profile.location')" width="120">
          <template #default="{ row }">
            <el-tag size="small" type="success">{{ row.location || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :prop="'browserVersion'" :label="t('profile.browserVersion')" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.browserVersion || t('profile.deviceUnknown') }}
          </template>
        </el-table-column>
        <el-table-column :prop="'loginTime'" :label="t('profile.loginTime')" width="180">
          <template #default="{ row }">
            {{ formatTime(row.loginTime) }}
          </template>
        </el-table-column>
        <el-table-column :prop="'lastActive'" :label="t('profile.lastActiveTime')" width="180">
          <template #default="{ row }">
            {{ formatTime(row.lastActive) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('common.action')" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.current" size="small" type="success">{{ t('profile.currentSessionTag') }}</el-tag>
            <el-button v-else type="danger" size="small" link @click="handleTerminateSession(row.id)">{{ t('profile.terminate') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import type { FormInstance } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { getUserInfo, updateUserInfo, changePassword } from '@/api/auth'
import { testAiConnection, getOllamaModels } from '@/api/ai'
import { getSystemConfigs, saveSystemConfigs, getLoginHistory, getActiveSessions, terminateSession, terminateAllSessions } from '@/api/system'
import { Monitor } from '@element-plus/icons-vue'

const { t, locale } = useI18n()

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
  newPassword: [
    { required: true, message: t('profile.error.newPassword'), trigger: 'blur' },
    { min: 8, message: t('profile.error.passwordMin'), trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: (error?: Error) => void) => {
        if (!value) {
          callback()
          return
        }
        const strongPwd = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z\d]).{8,}$/
        if (!strongPwd.test(value)) {
          callback(new Error(t('profile.error.passwordComplexity')))
          return
        }
        if (value === pwdForm.oldPassword) {
          callback(new Error(t('profile.error.passwordSameAsOld')))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
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

const formatDevice = (device: string) => {
  if (!device) return t('profile.deviceUnknown')
  // Check Edge before Chrome because modern Edge UA also contains "Chrome".
  if (device.includes('Edg') || device.includes('Edge')) return t('profile.deviceEdge')
  if (device.includes('Firefox')) return t('profile.deviceFirefox')
  if (device.includes('Chrome')) return t('profile.deviceChrome')
  if (device.includes('Safari')) return t('profile.deviceSafari')
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
  return date.toLocaleString(locale.value === 'en' ? 'en-US' : 'zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
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
      ElMessage.error(t('profile.noUserInfo'))
    }
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const loginHistory = ref<any[]>([])
const historyLoading = ref(false)
const historyLoadFailed = ref(false)
const historyPage = ref(1)
const historyPageSize = ref(10)
const historyTotal = ref(0)

const loadLoginHistory = async () => {
  try {
    historyLoading.value = true
    historyLoadFailed.value = false
    const response = await getLoginHistory({
      page: historyPage.value,
      pageSize: historyPageSize.value
    })
    if (response && response.data) {
      loginHistory.value = response.data.list
      historyTotal.value = response.data.total
    }
  } catch (error) {
    historyLoadFailed.value = true
    console.error('Failed to load login history:', error)
    ElMessage.error(t('common.error'))
  } finally {
    historyLoading.value = false
  }
}

const showSessionsDialog = ref(false)
const activeSessions = ref<any[]>([])
const sessionsLoadFailed = ref(false)

const loadActiveSessions = async () => {
  try {
    sessionsLoadFailed.value = false
    const response = await getActiveSessions()
    const sessions = (response as any)?.data
    if (Array.isArray(sessions)) {
      activeSessions.value = sessions.map((s: any) => ({
        id: s.id,
        device: s.device,
        ipAddress: s.ip,
        location: s.location,
        browserVersion: s.browserVersion,
        loginTime: s.loginTime,
        lastActive: s.lastActive,
        current: !!s.current
      }))
    }
  } catch (error) {
    sessionsLoadFailed.value = true
    console.error('Failed to load active sessions:', error)
    ElMessage.error(t('common.error'))
  }
}

const handleLogoutOtherSessions = async () => {
  try {
    await ElMessageBox.confirm(t('profile.logoutConfirm'), t('common.confirm'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await terminateAllSessions()
    await loadActiveSessions()
    await loadLoginHistory()
    ElMessage.success(t('common.success'))
  } catch {
  }
}

const handleTerminateSession = async (id: number) => {
  try {
    await terminateSession(id)
    await loadActiveSessions()
    ElMessage.success(t('common.success'))
  } catch {
    ElMessage.error(t('common.error'))
  }
}

const openSessionsDialog = async () => {
  await loadActiveSessions()
  showSessionsDialog.value = true
}

const notificationSettings = reactive({
  expirationReminder: true,
  approvalNotification: true,
  commentNotification: true,
  systemNotification: true
})

const handleSaveNotificationSettings = async () => {
  try {
    await saveSystemConfigs({
      ntf_expiration_reminder: notificationSettings.expirationReminder,
      ntf_approval_notification: notificationSettings.approvalNotification,
      ntf_comment_notification: notificationSettings.commentNotification,
      ntf_system_notification: notificationSettings.systemNotification
    })
    ElMessage.success(t('common.success'))
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

interface AiConfig {
  provider: string
  apiUrl: string
  apiKey: string
  model: string
}

const aiConfig = reactive<AiConfig>({
  provider: 'none',
  apiUrl: '',
  apiKey: '',
  model: ''
})

interface AiProvider {
  apiUrl: string
  models: { value: string; label: string }[]
}

const providerPresets: Record<string, AiProvider> = {
  openai: {
    apiUrl: 'https://api.openai.com/v1/chat/completions',
    models: [
      { value: 'gpt-4o', label: 'GPT-4o (Latest)' },
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
      { value: 'glm-4-plus', label: 'GLM-4 Plus (Latest)' },
      { value: 'glm-4', label: 'GLM-4' },
      { value: 'glm-4-flash', label: 'GLM-4 Flash' },
      { value: 'glm-3-turbo', label: 'GLM-3 Turbo' }
    ]
  },
  baichuan: {
    apiUrl: 'https://api.baichuan-ai.com/v1/chat/completions',
    models: [
      { value: 'Baichuan4', label: 'Baichuan 4' },
      { value: 'Baichuan3-Turbo', label: 'Baichuan 3 Turbo' },
      { value: 'Baichuan2-Turbo', label: 'Baichuan 2 Turbo' }
    ]
  },
  tongyi: {
    apiUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions',
    models: [
      { value: 'qwen-plus', label: 'Qwen Plus' },
      { value: 'qwen-turbo', label: 'Qwen Turbo' },
      { value: 'qwen-max', label: 'Qwen Max' },
      { value: 'qwen-long', label: 'Qwen Long' }
    ]
  },
  yi: {
    apiUrl: 'https://api.lingyiwanwu.com/v1/chat/completions',
    models: [
      { value: 'yi-large', label: 'Yi Large (Latest)' },
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
      { value: 'gpt-oss:20b', label: 'GPT-OSS 20B (Current)' },
      { value: 'llama3.2', label: 'Llama 3.2 (Latest)' },
      { value: 'llama3.1', label: 'Llama 3.1' },
      { value: 'llama3', label: 'Llama 3' },
      { value: 'llama3.2-vision', label: 'Llama 3.2 Vision (Multimodal)' },
      { value: 'mistral', label: 'Mistral' },
      { value: 'mistral-nemo', label: 'Mistral Nemo' },
      { value: 'mixtral', label: 'Mixtral 8x7B' },
      { value: 'qwen2.5', label: 'Qwen 2.5' },
      { value: 'qwen2.5-coder', label: 'Qwen 2.5 Coder' },
      { value: 'deepseek-r1', label: 'DeepSeek R1' },
      { value: 'deepseek-r1-distill-qwen', label: 'DeepSeek R1 (Qwen Distilled)' },
      { value: 'deepseek-r1-distill-llama', label: 'DeepSeek R1 (Llama Distilled)' },
      { value: 'codellama', label: 'Code Llama' },
      { value: 'codellama:70b', label: 'Code Llama 70B' },
      { value: 'gemma2', label: 'Gemma 2' },
      { value: 'gemma2:27b', label: 'Gemma 2 27B' },
      { value: 'phi3', label: 'Phi-3' },
      { value: 'phi3.5', label: 'Phi-3.5' },
      { value: 'nomic-embed-text', label: 'Nomic Embed Text (Embedding)' },
      { value: 'mxbai-embed-large', label: 'MxBai Embed Large (Embedding)' },
      { value: 'granite3.3', label: 'Granite 3.3' },
      { value: 'command-r7b', label: 'Command R7B' },
      { value: 'stablelm2', label: 'Stable LM 2' },
      { value: 'aya', label: 'Aya (Multilingual)' },
      { value: 'wizardlm2', label: 'WizardLM 2' },
      { value: 'neural-chat', label: 'Neural Chat' }
    ]
  }
}

const aiTesting = ref(false)
const aiTestResult = ref<'success' | 'error' | ''>('')
const ollamaLoading = ref(false)
const dynamicOllamaModels = ref<{ value: string; label: string }[]>([])

interface BigDataConfig {
  riskThreshold: number
  maxTokens: number
  temperature: number
  enableCache: boolean
  cacheTimeout: number
}

const bigDataConfig = reactive<BigDataConfig>({
  riskThreshold: 50,
  maxTokens: 2048,
  temperature: 0.7,
  enableCache: false,
  cacheTimeout: 60
})

const availableModels = computed(() => {
  if (aiConfig.provider === 'ollama' && dynamicOllamaModels.value.length > 0) {
    return dynamicOllamaModels.value
  }
  if (!aiConfig.provider || aiConfig.provider === 'none') return []
  const preset = providerPresets[aiConfig.provider]
  return preset?.models || []
})

const setRiskThresholdPreset = (value: number) => {
  bigDataConfig.riskThreshold = value
}

const isRiskPresetActive = (value: number) => {
  return bigDataConfig.riskThreshold === value
}

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
  
  if (provider === 'ollama') {
    loadOllamaModels()
  }
}

const loadOllamaModels = async () => {
  try {
    ollamaLoading.value = true
    dynamicOllamaModels.value = []
    
    const baseUrl = aiConfig.apiUrl.replace('/v1/chat/completions', '')
    const response = await getOllamaModels(baseUrl)
    
    if (response && response.data && response.data.length > 0) {
      dynamicOllamaModels.value = response.data
      if (!aiConfig.model && response.data.length > 0) {
        aiConfig.model = response.data[0].value
      }
      ElMessage.success(t('profile.ollamaModelsLoaded', { count: response.data.length }))
    } else {
      ElMessage.warning(t('profile.ollamaNoModels'))
    }
  } catch (error: any) {
    ElMessage.error(`${t('profile.ollamaLoadFailed')}: ${error.message || t('common.error')}`)
  } finally {
    ollamaLoading.value = false
  }
}

const handleTestAiConnection = async () => {
  if (!aiConfig.apiKey && aiConfig.provider !== 'ollama') {
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

const loadConfigs = async () => {
  try {
    const response = await getSystemConfigs()
    if (response && response.data) {
      const configs = response.data
      
      if (configs.ai_provider) aiConfig.provider = configs.ai_provider
      if (configs.ai_api_url) aiConfig.apiUrl = configs.ai_api_url
      if (configs.ai_api_key) aiConfig.apiKey = configs.ai_api_key
      if (configs.ai_model) aiConfig.model = configs.ai_model
      
      if (configs.bd_risk_threshold !== undefined) bigDataConfig.riskThreshold = Number(configs.bd_risk_threshold)
      if (configs.bd_max_tokens !== undefined) bigDataConfig.maxTokens = Number(configs.bd_max_tokens)
      if (configs.bd_temperature !== undefined) bigDataConfig.temperature = Number(configs.bd_temperature)
      if (configs.bd_enable_cache !== undefined) bigDataConfig.enableCache = configs.bd_enable_cache === true || configs.bd_enable_cache === 'true'
      if (configs.bd_cache_timeout !== undefined) bigDataConfig.cacheTimeout = Number(configs.bd_cache_timeout)
      
      if (configs.ntf_expiration_reminder !== undefined) notificationSettings.expirationReminder = configs.ntf_expiration_reminder === true || configs.ntf_expiration_reminder === 'true'
      if (configs.ntf_approval_notification !== undefined) notificationSettings.approvalNotification = configs.ntf_approval_notification === true || configs.ntf_approval_notification === 'true'
      if (configs.ntf_comment_notification !== undefined) notificationSettings.commentNotification = configs.ntf_comment_notification === true || configs.ntf_comment_notification === 'true'
      if (configs.ntf_system_notification !== undefined) notificationSettings.systemNotification = configs.ntf_system_notification === true || configs.ntf_system_notification === 'true'

    }
  } catch (error) {
    console.error('Failed to load profile configs:', error)
    ElMessage.error(t('common.error'))
  }
}

const handleSaveAiConfig = async () => {
  try {
    await saveSystemConfigs({
      ai_provider: aiConfig.provider,
      ai_api_url: aiConfig.apiUrl,
      ai_api_key: aiConfig.apiKey,
      ai_model: aiConfig.model
    })
    ElMessage.success(t('common.success'))
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const handleSaveBigDataConfig = async () => {
  try {
    await saveSystemConfigs({
      bd_risk_threshold: bigDataConfig.riskThreshold,
      bd_max_tokens: bigDataConfig.maxTokens,
      bd_temperature: bigDataConfig.temperature,
      bd_enable_cache: bigDataConfig.enableCache,
      bd_cache_timeout: bigDataConfig.cacheTimeout
    })
    ElMessage.success(t('common.success'))
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const handleResetBigDataConfig = async () => {
  bigDataConfig.riskThreshold = 50
  bigDataConfig.maxTokens = 2048
  bigDataConfig.temperature = 0.7
  bigDataConfig.enableCache = false
  bigDataConfig.cacheTimeout = 60
  
  try {
    await saveSystemConfigs({
      bd_risk_threshold: bigDataConfig.riskThreshold,
      bd_max_tokens: bigDataConfig.maxTokens,
      bd_temperature: bigDataConfig.temperature,
      bd_enable_cache: bigDataConfig.enableCache,
      bd_cache_timeout: bigDataConfig.cacheTimeout
    })
    ElMessage.success(t('common.success'))
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

onMounted(() => {
  loadUserData()
  loadConfigs()
  loadLoginHistory()
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
  
  .smart-analysis-panel {
    padding: 20px 24px 12px;
    margin-bottom: 8px;
    border-radius: 12px;
    border: 1px solid var(--el-border-color-lighter);
    background: var(--el-fill-color-blank);
  }

  .smart-analysis-intro {
    margin: 0 0 18px;
    font-size: 13px;
    line-height: 1.55;
    color: var(--el-text-color-secondary);
  }

  .analysis-section {
    margin-bottom: 12px;
  }

  .analysis-section-title {
    font-size: 13px;
    font-weight: 600;
    color: var(--el-text-color-primary);
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .advanced-section {
    margin-top: 8px;
    padding-top: 4px;
  }

  .smart-analysis-form-item {
    margin-bottom: 16px;
    :deep(.el-form-item__label) {
      width: 112px !important;
      padding-right: 12px;
      line-height: 32px;
    }
    :deep(.el-form-item__content) {
      align-items: flex-start;
      min-width: 0;
    }
  }

  .form-item-stack {
    display: flex;
    flex-direction: column;
    gap: 8px;
    width: 100%;
    max-width: 760px;
    min-width: 0;
  }

  .risk-presets {
    margin-bottom: 4px;
    :deep(.el-button) {
      min-width: 64px;
    }
  }

  .token-slider,
  .risk-slider,
  .temp-slider {
    width: 100%;
    max-width: 760px;
    :deep(.el-slider__runway) {
      margin-right: 8px;
    }
    :deep(.el-slider__input) {
      width: 108px;
    }
  }

  .cache-timeout-input {
    width: 100%;
    max-width: 320px;
  }

  .cache-row {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
    width: 100%;
    max-width: 760px;
  }

  .smart-analysis-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    margin-top: 20px;
    padding-top: 16px;
    border-top: 1px solid var(--el-border-color-lighter);
  }

  .form-item-hint {
    display: block;
    margin-top: 0;
    color: var(--el-text-color-secondary);
    font-size: 12px;
    line-height: 1.45;
  }

  @media (max-width: 900px) {
    .smart-analysis-panel {
      padding: 16px;
    }

    .risk-slider,
    .temp-slider {
      max-width: 100%;
    }

    .cache-timeout-input {
      max-width: 100%;
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
</style>
