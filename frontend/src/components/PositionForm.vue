<template>
  <div class="card">
    <h3>新增持仓</h3>
    <div class="row">
      <select v-model="form.assetType">
        <option value="STOCK">STOCK</option>
        <option value="BOND">BOND</option>
        <option value="CASH">CASH</option>
      </select>
      <input v-model="form.ticker" placeholder="Ticker, e.g. AAPL" />
      <input v-model.number="form.quantity" type="number" min="0.0001" step="0.0001" placeholder="Quantity" />
      <input v-model.number="form.avgCost" type="number" min="0" step="0.0001" placeholder="Avg Cost" />
      <input v-model="form.currency" placeholder="Currency" maxlength="3" />
      <button @click="submit">保存</button>
    </div>
    <p class="small">输入后会调用后端 `POST /api/v1/positions`。</p>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import api from '../api/client'

const props = defineProps({
  portfolioId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['created'])

// 持仓表单数据
const form = reactive({
  assetType: 'STOCK',
  ticker: '',
  quantity: 1,
  avgCost: 0,
  currency: 'USD'
})

// 提交表单创建持仓
async function submit() {
  await api.post('/positions', {
    portfolioId: props.portfolioId,
    assetType: form.assetType,
    ticker: form.ticker,
    quantity: form.quantity,
    avgCost: form.avgCost,
    currency: form.currency
  })
  // 重置表单
  form.ticker = ''
  form.quantity = 1
  form.avgCost = 0
  // 通知父组件已创建
  emit('created')
}
</script>

