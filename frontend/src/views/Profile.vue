<!--
  个人中心页：左侧用户信息卡，右侧 Tab 页签（账号设置 / 领养申请 / 救助记录 / 我的捐赠 / 消息通知）。
  每个 Tab 对应一组独立的数据加载接口，进入页面时并行拉取。
-->
<template>
  <div class="page-container profile-page">
    <el-row :gutter="24">
      <!-- 左侧用户信息卡片：头像、昵称、角色标签、基础信息 -->
      <el-col :span="6">
        <el-card shadow="never" class="user-card">
          <div class="user-head">
            <el-avatar :size="72" :src="info.avatar">{{ avatarText }}</el-avatar>
            <div class="user-name">{{ info.nickname || info.username }}</div>
            <el-tag size="small" :type="roleType">{{ roleLabel }}</el-tag>
          </div>
          <div class="user-meta">
            <div><span>用户名</span>{{ info.username }}</div>
            <div><span>邮箱</span>{{ info.email || '未设置' }}</div>
            <div><span>电话</span>{{ info.phone || '未设置' }}</div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧 Tab 页签区 -->
      <el-col :span="18">
        <el-card shadow="never">
          <el-tabs v-model="activeTab">
            <!-- 账号设置：资料编辑 + 修改密码 -->
            <el-tab-pane label="账号设置" name="profile">
              <el-form :model="profileForm" label-width="80px" class="profile-form">
                <el-row :gutter="20">
                  <el-col :span="12"><el-form-item label="昵称"><el-input v-model="profileForm.nickname" /></el-form-item></el-col>
                  <el-col :span="12"><el-form-item label="真实姓名"><el-input v-model="profileForm.realName" /></el-form-item></el-col>
                  <el-col :span="12"><el-form-item label="邮箱"><el-input v-model="profileForm.email" /></el-form-item></el-col>
                  <el-col :span="12"><el-form-item label="电话"><el-input v-model="profileForm.phone" /></el-form-item></el-col>
                  <el-col :span="12"><el-form-item label="性别">
                    <el-radio-group v-model="profileForm.gender">
                      <el-radio label="男">男</el-radio>
                      <el-radio label="女">女</el-radio>
                    </el-radio-group>
                  </el-form-item></el-col>
                  <el-col :span="12"><el-form-item label="年龄"><el-input-number v-model="profileForm.age" :min="1" :max="120" /></el-form-item></el-col>
                  <el-col :span="24"><el-form-item label="地址"><el-input v-model="profileForm.address" /></el-form-item></el-col>
                  <el-col :span="24"><el-form-item label="简介"><el-input v-model="profileForm.description" type="textarea" :rows="3" /></el-form-item></el-col>
                </el-row>
                <el-button type="primary" :loading="savingProfile" @click="saveProfile">保存资料</el-button>
              </el-form>

              <el-divider />
              <!-- 修改密码表单：修改成功后后端会吊销旧 token，前端跳回登录页 -->
              <h4 class="sub-title">修改密码</h4>
              <el-form :model="pwdForm" label-width="80px" class="profile-form">
                <el-col :span="12"><el-form-item label="原密码"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item></el-col>
                <el-button type="primary" :loading="savingPwd" @click="savePwd">修改密码</el-button>
              </el-form>
            </el-tab-pane>

            <!-- 领养申请：展示我的申请列表及审核状态 -->
            <el-tab-pane label="领养申请" name="adopt">
              <el-table :data="adoptions" v-loading="loadingAdopt" empty-text="暂无申请">
                <el-table-column prop="petName" label="宠物" min-width="120" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }"><el-tag :type="adoptStatusType(row.status)" size="small">{{ row.status }}</el-tag></template>
                </el-table-column>
                <el-table-column prop="applyTime" label="申请时间" width="170" />
                <el-table-column prop="auditRemark" label="审核备注" min-width="140" show-overflow-tooltip />
              </el-table>
            </el-tab-pane>

            <!-- 救助记录：展示我上报的救助线索及处理状态 -->
            <el-tab-pane label="救助记录" name="rescue">
              <el-table :data="rescues" v-loading="loadingRescue" empty-text="暂无记录">
                <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }"><el-tag :type="rescueStatusType(row.status)" size="small">{{ row.status }}</el-tag></template>
                </el-table-column>
                <el-table-column prop="createTime" label="上报时间" width="170" />
              </el-table>
            </el-tab-pane>

            <!-- 我的捐赠：资金/物资捐赠记录 -->
            <el-tab-pane label="我的捐赠" name="donation">
              <el-table :data="donations" v-loading="loadingDonation" empty-text="暂无记录">
                <el-table-column prop="type" label="类型" width="80" />
                <el-table-column label="内容" min-width="140">
                  <template #default="{ row }">{{ row.type === '资金' ? '¥' + row.amount : (row.itemName + ' × ' + row.quantity) }}</template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }"><el-tag size="small">{{ row.status }}</el-tag></template>
                </el-table-column>
                <el-table-column prop="donateTime" label="时间" width="170" />
              </el-table>
            </el-tab-pane>

            <!-- 消息通知：未读消息带圆点标记，点击单条标记已读，顶部按钮可全部已读 -->
            <el-tab-pane label="消息通知" name="message">
              <div class="msg-toolbar">
                <el-button size="small" text type="primary" @click="readAll">全部已读</el-button>
              </div>
              <el-empty v-if="!notifications.length" description="暂无消息" />
              <div v-else class="msg-list">
                <div v-for="n in notifications" :key="n.id" class="msg-item" :class="{ unread: n.isRead === 0 }" @click="markRead(n)">
                  <div class="msg-dot"></div>
                  <div class="msg-main">
                    <div class="msg-title">{{ n.title }} <el-tag size="small" type="info">{{ n.type }}</el-tag></div>
                    <div class="msg-content">{{ n.content }}</div>
                    <div class="msg-time">{{ n.createTime }}</div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getInfo, updateProfile, changePassword } from '@/api/auth'
import { myAdoptions } from '@/api/adopt'
import { myRescues } from '@/api/rescue'
import { myDonations } from '@/api/donation'
import { myNotifications, readNotification, readAllNotifications } from '@/api/notification'
import { useUserStore } from '@/stores/user'
import { useTokenStore } from '@/stores/token'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const tokenStore = useTokenStore()

// 响应式状态
const info = ref({})             // 用户基础信息
const activeTab = ref('profile') // 当前激活的 Tab
const savingProfile = ref(false) // 保存资料中
const savingPwd = ref(false)     // 修改密码中

// 资料编辑表单 与 修改密码表单
const profileForm = reactive({ nickname: '', realName: '', email: '', phone: '', gender: '男', age: null, address: '', description: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '' })

// 各 Tab 的数据列表及各自的加载状态
const adoptions = ref([])
const rescues = ref([])
const donations = ref([])
const notifications = ref([])
const loadingAdopt = ref(false)
const loadingRescue = ref(false)
const loadingDonation = ref(false)

// 头像显示：取昵称/用户名的首字符
const avatarText = computed(() => (info.value.nickname || info.value.username || '用').slice(0, 1))
// 角色中文名映射
const roleLabel = computed(() => {
  const m = { ADMIN: '管理员', STAFF: '员工', VOLUNTEER: '志愿者', USER: '普通用户' }
  return m[info.value.role] || '用户'
})
// 角色对应标签颜色
const roleType = computed(() => (info.value.role === 'ADMIN' ? 'danger' : info.value.role === 'STAFF' ? 'warning' : info.value.role === 'VOLUNTEER' ? 'success' : 'info'))

// 领养状态 / 救助状态 到标签颜色的映射函数
const adoptStatusType = (s) => (s === '已通过' ? 'success' : s === '已拒绝' ? 'info' : 'warning')
const rescueStatusType = (s) => (s === '已救助' ? 'success' : s === '处理中' ? 'warning' : 'danger')

// 加载用户信息，并同步到 store 和资料表单
const loadInfo = async () => {
  const res = await getInfo()
  info.value = res.data
  userStore.setInfo(res.data)
  Object.assign(profileForm, {
    nickname: res.data.nickname,
    realName: res.data.realName,
    email: res.data.email,
    phone: res.data.phone,
    gender: res.data.gender || '男',
    age: res.data.age,
    address: res.data.address,
    description: res.data.description
  })
}

// 保存资料：调用更新接口，成功后重新拉取最新信息
const saveProfile = async () => {
  savingProfile.value = true
  try {
    await updateProfile(profileForm)
    ElMessage.success('资料已更新')
    await loadInfo()
  } finally {
    savingProfile.value = false
  }
}

// 修改密码：改完后清空本地会话并跳登录（因为后端已吊销旧 token）
const savePwd = async () => {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) {
    ElMessage.warning('请填写原密码和新密码')
    return
  }
  savingPwd.value = true
  try {
    await changePassword(pwdForm)
    ElMessage.success('密码修改成功，请重新登录')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    // 后端已吊销当前令牌，清理本地会话并跳转登录
    tokenStore.removeToken()
    userStore.removeInfo()
    router.push('/login')
  } finally {
    savingPwd.value = false
  }
}

// 加载四个 Tab 的数据（领养申请 / 救助记录 / 捐赠 / 通知）
const loadAdoptions = async () => { loadingAdopt.value = true; try { adoptions.value = (await myAdoptions()).data || [] } finally { loadingAdopt.value = false } }
const loadRescues = async () => { loadingRescue.value = true; try { rescues.value = (await myRescues()).data || [] } finally { loadingRescue.value = false } }
const loadDonations = async () => { loadingDonation.value = true; try { donations.value = (await myDonations()).data || [] } finally { loadingDonation.value = false } }
const loadNotifications = async () => { notifications.value = (await myNotifications()).data || [] }

// 标记单条消息为已读（仅未读时请求）
const markRead = async (n) => {
  if (n.isRead === 0) {
    await readNotification(n.id)
    n.isRead = 1
  }
}

// 全部标记为已读
const readAll = async () => {
  await readAllNotifications()
  notifications.value.forEach((n) => { n.isRead = 1 })
  ElMessage.success('已全部标记为已读')
}

onMounted(async () => {
  await loadInfo()
  // 支持从 URL 带 tab 参数直接定位到指定页签
  const tab = route.query.tab
  if (tab && ['adopt', 'rescue', 'donation', 'message'].includes(tab)) activeTab.value = tab
  // 并行加载各 Tab 数据
  loadAdoptions()
  loadRescues()
  loadDonations()
  loadNotifications()
})
</script>

<style scoped lang="scss">
.profile-page {
  padding-top: 32px;
  padding-bottom: 40px;
}

.user-card {
  text-align: center;

  .user-head {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    padding: 16px 0;
    border-bottom: 1px solid var(--border);

    .user-name {
      font-size: 18px;
      font-weight: 600;
    }
  }

  .user-meta {
    padding: 16px 0;
    text-align: left;

    div {
      display: flex;
      justify-content: space-between;
      padding: 6px 0;
      font-size: 14px;
      color: var(--text-light);

      span {
        color: var(--text);
      }
    }
  }
}

.profile-form {
  max-width: 720px;
}

.sub-title {
  margin-bottom: 16px;
  color: var(--text);
}

.msg-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}

.msg-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.msg-item {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  border: 1px solid var(--border);
  border-radius: 8px;
  cursor: pointer;

  .msg-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: transparent;
    margin-top: 7px;
    flex-shrink: 0;
  }

  &.unread {
    background: #fff9f5;

    .msg-dot {
      background: var(--primary);
    }
  }

  .msg-main {
    flex: 1;

    .msg-title {
      font-weight: 600;
      margin-bottom: 4px;
    }

    .msg-content {
      font-size: 13px;
      color: var(--text-light);
      margin-bottom: 4px;
    }

    .msg-time {
      font-size: 12px;
      color: var(--text-light);
    }
  }
}
</style>
