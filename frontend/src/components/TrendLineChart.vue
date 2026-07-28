<template>
  <div class="trend-chart">
    <svg viewBox="0 0 520 260" class="trend-chart__canvas" preserveAspectRatio="none" :aria-label="t('trendAriaLabel')">
      <defs>
        <linearGradient :id="areaGradientId" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#1e6a52" stop-opacity="0.32" />
          <stop offset="100%" stop-color="#1e6a52" stop-opacity="0.02" />
        </linearGradient>
      </defs>

      <line x1="24" y1="220" x2="496" y2="220" stroke="rgba(29, 26, 19, 0.12)" stroke-width="1" />
      <line x1="24" y1="32" x2="24" y2="220" stroke="rgba(29, 26, 19, 0.12)" stroke-width="1" />

      <path :d="areaPath" :fill="`url(#${areaGradientId})`" />
      <polyline :points="polylinePoints" fill="none" stroke="#1e6a52" stroke-width="4" stroke-linecap="round" stroke-linejoin="round" />

      <g v-for="point in chartPoints" :key="point.id">
        <circle :cx="point.x" :cy="point.y" r="6" fill="#fff8ed" stroke="#1e6a52" stroke-width="3" />
      </g>
    </svg>

    <div class="trend-chart__footer">
      <div>
        <p class="eyebrow">{{ t('trendCurrentInvestedCost') }}</p>
        <p class="trend-chart__value">{{ formatCurrency(latestValue, currency) }}</p>
      </div>

      <div>
        <p>{{ firstLabel }}</p>
        <p>{{ lastLabel }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from '../composables/useI18n'
import { formatCurrency } from '../utils/format'

const { t } = useI18n()

const props = defineProps({
  points: {
    type: Array,
    required: true
  },
  currency: {
    type: String,
    default: 'USD'
  }
})

const areaGradientId = `trend-area-${Math.random().toString(36).slice(2, 8)}`

// Convert the raw series into SVG coordinates once so the template stays declarative.
const chartPoints = computed(() => {
  const width = 472
  const height = 188
  const left = 24
  const top = 32
  const values = props.points.map((point) => Number(point.value || 0))
  const minValue = Math.min(...values)
  const maxValue = Math.max(...values)
  const span = maxValue - minValue || 1

  return props.points.map((point, index) => {
    const x = left + (props.points.length === 1 ? width / 2 : (index / (props.points.length - 1)) * width)
    const y = top + height - ((Number(point.value || 0) - minValue) / span) * height

    return {
      ...point,
      x,
      y
    }
  })
})

const polylinePoints = computed(() => chartPoints.value.map((point) => `${point.x},${point.y}`).join(' '))

const areaPath = computed(() => {
  if (!chartPoints.value.length) {
    return ''
  }

  const firstPoint = chartPoints.value[0]
  const lastPoint = chartPoints.value[chartPoints.value.length - 1]
  const path = chartPoints.value.map((point, index) => `${index === 0 ? 'M' : 'L'} ${point.x} ${point.y}`).join(' ')

  return `${path} L ${lastPoint.x} 220 L ${firstPoint.x} 220 Z`
})

const latestValue = computed(() => Number(props.points[props.points.length - 1]?.value || 0))
const firstLabel = computed(() => props.points[0]?.shortLabel || t('trendStart'))
const lastLabel = computed(() => props.points[props.points.length - 1]?.shortLabel || t('trendCurrent'))
</script>