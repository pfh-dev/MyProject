// 流浪动物救助上报相关接口
import request from '@/utils/request'

// 提交救助上报（用户填写发现地点、情况等）
export const reportRescue = (data) => request.post('/rescue', data)

// 查询当前用户提交的救助记录
export const myRescues = () => request.get('/rescue/my')

// 分页查询救助记录（管理员管理用）
export const getRescuePage = (params) => request.get('/rescue/page', { params })

// 分配救助任务给某个处理人（志愿者/员工）
export const assignRescue = (id, assigneeId) => request.put(`/rescue/${id}/assign`, null, { params: { assigneeId } })

// 处理救助任务：更新处理状态，可附备注
export const handleRescue = (id, status, remark) =>
  request.put(`/rescue/${id}/handle`, null, { params: { status, remark } })

// 删除救助记录（管理员）
export const deleteRescue = (id) => request.delete(`/rescue/${id}`)
