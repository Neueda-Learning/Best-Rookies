<template>
  <div class="donut-chart">
    <div class="donut-chart__visual" :style="donutStyle">
      <div class="donut-chart__center">
        <div>
          <span>{{ t('donutTotalInvested') }}</span>
          <strong>{{ formatCurrency(totalValue, currency) }}</strong>
        </div>
      </div>
    </div>

    <div class="donut-chart__legend">
      <article v-for="segment in normalizedSegments" :key="segment.label" class="legend-row">
        <div class="legend-row__label">
          <span class="legend-row__dot" :style="{ background: segment.color }"></span>
          <span>{{ segment.label }}</span>
        </div>

        <div class="legend-row__value">
          <strong>{{ formatCurrency(segment.value, currency) }}</strong>
          <p>{{ segment.percentage.toFixed(1) }}%</p>
        </div>
      </article>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from '../composables/useI18n'
import { formatCurrency } from '../utils/format'

const { t } = useI18n()

const props = defineProps({
  segments: {
    type: Array,
    required: true
  },
  totalValue: {
    type: [Number, String],
    default: 0
  },
  currency: {
    type: String,
    default: 'USD'
  }
})

// Keep the donut geometry and the legend percentages driven by the same normalized source.
const normalizedSegments = computed(() => {
  const total = props.segments.reduce((sum, segment) => sum + Number(segment.value || 0), 0)

  return props.segments.map((segment) => ({
    ...segment,
    percentage: total > 0 ? (Number(segment.value || 0) / total) * 100 : 0
  }))
})

const donutStyle = computed(() => {
  if (!normalizedSegments.value.length) {
    return {
      background: 'rgba(29, 26, 19, 0.08)'
    }
  }

  let cursor = 0
  const stops = normalizedSegments.value.map((segment) => {
    const start = cursor
    cursor += segment.percentage
    return `${segment.color} ${start}% ${cursor}%`
  })

  return {
    background: `conic-gradient(${stops.join(', ')})`
  }
})
</script>