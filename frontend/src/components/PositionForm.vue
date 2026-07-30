<!-- 此文件是新增持仓表单组件，用于处理输入校验、提交和成功提示。 -->
<template>
  <form class="position-form card" novalidate @submit.prevent="submit">
    <div class="section-heading">
      <div>
        <h3>{{ t('positionFormTitle') }}</h3>
        <p>{{ t('positionFormDescription') }}</p>
      </div>
    </div>

    <div class="form-grid">
      <label class="field">
        <span class="field__label">{{ t('fieldAssetType') }}</span>
        <select v-model="form.assetType" @change="onAssetTypeChange" @blur="validateField('assetType')">
          <option v-for="option in assetTypeOptions" :key="option.value" :value="option.value">
            {{ t(option.labelKey) }}
          </option>
        </select>
        <small v-if="errors.assetType" class="field__error">{{ errors.assetType }}</small>
      </label>

      <label class="field">
        <span class="field__label">{{ t('fieldTicker') }}</span>
        <input
          v-model="form.ticker"
          list="ticker-suggestions"
          type="text"
          maxlength="12"
          :placeholder="t('placeholderTicker')"
          @input="onTickerInput"
          @blur="validateField('ticker')"
        />
        <datalist id="ticker-suggestions">
          <option v-for="ticker in tickerSuggestions" :key="ticker" :value="ticker" />
        </datalist>
        <small v-if="isTickerLoading">{{ t('labelTickerSearching') }}</small>
        <small v-if="errors.ticker" class="field__error">{{ errors.ticker }}</small>
      </label>

      <label class="field">
        <span class="field__label">{{ t('fieldInvestedCost') }}</span>
        <input
          v-model.number="form.investedCost"
          type="number"
          min="0.0001"
          step="0.0001"
          :placeholder="t('placeholderInvestedCost')"
          @blur="validateField('investedCost')"
        />
        <small v-if="errors.investedCost" class="field__error">{{ errors.investedCost }}</small>
      </label>

      <label class="field">
        <span class="field__label">{{ t('fieldCurrency') }}</span>
        <select v-model="form.currency" @blur="validateField('currency')">
          <option v-for="currency in currencyOptions" :key="currency" :value="currency">
            {{ currency }}
          </option>
        </select>
        <small v-if="errors.currency" class="field__error">{{ errors.currency }}</small>
      </label>

      <label class="field">
        <span class="field__label">{{ t('fieldUpdatedAt') }}</span>
        <input
          v-model="form.updatedAt"
          type="date"
          @blur="validateField('updatedAt')"
        />
        <small v-if="errors.updatedAt" class="field__error">{{ errors.updatedAt }}</small>
      </label>
    </div>

    <p v-if="submitError" class="form-alert">{{ submitError }}</p>

    <div class="form-actions">
      <button type="submit" class="button button--primary" :disabled="isSubmitting">
        {{ isSubmitting ? t('buttonSavingPosition') : t('buttonSavePosition') }}
      </button>
    </div>
  </form>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { getApiErrorMessage } from '../api/client'
import { useI18n } from '../composables/useI18n'
import {
  createPosition,
  getSupportedAssetTypes,
  getSupportedCurrencies,
  getSupportedTickersByAssetType,
  searchSupportedTickers
} from '../api/portfolio'
import { useToast } from '../composables/useToast'

const props = defineProps({
  portfolioId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['created'])
const { t } = useI18n()
const { success, error: notifyError } = useToast()

const isSubmitting = ref(false)
const submitError = ref('')
const isTickerLoading = ref(false)
const tickerSuggestions = ref([])
const assetTypeOptions = ref([])
const currencyOptions = ref(['USD', 'CNY', 'HKD', 'EUR', 'JPY'])
let tickerSearchDebounceId = null

// 持仓表单数据
const form = reactive({
  assetType: 'STOCK',
  ticker: '',
  investedCost: null,
  currency: 'USD',
  updatedAt: getTodayDateInputValue()
})

const errors = reactive({
  assetType: '',
  ticker: '',
  investedCost: '',
  currency: '',
  updatedAt: ''
})

function getTodayDateInputValue() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function buildUpdatedAtIso(dateValue) {
  return `${dateValue}T12:00:00Z`
}

function mapAssetTypeLabelKey(assetType) {
  if (assetType === 'STOCK') return 'assetTypeStock'
  if (assetType === 'BOND') return 'assetTypeBond'
  if (assetType === 'CASH') return 'assetTypeCash'
  if (assetType === 'ETF') return 'assetTypeEtf'
  if (assetType === 'FUND') return 'assetTypeFund'
  if (assetType === 'CRYPTO') return 'assetTypeCrypto'
  return 'assetTypeUnknown'
}

async function loadAssetTypeOptions() {
  try {
    const response = await getSupportedAssetTypes()
    const raw = Array.isArray(response.data) ? response.data : []
    assetTypeOptions.value = raw.map((value) => ({ value, labelKey: mapAssetTypeLabelKey(value) }))
    if (!assetTypeOptions.value.some((opt) => opt.value === form.assetType) && assetTypeOptions.value.length) {
      form.assetType = assetTypeOptions.value[0].value
    }
  } catch {
    // 回退静态枚举，保证离线时表单仍可用。
    assetTypeOptions.value = [
      { value: 'STOCK', labelKey: 'assetTypeStock' },
      { value: 'BOND', labelKey: 'assetTypeBond' },
      { value: 'CASH', labelKey: 'assetTypeCash' },
      { value: 'ETF', labelKey: 'assetTypeEtf' },
      { value: 'FUND', labelKey: 'assetTypeFund' },
      { value: 'CRYPTO', labelKey: 'assetTypeCrypto' }
    ]
  }
}

async function loadCurrencyOptions() {
  try {
    const response = await getSupportedCurrencies()
    const currencies = Array.isArray(response.data)
      ? response.data.map((item) => normalizeCurrency(String(item || ''))).filter(Boolean)
      : []

    if (currencies.length) {
      currencyOptions.value = [...new Set(currencies)]
    }
  } catch {
    // 币种接口不可用时保留常用币种回退值，保证表单依然可提交。
  }

  if (!currencyOptions.value.includes(form.currency)) {
    form.currency = currencyOptions.value[0] || 'USD'
  }
}

async function loadTickerSuggestions(query = '') {
  if (!form.assetType) {
    tickerSuggestions.value = []
    return
  }

  isTickerLoading.value = true
  try {
    const response = query
      ? await searchSupportedTickers(form.assetType, query, 20)
      : await getSupportedTickersByAssetType(form.assetType)
    tickerSuggestions.value = Array.isArray(response.data) ? response.data : []
  } catch {
    tickerSuggestions.value = []
  } finally {
    isTickerLoading.value = false
  }
}

function onAssetTypeChange() {
  form.ticker = ''
  validateField('assetType')
  loadTickerSuggestions('')
}

function onTickerInput(event) {
  const value = String(event?.target?.value ?? '').toUpperCase().trimStart()
  form.ticker = value

  if (tickerSearchDebounceId) {
    clearTimeout(tickerSearchDebounceId)
  }
  tickerSearchDebounceId = setTimeout(() => {
    loadTickerSuggestions(value.trim())
  }, 250)
}

function normalizeCurrency(value) {
  return value.trim().toUpperCase()
}

// Mirror backend DTO constraints in one place so the form and API fail for the same reasons.
function validateField(fieldName) {
  if (fieldName === 'assetType') {
    errors.assetType = form.assetType ? '' : t('validationAssetTypeRequired')
    return !errors.assetType
  }

  if (fieldName === 'ticker') {
    const ticker = form.ticker.trim().toUpperCase()
    form.ticker = ticker

    if (!ticker) {
      errors.ticker = t('validationTickerRequired')
      return false
    }

    if (ticker.length > 12) {
      errors.ticker = t('validationTickerTooLong')
      return false
    }

    errors.ticker = ''
    return true
  }

  if (fieldName === 'investedCost') {
    if (form.investedCost === null || form.investedCost === undefined || form.investedCost === '') {
      errors.investedCost = t('validationInvestedCostRequired')
      return false
    }

    if (Number(form.investedCost) < 0.0001) {
      errors.investedCost = t('validationInvestedCostMin')
      return false
    }

    errors.investedCost = ''
    return true
  }

  if (fieldName === 'currency') {
    const currency = normalizeCurrency(form.currency)
    form.currency = currency

    if (!currency) {
      errors.currency = t('validationCurrencyRequired')
      return false
    }

    if (!/^[A-Z]{3}$/.test(currency)) {
      errors.currency = t('validationCurrencyFormat')
      return false
    }

    errors.currency = ''
    return true
  }

  if (fieldName === 'updatedAt') {
    if (!form.updatedAt) {
      errors.updatedAt = t('validationUpdatedAtRequired')
      return false
    }

    errors.updatedAt = ''
    return true
  }

  return true
}

function validateForm() {
  const fields = ['assetType', 'ticker', 'investedCost', 'currency', 'updatedAt']
  return fields.every((fieldName) => validateField(fieldName))
}

function resetForm() {
  form.assetType = 'STOCK'
  form.ticker = ''
  form.investedCost = null
  form.currency = currencyOptions.value[0] || 'USD'
  form.updatedAt = getTodayDateInputValue()
  loadTickerSuggestions('')
  submitError.value = ''
}

async function submit() {
  submitError.value = ''

  if (!validateForm()) {
    return
  }

  isSubmitting.value = true

  try {
    const investedCost = Number(form.investedCost)
    const payload = {
      portfolioId: props.portfolioId,
      assetType: form.assetType,
      ticker: form.ticker.trim().toUpperCase(),
      // 后端当前仍使用 quantity × avgCost 计算持仓成本，这里固定 quantity=1，
      // 并把用户输入的投入成本映射到 avgCost，从而让整条成本链保持正确。
      quantity: 1,
      avgCost: investedCost,
      currency: normalizeCurrency(form.currency),
      // 日期控件只让用户选日，因此这里统一写入当天中午 UTC，避免展示时跨时区偏移到前一天。
      updatedAt: buildUpdatedAtIso(form.updatedAt)
    }

    await createPosition(payload)
    resetForm()
    success(t('toastPositionSavedTitle'), t('toastPositionSavedMessage', { ticker: payload.ticker }))
    emit('created')
  } catch (error) {
    submitError.value = getApiErrorMessage(error, t('errorSavePositionFallback'))
    notifyError(t('toastSaveFailedTitle'), submitError.value)
  } finally {
    isSubmitting.value = false
  }
}

onMounted(async () => {
  await loadCurrencyOptions()
  await loadAssetTypeOptions()
  await loadTickerSuggestions('')
})
</script>

