<template>
  <!-- 宠物卡片：点击整张卡片跳到宠物详情页 -->
  <div class="pet-card pet-card-hover" @click="$router.push(`/pet/${pet.id}`)">
    <!-- 封面区：优先显示封面图，无图时显示爪印占位 -->
    <div class="cover">
      <img v-if="pet.coverImg" :src="pet.coverImg" :alt="pet.name" />
      <div v-else class="cover-placeholder">🐾</div>
      <!-- 状态标签：右上角显示领养状态，颜色随状态变化 -->
      <span class="status-tag" :class="statusClass">
        <i class="dot"></i>{{ pet.status || '待领养' }}
      </span>
    </div>
    <!-- 信息区：名称、标签、简介、跳转提示 -->
    <div class="body">
      <div class="name">{{ pet.name }}</div>
      <div class="meta">
        <span class="tag">{{ pet.categoryName || '宠物' }}</span>
        <span v-if="pet.breed" class="tag">{{ pet.breed }}</span>
        <span class="tag">{{ pet.gender || '未知' }}</span>
      </div>
      <div class="desc text-ellipsis">{{ pet.description || '暂无简介' }}</div>
      <div class="more">去看看 <span class="arrow">→</span></div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

// 接收父组件传入的宠物对象
const props = defineProps({
  pet: { type: Object, required: true }
})

// 根据宠物状态返回对应的样式类名，用于给状态标签上色
const statusClass = computed(() => {
  const s = props.pet.status
  if (s === '待领养') return 'available'
  if (s === '领养中') return 'processing'
  if (s === '已领养') return 'adopted'
  return 'available'
})
</script>

<style scoped lang="scss">
/* 卡片整体：圆角白底、带边框与轻阴影、鼠标可点击 */
.pet-card {
  background: var(--surface);
  border-radius: var(--radius);
  overflow: hidden;
  cursor: pointer;
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
}

/* 封面区：固定高度，图片铺满并随悬停轻微放大 */
.cover {
  position: relative;
  height: 200px;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.6s cubic-bezier(0.22, 0.61, 0.36, 1);
  }

  &:hover img {
    transform: scale(1.06);
  }

  /* 无图时的占位背景 */
  .cover-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 56px;
    background: linear-gradient(135deg, var(--primary-soft), #fff3e8);
  }
}

/* 状态标签：固定在封面右上角 */
.status-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  backdrop-filter: blur(4px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.14);

  .dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: currentColor;
  }

  /* 不同状态对应不同背景色 */
  &.available {
    background: rgba(15, 143, 126, 0.92);
  }

  &.processing {
    background: rgba(242, 177, 52, 0.95);
  }

  &.adopted {
    background: rgba(140, 122, 109, 0.92);
  }
}

/* 信息区：名称、标签行、简介、跳转提示 */
.body {
  padding: 16px 18px 18px;

  .name {
    font-family: var(--font-display);
    font-size: 20px;
    font-weight: 700;
    color: var(--text);
    margin-bottom: 8px;
  }

  /* 标签行：分类、品种、性别 */
  .meta {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    margin-bottom: 10px;

    .tag {
      font-size: 12px;
      color: var(--primary);
      background: var(--primary-soft);
      padding: 3px 11px;
      border-radius: 999px;
    }
  }

  .desc {
    font-size: 13px;
    color: var(--text-light);
    margin-bottom: 14px;
  }

  /* 底部「去看看」提示，箭头悬停向右移动 */
  .more {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    font-weight: 600;
    color: var(--primary);

    .arrow {
      transition: transform 0.2s ease;
    }
  }

  &:hover .more .arrow {
    transform: translateX(4px);
  }
}
</style>
