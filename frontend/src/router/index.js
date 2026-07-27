import { createRouter, createWebHistory } from 'vue-router'
import PortfolioListView from '../views/PortfolioListView.vue'
import PortfolioDetailView from '../views/PortfolioDetailView.vue'

// 定义所有路由
const routes = [
  { path: '/', redirect: '/portfolios' },
  { path: '/portfolios', component: PortfolioListView },
  { path: '/portfolios/:id', component: PortfolioDetailView, props: true }
]

// 创建路由器
const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

