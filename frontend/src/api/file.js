// 文件上传相关接口
import request from '@/utils/request'

// 上传单个文件（如图片），以 multipart/form-data 方式提交
export const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
