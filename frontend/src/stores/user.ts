import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getUserInfo, register as registerApi } from '@/api/auth'
import type { User } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<User | null>(null)
  const roles = ref<string[]>([])
  const permissions = ref<string[]>([])

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => roles.value.includes('ADMIN'))
  const isLegal = computed(() => roles.value.includes('LEGAL'))

  // 登录
  const login = async (username: string, password: string) => {
    const res = await loginApi({ username, password })
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    userInfo.value = res.data
    roles.value = res.data.roles || []
    permissions.value = res.data.permissions || []
    return res
  }

  // 注册
  const register = async (data: { username: string; password: string; email?: string }) => {
    const res = await registerApi(data)
    return res
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    if (!token.value) return
    try {
      const res = await getUserInfo()
      userInfo.value = res.data
      roles.value = res.data.roles || []
      permissions.value = res.data.permissions || []
    } catch (error) {
      logout()
    }
  }

  // 登出
  const logout = () => {
    token.value = ''
    userInfo.value = null
    roles.value = []
    permissions.value = []
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    roles,
    permissions,
    isLoggedIn,
    isAdmin,
    isLegal,
    login,
    register,
    fetchUserInfo,
    logout
  }
})
