<template>
  <section class="page-section">
    <div class="detail-breadcrumbs">
      <button type="button" class="link-button" @click="goBack">{{ t('detailBreadcrumbHome') }}</button>
      <span>/</span>
      <span>{{ portfolio?.name || t('detailHeroFallbackTitle') }}</span>
    </div>

    <div class="page-heading page-heading--detail">
      <div>
        <button type="button" class="back-link" @click="goBack">&larr; {{ t('buttonBackToPortfolios') }}</button>
        <h2>{{ portfolio?.name || t('detailHeroFallbackTitle') }}</h2>
        <div v-if="portfolio" class="detail-heading__meta">
          <span>{{ portfolioCode }}</span>
          <span>{{ formatDisplayDate(portfolio.createdAt) }}</span>
          <span class="token-chip">{{ portfolio.baseCurrency }}</span>
        </div>
      </div>

      <div class="page-heading__actions">
        <button type="button" class="button button--secondary" :disabled="isLoading" @click="reload">
          {{ isLoading ? t('buttonRefreshing') : t('buttonRefreshPortfolio') }}
        </button>
      </div>
    </div>

    <StatusPanel
      v-if="isLoading"
      variant="loading"
      :title="t('statusLoadingPortfolioTitle')"
      :message="t('statusLoadingPortfolioMessage')"
    />

    <StatusPanel
      v-else-if="loadError"
      variant="error"
      :title="t('statusLoadPortfolioErrorTitle')"
      :message="loadError"
      :action-label="t('actionRetry')"
      @action="reload"
    />

    <template v-else-if="portfolio">
      <div class="summary-grid summary-grid--detail">
        <MetricCard :label="t('metricPositions')" :value="String(summary.totalPositions)" tone="indigo" badge="POS" :hint="t('metricPositionsHint')" />
        <MetricCard :label="t('metricInvestedCost')" :value="formatCurrency(summary.totalCost, portfolio.baseCurrency)" tone="mint" badge="USD" :hint="t('metricInvestedCostHint')" />
        <MetricCard :label="t('metricUniqueTickers')" :value="String(uniqueTickers)" tone="gold" badge="TK" :hint="t('metricUniqueTickersHint')" />
        <MetricCard :label="t('metricLastActivity')" :value="latestActivity" tone="silver" badge="ACT" :hint="t('metricLastActivityHint')" />
      </div>

      <div class="detail-layout-grid">
        <div class="card">
          <div class="section-heading">
            <div>
              <h3>{{ t('detailAssetAllocation') }}</h3>
              <p>{{ t('detailAssetAllocationDescription') }}</p>
            </div>
          </div>

          <StatusPanel
            v-if="!allocationSeries.length"
            variant="empty"
            :title="t('statusNoAllocationTitle')"
            :message="t('statusNoAllocationMessage')"
          />

          <AllocationDonutChart
            v-else
            :segments="allocationSeries"
            :total-value="summary.totalCost"
            :currency="portfolio.baseCurrency"
          />
        </div>

        <div class="card">
          <div class="section-heading">
            <div>
              <h3>{{ t('detailPerformanceBaseline') }}</h3>
              <p>{{ t('detailPerformanceDescription') }}</p>
            </div>
          </div>

          <StatusPanel
            v-if="!investedTrend.length"
            variant="empty"
            :title="t('statusNoTrendTitle')"
            :message="t('statusNoTrendMessage')"
          />

          <TrendLineChart v-else :points="investedTrend" :currency="portfolio.baseCurrency" />
        </div>

        <div class="detail-sidebar">
          <article class="card info-card">
            <p class="eyebrow">{{ t('detailAboutPortfolio') }}</p>
            <div class="info-list">
              <div class="info-list__row">
                <span>{{ t('fieldPortfolioName') }}</span>
                <strong>{{ portfolio.name }}</strong>
              </div>
              <div class="info-list__row">
                <span>{{ t('tableId') }}</span>
                <strong>{{ portfolioCode }}</strong>
              </div>
              <div class="info-list__row">
                <span>{{ t('fieldBaseCurrency') }}</span>
                <strong>{{ portfolio.baseCurrency }}</strong>
              </div>
              <div class="info-list__row">
                <span>{{ t('listCardCreated') }}</span>
                <strong>{{ formatDisplayDate(portfolio.createdAt) }}</strong>
              </div>
            </div>
          </article>

          <article class="card info-card">
            <p class="eyebrow">{{ t('detailDataNotes') }}</p>
            <ul class="note-list note-list--compact">
              <li>{{ t('detailNoteFormula') }}</li>
              <li>{{ t('detailNoteBaseCurrency') }}</li>
            </ul>
          </article>

          <article class="card info-card">
            <p class="eyebrow">{{ t('detailQuickActions') }}</p>
            <div class="quick-actions">
              <button type="button" class="button button--ghost button--block" @click="notifyPortfolioActionsUnavailable">
                {{ t('actionExportCsv') }}
              </button>
              <button type="button" class="button button--ghost button--block" @click="notifyPortfolioActionsUnavailable">
                {{ t('actionSharePortfolio') }}
              </button>
            </div>
          </article>
        </div>
      </div>

      <div class="card holdings-card">
        <div class="section-heading section-heading--tight">
          <div>
            <h3>{{ t('detailHoldingsTitle') }} <span class="count-badge">{{ positions.length }}</span></h3>
          </div>

          <button type="button" class="button button--ghost" @click="showPositionForm = !showPositionForm">
            + {{ t('buttonAddPosition') }}
          </button>
        </div>

        <div v-if="shouldShowPositionForm" class="holdings-card__composer">
          <PositionForm :portfolio-id="portfolioId" @created="handlePositionCreated" />
        </div>

        <StatusPanel
          v-if="!positions.length"
          variant="empty"
          :title="t('statusNoPositionsTitle')"
          :message="t('statusNoPositionsMessage')"
        />

        <div v-else class="table-wrapper">
          <table class="data-table">
            <thead>
              <tr>
                <th>{{ t('tableIndex') }}</th>
                <th>{{ t('tableAssetType') }}</th>
                <th>{{ t('fieldTicker') }}</th>
                <th>{{ t('tableQuantity') }}</th>
                <th>{{ t('tableAvgCost') }}</th>
                <th>{{ t('fieldCurrency') }}</th>
                <th>{{ t('tablePositionCost') }}</th>
                <th>{{ t('tableUpdated') }}</th>
                <th>{{ t('tableAction') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(pos, index) in positions" :key="pos.id">
                <td>{{ index + 1 }}</td>
                <td><span class="asset-pill">{{ formatAssetType(pos.assetType) }}</span></td>
                <td><strong>{{ pos.ticker }}</strong></td>
                <td>{{ formatNumber(pos.quantity, 4) }}</td>
                <td>{{ formatCurrency(pos.avgCost, pos.currency) }}</td>
                <td>{{ pos.currency }}</td>
                <td>{{ formatCurrency(calculatePositionCost(pos), pos.currency) }}</td>
                <td>{{ formatDisplayDate(pos.updatedAt) }}</td>
                <td>
                  <button
                    type="button"
                    class="button button--text-danger"
                    :disabled="Boolean(deletingPositionIds[pos.id])"
                    @click="removePosition(pos.id, pos.ticker)"
                  >
                    {{ deletingPositionIds[pos.id] ? t('buttonDeleting') : t('buttonDelete') }}
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
import {
  deletePosition,
  getPortfolio,
  getPortfolioSummary,
  listPositions
} from '../api/portfolio'
import AllocationDonutChart from '../components/AllocationDonutChart.vue'
import MetricCard from '../components/MetricCard.vue'
import PositionForm from '../components/PositionForm.vue'
import StatusPanel from '../components/StatusPanel.vue'
import TrendLineChart from '../components/TrendLineChart.vue'
import { useI18n } from '../composables/useI18n'
import { useToast } from '../composables/useToast'
import { formatCurrency, formatDisplayDate, formatNumber, formatRelativeDate } from '../utils/format'
import {
  buildAssetAllocation,
  buildInvestedTrend,
  calculatePositionCost,
  formatAssetType
} from '../utils/portfolio'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const { success, info, error: notifyError } = useToast()

const portfolio = ref(null)
const summary = ref({ totalPositions: 0, totalCost: 0 })
const positions = ref([])
const isLoading = ref(true)
const loadError = ref('')
const deletingPositionIds = reactive({})
const showPositionForm = ref(false)

// 从路由参数获取组合 ID
const portfolioId = computed(() => Number(route.params.id))
const isValidPortfolioId = computed(() => Number.isInteger(portfolioId.value) && portfolioId.value > 0)
const allocationSeries = computed(() => buildAssetAllocation(positions.value))
const investedTrend = computed(() => buildInvestedTrend(positions.value))
const uniqueTickers = computed(() => new Set(positions.value.map((position) => position.ticker).filter(Boolean)).size)
const latestActivity = computed(() => {
  const timestamps = positions.value
    .map((position) => position.updatedAt)
    .filter(Boolean)
    .sort((left, right) => new Date(right) - new Date(left))

  return timestamps.length ? formatRelativeDate(timestamps[0]) : t('noActivityYet')
})
const portfolioCode = computed(() => `PF-${String(portfolio.value?.id || portfolioId.value).padStart(3, '0')}`)
const shouldShowPositionForm = computed(() => showPositionForm.value || !positions.value.length)

// 重新加载组合、摘要和持仓数据
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
  } catch (error) {
    portfolio.value = null
    positions.value = []
    loadError.value = getApiErrorMessage(error, t('errorLoadPortfolioFallback'))
  } finally {
    isLoading.value = false
  }
}

<<<<<<< HEAD
// 删除持仓
async function removePosition(id) {
  await api.delete(`/positions/${id}`)
  await reload()
=======
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

function handlePositionCreated() {
  showPositionForm.value = false
  reload()
}

function goBack() {
  router.push({ name: 'portfolio-list' })
}

function notifyPortfolioActionsUnavailable() {
  info(t('toastBackendPendingTitle'), t('toastBackendPendingMessage'))
>>>>>>> 5bfe1c1 (Update frontend only)
}

// 挂载时加载数据
onMounted(reload)
watch(portfolioId, reload)
</script>

