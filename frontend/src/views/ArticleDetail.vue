<!--
  资讯详情页：根据路由参数 id 拉取单篇文章，展示标题、元信息、封面和正文（富文本）。
-->
<template>
  <div class="page-container article-detail" v-if="article">
    <el-page-header @back="$router.back()" class="back-header">
      <template #content><span class="back-title">资讯详情</span></template>
    </el-page-header>
    <!-- 文章正文容器 -->
    <div class="article-body">
      <h1 class="title">{{ article.title }}</h1>
      <div class="meta">
        <el-tag size="small" :type="typeTag">{{ article.type }}</el-tag>
        <span>作者：{{ article.author || '管理员' }}</span>
        <span>{{ article.createTime }}</span>
        <span>阅读 {{ article.viewCount || 0 }}</span>
      </div>
      <img v-if="article.cover" :src="article.cover" class="cover" />
      <!-- v-html 渲染富文本正文（后端返回的 HTML 字符串） -->
      <div class="content" v-html="article.content"></div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getArticleDetail } from '@/api/article'

const route = useRoute()
const article = ref(null) // 文章详情数据

// 根据文章类型映射标签颜色
const typeTag = computed(() => {
  const t = article.value && article.value.type
  return t === '公告' ? 'danger' : t === '科普' ? 'success' : 'primary'
})

onMounted(async () => {
  // 根据路由参数 id 请求详情
  const res = await getArticleDetail(route.params.id)
  article.value = res.data
})
</script>

<style scoped lang="scss">
.article-detail {
  padding-top: 24px;
  padding-bottom: 40px;
}

.back-header {
  margin-bottom: 20px;
}

.article-body {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .title {
    font-size: 28px;
    text-align: center;
    margin-bottom: 16px;
  }

  .meta {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16px;
    color: var(--text-light);
    font-size: 13px;
    padding-bottom: 24px;
    border-bottom: 1px solid var(--border);
  }

  .cover {
    width: 100%;
    max-height: 400px;
    object-fit: cover;
    border-radius: 12px;
    margin: 24px 0;
  }

  .content {
    line-height: 1.9;
    color: #444;
    font-size: 15px;
  }
}
</style>
