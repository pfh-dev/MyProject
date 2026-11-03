<!--
  轮播图管理页（后台）：首页轮播图的增删改，字段含图片 URL、标题、链接、排序、启用状态。
-->
<template>
  <div>
    <div class="toolbar">
      <div class="spacer" />
      <el-button type="primary" :icon="Plus" @click="openAdd">新增轮播</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column label="图片" width="140">
        <template #default="{ row }">
          <el-image v-if="row.img" :src="row.img" fit="cover" style="width: 120px; height: 60px; border-radius: 6px" />
          <div v-else class="img-empty">🖼️</div>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="140" />
      <el-table-column prop="link" label="链接" min-width="140" show-overflow-tooltip />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗：状态用 el-switch，1 启用 0 停用 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑轮播' : '新增轮播'" width="480px">
      <el-form :model="form" label-width="70px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="图片"><el-input v-model="form.img" placeholder="图片 URL" /></el-form-item>
        <el-form-item label="链接"><el-input v-model="form.link" placeholder="跳转链接（选填）" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" />
        </el-form-item>
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
import { getBannerList, addBanner, updateBanner, deleteBanner } from '@/api/banner'

// 响应式状态
const list = ref([])             // 轮播列表
const loading = ref(false)       // 加载中
const dialogVisible = ref(false) // 弹窗开关
const saving = ref(false)        // 保存中
const form = reactive({ id: null, title: '', img: '', link: '', sort: 0, status: 1 })

// 加载轮播列表（无分页，一次性返回）
const load = async () => {
  loading.value = true
  try {
    const res = await getBannerList()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

// 新增：重置表单并打开弹窗
const openAdd = () => {
  Object.assign(form, { id: null, title: '', img: '', link: '', sort: 0, status: 1 })
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
    if (form.id) await updateBanner(form)
    else await addBanner(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

// 删除：二次确认
const remove = async (row) => {
  await ElMessageBox.confirm('确认删除该轮播？', '提示', { type: 'warning' })
  await deleteBanner(row.id)
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

.img-empty {
  width: 120px;
  height: 60px;
  border-radius: 6px;
  background: #f5f6f8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}
</style>
