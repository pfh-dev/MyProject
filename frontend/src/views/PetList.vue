<!--
  宠物列表页：支持按名称搜索、按分类筛选，分页展示待领养宠物。
  也承接首页分类入口跳转过来的 categoryId 查询参数。
-->
<template>
  <div class="page-container pet-list-page">
    <div class="page-header">
      <h2>宠物领养</h2>
      <p>找到属于你的毛孩子</p>
    </div>

    <!-- 筛选栏：名称输入框（回车触发搜索）+ 分类下拉框 + 搜索按钮 -->
    <div class="filter-bar">
      <el-input
        v-model="query.name"
        placeholder="搜索宠物名称"
        clearable
        class="search-input"
        :prefix-icon="Search"
        @keyup.enter="onSearch"
      />
      <el-select v-model="query.categoryId" placeholder="全部分类" clearable class="filter-select" @change="onSearch">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-button type="primary" @click="onSearch">搜索</el-button>
    </div>

    <!-- 宠物卡片网格，空结果时显示占位 -->
    <div v-if="pets.length" class="pet-grid">
      <PetCard v-for="p in pets" :key="p.id" :pet="p" />
    </div>
    <el-empty v-else description="没有找到符合条件的宠物" />

    <!-- 分页组件：切换页码时触发 onPageChange 重新加载 -->
    <div class="pagination">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :total="total"
        :page-size="query.pageSize"
        :current-page="query.pageNum"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import PetCard from '@/components/PetCard.vue'
import { getCategories, getPetList } from '@/api/pet'

const route = useRoute()
// 响应式状态：categories 分类选项、pets 当前页宠物、total 总条数（用于分页）
const categories = ref([])
const pets = ref([])
const total = ref(0)

// 查询条件对象：name 名称、categoryId 分类、pageNum 页码、pageSize 每页数量
const query = reactive({
  name: '',
  categoryId: null,
  pageNum: 1,
  pageSize: 8
})

// 加载宠物列表：根据当前 query 条件调用 getPetList 接口
const load = async () => {
  const res = await getPetList({
    name: query.name || undefined,             // 空字符串转 undefined，避免后端收到空值
    categoryId: query.categoryId || undefined,
    pageNum: query.pageNum,
    pageSize: query.pageSize
  })
  pets.value = (res.data && res.data.list) || []
  total.value = (res.data && res.data.total) || 0
}

// 搜索：重置到第一页再加载
const onSearch = () => {
  query.pageNum = 1
  load()
}

// 翻页：记录新页码后重新加载
const onPageChange = (p) => {
  query.pageNum = p
  load()
}

onMounted(async () => {
  // 如果 URL 带 categoryId（来自首页分类入口），则回填到筛选条件
  if (route.query.categoryId) query.categoryId = Number(route.query.categoryId)
  // 先加载分类选项，再加载列表
  const c = await getCategories()
  categories.value = c.data || []
  load()
})
</script>

<style scoped lang="scss">
.pet-list-page {
  padding-top: 40px;
  padding-bottom: 48px;
}

.page-header {
  margin-bottom: 28px;

  h2 {
    font-family: var(--font-display);
    font-size: 34px;
    font-weight: 700;
    color: var(--text);
  }

  p {
    color: var(--text-light);
    margin-top: 8px;
    font-size: 15px;
  }
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 30px;
  padding: 18px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);

  .search-input {
    width: 280px;
  }

  .filter-select {
    width: 180px;
  }
}

.pet-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 36px;
}
</style>
