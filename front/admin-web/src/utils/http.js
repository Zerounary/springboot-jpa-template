import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: '',
  timeout: 15000,
})

function showError(message) {
  if (message) {
    ElMessage.error(message)
  }
}

function handleUnauthorized(message) {
  localStorage.removeItem('token')
  localStorage.removeItem('me')
  if (window.location.pathname !== '/login') {
    if (message) {
      showError(message)
    }
    window.location.href = '/login'
    return
  }
  if (message) {
    showError(message)
  }
}

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (resp) => {
    const body = resp?.data
    if (body && typeof body === 'object' && 'code' in body && 'message' in body) {
      if (body.code === 0) {
        return body.data
      }
      const message = body.message || '请求失败'
      if (body.code === 401) {
        handleUnauthorized(message)
      } else {
        showError(message)
      }
      const err = new Error(message)
      err.code = body.code
      throw err
    }
    return body
  },
  (error) => {
    const status = error?.response?.status
    const body = error?.response?.data
    const message = body?.message || body?.msg || error?.message || (status ? `请求失败(${status})` : '网络异常，请稍后重试')

    if (status === 401) {
      handleUnauthorized(message)
      throw error
    }

    showError(message)
    throw error
  },
)

export default http
