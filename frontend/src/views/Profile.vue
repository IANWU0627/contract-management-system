<template>
  <div class="profile">
    <h1 class="page-title">{{ $t('profile.personalInfo') }}</h1>
    
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header><span>{{ t('profile.personalInfo') }}</span></template>
          
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
        
        <el-card shadow="hover" class="password-card">
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import type { FormInstance } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUserInfo, updateUserInfo, changePassword } from '@/api/auth'

const { t } = useI18n()

const userStore = useUserStore()

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
      // 直接更新本地存储的用户信息，不调用fetchUserInfo()
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
    // 验证文件
    if (!beforeAvatarUpload(file.raw)) {
      return
    }
    
    avatarLoading.value = true
    
    // 创建本地预览URL
    const url = URL.createObjectURL(file.raw)
    avatarUrl.value = url
    
    // 将头像转为base64保存到服务器
    const reader = new FileReader()
    reader.onload = async (e) => {
      const base64 = e.target?.result as string
      try {
        await updateUserInfo({ avatar: base64 })
        // 更新本地存储的用户信息
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
    // 直接使用store中的用户信息，不调用API
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

onMounted(() => {
  loadUserData()
})
</script>

<style scoped lang="scss">
.profile {
  .page-title { font-size: 24px; font-weight: 700; margin-bottom: 24px; background: var(--primary-gradient); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
  .password-card { margin-top: 20px; }
  .avatar-section { text-align: center;
    .avatar-upload-tip {
      font-size: 12px;
      color: var(--text-secondary);
      margin-top: 8px;
      line-height: 1.4;
    }
  }
}
</style>
