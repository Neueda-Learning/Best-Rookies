import axios from 'axios'

// 集中后端基础 URL，所以所有 API 调用都重用一个客户端
const api = axios.create({
  baseURL: 'http://localhost:8080/api/v1'
})

export default api

