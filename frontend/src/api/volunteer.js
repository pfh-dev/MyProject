// 志愿者相关接口
import request from '@/utils/request'

// 提交志愿者申请（用户填写报名信息）
export const applyVolunteer = (data) => request.post('/volunteer/apply', data)

// 查询当前用户的志愿者申请/信息
export const myVolunteer = () => request.get('/volunteer/my')

// 分页查询志愿者申请（管理员审核用）
export const getVolunteerPage = (params) => request.get('/volunteer/page', { params })

// 获取已审核通过的志愿者列表（用于分配救助任务等）
export const getApprovedVolunteers = () => request.get('/volunteer/approved')

// 审核志愿者申请：通过/驳回
export const auditVolunteer = (id, status) => request.put(`/volunteer/${id}/audit`, null, { params: { status } })

// 删除志愿者记录（管理员）
export const deleteVolunteer = (id) => request.delete(`/volunteer/${id}`)
