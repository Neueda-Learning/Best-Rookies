<template>
  <section>
    <div class="card" v-if="portfolio">
      <h2>组合详情</h2>
      <p><strong>ID:</strong> {{ portfolio.id }}</p>
      <p><strong>名称:</strong> {{ portfolio.name }}</p>
      <p><strong>基础币种:</strong> {{ portfolio.baseCurrency }}</p>
      <p><strong>总持仓数:</strong> {{ summary.totalPositions }}</p>
      <p><strong>总成本:</strong> {{ summary.totalCost }}</p>
    </div>

    <PositionForm :portfolio-id="portfolioId" @created="reload" />

    <div class="card">
      <h3>持仓列表</h3>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>资产类型</th>
            <th>Ticker</th>
            <th>数量</th>
            <th>均价</th>
            <th>币种</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="pos in positions" :key="pos.id">
            <td>{{ pos.id }}</td>
            <td>{{ pos.assetType }}</td>
            <td>{{ pos.ticker }}</td>
            <td>{{ pos.quantity }}</td>
            <td>{{ pos.avgCost }}</td>
            <td>{{ pos.currency }}</td>
            <td>
              <button class="secondary" @click="removePosition(pos.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import api from '../api/client'
import PositionForm from '../components/PositionForm.vue'

const route = useRoute()
const portfolio = ref(null)
const summary = ref({ totalPositions: 0, totalCost: 0 })
const positions = ref([])

// 从路由参数获取组合 ID
const portfolioId = computed(() => Number(route.params.id))

// 重新加载组合、摘要和持仓数据
async function reload() {
  const [portfolioRes, summaryRes, positionsRes] = await Promise.all([
    api.get(`/portfolios/${portfolioId.value}`),
    api.get(`/portfolios/${portfolioId.value}/summary`),
    api.get('/positions', { params: { portfolioId: portfolioId.value } })
  ])

  portfolio.value = portfolioRes.data
  summary.value = summaryRes.data
  positions.value = positionsRes.data
}

// 删除持仓
async function removePosition(id) {
  await api.delete(`/positions/${id}`)
  await reload()
}

// 挂载时加载数据
onMounted(reload)
</script>

