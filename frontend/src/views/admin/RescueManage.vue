<!--
  救助管理页（后台）：查看用户上报的救助线索，支持状态筛选/搜索，
  以及「分配志愿者」「标记已救助/驳回」「删除」等操作。
-->
<template>
  <div>
    <!-- 筛选栏：状态 + 标题/位置关键词 -->
    <div class="toolbar">
      <el-select v-model="query.status" placeholder="状态" clearable class="w-140" @change="onSearch">
        <el-option label="待处理" value="待处理" />
        <el-option label="处理中" value="处理中" />
        <el-option label="已救助" value="已救助" />
        <el-option label="已驳回" value="已驳回" />
      </el-select>
      <el-input v-model="query.keyword" placeholder="搜索标题/位置" clearable class="w-200" :prefix-icon="Search" @keyup.enter="onSearch" />
      <el-button type="primary" @click="onSearch">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="title" label="标题" min-width="140" show-overflow-tooltip />
      <el-table-column prop="location" label="位置" min-width="120" show-overflow-tooltip />
      <el-table-column prop="contact" label="联系方式" width="130" />
      <el-table-column prop="reporterName" label="上报人" width="100" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }"><el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="assigneeName" label="处理人" width="100" />
      <el-table-column prop="createTime" label="上报时间" width="170" />
      <!-- 操作列：分配 / 已救助（处理中或待处理）/ 驳回（仅待处理）/ 删除 -->
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openAssign(row)">分配</el-button>
          <el-button v-if="row.status === '处理中' || row.status === '待处理'" size="small" type="success" @click="handle(row, '已救助')">已救助</el-button>
          <el-button v-if="row.status === '待处理'" size="small" type="danger" @click="handle(row, '已驳回')">驳回</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination background layout="prev, pager, next, total" :total="total" :page-size="query.pageSize" :current-page="query.pageNum" @current-change="onPageChange" />
    </div>

    <!-- 分配志愿者弹窗：下拉选择已通过审核的志愿者 -->
    <el-dialog v-model="assignVisible" title="分配志愿者" width="420px">
      <el-select v-model="assigneeId" placeholder="选择志愿者" style="width: 100%">
        <el-option v-for="v in volunteers" :key="v.id" :label="v.realName" :value="v.id" />
      </el-select>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAssign">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getRescuePage, assignRescue, handleRescue, deleteRescue } from '@/api/rescue'
import { getApprovedVolunteers } from '@/api/volunteer'

// 响应式状态
const list = ref([])              // 救助线索列表
const total = ref(0)              // 总条数
const loading = ref(false)        // 表格加载中
const volunteers = ref([])        // 可选志愿者（已通过审核的）
const assignVisible = ref(false)  // 分配弹窗开关
const assigneeId = ref(null)      // 选中的志愿者 id
const currentRow = ref(null)      // 当前分配操作的线索

// 查询条件
const query = reactive({ status: '', keyword: '', pageNum: 1, pageSize: 10 })

// 状态 -> 标签颜色
const statusType = (s) => (s === '已救助' ? 'success' : s === '处理中' ? 'warning' : s === '已驳回' ? 'info' : 'danger')

// 加载救助线索列表
const load = async () => {
  loading.value = true
  try {
    const res = await getRescuePage({ status: query.status || undefined, keyword: query.keyword || undefined, pageNum: query.pageNum, pageSize: query.pageSize })
    list.value = (res.data && res.data.list) || []
    total.value = (res.data && res.data.total) || 0
  } finally {
    loading.value = false
  }
}

const onSearch = () => { query.pageNum = 1; load() }
const onPageChange = (p) => { query.pageNum = p; load() }

// 打开分配弹窗：记录当前行，回填已有的分配人
const openAssign = (row) => {
  currentRow.value = row
  assigneeId.value = row.assigneeId || null
  assignVisible.value = true
}

// 确认分配：校验已选志愿者后调用分配接口
const confirmAssign = async () => {
  if (!assigneeId.value) { ElMessage.warning('请选择志愿者'); return }
  await assignRescue(currentRow.value.id, assigneeId.value)
  ElMessage.success('分配成功')
  assignVisible.value = false
  load()
}

// 处理线索（已救助/驳回）：弹窗填写处理说明后调用接口
const handle = async (row, status) => {
  const remark = await ElMessageBox.prompt('请输入处理说明', '处理', { inputValue: '' }).then(({ value }) => value).catch(() => null)
  if (remark === null) return // 用户取消弹窗
  await handleRescue(row.id, status, remark)
  ElMessage.success('处理完成')
  load()
}

// 删除线索：二次确认
const remove = async (row) => {
  await ElMessageBox.confirm('确认删除该救助线索？', '提示', { type: 'warning' })
  await deleteRescue(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(async () => {
  load()
  // 加载可选志愿者列表（用于分配）
  const v = await getApprovedVolunteers()
  volunteers.value = v.data || []
})
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
