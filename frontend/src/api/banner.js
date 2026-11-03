// 首页轮播图相关接口
import request from '@/utils/request'

// 获取所有启用的轮播图（前台首页展示用）
export const getBanners = () => request.get('/banner/all')

// 获取轮播图列表（后台管理用）
export const getBannerList = () => request.get('/banner/list')

// 新增轮播图（管理员）
export const addBanner = (data) => request.post('/banner', data)

// 更新轮播图（管理员）
export const updateBanner = (data) => request.put('/banner', data)

// 删除轮播图（管理员）
export const deleteBanner = (id) => request.delete(`/banner/${id}`)
