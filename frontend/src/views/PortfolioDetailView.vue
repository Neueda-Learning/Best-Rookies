<!-- 此文件是投资组合详情页，用于展示组合摘要、图表和持仓列表。 -->
<template>
  <section class="page-section">
    <div class="detail-breadcrumbs">
      <button type="button" class="link-button" @click="goBack">{{ t("detailBreadcrumbHome") }}</button>
      <span>/</span>
      <span>{{ portfolio?.name || t("detailHeroFallbackTitle") }}</span>
    </div>
    <div class="page-heading page-heading--detail">
      <div>
        <button type="button" class="back-link" @click="goBack">&larr; {{ t("buttonBackToPortfolios") }}</button>
        <h2>{{ portfolio?.name || t("detailHeroFallbackTitle") }}</h2>
        <div v-if="portfolio" class="detail-heading__meta">
          <span>{{ portfolioCode }}</span>
          <span>{{ formatDisplayDate(portfolio.createdAt) }}</span>
          <span class="token-chip">{{ portfolio.baseCurrency }}</span>
        </div>
      </div>
      <div class="page-heading__actions">
        <button type="button" class="button button--secondary" :disabled="isLoading" @click="reload">
          {{ isLoading ? t("buttonRefreshing") : t("buttonRefreshPortfolio") }}
        </button>
      </div>
    </div>
    <StatusPanel v-if="isLoading" variant="loading" :title="t('statusLoadingPortfolioTitle')" :message="t('statusLoadingPortfolioMessage')" />
    <StatusPanel v-else-if="loadError" variant="error" :title="t('statusLoadPortfolioErrorTitle')" :message="loadError" :action-label="t('actionRetry')" @action="reload" />
    <template v-else-if="portfolio">
      <div class="summary-grid summary-grid--detail">
        <MetricCard :label="t('metricPositions')" :value="String(summary.totalPositions)" tone="indigo" badge="POS" :hint="t('metricPositionsHint')" />
        <MetricCard :label="t('metricInvestedCost')" :value="formatCurrency(summary.totalCost, summaryBaseCurrency)" tone="mint" badge="COST" :hint="t('metricInvestedCostHint')" />
        <MetricCard :label="t('metricMarketValue')" :value="formatCurrency(summary.marketValue, summaryBaseCurrency)" tone="gold" badge="MKT" :hint="t('metricMarketValueHint')" />
        <MetricCard :label="t('metricUnrealizedPnl')" :value="formatCurrency(summary.unrealizedPnL, summaryBaseCurrency)" :tone="pnlTone" badge="PNL" :hint="t('metricUnrealizedPnlHint')" />
        <MetricCard :label="t('metricReturnRate')" :value="formatPercent(summary.returnRate)" :tone="pnlTone" badge="%" :hint="t('metricReturnRateHint')" />
        <MetricCard :label="t('metricLastActivity')" :value="latestActivity" tone="silver" badge="ACT" :hint="t('metricLastActivityHint')" />
      </div>
      <div class="detail-layout-grid">
        <div class="card detail-feature-card detail-feature-card--allocation stagger-card">
          <div class="section-heading"><div><h3>{{ t("detailAssetAllocation") }}</h3><p>{{ t("detailAssetAllocationDescription") }}</p></div></div>
          <StatusPanel v-if="!allocationSeries.length" variant="empty" :title="t('statusNoAllocationTitle')" :message="t('statusNoAllocationMessage')" />
          <AllocationDonutChart v-else :segments="allocationSeries" :total-value="summary.totalCost" :currency="summaryBaseCurrency" />
        </div>
        <div class="card detail-feature-card detail-feature-card--trend stagger-card">
          <div class="section-heading"><div><h3>{{ t("detailPerformanceBaseline") }}</h3><p>{{ t("detailPerformanceDescription") }}</p></div></div>
          <StatusPanel v-if="!investedTrend.length" variant="empty" :title="t('statusNoTrendTitle')" :message="t('statusNoTrendMessage')" />
          <TrendLineChart v-else :points="investedTrend" :currency="summaryBaseCurrency" />
        </div>
      </div>
      <div class="card holdings-card">
        <div class="section-heading section-heading--tight">
          <div><h3>{{ t("detailHoldingsTitle") }} <span class="count-badge">{{ positions.length }}</span></h3></div>
          <button type="button" class="button button--ghost" @click="showPositionForm = !showPositionForm">+ {{ t("buttonAddPosition") }}</button>
        </div>
        <div v-if="shouldShowPositionForm" class="holdings-card__composer">
          <PositionForm :portfolio-id="portfolioId" @created="handlePositionCreated" />
        </div>
        <StatusPanel v-if="!positions.length" variant="empty" :title="t('statusNoPositionsTitle')" :message="t('statusNoPositionsMessage')" />
        <div v-else class="table-wrapper">
          <table class="data-table">
            <thead>
              <tr>
                <th>{{ t("tableIndex") }}</th><th>{{ t("tableAssetType") }}</th><th>{{ t("fieldTicker") }}</th>
                <th>{{ t("fieldCurrency") }}</th><th>{{ t("tableInvestedCost") }}</th>
                <th>{{ t("tablePositionCost") }}</th><th>{{ t("tableUpdated") }}</th><th>{{ t("tableAction") }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(pos, index) in positions" :key="pos.id">
                <td>{{ index + 1 }}</td>
                <td><span class="asset-pill">{{ formatAssetType(pos.assetType) }}</span></td>
                <td><strong>{{ pos.ticker }}</strong></td>
                <td>{{ pos.currency }}</td>
                <td>{{ formatCurrency(calculatePositionCost(pos), pos.currency) }}</td>
                <td>{{ formatCurrency(calculatePositionCost(pos), pos.currency) }}</td>
                <td>{{ formatDisplayDate(pos.updatedAt) }}</td>
                <td>
                  <button type="button" class="button button--text-danger" :disabled="Boolean(deletingPositionIds[pos.id])" @click="removePosition(pos.id, pos.ticker)">
                    {{ deletingPositionIds[pos.id] ? t("buttonDeleting") : t("buttonDelete") }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </section>
</template>
<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getApiErrorMessage } from '../api/client'
import { deletePosition, getExchangeRates, getPortfolio, getPortfolioSummary, listPositions } from '../api/portfolio'
import AllocationDonutChart from '../components/AllocationDonutChart.vue'
import MetricCard from '../components/MetricCard.vue'
import PositionForm from '../components/PositionForm.vue'
import StatusPanel from '../components/StatusPanel.vue'
import TrendLineChart from '../components/TrendLineChart.vue'
import { useI18n } from '../composables/useI18n'
import { useToast } from '../composables/useToast'
import { formatCurrency, formatDisplayDate, formatNumber, formatPercent, formatRelativeDate } from '../utils/format'
import { buildAssetAllocation, buildInvestedTrend, calculatePositionCost, formatAssetType } from '../utils/portfolio'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const { success, error: notifyError } = useToast()
const portfolio = ref(null)
const summary = ref({ totalPositions: 0, totalCost: 0, marketValue: 0, unrealizedPnL: 0, returnRate: 0, positionsWithLivePrice: 0, positionsWithFallback: 0, baseCurrency: '' })
const positions = ref([])
const ratesByCurrency = ref({})
const isLoading = ref(true)
const loadError = ref('')
const deletingPositionIds = reactive({})
const showPositionForm = ref(false)
const portfolioId = computed(() => Number(route.params.id))
const isValidPortfolioId = computed(() => Number.isInteger(portfolioId.value) && portfolioId.value > 0)
const allocationSeries = computed(() => buildAssetAllocation(positions.value, summaryBaseCurrency.value, ratesByCurrency.value))
const investedTrend = computed(() => buildInvestedTrend(positions.value, summaryBaseCurrency.value, ratesByCurrency.value))
// 基础币种：优先使用 summary 返回值，兜底 portfolio.baseCurrency
const summaryBaseCurrency = computed(() => summary.value.baseCurrency || portfolio.value?.baseCurrency || 'USD')
// 盈亏色调：正绿负红持平灰
const pnlTone = computed(() => {
  const r = Number(summary.value.returnRate)
  if (r > 0) return 'mint'
  if (r < 0) return 'rose'
  return 'silver'
})
const latestActivity = computed(() => {
  const ts = positions.value.map((p) => p.updatedAt).filter(Boolean).sort((a, b) => new Date(b) - new Date(a))
  return ts.length ? formatRelativeDate(ts[0]) : t('noActivityYet')
})

// 详情页中的组合编号统一转成 PF-001 这样的展示格式，避免模板里重复拼接。
const portfolioCode = computed(() => `PF-${String(portfolio.value?.id || portfolioId.value).padStart(3, '0')}`)
const shouldShowPositionForm = computed(() => showPositionForm.value || !positions.value.length)

async function reload() {
  if (!isValidPortfolioId.value) {
    isLoading.value = false
    loadError.value = t('errorInvalidPortfolioId')
    portfolio.value = null
    positions.value = []
    return
  }
  isLoading.value = true
  loadError.value = ''
  try {
    const [portfolioRes, summaryRes, positionsRes] = await Promise.all([
      getPortfolio(portfolioId.value),
      getPortfolioSummary(portfolioId.value),
      listPositions(portfolioId.value)
    ])
    portfolio.value = portfolioRes.data
    summary.value = summaryRes.data
    positions.value = Array.isArray(positionsRes.data) ? positionsRes.data : []

    const baseCurrency = summary.value.baseCurrency || portfolio.value?.baseCurrency || 'USD'
    const fromCurrencies = [...new Set(positions.value.map((p) => p?.currency).filter(Boolean))]
      .filter((currency) => String(currency).toUpperCase() !== String(baseCurrency).toUpperCase())

    if (!fromCurrencies.length) {
      ratesByCurrency.value = {}
    } else {
      try {
        const ratesRes = await getExchangeRates(baseCurrency, fromCurrencies)
        ratesByCurrency.value = ratesRes?.data && typeof ratesRes.data === 'object' ? ratesRes.data : {}
      } catch {
        // 汇率接口失败时图表回退使用 1:1，避免页面不可用。
        ratesByCurrency.value = {}
      }
    }
  } catch (error) {
    portfolio.value = null
    positions.value = []
    ratesByCurrency.value = {}
    loadError.value = getApiErrorMessage(error, t('errorLoadPortfolioFallback'))
  } finally {
    isLoading.value = false
  }
}

async function removePosition(id, ticker) {
  deletingPositionIds[id] = true
  try {
    await deletePosition(id)
    success(t('toastPositionDeletedTitle'), t('toastPositionDeletedMessage', { ticker }))
    await reload()
  } catch (error) {
    notifyError(t('toastDeleteFailedTitle'), getApiErrorMessage(error, t('errorDeletePositionFallback')))
  } finally {
    delete deletingPositionIds[id]
  }
}

function handlePositionCreated() { showPositionForm.value = false; reload() }
function goBack() { router.push({ name: 'portfolio-list' }) }

onMounted(reload)
watch(portfolioId, reload)
</script>
