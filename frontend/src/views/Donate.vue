<!--
  爱心捐赠页：左侧发起捐赠（资金/物资二选一，字段随类型切换），右侧展示我的捐赠记录。
-->
<template>
  <div class="page-container donate-page">
    <div class="page-header">
      <h2>爱心捐赠</h2>
      <p>您的每一份爱心，都是流浪动物活下去的希望</p>
    </div>

    <el-row :gutter="24">
      <!-- 左侧：发起捐赠表单，资金/物资切换显示不同字段 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>发起捐赠</template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
            <el-form-item label="捐赠类型" prop="type">
              <el-radio-group v-model="form.type">
                <el-radio-button label="资金">资金</el-radio-button>
                <el-radio-button label="物资">物资</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <!-- 选「资金」时填金额，选「物资」时填物品名称和数量 -->
            <el-form-item v-if="form.type === '资金'" label="金额" prop="amount">
              <el-input-number v-model="form.amount" :min="1" :precision="2" :step="10" style="width: 200px" />
            </el-form-item>
            <template v-else>
              <el-form-item label="物品名称" prop="itemName">
                <el-input v-model="form.itemName" placeholder="如：狗粮、猫砂" />
              </el-form-item>
              <el-form-item label="数量" prop="quantity">
                <el-input-number v-model="form.quantity" :min="1" style="width: 200px" />
              </el-form-item>
            </template>
            <el-form-item label="留言" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="3" placeholder="想说的话（选填）" />
            </el-form-item>
            <el-button type="primary" :loading="submitting" @click="submit">确认捐赠</el-button>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：我的捐赠记录表格 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>我的捐赠记录</template>
          <el-table :data="donations" v-loading="loading" empty-text="暂无捐赠记录">
            <el-table-column prop="type" label="类型" width="80">
              <template #default="{ row }">
                <el-tag :type="row.type === '资金' ? 'success' : 'primary'" size="small">{{ row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="内容" min-width="140">
              <template #default="{ row }">
                {{ row.type === '资金' ? '¥' + row.amount : (row.itemName + ' × ' + row.quantity) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="donateStatusType(row.status)" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="donateTime" label="时间" width="170" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { donate, myDonations } from '@/api/donation'

const formRef = ref()          // 表单引用
const submitting = ref(false)  // 提交中
const loading = ref(false)     // 记录表格加载中
const donations = ref([])      // 我的捐赠记录

// 捐赠表单：默认资金类型，金额默认 100
const form = reactive({ type: '资金', amount: 100, itemName: '', quantity: 1, description: '' })
const rules = {
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

// 捐赠状态 -> 标签颜色
const donateStatusType = (s) => (s === '已确认' ? 'success' : s === '已到账' ? 'warning' : 'info')

// 提交捐赠：校验 -> 调捐赠接口 -> 重置表单并刷新记录
const submit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    await donate(form)
    ElMessage.success('感谢您的爱心捐赠')
    formRef.value.resetFields()
    form.type = '资金'   // resetFields 后需手动恢复默认类型
    load()
  } finally {
    submitting.value = false
  }
}

// 加载我的捐赠记录
const load = async () => {
  loading.value = true
  try {
    const res = await myDonations()
    donations.value = res.data || []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.donate-page {
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
