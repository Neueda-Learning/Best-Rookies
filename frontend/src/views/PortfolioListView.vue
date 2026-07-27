<template>
  <section>
    <div class="card">
      <h2>组合列表</h2>
      <div class="row">
        <input v-model="form.name" placeholder="组合名称" />
        <input v-model="form.baseCurrency" maxlength="3" placeholder="基础币种" />
        <button @click="createPortfolio">创建组合</button>
      </div>
      <p class="small">输入后会调用后端 `POST /api/v1/portfolios`。</p>
    </div>

    <div class="card">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>名称</th>
            <th>基础币种</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in portfolios" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.name }}</td>
            <td>{{ item.baseCurrency }}</td>
            <td>
              <button class="secondary" @click="goDetail(item.id)">查看详情</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api/client'

const router = useRouter()
const portfolios = ref([])
const form = reactive({
  name: '',
  baseCurrency: 'USD'
})

// 从后端加载组合列表
async function loadPortfolios() {
  const { data } = await api.get('/portfolios')
  portfolios.value = data
}

// 创建新组合
async function createPortfolio() {
  await api.post('/portfolios', {
    name: form.name,
    baseCurrency: form.baseCurrency
  })
  form.name = ''
  await loadPortfolios()
}

// 导航到组合详情页
function goDetail(id) {
  router.push(`/portfolios/${id}`)
}

// 挂载时加载组合列表
onMounted(loadPortfolios)
</script>

