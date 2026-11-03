// 资讯/科普文章相关接口
import request from '@/utils/request'

// 分页查询文章列表（前台展示用，可传分页/关键字参数）
export const getArticleList = (params) => request.get('/article/list', { params })

// 查询文章详情
export const getArticleDetail = (id) => request.get(`/article/detail/${id}`)

// 新增文章（管理员）
export const addArticle = (data) => request.post('/article', data)

// 更新文章（管理员）
export const updateArticle = (data) => request.put('/article', data)

// 删除文章（管理员）
export const deleteArticle = (id) => request.delete(`/article/${id}`)
