<!--
  志愿者管理页（后台）：审核志愿者申请（通过/拒绝）、删除申请，支持状态筛选和姓名搜索。
-->
<template>
  <div>
    <!-- 筛选栏：状态 + 姓名关键词 -->
    <div class="toolbar">
      <el-select v-model="query.status" placeholder="状态" clearable class="w-140" @change="onSearch">
        <el-option label="申请中" value="申请中" />
        <el-option label="已通过" value="已通过" />
        <el-option label="已拒绝" value="已拒绝" />
      </el-select>
      <el-input v-model="query.keyword" placeholder="搜索姓名" clearable class="w-200" :prefix-icon="Search" @keyup.enter="onSearch" />
      <el-button type="primary" @click="onSearch">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column prop="skills" label="技能特长" min-width="140" show-overflow-tooltip />
      <el-table-column prop="availableTime" label="可服务时间" min-width="120" />
      <el-table-column prop="applyTime" label="申请时间" width="170" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }"><el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag></template>
      </el-table-column>
      <!-- 操作列：仅「申请中」显示通过/拒绝，删除始终可见 -->
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === '申请中'">
            <el-button size="small" type="success" @click="audit(row, '已通过')">通过</el-button>
            <el-button size="small" type="danger" @click="audit(row, '已拒绝')">拒绝</el-button>
          </template>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getVolunteerPage, auditVolunteer, deleteVolunteer } from '@/api/volunteer'

// 响应式状态
const list = ref([])
const total = ref(0)
const loading = ref(false)

// 查询条件：状态 + 姓名关键词 + 分页
const query = reactive({ status: '', keyword: '', pageNum: 1, pageSize: 10 })

// 状态 -> 标签颜色
const statusType = (s) => (s === '已通过' ? 'success' : s === '已拒绝' ? 'info' : 'warning')

// 加载志愿者申请列表
const load = async () => {
  loading.value = true
  try {
    const res = await getVolunteerPage({ status: query.status || undefined, keyword: query.keyword || undefined, pageNum: query.pageNum, pageSize: query.pageSize })
    list.value = (res.data && res.data.list) || []
    total.value = (res.data && res.data.total) || 0
  } finally {
    loading.value = false
  }
}

const onSearch = () => { query.pageNum = 1; load() }
const onPageChange = (p) => { query.pageNum = p; load() }

// 审核志愿者申请：通过/拒绝
const audit = async (row, status) => {
  await auditVolunteer(row.id, status)
  ElMessage.success('审核完成')
  load()
}

// 删除：二次确认
const remove = async (row) => {
  await ElMessageBox.confirm('确认删除该志愿者？', '提示', { type: 'warning' })
  await deleteVolunteer(row.id)
  ElMessage.success('删除成功')
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
