// 此文件是 Vite 构建配置，用于定义 Vue 插件和本地开发服务器参数。
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Vite 前端开发和构建配置
export default defineConfig({
  plugins: [vue()],
  server: {
    // 明确绑定到 IPv4 环回地址
    host: '127.0.0.1',
    port: 5173
  }
})

