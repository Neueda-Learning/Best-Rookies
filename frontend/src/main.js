// 此文件是前端应用启动入口，用于创建 Vue 实例并挂载路由与全局样式。
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './style.css'

// 创建并挂载 Vue 应用
createApp(App).use(router).mount('#app')

