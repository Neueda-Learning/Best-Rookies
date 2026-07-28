// 此文件是格式化工具模块，用于处理数字、货币和日期时间的展示格式。
import { getLocale, t } from '../composables/useI18n'

export function toNumber(value) {
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : 0
}

function getIntlLocale() {
  return getLocale() === 'zh' ? 'zh-CN' : 'en-GB'
}

export function formatCurrency(value, currency = 'USD') {
  const safeValue = toNumber(value)

  try {
    return new Intl.NumberFormat(getIntlLocale(), {
      style: 'currency',
      currency,
      maximumFractionDigits: 2
    }).format(safeValue)
  } catch {
    return `${safeValue.toFixed(2)} ${currency}`
  }
}

export function formatNumber(value, fractionDigits = 2) {
  return new Intl.NumberFormat(getIntlLocale(), {
    minimumFractionDigits: 0,
    maximumFractionDigits: fractionDigits
  }).format(toNumber(value))
}

export function formatDateTime(value) {
  if (!value) {
    return t('formatNoTimestamp')
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return t('formatInvalidDate')
  }

  return new Intl.DateTimeFormat(getIntlLocale(), {
    dateStyle: 'medium',
    timeStyle: 'short'
  }).format(date)
}

export function formatDisplayDate(value) {
  if (!value) {
    return t('formatNoTimestamp')
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return t('formatInvalidDate')
  }

  return new Intl.DateTimeFormat(getIntlLocale(), {
    dateStyle: 'medium'
  }).format(date)
}

export function formatRelativeDate(value) {
  if (!value) {
    return t('formatNoTimestamp')
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return t('formatInvalidDate')
  }

  const diffMs = date.getTime() - Date.now()
  const diffDays = Math.round(diffMs / 86400000)
  const formatter = new Intl.RelativeTimeFormat(getIntlLocale(), { numeric: 'auto' })

  if (Math.abs(diffDays) < 1) {
    return formatter.format(0, 'day')
  }

  return formatter.format(diffDays, 'day')
}

export function formatShortDate(value) {
  if (!value) {
    return t('trendCurrent')
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return t('trendCurrent')
  }

  return new Intl.DateTimeFormat(getIntlLocale(), {
    month: 'short',
    day: 'numeric'
  }).format(date)
}