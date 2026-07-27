import axios from 'axios'

// Centralize backend base URL so all API calls reuse one client.
const api = axios.create({
  baseURL: 'http://localhost:8080/api/v1'
})

export default api

