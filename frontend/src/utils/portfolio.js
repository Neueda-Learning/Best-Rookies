// 此文件是投资组合数据处理工具模块，用于计算持仓成本并生成图表所需数据。
import { t } from '../composables/useI18n'
import { formatShortDate, toNumber } from './format'

const ASSET_COLORS = {
  STOCK: '#1e6a52',
  BOND: '#db7c26',
  CASH: '#2158c5',
  ETF: '#5646e8',
  FUND: '#14a57d',
  CRYPTO: '#f28a1b',
  OTHER: '#b9412e'
}

export function calculatePositionCost(position) {
  return toNumber(position?.quantity) * toNumber(position?.avgCost)
}

export function formatAssetType(assetType) {
  if (!assetType) {
    return t('assetTypeUnknown')
  }

  if (assetType === 'STOCK') {
    return t('assetTypeStock')
  }

  if (assetType === 'BOND') {
    return t('assetTypeBond')
  }

  if (assetType === 'CASH') {
    return t('assetTypeCash')
  }

  if (assetType === 'ETF') {
    return t('assetTypeEtf')
  }

  if (assetType === 'FUND') {
    return t('assetTypeFund')
  }

  if (assetType === 'CRYPTO') {
    return t('assetTypeCrypto')
  }

  return t('assetTypeOther')
}

export function buildAssetAllocation(positions) {
  const totals = positions.reduce((map, position) => {
    const key = position?.assetType || 'OTHER'
    const nextValue = (map.get(key) || 0) + calculatePositionCost(position)
    map.set(key, nextValue)
    return map
  }, new Map())

  return [...totals.entries()]
    .map(([assetType, value]) => ({
      label: formatAssetType(assetType),
      value,
      color: ASSET_COLORS[assetType] || ASSET_COLORS.OTHER
    }))
    .filter((segment) => segment.value > 0)
    .sort((left, right) => right.value - left.value)
}

export function buildInvestedTrend(positions) {
  const timeline = [...positions]
    .filter((position) => position?.updatedAt)
    .sort((left, right) => new Date(left.updatedAt) - new Date(right.updatedAt))

  if (!timeline.length) {
    const currentTotal = positions.reduce((sum, position) => sum + calculatePositionCost(position), 0)

    return currentTotal > 0
      ? [{ id: 'snapshot', shortLabel: t('trendCurrent'), value: currentTotal }]
      : []
  }

  let runningTotal = 0

  // The backend does not expose live price history yet, so we chart cumulative invested cost as a reliable baseline.
  return timeline.map((position, index) => {
    runningTotal += calculatePositionCost(position)

    return {
      id: position.id ?? index,
      shortLabel: formatShortDate(position.updatedAt),
      value: runningTotal
    }
  })
}