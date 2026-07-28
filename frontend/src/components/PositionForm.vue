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
        <select v-model="form.assetType" @blur="validateField('assetType')">
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
          type="text"
          maxlength="12"
          :placeholder="t('placeholderTicker')"
          @input="form.ticker = form.ticker.toUpperCase().trimStart()"
          @blur="validateField('ticker')"
        />
        <small v-if="errors.ticker" class="field__error">{{ errors.ticker }}</small>
      </label>

      <label class="field">
        <span class="field__label">{{ t('fieldQuantity') }}</span>
        <input
          v-model.number="form.quantity"
          type="number"
          min="0.0001"
          step="0.0001"
          :placeholder="t('placeholderQuantity')"
          @blur="validateField('quantity')"
        />
        <small v-if="errors.quantity" class="field__error">{{ errors.quantity }}</small>
      </label>

      <label class="field">
        <span class="field__label">{{ t('fieldAverageCost') }}</span>
        <input
          v-model.number="form.avgCost"
          type="number"
          min="0"
          step="0.0001"
          :placeholder="t('placeholderAverageCost')"
          @blur="validateField('avgCost')"
        />
        <small v-if="errors.avgCost" class="field__error">{{ errors.avgCost }}</small>
      </label>

      <label class="field">
        <span class="field__label">{{ t('fieldCurrency') }}</span>
        <input
          v-model="form.currency"
          type="text"
          maxlength="3"
          placeholder="USD"
          @input="form.currency = normalizeCurrency(form.currency)"
          @blur="validateField('currency')"
        />
        <small v-if="errors.currency" class="field__error">{{ errors.currency }}</small>
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
import { reactive, ref } from 'vue'
import { getApiErrorMessage } from '../api/client'
import { useI18n } from '../composables/useI18n'
import { createPosition } from '../api/portfolio'
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

// 持仓表单数据
const form = reactive({
  assetType: 'STOCK',
  ticker: '',
  quantity: 1,
  avgCost: 0,
  currency: 'USD'
})

const errors = reactive({
  assetType: '',
  ticker: '',
  quantity: '',
  avgCost: '',
  currency: ''
})

// 这里直接镜像后端枚举，避免前端表单可选值落后于最新 API 契约。
const assetTypeOptions = [
  { value: 'STOCK', labelKey: 'assetTypeStock' },
  { value: 'BOND', labelKey: 'assetTypeBond' },
  { value: 'CASH', labelKey: 'assetTypeCash' },
  { value: 'ETF', labelKey: 'assetTypeEtf' },
  { value: 'FUND', labelKey: 'assetTypeFund' },
  { value: 'CRYPTO', labelKey: 'assetTypeCrypto' }
]

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

  if (fieldName === 'quantity') {
    if (form.quantity === null || form.quantity === undefined || form.quantity === '') {
      errors.quantity = t('validationQuantityRequired')
      return false
    }

    if (Number(form.quantity) < 0.0001) {
      errors.quantity = t('validationQuantityMin')
      return false
    }

    errors.quantity = ''
    return true
  }

  if (fieldName === 'avgCost') {
    if (form.avgCost === null || form.avgCost === undefined || form.avgCost === '') {
      errors.avgCost = t('validationAverageCostRequired')
      return false
    }

    if (Number(form.avgCost) < 0) {
      errors.avgCost = t('validationAverageCostNegative')
      return false
    }

    errors.avgCost = ''
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

  return true
}

function validateForm() {
  const fields = ['assetType', 'ticker', 'quantity', 'avgCost', 'currency']
  return fields.every((fieldName) => validateField(fieldName))
}

function resetForm() {
  form.assetType = 'STOCK'
  form.ticker = ''
  form.quantity = 1
  form.avgCost = 0
  form.currency = 'USD'
  submitError.value = ''
}

async function submit() {
  submitError.value = ''

  if (!validateForm()) {
    return
  }

  isSubmitting.value = true

  try {
    const payload = {
      portfolioId: props.portfolioId,
      assetType: form.assetType,
      ticker: form.ticker.trim().toUpperCase(),
      quantity: Number(form.quantity),
      avgCost: Number(form.avgCost),
      currency: normalizeCurrency(form.currency)
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
</script>

