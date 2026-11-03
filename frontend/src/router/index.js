// 路由配置：定义页面路径、嵌套结构，以及登录/权限守卫
import { createRouter, createWebHistory } from 'vue-router'
import { useTokenStore } from '@/stores/token'
import { useUserStore } from '@/stores/user'

const routes = [
  // 登录/注册页：公开页面（meta.public = true 表示无需登录即可访问）
  { path: '/login', name: 'login', component: () => import('@/views/Login.vue'), meta: { public: true } },
  { path: '/register', name: 'register', component: () => import('@/views/Register.vue'), meta: { public: true } },
  {
    // 前台整体布局：所有前台页面共用 FrontLayout（顶部导航 + 底部版权）
    path: '/',
    component: () => import('@/layout/FrontLayout.vue'),
    children: [
      { path: '', redirect: '/home' },
      // 以下带 meta.public 的页面无需登录，其余需登录
      { path: 'home', name: 'home', component: () => import('@/views/Home.vue'), meta: { public: true } },
      { path: 'pet', name: 'petList', component: () => import('@/views/PetList.vue'), meta: { public: true } },
      { path: 'pet/:id', name: 'petDetail', component: () => import('@/views/PetDetail.vue'), meta: { public: true } },
      { path: 'article', name: 'articleList', component: () => import('@/views/ArticleList.vue'), meta: { public: true } },
      { path: 'article/:id', name: 'articleDetail', component: () => import('@/views/ArticleDetail.vue'), meta: { public: true } },
      // 以下前台页面需要登录（未登录会被守卫拦到登录页）
      { path: 'rescue', name: 'rescue', component: () => import('@/views/Rescue.vue') },
      { path: 'volunteer', name: 'volunteer', component: () => import('@/views/Volunteer.vue') },
      { path: 'donate', name: 'donate', component: () => import('@/views/Donate.vue') },
      { path: 'ai', name: 'ai', component: () => import('@/views/AiChat.vue') },
      { path: 'profile', name: 'profile', component: () => import('@/views/Profile.vue') }
    ]
  },
  {
    // 后台整体布局：所有后台页面共用 AdminLayout（侧边栏 + 顶栏）
    // 后台页面还要求角色为 ADMIN 或 STAFF，见下方守卫逻辑
    path: '/admin',
    component: () => import('@/layout/AdminLayout.vue'),
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'dashboard', component: () => import('@/views/admin/Dashboard.vue') },
      { path: 'pet', name: 'adminPet', component: () => import('@/views/admin/PetManage.vue') },
      { path: 'adopt', name: 'adminAdopt', component: () => import('@/views/admin/AdoptAudit.vue') },
      { path: 'follow-up', name: 'adminFollowUp', component: () => import('@/views/admin/FollowUpManage.vue') },
      { path: 'rescue', name: 'adminRescue', component: () => import('@/views/admin/RescueManage.vue') },
      { path: 'volunteer', name: 'adminVolunteer', component: () => import('@/views/admin/VolunteerManage.vue') },
      { path: 'donation', name: 'adminDonation', component: () => import('@/views/admin/DonationManage.vue') },
      { path: 'user', name: 'adminUser', component: () => import('@/views/admin/UserManage.vue') },
      { path: 'banner', name: 'adminBanner', component: () => import('@/views/admin/BannerManage.vue') },
      { path: 'article', name: 'adminArticle', component: () => import('@/views/admin/ArticleManage.vue') },
      { path: 'category', name: 'adminCategory', component: () => import('@/views/admin/CategoryManage.vue') }
    ]
  },
  // 兜底路由：访问不存在的路径时重定向回首页
  { path: '/:pathMatch(.*)*', redirect: '/home' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫：每次路由跳转前执行，做登录与权限校验
router.beforeEach((to, from, next) => {
  const tokenStore = useTokenStore()
  const userStore = useUserStore()
  // 公开页面直接放行
  if (to.meta.public) {
    next()
    return
  }
  // 未登录：跳转到登录页
  if (!tokenStore.token) {
    next('/login')
    return
  }
  // 访问后台：只有 ADMIN（管理员）或 STAFF（员工）角色才允许，否则踢回前台首页
  if (to.path.startsWith('/admin')) {
    const role = userStore.info && userStore.info.role
    if (role !== 'ADMIN' && role !== 'STAFF') {
      next('/home')
      return
    }
  }
  next()
})

export default router
