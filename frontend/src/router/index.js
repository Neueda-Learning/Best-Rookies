import { createRouter, createWebHistory } from 'vue-router'
import PortfolioListView from '../views/PortfolioListView.vue'
import PortfolioDetailView from '../views/PortfolioDetailView.vue'

const routes = [
  { path: '/', redirect: '/portfolios' },
  { path: '/portfolios', component: PortfolioListView },
  { path: '/portfolios/:id', component: PortfolioDetailView, props: true }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

