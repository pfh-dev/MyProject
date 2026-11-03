// 爱心捐赠相关接口
import request from '@/utils/request'

// 提交捐赠（用户填写金额/物资等信息）
export const donate = (data) => request.post('/donation', data)

// 查询当前用户的捐赠记录
export const myDonations = () => request.get('/donation/my')

// 分页查询捐赠记录（管理员管理用）
export const getDonationPage = (params) => request.get('/donation/page', { params })

// 确认/审核捐赠状态（如确认到账）
export const confirmDonation = (id, status) => request.put(`/donation/${id}/confirm`, null, { params: { status } })
