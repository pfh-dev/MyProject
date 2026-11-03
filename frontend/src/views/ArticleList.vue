<!--
  资讯科普列表页：按类型（公告/科普/资讯）筛选 + 标题关键词搜索，分页展示文章卡片。
  点击卡片跳转到文章详情页。
-->
<template>
  <div class="page-container article-page">
    <div class="page-header">
      <h2>资讯科普</h2>
      <p>了解宠物知识，科学养宠</p>
    </div>

    <!-- 筛选栏：类型单选组 + 标题搜索框 -->
    <div class="filter-bar">
      <el-radio-group v-model="query.type" @change="onSearch">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="公告">公告</el-radio-button>
        <el-radio-button label="科普">科普</el-radio-button>
        <el-radio-button label="资讯">资讯</el-radio-button>
      </el-radio-group>
      <el-input
        v-model="query.keyword"
        placeholder="搜索标题"
        clearable
        class="search-input"
        :prefix-icon="Search"
        @keyup.enter="onSearch"
      />
    </div>

    <!-- 文章卡片网格，点击跳转详情页 -->
    <div v-if="articles.length" class="article-grid">
      <div v-for="a in articles" :key="a.id" class="article-card pet-card-hover" @click="$router.push(`/article/${a.id}`)">
        <div class="cover">
          <img v-if="a.cover" :src="a.cover" :alt="a.title" />
          <div v-else class="cover-placeholder">📰</div>
        </div>
        <div class="info">
          <div class="top">
            <el-tag size="small" :type="typeTag(a.type)">{{ a.type }}</el-tag>
            <span class="date">{{ a.createTime }}</span>
          </div>
          <h3 class="title">{{ a.title }}</h3>
          <div class="meta">
            <span>作者：{{ a.author || '管理员' }}</span>
            <span>阅读 {{ a.viewCount || 0 }}</span>
          </div>
        </div>
      </div>
    </div>
    <el-empty v-else description="暂无资讯" />

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
import { Search } from '@element-plus/icons-vue'
import { getArticleList } from '@/api/article'

// 响应式状态：articles 文章列表、total 总条数
const articles = ref([])
const total = ref(0)
// 查询条件：类型 + 关键词 + 分页（每页 9 条，3 列布局）
const query = reactive({ type: '', keyword: '', pageNum: 1, pageSize: 9 })

// 文章类型 -> 标签颜色
const typeTag = (t) => (t === '公告' ? 'danger' : t === '科普' ? 'success' : 'primary')

// 加载文章列表
const load = async () => {
  const res = await getArticleList({
    type: query.type || undefined,
    keyword: query.keyword || undefined,
    pageNum: query.pageNum,
    pageSize: query.pageSize
  })
  articles.value = (res.data && res.data.list) || []
  total.value = (res.data && res.data.total) || 0
}

// 搜索：重置页码后重新加载
const onSearch = () => {
  query.pageNum = 1
  load()
}

// 翻页
const onPageChange = (p) => {
  query.pageNum = p
  load()
}

onMounted(load)
</script>

<style scoped lang="scss">
.article-page {
  padding-top: 32px;
  padding-bottom: 40px;
}

.page-header {
  margin-bottom: 24px;

  h2 {
    font-size: 28px;
  }

  p {
    color: var(--text-light);
    margin-top: 6px;
  }
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  .search-input {
    width: 240px;
  }
}

.article-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.article-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .cover {
    height: 160px;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .cover-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 48px;
      background: linear-gradient(135deg, #ffe2d5, #fff1ea);
    }
  }

  .info {
    padding: 16px;

    .top {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 10px;

      .date {
        font-size: 12px;
        color: var(--text-light);
      }
    }

    .title {
      font-size: 16px;
      line-height: 1.5;
      margin-bottom: 12px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .meta {
      display: flex;
      gap: 16px;
      font-size: 12px;
      color: var(--text-light);
    }
  }
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>
