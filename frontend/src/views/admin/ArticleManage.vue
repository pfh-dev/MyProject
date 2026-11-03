<!--
  资讯管理页（后台）：文章（公告/科普/资讯）的增删改查，支持类型筛选和标题搜索。
  内容字段为富文本/文本，新增/编辑共用一个弹窗。
-->
<template>
  <div>
    <!-- 筛选栏：类型 + 标题关键词 + 新增按钮 -->
    <div class="toolbar">
      <el-select v-model="query.type" placeholder="类型" clearable class="w-140" @change="onSearch">
        <el-option label="公告" value="公告" />
        <el-option label="科普" value="科普" />
        <el-option label="资讯" value="资讯" />
      </el-select>
      <el-input v-model="query.keyword" placeholder="搜索标题" clearable class="w-200" :prefix-icon="Search" @keyup.enter="onSearch" />
      <el-button type="primary" @click="onSearch">查询</el-button>
      <div class="spacer" />
      <el-button type="primary" :icon="Plus" @click="openAdd">新增资讯</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="type" label="类型" width="90">
        <template #default="{ row }"><el-tag :type="typeTag(row.type)" size="small">{{ row.type }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="author" label="作者" width="100" />
      <el-table-column prop="viewCount" label="阅读量" width="90" />
      <el-table-column prop="createTime" label="发布时间" width="170" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination background layout="prev, pager, next, total" :total="total" :page-size="query.pageSize" :current-page="query.pageNum" @current-change="onPageChange" />
    </div>

    <!-- 新增/编辑弹窗：标题、类型、封面、作者、内容 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑资讯' : '新增资讯'" width="640px">
      <el-form :model="form" label-width="70px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="公告" value="公告" />
            <el-option label="科普" value="科普" />
            <el-option label="资讯" value="资讯" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面"><el-input v-model="form.cover" placeholder="图片 URL（选填）" /></el-form-item>
        <el-form-item label="作者"><el-input v-model="form.author" placeholder="选填" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="8" /></el-form-item>
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
import { getArticleList, addArticle, updateArticle, deleteArticle } from '@/api/article'

// 响应式状态
const list = ref([])             // 文章列表
const total = ref(0)             // 总条数
const loading = ref(false)       // 表格加载中
const dialogVisible = ref(false) // 弹窗开关
const saving = ref(false)        // 保存中

// 查询条件 与 表单数据
const query = reactive({ type: '', keyword: '', pageNum: 1, pageSize: 10 })
const form = reactive({ id: null, title: '', cover: '', content: '', type: '资讯', author: '' })

// 类型 -> 标签颜色
const typeTag = (t) => (t === '公告' ? 'danger' : t === '科普' ? 'success' : 'primary')

// 加载文章列表
const load = async () => {
  loading.value = true
  try {
    const res = await getArticleList({ type: query.type || undefined, keyword: query.keyword || undefined, pageNum: query.pageNum, pageSize: query.pageSize })
    list.value = (res.data && res.data.list) || []
    total.value = (res.data && res.data.total) || 0
  } finally {
    loading.value = false
  }
}

const onSearch = () => { query.pageNum = 1; load() }
const onPageChange = (p) => { query.pageNum = p; load() }

// 新增：重置表单并打开弹窗
const openAdd = () => {
  Object.assign(form, { id: null, title: '', cover: '', content: '', type: '资讯', author: '' })
  dialogVisible.value = true
}

// 编辑：当前行数据复制进表单
const openEdit = (row) => {
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

// 保存：有 id 更新，无 id 新增（注意此页用 updateArticle(form) 整对象提交）
const save = async () => {
  saving.value = true
  try {
    if (form.id) await updateArticle(form)
    else await addArticle(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

// 删除：二次确认
const remove = async (row) => {
  await ElMessageBox.confirm('确认删除该资讯？', '提示', { type: 'warning' })
  await deleteArticle(row.id)
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
  .spacer { flex: 1; }
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
