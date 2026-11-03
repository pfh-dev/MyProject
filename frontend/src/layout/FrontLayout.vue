<template>
  <div class="front-layout">
    <!-- 顶部导航栏：Logo + 导航链接 + 用户区 -->
    <header class="front-header">
      <div class="page-container header-inner">
        <!-- Logo，点击返回首页 -->
        <router-link to="/home" class="logo">
          <span class="logo-badge">
            <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
              <ellipse cx="12" cy="15" rx="5.4" ry="4.4" />
              <ellipse cx="4.6" cy="8.6" rx="2.3" ry="3" />
              <ellipse cx="9.7" cy="5.9" rx="2.3" ry="3" />
              <ellipse cx="14.3" cy="5.9" rx="2.3" ry="3" />
              <ellipse cx="19.4" cy="8.6" rx="2.3" ry="3" />
            </svg>
          </span>
          <span class="logo-text">宠爱之家</span>
        </router-link>
        <!-- 前台主导航链接 -->
        <nav class="nav">
          <router-link to="/home" class="nav-item">首页</router-link>
          <router-link to="/pet" class="nav-item">宠物领养</router-link>
          <router-link to="/article" class="nav-item">资讯科普</router-link>
          <router-link to="/rescue" class="nav-item">救助上报</router-link>
          <router-link to="/volunteer" class="nav-item">志愿者</router-link>
          <router-link to="/donate" class="nav-item">爱心捐赠</router-link>
          <router-link to="/ai" class="nav-item">AI 助手</router-link>
        </nav>
        <!-- 用户区：已登录显示消息红点 + 头像下拉；未登录显示登录/注册按钮 -->
        <div class="user-area">
          <template v-if="tokenStore.token">
            <!-- 消息铃铛：有未读时显示数量红点，点击跳到个人中心消息页 -->
            <el-badge :value="unread" :hidden="unread === 0" class="msg-badge">
              <el-icon class="msg-icon" @click="$router.push('/profile?tab=message')"><Bell /></el-icon>
            </el-badge>
            <!-- 头像下拉菜单：个人中心 / 管理后台（管理员可见）/ 退出登录 -->
            <el-dropdown @command="onCommand">
              <span class="avatar-wrap">
                <el-avatar :size="34" :src="avatar">{{ avatarText }}</el-avatar>
                <span class="nickname">{{ nickname }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item v-if="isAdmin" command="admin">管理后台</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button text @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" round @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- 前台主内容区：前台子路由页面在这里渲染 -->
    <main class="front-main">
      <router-view />
    </main>

    <!-- 底部版权区 -->
    <footer class="front-footer">
      <div class="page-container footer-inner">
        <div class="footer-brand">
          <span class="logo-badge">
            <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
              <ellipse cx="12" cy="15" rx="5.4" ry="4.4" />
              <ellipse cx="4.6" cy="8.6" rx="2.3" ry="3" />
              <ellipse cx="9.7" cy="5.9" rx="2.3" ry="3" />
              <ellipse cx="14.3" cy="5.9" rx="2.3" ry="3" />
              <ellipse cx="19.4" cy="8.6" rx="2.3" ry="3" />
            </svg>
          </span>
          <span>宠爱之家 · 宠物领养救助平台</span>
        </div>
        <p>让每一只流浪的毛孩子都有一个温暖的家</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useTokenStore } from '@/stores/token'
import { useUserStore } from '@/stores/user'
import { unreadCount } from '@/api/notification'
import { logout } from '@/api/auth'

const router = useRouter()
// 获取 token 与用户信息 store
const tokenStore = useTokenStore()
const userStore = useUserStore()

// 未读消息数量，默认 0
const unread = ref(0)
// 是否管理员/员工（用于决定是否显示「管理后台」入口）
const isAdmin = computed(() => {
  const role = userStore.info && userStore.info.role
  return role === 'ADMIN' || role === 'STAFF'
})
// 显示的昵称：优先昵称，其次用户名，最后兜底「用户」
const nickname = computed(() => userStore.info.nickname || userStore.info.username || '用户')
// 头像地址（可能为空，空时显示文字头像）
const avatar = computed(() => userStore.info.avatar || '')
// 文字头像：取昵称的第一个字
const avatarText = computed(() => (nickname.value || '用').slice(0, 1))

// 拉取未读消息数量（仅登录后请求）
const loadUnread = async () => {
  if (!tokenStore.token) return
  try {
    const res = await unreadCount()
    unread.value = res.data.count || 0
  } catch (e) { /* ignore */ }
}

// 处理头像下拉菜单命令
const onCommand = async (cmd) => {
  if (cmd === 'profile') router.push('/profile')
  else if (cmd === 'admin') router.push('/admin')
  else if (cmd === 'logout') {
    // 退出登录：先调后端接口（失败也继续本地清理），再清空本地状态并回首页
    try { await logout() } catch (e) { /* 忽略登出接口失败 */ }
    tokenStore.removeToken()
    userStore.removeInfo()
    router.push('/home')
  }
}

// 页面挂载时拉取一次未读消息数量
onMounted(loadUnread)
</script>

<style scoped lang="scss">
/* 前台整体布局：纵向排列头部、主内容、底部 */
.front-layout {
  min-height: 100%;
  display: flex;
  flex-direction: column;
}

/* 顶部导航：吸顶固定，半透明毛玻璃背景 */
.front-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 253, 248, 0.82);
  backdrop-filter: saturate(180%) blur(12px);
  border-bottom: 1px solid var(--border);
}

/* 头部内层：水平排列 Logo、导航、用户区 */
.header-inner {
  height: 68px;
  display: flex;
  align-items: center;
  gap: 30px;
}

/* Logo 区 */
.logo {
  display: flex;
  align-items: center;
  gap: 10px;

  .logo-badge {
    width: 38px;
    height: 38px;
    display: grid;
    place-items: center;
    border-radius: 12px;
    background: linear-gradient(135deg, var(--primary), var(--primary-deep));
    color: #fff;
    box-shadow: 0 6px 14px rgba(232, 93, 43, 0.32);

    svg {
      width: 22px;
      height: 22px;
    }
  }

  .logo-text {
    font-family: var(--font-display);
    font-size: 21px;
    font-weight: 700;
    color: var(--text);
  }
}

/* 导航链接：胶囊样式，激活项高亮 */
.nav {
  display: flex;
  gap: 4px;
  flex: 1;

  .nav-item {
    padding: 8px 14px;
    border-radius: 999px;
    color: var(--text);
    font-size: 15px;
    transition: all 0.2s ease;

    &:hover {
      color: var(--primary);
      background: var(--primary-soft);
    }

    &.router-link-active {
      color: var(--primary);
      background: var(--primary-soft);
      font-weight: 600;
    }
  }
}

/* 用户区（右侧） */
.user-area {
  display: flex;
  align-items: center;
  gap: 14px;
}

/* 消息铃铛 */
.msg-badge {
  cursor: pointer;

  .msg-icon {
    font-size: 20px;
    color: var(--text-light);
    transition: color 0.18s ease;

    &:hover {
      color: var(--primary);
    }
  }
}

/* 头像 + 昵称 的下拉触发器 */
.avatar-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;

  .nickname {
    font-size: 14px;
    color: var(--text);
  }
}

/* 主内容区：占据剩余高度 */
.front-main {
  flex: 1;
}

/* 底部：深色背景，顶部一条渐变装饰线 */
.front-footer {
  background: #2f2a26;
  color: #d8cfc4;
  padding: 48px 0;
  margin-top: 72px;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, var(--primary), var(--accent), var(--secondary));
  }

  .footer-inner {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
  }

  .footer-brand {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 17px;
    font-weight: 600;
    color: #fff;

    .logo-badge {
      width: 30px;
      height: 30px;
      display: grid;
      place-items: center;
      border-radius: 9px;
      background: var(--primary);
      color: #fff;

      svg {
        width: 17px;
        height: 17px;
      }
    }
  }

  p {
    font-size: 13px;
    color: #9a8f84;
  }
}
</style>
