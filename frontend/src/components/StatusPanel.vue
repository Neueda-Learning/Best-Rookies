<!-- 此文件是状态面板组件，用于统一展示加载中、错误和空数据状态。 -->
<template>
  <div class="status-panel" :class="`status-panel--${variant}`">
    <div v-if="variant === 'loading'" class="spinner" aria-hidden="true"></div>
    <div v-else class="status-panel__badge" aria-hidden="true">{{ icon }}</div>

    <div class="status-panel__content">
      <h3>{{ title }}</h3>
      <p>{{ message }}</p>
    </div>

    <button v-if="actionLabel" type="button" class="button button--secondary" @click="$emit('action')">
      {{ actionLabel }}
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  variant: {
    type: String,
    default: 'info'
  },
  title: {
    type: String,
    required: true
  },
  message: {
    type: String,
    required: true
  },
  actionLabel: {
    type: String,
    default: ''
  }
})

defineEmits(['action'])

const icon = computed(() => {
  if (props.variant === 'error') {
    return '!'
  }

  if (props.variant === 'empty') {
    return '0'
  }

  return 'i'
})
</script>