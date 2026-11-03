<!--
  捐赠管理页（后台）：查看所有用户捐赠记录，支持状态筛选和捐赠人搜索，并确认到账。
-->
<template>
  <div>
    <!-- 筛选栏：状态 + 捐赠人关键词 -->
    <div class="toolbar">
      <el-select v-model="query.status" placeholder="状态" clearable class="w-140" @change="onSearch">
        <el-option label="待确认" value="待确认" />
        <el-option label="已到账" value="已到账" />
        <el-option label="已确认" value="已确认" />
      </el-select>
      <el-input v-model="query.keyword" placeholder="搜索捐赠人" clearable class="w-200" :prefix-icon="Search" @keyup.enter="onSearch" />
      <el-button type="primary" @click="onSearch">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="username" label="捐赠人" width="100" />
      <el-table-column prop="type" label="类型" width="80">
        <template #default="{ row }"><el-tag :type="row.type === '资金' ? 'success' : 'primary'" size="small">{{ row.type }}</el-tag></template>
      </el-table-column>
      <el-table-column label="内容" min-width="150">
        <template #default="{ row }">{{ row.type === '资金' ? '¥' + row.amount : (row.itemName + ' × ' + row.quantity) }}</template>
      </el-table-column>
      <el-table-column prop="description" label="留言" min-width="140" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }"><el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="donateTime" label="时间" width="170" />
      <!-- 操作列：非「已确认」状态才显示确认按钮 -->
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status !== '已确认'" size="small" type="success" @click="confirm(row)">确认</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination background layout="prev, pager, next, total" :total="total" :page-size="query.pageSize" :current-page="query.pageNum" @current-change="onPageChange" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getDonationPage, confirmDonation } from '@/api/donation'

// 响应式状态
const list = ref([])
const total = ref(0)
const loading = ref(false)

// 查询条件：状态 + 捐赠人关键词 + 分页
const query = reactive({ status: '', keyword: '', pageNum: 1, pageSize: 10 })

// 状态 -> 标签颜色
const statusType = (s) => (s === '已确认' ? 'success' : s === '已到账' ? 'warning' : 'info')

// 加载捐赠记录列表
const load = async () => {
  loading.value = true
  try {
    const res = await getDonationPage({ status: query.status || undefined, keyword: query.keyword || undefined, pageNum: query.pageNum, pageSize: query.pageSize })
    list.value = (res.data && res.data.list) || []
    total.value = (res.data && res.data.total) || 0
  } finally {
    loading.value = false
  }
}

const onSearch = () => { query.pageNum = 1; load() }
const onPageChange = (p) => { query.pageNum = p; load() }

// 确认到账：调用确认接口，状态置为「已确认」
const confirm = async (row) => {
  await confirmDonation(row.id, '已确认')
  ElMessage.success('已确认')
  load()
}

onMounted(load)
</script>

<style scoped lang="scss">
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;

  .w-140 { width: 140px; }
  .w-200 { width: 200px; }
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
