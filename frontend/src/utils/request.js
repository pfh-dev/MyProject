// axios 请求封装：统一配置、注入 JWT、统一处理响应与错误
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useTokenStore } from '@/stores/token'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建 axios 实例
// baseURL：所有请求统一走 /api 前缀（由 vite 开发代理转发到后端）
// timeout：超过 30 秒未响应则视为超时
const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// ========== 请求拦截器 ==========
// 在每次请求真正发出之前执行，用来统一做「注入登录凭证」
request.interceptors.request.use(
  (config) => {
    // 从 Pinia 里取出 token store
    const tokenStore = useTokenStore()
    // 如果本地存在 token，就把它放进请求头 Authorization 里（实现 JWT 注入）
    if (tokenStore.token) {
      config.headers.Authorization = tokenStore.token
    }
    // 返回修改后的配置，请求才会继续发出去
    return config
  },
  (error) => Promise.reject(error)
)

// ========== 响应拦截器 ==========
// 每次请求返回后执行，用来统一处理「业务 code」与「HTTP 错误」
request.interceptors.response.use(
  (response) => {
    // 后端统一返回 { code, msg, data } 结构
    const res = response.data
    // code === 200 表示业务成功，直接返回整个 res 给调用方
    if (res.code === 200) {
      return res
    }
    // 业务失败：弹出后端返回的错误信息，并抛错让调用方 catch
    ElMessage.error(res.msg || '操作失败')
    return Promise.reject(new Error(res.msg || '操作失败'))
  },
  (error) => {
    // 这里处理 HTTP 层错误（如 401/403/500 等状态码）
    const status = error.response && error.response.status
    if (status === 401) {
      // 401：未登录或登录已过期。清空本地 token 和用户信息，并跳转登录页
      const tokenStore = useTokenStore()
      const userStore = useUserStore()
      tokenStore.removeToken()
      userStore.removeInfo()
      ElMessage.error('请先登录')
      router.push('/login')
    } else if (status === 403) {
      // 403：没有权限访问
      ElMessage.error('无权限访问')
    } else {
      // 其他错误：优先展示后端返回的 msg，否则给通用提示
      ElMessage.error((error.response && error.response.data && error.response.data.msg) || '服务器异常')
    }
    return Promise.reject(error)
  }
)

export default request
