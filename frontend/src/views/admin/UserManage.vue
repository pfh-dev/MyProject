<!--
  用户管理页（后台）：查看用户列表，支持搜索/角色筛选，以及禁用/启用、改角色、删除操作。
-->
<template>
  <div>
    <!-- 筛选栏：关键词搜索 + 角色筛选 -->
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索用户名/昵称" clearable class="w-200" :prefix-icon="Search" @keyup.enter="onSearch" />
      <el-select v-model="query.role" placeholder="角色" clearable class="w-140" @change="onSearch">
        <el-option label="管理员" value="ADMIN" />
        <el-option label="员工" value="STAFF" />
        <el-option label="志愿者" value="VOLUNTEER" />
        <el-option label="普通用户" value="USER" />
      </el-select>
      <el-button type="primary" @click="onSearch">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column prop="email" label="邮箱" min-width="140" show-overflow-tooltip />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }"><el-tag :type="roleType(row.role)" size="small">{{ roleLabel(row.role) }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }"><el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">{{ row.status === 0 ? '正常' : '禁用' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="170" />
      <!-- 操作列：禁用/启用（状态互转）、改角色、删除 -->
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" :type="row.status === 0 ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 0 ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" @click="changeRole(row)">改角色</el-button>
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
import { getUserPage, updateUserStatus, updateUserRole, deleteUser } from '@/api/user'

// 响应式状态：list 用户列表、total 总条数、loading 加载中
const list = ref([])
const total = ref(0)
const loading = ref(false)

// 查询条件：关键词 + 角色 + 分页
const query = reactive({ keyword: '', role: '', pageNum: 1, pageSize: 10 })

// 角色代码 -> 中文名 / 标签颜色 的映射
const roleLabel = (r) => ({ ADMIN: '管理员', STAFF: '员工', VOLUNTEER: '志愿者', USER: '普通用户' }[r] || r)
const roleType = (r) => ({ ADMIN: 'danger', STAFF: 'warning', VOLUNTEER: 'success', USER: 'info' }[r] || 'info')

// 加载用户列表
const load = async () => {
  loading.value = true
  try {
    const res = await getUserPage({ keyword: query.keyword || undefined, role: query.role || undefined, pageNum: query.pageNum, pageSize: query.pageSize })
    list.value = (res.data && res.data.list) || []
    total.value = (res.data && res.data.total) || 0
  } finally {
    loading.value = false
  }
}

const onSearch = () => { query.pageNum = 1; load() }
const onPageChange = (p) => { query.pageNum = p; load() }

// 切换用户状态：当前 0（正常）则禁用为 1，反之启用为 0
const toggleStatus = async (row) => {
  await updateUserStatus(row.id, row.status === 0 ? 1 : 0)
  ElMessage.success('操作成功')
  load()
}

// 修改角色：弹窗输入角色代码
const changeRole = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入角色（ADMIN/STAFF/VOLUNTEER/USER）', '修改角色', { inputValue: row.role })
  await updateUserRole(row.id, value)
  ElMessage.success('角色已更新')
  load()
}

// 删除用户：二次确认
const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除用户「${row.username}」？`, '提示', { type: 'warning' })
  await deleteUser(row.id)
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
