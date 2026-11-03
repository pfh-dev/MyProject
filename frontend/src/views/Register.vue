<!--
  注册页：用户名、密码、确认密码、昵称、手机号。注册成功后自动登录并跳转首页。
  与登录页共用的视觉结构，区别是多几个字段和「两次密码一致」的自定义校验。
-->
<template>
  <div class="auth-page">
    <!-- 左侧品牌展示区（纯装饰） -->
    <aside class="auth-panel">
      <div class="panel-logo">
        <span class="logo-badge">
          <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
            <ellipse cx="12" cy="15" rx="5.4" ry="4.4" />
            <ellipse cx="4.6" cy="8.6" rx="2.3" ry="3" />
            <ellipse cx="9.7" cy="5.9" rx="2.3" ry="3" />
            <ellipse cx="14.3" cy="5.9" rx="2.3" ry="3" />
            <ellipse cx="19.4" cy="8.6" rx="2.3" ry="3" />
          </svg>
        </span>
        <span class="panel-brand">宠爱之家</span>
      </div>
      <h1>加入我们<br />开启爱心之旅</h1>
      <p>注册成为宠爱之家的一员，一起为流浪动物找到温暖的家。</p>
      <div class="panel-deco">
        <span class="ring ring-1"></span>
        <span class="ring ring-2"></span>
        <span class="float-paw paw-a">🐶</span>
        <span class="float-paw paw-b">🐱</span>
        <span class="float-paw paw-c">🐰</span>
      </div>
    </aside>

    <!-- 右侧注册表单区 -->
    <main class="auth-main">
      <div class="auth-card">
        <div class="auth-brand">
          <h2>注册账号</h2>
          <p>加入宠爱之家，开启爱心之旅</p>
        </div>
        <!-- 注册表单：用户名/密码/确认密码/昵称/手机号，均带校验 -->
        <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent>
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码（至少6位）" show-password :prefix-icon="Lock" />
          </el-form-item>
          <el-form-item prop="confirm">
            <el-input v-model="form.confirm" type="password" placeholder="确认密码" show-password :prefix-icon="Lock" />
          </el-form-item>
          <el-form-item prop="nickname">
            <el-input v-model="form.nickname" placeholder="昵称" :prefix-icon="Postcard" />
          </el-form-item>
          <el-form-item prop="phone">
            <el-input v-model="form.phone" placeholder="手机号" :prefix-icon="Iphone" />
          </el-form-item>
          <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="onSubmit">
            注 册
          </el-button>
        </el-form>
        <div class="auth-footer">
          已有账号？<router-link to="/login" class="link">去登录</router-link>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Postcard, Iphone } from '@element-plus/icons-vue'
import { register } from '@/api/auth'
import { useTokenStore } from '@/stores/token'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const tokenStore = useTokenStore()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '', confirm: '', nickname: '', phone: '' })

// 自定义校验函数：确认密码必须与密码一致（Element Plus 的 validator 约定回调签名）
const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) callback(new Error('两次输入的密码不一致'))
  else callback()
}

// 校验规则：confirm 字段用自定义 validator，password 限制至少 6 位，phone 校验格式
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirm: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [{ pattern: /^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}

// 提交注册：校验 -> 调注册接口（注意不提交 confirm 字段）-> 存 token/用户 -> 跳首页
const onSubmit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await register({
      username: form.username,
      password: form.password,
      nickname: form.nickname,
      phone: form.phone
    })
    tokenStore.setToken(res.data.token)
    userStore.setInfo(res.data.user)
    ElMessage.success('注册成功')
    router.push('/home')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.auth-page {
  min-height: 100%;
  display: grid;
  grid-template-columns: 1fr 1fr;
}

.auth-panel {
  position: relative;
  overflow: hidden;
  padding: 56px 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #5c463a;
  background:
    radial-gradient(600px 400px at 85% 15%, rgba(255, 255, 255, 0.55), transparent 55%),
    linear-gradient(150deg, #f8e7da 0%, #f2d5c2 100%);

  .panel-logo {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 48px;

    .logo-badge {
      width: 44px;
      height: 44px;
      display: grid;
      place-items: center;
      border-radius: 14px;
      background: linear-gradient(135deg, var(--primary), var(--primary-deep));
      color: #fff;

      svg {
        width: 25px;
        height: 25px;
      }
    }

    .panel-brand {
      font-family: var(--font-display);
      font-size: 24px;
      font-weight: 700;
      color: #4a352b;
    }
  }

  h1 {
    font-size: 40px;
    line-height: 1.32;
    font-weight: 700;
    margin-bottom: 20px;
    color: #4a352b;
  }

  p {
    font-size: 15px;
    line-height: 1.8;
    color: rgba(74, 53, 43, 0.72);
    max-width: 360px;
  }

  .panel-deco {
    position: absolute;
    inset: 0;
    pointer-events: none;

    .ring {
      position: absolute;
      border-radius: 50%;
      border: 2px solid rgba(232, 93, 43, 0.16);
    }

    .ring-1 {
      width: 260px;
      height: 260px;
      right: -70px;
      bottom: -60px;
    }

    .ring-2 {
      width: 160px;
      height: 160px;
      right: 20px;
      bottom: 40px;
      border-color: rgba(232, 93, 43, 0.10);
    }

    .float-paw {
      position: absolute;
      font-size: 34px;
      animation: float 6s ease-in-out infinite;
      filter: drop-shadow(0 8px 16px rgba(180, 120, 80, 0.25));
    }

    .paw-a { right: 18%; top: 30%; animation-delay: 0s; }
    .paw-b { right: 34%; top: 18%; animation-delay: 1.4s; font-size: 26px; }
    .paw-c { right: 8%; top: 52%; animation-delay: 2.6s; font-size: 28px; }
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-12px); }
}

.auth-main {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.auth-card {
  width: 400px;
  max-width: 100%;
}

.auth-brand {
  margin-bottom: 26px;

  h2 {
    font-size: 30px;
    font-weight: 700;
    color: var(--text);
    margin-bottom: 8px;
  }

  p {
    font-size: 14px;
    color: var(--text-light);
  }
}

.submit-btn {
  width: 100%;
  margin-top: 6px;
  height: 46px;
  font-size: 16px;
  letter-spacing: 0.2em;
}

.auth-footer {
  margin-top: 22px;
  text-align: center;
  font-size: 14px;
  color: var(--text-light);

  .link {
    color: var(--primary);
    font-weight: 600;
  }
}

@media (max-width: 900px) {
  .auth-page {
    grid-template-columns: 1fr;
  }

  .auth-panel {
    display: none;
  }
}
</style>
