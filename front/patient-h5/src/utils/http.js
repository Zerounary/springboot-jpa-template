import axios from 'axios'

const http = axios.create({
  baseURL: '',
  timeout: 15000,
})

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
      if (body.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('me')
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
      }
      const err = new Error(body.message || '请求失败')
      err.code = body.code
      throw err
    }
    return body
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('me')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    
    // 处理服务器返回的错误响应
    if (error.response?.data) {
      const errorData = error.response.data
      if (errorData && typeof errorData === 'object' && 'message' in errorData) {
        // 创建一个新的错误对象，包含服务器返回的message
        const err = new Error(errorData.message || '请求失败')
        err.code = errorData.code
        err.response = error.response
        throw err
      }
    }
    
    throw error
  },
)

export default http
