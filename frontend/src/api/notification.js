// 消息通知相关接口
import request from '@/utils/request'

// 查询当前用户的通知列表
export const myNotifications = () => request.get('/notification/my')

// 查询当前用户未读通知的数量（用于右上角红点提示）
export const unreadCount = () => request.get('/notification/unread-count')

// 将某条通知标记为已读
export const readNotification = (id) => request.put(`/notification/${id}/read`)

// 将所有通知标记为已读
export const readAllNotifications = () => request.put('/notification/read-all')
