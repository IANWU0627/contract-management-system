import { get, post, put } from './index'

// 登录
export const login = (data: { username: string; password: string }) => 
  post('/auth/login', data)

// 注册
export const register = (data: { username: string; password: string; email?: string }) => 
  post('/auth/register', data)

// 获取用户信息
export const getUserInfo = () => get('/users/me')

// 更新用户信息
export const updateUserInfo = (data: any) => put('/users/me', data)

// 修改密码
export const changePassword = (data: { oldPassword: string; newPassword: string }) => 
  post('/users/change-password', data)
