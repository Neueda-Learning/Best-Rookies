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

