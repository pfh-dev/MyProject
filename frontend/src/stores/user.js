// 用户信息状态管理（Pinia store）
import { defineStore } from 'pinia'
import { ref } from 'vue'

// 定义名为 'user' 的 store，persist: true 表示会自动持久化到 localStorage
export const useUserStore = defineStore('user', () => {
  // 保存当前登录用户的信息对象（昵称、头像、角色等），初始为空对象
  const info = ref({})
  // 设置用户信息（登录或获取 info 成功后调用）
  const setInfo = (u) => { info.value = u }
  // 清除用户信息（退出登录时调用）
  const removeInfo = () => { info.value = {} }
  // 导出对外可用的状态和方法
  return { info, setInfo, removeInfo }
}, { persist: true })
