<!--
  领养审核管理页（后台）：管理员查看领养申请列表，对「待审核」的申请执行通过/拒绝操作。
  通过/拒绝时用弹窗让管理员填写备注，审核后重新刷新列表。
-->
<template>
  <div>
    <!-- 筛选栏：按状态筛选 + 按宠物/申请人关键词搜索 -->
    <div class="toolbar">
      <el-select v-model="query.status" placeholder="状态" clearable class="w-140" @change="onSearch">
        <el-option label="待审核" value="待审核" />
        <el-option label="已通过" value="已通过" />
        <el-option label="已拒绝" value="已拒绝" />
      </el-select>
      <el-input v-model="query.keyword" placeholder="搜索宠物/申请人" clearable class="w-200" :prefix-icon="Search" @keyup.enter="onSearch" />
      <el-button type="primary" @click="onSearch">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="petName" label="宠物" min-width="100" />
      <el-table-column prop="applicantName" label="申请人" width="100" />
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column prop="occupation" label="职业" width="100" />
      <el-table-column prop="hasExperience" label="养宠经验" width="90" />
      <el-table-column prop="reason" label="申请理由" min-width="160" show-overflow-tooltip />
      <el-table-column prop="applyTime" label="申请时间" width="170" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }"><el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="auditRemark" label="备注" min-width="120" show-overflow-tooltip />
      <!-- 操作列：仅「待审核」显示通过/拒绝按钮，否则只显示详情 -->
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === '待审核'">
            <el-button size="small" type="success" @click="audit(row, '已通过')">通过</el-button>
            <el-button size="small" type="danger" @click="audit(row, '已拒绝')">拒绝</el-button>
          </template>
          <el-button v-else size="small" link @click="viewDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination background layout="prev, pager, next, total" :total="total" :page-size="query.pageSize" :current-page="query.pageNum" @current-change="onPageChange" />
    </div>

    <!-- 申请详情弹窗：只读展示完整申请信息 -->
    <el-dialog v-model="detailVisible" title="申请详情" width="520px">
      <el-descriptions :column="1" border v-if="current">
        <el-descriptions-item label="宠物">{{ current.petName }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ current.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ current.phone }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{ current.address }}</el-descriptions-item>
        <el-descriptions-item label="职业">{{ current.occupation }}</el-descriptions-item>
        <el-descriptions-item label="养宠经验">{{ current.hasExperience }}</el-descriptions-item>
        <el-descriptions-item label="理由">{{ current.reason }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getAdoptPage, auditAdopt } from '@/api/adopt'

// 响应式状态
const list = ref([])           // 申请列表
const total = ref(0)           // 总条数（分页用）
const loading = ref(false)     // 表格加载中
const detailVisible = ref(false) // 详情弹窗开关
const current = ref(null)      // 当前查看详情的申请

// 查询条件：状态 + 关键词 + 分页
const query = reactive({ status: '', keyword: '', pageNum: 1, pageSize: 10 })

// 状态 -> 标签颜色映射
const statusType = (s) => (s === '已通过' ? 'success' : s === '已拒绝' ? 'danger' : 'warning')

// 加载申请列表（分页 + 条件筛选）
const load = async () => {
  loading.value = true
  try {
    const res = await getAdoptPage({ status: query.status || undefined, keyword: query.keyword || undefined, pageNum: query.pageNum, pageSize: query.pageSize })
    list.value = (res.data && res.data.list) || []
    total.value = (res.data && res.data.total) || 0
  } finally {
    loading.value = false
  }
}

const onSearch = () => { query.pageNum = 1; load() }
const onPageChange = (p) => { query.pageNum = p; load() }

// 审核核心流程：先弹窗要备注 -> 拿到备注后调用审核接口 -> 刷新列表
const audit = async (row, status) => {
  const remark = await promptRemark(status)
  if (remark === null) return      // 用户取消弹窗则中止
  await auditAdopt(row.id, status, remark) // 调用审核接口（通过/拒绝 + 备注）
  ElMessage.success('审核完成')
  load()
}

// 根据审核结果弹出不同的备注输入框：通过给默认祝福语，拒绝要填原因
const promptRemark = async (status) => {
  if (status === '已通过') {
    const { value } = await ElMessageBox.prompt('请输入通过备注（选填）', '审核通过', { inputValue: '恭喜，审核通过' })
    return value
  }
  const { value } = await ElMessageBox.prompt('请输入拒绝原因', '审核拒绝', { inputValue: '很抱歉，您暂不符合领养条件' })
  return value
}

// 查看详情：把当前行存到 current 并打开弹窗
const viewDetail = (row) => { current.value = row; detailVisible.value = true }

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
