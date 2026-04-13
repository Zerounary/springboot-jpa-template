import axios, { type AxiosError } from 'axios'
import { message } from 'ant-design-vue'
import type { ApiResponse } from './types'

function normalizeDateTimeString(value: string) {
  return value.replace(/^(\d{4}-\d{2}-\d{2})T(\d{2}:\d{2}:\d{2}(?:\.\d+)?)$/, '$1 $2')
}

function normalizeApiData<T>(value: T): T {
  if (Array.isArray(value)) {
    return value.map((item) => normalizeApiData(item)) as T
  }
  if (value && typeof value === 'object') {
    const entries = Object.entries(value as Record<string, unknown>).map(([k, v]) => [k, normalizeApiData(v)])
    return Object.fromEntries(entries) as T
  }
  if (typeof value === 'string') {
    return normalizeDateTimeString(value) as T
  }
  return value
}

export const tokenStorageKey = 'AUTH_TOKEN'

export function getToken(): string | null {
  return localStorage.getItem(tokenStorageKey)
}

export function setToken(token: string | null) {
  if (!token) {
    localStorage.removeItem(tokenStorageKey)
    return
  }
  localStorage.setItem(tokenStorageKey, token)
}

export const http = axios.create({
  baseURL: '',
  timeout: 15000,
})

http.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (resp) => resp,
  (err: AxiosError) => {
    const status = err.response?.status
    if (status === 401) {
      setToken(null)
    }
    return Promise.reject(err)
  },
)

export async function request<T>(p: Promise<{ data: ApiResponse<T> }>): Promise<T> {
  try {
    const resp = await p
    if (resp.data.code !== 0) {
      message.error(resp.data.message || '请求失败')
      throw new Error(resp.data.message || 'Request failed')
    }
    return normalizeApiData(resp.data.data)
  } catch (e) {
    const err = e as AxiosError<{ message?: string }>
    const msg = err.response?.data?.message
    if (msg) {
      message.error(msg)
    }
    throw e
  }
}
