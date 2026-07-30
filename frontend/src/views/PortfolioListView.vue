<!-- 此文件是投资组合列表页，用于展示组合卡片并支持新建组合。 -->
<template>
  <section class="page-section">
    <div class="page-heading">
      <div>
        <h2>{{ t('topbarSectionList') }}</h2>
        <p class="page-copy">{{ t('listManageDescription') }}</p>
      </div>

      <div class="page-heading__actions">
        <button type="button" class="button button--secondary" :disabled="isLoading" @click="loadPortfolios">
          {{ isLoading ? t('buttonRefreshing') : t('buttonRefreshRegistry') }}
        </button>
        <button type="button" class="button button--primary" @click="toggleCreatePanel">
          + {{ t('buttonNewPortfolio') }}
        </button>
      </div>
    </div>

    <form v-if="showCreatePanel" class="card composer-card" novalidate @submit.prevent="handleCreatePortfolio">
      <div class="section-heading">
        <div>
          <p class="eyebrow">{{ t('listCreatePortfolioEyebrow') }}</p>
          <h3>{{ t('listCreatePortfolioTitle') }}</h3>
          <p>{{ t('listCreatePortfolioDescription') }}</p>
        </div>

        <button type="button" class="button button--ghost" @click="isCreatePanelOpen = false">
          {{ t('actionClose') }}
        </button>
      </div>

      <div class="form-grid form-grid--two-columns">
        <label class="field">
          <span class="field__label">{{ t('fieldPortfolioName') }}</span>
          <input
            v-model="form.name"
            type="text"
            maxlength="100"
            :placeholder="t('placeholderPortfolioName')"
            @blur="validateField('name')"
          />
          <small v-if="formErrors.name" class="field__error">{{ formErrors.name }}</small>
        </label>

        <label class="field">
          <span class="field__label">{{ t('fieldBaseCurrency') }}</span>
          <select v-model="form.baseCurrency" @blur="validateField('baseCurrency')">
            <option v-for="currency in supportedCurrencies" :key="currency" :value="currency">
              {{ currency }}
            </option>
          </select>
          <small v-if="formErrors.baseCurrency" class="field__error">{{ formErrors.baseCurrency }}</small>
        </label>
      </div>

      <p v-if="formError" class="form-alert">{{ formError }}</p>

      <div class="form-actions">
        <button type="submit" class="button button--primary" :disabled="isCreating">
          {{ isCreating ? t('buttonCreating') : t('buttonCreatePortfolio') }}
        </button>
      </div>
    </form>

    <div class="summary-grid summary-grid--list">
      <MetricCard
        :label="t('listActivePortfolios')"
        :value="String(portfolios.length)"
        tone="indigo"
        badge="PF"
        :hint="t('listMetricActiveHint')"
      />
      <MetricCard
        :label="t('listBaseCurrencies')"
        :value="currencySummary"
        tone="mint"
        badge="$"
        :hint="t('listMetricCurrenciesHint')"
      />
      <MetricCard
        :label="t('listLatestPortfolio')"
        :value="latestPortfolioName"
        tone="gold"
        badge="NEW"
        :hint="t('listMetricLatestHint')"
      />
    </div>

    <div class="card">
      <div class="section-heading">
        <div>
          <h3>{{ t('topbarSectionList') }}</h3>
          <p>{{ t('listPortfolioListDescription') }}</p>
        </div>
      </div>

      <StatusPanel
        v-if="isLoading"
        variant="loading"
        :title="t('statusLoadingPortfoliosTitle')"
        :message="t('statusLoadingPortfoliosMessage')"
      />

      <StatusPanel
        v-else-if="loadError"
        variant="error"
        :title="t('statusLoadPortfoliosErrorTitle')"
        :message="loadError"
        :action-label="t('actionRetry')"
        @action="loadPortfolios"
      />

      <StatusPanel
        v-else-if="!portfolios.length"
        variant="empty"
        :title="t('statusNoPortfoliosTitle')"
        :message="t('statusNoPortfoliosMessage')"
      />

      <div v-else class="portfolio-card-grid portfolio-card-grid--list">
        <article v-for="item in portfolioCards" :key="item.id" class="portfolio-card portfolio-card--workspace">
          <div class="portfolio-card__header">
            <div>
              <h3>{{ item.name }}</h3>
              <p class="portfolio-card__code">{{ item.code }}</p>
            </div>

            <span class="token-chip">{{ item.baseCurrency }}</span>
          </div>

          <div class="portfolio-card__facts">
            <div>
              <span class="portfolio-card__label">{{ t('listCardPositions') }}</span>
              <strong>{{ item.totalPositions }}</strong>
            </div>
            <div>
              <span class="portfolio-card__label">{{ t('listCardTotalCost') }}</span>
              <strong>{{ item.totalCostLabel }}</strong>
            </div>
            <div>
              <span class="portfolio-card__label">{{ t('listCardCreated') }}</span>
              <strong>{{ item.createdLabel }}</strong>
            </div>
            <div>
              <span class="portfolio-card__label">{{ t('listCardLastActivity') }}</span>
              <strong>{{ item.lastActivityLabel }}</strong>
            </div>
          </div>

          <div class="portfolio-card__footer">
            <button type="button" class="button button--primary button--block" @click="goDetail(item.id)">
              {{ t('buttonOpenWorkspace') }}
            </button>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getApiErrorMessage } from '../api/client'
import { createPortfolio, getPortfolioSummary, getSupportedCurrencies, listPortfolios, listPositions } from '../api/portfolio'
import MetricCard from '../components/MetricCard.vue'
import StatusPanel from '../components/StatusPanel.vue'
import { useI18n } from '../composables/useI18n'
import { useToast } from '../composables/useToast'
import { formatCurrency, formatDisplayDate, formatRelativeDate } from '../utils/format'

const router = useRouter()
const { t } = useI18n()
const { success, error: notifyError } = useToast()

const portfolios = ref([])
const isLoading = ref(true)
const isCreating = ref(false)
const loadError = ref('')
const formError = ref('')
const isCreatePanelOpen = ref(false)
const portfolioInsights = ref({})
const supportedCurrencies = ref(['USD', 'CNY', 'HKD', 'EUR', 'JPY'])

const form = reactive({
  name: '',
  baseCurrency: 'USD'
})

const formErrors = reactive({
  name: '',
  baseCurrency: ''
})

// 列表页直接拼出卡片所需摘要，避免额外引入只为展示存在的数据层。
const sortedPortfolios = computed(() => {
  return [...portfolios.value].sort((left, right) => new Date(right.createdAt || 0) - new Date(left.createdAt || 0))
})

const currencySummary = computed(() => {
  const currencies = [...new Set(portfolios.value.map((item) => item.baseCurrency).filter(Boolean))]
  return currencies.length ? currencies.join(' · ') : t('noDataYet')
})

const latestPortfolioName = computed(() => sortedPortfolios.value[0]?.name || t('createFirstPortfolio'))
const showCreatePanel = computed(() => isCreatePanelOpen.value || !portfolios.value.length || Boolean(formError.value))
const portfolioCards = computed(() => {
  return sortedPortfolios.value.map((item) => {
    const insight = portfolioInsights.value[item.id] || {}
    const summaryCurrency = insight.summaryCurrency || 'USD'

    return {
      ...item,
      code: `PF-${String(item.id).padStart(3, '0')}`,
      totalPositions: insight.totalPositions ?? 0,
      totalCostLabel: formatCurrency(insight.totalCost ?? 0, summaryCurrency),
      createdLabel: formatDisplayDate(item.createdAt),
      lastActivityLabel: insight.lastActivity ? formatRelativeDate(insight.lastActivity) : t('noActivityYet')
    }
  })
})

function normalizeCurrency(value) {
  return value.trim().toUpperCase()
}

async function loadSupportedCurrencyOptions() {
  try {
    const response = await getSupportedCurrencies()
    const currencies = Array.isArray(response.data)
      ? response.data.map((item) => normalizeCurrency(String(item || ''))).filter(Boolean)
      : []

    if (currencies.length) {
      supportedCurrencies.value = [...new Set(currencies)]
    }
  } catch {
    // 币种接口异常时回退到静态常用列表，避免创建表单无法使用。
  }

  if (!supportedCurrencies.value.includes(form.baseCurrency)) {
    form.baseCurrency = supportedCurrencies.value[0] || 'USD'
  }
}

function validateField(fieldName) {
  if (fieldName === 'name') {
    const name = form.name.trim()

    if (!name) {
      formErrors.name = t('validationPortfolioNameRequired')
      return false
    }

    if (name.length > 100) {
      formErrors.name = t('validationPortfolioNameTooLong')
      return false
    }

    formErrors.name = ''
    return true
  }

  if (fieldName === 'baseCurrency') {
    const baseCurrency = normalizeCurrency(form.baseCurrency)
    form.baseCurrency = baseCurrency

    if (!baseCurrency) {
      formErrors.baseCurrency = t('validationBaseCurrencyRequired')
      return false
    }

    if (!/^[A-Z]{3}$/.test(baseCurrency)) {
      formErrors.baseCurrency = t('validationBaseCurrencyFormat')
      return false
    }

    formErrors.baseCurrency = ''
    return true
  }

  return true
}

function validateForm() {
  const isNameValid = validateField('name')
  const isCurrencyValid = validateField('baseCurrency')
  return isNameValid && isCurrencyValid
}

async function loadPortfolios() {
  isLoading.value = true
  loadError.value = ''

  try {
    const { data } = await listPortfolios()
    portfolios.value = Array.isArray(data) ? data : []
    portfolioInsights.value = await buildPortfolioInsights(portfolios.value)
  } catch (error) {
    portfolios.value = []
    portfolioInsights.value = {}
    loadError.value = getApiErrorMessage(error, t('errorLoadPortfoliosFallback'))
  } finally {
    isLoading.value = false
  }
}

async function buildPortfolioInsights(items) {
  const entries = await Promise.all(
    items.map(async (item) => {
      try {
        const [summaryRes, positionsRes] = await Promise.all([
          getPortfolioSummary(item.id),
          listPositions(item.id)
        ])

        const positions = Array.isArray(positionsRes.data) ? positionsRes.data : []
        const lastActivity = positions
          .map((position) => position.updatedAt)
          .filter(Boolean)
          .sort((left, right) => new Date(right) - new Date(left))[0]

        return [item.id, {
          totalPositions: Number(summaryRes.data?.totalPositions || 0),
          totalCost: Number(summaryRes.data?.totalCost || 0),
          summaryCurrency: summaryRes.data?.baseCurrency || 'USD',
          lastActivity
        }]
      } catch {
        return [item.id, {
          totalPositions: 0,
          totalCost: 0,
          summaryCurrency: 'USD',
          lastActivity: ''
        }]
      }
    })
  )

  return Object.fromEntries(entries)
}

function toggleCreatePanel() {
  isCreatePanelOpen.value = !isCreatePanelOpen.value
}

async function handleCreatePortfolio() {
  formError.value = ''

  if (!validateForm()) {
    return
  }

  isCreating.value = true

  try {
    const payload = {
      name: form.name.trim(),
      baseCurrency: normalizeCurrency(form.baseCurrency)
    }

    await createPortfolio(payload)
    form.name = ''
    form.baseCurrency = supportedCurrencies.value[0] || 'USD'
    isCreatePanelOpen.value = false
    success(t('toastPortfolioCreatedTitle'), t('toastPortfolioCreatedMessage', { name: payload.name }))
    await loadPortfolios()
  } catch (error) {
    formError.value = getApiErrorMessage(error, t('errorCreatePortfolioFallback'))
    notifyError(t('toastCreateFailedTitle'), formError.value)
  } finally {
    isCreating.value = false
  }
}

// 导航到组合详情页
function goDetail(id) {
  router.push({ name: 'portfolio-detail', params: { id } })
}

// 挂载时先拉取币种选项，再加载组合列表，保证创建表单直接显示后端支持值。
onMounted(async () => {
  await loadSupportedCurrencyOptions()
  await loadPortfolios()
})
</script>

