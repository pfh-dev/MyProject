<!--
  宠物分类管理页（后台）：分类的增删改，字段含名称、图标（emoji）、排序。
-->
<template>
  <div>
    <div class="toolbar">
      <div class="spacer" />
      <el-button type="primary" :icon="Plus" @click="openAdd">新增分类</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="icon" label="图标" width="90">
        <template #default="{ row }"><span class="cat-icon">{{ row.icon || '🐾' }}</span></template>
      </el-table-column>
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column prop="sort" label="排序" width="90" />
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗：名称、图标、排序 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '新增分类'" width="420px">
      <el-form :model="form" label-width="70px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="form.icon" placeholder="如：🐱、🐶（emoji）" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
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
import { Plus } from '@element-plus/icons-vue'
import { getCategories, addCategory, updateCategory, deleteCategory } from '@/api/pet'

// 响应式状态
const list = ref([])             // 分类列表
const loading = ref(false)       // 加载中
const dialogVisible = ref(false) // 弹窗开关
const saving = ref(false)        // 保存中
const form = reactive({ id: null, name: '', icon: '', sort: 0 })

// 加载分类列表（复用 getCategories 接口）
const load = async () => {
  loading.value = true
  try {
    const res = await getCategories()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

// 新增：重置表单并打开弹窗
const openAdd = () => {
  Object.assign(form, { id: null, name: '', icon: '', sort: 0 })
  dialogVisible.value = true
}

// 编辑：当前行复制进表单
const openEdit = (row) => {
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

// 保存：有 id 更新，无 id 新增
const save = async () => {
  saving.value = true
  try {
    if (form.id) await updateCategory(form)
    else await addCategory(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

// 删除：二次确认
const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除分类「${row.name}」？`, '提示', { type: 'warning' })
  await deleteCategory(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<style scoped lang="scss">
.toolbar {
  display: flex;
  margin-bottom: 16px;

  .spacer { flex: 1; }
}

.cat-icon {
  font-size: 24px;
}
</style>
