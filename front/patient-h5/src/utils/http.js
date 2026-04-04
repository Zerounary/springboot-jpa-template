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
    throw error
  },
)

export default http
