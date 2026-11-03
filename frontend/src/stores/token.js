// token 状态管理（Pinia store）
import { defineStore } from 'pinia'
import { ref } from 'vue'

// 定义名为 'token' 的 store，persist: true 表示会自动持久化到 localStorage
export const useTokenStore = defineStore('token', () => {
  // 保存登录凭证 token，初始为空字符串
  const token = ref('')
  // 设置 token（登录成功后调用）
  const setToken = (t) => { token.value = t }
  // 清除 token（退出登录时调用）
  const removeToken = () => { token.value = '' }
  // 导出对外可用的状态和方法
  return { token, setToken, removeToken }
}, { persist: true })
