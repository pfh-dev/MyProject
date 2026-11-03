// 领养申请与回访记录相关接口
import request from '@/utils/request'

// 提交领养申请（用户填写表单后提交）
export const applyAdopt = (data) => request.post('/adopt', data)

// 查询当前登录用户提交的领养申请列表
export const myAdoptions = () => request.get('/adopt/my')

// 分页查询领养申请（管理员审核用，可传分页/筛选参数）
export const getAdoptPage = (params) => request.get('/adopt/page', { params })

// 审核领养申请：通过/驳回，可附审核备注
export const auditAdopt = (id, status, remark) =>
  request.put(`/adopt/${id}/audit`, null, { params: { status, remark } })

// 查询某条领养记录的回访记录
export const getFollowUps = (adoptId) => request.get(`/follow-up/adopt/${adoptId}`)

// 新增一条回访记录
export const addFollowUp = (data) => request.post('/follow-up', data)

// 登记回访：更新一条回访记录（填写内容/宠物状况/是否正常等）
export const updateFollowUp = (id, data) => request.put(`/follow-up/${id}`, data)

// 分页查询回访记录（管理员管理用，可传状态/关键词筛选）
export const getFollowUpPage = (params) => request.get('/follow-up/page', { params })

// 删除某条回访记录（管理员）
export const deleteFollowUp = (id) => request.delete(`/follow-up/${id}`)
