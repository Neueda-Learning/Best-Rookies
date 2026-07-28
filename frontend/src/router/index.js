// 此文件是前端路由配置，用于定义页面地址、跳转规则和视图组件映射。
import { createRouter, createWebHistory } from 'vue-router'
import PortfolioListView from '../views/PortfolioListView.vue'
import PortfolioDetailView from '../views/PortfolioDetailView.vue'

// 定义所有路由
const routes = [
  { path: '/', redirect: '/portfolios' },
  {
    path: '/portfolios',
    name: 'portfolio-list',
    component: PortfolioListView
  },
  {
    path: '/portfolios/:id',
    name: 'portfolio-detail',
    component: PortfolioDetailView,
    props: true
  }
]

// 创建路由器
const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

