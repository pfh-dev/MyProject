<!--
  救助上报页：左侧提交救助线索（标题/位置/联系方式/描述），右侧展示我的上报记录及处理状态。
-->
<template>
  <div class="page-container rescue-page">
    <div class="page-header">
      <h2>救助上报</h2>
      <p>发现需要救助的流浪动物，请及时上报，我们会尽快处理</p>
    </div>

    <el-row :gutter="24">
      <!-- 左侧：提交救助线索表单 -->
      <el-col :span="10">
        <el-card shadow="never" class="report-card">
          <template #header>提交救助线索</template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
            <el-form-item label="标题" prop="title">
              <el-input v-model="form.title" placeholder="简要描述，如：小区门口流浪猫" />
            </el-form-item>
            <el-form-item label="位置" prop="location">
              <el-input v-model="form.location" placeholder="发现地点" />
            </el-form-item>
            <el-form-item label="联系方式" prop="contact">
              <el-input v-model="form.contact" placeholder="便于联系的电话" />
            </el-form-item>
            <el-form-item label="描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请描述动物的状况、是否有伤等" />
            </el-form-item>
            <el-button type="primary" :loading="submitting" @click="submit">提交上报</el-button>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：我的上报记录表格 -->
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>我的上报记录</template>
          <el-table :data="rescues" v-loading="loading" empty-text="暂无上报记录">
            <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
            <el-table-column prop="location" label="位置" min-width="120" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="handleRemark" label="处理说明" min-width="140" show-overflow-tooltip />
            <el-table-column prop="createTime" label="上报时间" width="170" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { reportRescue, myRescues } from '@/api/rescue'

const formRef = ref()
const submitting = ref(false)
const loading = ref(false)
const rescues = ref([]) // 我的上报记录

// 救助线索表单
const form = reactive({ title: '', location: '', contact: '', description: '' })
// 校验规则：四个字段均必填
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  location: [{ required: true, message: '请输入位置', trigger: 'blur' }],
  contact: [{ required: true, message: '请输入联系方式', trigger: 'blur' }],
  description: [{ required: true, message: '请描述情况', trigger: 'blur' }]
}

// 救助状态 -> 标签颜色
const statusType = (s) => {
  if (s === '已救助') return 'success'
  if (s === '处理中') return 'warning'
  if (s === '已驳回') return 'info'
  return 'danger'
}

// 提交上报：校验 -> 调上报接口 -> 重置表单并刷新记录
const submit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    await reportRescue(form)
    ElMessage.success('上报成功，感谢您的爱心')
    formRef.value.resetFields()
    load()
  } finally {
    submitting.value = false
  }
}

// 加载我的上报记录
const load = async () => {
  loading.value = true
  try {
    const res = await myRescues()
    rescues.value = res.data || []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.rescue-page {
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

.report-card {
  border-radius: 12px;
}
</style>
