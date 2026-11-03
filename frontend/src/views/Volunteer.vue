<!--
  志愿者申请页：左侧填写申请表（姓名/电话/技能/可服务时间），右侧展示当前申请状态。
-->
<template>
  <div class="page-container volunteer-page">
    <div class="page-header">
      <h2>志愿者申请</h2>
      <p>加入志愿者团队，用行动传递温暖</p>
    </div>

    <el-row :gutter="24">
      <!-- 左侧：志愿者申请表 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>申请成为志愿者</template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="您的真实姓名" />
            </el-form-item>
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="手机号" />
            </el-form-item>
            <el-form-item label="技能特长" prop="skills">
              <el-input v-model="form.skills" placeholder="如：宠物护理、摄影、文案等" />
            </el-form-item>
            <el-form-item label="可服务时间" prop="availableTime">
              <el-input v-model="form.availableTime" placeholder="如：周末全天" />
            </el-form-item>
            <el-button type="primary" :loading="submitting" @click="submit">提交申请</el-button>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：我的申请状态（有申请则展示详情，无则占位） -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>申请状态</template>
          <template v-if="myApply">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="姓名">{{ myApply.realName }}</el-descriptions-item>
              <el-descriptions-item label="技能">{{ myApply.skills || '未填写' }}</el-descriptions-item>
              <el-descriptions-item label="可服务时间">{{ myApply.availableTime || '未填写' }}</el-descriptions-item>
              <el-descriptions-item label="申请时间">{{ myApply.applyTime }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="statusType(myApply.status)">{{ myApply.status }}</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </template>
          <el-empty v-else description="您还未申请志愿者" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { applyVolunteer, myVolunteer } from '@/api/volunteer'

const formRef = ref()
const submitting = ref(false)
const myApply = ref(null) // 我当前的申请记录（没有则为 null）

// 申请表数据
const form = reactive({ realName: '', phone: '', skills: '', availableTime: '' })
// 校验规则：姓名必填、手机号格式、技能必填
const rules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, pattern: /^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  skills: [{ required: true, message: '请填写技能特长', trigger: 'blur' }]
}

// 状态 -> 标签颜色
const statusType = (s) => (s === '已通过' ? 'success' : s === '已拒绝' ? 'info' : 'warning')

// 提交申请：校验 -> 调申请接口 -> 刷新状态
const submit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    await applyVolunteer(form)
    ElMessage.success('申请已提交')
    load()
  } finally {
    submitting.value = false
  }
}

// 加载我的申请状态
const load = async () => {
  const res = await myVolunteer()
  myApply.value = res.data
}

onMounted(load)
</script>

<style scoped lang="scss">
.volunteer-page {
  padding-top: 32px;
  padding-bottom: 40px;
}

.page-header {
  margin-bottom: 24px;

  h2 {
    font-size: 28px;
  }

  p {
    color: var(--text-light);
    margin-top: 6px;
  }
}
</style>
