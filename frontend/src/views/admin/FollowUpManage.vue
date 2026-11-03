<!--
  回访管理页（后台）：宠物被领养后，系统会自动生成「待回访计划」，
  本页展示哪些用户领养了什么宠物、领养了多久、需要/已完成回访，
  方便管理员掌握回访进度。
-->
<template>
  <div>
    <!-- 筛选栏：回访状态 + 关键词搜索 -->
    <div class="toolbar">
      <el-select v-model="query.status" placeholder="回访状态" clearable class="w-140" @change="onSearch">
        <el-option label="待回访" value="待回访" />
        <el-option label="已回访" value="已回访" />
      </el-select>
      <el-input v-model="query.keyword" placeholder="搜索领养人/宠物/志愿者" clearable class="w-220" :prefix-icon="Search" @keyup.enter="onSearch" />
      <el-button type="primary" @click="onSearch">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <!-- 宠物：封面缩略图 + 名称 -->
      <el-table-column label="宠物" min-width="140">
        <template #default="{ row }">
          <div class="pet-cell">
            <el-image v-if="row.petCoverImg" :src="row.petCoverImg" fit="cover" class="pet-cover" />
            <span v-else class="pet-cover placeholder">🐾</span>
            <span>{{ row.petName || '-' }}</span>
          </div>
        </template>
      </el-table-column>
      <!-- 领养人 -->
      <el-table-column prop="applicantName" label="领养人" width="110" show-overflow-tooltip />
      <!-- 联系电话 -->
      <el-table-column prop="phone" label="联系电话" width="130" />
      <!-- 领养天数：从审核通过到现在的天数 -->
      <el-table-column label="领养天数" width="110" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.adoptDays != null" :type="daysType(row)" size="small" effect="light">
            {{ row.adoptDays }} 天
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <!-- 领养通过时间 -->
      <el-table-column prop="auditTime" label="领养通过时间" width="170" />
      <!-- 回访志愿者 -->
      <el-table-column prop="volunteerName" label="回访志愿者" width="110" show-overflow-tooltip>
        <template #default="{ row }">{{ row.volunteerName || '待分配' }}</template>
      </el-table-column>
      <!-- 回访状态：content 为空是「待回访」计划，非空是「已回访」记录 -->
      <el-table-column label="回访状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.content ? 'success' : 'warning'" size="small">{{ row.content ? '已回访' : '待回访' }}</el-tag>
        </template>
      </el-table-column>
      <!-- 回访内容 -->
      <el-table-column prop="content" label="回访内容" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">{{ row.content || '-' }}</template>
      </el-table-column>
      <!-- 操作列 -->
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button v-if="!row.content" size="small" type="primary" @click="openEdit(row)">登记回访</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination background layout="prev, pager, next, total" :total="total" :page-size="query.pageSize" :current-page="query.pageNum" @current-change="onPageChange" />
    </div>

    <!-- 登记回访弹窗：把待回访计划补充成已回访记录 -->
    <el-dialog v-model="editVisible" title="登记回访" width="520px">
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="宠物">
          <el-input :model-value="editForm.petName" disabled />
        </el-form-item>
        <el-form-item label="领养人">
          <el-input :model-value="editForm.applicantName" disabled />
        </el-form-item>
        <el-form-item label="回访内容" required>
          <el-input v-model="editForm.content" type="textarea" :rows="3" placeholder="宠物生活情况、健康状况、与领养人的相处等" />
        </el-form-item>
        <el-form-item label="宠物状况">
          <el-input v-model="editForm.petCondition" placeholder="例如：健康、活泼、状态良好" />
        </el-form-item>
        <el-form-item label="是否正常">
          <el-radio-group v-model="editForm.isNormal">
            <el-radio value="是">是</el-radio>
            <el-radio value="否">否</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getFollowUpPage, deleteFollowUp, updateFollowUp } from '@/api/adopt'

// 响应式状态
const list = ref([])          // 回访记录列表
const total = ref(0)          // 总条数
const loading = ref(false)    // 表格加载中

// 查询条件（status 用于筛选待回访/已回访）
const query = reactive({ status: '', keyword: '', pageNum: 1, pageSize: 10 })

// 登记回访弹窗状态
const editVisible = ref(false)
const saving = ref(false)
const editForm = reactive({ id: null, petName: '', applicantName: '', content: '', petCondition: '', isNormal: '是' })

// 根据领养天数返回标签颜色：待回访且已满 7 天（到计划回访时间）用红色提醒
const daysType = (row) => {
  if (!row.content && row.adoptDays >= 7) return 'danger'
  if (!row.content) return 'warning'
  return 'success'
}

// 加载回访记录列表
const load = async () => {
  loading.value = true
  try {
    const res = await getFollowUpPage({
      status: query.status || undefined,
      keyword: query.keyword || undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize
    })
    list.value = (res.data && res.data.list) || []
    total.value = (res.data && res.data.total) || 0
  } finally {
    loading.value = false
  }
}

const onSearch = () => { query.pageNum = 1; load() }
const onPageChange = (p) => { query.pageNum = p; load() }

// 删除回访记录：二次确认
const remove = async (row) => {
  await ElMessageBox.confirm('确认删除该回访记录？', '提示', { type: 'warning' })
  await deleteFollowUp(row.id)
  ElMessage.success('删除成功')
  load()
}

// 打开登记回访弹窗：带入宠物名、领养人，清空待填字段
const openEdit = (row) => {
  editForm.id = row.id
  editForm.petName = row.petName
  editForm.applicantName = row.applicantName
  editForm.content = ''
  editForm.petCondition = ''
  editForm.isNormal = '是'
  editVisible.value = true
}

// 提交登记回访：填写内容后把计划转成已回访记录
const submitEdit = async () => {
  if (!editForm.content) {
    ElMessage.warning('请填写回访内容')
    return
  }
  saving.value = true
  try {
    await updateFollowUp(editForm.id, {
      content: editForm.content,
      petCondition: editForm.petCondition,
      isNormal: editForm.isNormal
    })
    ElMessage.success('回访登记成功')
    editVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;

  .w-140 { width: 140px; }
  .w-220 { width: 220px; }
}

/* 宠物封面缩略图 + 名称 */
.pet-cell {
  display: flex;
  align-items: center;
  gap: 8px;

  .pet-cover {
    width: 40px;
    height: 40px;
    border-radius: 8px;
    flex-shrink: 0;
  }

  .placeholder {
    display: grid;
    place-items: center;
    font-size: 20px;
    background: var(--primary-soft);
  }
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
