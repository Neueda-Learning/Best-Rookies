<!-- 此文件是指标卡片组件，用于展示单个统计指标的标题、数值和说明。 -->
<template>
  <article class="metric-card" :class="toneClass">
    <div class="metric-card__topline">
      <p class="metric-card__label">{{ label }}</p>
      <span v-if="badge" class="metric-card__badge">{{ badge }}</span>
    </div>
    <p class="metric-card__value">{{ value }}</p>
    <p class="metric-card__hint">{{ hint }}</p>
  </article>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  label: {
    type: String,
    required: true
  },
  value: {
    type: String,
    required: true
  },
  hint: {
    type: String,
    default: ''
  },
  badge: {
    type: String,
    default: ''
  },
  tone: {
    type: String,
    default: 'emerald'
  }
})

const toneClass = computed(() => `metric-card--${props.tone}`)
</script>