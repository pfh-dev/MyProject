// AI 助手（对话、宠物匹配推荐）相关接口
import request from '@/utils/request'

// 获取当前用户的 AI 会话列表
export const getSessions = () => request.get('/ai/sessions')

// 新建/保存一个 AI 会话
export const saveSession = (data) => request.post('/ai/session', data)

// 获取某个会话下的所有消息记录
export const getMessages = (sessionId) => request.get(`/ai/messages/${sessionId}`)

// 根据宠物 ID 让 AI 做领养匹配推荐
export const matchPet = (petId) => request.get(`/ai/match/${petId}`)
