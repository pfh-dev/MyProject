<template>
  <div class="admin-layout">
    <!-- 左侧边栏：品牌标识 + 后台菜单导航 -->
    <aside class="sidebar">
      <div class="brand">
        <span class="logo-badge">
          <!-- 爪印样式的 Logo 图标（SVG） -->
          <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
            <ellipse cx="12" cy="15" rx="5.4" ry="4.4" />
            <ellipse cx="4.6" cy="8.6" rx="2.3" ry="3" />
            <ellipse cx="9.7" cy="5.9" rx="2.3" ry="3" />
            <ellipse cx="14.3" cy="5.9" rx="2.3" ry="3" />
            <ellipse cx="19.4" cy="8.6" rx="2.3" ry="3" />
          </svg>
        </span>
        <span class="brand-text">宠爱之家 · 管理后台</span>
      </div>
      <!-- el-menu 开启 router 模式，点击菜单项会按 index 路由跳转 -->
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#24303c"
        text-color="#aab6c4"
        active-text-color="#ff8a5c"
        class="side-menu"
      >
        <el-menu-item index="/admin/dashboard"><el-icon><DataAnalysis /></el-icon><span>数据看板</span></el-menu-item>
        <el-menu-item index="/admin/pet"><el-icon><Promotion /></el-icon><span>宠物管理</span></el-menu-item>
        <el-menu-item index="/admin/adopt"><el-icon><Stamp /></el-icon><span>领养审核</span></el-menu-item>
        <el-menu-item index="/admin/follow-up"><el-icon><Calendar /></el-icon><span>回访管理</span></el-menu-item>
        <el-menu-item index="/admin/rescue"><el-icon><FirstAidKit /></el-icon><span>救助管理</span></el-menu-item>
        <el-menu-item index="/admin/volunteer"><el-icon><UserFilled /></el-icon><span>志愿者管理</span></el-menu-item>
        <el-menu-item index="/admin/donation"><el-icon><Wallet /></el-icon><span>捐赠管理</span></el-menu-item>
        <el-menu-item index="/admin/user"><el-icon><User /></el-icon><span>用户管理</span></el-menu-item>
        <el-menu-item index="/admin/banner"><el-icon><Picture /></el-icon><span>轮播管理</span></el-menu-item>
        <el-menu-item index="/admin/article"><el-icon><Document /></el-icon><span>资讯管理</span></el-menu-item>
        <el-menu-item index="/admin/category"><el-icon><Menu /></el-icon><span>分类管理</span></el-menu-item>
      </el-menu>
    </aside>

    <!-- 右侧区域：顶栏（标题 + 用户操作）+ 内容区 -->
    <div class="admin-right">
      <header class="admin-header">
        <div class="page-title">{{ pageTitle }}</div>
        <div class="header-actions">
          <el-button text @click="$router.push('/home')">返回前台</el-button>
          <el-dropdown @command="onCommand">
            <span class="admin-avatar">
              <el-avatar :size="32">{{ avatarText }}</el-avatar>
              <span>{{ nickname }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <main class="admin-main">
        <!-- 后台子路由页面在这里渲染 -->
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTokenStore } from '@/stores/token'
import { useUserStore } from '@/stores/user'
import { logout } from '@/api/auth'

// 获取当前路由与路由对象
const route = useRoute()
const router = useRouter()
// 获取 token 与用户信息 store
const tokenStore = useTokenStore()
const userStore = useUserStore()

// 路由路径 -> 页面标题 的映射表，用于顶栏显示当前页名称
const titleMap = {
  '/admin/dashboard': '数据看板',
  '/admin/pet': '宠物管理',
  '/admin/adopt': '领养审核',
  '/admin/follow-up': '回访管理',
  '/admin/rescue': '救助管理',
  '/admin/volunteer': '志愿者管理',
  '/admin/donation': '捐赠管理',
  '/admin/user': '用户管理',
  '/admin/banner': '轮播管理',
  '/admin/article': '资讯管理',
  '/admin/category': '分类管理'
}

// 当前高亮的菜单项：直接取当前路径，与菜单 index 一致即可高亮
const activeMenu = computed(() => route.path)
// 当前页面标题：从 titleMap 查找，找不到则显示默认标题
const pageTitle = computed(() => titleMap[route.path] || '管理后台')
// 显示的昵称：优先昵称，其次用户名，最后兜底「管理员」
const nickname = computed(() => userStore.info.nickname || userStore.info.username || '管理员')
// 头像文字：取昵称的第一个字
const avatarText = computed(() => (nickname.value || '管').slice(0, 1))

// 处理顶栏下拉菜单命令
const onCommand = async (cmd) => {
  if (cmd === 'logout') {
    // 退出登录：先调后端接口（失败也继续本地清理），再清空本地状态并跳转登录页
    try { await logout() } catch (e) { /* 忽略登出接口失败 */ }
    tokenStore.removeToken()
    userStore.removeInfo()
    router.push('/login')
  }
}
</script>

<style scoped lang="scss">
/* 整体布局：左右分栏，左侧固定宽度侧边栏，右侧自适应 */
.admin-layout {
  display: flex;
  height: 100%;
}

/* 侧边栏：深色背景，纵向排列品牌区与菜单 */
.sidebar {
  width: 224px;
  background: var(--sidebar-bg);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}

/* 品牌区：Logo + 名称，底部加分隔线 */
.brand {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);

  /* Logo 图标：渐变背景的圆角小方块 */
  .logo-badge {
    width: 30px;
    height: 30px;
    display: grid;
    place-items: center;
    border-radius: 9px;
    background: linear-gradient(135deg, var(--primary), var(--primary-deep));
    color: #fff;

    svg {
      width: 17px;
      height: 17px;
    }
  }

  .brand-text {
    font-family: var(--font-display);
    font-size: 16px;
    font-weight: 600;
  }
}

/* 菜单：撑满剩余高度，可滚动；菜单项做成圆角样式 */
.side-menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;

  :deep(.el-menu-item) {
    height: 46px;
    margin: 4px 10px;
    border-radius: 10px;

    &.is-active {
      background: rgba(255, 138, 92, 0.14);
    }
  }
}

/* 右侧区域：纵向排列顶栏与内容区 */
.admin-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

/* 顶栏：左侧标题、右侧操作按钮 */
.admin-header {
  height: 64px;
  background: var(--surface);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  border-bottom: 1px solid var(--border);

  .page-title {
    font-family: var(--font-display);
    font-size: 19px;
    font-weight: 700;
    color: var(--text);
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 16px;
  }
}

/* 头像 + 昵称 的下拉触发器 */
.admin-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
  color: var(--text);
  font-size: 14px;
}

/* 内容区：内边距包裹，可滚动 */
.admin-main {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}
</style>
