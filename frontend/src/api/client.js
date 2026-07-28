// 此文件是 API 客户端封装，用于统一配置后端请求实例和错误消息处理。
import axios from 'axios'
import { t } from '../composables/useI18n'


// 集中后端基础 URL，所以所有 API 调用都重用一个客户端

// Centralize backend configuration so every request shares the same base URL and timeout.

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  timeout: 10000,
  headers: {
    Accept: 'application/json'
  }
})

// Convert backend and network failures into messages that are safe to show in the UI.
export function getApiErrorMessage(error, fallbackMessage = t('apiRequestFailed')) {
  if (error?.response?.data?.message) {
    return error.response.data.message
  }

  if (error?.code === 'ECONNABORTED') {
    return t('apiRequestTimeout')
  }

  if (error?.message === 'Network Error') {
    return t('apiNetworkError')
  }

  return fallbackMessage
}

export default api

