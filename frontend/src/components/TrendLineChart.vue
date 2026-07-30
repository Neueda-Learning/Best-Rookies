<!-- 此文件是趋势折线图组件，用于展示投资组合投入金额的时间变化。 -->
<template>
  <div class="trend-chart">
    <svg viewBox="0 0 560 300" class="trend-chart__canvas" preserveAspectRatio="none" :aria-label="t('trendAriaLabel')">
      <defs>
        <linearGradient :id="areaGradientId" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="var(--chart-accent)" stop-opacity="0.32" />
          <stop offset="100%" stop-color="var(--chart-accent)" stop-opacity="0.02" />
        </linearGradient>
      </defs>

      <g v-for="tick in yTicks" :key="tick.id">
        <line :x1="chartFrame.left" :y1="tick.y" :x2="chartFrame.right" :y2="tick.y" class="trend-chart__grid-line" />
        <text :x="chartFrame.left - 10" :y="tick.y + 4" text-anchor="end" class="trend-chart__tick-label">
          {{ tick.label }}
        </text>
      </g>

      <line :x1="chartFrame.left" :y1="chartFrame.bottom" :x2="chartFrame.right" :y2="chartFrame.bottom" class="trend-chart__axis-line" />
      <line :x1="chartFrame.left" :y1="chartFrame.top" :x2="chartFrame.left" :y2="chartFrame.bottom" class="trend-chart__axis-line" />

      <g v-for="tick in xTicks" :key="tick.id">
        <line :x1="tick.x" :y1="chartFrame.bottom" :x2="tick.x" :y2="chartFrame.bottom + 6" class="trend-chart__axis-line" />
        <text :x="tick.x" :y="chartFrame.bottom + 22" text-anchor="middle" class="trend-chart__tick-label">
          {{ tick.label }}
        </text>
      </g>

      <path :d="areaPath" :fill="`url(#${areaGradientId})`" />
      <polyline :points="polylinePoints" fill="none" stroke="var(--chart-accent)" stroke-width="4" stroke-linecap="round" stroke-linejoin="round" />

      <g v-for="point in chartPoints" :key="point.id">
        <circle :cx="point.x" :cy="point.y" r="6" fill="var(--chart-point-fill)" stroke="var(--chart-accent)" stroke-width="3" />
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
import { getLocale, useI18n } from '../composables/useI18n'
import { formatCurrency } from '../utils/format'

const { t } = useI18n()
const chartFrame = {
  left: 72,
  right: 516,
  top: 28,
  bottom: 220
}
const chartWidth = chartFrame.right - chartFrame.left
const chartHeight = chartFrame.bottom - chartFrame.top

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

function formatAxisCurrency(value) {
  const locale = getLocale() === 'zh' ? 'zh-CN' : 'en-GB'

  try {
    return new Intl.NumberFormat(locale, {
      style: 'currency',
      currency: props.currency,
      notation: 'compact',
      maximumFractionDigits: 1
    }).format(Number(value || 0))
  } catch {
    return formatCurrency(value, props.currency)
  }
}

const maxValue = computed(() => {
  const values = props.points.map((point) => Number(point.value || 0))
  return Math.max(...values, 0)
})

// Convert the raw series into SVG coordinates once so the template stays declarative.
const chartPoints = computed(() => {
  const span = maxValue.value || 1

  return props.points.map((point, index) => {
    const x = chartFrame.left + (props.points.length === 1 ? chartWidth / 2 : (index / (props.points.length - 1)) * chartWidth)
    const y = chartFrame.top + chartHeight - (Number(point.value || 0) / span) * chartHeight

    return {
      ...point,
      x,
      y
    }
  })
})

const polylinePoints = computed(() => chartPoints.value.map((point) => `${point.x},${point.y}`).join(' '))

const yTicks = computed(() => {
  const steps = 4

  return Array.from({ length: steps + 1 }, (_, index) => {
    const ratio = index / steps
    const value = maxValue.value * (1 - ratio)

    return {
      id: `y-${index}`,
      y: chartFrame.top + chartHeight * ratio,
      label: formatAxisCurrency(value)
    }
  })
})

const xTicks = computed(() => {
  const ticks = props.points.map((point, index) => ({
    id: point.id ?? index,
    label: point.shortLabel,
    x: chartPoints.value[index]?.x ?? chartFrame.left
  }))

  if (ticks.length <= 4) {
    return ticks
  }

  const middleIndex = Math.floor((ticks.length - 1) / 2)
  return [ticks[0], ticks[middleIndex], ticks[ticks.length - 1]]
    .filter((tick, index, list) => list.findIndex((item) => item.id === tick.id) === index)
})

const areaPath = computed(() => {
  if (!chartPoints.value.length) {
    return ''
  }

  const firstPoint = chartPoints.value[0]
  const lastPoint = chartPoints.value[chartPoints.value.length - 1]
  const path = chartPoints.value.map((point, index) => `${index === 0 ? 'M' : 'L'} ${point.x} ${point.y}`).join(' ')

  return `${path} L ${lastPoint.x} ${chartFrame.bottom} L ${firstPoint.x} ${chartFrame.bottom} Z`
})

const latestValue = computed(() => Number(props.points[props.points.length - 1]?.value || 0))
const firstLabel = computed(() => props.points[0]?.shortLabel || t('trendStart'))
const lastLabel = computed(() => props.points[props.points.length - 1]?.shortLabel || t('trendCurrent'))
</script>