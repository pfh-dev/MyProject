<!--
  宠物管理页（后台）：宠物档案的增删改查，以及「审核（通过/驳回）」「修改领养状态」等管理操作。
  查询支持名称/分类/状态/审核状态四个条件，新增/编辑共用一个弹窗表单。
-->
<template>
  <div>
    <!-- 筛选栏：名称 + 分类 + 状态 + 审核状态 + 新增按钮 -->
    <div class="toolbar">
      <el-input v-model="query.name" placeholder="宠物名称" clearable class="w-180" :prefix-icon="Search" @keyup.enter="onSearch" />
      <el-select v-model="query.categoryId" placeholder="分类" clearable class="w-140" @change="onSearch">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable class="w-140" @change="onSearch">
        <el-option label="待领养" value="待领养" />
        <el-option label="领养中" value="领养中" />
        <el-option label="已领养" value="已领养" />
      </el-select>
      <el-select v-model="query.reviewStatus" placeholder="审核状态" clearable class="w-140" @change="onSearch">
        <el-option label="待审核" value="待审核" />
        <el-option label="已通过" value="已通过" />
        <el-option label="已驳回" value="已驳回" />
      </el-select>
      <el-button type="primary" @click="onSearch">查询</el-button>
      <div class="spacer" />
      <el-button type="primary" :icon="Plus" @click="openAdd">新增宠物</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column label="封面" width="80">
        <template #default="{ row }">
          <el-image v-if="row.coverImg" :src="row.coverImg" fit="cover" style="width: 48px; height: 48px; border-radius: 6px" />
          <div v-else class="cover-empty">🐾</div>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" min-width="100" />
      <el-table-column prop="categoryName" label="分类" width="90" />
      <el-table-column prop="breed" label="品种" width="90" />
      <el-table-column prop="gender" label="性别" width="70" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }"><el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="reviewStatus" label="审核" width="90">
        <template #default="{ row }"><el-tag :type="reviewType(row.reviewStatus)" size="small">{{ row.reviewStatus }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createTime" label="登记时间" width="170" />
      <!-- 操作列：编辑/审核（仅待审核显示）/改状态/删除 -->
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="row.reviewStatus === '待审核'" size="small" type="success" @click="review(row, '已通过')">通过</el-button>
          <el-button v-if="row.reviewStatus === '待审核'" size="small" type="warning" @click="review(row, '已驳回')">驳回</el-button>
          <el-button size="small" type="info" @click="changeStatus(row)">改状态</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination background layout="prev, pager, next, total" :total="total" :page-size="query.pageSize" :current-page="query.pageNum" @current-change="onPageChange" />
    </div>

    <!-- 新增/编辑弹窗：共用同一个表单，标题根据 form.id 动态变化 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑宠物' : '新增宠物'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="分类" prop="categoryId">
            <el-select v-model="form.categoryId" style="width: 100%"><el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" /></el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="品种"><el-input v-model="form.breed" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="性别">
            <el-select v-model="form.gender" style="width: 100%"><el-option label="公" value="公" /><el-option label="母" value="母" /></el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="年龄"><el-input v-model="form.age" placeholder="如：1岁" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态">
            <el-select v-model="form.status" style="width: 100%"><el-option label="待领养" value="待领养" /><el-option label="领养中" value="领养中" /><el-option label="已领养" value="已领养" /></el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="疫苗"><el-input v-model="form.vaccine" placeholder="如：已接种" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="绝育"><el-input v-model="form.sterilization" placeholder="如：已绝育" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="封面图"><el-input v-model="form.coverImg" placeholder="图片 URL" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="简介"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="详细介绍"><el-input v-model="form.detail" type="textarea" :rows="4" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getCategories, getPetPage, addPet, updatePet, deletePet, reviewPet, updatePetStatus } from '@/api/pet'

// 响应式状态
const categories = ref([])        // 分类选项（下拉框用）
const list = ref([])              // 宠物列表
const total = ref(0)              // 总条数
const loading = ref(false)        // 表格加载中
const dialogVisible = ref(false)  // 新增/编辑弹窗开关
const saving = ref(false)         // 保存中
const formRef = ref()             // 表单引用

// 查询条件与表单数据（表单字段较多，包含宠物档案各项）
const query = reactive({ name: '', categoryId: null, status: '', reviewStatus: '', pageNum: 1, pageSize: 10 })
const form = reactive({ id: null, name: '', categoryId: null, breed: '', gender: '公', age: '', vaccine: '', sterilization: '', description: '', detail: '', coverImg: '', status: '待领养' })

// 校验规则：名称必填、分类必选
const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

// 领养状态 / 审核状态 到标签颜色的映射
const statusType = (s) => (s === '待领养' ? 'success' : s === '领养中' ? 'warning' : 'info')
const reviewType = (s) => (s === '已通过' ? 'success' : s === '已驳回' ? 'danger' : 'warning')

// 加载宠物列表（分页 + 多条件筛选）
const load = async () => {
  loading.value = true
  try {
    const res = await getPetPage({ ...query, categoryId: query.categoryId || undefined, status: query.status || undefined, reviewStatus: query.reviewStatus || undefined })
    list.value = (res.data && res.data.list) || []
    total.value = (res.data && res.data.total) || 0
  } finally {
    loading.value = false
  }
}

const onSearch = () => { query.pageNum = 1; load() }
const onPageChange = (p) => { query.pageNum = p; load() }

// 新增：把表单重置为默认值后打开弹窗
const openAdd = () => {
  Object.assign(form, { id: null, name: '', categoryId: null, breed: '', gender: '公', age: '', vaccine: '', sterilization: '', description: '', detail: '', coverImg: '', status: '待领养' })
  dialogVisible.value = true
}

// 编辑：把当前行数据复制进表单后打开弹窗
const openEdit = (row) => {
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

// 保存：校验 -> 根据有无 id 决定新增还是更新 -> 刷新列表
const save = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) await updatePet(form.id, form)  // 有 id 走更新
    else await addPet(form)                       // 无 id 走新增
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

// 审核宠物：通过/驳回
const review = async (row, reviewStatus) => {
  await reviewPet(row.id, reviewStatus)
  ElMessage.success('操作成功')
  load()
}

// 修改领养状态：弹窗输入新状态
const changeStatus = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入新状态（待领养/领养中/已领养）', '修改状态', { inputValue: row.status })
  await updatePetStatus(row.id, value)
  ElMessage.success('状态已更新')
  load()
}

// 删除宠物：二次确认后调用删除接口
const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除宠物「${row.name}」？`, '提示', { type: 'warning' })
  await deletePet(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(async () => {
  // 先加载分类选项，再加载列表
  const c = await getCategories()
  categories.value = c.data || []
  load()
})
</script>

<style scoped lang="scss">
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;

  .w-180 { width: 180px; }
  .w-140 { width: 140px; }
  .spacer { flex: 1; }
}

.cover-empty {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  background: #fff1ea;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
