// 宠物信息与宠物分类相关接口
import request from '@/utils/request'

// 分页查询宠物列表（前台展示，可传筛选条件）
export const getPetList = (params) => request.get('/pet/list', { params })

// 查询宠物详情
export const getPetDetail = (id) => request.get(`/pet/detail/${id}`)

// 获取所有宠物分类
export const getCategories = () => request.get('/pet-category/all')

// 分页查询宠物（后台管理用）
export const getPetPage = (params) => request.get('/pet/page', { params })

// 新增宠物（管理员）
export const addPet = (data) => request.post('/pet', data)

// 更新宠物信息（管理员）
export const updatePet = (id, data) => request.put(`/pet/${id}`, data)

// 更新宠物状态（待领养/领养中/已领养等）
export const updatePetStatus = (id, status) => request.put(`/pet/${id}/status`, null, { params: { status } })

// 审核宠物发布（通过/驳回）
export const reviewPet = (id, reviewStatus) => request.put(`/pet/${id}/review`, null, { params: { reviewStatus } })

// 删除宠物（管理员）
export const deletePet = (id) => request.delete(`/pet/${id}`)

// 新增宠物分类（管理员）
export const addCategory = (data) => request.post('/pet-category', data)

// 更新宠物分类（管理员）
export const updateCategory = (data) => request.put('/pet-category', data)

// 删除宠物分类（管理员）
export const deleteCategory = (id) => request.delete(`/pet-category/${id}`)
