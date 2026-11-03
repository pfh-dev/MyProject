// 数据看板统计相关接口
import request from '@/utils/request'

// 获取平台概览数据（宠物、领养、捐赠等总览数字）
export const getOverview = () => request.get('/stats/overview')

// 获取趋势数据（用于折线图，如按月统计）
export const getTrend = () => request.get('/stats/trend')

// 获取分布数据（用于饼图/柱状图，如各分类占比）
export const getDistribution = () => request.get('/stats/distribution')
