<!--
  后台数据看板：顶部 6 个统计卡片展示关键指标，下方 3 个 ECharts 图表
  （近 7 天趋势折线图、宠物分类饼图、领养状态饼图）。
-->
<template>
  <div class="dashboard">
    <!-- 顶部统计卡片区：循环渲染 statCards -->
    <div class="stat-grid">
      <div class="stat-card" v-for="s in statCards" :key="s.label">
        <div class="stat-icon" :style="{ background: s.bg }">{{ s.icon }}</div>
        <div class="stat-info">
          <div class="stat-num">{{ s.value }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </div>
      </div>
    </div>

    <!-- 图表区：每个 div 通过 ref 绑定，脚本里用 echarts.init 挂载图表 -->
    <div class="chart-grid">
      <el-card shadow="never" class="chart-card">
        <template #header>近 7 天领养 / 救助趋势</template>
        <div ref="trendRef" class="chart"></div>
      </el-card>
      <el-card shadow="never" class="chart-card">
        <template #header>宠物分类分布</template>
        <div ref="categoryRef" class="chart"></div>
      </el-card>
      <el-card shadow="never" class="chart-card">
        <template #header>领养状态分布</template>
        <div ref="statusRef" class="chart"></div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { getOverview, getTrend, getDistribution } from '@/api/stats'

// 三个图表容器的 DOM 引用，供 echarts.init 使用
const trendRef = ref()
const categoryRef = ref()
const statusRef = ref()

// 顶部统计卡片数据：初始 value 为 0，加载接口后回填
const statCards = ref([
  { label: '宠物总数', value: 0, icon: '🐾', bg: '#fff1ea' },
  { label: '领养申请', value: 0, icon: '📋', bg: '#e6f7ef' },
  { label: '救助线索', value: 0, icon: '🚑', bg: '#fdf3e0' },
  { label: '注册用户', value: 0, icon: '👤', bg: '#eef3ff' },
  { label: '捐赠笔数', value: 0, icon: '❤️', bg: '#fdeaea' },
  { label: '志愿者', value: 0, icon: '🤝', bg: '#f0faf9' }
])

// 渲染近 7 天领养/救助趋势折线图
function renderTrend(data) {
  const chart = echarts.init(trendRef.value)
  const adopt = data.adoptTrend || []    // 领养趋势数组 [{ date, count }]
  const rescue = data.rescueTrend || []  // 救助趋势数组
  // 合并领养和救助的日期，去重排序后作为 x 轴
  const dates = [...new Set([...adopt.map((i) => i.date), ...rescue.map((i) => i.date)])].sort()
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['领养', '救助'] },
    grid: { left: 40, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', minInterval: 1 }, // 整数刻度
    series: [
      // 每个日期按对应 count 取数，缺失日期补 0
      { name: '领养', type: 'line', smooth: true, data: dates.map((d) => Number((adopt.find((i) => i.date === d) || {}).count || 0)) },
      { name: '救助', type: 'line', smooth: true, data: dates.map((d) => Number((rescue.find((i) => i.date === d) || {}).count || 0)) }
    ]
  })
}

// 渲染宠物分类分布饼图（环形）
function renderCategory(data) {
  const chart = echarts.init(categoryRef.value)
  const list = data.petCategory || []
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'], // 内外半径做成环形
      data: list.map((i) => ({ name: i.name, value: Number(i.value) }))
    }]
  })
}

// 渲染领养状态分布饼图（实心）
function renderStatus(data) {
  const chart = echarts.init(statusRef.value)
  const list = data.adoptStatus || []
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: '60%',
      data: list.map((i) => ({ name: i.name, value: Number(i.value) }))
    }]
  })
}

onMounted(async () => {
  try {
    // 并行请求三个统计接口，拿到数据后分别回填
    const [ov, tr, dis] = await Promise.all([getOverview(), getTrend(), getDistribution()])
    const o = ov.data || {}
    statCards.value[0].value = o.petCount || 0
    statCards.value[1].value = o.adoptCount || 0
    statCards.value[2].value = o.rescueCount || 0
    statCards.value[3].value = o.userCount || 0
    statCards.value[4].value = o.donationCount || 0
    statCards.value[5].value = o.volunteerCount || 0
    renderTrend(tr.data || {})
    renderCategory(dis.data || {})
    renderStatus(dis.data || {})
  } catch (e) { /* ignore */ }
})
</script>

<style scoped lang="scss">
.stat-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
  }

  .stat-num {
    font-size: 24px;
    font-weight: 700;
    color: var(--text);
  }

  .stat-label {
    font-size: 13px;
    color: var(--text-light);
  }
}

.chart-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.chart-card {
  border-radius: 12px;

  .chart {
    height: 320px;
  }
}

.chart-card:nth-child(3) {
  grid-column: 1 / -1;

  .chart {
    height: 280px;
  }
}
</style>
