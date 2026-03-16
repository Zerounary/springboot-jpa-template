import { defineStore } from 'pinia'
import { getToken, setToken } from '../api/http'
import { loginApi, meApi, type UserDto } from '../api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getToken() as string | null,
    user: null as UserDto | null,
    loadingMe: false,
  }),
  actions: {
    async login(username: string, password: string) {
      const token = await loginApi({ username, password })
      this.token = token
      setToken(token)
      await this.fetchMe()
    },
    async fetchMe() {
      if (!this.token) {
        this.user = null
        return
      }
      this.loadingMe = true
      try {
        this.user = await meApi()
      } finally {
        this.loadingMe = false
      }
    },
    logout() {
      this.token = null
      this.user = null
      setToken(null)
    },
  },
})
