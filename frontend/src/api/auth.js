// 认证相关接口：登录、注册、个人信息维护
import request from '@/utils/request'

// 登录：提交账号密码，成功后后端返回 token 与用户信息
export const login = (data) => request.post('/auth/login', data)

// 注册：提交注册信息，创建新用户
export const register = (data) => request.post('/auth/register', data)

// 获取当前登录用户的信息（刷新页面后用于恢复用户状态）
export const getInfo = () => request.get('/auth/info')

// 退出登录
export const logout = () => request.post('/auth/logout')

// 修改密码
export const changePassword = (data) => request.put('/auth/password', data)

// 更新个人资料（昵称、头像等）
export const updateProfile = (data) => request.put('/auth/profile', data)
