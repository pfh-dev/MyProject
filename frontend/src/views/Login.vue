<!--
  登录页：用户名+密码登录。登录成功后把后端返回的 token 和用户信息写入 Pinia store，
  再跳转到 redirect 参数指定的页面（或默认首页）。
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
      <h1>给流浪的 TA<br />一个温暖的家</h1>
      <p>登录后即可申请领养、上报救助、参与志愿者与爱心捐赠。</p>
      <div class="panel-deco">
        <span class="ring ring-1"></span>
        <span class="ring ring-2"></span>
        <span class="float-paw paw-a">🐶</span>
        <span class="float-paw paw-b">🐱</span>
        <span class="float-paw paw-c">🐰</span>
      </div>
    </aside>

    <!-- 右侧登录表单区 -->
    <main class="auth-main">
      <div class="auth-card">
        <div class="auth-brand">
          <h2>欢迎回来</h2>
          <p>登录宠爱之家，继续你的爱心之旅</p>
        </div>
        <!-- 登录表单：带必填校验，密码框按回车也可提交 -->
        <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent>
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" show-password :prefix-icon="Lock" @keyup.enter="onSubmit" />
          </el-form-item>
          <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="onSubmit">
            登 录
          </el-button>
        </el-form>
        <div class="auth-footer">
          还没有账号？<router-link to="/register" class="link">立即注册</router-link>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '@/api/auth'
import { useTokenStore } from '@/stores/token'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const tokenStore = useTokenStore() // token 持久化 store（本地存储）
const userStore = useUserStore()   // 用户信息 store

const formRef = ref()              // 表单引用，用于校验
const loading = ref(false)         // 登录按钮 loading 状态
const form = reactive({ username: '', password: '' }) // 登录表单数据

// 表单校验规则：用户名和密码均为必填
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 提交登录：校验表单 -> 调用登录接口 -> 存储 token 和用户信息 -> 跳转
const onSubmit = async () => {
  await formRef.value.validate()   // 校验不通过会抛异常并中止
  loading.value = true
  try {
    const res = await login(form)             // 调用登录接口
    tokenStore.setToken(res.data.token)       // 保存 token，后续请求会通过拦截器携带
    userStore.setInfo(res.data.user)          // 保存用户信息到 store
    ElMessage.success('登录成功')
    // 若 URL 带了 redirect 参数（如未登录访问某页被拦截），登录后回到原页面；否则去首页
    router.push(route.query.redirect || '/home')
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
  margin-bottom: 30px;

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
