// 用户管理相关接口（管理员）
import request from '@/utils/request'

// 分页查询用户列表
export const getUserPage = (params) => request.get('/user/page', { params })

// 启用/禁用用户（修改账号状态）
export const updateUserStatus = (id, status) => request.put(`/user/${id}/status`, null, { params: { status } })

// 修改用户角色（如设为管理员、员工、志愿者等）
export const updateUserRole = (id, role) => request.put(`/user/${id}/role`, null, { params: { role } })

// 删除用户
export const deleteUser = (id) => request.delete(`/user/${id}`)
