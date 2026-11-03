<!--
  首页组件：平台门户页面，展示品牌主视觉、宠物分类入口、最新待领养宠物列表和平台数据亮点。
  页面加载时会并行请求「分类列表」和「待领养宠物列表」两个接口，渲染到对应区块。
-->
<template>
  <div class="home">
    <!-- 顶部主视觉区：品牌标语 + 两个主按钮（去看宠物 / 上报救助） -->
    <section class="hero">
      <div class="page-container hero-inner">
        <div class="hero-text reveal">
          <span class="eyebrow">🐾 公益领养 · 让爱有家</span>
          <h1>给流浪的 TA<br /><em>一个温暖的家</em></h1>
          <p>宠爱之家致力于宠物领养与救助，汇聚每一份善意，让每一只毛孩子都被温柔以待。</p>
          <div class="hero-actions">
            <el-button type="primary" size="large" round @click="$router.push('/pet')">去看看毛孩子</el-button>
            <el-button size="large" round class="ghost-btn" @click="$router.push('/rescue')">我要上报救助</el-button>
          </div>
        </div>
        <div class="hero-art reveal" style="animation-delay: 0.12s">
          <div class="blob blob-teal"></div>
          <div class="blob blob-orange"></div>
          <div class="paw-disc">
            <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
              <ellipse cx="12" cy="15" rx="5.4" ry="4.4" />
              <ellipse cx="4.6" cy="8.6" rx="2.3" ry="3" />
              <ellipse cx="9.7" cy="5.9" rx="2.3" ry="3" />
              <ellipse cx="14.3" cy="5.9" rx="2.3" ry="3" />
              <ellipse cx="19.4" cy="8.6" rx="2.3" ry="3" />
            </svg>
          </div>
          <div class="chip chip-1">🐶 忠诚伙伴</div>
          <div class="chip chip-2">🐱 温柔陪伴</div>
          <div class="chip chip-3">🐰 治愈时光</div>
        </div>
      </div>
    </section>

    <!-- 分类入口：循环渲染分类卡片，点击后跳转到宠物列表页并带上 categoryId 查询参数 -->
    <section class="page-container section">
      <div class="section-head">
        <div class="title"><h2>宠物分类</h2><span class="dot"></span></div>
        <router-link to="/pet" class="more">查看全部 →</router-link>
      </div>
      <div class="category-grid">
        <div
          v-for="c in categories"
          :key="c.id"
          class="category-item pet-card-hover"
          @click="$router.push({ path: '/pet', query: { categoryId: c.id } })"
        >
          <span class="cat-icon">{{ c.icon || '🐾' }}</span>
          <span class="cat-name">{{ c.name }}</span>
        </div>
      </div>
    </section>

    <!-- 最新待领养：展示最多 8 只宠物（复用 PetCard 卡片组件），空数据时显示占位图 -->
    <section class="page-container section">
      <div class="section-head">
        <div class="title"><h2>最新待领养</h2><span class="dot"></span></div>
        <router-link to="/pet" class="more">更多宠物 →</router-link>
      </div>
      <div v-if="pets.length" class="pet-grid">
        <PetCard v-for="p in pets" :key="p.id" :pet="p" />
      </div>
      <el-empty v-else description="暂时没有待领养的宠物" />
    </section>

    <!-- 数据亮点：静态展示平台累计成果数字（当前为写死的展示文案） -->
    <section class="highlight-band">
      <div class="page-container highlight-grid">
        <div class="highlight-item">
          <div class="num">1000+</div>
          <div class="label">已救助宠物</div>
        </div>
        <div class="highlight-item">
          <div class="num">500+</div>
          <div class="label">成功领养</div>
        </div>
        <div class="highlight-item">
          <div class="num">300+</div>
          <div class="label">爱心志愿者</div>
        </div>
        <div class="highlight-item">
          <div class="num">7×24</div>
          <div class="label">AI 咨询陪伴</div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import PetCard from '@/components/PetCard.vue'
import { getCategories, getPetList } from '@/api/pet'

// 响应式状态：categories 存放分类列表，pets 存放最新待领养宠物列表
const categories = ref([])
const pets = ref([])

// 组件挂载后执行：并行请求两个接口，比串行更快
onMounted(async () => {
  try {
    // Promise.all 同时请求「分类」和「宠物列表」两个接口
    const [c, p] = await Promise.all([
      getCategories(),                       // 获取宠物分类
      getPetList({ pageNum: 1, pageSize: 8 }) // 获取前 8 条待领养宠物
    ])
    // 把接口返回的数据填入响应式变量，触发模板渲染
    categories.value = c.data || []
    pets.value = (p.data && p.data.list) || []
  } catch (e) { /* ignore */ } // 首页数据失败时静默处理，不影响页面展示
})
</script>

<style scoped lang="scss">
.hero {
  position: relative;
  overflow: hidden;
  padding: 72px 0 80px;
  background:
    radial-gradient(720px 360px at 85% 10%, rgba(15, 143, 126, 0.10), transparent 60%),
    radial-gradient(560px 360px at 8% 90%, rgba(242, 177, 52, 0.10), transparent 55%),
    linear-gradient(180deg, #fff6ec 0%, var(--bg) 100%);
}

.hero-inner {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  align-items: center;
  gap: 40px;
}

.hero-text {
  .eyebrow {
    display: inline-block;
    padding: 7px 16px;
    border-radius: 999px;
    background: var(--primary-soft);
    color: var(--primary-deep);
    font-size: 13px;
    font-weight: 600;
    margin-bottom: 20px;
  }

  h1 {
    font-size: 52px;
    line-height: 1.22;
    color: var(--text);
    margin-bottom: 18px;
    font-weight: 700;

    em {
      font-style: normal;
      color: var(--primary);
    }
  }

  p {
    font-size: 16px;
    line-height: 1.8;
    color: var(--text-light);
    max-width: 440px;
    margin-bottom: 30px;
  }
}

.hero-actions {
  display: flex;
  gap: 16px;
}

.ghost-btn {
  background: transparent;
  border-color: var(--primary);
  color: var(--primary);

  &:hover {
    background: var(--primary-soft);
  }
}

// 右侧插画：层叠几何 + 爪印 + 悬浮标签
.hero-art {
  position: relative;
  height: 340px;

  .blob {
    position: absolute;
    border-radius: 50%;
    filter: blur(2px);
  }

  .blob-teal {
    width: 220px;
    height: 220px;
    left: 6%;
    top: 8%;
    background: radial-gradient(circle at 30% 30%, #4bc4b2, var(--secondary));
    opacity: 0.9;
  }

  .blob-orange {
    width: 150px;
    height: 150px;
    right: 8%;
    bottom: 6%;
    background: radial-gradient(circle at 30% 30%, #ffb37a, var(--primary));
    opacity: 0.9;
  }

  .paw-disc {
    position: absolute;
    width: 240px;
    height: 240px;
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);
    border-radius: 50%;
    background: var(--surface);
    box-shadow: var(--shadow-lg);
    display: grid;
    place-items: center;

    svg {
      width: 96px;
      height: 96px;
      color: var(--primary);
    }
  }

  .chip {
    position: absolute;
    padding: 8px 14px;
    border-radius: 999px;
    background: var(--surface);
    box-shadow: var(--shadow);
    font-size: 13px;
    font-weight: 600;
    color: var(--text);
    white-space: nowrap;
    animation: float 5s ease-in-out infinite;
  }

  .chip-1 { left: 0; top: 14%; animation-delay: 0s; }
  .chip-2 { right: 0; top: 40%; animation-delay: 1.2s; }
  .chip-3 { left: 12%; bottom: 4%; animation-delay: 2.1s; }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.section {
  margin-top: 56px;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
}

.category-item {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 26px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  cursor: pointer;

  .cat-icon {
    width: 58px;
    height: 58px;
    display: grid;
    place-items: center;
    font-size: 30px;
    border-radius: 18px;
    background: linear-gradient(135deg, var(--primary-soft), #fff3e8);
  }

  .cat-name {
    font-size: 15px;
    font-weight: 600;
    color: var(--text);
  }
}

.pet-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.highlight-band {
  margin-top: 64px;
  padding: 48px 0;
  background: linear-gradient(120deg, #2f2a26 0%, #3d322b 100%);
}

.highlight-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.highlight-item {
  text-align: center;

  .num {
    font-family: var(--font-display);
    font-size: 38px;
    font-weight: 700;
    color: #ff8a5c;
  }

  .label {
    font-size: 14px;
    color: #b8ab9f;
    margin-top: 6px;
  }
}
</style>
