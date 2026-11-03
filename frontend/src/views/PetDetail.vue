<!--
  宠物详情页：展示单只宠物的完整信息（封面、标签、描述、档案等）。
  提供「申请领养」和「AI 匹配推荐」两个核心交互，领养申请通过弹窗表单提交。
-->
<template>
  <div class="page-container detail-page" v-if="pet">
    <el-page-header @back="$router.back()" class="back-header">
      <template #content><span class="back-title">宠物详情</span></template>
    </el-page-header>

    <!-- 左侧封面图 + 右侧信息区 -->
    <div class="detail-main">
      <div class="detail-cover">
        <img v-if="pet.coverImg" :src="pet.coverImg" :alt="pet.name" />
        <div v-else class="cover-placeholder">🐾</div>
      </div>
      <div class="detail-info">
        <div class="name-row">
          <h1>{{ pet.name }}</h1>
          <el-tag :type="statusType" effect="dark">{{ pet.status || '待领养' }}</el-tag>
        </div>
        <div class="tags">
          <el-tag v-if="pet.categoryName">{{ pet.categoryName }}</el-tag>
          <el-tag v-if="pet.breed" type="info">{{ pet.breed }}</el-tag>
          <el-tag type="info">{{ pet.gender || '未知' }}</el-tag>
          <el-tag type="info">{{ pet.age || '年龄未知' }}</el-tag>
        </div>
        <p class="desc">{{ pet.description }}</p>
        <el-descriptions :column="2" border class="detail-desc">
          <el-descriptions-item label="疫苗情况">{{ pet.vaccine || '未说明' }}</el-descriptions-item>
          <el-descriptions-item label="绝育情况">{{ pet.sterilization || '未说明' }}</el-descriptions-item>
          <el-descriptions-item label="来源">{{ pet.source === 'USER' ? '爱心人士' : '平台' }}</el-descriptions-item>
          <el-descriptions-item label="登记时间">{{ pet.createTime }}</el-descriptions-item>
        </el-descriptions>
        <!-- 操作按钮：申请领养（仅「待领养」状态可点）、AI 匹配推荐 -->
        <div class="actions">
          <el-button type="primary" size="large" :disabled="pet.status !== '待领养'" @click="openApply">
            申请领养
          </el-button>
          <el-button size="large" :loading="matchLoading" @click="loadMatch">AI 匹配推荐</el-button>
        </div>
        <!-- AI 匹配推荐结果展示区，调用匹配接口后显示文本 -->
        <div v-if="matchText" class="match-result">
          <div class="match-title">🤖 AI 匹配推荐</div>
          <div class="match-body">{{ matchText }}</div>
        </div>
      </div>
    </div>

    <div v-if="pet.detail" class="detail-content">
      <h3>详细介绍</h3>
      <div class="rich-content" v-html="pet.detail"></div>
    </div>

    <!-- 领养申请弹窗：带表单校验，提交时调用申请领养接口 -->
    <el-dialog v-model="applyVisible" title="领养申请" width="520px">
      <el-form ref="formRef" :model="applyForm" :rules="rules" label-width="90px">
        <el-form-item label="宠物" required>
          <el-input :model-value="pet.name" disabled />
        </el-form-item>
        <el-form-item label="申请人" prop="applicantName">
          <el-input v-model="applyForm.applicantName" placeholder="您的姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="applyForm.phone" placeholder="手机号" />
        </el-form-item>
        <el-form-item label="居住地址" prop="address">
          <el-input v-model="applyForm.address" placeholder="现居住地址" />
        </el-form-item>
        <el-form-item label="职业" prop="occupation">
          <el-input v-model="applyForm.occupation" placeholder="您的职业" />
        </el-form-item>
        <el-form-item label="养宠经验" prop="hasExperience">
          <el-radio-group v-model="applyForm.hasExperience">
            <el-radio value="有">有</el-radio>
            <el-radio value="无">无</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="申请理由" prop="reason">
          <el-input v-model="applyForm.reason" type="textarea" :rows="3" placeholder="请简述领养原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPetDetail } from '@/api/pet'
import { applyAdopt } from '@/api/adopt'
import { matchPet } from '@/api/ai'
import { useTokenStore } from '@/stores/token'

const route = useRoute()
const router = useRouter()
const tokenStore = useTokenStore()

// 响应式状态
const pet = ref(null)          // 宠物详情数据
const matchText = ref('')      // AI 匹配推荐文本
const matchLoading = ref(false) // AI 匹配请求中
const applyVisible = ref(false) // 领养申请弹窗是否显示
const submitting = ref(false)   // 提交申请中
const formRef = ref()           // 表单引用，用于触发校验

// 领养申请表单数据
const applyForm = reactive({
  applicantName: '',
  phone: '',
  address: '',
  occupation: '',
  hasExperience: '无',
  reason: ''
})

// 表单校验规则：必填项 + 手机号格式校验
const rules = {
  applicantName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, pattern: /^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }],
  occupation: [{ required: true, message: '请输入职业', trigger: 'blur' }],
  reason: [{ required: true, message: '请填写申请理由', trigger: 'blur' }]
}

// 根据宠物状态映射到 el-tag 的颜色类型
const statusType = computed(() => {
  const s = pet.value && pet.value.status
  if (s === '待领养') return 'success'
  if (s === '领养中') return 'warning'
  return 'info'
})

// 加载宠物详情：根据路由参数 id 请求详情接口
const load = async () => {
  const res = await getPetDetail(route.params.id)
  pet.value = res.data
}

// 打开申请弹窗前先检查登录态，未登录则跳转登录页
const openApply = () => {
  if (!tokenStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  applyVisible.value = true
}

// 提交领养申请：先校验表单，再调用申请接口
const submitApply = async () => {
  await formRef.value.validate()               // 校验失败会抛出异常、阻止提交
  submitting.value = true
  try {
    await applyAdopt({ ...applyForm, petId: pet.value.id }) // 展开表单并附加宠物 id
    ElMessage.success('申请已提交，请等待审核')
    applyVisible.value = false
  } finally {
    submitting.value = false
  }
}

// AI 匹配推荐：调用 AI 接口获取针对该宠物的推荐文案
const loadMatch = async () => {
  matchLoading.value = true
  try {
    const res = await matchPet(pet.value.id)
    matchText.value = res.data || '暂无推荐结果'
  } catch (e) {
    matchText.value = 'AI 服务暂不可用，请稍后再试' // 失败时给用户友好提示
  } finally {
    matchLoading.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.detail-page {
  padding-top: 24px;
  padding-bottom: 40px;
}

.back-header {
  margin-bottom: 20px;
}

.detail-main {
  display: grid;
  grid-template-columns: 480px 1fr;
  gap: 32px;
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.detail-cover {
  height: 360px;
  border-radius: 12px;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .cover-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 80px;
    background: linear-gradient(135deg, #ffe2d5, #fff1ea);
  }
}

.detail-info {
  .name-row {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 16px;

    h1 {
      font-size: 30px;
      color: var(--text);
    }
  }

  .tags {
    display: flex;
    gap: 8px;
    margin-bottom: 16px;
  }

  .desc {
    color: var(--text-light);
    line-height: 1.7;
    margin-bottom: 20px;
  }

  .detail-desc {
    margin-bottom: 24px;
  }

  .actions {
    display: flex;
    gap: 12px;
  }
}

.match-result {
  margin-top: 20px;
  background: #f0faf9;
  border: 1px solid #b7e6e2;
  border-radius: 8px;
  padding: 16px;

  .match-title {
    font-weight: 600;
    color: var(--secondary);
    margin-bottom: 8px;
  }

  .match-body {
    font-size: 14px;
    color: var(--text);
    white-space: pre-wrap;
    line-height: 1.6;
  }
}

.detail-content {
  margin-top: 24px;
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  h3 {
    font-size: 20px;
    margin-bottom: 16px;
  }

  .rich-content {
    line-height: 1.8;
    color: #444;
  }
}
</style>
